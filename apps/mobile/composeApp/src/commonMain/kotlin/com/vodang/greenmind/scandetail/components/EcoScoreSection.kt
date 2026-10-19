package com.vodang.greenmind.scandetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Star
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
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.scandetail.ecoGreen700
import com.vodang.greenmind.scandetail.ecoGreen50
import com.vodang.greenmind.scandetail.ecoRed600
import com.vodang.greenmind.scandetail.neutralGray400
import com.vodang.greenmind.scandetail.neutralGray700

// ── Eco Score Section ─────────────────────────────────────────────────────────

@Composable
fun EcoScoreSection(
    greenScore: GreenScoreEntryDto?,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
) {
    val s = LocalAppStrings.current

    val hasDetail = greenScore != null &&
        (!greenScore.items.isNullOrEmpty() || !greenScore.reasons.isNullOrEmpty())
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White),
    ) {
        // Summary row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (hasDetail) Modifier.clickable { expanded = !expanded } else Modifier)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(s.ecoScore, fontSize = 12.sp, color = neutralGray400)
                    if (hasDetail) {
                        Text(if (expanded) "▲" else "▼", fontSize = 9.sp, color = neutralGray400)
                    }
                }
                when {
                    isLoading -> {
                        Text("...", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = neutralGray400)
                        Text(s.calculatingScore, fontSize = 11.sp, color = neutralGray400)
                    }
                    greenScore == null -> {
                        Text(s.noScoreYet, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = neutralGray400)
                        Text(s.noScoreYet, fontSize = 11.sp, color = neutralGray400)
                    }
                    else -> {
                        Text(
                            "${greenScore.finalScore}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ecoGreen700,
                        )
                        Text("${greenScore.finalScore} pts", fontSize = 11.sp, color = ecoGreen700.copy(alpha = 0.7f))
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isLoading -> Color(0xFFFFF3E0)
                            greenScore == null -> Color(0xFFF5F5F5)
                            greenScore.finalScore >= 50 -> ecoGreen50
                            else -> Color(0xFFFEF2F2)
                        }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFFE65100),
                    )
                } else {
                    val icon = when {
                        greenScore == null -> Icons.Filled.Eco
                        greenScore.finalScore >= 70 -> Icons.Filled.Star
                        greenScore.finalScore >= 40 -> Icons.Filled.Favorite
                        else -> Icons.Filled.Warning
                    }
                    val tint = when {
                        greenScore == null -> neutralGray400
                        greenScore.finalScore >= 50 -> ecoGreen700
                        else -> Color(0xFFD32F2F)
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Expanded detail
        if (expanded && greenScore != null) {
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Items
                if (!greenScore.items.isNullOrEmpty()) {
                    Text(s.detectedItems, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = neutralGray700)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        greenScore.items.forEach { item ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("• ${item.name}", fontSize = 12.sp, color = neutralGray700, modifier = Modifier.weight(1f))
                                Text("×${item.quantity}", fontSize = 12.sp, color = neutralGray400, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // Reasons
                if (!greenScore.reasons.isNullOrEmpty()) {
                    if (!greenScore.items.isNullOrEmpty()) HorizontalDivider(color = Color(0xFFEEEEEE))
                    Text(s.scoreBreakdown, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = neutralGray700)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        greenScore.reasons.forEach { reason ->
                            val isPositive = reason.contains("→ +") || reason.contains("(tốt)")
                            val isNegative = reason.contains("→ -") || reason.contains("(gây hại)")
                            val textColor = when {
                                isPositive -> ecoGreen700
                                isNegative -> ecoRed600
                                else -> neutralGray700
                            }
                            Text("• $reason", fontSize = 12.sp, color = textColor, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
    }
}