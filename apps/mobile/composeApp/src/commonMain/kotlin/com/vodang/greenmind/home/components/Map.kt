package com.vodang.greenmind.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.location.Geo
import com.vodang.greenmind.location.Location
import kotlinx.coroutines.flow.collect

private const val ZOOM_MIN = 8f
private const val ZOOM_MAX = 18f
private const val ZOOM_STEP = 1f
private const val ZOOM_DEFAULT = 13.5f

/** OSM map with a Leaflet Routing Machine route drawn between [points], plus zoom and re-center controls. */
@Composable
fun RouteMap(
    points: List<RouteMapPoint>,
    modifier: Modifier = Modifier,
    height: Dp = 300.dp,
    /** Emits the OSRM-optimized order as 0-based indices into [points]. */
    onRouteOrderChanged: ((List<Int>) -> Unit)? = null,
) {
    var center by remember { mutableStateOf<Location?>(null) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }

    LaunchedEffect(Unit) {
        Geo.service.locationUpdates.collect { loc ->
            currentLocation = loc
            if (center == null) center = loc
        }
    }

    var zoomLevel by remember { mutableStateOf(13f) }

    val scrollConsumer = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource) = available
            override suspend fun onPreFling(available: Velocity) = available
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (height > 0.dp) Modifier.height(height) else Modifier)
            .nestedScroll(scrollConsumer)
    ) {
        RouteMapView(
            points = points,
            center = center,
            zoomLevel = zoomLevel,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    clip = true
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                },
            onRouteOrderChanged = onRouteOrderChanged,
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .shadow(2.dp, RoundedCornerShape(4.dp))
                .background(Color.White, RoundedCornerShape(4.dp))
                .width(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MapControlButton(label = "⌖", color = Color(0xFF1565C0)) {
                currentLocation?.let { center = it }
            }
            MapControlDivider()
            MapControlButton(label = "+", color = Color(0xFF2E7D32)) {
                if (zoomLevel < ZOOM_MAX) zoomLevel += ZOOM_STEP
            }
            MapControlDivider()
            MapControlButton(label = "−", color = Color(0xFF2E7D32)) {
                if (zoomLevel > ZOOM_MIN) zoomLevel -= ZOOM_STEP
            }
        }
    }
}

/** Plain OSM map with zoom and re-center controls. No heatmap data. */
@Composable
fun Map(
    modifier: Modifier = Modifier,
    height: Dp = 280.dp,
) {
    MapCore(points = emptyList(), modifier = modifier, height = height)
}

/** OSM map with a heatmap layer rendered from [points], plus zoom and re-center controls. */
@Composable
fun HeatMap(
    points: List<GarbageMapPoint>,
    modifier: Modifier = Modifier,
    height: Dp = 280.dp,
) {
    MapCore(points = points, modifier = modifier, height = height)
}

/** OSM map showing a campaign location with its radius circle, plus zoom and re-center controls. */
@Composable
fun CampaignMap(
    campaignLat: Double,
    campaignLng: Double,
    radius: Int,
    modifier: Modifier = Modifier,
    height: Dp = 280.dp,
    onRouteClick: (() -> Unit)? = null,
) {
    var center by remember { mutableStateOf<Location?>(null) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    LaunchedEffect(Unit) {
        Geo.service.locationUpdates.collect { loc ->
            currentLocation = loc
        }
    }

    // Focus on campaign location by default
    LaunchedEffect(campaignLat, campaignLng) {
        center = Location(campaignLat, campaignLng)
    }

    var zoomLevel by remember { mutableStateOf(14f) }

    val scrollConsumer = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource) = available
            override suspend fun onPreFling(available: Velocity) = available
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (height > 0.dp) Modifier.height(height) else Modifier)
            .nestedScroll(scrollConsumer)
    ) {
        CampaignMapView(
            campaign = CampaignMapPoint(campaignLat, campaignLng, radius),
            center = center,
            zoomLevel = zoomLevel,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    clip = true
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                }
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .shadow(2.dp, RoundedCornerShape(4.dp))
                .background(Color.White, RoundedCornerShape(4.dp))
                .width(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (onRouteClick != null) {
                MapControlButton(label = "↱", color = Color(0xFFE65100)) {
                    onRouteClick()
                }
                MapControlDivider()
            }
            MapControlButton(label = "⌖", color = Color(0xFF1565C0)) {
                currentLocation?.let { center = it }
            }
            MapControlDivider()
            MapControlButton(label = "+", color = Color(0xFF2E7D32)) {
                if (zoomLevel < ZOOM_MAX) zoomLevel += ZOOM_STEP
            }
            MapControlDivider()
            MapControlButton(label = "−", color = Color(0xFF2E7D32)) {
                if (zoomLevel > ZOOM_MIN) zoomLevel -= ZOOM_STEP
            }
        }
    }
}

@Composable
private fun MapCore(
    points: List<GarbageMapPoint>,
    modifier: Modifier,
    height: Dp,
) {
    var center by remember { mutableStateOf<Location?>(null) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    LaunchedEffect(Unit) {
        Geo.service.locationUpdates.collect { loc ->
            currentLocation = loc
            if (center == null) center = loc
        }
    }

    var zoomLevel by remember { mutableStateOf(ZOOM_DEFAULT) }

    val scrollConsumer = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource) = available
            override suspend fun onPreFling(available: Velocity) = available
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .nestedScroll(scrollConsumer)
    ) {
        GarbageHeatMapView(
            points = points,
            center = center,
            zoomLevel = zoomLevel,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    clip = true
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                }
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .shadow(2.dp, RoundedCornerShape(4.dp))
                .background(Color.White, RoundedCornerShape(4.dp))
                .width(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MapControlButton(label = "⌖", color = Color(0xFF1565C0)) {
                currentLocation?.let { center = it }
            }
            MapControlDivider()
            MapControlButton(label = "+", color = Color(0xFF2E7D32)) {
                if (zoomLevel < ZOOM_MAX) zoomLevel += ZOOM_STEP
            }
            MapControlDivider()
            MapControlButton(label = "−", color = Color(0xFF2E7D32)) {
                if (zoomLevel > ZOOM_MIN) zoomLevel -= ZOOM_STEP
            }
        }
    }
}

@Composable
private fun MapControlButton(label: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 16.sp, color = color, textAlign = TextAlign.Center)
    }
}

@Composable
private fun MapControlDivider() {
    Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Color(0xFFE0E0E0)))
}
