package com.vodang.greenmind.wastesort

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.vodang.greenmind.permission.CameraPermissionGate
import com.vodang.greenmind.api.households.AnalyzeImageRequest
import com.vodang.greenmind.api.households.analyzeImage
import com.vodang.greenmind.api.households.submitGreenScoreByDetectId
import com.vodang.greenmind.api.households.getGreenScoreHistory
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.store.WasteSortStore
import com.vodang.greenmind.api.upload.requestAndUpload
import com.vodang.greenmind.time.nowIso8601
import com.vodang.greenmind.util.AppLogger
import kotlinx.coroutines.launch
import java.io.File

private val green600 = Color(0xFF388E3C)
private val green800 = Color(0xFF2E7D32)
private val green50  = Color(0xFFE8F5E9)

private enum class WasteSortScanPhase { IDLE, ANALYZING, ERROR }

private fun createScanPhotoUri(context: android.content.Context): Uri {
    val dir  = File(context.cacheDir, "camera_photos").also { it.mkdirs() }
    val file = File(dir, "waste_scan_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
actual fun WasteSortScanScreen(
    onResult: (WasteSortEntry) -> Unit,
    onBack: () -> Unit,
    useGallery: Boolean,
) {
    CameraPermissionGate(onDenied = onBack, skip = useGallery) {
    WasteSortScanContent(onResult = onResult, onBack = onBack, useGallery = useGallery)
    }
}

@Composable
private fun WasteSortScanContent(
    onResult: (WasteSortEntry) -> Unit,
    onBack: () -> Unit,
    useGallery: Boolean,
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var phase         by remember { mutableStateOf(WasteSortScanPhase.IDLE) }
    var errorMsg      by remember { mutableStateOf<String?>(null) }
    var pendingUri    by remember { mutableStateOf<Uri?>(null) }
    var capturedBytes by remember { mutableStateOf<ByteArray?>(null) }

    fun analyze(bytes: ByteArray) {
        phase = WasteSortScanPhase.ANALYZING
        scope.launch {
            val token = SettingsStore.getAccessToken()
            if (token == null) {
                errorMsg = "Sign in required to scan waste"
                phase = WasteSortScanPhase.ERROR
                return@launch
            }
            try {
                val filename = "waste_scan_${System.currentTimeMillis()}.jpg"
                val userName = SettingsStore.user.value?.fullName
                    ?: SettingsStore.user.value?.username
                    ?: "Me"

                // 1. Upload image to get hosted URL
                val upload = requestAndUpload(
                    accessToken = token,
                    filename    = filename,
                    fileBytes   = bytes,
                    contentType = "image/jpeg",
                )
                val imageUrl = upload.imageUrl

                // 2. Call unified analyzeImage API
                val data = analyzeImage(token, AnalyzeImageRequest(imageUrl = imageUrl, type = "DETECT_TRASH")).data

                // 3. Build entry
                val entry = WasteSortEntry(
                    id              = imageUrl.substringAfterLast("/").substringBeforeLast("."),
                    backendId       = data.id,
                    imageUrl        = data.aiAnalysis ?: data.imageUrl ?: imageUrl,
                    totalObjects    = data.totalObjects ?: 0,
                    grouped         = data.segments?.let { seg ->
                        buildMap {
                            if (seg.recyclable.isNotEmpty()) put("recyclable", seg.recyclable)
                            if (seg.residual.isNotEmpty()) put("residual", seg.residual)
                        }
                    } ?: emptyMap(),
                    createdAt       = nowIso8601().take(10),
                    scannedBy       = userName,
                    status          = WasteSortStatus.SORTED,
                    detectType      = data.detectType,
                )

                AppLogger.i("HouseholdScan", "analyzeImage ok id=${data.id} total=${data.totalObjects}")

                // 4. Fetch green score BEFORE showing result (wait for it to complete)
                var greenScoreEntry: com.vodang.greenmind.api.households.GreenScoreEntryDto? = null
                val detectId = data.id
                if (detectId != null) {
                    try {
                        val greenScoreResp = submitGreenScoreByDetectId(token, detectId)
                        greenScoreEntry = greenScoreResp.data
                        AppLogger.i("HouseholdScan", "submitGreenScoreByDetectId ok id=${greenScoreResp.data.id}")
                    } catch (e: Throwable) {
                        AppLogger.e("HouseholdScan", "submitGreenScoreByDetectId failed: ${e.message}")
                    }
                }
                if (greenScoreEntry == null) {
                    try {
                        val historyResp = getGreenScoreHistory(token)
                        greenScoreEntry = historyResp.data.lastOrNull()
                        if (greenScoreEntry != null) {
                            AppLogger.i("HouseholdScan", "getGreenScoreHistory fallback ok")
                        }
                    } catch (e2: Throwable) {
                        AppLogger.e("HouseholdScan", "getGreenScoreHistory fallback failed: ${e2.message}")
                    }
                }

                // 6. Update entry with green score before showing result
                val entryWithScore = if (greenScoreEntry != null) {
                    entry.copy(greenScoreResult = greenScoreEntry)
                } else {
                    entry
                }
                onResult(entryWithScore)
            } catch (e: Throwable) {
                AppLogger.e("HouseholdScan", "analyzeImage failed: ${e.message}")
                errorMsg = e.message ?: "Detection failed"
                phase = WasteSortScanPhase.ERROR
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { saved ->
        if (saved) {
            val uri = pendingUri
            if (uri != null) {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    capturedBytes = bytes
                    analyze(bytes)
                } else {
                    onBack()
                }
            }
        } else {
            onBack()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                capturedBytes = bytes
                analyze(bytes)
            } else {
                onBack()
            }
        } else {
            onBack()
        }
    }

    fun launchCamera() {
        val uri = createScanPhotoUri(context)
        pendingUri = uri
        cameraLauncher.launch(uri)
    }

    when (phase) {
        WasteSortScanPhase.IDLE -> {
            LaunchedEffect(Unit) {
                if (useGallery) galleryLauncher.launch("image/*") else launchCamera()
            }
            Box(modifier = Modifier.fillMaxSize().background(green50))
        }

        WasteSortScanPhase.ANALYZING -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 3.dp,
                    )
                    Text(
                        "Detecting waste…",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        "This may take a few seconds",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                    )
                }
            }
        }

        WasteSortScanPhase.ERROR -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "Detection failed",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1B),
                    )
                    Text(
                        errorMsg ?: "Unknown error",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onBack,
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val bytes = capturedBytes
                                if (bytes != null) analyze(bytes) else phase = WasteSortScanPhase.IDLE
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = green800),
                        ) {
                            Text("Retry")
                        }
                    }
                    TextButton(onClick = { phase = WasteSortScanPhase.IDLE }) {
                        Text("Retake photo", color = green600)
                    }
                }
            }
        }
    }
}
