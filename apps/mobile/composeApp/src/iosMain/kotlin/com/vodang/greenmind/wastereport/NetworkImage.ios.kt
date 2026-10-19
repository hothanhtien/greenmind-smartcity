package com.vodang.greenmind.wastereport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.compose.ui.layout.ContentScale

@Composable
actual fun NetworkImage(url: String, modifier: Modifier, contentScale: ContentScale) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFEEEEEE)),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
            color = Color(0xFF2E7D32),
        )
    }
}
