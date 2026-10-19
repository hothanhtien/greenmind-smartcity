package com.vodang.greenmind.camera

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.vodang.greenmind.permission.PermissionGroup
import com.vodang.greenmind.permission.PermissionRequester
import kotlinx.coroutines.launch

@Composable
fun CameraScreen() {
    val cameraGranted by PermissionRequester.grantedFlow(PermissionGroup.CAMERA).collectAsState()
    val scope = rememberCoroutineScope()

    var capturedBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var capturing by remember { mutableStateOf(false) }

    // Start/stop preview when permission state changes
    LaunchedEffect(cameraGranted) {
        if (cameraGranted) Camera.service.startPreview()
        else Camera.service.stopPreview()
    }
    DisposableEffect(Unit) {
        onDispose { Camera.service.stopPreview() }
    }

    if (!cameraGranted) {
        // ── Permission prompt ────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Camera permission is required to use this feature.")
            Spacer(Modifier.height(16.dp))
            Button(onClick = { PermissionRequester.request(PermissionGroup.CAMERA) }) {
                Text("Grant Camera Permission")
            }
        }
        return
    }

    // ── Camera UI ────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {

        // Live viewfinder — edge-to-edge intentionally
        CameraPreview(modifier = Modifier.fillMaxSize())

        // Overlay controls sit inside safe area so they don't hide behind
        // the camera cutout or navigation bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // Captured photo thumbnail — bottom-left
            capturedBitmap?.let { bmp ->
                Image(
                    bitmap = bmp,
                    contentDescription = "Captured photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                )
            }

            // Shutter button — bottom-center
            Button(
                onClick = {
                    if (!capturing) {
                        capturing = true
                        scope.launch {
                            val photo = Camera.service.takePhoto()
                            photo?.let { p ->
                                val bmp = BitmapFactory.decodeByteArray(p.bytes, 0, p.bytes.size)
                                capturedBitmap = bmp?.asImageBitmap()
                            }
                            capturing = false
                        }
                    }
                },
                enabled = !capturing,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .size(72.dp)
            ) {
                if (capturing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                }
            }
        } // end safe-area Box
    } // end root Box
}
