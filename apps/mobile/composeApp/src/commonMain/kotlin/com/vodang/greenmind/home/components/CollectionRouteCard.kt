package com.vodang.greenmind.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings

private val green800r = Color(0xFF2E7D32)

@Composable
fun CollectionRouteCard(
    points: List<WastePoint>,
    /** Pending points pre-sorted by OSRM-optimized route order. Null = use API order. */
    sortedPendingPoints: List<WastePoint>? = null,
) {
    val s = LocalAppStrings.current
    val pendingPoints = remember(points) { points.filter { !it.collected } }
    val groups: List<Map.Entry<String, List<WastePoint>>> = remember(pendingPoints, sortedPendingPoints) {
        val ordered = sortedPendingPoints ?: pendingPoints
        val byAddress = pendingPoints.groupBy { it.address }
        ordered.map { it.address }.distinct()
            .mapNotNull { addr -> byAddress[addr]?.let { records -> object : Map.Entry<String, List<WastePoint>> { override val key = addr; override val value = records } } }
    }

    SectionCard {
        Text(s.routeCardTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(10.dp))
        groups.forEachIndexed { index, entry ->
            val address = entry.key
            val groupPoints = entry.value
            val allCollected = groupPoints.all { pt -> pt.collected }
            val totalBags = groupPoints.sumOf { pt -> pt.bags }

            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxHeight()) {
                    Box(modifier = Modifier.size(14.dp).background(if (allCollected) green800r else Color(0xFFBDBDBD), CircleShape))
                    if (index < groups.lastIndex) {
                        Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color(0xFFE0E0E0)))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.padding(bottom = if (index < groups.lastIndex) 16.dp else 4.dp)) {
                    Text(address, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (allCollected) Color.Gray else Color.Black)
                    Text(
                        "${groupPoints.size} records · $totalBags ${s.bagsUnit}" + if (allCollected) " ✓" else "",
                        fontSize = 11.sp,
                        color = if (allCollected) green800r else Color.Gray
                    )
                    Spacer(Modifier.height(4.dp))
                    groupPoints.forEach { pt ->
                        Text(
                            "- ${pt.zone} (${pt.bags} ${s.bagsUnit})" + if (pt.collected) " ✓" else "",
                            fontSize = 11.sp,
                            color = if (pt.collected) green800r else Color.Gray
                        )
                    }
                }
            }
        }
    }
}
