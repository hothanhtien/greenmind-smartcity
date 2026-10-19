package com.vodang.greenmind.home.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import com.vodang.greenmind.platform.BackHandler
import com.vodang.greenmind.store.SettingsStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.math.*

private fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val r = 6371000.0
    val toRad = kotlin.math.PI / 180.0
    val dLat = (lat2 - lat1) * toRad
    val dLng = (lng2 - lng1) * toRad
    val a = sin(dLat / 2).pow(2) + cos(lat1 * toRad) * cos(lat2 * toRad) * sin(dLng / 2).pow(2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}

private fun campaignDistance(userLocation: com.vodang.greenmind.location.Location?, campaign: CampaignDto): Double? =
    userLocation?.let { loc -> distanceMeters(loc.latitude, loc.longitude, campaign.lat, campaign.lng) }

/** Sorts campaigns by startDate descending (newest first) */
private fun sortByTime(campaigns: List<CampaignDto>): List<CampaignDto> =
    campaigns.sortedByDescending { it.startDate }

private data class CampaignsUiState(
    val participant: CampaignParticipant? = null,
    val busy: Boolean = false,
    val error: String? = null,
)

private val green800c = Color(0xFF2E7D32)
private val green600c = Color(0xFF388E3C)
private val green50c  = Color(0xFFE8F5E9)
private val blue600c  = Color(0xFF1976D2)
private val blue50c   = Color(0xFFE3F2FD)
private val teal600c  = Color(0xFF00897B)
private val teal50c   = Color(0xFFE0F2F1)
private val orange600c = Color(0xFFF57C00)
private val orange50c  = Color(0xFFFFF3E0)
private val gray200 = Color(0xFFEEEEEE)

private enum class CampaignsTab { ALL, MY }
private enum class MyCampaignsTab { ACTIVE, HISTORY }

@Composable
fun CampaignsList(
    onBack: () -> Unit,
    onSelectCampaign: (CampaignDto) -> Unit = {},
) {
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()

    var campaigns by remember { mutableStateOf<List<CampaignDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var userLocation by remember { mutableStateOf<com.vodang.greenmind.location.Location?>(null) }
    var selectedCampaignId by remember { mutableStateOf<String?>(null) }
    val campaignStates = remember { mutableStateMapOf<String, CampaignsUiState>() }
    val accessToken = remember { SettingsStore.getAccessToken() ?: "" }
    val currentUser = remember { SettingsStore.getUser() }

    var topTab by remember { mutableStateOf(CampaignsTab.ALL) }
    var myTab by remember { mutableStateOf(MyCampaignsTab.ACTIVE) }

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

    // Load campaigns
    LaunchedEffect(accessToken) {
        if (accessToken.isBlank()) { loading = false; return@LaunchedEffect }
        loading = true
        loadError = null
        try {
            userLocation = Geo.service.locationUpdates.firstOrNull()
            val fetchedCampaigns = getAllCampaigns(accessToken)
            val currentUserId = SettingsStore.getUser()?.id ?: ""
            fetchedCampaigns.forEach { campaign ->
                val myParticipation = campaign.participants.find { it.user.id == currentUserId }
                if (myParticipation != null && !campaignStates.containsKey(campaign.id)) {
                    campaignStates[campaign.id] = CampaignsUiState(participant = myParticipation)
                }
            }
            campaigns = fetchedCampaigns
        } catch (e: Throwable) {
            loadError = e.message ?: s.volunteerLoadError
        } finally {
            loading = false
        }
    }

    val currentUserId = SettingsStore.getUser()?.id ?: ""

    // Derived campaign lists
    val myCampaignIds = remember(campaignStates, currentUserId) {
        campaignStates.keys.toSet()
    }

    val myActiveCampaigns = remember(campaigns, campaignStates) {
        val myActive = campaigns.filter { c ->
            val p = campaignStates[c.id]?.participant
            p != null && (p.status == "PENDING" || p.status == "REGISTERED" || p.status == "APPROVED" || p.status == "CHECKED_IN")
        }
        sortByTime(myActive)
    }

    val myHistoryCampaigns = remember(campaigns, campaignStates) {
        val myDone = campaigns.filter { c ->
            val p = campaignStates[c.id]?.participant
            p != null && (p.status == "CHECKED_OUT" || p.status == "COMPLETED")
        }
        sortByTime(myDone)
    }

    val sortedAllCampaigns = remember(campaigns) {
        sortByTime(campaigns)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // ── Top tabs: All | My Campaigns ────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            CampaignsTab.values().forEach { tab ->
                val selected = topTab == tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { topTab = tab }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = when (tab) {
                            CampaignsTab.ALL -> s.campaignsAllTab
                            CampaignsTab.MY  -> s.campaignsMyTab
                        },
                        fontSize = 14.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) green800c else Color(0xFF9E9E9E),
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .fillMaxWidth()
                            .background(if (selected) green800c else Color.Transparent),
                    )
                }
            }
        }

        HorizontalDivider(color = gray200)

        // ── Sub-tabs for My Campaigns ──────────────────────────────────────────
        if (topTab == CampaignsTab.MY) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MyCampaignsTab.values().forEach { tab ->
                    val selected = myTab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) green800c else Color(0xFFEEEEEE))
                            .clickable { myTab = tab }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = when (tab) {
                                MyCampaignsTab.ACTIVE  -> s.campaignsActiveTab
                                MyCampaignsTab.HISTORY -> s.campaignsHistoryTab
                            },
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) Color.White else Color(0xFF757575),
                        )
                    }
                }
            }
            HorizontalDivider(color = gray200)
        }

        // ── Content ────────────────────────────────────────────────────────────
        when {
            loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = green800c)
                        Text(s.volunteerLoading, fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }
            loadError != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
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
                            Text(s.retry, fontSize = 12.sp, color = green800c)
                        }
                    }
                }
            }
            else -> {
                val displayedCampaigns = when (topTab) {
                    CampaignsTab.ALL -> sortedAllCampaigns
                    CampaignsTab.MY -> when (myTab) {
                        MyCampaignsTab.ACTIVE  -> myActiveCampaigns
                        MyCampaignsTab.HISTORY -> myHistoryCampaigns
                    }
                }

                if (displayedCampaigns.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = when (topTab) {
                                CampaignsTab.ALL -> s.volunteerNoCampaigns
                                CampaignsTab.MY -> when (myTab) {
                                    MyCampaignsTab.ACTIVE  -> s.volunteerNoCampaigns
                                    MyCampaignsTab.HISTORY -> s.volunteerNoCampaigns
                                }
                            },
                            fontSize = 13.sp,
                            color = Color.Gray,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (topTab == CampaignsTab.MY) {
                            // Section label
                            item {
                                Text(
                                    text = when (myTab) {
                                        MyCampaignsTab.ACTIVE  -> s.campaignsActiveTab
                                        MyCampaignsTab.HISTORY -> s.campaignsHistoryTab
                                    } + " (${displayedCampaigns.size})",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575),
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                        items(displayedCampaigns, key = { it.id }) { campaign ->
                            val dist = campaignDistance(userLocation, campaign)
                            val inRange = dist != null && dist <= campaign.radius
                            val state = campaignStates[campaign.id] ?: CampaignsUiState()
                            CampaignsCampaignRow(
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
                                            campaignStates[campaign.id] = CampaignsUiState(
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
                                            campaignStates[campaign.id] = CampaignsUiState(
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
                                            campaignStates[campaign.id] = CampaignsUiState(
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
    }
}

@Composable
private fun CampaignsCampaignRow(
    campaign: CampaignDto,
    state: CampaignsUiState,
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
    val isCompleted = campaign.status.equals("COMPLETED", ignoreCase = true)

    val isActive = campaign.status.equals("ACTIVE", ignoreCase = true) ||
            campaign.status.equals("IN_PROGRESS", ignoreCase = true) ||
            campaign.status.equals("ONGOING", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
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
                    .background(if (isActive) green50c else Color(0xFFF5F5F5)),
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
                if (distanceMeters != null) {
                    Spacer(Modifier.height(4.dp))
                    val badgeColor = if (isInRange) green800c else Color(0xFFBDBDBD)
                    val badgeBg = if (isInRange) green50c else Color(0xFFF5F5F5)
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

        if (state.error != null) {
            Text(s.errorDisplay(state.error), fontSize = 11.sp, color = Color(0xFFB71C1C))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.busy) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = green800c)
            } else {
                when (participantStatus) {
                    null -> if (isCompleted) {
                        CampaignsStatusBadge(s.campaignInactive, Color.Gray, Color(0xFFF5F5F5))
                    } else {
                        CampaignsActionButton(s.volunteerJoinButton, green800c, onRegister)
                    }
                    "PENDING" -> {
                        CampaignsStatusBadge(s.volunteerPendingApproval, orange600c, orange50c)
                    }
                    "REGISTERED", "APPROVED" -> {
                        CampaignsStatusBadge(s.registeredStatus(s.volunteerRegistered), green600c, green50c)
                        Spacer(Modifier.width(8.dp))
                        CampaignsActionButton(s.volunteerCheckIn, teal600c, onCheckIn)
                    }
                    "CHECKED_IN" -> {
                        CampaignsStatusBadge(s.checkedInStatus(s.volunteerCheckedIn), teal600c, teal50c)
                        Spacer(Modifier.width(8.dp))
                        CampaignsActionButton(s.volunteerCheckOut, orange600c, onCheckOut)
                    }
                    "CHECKED_OUT", "COMPLETED" -> {
                        CampaignsStatusBadge(s.completedStatus(s.volunteerCheckedOut), blue600c, blue50c)
                    }
                    else -> {
                        CampaignsStatusBadge(participantStatus, Color.Gray, Color(0xFFF5F5F5))
                    }
                }
            }
        }
    }
}

@Composable
private fun CampaignsActionButton(label: String, color: Color, onClick: () -> Unit) {
    androidx.compose.material3.Button(
        onClick = onClick,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(label, fontSize = 12.sp, color = Color.White)
    }
}

@Composable
private fun CampaignsStatusBadge(label: String, textColor: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 11.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}

private fun formatDate(iso: String): String =
    if (iso.length >= 10) iso.substring(0, 10) else iso
