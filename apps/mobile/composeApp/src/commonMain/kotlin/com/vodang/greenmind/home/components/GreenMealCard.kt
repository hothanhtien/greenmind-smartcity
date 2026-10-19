package com.vodang.greenmind.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import com.vodang.greenmind.i18n.LocalAppStrings

private val green600 = Color(0xFF388E3C)
private val green100 = Color(0xFFC8E6C9)

@Composable
fun GreenMealCard() {
    val s = LocalAppStrings.current
    val pct = 0.68f
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Eco, contentDescription = null, modifier = Modifier.size(22.dp), tint = green600)
            Spacer(Modifier.width(8.dp))
            Text(s.greenMealCardTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            Text("${(pct * 100).toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = green600)
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { pct },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(6.dp)),
            color = green600,
            trackColor = green100
        )
        Spacer(Modifier.height(6.dp))
        Text(s.greenMealCardDesc, fontSize = 11.sp, color = Color.Gray)
    }
}
