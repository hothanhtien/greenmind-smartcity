package com.vodang.greenmind.wastesort

import androidx.compose.runtime.Composable

@Composable
expect fun WasteTotalMassPicker(
    onScanned: () -> Unit,
    onDismiss: () -> Unit,
)
