package com.vodang.greenmind.wastereport

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Platform-specific composable that decodes and displays a base64-encoded image. */
@Composable
expect fun Base64Image(base64: String, modifier: Modifier = Modifier)
