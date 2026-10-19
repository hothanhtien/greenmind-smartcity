package com.vodang.greenmind.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.location.Location

@Composable
actual fun GarbageHeatMapView(
    points: List<GarbageMapPoint>,
    center: Location?,
    zoomLevel: Float,
    modifier: Modifier,
) {
    Box(
        modifier = modifier.background(Color(0xFFE8F5E9)),
        contentAlignment = Alignment.Center,
    ) {
        Text("🗺️  Map view not available on desktop", fontSize = 14.sp, color = Color(0xFF2E7D32))
    }
}

@Composable
actual fun RouteMapView(
    points: List<RouteMapPoint>,
    center: Location?,
    zoomLevel: Float,
    modifier: Modifier,
    onRouteOrderChanged: ((List<Int>) -> Unit)?,
) {
    Box(
        modifier = modifier.background(Color(0xFFE8F5E9)),
        contentAlignment = Alignment.Center,
    ) {
        Text("🗺️  Route map not available on desktop", fontSize = 14.sp, color = Color(0xFF2E7D32))
    }
}

@Composable
actual fun CampaignMapView(
    campaign: CampaignMapPoint,
    center: Location?,
    zoomLevel: Float,
    modifier: Modifier,
) {
    Box(
        modifier = modifier.background(Color(0xFFE8F5E9)),
        contentAlignment = Alignment.Center,
    ) {
        Text("🗺️  Campaign map not available on desktop", fontSize = 14.sp, color = Color(0xFF2E7D32))
    }
}
