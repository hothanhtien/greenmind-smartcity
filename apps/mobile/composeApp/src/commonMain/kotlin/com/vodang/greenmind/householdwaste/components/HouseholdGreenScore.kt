package com.vodang.greenmind.householdwaste.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.households.GreenScoreEntryDto
import com.vodang.greenmind.householdwaste.RemainingCard
import com.vodang.greenmind.i18n.LocalAppStrings

private val green700 = Color(0xFF2E7D32)
private val green50  = Color(0xFFE8F5E9)
private val red600   = Color(0xFFDC2626)
private val red50    = Color(0xFFFEF2F2)
private val gray700  = Color(0xFF374151)
private val gray400  = Color(0xFF9CA3AF)

@Composable
fun GreenScoreSection(
    entries: List<GreenScoreEntryDto>,
    onOpenDetail: (GreenScoreEntryDto) -> Unit,
    onViewAll: () -> Unit,
) {
    val s = LocalAppStrings.current
    val latest = entries.last()

    // Current score card
    Card(
        Modifier.fillMaxWidth().clickable { onOpenDetail(latest) },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(s.currentScore, fontSize = 12.sp, color = gray400)
                Text("${latest.finalScore}", fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = green700)
                Text("${latest.finalScore} pts", fontSize = 12.sp, color = green700.copy(alpha = 0.7f))
            }
            // Score ring
            Box(
                Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (latest.finalScore >= 50) green50 else red50),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        latest.finalScore >= 70 -> Icons.Filled.EmojiEvents
                        latest.finalScore >= 40 -> Icons.Filled.Eco
                        else -> Icons.Filled.Warning
                    },
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = when {
                        latest.finalScore >= 70 -> green700
                        latest.finalScore >= 40 -> green700
                        else -> red600
                    }
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    // Score history
    Text(s.scoreHistory, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray700)
    Spacer(Modifier.height(4.dp))

    val reversedEntries = entries.reversed()
    reversedEntries.drop(1).take(3).forEach { entry ->
        ScoreEntryRow(entry, onOpenDetail)
        Spacer(Modifier.height(8.dp))
    }

    val remaining = reversedEntries.size - 1 - 3
    if (remaining > 0) {
        RemainingCard(remaining, "history entry", onClick = onViewAll)
    }
}

@Composable
fun ScoreEntryRow(entry: GreenScoreEntryDto, onOpenDetail: (GreenScoreEntryDto) -> Unit) {
    val s = LocalAppStrings.current
    val isPositive = entry.delta >= 0
    val deltaColor = if (isPositive) green700 else red600
    val deltaBg    = if (isPositive) green50 else red50
    val deltaText  = if (isPositive) "+${entry.delta}" else "${entry.delta}"

    Card(
        Modifier.fillMaxWidth().clickable { onOpenDetail(entry) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            // ── Header row ────────────────────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(entry.createdAt.take(10), fontSize = 11.sp, color = gray400)
                    Text(s.chevronRight, fontSize = 12.sp, color = gray400)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(s.scoreTransition(entry.previousScore, entry.finalScore), fontSize = 12.sp, color = gray400)
                    Text("${entry.finalScore}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = gray700)
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(deltaBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(deltaText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = deltaColor)
                    }
                }
            }
            // details are shown in a bottom sheet via onOpenDetail
        }
    }
}

@Composable
fun GreenScoreDetailSheet(entry: GreenScoreEntryDto, onDismiss: () -> Unit) {
    val s = LocalAppStrings.current
    val isPositive = entry.delta >= 0
    val deltaColor = if (isPositive) green700 else red600
    val deltaBg    = if (isPositive) green50 else red50
    val deltaText  = if (isPositive) "+${entry.delta}" else "${entry.delta}"

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x80000000))
            .clickable { onDismiss() }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Color.White)
                .clickable { /* block propagation */ }
                .padding(16.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(entry.createdAt.take(10), fontSize = 13.sp, color = gray400)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(s.scoreLabel(entry.finalScore), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = green700)
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(deltaBg)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(deltaText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = deltaColor)
                        }
                    }
                    Text("${entry.previousScore} → ${entry.finalScore}", fontSize = 12.sp, color = gray400)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }
            Spacer(Modifier.height(12.dp))

            // ── Detected items ────────────────────────────────────────────
            if (!entry.items.isNullOrEmpty()) {
                Text(s.detectedItems, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray700)
                Spacer(Modifier.height(6.dp))
                entry.items.forEach { item ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• ${item.name}", fontSize = 13.sp, color = gray700, modifier = Modifier.weight(1f))
                        Text("×${item.quantity}", fontSize = 13.sp, color = gray400)
                    }
                    Spacer(Modifier.height(6.dp))
                }
                Spacer(Modifier.height(4.dp))
            }

            // ── Score breakdown / reasons ─────────────────────────────────
            if (!entry.reasons.isNullOrEmpty()) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(Modifier.height(8.dp))
                Text(s.scoreBreakdown, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray700)
                Spacer(Modifier.height(6.dp))
                entry.reasons.forEach { reason ->
                    val textColor = when {
                        reason.contains("→ +") || reason.contains("(tốt)")     -> green700
                        reason.contains("→ -") || reason.contains("(gây hại)") -> red600
                        else -> gray700
                    }
                    Text("• $reason", fontSize = 13.sp, color = textColor, lineHeight = 18.sp)
                    Spacer(Modifier.height(6.dp))
                }
            }

            // ── No detail at all ──────────────────────────────────────────
            if (entry.items.isNullOrEmpty() && entry.reasons.isNullOrEmpty()) {
                Text(s.noDetailAvailable, fontSize = 13.sp, color = gray400)
            }

            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    }
}