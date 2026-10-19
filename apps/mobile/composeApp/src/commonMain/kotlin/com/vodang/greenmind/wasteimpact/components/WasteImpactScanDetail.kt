package com.vodang.greenmind.wasteimpact.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.fmt
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.wastereport.NetworkImage
import com.vodang.greenmind.wastesort.WasteSortEntry

// All palette colours live in WasteImpactPalette.

// Friendly display names for raw pollution keys (shared with WasteImpactScreen)
private val pollutantLabel = mapOf(
    "CO2"               to "CO₂",
    "dioxin"            to "Dioxin",
    "microplastic"      to "Microplastic",
    "toxic_chemicals"   to "Toxic chemicals",
    "non_biodegradable" to "Non-biodegradable",
    "NOx"               to "NOₓ",
    "SO2"               to "SO₂",
    "CH4"               to "CH₄",
    "PM2.5"             to "PM2.5",
    "Pb"                to "Lead (Pb)",
    "Hg"                to "Mercury (Hg)",
    "Cd"                to "Cadmium (Cd)",
    "nitrate"           to "Nitrate",
    "chemical_residue"  to "Chemical residue",
    "styrene"           to "Styrene",
)

private val allPollutantKeys = listOf(
    "CO2", "CH4", "PM2.5", "NOx", "SO2",
    "Pb", "Hg", "Cd",
    "nitrate", "chemical_residue", "microplastic",
    "dioxin", "toxic_chemicals", "non_biodegradable", "styrene",
)

// ── Scan detail screen ─────────────────────────────────────────────────────────

@Composable
fun WasteImpactScanDetail(entry: WasteSortEntry, onBack: () -> Unit) {
    val s   = LocalAppStrings.current
    val result = entry.pollutantResult

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TextButton(onClick = onBack) {
                    Text(s.backArrow, fontSize = 14.sp, color = orange700)
                }
                Column {
                    Text(
                        s.scanDetail,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212121)
                    )
                    Text(entry.createdAt, fontSize = 11.sp, color = Color.Gray)
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Scan image ────────────────────────────────────────────────
                val imgUrl = result?.imageUrl ?: entry.imageUrl
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEEEEEE))
                ) {
                    NetworkImage(url = imgUrl, modifier = Modifier.fillMaxSize())
                }

                // ── Summary chip row ────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryChip("${entry.totalObjects}", s.objects, orange600, Modifier.weight(1f).fillMaxHeight())
                    if (result != null) {
                        SummaryChip("${result.ecoScore}%", s.ecoScoreLabel(result.ecoScore), ecoScoreColor(result.ecoScore), Modifier.weight(1f).fillMaxHeight())
                    }
                    SummaryChip(entry.scannedBy, "By", Color(0xFF455A64), Modifier.weight(1f).fillMaxHeight())
                }

                // ── Detected items ───────────────────────────────────────────────
                if (result != null && result.items.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                s.itemsDetected,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF424242)
                            )
                            result.items.forEachIndexed { idx, item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(orange600.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${idx + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = orange600)
                                    }
                                    Text(item.name, fontSize = 13.sp, modifier = Modifier.weight(1f), color = Color(0xFF212121))
                                    Text(
                                        "×${item.quantity}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = orange700
                                    )
                                }
                                if (idx < result.items.lastIndex) {
                                    HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }

                // ── Impact ────────────────────────────────────────────────────
                if (result != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                s.pollutionImpact,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF424242)
                            )
                            ImpactMeter(s.airIcon, s.air,   result.impact.airPollution.toFloat())
                            ImpactMeter("💧", s.waterPollutionLabel, result.impact.waterPollution.toFloat())
                            ImpactMeter("🌱", s.soilPollutionLabel,  result.impact.soilPollution.toFloat())
                        }
                    }
                }

                // ── Pollutant breakdown ───────────────────────────────────────
                if (result != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                s.pollutantBreakdownAvg,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF424242)
                            )
                            allPollutantKeys.forEach { key ->
                                val value = result.pollution[key] ?: 0.0
                                val active = value > 0.0
                                val labelColor = if (active) orange700 else Color(0xFF9E9E9E)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (active) orange700 else Color(0xFFE0E0E0))
                                    )
                                    Text(
                                        pollutantLabel[key] ?: key,
                                        fontSize = 12.sp,
                                        color = Color(0xFF424242),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        if (active) value.fmt(3) else "0",
                                        fontSize = 12.sp,
                                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                                        color = labelColor
                                    )
                                }
                                if (active) {
                                    LinearProgressIndicator(
                                        progress = { value.toFloat().coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                                        color = orange700,
                                        trackColor = orange700.copy(alpha = 0.08f)
                                    )
                                }
                                if (key != allPollutantKeys.last()) {
                                    HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}
