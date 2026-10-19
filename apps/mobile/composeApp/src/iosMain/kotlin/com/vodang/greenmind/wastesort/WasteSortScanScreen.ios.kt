package com.vodang.greenmind.wastesort

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
actual fun WasteSortScanScreen(
    onResult: (WasteSortEntry) -> Unit,
    onBack: () -> Unit,
    useGallery: Boolean,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Waste scan is not available on iOS yet.", color = Color.Gray, fontSize = 15.sp)
    }
}
