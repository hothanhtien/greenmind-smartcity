package com.vodang.greenmind.wastesort.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.wastereport.NetworkImage
import com.vodang.greenmind.time.formatChatTime
import com.vodang.greenmind.wastesort.WasteSortEntry
import com.vodang.greenmind.wastesort.WasteSortStatus
import com.vodang.greenmind.wastesort.green50
import com.vodang.greenmind.wastesort.label
import com.vodang.greenmind.wastesort.green800

@Composable
fun ScanCard(entry: WasteSortEntry, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            NetworkImage(
                url = entry.imageUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 160.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            )

            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (entry.status) {
                                WasteSortStatus.SORTED     -> green50
                                WasteSortStatus.BRINGOUTED -> Color(0xFFE3F2FD)
                                WasteSortStatus.COLLECTED  -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        entry.status.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (entry.status) {
                            WasteSortStatus.SORTED     -> green800
                            WasteSortStatus.BRINGOUTED -> Color(0xFF1565C0)
                            WasteSortStatus.COLLECTED  -> green800
                        },
                    )
                }
                Text(formatChatTime(entry.createdAt), fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}
