package com.vodang.greenmind.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.components.CircularArcProgress
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.HouseholdStore
import com.vodang.greenmind.store.WalkDistanceStore
import com.vodang.greenmind.store.WasteSortStore
import com.vodang.greenmind.theme.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

@Composable
fun StatsSummaryCard() {
    val s = LocalAppStrings.current
    val greenScoreEntries by HouseholdStore.greenScoreEntries.collectAsState()
    val walkDistance by WalkDistanceStore.distanceMeters.collectAsState()
    val wasteKg by WasteSortStore.todayTotalKg.collectAsState()

    // Load waste data from API on first composition
    LaunchedEffect(Unit) {
        WasteSortStore.loadFromApi()
    }

    val greenScore = greenScoreEntries.lastOrNull()?.finalScore ?: 0
    val walkKm = (walkDistance / 1000.0 * 10).toInt() / 10.0
    val wasteKgDisplay = if (wasteKg > 0) "${wasteKg.toInt()}kg" else "0"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = s.todayOverview,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Filled.Eco,
                    value = "$greenScore",
                    label = s.greenScoreShort,
                    bgColor = GreenBg50,
                    iconColor = GreenIcon,
                    progress = greenScore / 100f
                )
                StatItem(
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    value = "${walkKm}km",
                    label = s.walkDistanceShort,
                    bgColor = GreenBg50,
                    iconColor = GreenIcon,
                    progress = null
                )
                StatItem(
                    icon = Icons.Filled.Delete,
                    value = wasteKgDisplay,
                    label = s.wasteKgShort,
                    bgColor = RedBg50,
                    iconColor = RedIcon,
                    progress = null
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    bgColor: Color,
    iconColor: Color,
    progress: Float?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier.size(52.dp),
            contentAlignment = Alignment.Center
        ) {
            if (progress != null) {
                CircularArcProgress(
                    progress = progress,
                    color = iconColor,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 4f
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = iconColor
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextSecondary
        )
    }
}