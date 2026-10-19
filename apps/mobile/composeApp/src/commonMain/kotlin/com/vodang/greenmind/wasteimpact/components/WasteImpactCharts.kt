package com.vodang.greenmind.wasteimpact.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.fmt
import com.vodang.greenmind.i18n.LocalAppStrings

// All palette colours live in WasteImpactPalette.

// ── Eco score helpers (used across hero stats, charts, and scan detail) ───────

fun ecoScoreColor(score: Int): Color = when {
    score >= 60 -> Color(0xFF2E7D32)
    score >= 35 -> Color(0xFFE65100)
    else        -> Color(0xFFC62828)
}

@Composable
fun ecoScoreLabel(score: Int): String {
    val s = LocalAppStrings.current
    return when {
        score >= 80 -> s.excellentMinimalImpact
        score >= 60 -> s.goodLowImpact
        score >= 35 -> s.fairModerateImpact
        else        -> s.poorHighImpact
    }
}

// ── Pollutant bar ──────────────────────────────────────────────────────────────

@Composable
fun PollutantBar(rank: Int, label: String, value: Double, barColor: Color) {
    val progress = value.toFloat().coerceIn(0f, 1f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(barColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text("$rank", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = barColor)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, fontSize = 12.sp, color = Color(0xFF212121), fontWeight = FontWeight.Medium)
                Text(
                    value.fmt(2),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = barColor
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = barColor,
                trackColor = barColor.copy(alpha = 0.10f)
            )
        }
    }
}

// ── Summary chip ──────────────────────────────────────────────────────────────

@Composable
fun SummaryChip(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center)
        Text(label, fontSize = 10.sp, color = color.copy(alpha = 0.8f), textAlign = TextAlign.Center)
    }
}
