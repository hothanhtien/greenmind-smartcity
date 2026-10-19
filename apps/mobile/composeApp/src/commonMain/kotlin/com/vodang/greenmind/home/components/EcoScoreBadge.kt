package com.vodang.greenmind.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val green800 = Color(0xFF2E7D32)

@Composable
fun EcoScoreBadge(score: Int) {
    Box(
        modifier = Modifier.size(56.dp).background(green800, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$score", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Eco", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}
