package com.vodang.greenmind.meal

import androidx.compose.runtime.Composable

@Composable
expect fun MealScanScreen(
    onScanComplete: (plantRatio: Int, description: String, imageUrl: String?, plantImageBase64: String?, dishImageBase64: String?) -> Unit,
    onBack: () -> Unit,
)
