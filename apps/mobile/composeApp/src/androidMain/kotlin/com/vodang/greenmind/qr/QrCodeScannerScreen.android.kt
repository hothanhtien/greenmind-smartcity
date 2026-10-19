package com.vodang.greenmind.qr

import android.graphics.Color
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.vodang.greenmind.permission.CameraPermissionGate
import java.util.concurrent.Executors

@Composable
actual fun QrCodeScannerScreen(
    onScanResult: (result: String) -> Unit,
    onBack: () -> Unit,
) {
    CameraPermissionGate(onDenied = onBack) {
        QrCodeScannerContent(onScanResult = onScanResult, onBack = onBack)
    }
}

@Composable
private fun QrCodeScannerContent(
    onScanResult: (String) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val barcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }

    var hasResult by remember { mutableStateOf(false) }
    var scanError by remember { mutableStateOf<String?>(null) }

    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }

    imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
        if (hasResult) {
            imageProxy.close()
            return@setAnalyzer
        }
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { value ->
                            if (!hasResult) {
                                hasResult = true
                                onScanResult(value)
                            }
                            break
                        }
                    }
                }
                .addOnFailureListener { e ->
                    scanError = e.message
                }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(surfaceProvider)
                        }
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis,
                        )
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Semi-transparent overlay with scan frame
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .background(
                        androidx.compose.ui.graphics.Color.Transparent,
                        RoundedCornerShape(16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Color.Transparent,
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Scan QR Code",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        // Back button
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Text("←", fontSize = 18.sp, color = androidx.compose.ui.graphics.Color.White)
        }

        // Hint at bottom
        Text(
            "Point the camera at a QR code",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            color = androidx.compose.ui.graphics.Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )

        // Error
        scanError?.let { err ->
            Text(
                err,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp),
                color = androidx.compose.ui.graphics.Color.Red,
                fontSize = 12.sp,
            )
        }
    }
}
