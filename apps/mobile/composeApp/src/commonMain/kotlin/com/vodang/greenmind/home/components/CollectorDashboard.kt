package com.vodang.greenmind.home.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.auth.UserDto
import com.vodang.greenmind.api.wastecollector.CollectorDetectRecordDto
import com.vodang.greenmind.api.wastecollector.getBroughtOut
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.SettingsStore
import kotlinx.coroutines.launch

private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF388E3C)
private val green50 = Color(0xFFE8F5E9)
private val amber600 = Color(0xFFFFB300)
private val blue600 = Color(0xFF1976D2)
private val blue50 = Color(0xFFE3F2FD)

data class WastePoint(val id: Int, val reportId: String, val address: String, val groupKey: String, val zone: String, val collected: Boolean, val bags: Int, val lat: Double?, val lng: Double?)

private fun CollectorDetectRecordDto.toWastePoint(index: Int, unknownLocation: String) = WastePoint(
    id = index,
    reportId = id,
    address = household?.address ?: unknownLocation,
    groupKey = household?.address ?: unknownLocation,
    zone = detectType,
    collected = status == "picked_up" || status == "done" || status == "completed" || status == "resolved",
    bags = totalObjects ?: totalMassKg?.toInt() ?: 0,
    lat = household?.lat?.toDoubleOrNull(),
    lng = household?.lng?.toDoubleOrNull(),
)

@Composable
fun CollectorDashboard(user: UserDto? = null, scrollState: ScrollState = rememberScrollState()) {
    val s = LocalAppStrings.current

    val scope = rememberCoroutineScope()
    var reports by remember { mutableStateOf<List<CollectorDetectRecordDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var checkInReportId by remember { mutableStateOf<String?>(null) }
    // OSRM-optimized order as indices into routedPendingPoints (see below).
    var sortedIndices by remember { mutableStateOf<List<Int>?>(null) }

    LaunchedEffect(Unit) {
        val token = SettingsStore.getAccessToken() ?: run { isLoading = false; return@LaunchedEffect }
        try {
            reports = getBroughtOut(token).data
            errorMsg = null
        } catch (e: Throwable) {
            errorMsg = e.message
        } finally {
            isLoading = false
        }
    }

    val points = reports.mapIndexed { i, dto -> dto.toWastePoint(i, s.unknownLocation) }
    val pendingPoints = points.filter { !it.collected }
    // Only pending points with valid coords can be routed by OSRM.
    // routedPendingPoints[i] corresponds 1:1 with routePoints[i] — same index across both lists,
    // so OSRM-returned indices map back unambiguously even if multiple stops share coordinates.
    val routedPendingPoints = pendingPoints.filter { it.lat != null && it.lng != null }
    val routePoints = routedPendingPoints.map {
        RouteMapPoint(lat = it.lat!!, lng = it.lng!!, label = it.address)
    }
    // When OSRM has sorted, expose all pending points in route order:
    // routed stops first (OSRM-optimized), then any stops missing coords appended at the end.
    val sortedPendingPoints: List<WastePoint>? = sortedIndices?.let { idxs ->
        val routed = idxs.mapNotNull { routedPendingPoints.getOrNull(it) }
        if (routed.isEmpty()) null
        else routed + pendingPoints.filter { it.lat == null || it.lng == null }
    }
    val collectedCount = points.count { it.collected }
    val totalCount = points.size

    val token = SettingsStore.getAccessToken() ?: ""

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = amber600)
                    }
                }
                errorMsg != null -> {
                    SectionCard {
                        Text(
                            s.errorDisplay(errorMsg ?: ""),
                            fontSize = 13.sp,
                            color = Color(0xFFC62828),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                        )
                    }
                }
                else -> {
                    CollectionRouteMapCard(points = routePoints, onRouteOrderChanged = { sortedIndices = it })
                    CollectionRouteCard(points, sortedPendingPoints = sortedPendingPoints)
                    // Reserve space so the sticky CheckInCard doesn't cover the last item when scrolled to bottom.
                    Spacer(Modifier.height(140.dp))
                }
            }
        }

        // Sticky bottom action card — always reachable, easy to thumb-tap.
        if (!isLoading && errorMsg == null) {
            CheckInCard(
                points = points,
                sortedPendingPoints = sortedPendingPoints,
                isBusy = checkInReportId != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                onCheckInClick = { group -> checkInReportId = group.firstOrNull()?.reportId },
            )
        }

        CheckInScanFlow(
            allPoints = points,
            reportId = checkInReportId,
            accessToken = token,
            onSuccess = {
                checkInReportId = null
                // Stale indices reference the OLD routedPendingPoints; clear so cards
                // fall back to API order until the new OSRM trip resolves.
                sortedIndices = null
                scope.launch {
                    runCatching {
                        reports = getBroughtOut(token).data
                    }
                }
            },
            onDismiss = { checkInReportId = null },
        )
    }
}
