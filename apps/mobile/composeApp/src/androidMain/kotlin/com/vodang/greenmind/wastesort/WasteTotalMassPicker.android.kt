package com.vodang.greenmind.wastesort

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.vodang.greenmind.api.households.DetectImageUrlRequest
import com.vodang.greenmind.api.households.detectTotalMass
import com.vodang.greenmind.api.upload.requestAndUpload
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.permission.CameraPermissionGate
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.util.AppLogger
import kotlinx.coroutines.launch
import java.io.File

private val green800 = Color(0xFF2E7D32)
private val green50  = Color(0xFFE8F5E9)
private val blue50  = Color(0xFFE3F2FD)
private val red     = Color(0xFFC62828)

private fun createPhotoUri(ctx: android.content.Context): Uri {
    val dir  = File(ctx.cacheDir, "total_mass_photos").also { it.mkdirs() }
    val file = File(dir, "mass_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
}

private suspend fun doProcess(
    bytes: ByteArray,
    onScanned: () -> Unit,
    onDismiss: () -> Unit,
    setError: (String) -> Unit,
    setDone: () -> Unit,
) {
    val token = SettingsStore.getAccessToken()
    if (token == null) { setError("No token"); setDone(); return }
    try {
        val filename = "total_mass_${System.currentTimeMillis()}.jpg"
        val result = requestAndUpload(token, filename, bytes, "image/jpeg")
        AppLogger.i("WasteTotalMass", "Uploaded key=${result.key} url=${result.imageUrl}")
        detectTotalMass(token, DetectImageUrlRequest(result.imageUrl))
        AppLogger.i("WasteTotalMass", "detectTotalMass done")
        onScanned()
        onDismiss()
    } catch (e: Throwable) {
        AppLogger.e("WasteTotalMass", "processAndUpload failed: ${e.message}")
        setError(e.message ?: "Error")
    }
    setDone()
}

@Composable
actual fun WasteTotalMassPicker(onScanned: () -> Unit, onDismiss: () -> Unit) {
    val s = LocalAppStrings.current
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // ── Camera launcher ─────────────────────────────────────────────────────────
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { saved ->
        if (saved) {
            pendingUri?.let { uri ->
                scope.launch {
                    isProcessing = true
                    errorMsg = null
                    try {
                        val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            ?: throw Exception("Cannot read image")
                        doProcess(
                            bytes = bytes,
                            onScanned = onScanned,
                            onDismiss = onDismiss,
                            setError = { errorMsg = it },
                            setDone = { isProcessing = false },
                        )
                    } catch (e: Throwable) {
                        errorMsg = e.message ?: "Error"
                        isProcessing = false
                    }
                }
            }
        } else {
            pendingUri = null
        }
    }

    // ── Gallery launcher ────────────────────────────────────────────────────────
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isProcessing = true
                errorMsg = null
                try {
                    val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: throw Exception("Cannot read image")
                    doProcess(
                        bytes = bytes,
                        onScanned = onScanned,
                        onDismiss = onDismiss,
                        setError = { errorMsg = it },
                        setDone = { isProcessing = false },
                    )
                } catch (e: Throwable) {
                    errorMsg = e.message ?: "Error"
                    isProcessing = false
                }
            }
        } else {
            onDismiss()
        }
    }

    fun launchCamera() {
        val uri = createPhotoUri(ctx)
        pendingUri = uri
        cameraLauncher.launch(uri)
    }

    // ── Processing overlay ────────────────────────────────────────────────────────
    if (isProcessing) {
        Dialog(onDismissRequest = {}) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator(color = green800)
                    Text(s.wasteTotalMassProcessing, fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
        return
    }

    // ── Error dialog ───────────────────────────────────────────────────────────
    if (errorMsg != null) {
        Dialog(onDismissRequest = { errorMsg = null }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("⚠️", fontSize = 32.sp)
                    Text(errorMsg!!, fontSize = 14.sp, color = red, lineHeight = 20.sp)
                    Button(
                        onClick = { errorMsg = null },
                        colors = ButtonDefaults.buttonColors(containerColor = green800),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text(s.retry)
                    }
                    TextButton(onClick = { errorMsg = null; onDismiss() }) {
                        Text(s.cancel)
                    }
                }
            }
        }
        return
    }

    // ── Camera permission gate ──────────────────────────────────────────────────
    CameraPermissionGate(onDenied = onDismiss) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(s.wasteTotalMassQuickScan, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = green800)
                    Spacer(Modifier.height(8.dp))
                    Text(s.wasteTotalMassQuickScanDesc, fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp)
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Camera button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(green50)
                                .clickable { launchCamera() }
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("📷", fontSize = 32.sp)
                                Text(s.wasteReportCamera, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = green800)
                            }
                        }
                        // Gallery button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(blue50)
                                .clickable { galleryLauncher.launch("image/*") }
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("🖼️", fontSize = 32.sp)
                                Text(s.wasteReportGallery, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = green800)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onDismiss) {
                        Text(s.cancel)
                    }
                }
            }
        }
    }
}
