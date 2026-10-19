package com.vodang.greenmind.scandetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.fmt
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.scandetail.ScanImpact

@Composable
fun ImpactMeterSection(impact: ScanImpact?) {
    if (impact == null) return
    val s = LocalAppStrings.current
    val gray700 = Color(0xFF374151)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(s.envImpact, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = gray700)

        val items = listOf(
            s.airPollution to impact.air,
            s.waterPollution to impact.water,
            s.soilPollution to impact.soil,
        )
        items.forEach { (label, value) ->
            val normalized = (value * 100).coerceIn(0.0, 100.0)
            val color = when {
                normalized < 20 -> Color(0xFF2E7D32)
                normalized < 50 -> Color(0xFFF57C00)
                else            -> Color(0xFFD32F2F)
            }
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(label, fontSize = 12.sp, color = gray700)
                    Text("%.1f%%".fmt(normalized), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFEEEEEE))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (normalized / 100.0).toFloat())
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
fun PollutantBreakdownSection(pollution: Map<String, Double>?) {
    if (pollution.isNullOrEmpty()) return
    val s = LocalAppStrings.current
    val gray700 = Color(0xFF374151)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(s.pollutantDetails, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = gray700)
        pollution.forEach { (name, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(name, fontSize = 12.sp, color = gray700)
                Text("%.2f".fmt(value), fontSize = 12.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Medium)
            }
        }
    }
}
