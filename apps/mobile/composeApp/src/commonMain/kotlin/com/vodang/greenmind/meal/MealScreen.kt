package com.vodang.greenmind.meal

import androidx.compose.runtime.*
import com.vodang.greenmind.platform.BackHandler
import com.vodang.greenmind.store.MealRecord
import com.vodang.greenmind.store.MealStore

@Composable
fun MealScreen() {
    var showScan by remember { mutableStateOf(false) }
    var selectedMeal by remember { mutableStateOf<MealRecord?>(null) }

    BackHandler(enabled = selectedMeal != null) { selectedMeal = null }
    BackHandler(enabled = showScan) { showScan = false }

    when {
        showScan -> {
            MealScanScreen(
                onScanComplete = { ratio, desc, imageUrl, plantImageBase64, dishImageBase64 ->
                    MealStore.add(ratio, desc, imageUrl, plantImageBase64, dishImageBase64)
                    showScan = false
                },
                onBack = { showScan = false }
            )
        }
        selectedMeal != null -> {
            MealDetailScreen(
                meal   = selectedMeal!!,
                onBack = { selectedMeal = null }
            )
        }
        else -> {
            MealListScreen(
                onScanClick = { showScan = true },
                onCardClick = { selectedMeal = it }
            )
        }
    }
}
