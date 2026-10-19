package com.vodang.greenmind.platform

import androidx.activity.compose.BackHandler as ActivityBackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    ActivityBackHandler(enabled = enabled, onBack = onBack)
}
