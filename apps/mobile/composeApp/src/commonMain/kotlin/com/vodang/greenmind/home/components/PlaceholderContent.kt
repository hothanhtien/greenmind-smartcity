package com.vodang.greenmind.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val green800 = Color(0xFF2E7D32)

data class NavSection(val icon: String, val label: String)

@Composable
fun PlaceholderContent(section: NavSection) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(section.icon, fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(section.label, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = green800)
            Spacer(Modifier.height(8.dp))
            Text("Coming soon", fontSize = 14.sp, color = Color.Gray)
        }
    }
}
