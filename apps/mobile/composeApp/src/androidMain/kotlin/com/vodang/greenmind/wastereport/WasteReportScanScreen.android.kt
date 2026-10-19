package com.vodang.greenmind.wastereport

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import android.graphics.BitmapFactory
import com.vodang.greenmind.api.nominatim.ReverseOptions
import com.vodang.greenmind.api.nominatim.nominatimReverse
import com.vodang.greenmind.api.upload.requestAndUpload
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.location.Geo
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.permission.CameraPermissionGate
import com.vodang.greenmind.util.AppLogger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF388E3C)
private val green50  = Color(0xFFE8F5E9)
private val red      = Color(0xFFC62828)

private enum class WasteReportScanPhase { IDLE, FORM, SUBMITTING }
private enum class UploadState { UPLOADING, DONE, ERROR }

private data class CapturedPhoto(
    val bytes: ByteArray,
    val state: UploadState = UploadState.UPLOADING,
    val key: String? = null,
    val imageUrl: String? = null,
)

private fun createWastePhotoUri(context: android.content.Context): Uri {
    val dir  = File(context.cacheDir, "camera_photos").also { it.mkdirs() }
    val file = File(dir, "waste_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
actual fun WasteReportScanScreen(
    onStartSubmit: (WasteReportFormData) -> Unit,
    onBack: () -> Unit,
    launchCamera: Boolean,
    onSubmitDone: () -> Unit,
    isSubmitting: Boolean,
) {
    CameraPermissionGate(onDenied = onBack) {
        WasteReportScanContent(onStartSubmit = onStartSubmit, onBack = onBack, launchCamera = launchCamera, onSubmitDone = onSubmitDone, isSubmitting = isSubmitting)
    }
}

@Composable
private fun WasteReportScanContent(
    onStartSubmit: (WasteReportFormData) -> Unit,
    onBack: () -> Unit,
    launchCamera: Boolean,
    onSubmitDone: () -> Unit,
    isSubmitting: Boolean,
) {
    val s       = LocalAppStrings.current
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var phase       by remember { mutableStateOf(WasteReportScanPhase.IDLE) }
    var pendingUri  by remember { mutableStateOf<Uri?>(null) }
    val photos      = remember { mutableStateListOf<CapturedPhoto>() }

    // Form fields
    var description       by remember { mutableStateOf("") }
    var wardName          by remember { mutableStateOf("") }
    var currentLat        by remember { mutableStateOf(0.0) }
    var currentLng        by remember { mutableStateOf(0.0) }
    var isLoadingLocation by remember { mutableStateOf(false) }
    var error             by remember { mutableStateOf<String?>(null) }

    // ── Helper: add photo + kick off upload immediately ───────────────────────
    fun addAndUpload(bytes: ByteArray) {
        val idx = photos.size
        photos.add(CapturedPhoto(bytes = bytes))
        scope.launch {
            val token = SettingsStore.getAccessToken()
            if (token == null) {
                photos[idx] = photos[idx].copy(state = UploadState.ERROR)
                return@launch
            }
            try {
                val filename = "waste_${System.currentTimeMillis()}.jpg"
                val result   = requestAndUpload(token, filename, bytes, "image/jpeg")
                AppLogger.i("WasteReport", "Uploaded idx=$idx key=${result.key}")
                photos[idx] = photos[idx].copy(
                    state    = UploadState.DONE,
                    key      = result.key,
                    imageUrl = result.imageUrl,
                )
            } catch (e: Throwable) {
                AppLogger.e("WasteReport", "Upload failed idx=$idx: ${e.message}")
                photos[idx] = photos[idx].copy(state = UploadState.ERROR)
            }
        }
    }

    // ── Camera launcher (single; used for first + additional photos) ──────────
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { saved ->
        if (saved) {
            val uri = pendingUri
            if (uri != null) {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    addAndUpload(bytes)
                    if (phase == WasteReportScanPhase.IDLE) phase = WasteReportScanPhase.FORM
                }
            }
        } else if (phase == WasteReportScanPhase.IDLE) {
            onBack()
        }
        // if in FORM and user cancelled → just stay in FORM with existing photos
    }

    // ── Gallery launcher ─────────────────────────────────────────────────────
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    addAndUpload(bytes)
                    if (phase == WasteReportScanPhase.IDLE) phase = WasteReportScanPhase.FORM
                }
            } catch (e: Throwable) {
                AppLogger.e("WasteReport", "Gallery read failed: ${e.message}")
            }
        }
    }

    // ── Helper: launch camera ─────────────────────────────────────────────────
    fun launchCamera() {
        val uri = createWastePhotoUri(context)
        pendingUri = uri
        cameraLauncher.launch(uri)
    }

    when (phase) {
        // ── IDLE: auto-open camera or gallery on first entry ───────────────────
        WasteReportScanPhase.IDLE -> {
            LaunchedEffect(Unit) {
                if (launchCamera) launchCamera() else galleryLauncher.launch("image/*")
            }
            Box(modifier = Modifier.fillMaxSize().background(green50))
        }

        // ── FORM: detail form + photo strip ───────────────────────────────────
        WasteReportScanPhase.FORM -> {

            // Auto-fill ward name from GPS (runs once)
            LaunchedEffect(Unit) {
                isLoadingLocation = true
                try {
                    val loc = withTimeoutOrNull(5_000L) { Geo.service.locationUpdates.first() }
                    if (loc != null) {
                        currentLat = loc.latitude
                        currentLng = loc.longitude
                        val place = nominatimReverse(
                            loc.latitude, loc.longitude,
                            ReverseOptions(zoom = 18, addressDetails = true, language = "vi")
                        )
                        val suburb = place.address?.suburb ?: place.address?.neighbourhood
                        if (!suburb.isNullOrBlank() && wardName.isBlank()) {
                            wardName = suburb.replace(Regex("^(phường|ward|quận)\\s+", RegexOption.IGNORE_CASE), "")
                        }
                    }
                } catch (_: Throwable) { } finally {
                    isLoadingLocation = false
                }
            }

            val lastPhoto      = photos.lastOrNull()
            val lastReady      = lastPhoto?.state == UploadState.DONE
            val canSubmit      = lastReady && photos.isNotEmpty()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(green50)
            ) {
                // ── Scrollable form body ──────────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {

                    // ── Photo strip ───────────────────────────────────────────
                    Text(
                        "Photos  (${photos.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF424242),
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(end = 4.dp),
                    ) {
                        itemsIndexed(photos) { idx, photo ->
                            PhotoThumb(
                                photo  = photo,
                                isLast = idx == photos.lastIndex,
                                onRetry = {
                                    scope.launch {
                                        photos[idx] = photos[idx].copy(state = UploadState.UPLOADING, key = null, imageUrl = null)
                                        val token = SettingsStore.getAccessToken() ?: run {
                                            photos[idx] = photos[idx].copy(state = UploadState.ERROR)
                                            return@launch
                                        }
                                        try {
                                            val filename = "waste_${System.currentTimeMillis()}.jpg"
                                            val result   = requestAndUpload(token, filename, photo.bytes, "image/jpeg")
                                            photos[idx]  = photos[idx].copy(state = UploadState.DONE, key = result.key, imageUrl = result.imageUrl)
                                        } catch (_: Throwable) {
                                            photos[idx] = photos[idx].copy(state = UploadState.ERROR)
                                        }
                                    }
                                },
                            )
                        }
                        // "Add photo" button — camera
                        item {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(2.dp, green600, RoundedCornerShape(12.dp))
                                    .clickable { launchCamera() },
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Icon(
                                        Icons.Filled.CameraAlt,
                                        contentDescription = null,
                                        tint = green600,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Text(
                                        s.wasteReportCamera,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = green600,
                                    )
                                }
                            }
                        }
                        // "Add photo" button — gallery
                        item {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(2.dp, green600, RoundedCornerShape(12.dp))
                                    .clickable { galleryLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Icon(
                                        Icons.Filled.Image,
                                        contentDescription = null,
                                        tint = green600,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Text(
                                        s.wasteReportGallery,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = green600,
                                    )
                                }
                            }
                        }
                    }

                    // Last-photo hint
                    if (photos.size > 1) {
                        Text(
                            "Only the last photo will be submitted to the report.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                        )
                    }

                    // ── Ward ──────────────────────────────────────────────────
                    OutlinedTextField(
                        value = wardName,
                        onValueChange = { wardName = it },
                        label = { Text(s.wasteReportWardLabel) },
                        placeholder = {
                            if (isLoadingLocation) Text("Đang lấy vị trí…", color = Color.LightGray)
                            else Text("e.g. Phường Hải Châu 1", color = Color.LightGray)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            if (isLoadingLocation) CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = green600,
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = green800,
                            focusedLabelColor  = green800,
                            cursorColor        = green800,
                        ),
                    )

                    // ── Description ───────────────────────────────────────────
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(s.wasteReportNoteLabel) },
                        placeholder = { Text(s.wasteReportNoteHint, color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = green800,
                            focusedLabelColor  = green800,
                            cursorColor        = green800,
                        ),
                    )

                    error?.let { msg -> Text(msg, color = red, fontSize = 13.sp) }
                }

                // ── Submit bar ────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (!canSubmit && photos.isNotEmpty()) {
                        Text(
                            if (lastPhoto?.state == UploadState.UPLOADING)
                                "Uploading last photo..."
                            else
                                "Last photo upload failed — retry it above",
                            fontSize = 12.sp,
                            color = if (lastPhoto?.state == UploadState.ERROR) red else Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Button(
                        onClick = {
                            val last = photos.lastOrNull() ?: return@Button
                            error = null
                            onStartSubmit(
                                WasteReportFormData(
                                    imageUrl  = last.imageUrl ?: "",
                                    description = description,
                                    lat      = currentLat,
                                    lng      = currentLng,
                                    wardName = wardName.ifBlank { "Unknown" },
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canSubmit && !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = green800),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Đang gửi...", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        } else {
                            Icon(Icons.Filled.Check, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                s.wasteReportSubmit,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                            )
                        }
                    }
                }
            }
        }

        // ── SUBMITTING overlay ────────────────────────────────────────────────
        WasteReportScanPhase.SUBMITTING -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Text(s.wasteReportUploading, color = Color.White, fontSize = 15.sp)
                }
            }
        }
    }
}

// ── Photo thumbnail ───────────────────────────────────────────────────────────

@Composable
private fun PhotoThumb(
    photo: CapturedPhoto,
    isLast: Boolean,
    onRetry: () -> Unit,
) {
    val bitmap = remember(photo.bytes) {
        BitmapFactory.decodeByteArray(photo.bytes, 0, photo.bytes.size)?.asImageBitmap()
    }

    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isLast) 2.dp else 0.dp,
                color = if (isLast) green800 else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
            ),
    ) {
        // Photo
        if (bitmap != null) {
            Image(
                bitmap       = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier     = Modifier.fillMaxSize(),
            )
        } else {
            Box(Modifier.fillMaxSize().background(Color(0xFFE0E0E0)))
        }

        // "Last" badge
        if (isLast) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(green800)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            ) {
                Text("LAST", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Upload state overlay
        when (photo.state) {
            UploadState.UPLOADING -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(24.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp,
                    )
                }
            }
            UploadState.DONE -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(green800),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✓", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            UploadState.ERROR -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .clickable { onRetry() },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Retry", fontSize = 9.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
