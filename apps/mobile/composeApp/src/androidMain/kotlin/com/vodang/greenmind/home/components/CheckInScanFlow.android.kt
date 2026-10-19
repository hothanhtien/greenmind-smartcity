package com.vodang.greenmind.home.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.vodang.greenmind.api.upload.requestAndUpload
import com.vodang.greenmind.api.wastecollector.CollectorCheckinRequest
import com.vodang.greenmind.api.wastecollector.checkinPickup
import com.vodang.greenmind.api.wastecollector.checkinPickupBatch
import com.vodang.greenmind.permission.CameraPermissionGate
import com.vodang.greenmind.util.AppLogger
import kotlinx.coroutines.launch
import java.io.File

private enum class Phase { IDLE, UPLOADING, BATCHING, ERROR }

private fun createPhotoUri(context: android.content.Context): Uri {
    val dir  = File(context.cacheDir, "camera_photos").also { it.mkdirs() }
    val file = File(dir, "checkin_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
actual fun CheckInScanFlow(
    allPoints: List<WastePoint>,
    reportId: String?,
    accessToken: String,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
) {
    CameraPermissionGate(onDenied = onDismiss) {
    CheckInScanContent(allPoints = allPoints, reportId = reportId, accessToken = accessToken, onSuccess = onSuccess, onDismiss = onDismiss)
    }
}

@Composable
private fun CheckInScanContent(
    allPoints: List<WastePoint>,
    reportId: String?,
    accessToken: String,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var phase      by remember { mutableStateOf(Phase.IDLE) }
    var errorMsg   by remember { mutableStateOf("") }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var total      by remember { mutableIntStateOf(0) }
    var completed  by remember { mutableIntStateOf(0) }
    var failedAddresses by remember { mutableStateOf<List<String>>(emptyList()) }

    // Derive the group from the triggering reportId
    val triggerPoint = remember(reportId, allPoints) {
        reportId?.let { id -> allPoints.find { it.reportId == id } }
    }
    val groupPoints = remember(triggerPoint, allPoints) {
        triggerPoint?.let { t -> allPoints.filter { it.groupKey == t.groupKey && !it.collected } } ?: emptyList()
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { saved ->
        if (!saved) { onDismiss(); return@rememberLauncherForActivityResult }
        val uri = pendingUri ?: run { onDismiss(); return@rememberLauncherForActivityResult }

        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        if (bytes == null) {
            errorMsg = "Failed to read photo"
            phase = Phase.ERROR
            return@rememberLauncherForActivityResult
        }

        scope.launch {
            try {
                phase = Phase.UPLOADING
                AppLogger.i("CheckIn", "Uploading evidence for ${groupPoints.size} reports")
                val filename = "checkin_${System.currentTimeMillis()}.jpg"
                val upload   = requestAndUpload(accessToken, filename, bytes, "image/jpeg")

                phase = Phase.BATCHING
                total = groupPoints.size
                completed = 0
                failedAddresses = emptyList()

                val requests = groupPoints.map { it.reportId to CollectorCheckinRequest(upload.imageUrl) }
                val results = checkinPickupBatch(accessToken, requests)

                results.forEachIndexed { idx, result ->
                    result.onSuccess {
                        completed++
                        AppLogger.i("CheckIn", "Batch ${idx + 1}/${groupPoints.size} done for ${groupPoints[idx].reportId}")
                    }.onFailure { e ->
                        failedAddresses = failedAddresses + groupPoints[idx].address
                        AppLogger.e("CheckIn", "Batch ${idx + 1} failed: ${e.message}")
                    }
                }

                if (failedAddresses.isEmpty()) {
                    phase = Phase.IDLE
                    onSuccess()
                } else {
                    errorMsg = failedAddresses.joinToString(", ")
                    phase = Phase.ERROR
                }
            } catch (e: Throwable) {
                AppLogger.e("CheckIn", "Check-in failed: ${e.message}")
                errorMsg = e.message ?: "Unknown error"
                phase = Phase.ERROR
            }
        }
    }

    LaunchedEffect(reportId) {
        if (reportId == null) {
            phase = Phase.IDLE
            return@LaunchedEffect
        }
        if (groupPoints.isEmpty()) {
            onDismiss()
            return@LaunchedEffect
        }
        val uri = createPhotoUri(context)
        pendingUri = uri
        cameraLauncher.launch(uri)
    }

    if (reportId != null && phase != Phase.IDLE) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center,
        ) {
            when (phase) {
                Phase.UPLOADING -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Text(
                            "⬆  Uploading photo…",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                Phase.BATCHING -> {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Text(
                            "✅  Saving $completed/$total…",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        LinearProgressIndicator(
                            progress = { if (total == 0) 0f else completed.toFloat() / total },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF4CAF50),
                            trackColor = Color.White.copy(alpha = 0.3f),
                        )
                    }
                }
                Phase.ERROR -> {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("⚠", fontSize = 40.sp)
                        Text(
                            if (failedAddresses.isNotEmpty())
                                "Failed: ${failedAddresses.joinToString(", ")}"
                            else
                                errorMsg,
                            color = Color.White,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                        )
                        TextButton(onClick = onDismiss) {
                            Text("Dismiss", color = Color(0xFFFFB300), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
