package com.vodang.greenmind.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings

private val heatmapColors = listOf(
    Color(0xFF81C784),
    Color(0xFFA5D6A7),
    Color(0xFFFFEE58),
    Color(0xFFFF9800),
    Color(0xFFF44336)
)

@Composable
fun GarbageHeatmapCard() {
    val s = LocalAppStrings.current
    SectionCard {
        Text(s.heatmapCardTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        // TODO: Replace sampleGarbageMapPoints with live data from the API (see GarbageMapView.kt).
        HeatMap(points = sampleGarbageMapPoints)
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(s.heatLow, fontSize = 10.sp, color = Color.Gray)
            heatmapColors.forEach { color ->
                Box(modifier = Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
            }
            Text(s.heatHigh, fontSize = 10.sp, color = Color.Gray)
        }
    }
}
