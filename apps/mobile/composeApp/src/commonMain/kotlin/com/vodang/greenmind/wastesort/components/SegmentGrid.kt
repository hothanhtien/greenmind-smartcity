package com.vodang.greenmind.wastesort.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vodang.greenmind.wastereport.NetworkImage
import com.vodang.greenmind.wastesort.categoryBg

@Composable
fun SegmentGrid(imageUrls: List<String>, category: String) {
    val chunks = imageUrls.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        chunks.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                row.forEach { url ->
                    SegmentCell(imageUrl = url, category = category, modifier = Modifier.weight(1f))
                }
                repeat(3 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun SegmentCell(imageUrl: String, category: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = categoryBg(category)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        NetworkImage(
            url = imageUrl,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )
    }
}
