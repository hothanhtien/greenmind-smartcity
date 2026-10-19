package com.vodang.greenmind.wastereport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun Base64Image(base64: String, modifier: Modifier) {
    Box(modifier.background(Color(0xFFEEEEEE)))
}
