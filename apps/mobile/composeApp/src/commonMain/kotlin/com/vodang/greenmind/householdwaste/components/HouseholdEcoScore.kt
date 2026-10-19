package com.vodang.greenmind.householdwaste.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.vodang.greenmind.fmt
import com.vodang.greenmind.i18n.LocalAppStrings

private val green700 = Color(0xFF2E7D32)
private val green50  = Color(0xFFE8F5E9)
private val red600   = Color(0xFFDC2626)
private val red50    = Color(0xFFFEF2F2)
private val gray400c = Color(0xFF9CA3AF)
private val gray700c = Color(0xFF374151)

@Composable
internal fun EcoScoreRow(entry: GreenScoreEntryDto?) {
    val s = LocalAppStrings.current
    val hasDetail = entry != null && (!entry.items.isNullOrEmpty() || !entry.reasons.isNullOrEmpty())
    var expanded  by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (entry != null && entry.finalScore >= 50) green50 else Color(0xFFF5F5F5)),
    ) {
        // ── Summary row ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (hasDetail) Modifier.clickable { expanded = !expanded } else Modifier)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                entry == null          -> Color(0xFFE0E0E0)
                                entry.finalScore >= 50 -> green50
                                else                   -> red50
                            }
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = when {
                            entry == null          -> Icons.Filled.Eco
                            entry.finalScore >= 70 -> Icons.Filled.EmojiEvents
                            entry.finalScore >= 40 -> Icons.Filled.Eco
                            else                   -> Icons.Filled.Warning
                        },
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = when {
                            entry == null          -> gray400c
                            entry.finalScore >= 70 -> green700
                            entry.finalScore >= 40 -> green700
                            else                   -> red600
                        }
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(s.ecoScore, fontSize = 11.sp, color = gray400c)
                    Text(
                        if (entry == null) "—" else "${entry.finalScore}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (entry != null && entry.finalScore >= 50) green700 else gray400c,
                    )
                }
            }
            if (hasDetail) {
                Text(
                    if (expanded) "▲" else "▼",
                    fontSize = 10.sp,
                    color = gray400c,
                )
            }
        }

        // ── Expanded detail ───────────────────────────────────────────────────
        if (expanded && entry != null) {
            HorizontalDivider(color = Color(0xFFDDDDDD).copy(alpha = 0.6f))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Items
                if (!entry.items.isNullOrEmpty()) {
                    Text(s.detectedItems, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = gray700c)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        entry.items.forEach { item ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(s.itemBullet(item.name), fontSize = 11.sp, color = gray700c, modifier = Modifier.weight(1f))
                                Text(
                                    "×${item.quantity}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = gray400c,
                                )
                            }
                        }
                    }
                }

                // Reasons
                if (!entry.reasons.isNullOrEmpty()) {
                    if (!entry.items.isNullOrEmpty()) HorizontalDivider(color = Color(0xFFDDDDDD).copy(alpha = 0.4f))
                    Text(s.scoreBreakdown, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = gray700c)
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        entry.reasons.forEach { reason ->
                            val isPositive = reason.contains("→ +") || reason.contains("(tốt)")
                            val isNegative = reason.contains("→ -") || reason.contains("(gây hại)")
                            val textColor = when {
                                isPositive -> green700
                                isNegative -> Color(0xFFDC2626)
                                else       -> gray700c
                            }
                            Text(
                                "• $reason",
                                fontSize = 11.sp,
                                color = textColor,
                                lineHeight = 16.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}
