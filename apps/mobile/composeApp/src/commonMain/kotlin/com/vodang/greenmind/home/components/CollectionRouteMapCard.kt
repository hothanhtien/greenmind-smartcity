package com.vodang.greenmind.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings

@Composable
fun CollectionRouteMapCard(
    points: List<RouteMapPoint>,
    onRouteOrderChanged: ((List<Int>) -> Unit)? = null,
) {
    val s = LocalAppStrings.current
    SectionCard {
        Text(s.routeMapCardTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        // TODO: Replace sampleRoutePoints with live waypoints from GET /collector/route
        RouteMap(points = points, height = 300.dp, onRouteOrderChanged = onRouteOrderChanged)
        Spacer(Modifier.height(10.dp))
        // Legend: start → stops → end
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendDot(Color(0xFF1976D2))
            Text(s.routeMapLegendStart, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
            LegendDot(Color(0xFF388E3C))
            Text(s.routeMapLegendStop, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
            LegendDot(Color(0xFFD32F2F))
            Text(s.routeMapLegendEnd, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun LegendDot(color: Color) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(RoundedCornerShape(50))
            .background(color)
    )
}
