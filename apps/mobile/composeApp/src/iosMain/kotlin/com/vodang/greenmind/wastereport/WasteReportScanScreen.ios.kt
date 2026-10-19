package com.vodang.greenmind.wastereport

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vodang.greenmind.api.upload.requestAndUpload
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.location.Geo
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.util.AppLogger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.cinterop.ExperimentalForeignApi
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.UIKit.UIImage
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerSourceType
import platform.Foundation.NSData
import platform.Foundation.toByteArray

private val green800 = Color(0xFF2E7D32)
private val green50  = Color(0xFFE8F5E9)

private enum class WasteReportScanPhase { IDLE, FORM, SUBMITTING }
private enum class UploadState { UPLOADING, DONE, ERROR }

private data class CapturedPhoto(
    val bytes: ByteArray,
    val state: UploadState = UploadState.UPLOADING,
    val key: String? = null,
    val imageUrl: String? = null,
)

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun WasteReportScanScreen(
    onStartSubmit: (WasteReportFormData) -> Unit,
    onBack: () -> Unit,
    launchCamera: Boolean,
    onSubmitDone: () -> Unit,
    isSubmitting: Boolean,
) {
    val s       = LocalAppStrings.current
    val scope   = rememberCoroutineScope()

    var phase       by remember { mutableStateOf(WasteReportScanPhase.IDLE) }
    val photos      = remember { mutableStateListOf<CapturedPhoto>() }

    var description       by remember { mutableStateOf("") }
    var wardName          by remember { mutableStateOf("") }
    var currentLat        by remember { mutableStateOf(0.0) }
    var currentLng        by remember { mutableStateOf(0.0) }
    var isLoadingLocation by remember { mutableStateOf(false) }
    var error             by remember { mutableStateOf<String?>(null) }

    fun addAndUpload(bytes: ByteArray) {
        val idx = photos.size
        photos.add(CapturedPhoto(bytes = bytes))
        scope.launch {
            try {
                val result = requestAndUpload(
                    SettingsStore.getAccessToken() ?: return@launch,
                    imageBytes = bytes,
                    filename   = "waste_${System.currentTimeMillis()}.jpg",
                )
                photos[idx] = photos[idx].copy(state = UploadState.DONE, key = result.key, imageUrl = result.url)
            } catch (e: Throwable) {
                AppLogger.e("WasteReport", "Upload failed idx=$idx: ${e.message}")
                photos[idx] = photos[idx].copy(state = UploadState.ERROR)
            }
        }
    }

    if (phase == WasteReportScanPhase.IDLE) {
        LaunchedEffect(Unit) {
            if (launchCamera) {
                AppLogger.e("WasteReport", "iOS camera not implemented yet")
                onBack()
            } else {
                val picker = PHPickerViewController(PHPickerConfiguration())
                var completed = false
                picker.delegate = object : platform.PhotosUI.PHPickerViewControllerDelegate {
                    override fun picker(picker: platform.PhotosUI.PHPickerViewController, didFinishPicking: List<PHPickerResult>) {
                        picker.dismiss()
                        if (didFinishPicking.isNotEmpty() && !completed) {
                            completed = true
                            val result = didFinishPicking.first()
                            result.imageProvider?.loadObject(UIImage::class) { image, _ ->
                                if (image is UIImage) {
                                    val nsData = image.jpegData(0.8)
                                    if (nsData != null) {
                                        val bytes = nsData.toByteArray()
                                        addAndUpload(bytes)
                                        phase = WasteReportScanPhase.FORM
                                    }
                                }
                            }
                        }
                    }
                }
                val controller = androidx.compose.ui.platform.LocalContext.current as? platform.UIKit.UIViewController
                controller?.present(picker, true)
            }
        }
        Box(modifier = Modifier.fillMaxSize().background(green50))
    } else {
        when (phase) {
            WasteReportScanPhase.FORM -> {
                LaunchedEffect(Unit) {
                    isLoadingLocation = true
                    try {
                        val loc = withTimeoutOrNull(5_000L) { Geo.service.locationUpdates.first() }
                        if (loc != null) {
                            currentLat = loc.latitude
                            currentLng = loc.longitude
                        }
                    } catch (_: Throwable) { } finally {
                        isLoadingLocation = false
                    }
                }

                Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = s.wasteReportTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            color = green800,
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            itemsIndexed(photos) { idx, photo ->
                                Box(
                                    modifier = Modifier.size(80.dp)
                                        .background(Color.Gray, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    when (photo.state) {
                                        UploadState.UPLOADING -> CircularProgressIndicator(modifier = Modifier.size(24.dp), color = green800)
                                        UploadState.DONE -> Icon(Icons.Filled.Check, contentDescription = null, tint = green800)
                                        UploadState.ERROR -> Icon(Icons.Filled.Close, contentDescription = null, tint = Color.Red)
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text(s.descriptionLabel) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                        )

                        OutlinedTextField(
                            value = wardName,
                            onValueChange = { wardName = it },
                            label = { Text(s.wardLabel) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoadingLocation,
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                val photo = photos.lastOrNull { it.state == UploadState.DONE }
                                if (photo == null) { error = "Please wait for image to upload"; return@Button }
                                onStartSubmit(WasteReportFormData(
                                    imageUrl     = photo.imageUrl ?: "",
                                    description  = description,
                                    lat          = currentLat,
                                    lng          = currentLng,
                                    wardName     = wardName,
                                ))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = green800),
                            enabled = photos.any { it.state == UploadState.DONE } && !isLoadingLocation && !isSubmitting,
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Đang gửi...")
                            } else {
                                Text(s.submitReport)
                            }
                        }
                    }

                    if (isLoadingLocation) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    error?.let {
                        Snackbar(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                            action = { TextButton(onClick = { error = null }) { Text(s.dismiss) } },
                        ) { Text(it) }
                    }
                }
            }

            WasteReportScanPhase.SUBMITTING -> {
                Box(modifier = Modifier.fillMaxSize().background(green50), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            else -> { }
        }
    }
}
