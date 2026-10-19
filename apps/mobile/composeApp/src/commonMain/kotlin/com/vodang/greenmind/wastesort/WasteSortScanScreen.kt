package com.vodang.greenmind.wastesort

import androidx.compose.runtime.Composable

@Composable
expect fun WasteSortScanScreen(
    onResult: (WasteSortEntry) -> Unit,
    onBack: () -> Unit,
    useGallery: Boolean = false,
)
