package com.vodang.greenmind.platform

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS uses swipe-from-edge natively; no-op here.
}
