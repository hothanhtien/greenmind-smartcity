package com.vodang.greenmind

import androidx.compose.runtime.Composable
import com.vodang.greenmind.camera.CameraScreen as CameraScreenImpl

@Composable
actual fun CameraScreen() {
    CameraScreenImpl()
}
