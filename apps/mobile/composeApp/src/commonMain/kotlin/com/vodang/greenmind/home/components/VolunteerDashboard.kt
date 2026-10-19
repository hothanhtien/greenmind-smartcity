package com.vodang.greenmind.home.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.auth.UserDto
import com.vodang.greenmind.api.campaign.CampaignDto
import com.vodang.greenmind.api.campaign.CampaignParticipant
import com.vodang.greenmind.api.campaign.CampaignParticipantUser
import com.vodang.greenmind.api.campaign.getAllCampaigns
import com.vodang.greenmind.api.campaign.toCampaignParticipant
import com.vodang.greenmind.api.participantcampaign.checkInCampaign
import com.vodang.greenmind.api.participantcampaign.checkOutCampaign
import com.vodang.greenmind.api.participantcampaign.registerCampaign
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.location.Geo
import com.vodang.greenmind.location.Location
import com.vodang.greenmind.platform.BackHandler
import com.vodang.greenmind.store.SettingsStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.math.*

/** Haversine distance in meters between two lat/lng points */
private fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val r = 6371000.0 // Earth radius in meters
    val toRad = kotlin.math.PI / 180.0
    val dLat = (lat2 - lat1) * toRad
    val dLng = (lng2 - lng1) * toRad
    val a = sin(dLat / 2).pow(2) + cos(lat1 * toRad) * cos(lat2 * toRad) * sin(dLng / 2).pow(2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}

/** Returns distance in meters from user location to campaign, or null if location unavailable */
private fun campaignDistance(userLocation: Location?, campaign: CampaignDto): Double? =
    userLocation?.let { loc -> distanceMeters(loc.latitude, loc.longitude, campaign.lat, campaign.lng) }

private fun isInRange(dist: Double, radius: Int) = dist <= radius

private val green800v = Color(0xFF2E7D32)
private val green600v = Color(0xFF388E3C)
private val green50v  = Color(0xFFE8F5E9)
private val blue600v  = Color(0xFF1976D2)
private val blue50v   = Color(0xFFE3F2FD)
private val teal600v  = Color(0xFF00897B)
private val teal50v   = Color(0xFFE0F2F1)
private val orange600v = Color(0xFFF57C00)
private val orange50v  = Color(0xFFFFF3E0)

// Per-campaign mutable state tracked in memory for this session
private data class CampaignUiState(
    val participant: CampaignParticipant? = null,
    val busy: Boolean = false,
    val error: String? = null,
)

@Composable
fun VolunteerDashboard(
    user: UserDto? = null,
    scrollState: ScrollState = rememberScrollState(),
    onSelectCampaign: (CampaignDto) -> Unit = {},
) {
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()

    var campaigns by remember { mutableStateOf<List<CampaignDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var selectedCampaignId by remember { mutableStateOf<String?>(null) }
    // Track participant state per campaign id
    val campaignStates = remember { mutableStateMapOf<String, CampaignUiState>() }

    val accessToken = remember { SettingsStore.getAccessToken() ?: "" }
    val currentUser = remember { SettingsStore.getUser() }

    BackHandler(enabled = selectedCampaignId != null) {
        selectedCampaignId = null
    }

    // Show detail screen
    val campaignId = selectedCampaignId
    if (campaignId != null) {
        val campaign = campaigns.find { it.id == campaignId }
        if (campaign != null) {
            onSelectCampaign(campaign)
            selectedCampaignId = null
            return
        }
    }

    // Load campaigns (participation info is embedded in each campaign)
    LaunchedEffect(accessToken) {
        if (accessToken.isBlank()) { loading = false; return@LaunchedEffect }
        loading = true
        loadError = null
        try {
            // Get user location first
            userLocation = Geo.service.locationUpdates.firstOrNull()
            val fetchedCampaigns = getAllCampaigns(accessToken)
            val currentUserId = SettingsStore.getUser()?.id ?: ""
            // Pre-populate campaignStates from embedded participants list
            fetchedCampaigns.forEach { campaign ->
                val myParticipation = campaign.participants.find { it.user.id == currentUserId }
                if (myParticipation != null && !campaignStates.containsKey(campaign.id)) {
                    campaignStates[campaign.id] = CampaignUiState(participant = myParticipation)
                }
            }
            campaigns = fetchedCampaigns
        } catch (e: Throwable) {
            loadError = e.message ?: s.volunteerLoadError
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OceanScoreCard()

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(Icons.Filled.Timer, s.volunteerHoursLabel, s.volunteerHoursValue, "", teal50v, teal600v, teal600v, Modifier.weight(1f).aspectRatio(1f))
            MetricCard(Icons.Filled.CalendarToday, s.volunteerEventsLabel, s.volunteerEventsValue, "", green50v, green800v, green800v, Modifier.weight(1f).aspectRatio(1f))
            MetricCard(Icons.Filled.Star, s.volunteerPointsLabel, s.volunteerPointsValue, "", blue50v, blue600v, blue600v, Modifier.weight(1f).aspectRatio(1f))
        }

        // Campaign list section
        SectionCard {
            Text(s.volunteerEventsCardTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))

            when {
                loading -> {
                    Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = green800v)
                            Text(s.volunteerLoading, fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }
                loadError != null -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(s.errorDisplay(loadError ?: ""), fontSize = 13.sp, color = Color(0xFFB71C1C))
                        TextButton(onClick = {
                            scope.launch {
                                loading = true
                                loadError = null
                                try { campaigns = getAllCampaigns(accessToken) }
                                catch (e: Throwable) { loadError = e.message ?: s.volunteerLoadError }
                                finally { loading = false }
                            }
                        }) {
                            Text(s.retry, fontSize = 12.sp, color = green800v)
                        }
                    }
                }
                campaigns.isEmpty() -> {
                    Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                        Text(s.volunteerNoCampaigns, fontSize = 13.sp, color = Color.Gray)
                    }
                }
                else -> {
                    // Sort: in-range first (by distance ascending), then out-of-range by distance
                    val sortedCampaigns = remember(campaigns, userLocation) {
                        campaigns.sortedWith(
                            compareBy<CampaignDto> { campaign ->
                                val dist = campaignDistance(userLocation, campaign) ?: Double.MAX_VALUE
                                !isInRange(dist, campaign.radius)
                            }.thenBy { campaign ->
                                campaignDistance(userLocation, campaign) ?: Double.MAX_VALUE
                            }
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        sortedCampaigns.forEach { campaign ->
                            val dist = campaignDistance(userLocation, campaign)
                            val inRange = dist != null && dist <= campaign.radius
                            val state = campaignStates[campaign.id] ?: CampaignUiState()
                            CampaignRow(
                                campaign = campaign,
                                state = state,
                                s = s,
                                distanceMeters = dist,
                                isInRange = inRange,
                                onClick = { selectedCampaignId = campaign.id },
                                onRegister = {
                                    scope.launch {
                                        campaignStates[campaign.id] = state.copy(busy = true, error = null)
                                        try {
                                            val result = registerCampaign(accessToken, campaign.id)
                                            val user = CampaignParticipantUser(
                                                id = currentUser?.id ?: "",
                                                fullName = currentUser?.fullName ?: "",
                                                email = currentUser?.email ?: "",
                                                phoneNumber = null,
                                            )
                                            campaignStates[campaign.id] = CampaignUiState(
                                                participant = result.toCampaignParticipant(user)
                                            )
                                        } catch (e: Throwable) {
                                            campaignStates[campaign.id] = state.copy(busy = false, error = e.message ?: "Error")
                                        }
                                    }
                                },
                                onCheckIn = {
                                    scope.launch {
                                        val participant = state.participant ?: return@launch
                                        campaignStates[campaign.id] = state.copy(busy = true, error = null)
                                        try {
                                            val loc = Geo.service.locationUpdates.firstOrNull()
                                            if (loc == null) {
                                                campaignStates[campaign.id] = state.copy(busy = false, error = s.waitingForGps)
                                                return@launch
                                            }
                                            val lat = loc.latitude
                                            val lng = loc.longitude
                                            val dist = distanceMeters(lat, lng, campaign.lat, campaign.lng)
                                            if (dist > campaign.radius) {
                                                campaignStates[campaign.id] = state.copy(busy = false, error = s.volunteerCheckInTooFar(dist.roundToInt()))
                                                return@launch
                                            }
                                            val result = checkInCampaign(accessToken, campaign.id, lat, lng)
                                            campaignStates[campaign.id] = CampaignUiState(
                                                participant = result.toCampaignParticipant(participant.user)
                                            )
                                        } catch (e: Throwable) {
                                            campaignStates[campaign.id] = state.copy(busy = false, error = e.message ?: "Error")
                                        }
                                    }
                                },
                                onCheckOut = {
                                    scope.launch {
                                        val participant = state.participant ?: return@launch
                                        campaignStates[campaign.id] = state.copy(busy = true, error = null)
                                        try {
                                            val loc = Geo.service.locationUpdates.firstOrNull()
                                            val lat = loc?.latitude ?: 0.0
                                            val lng = loc?.longitude ?: 0.0
                                            val result = checkOutCampaign(accessToken, campaign.id, lat, lng)
                                            campaignStates[campaign.id] = CampaignUiState(
                                                participant = result.toCampaignParticipant(participant.user)
                                            )
                                        } catch (e: Throwable) {
                                            campaignStates[campaign.id] = state.copy(busy = false, error = e.message ?: "Error")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        GarbageHeatmapCard()
    }
}

@Composable
private fun CampaignRow(
    campaign: CampaignDto,
    state: CampaignUiState,
    s: com.vodang.greenmind.i18n.AppStrings,
    distanceMeters: Double?,
    isInRange: Boolean,
    onClick: () -> Unit,
    onRegister: () -> Unit,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit,
) {
    val participant = state.participant
    val participantStatus = participant?.status

    // Derive isActive from campaign dates if status not present
    val isActive = campaign.status.equals("ACTIVE", ignoreCase = true) ||
            campaign.status.equals("IN_PROGRESS", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF9FBF9))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isActive) green50v else Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Text(if (isActive) s.campaignActive else s.campaignInactive, fontSize = 18.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(campaign.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                Spacer(Modifier.height(2.dp))
                Text(s.campaignDescription(campaign.description), fontSize = 11.sp, color = Color.Gray, maxLines = 2)
                Text(s.dateRange(formatDate(campaign.startDate), formatDate(campaign.endDate)), fontSize = 11.sp, color = Color.Gray)
                Text(s.locationRadius(campaign.lat, campaign.lng, campaign.radius), fontSize = 11.sp, color = Color.Gray)
                if (distanceMeters != null) {
                    Spacer(Modifier.height(4.dp))
                    val badgeColor = if (isInRange) green800v else Color(0xFFBDBDBD)
                    val badgeBg = if (isInRange) green50v else Color(0xFFF5F5F5)
                    val badgeText = if (distanceMeters >= 1000) {
                        "${(distanceMeters / 1000 * 10).toInt() / 10.0} km"
                    } else {
                        "${distanceMeters.toInt()} m"
                    }
                    val inRangeText = if (isInRange) " \u2022 In range" else ""
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(badgeText + inRangeText, fontSize = 11.sp, color = badgeColor, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Error row
        if (state.error != null) {
            Text(s.errorDisplay(state.error), fontSize = 11.sp, color = Color(0xFFB71C1C))
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.busy) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = green800v)
            } else {
                when (participantStatus) {
                    null -> {
                        // Not registered
                        ActionButton(s.volunteerJoinButton, green800v, onRegister)
                    }
                    "PENDING" -> {
                        // Registered, waiting for approval
                        StatusBadge(s.volunteerPendingApproval, orange600v, orange50v)
                    }
                    "REGISTERED", "APPROVED" -> {
                        // Approved, not checked in
                        StatusBadge(s.registeredStatus(s.volunteerRegistered), green600v, green50v)
                        Spacer(Modifier.width(8.dp))
                        ActionButton(s.volunteerCheckIn, teal600v, onCheckIn)
                    }
                    "CHECKED_IN" -> {
                        // Checked in, can check out
                        StatusBadge(s.checkedInStatus(s.volunteerCheckedIn), teal600v, teal50v)
                        Spacer(Modifier.width(8.dp))
                        ActionButton(s.volunteerCheckOut, orange600v, onCheckOut)
                    }
                    "CHECKED_OUT", "COMPLETED" -> {
                        StatusBadge(s.completedStatus(s.volunteerCheckedOut), blue600v, blue50v)
                    }
                    else -> {
                        StatusBadge(participantStatus, Color.Gray, Color(0xFFF5F5F5))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(label, fontSize = 12.sp, color = Color.White)
    }
}

@Composable
private fun StatusBadge(label: String, textColor: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 11.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}

/** Trims ISO timestamp to just the date portion for display */
private fun formatDate(iso: String): String =
    if (iso.length >= 10) iso.substring(0, 10) else iso
