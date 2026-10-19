package com.vodang.greenmind.wastesort

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.wastesort.DetectTrashResponse
import com.vodang.greenmind.api.households.DetectImageUrlRequest
import com.vodang.greenmind.api.households.detectTrashOnly
import com.vodang.greenmind.api.upload.requestAndUpload
import com.vodang.greenmind.api.wastesort.detectTrash
import com.vodang.greenmind.api.wastesort.predictTrashSeg
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.time.nowIso8601
import com.vodang.greenmind.util.AppLogger
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

private val green800 = Color(0xFF2E7D32)
private val green50  = Color(0xFFE8F5E9)

private enum class Phase { IDLE, ANALYZING, ERROR }

@Composable
actual fun WasteSortScanScreen(
    onResult: (WasteSortEntry) -> Unit,
    onBack: () -> Unit,
    useGallery: Boolean,
) {
    val scope    = rememberCoroutineScope()
    var phase    by remember { mutableStateOf(Phase.IDLE) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    fun analyze(bytes: ByteArray) {
        phase = Phase.ANALYZING
        scope.launch {
            val token = SettingsStore.getAccessToken()
            if (token == null) { errorMsg = "Sign in required"; phase = Phase.ERROR; return@launch }
            try {
                val filename = "waste_scan_${System.currentTimeMillis()}.jpg"
                val userName = SettingsStore.user.value?.fullName
                    ?: SettingsStore.user.value?.username
                    ?: "Me"
                val ver2Deferred   = async { runCatching { detectTrash(bytes, filename) }.getOrNull() }
                val segDeferred    = async { runCatching { predictTrashSeg(bytes, filename) }.getOrNull() }
                val uploadDeferred = async { requestAndUpload(token, filename, bytes, "image/jpeg") }

                val ver2Result = ver2Deferred.await()
                val segResult  = segDeferred.await()
                val upload     = uploadDeferred.await()

                val request     = DetectImageUrlRequest(imageUrl = upload.imageUrl)
                val detectResult = detectTrashOnly(token, request)
                val dto          = detectResult.data

                val imageUrl = upload.imageUrl
                val entryId = imageUrl.substringAfterLast("/").substringBeforeLast(".")

                val grouped: Map<String, List<String>> = when {
                    ver2Result != null && ver2Result.grouped.isNotEmpty() ->
                        ver2Result.grouped.mapValues { (cat, ver2Urls) ->
                            segResult?.grouped?.get(cat)?.takeIf { it.isNotEmpty() } ?: ver2Urls
                        }
                    segResult != null && segResult.grouped.isNotEmpty() -> segResult.grouped
                    else -> dto.items?.groupBy { it.name }?.mapValues { listOf(imageUrl) } ?: emptyMap()
                }

                val entry = WasteSortEntry(
                    id           = entryId,
                    backendId    = dto.id,
                    imageUrl     = ver2Result?.imageUrl ?: segResult?.imageUrl
                        ?: dto.aiAnalysis ?: dto.imageUrl,
                    totalObjects = ver2Result?.totalObjects ?: segResult?.totalObjects ?: dto.totalObjects ?: 0,
                    grouped      = grouped,
                    createdAt    = nowIso8601().take(10),
                    scannedBy    = userName,
                    status       = WasteSortStatus.SORTED,
                    totalMassKg  = dto.totalMassKg,
                )
                onResult(entry)
            } catch (e: Throwable) {
                AppLogger.e("DesktopScan", "analyze failed: ${e.message}")
                errorMsg = e.message ?: "Detection failed"
                phase = Phase.ERROR
            }
        }
    }

    when (phase) {
        Phase.IDLE -> {
            LaunchedEffect(Unit) {
                val dialog = FileDialog(null as Frame?, "Select waste image", FileDialog.LOAD)
                dialog.file = "*.jpg;*.jpeg;*.png"
                dialog.isVisible = true
                val file = dialog.file?.let { File(dialog.directory, it) }
                if (file != null && file.exists()) {
                    analyze(file.readBytes())
                } else {
                    onBack()
                }
            }
            Box(modifier = Modifier.fillMaxSize().background(green50))
        }

        Phase.ANALYZING -> {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(48.dp), strokeWidth = 3.dp)
                    Text("Detecting waste…", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("This may take a few seconds", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                }
            }
        }

        Phase.ERROR -> {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("⚠️", fontSize = 48.sp)
                    Text("Detection failed", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(errorMsg ?: "Unknown error", fontSize = 13.sp, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onBack, shape = RoundedCornerShape(12.dp)) { Text("Cancel") }
                        Button(
                            onClick = { phase = Phase.IDLE },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = green800),
                        ) { Text("Retry") }
                    }
                }
            }
        }
    }
}
