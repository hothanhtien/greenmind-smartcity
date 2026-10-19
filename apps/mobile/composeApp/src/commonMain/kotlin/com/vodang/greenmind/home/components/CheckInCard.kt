package com.vodang.greenmind.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.location.Geo
import com.vodang.greenmind.store.haversineMeters

private val green800c = Color(0xFF2E7D32)

@Composable
fun CheckInCard(
    points: List<WastePoint>,
    /** Pending points pre-sorted by OSRM-optimized route order. Null = use API order. */
    sortedPendingPoints: List<WastePoint>? = null,
    /** True while a check-in is in-flight (camera open, uploading, or batching). Disables the button. */
    isBusy: Boolean = false,
    modifier: Modifier = Modifier,
    onCheckInClick: (group: List<WastePoint>) -> Unit,
) {
    val s = LocalAppStrings.current
    val groups: List<Map.Entry<String, List<WastePoint>>> = remember(points, sortedPendingPoints) {
        val filtered = points.filter { !it.collected }
        val ordered = sortedPendingPoints ?: filtered
        val byAddress = filtered.groupBy { it.groupKey }
        ordered.map { it.groupKey }.distinct()
            .mapNotNull { addr -> byAddress[addr]?.let { records -> object : Map.Entry<String, List<WastePoint>> { override val key = addr; override val value = records } } }
    }
    val nextGroup: Map.Entry<String, List<WastePoint>>? = groups.firstOrNull()

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 10.dp,
    ) {
        if (nextGroup == null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Filled.Celebration, contentDescription = null, modifier = Modifier.size(28.dp), tint = green800c)
                Column(modifier = Modifier.weight(1f)) {
                    Text(s.checkInDone, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = green800c)
                    Text(s.checkInDoneDesc, fontSize = 11.sp, color = Color.Gray)
                }
            }
        } else {
            val groupPoints: List<WastePoint> = nextGroup.value
            val firstPoint: WastePoint = groupPoints.first()
            val totalBags = groupPoints.sumOf { it.bags }

            val currentLoc by Geo.service.locationUpdates.collectAsState(initial = null)
            val nextLat = firstPoint.lat
            val nextLng = firstPoint.lng

            var isTooFar = false
            var distanceMeters: Double? = null
            if (currentLoc != null && nextLat != null && nextLng != null) {
                distanceMeters = haversineMeters(currentLoc!!.latitude, currentLoc!!.longitude, nextLat, nextLng)
                isTooFar = distanceMeters > 50.0
            }

            val btnColor = when {
                isBusy -> Color.Gray
                nextLat != null && currentLoc == null -> Color.Gray
                isTooFar -> Color.Gray
                else -> green800c
            }
            val btnText = when {
                isBusy -> "Đang xử lý..."
                nextLat != null && currentLoc == null -> "Đang lấy vị trí..."
                isTooFar -> "Quá xa (${distanceMeters?.toInt()}m)"
                groupPoints.size > 1 -> "${s.checkInButton} (${groupPoints.size})"
                else -> s.checkInButton
            }

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(s.nextPointLabel, fontSize = 10.sp, color = Color.Gray)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        firstPoint.address,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "${groupPoints.size} records · $totalBags ${s.bagsEstimatedFmt(totalBags)}",
                    fontSize = 11.sp,
                    color = Color.Gray,
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = btnColor,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (!isBusy && (nextLat == null || (currentLoc != null && !isTooFar))) {
                            onCheckInClick(groupPoints)
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(btnText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
