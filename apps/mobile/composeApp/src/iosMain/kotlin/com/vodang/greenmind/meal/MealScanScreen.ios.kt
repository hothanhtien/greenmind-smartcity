package com.vodang.greenmind.meal

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
actual fun MealScanScreen(
    onScanComplete: (plantRatio: Int, description: String, imageUrl: String?, plantImageBase64: String?, dishImageBase64: String?) -> Unit,
    onBack: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Camera scan is not available on iOS yet.", color = Color.Gray, fontSize = 15.sp)
    }
}
