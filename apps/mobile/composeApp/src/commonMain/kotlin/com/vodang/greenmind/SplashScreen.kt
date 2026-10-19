package com.vodang.greenmind

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings

private val green800 = Color(0xFF2E7D32)

@Composable
fun SplashScreen() {
    val s = LocalAppStrings.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFF1F8E9)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(green800, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(s.splashLogo, fontSize = 40.sp)
            }
            Text(s.greenMind, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = green800)
            Spacer(Modifier.height(8.dp))
            CircularProgressIndicator(color = green800, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
        }
    }
}
