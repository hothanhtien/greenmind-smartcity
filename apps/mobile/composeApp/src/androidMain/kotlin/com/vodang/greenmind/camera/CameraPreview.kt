package com.vodang.greenmind.camera

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Drop this composable anywhere to show the live camera feed.
 * It wires itself into [Camera.service] automatically.
 */
@Composable
fun CameraPreview(modifier: Modifier = Modifier) {
    AndroidView(
        factory = { ctx: Context ->
            val previewView = PreviewView(ctx)
            previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
            previewView
        },
        modifier = modifier,
        update = { view ->
            // Access the internal mutable surface property through the shared companion
            CameraServiceInternal.surfaceProvider = view.surfaceProvider
        }
    )
}
