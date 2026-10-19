package com.vodang.greenmind.wasteimpact.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.vodang.greenmind.wastereport.NetworkImage
import com.vodang.greenmind.wastesort.WasteSortEntry
import kotlin.math.roundToInt

// All palette colours live in WasteImpactPalette.

// ── Shared data class ──────────────────────────────────────────────────────────

/**
 * All derived metrics shown on the Waste Impact page.
 * Swap the implementation inside [com.vodang.greenmind.wasteimpact.aggregateImpact]
 * to change the algorithm without touching any UI code.
 */
data class ImpactSummary(
    val totalScans:  Int,
    val totalItems:  Int,
    val ecoScore:    Int?,           // null = not enough data
    val air:         Double?,
    val water:       Double?,
    val soil:        Double?,
    val pollutants:  Map<String, Double>,  // key → aggregated value, always contains allPollutantKeys
)

// ── Hero stat ──────────────────────────────────────────────────────────────────

@Composable
fun HeroStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f), textAlign = TextAlign.Center)
    }
}

// ── Impact meter ──────────────────────────────────────────────────────────────

@Composable
fun ImpactMeter(icon: String, label: String, value: Float) {
    val progress = value.coerceIn(0f, 1f)
    val pct = (value * 100).roundToInt()
    val meterColor = when {
        value < 0.5f -> green800
        value <= 0.7f -> amber
        else -> red600
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 14.sp)
                Text(label, fontSize = 12.sp, color = Color(0xFF424242))
            }
            Text("$pct%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = meterColor)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(6.dp)),
            color = meterColor,
            trackColor = meterColor.copy(alpha = 0.12f),
        )
    }
}

// ── Scan history row ──────────────────────────────────────────────────────────

@Composable
fun ScanHistoryRow(entry: WasteSortEntry, onClick: () -> Unit) {
    val s = LocalAppStrings.current
    val ecoScore = entry.pollutantResult?.ecoScore
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF0F0F0))
        ) {
            NetworkImage(
                url = entry.imageUrl,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Info
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                entry.createdAt,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121)
            )
            Text(
                s.scanHistoryRow(entry.totalObjects, entry.scannedBy),
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        // Eco score badge
        if (ecoScore != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ecoScoreColor(ecoScore).copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "$ecoScore%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ecoScoreColor(ecoScore)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEEEEEE))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("—", fontSize = 12.sp, color = Color.Gray)
            }
        }

        Text(s.chevronRight, fontSize = 18.sp, color = Color(0xFFBDBDBD))
    }
}
