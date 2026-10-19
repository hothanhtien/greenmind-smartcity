package com.vodang.greenmind.householdwaste.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.fmt
import com.vodang.greenmind.scandetail.ScanDetailData
import com.vodang.greenmind.theme.Green700
import com.vodang.greenmind.theme.TextSecondary
import com.vodang.greenmind.wastereport.NetworkImage
import com.vodang.greenmind.wastesort.WasteSortStatus
import com.vodang.greenmind.wastesort.label

@Composable
internal fun WasteStatusCard(
    scan: ScanDetailData,
    onClick: () -> Unit,
) {
    val statusBg = when (scan.status) {
        WasteSortStatus.SORTED -> Color(0xFFE3F2FD)
        WasteSortStatus.BRINGOUTED -> Color(0xFFFEF3C7)
        WasteSortStatus.COLLECTED -> Color(0xFFE8F5E9)
    }
    val statusFg = when (scan.status) {
        WasteSortStatus.SORTED -> Color(0xFF1565C0)
        WasteSortStatus.BRINGOUTED -> Color(0xFFB45309)
        WasteSortStatus.COLLECTED -> Color(0xFF2E7D32)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NetworkImage(
                url = scan.imageUrl,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            scan.status.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusFg,
                        )
                    }
                    Text(
                        scan.createdAt,
                        fontSize = 10.sp,
                        color = TextSecondary,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    scan.totalObjects?.let {
                        Text(
                            "$it objects",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF374151),
                        )
                    }
                    scan.totalMassKg?.let {
                        Text(
                            "· ${"%.2f".fmt(it)} kg",
                            fontSize = 12.sp,
                            color = Green700,
                        )
                    }
                }

                scan.items?.take(2)?.joinToString(" · ") { it.name }?.let {
                    Text(
                        it,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}