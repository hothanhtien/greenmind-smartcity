package com.vodang.greenmind.householdwaste.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val gray700 = Color(0xFF374151)
private val gray400 = Color(0xFF9CA3AF)
private val green700 = Color(0xFF2E7D32)

@Composable
fun SectionHeader(title: String, count: Int, icon: ImageVector? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = green700)
        }
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = gray700)
        Box(
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE5E7EB))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text("$count", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = gray700)
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, fontSize = 13.sp, color = gray400)
    }
}