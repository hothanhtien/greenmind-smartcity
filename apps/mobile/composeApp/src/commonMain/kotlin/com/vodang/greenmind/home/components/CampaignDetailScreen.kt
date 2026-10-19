package com.vodang.greenmind.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import com.vodang.greenmind.api.campaign.CampaignDto
import com.vodang.greenmind.api.campaign.CampaignParticipant
import com.vodang.greenmind.api.campaign.CampaignParticipantUser
import com.vodang.greenmind.api.campaign.CampaignReport
import com.vodang.greenmind.api.campaign.toCampaignParticipant
import com.vodang.greenmind.api.participantcampaign.MyCampaignDto
import com.vodang.greenmind.api.participantcampaign.ParticipantCampaignDto
import com.vodang.greenmind.api.participantcampaign.getMyParticipations
import com.vodang.greenmind.api.participantcampaign.checkInCampaign
import com.vodang.greenmind.api.participantcampaign.checkOutCampaign
import com.vodang.greenmind.api.participantcampaign.registerCampaign
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.location.Geo
import com.vodang.greenmind.platform.BackHandler
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.wastereport.NetworkImage
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vodang.greenmind.home.components.RouteMap
import com.vodang.greenmind.home.components.RouteMapPoint
import com.vodang.greenmind.location.Location
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.roundToInt

/** Haversine distance in meters between two lat/lng points. */
private fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val r = 6371000.0
    val toRad = PI / 180.0
    val dLat = (lat2 - lat1) * toRad
    val dLng = (lng2 - lng1) * toRad
    val a = sin(dLat / 2).pow(2) + cos(lat1 * toRad) * cos(lat2 * toRad) * sin(dLng / 2).pow(2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}

private val green800v = Color(0xFF2E7D32)
private val green600v = Color(0xFF388E3C)
private val green50v  = Color(0xFFE8F5E9)
private val teal600v  = Color(0xFF00897B)
private val teal50v   = Color(0xFFE0F2F1)
private val orange600v = Color(0xFFF57C00)
private val orange50v  = Color(0xFFFFF3E0)
private val blue600v  = Color(0xFF1976D2)
private val blue50v   = Color(0xFFE3F2FD)

@Composable
fun CampaignDetailScreen(
    campaign: CampaignDto,
    accessToken: String,
    onBack: () -> Unit,
    onRegistered: (CampaignParticipant) -> Unit = {},
    onOpenChat: (String) -> Unit = {},
) {
    BackHandler(onBack = onBack)
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val access = remember { accessToken.ifBlank { SettingsStore.getAccessToken() ?: "" } }
    val currentUser = remember { SettingsStore.getUser() }

    var participant by remember { mutableStateOf<CampaignParticipant?>(null) }
    var participations by remember { mutableStateOf<List<MyCampaignDto>>(emptyList()) }

    LaunchedEffect(campaign) {
        participations = try {
            getMyParticipations(access)
        } catch (_: Throwable) {
            emptyList()
        }
        val currentUserId = currentUser?.id ?: ""
        participant = participations.find { it.id == campaign.id }?.let { p ->
            CampaignParticipant(
                id = "",
                status = p.participantStatus,
                checkInTime = p.checkInTime,
                checkOutTime = p.checkOutTime,
                user = CampaignParticipantUser(
                    id = currentUserId,
                    fullName = currentUser?.fullName ?: "",
                    email = currentUser?.email ?: "",
                    phoneNumber = null,
                )
            )
        }
    }

    var isLoading by remember { mutableStateOf(false) }
    var isBusy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val status = participant?.status
    val isActive = campaign.status.equals("ACTIVE", ignoreCase = true) ||
            campaign.status.equals("IN_PROGRESS", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        // Custom Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(green50v)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = green800v,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(4.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = campaign.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212121),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isActive) green600v else Color(0xFFBDBDBD)),
                        )
                        Text(
                            text = if (isActive) s.campaignActive else s.campaignInactive,
                            fontSize = 12.sp,
                            color = if (isActive) green600v else Color(0xFF9E9E9E),
                        )
                    }
                }

                IconButton(onClick = { onOpenChat(campaign.id) }) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Open Chat",
                        tint = green800v,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFFEEEEEE))

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Date range
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBF9)),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s.campaignTime, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF424242))
                    Text(s.dateRange(formatDate(campaign.startDate), formatDate(campaign.endDate)),
                        fontSize = 14.sp, color = Color(0xFF616161))
                }
            }

            // Description
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBF9)),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s.campaignDescriptionLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF424242))
                    Text(campaign.description, fontSize = 14.sp, color = Color(0xFF616161), lineHeight = 20.sp)
                }
            }

            // Location info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBF9)),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s.campaignLocation, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF424242))
                    Text(s.locationRadius(campaign.lat, campaign.lng, campaign.radius), fontSize = 13.sp, color = Color.Gray)
                }
            }

            var showRouteFullScreen by remember { mutableStateOf(false) }
            var userLocation by remember { mutableStateOf<Location?>(null) }
            
            LaunchedEffect(Unit) {
                Geo.service.locationUpdates.collect { userLocation = it }
            }

            // Map
            CampaignMap(
                campaignLat = campaign.lat,
                campaignLng = campaign.lng,
                radius = campaign.radius,
                height = 240.dp,
                modifier = Modifier.fillMaxWidth(),
                onRouteClick = { showRouteFullScreen = true }
            )

            if (showRouteFullScreen) {
                Dialog(
                    onDismissRequest = { showRouteFullScreen = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
                        val uLoc = userLocation
                        if (uLoc == null) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = green800v)
                            }
                        } else {
                            RouteMap(
                                points = listOf(
                                    RouteMapPoint(uLoc.latitude, uLoc.longitude),
                                    RouteMapPoint(campaign.lat, campaign.lng)
                                ),
                                modifier = Modifier.fillMaxSize(),
                                height = 0.dp,
                            )
                        }
                        
                        IconButton(
                            onClick = { showRouteFullScreen = false },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .statusBarsPadding()
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.8f))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = Color.Black)
                        }
                    }
                }
            }

            // Error
            if (error != null) {
                Text(error!!, fontSize = 12.sp, color = Color(0xFFB71C1C))
            }

            // Action button
            if (isBusy || isLoading) {
                Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = green800v, modifier = Modifier.size(24.dp))
                }
            } else {
                when (status) {
                    null -> ActionButton(s.volunteerJoinButton, green800v, Modifier.fillMaxWidth()) {
                        scope.launch {
                            isBusy = true; error = null
                            try {
                                val result = registerCampaign(access, campaign.id)
                                val converted = result.toCampaignParticipant(
                                    CampaignParticipantUser(
                                        id = currentUser?.id ?: "",
                                        fullName = currentUser?.fullName ?: "",
                                        email = currentUser?.email ?: "",
                                        phoneNumber = null,
                                    )
                                )
                                participant = converted
                                onRegistered(converted)
                            } catch (e: Throwable) {
                                error = e.message
                            }
                            isBusy = false
                        }
                    }
                    "PENDING" -> {
                        StatusBadge(s.volunteerPendingApproval, orange600v, orange50v)
                    }
                    "REGISTERED", "APPROVED" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusBadge(s.registeredStatus(s.volunteerRegistered), green600v, green50v)
                            ActionButton(s.volunteerCheckIn, teal600v, Modifier.weight(1f)) {
                                scope.launch {
                                    val p = participant ?: return@launch
                                    isBusy = true; error = null
                                    try {
                                        val loc = Geo.service.locationUpdates.firstOrNull()
                                        if (loc == null) {
                                            error = s.waitingForGps
                                            isBusy = false
                                            return@launch
                                        }
                                        val lat = loc.latitude
                                        val lng = loc.longitude
                                        val dist = distanceMeters(lat, lng, campaign.lat, campaign.lng)
                                        if (dist > campaign.radius) {
                                            error = s.volunteerCheckInTooFar(dist.roundToInt())
                                            isBusy = false
                                            return@launch
                                        }
                                        val result = checkInCampaign(access, campaign.id, lat, lng)
                                        val converted = result.toCampaignParticipant(p.user)
                                        participant = converted
                                        onRegistered(converted)
                                    } catch (e: Throwable) {
                                        error = e.message
                                    }
                                    isBusy = false
                                }
                            }
                        }
                    }
                    "CHECKED_IN" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusBadge(s.checkedInStatus(s.volunteerCheckedIn), teal600v, teal50v)
                            ActionButton(s.volunteerCheckOut, orange600v, Modifier.weight(1f)) {
                                scope.launch {
                                    val p = participant ?: return@launch
                                    isBusy = true; error = null
                                    try {
                                        val loc = Geo.service.locationUpdates.firstOrNull()
                                        val lat = loc?.latitude ?: 0.0
                                        val lng = loc?.longitude ?: 0.0
                                        val result = checkOutCampaign(access, campaign.id, lat, lng)
                                        val converted = result.toCampaignParticipant(p.user)
                                        participant = converted
                                        onRegistered(converted)
                                    } catch (e: Throwable) {
                                        error = e.message
                                    }
                                    isBusy = false
                                }
                            }
                        }
                    }
                    "CHECKED_OUT", "COMPLETED" -> {
                        StatusBadge(s.completedStatus(s.volunteerCheckedOut), blue600v, blue50v)
                    }
                    else -> {
                        StatusBadge(status, Color.Gray, Color(0xFFF5F5F5))
                    }
                }
            }

            // Reports / Evidence images
            if (campaign.reports.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBF9)),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            "${s.myScanReports} (${campaign.reports.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF424242),
                        )
                        campaign.reports.forEach { report ->
                            ReportEvidenceItem(report)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportEvidenceItem(report: CampaignReport) {
    val s = LocalAppStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Report header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(report.code, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF424242))
            val statusColor = when (report.status.uppercase()) {
                "RESOLVED" -> green800v
                "PENDING" -> orange600v
                else -> Color.Gray
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(statusColor.copy(alpha = 0.1f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(report.status, fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Medium)
            }
        }

        if (report.description.isNotBlank()) {
            Text(report.description, fontSize = 11.sp, color = Color.Gray, maxLines = 2)
        }

        // Report image
        NetworkImage(
            url = report.imageUrl,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )

        // Evidence image (collection proof)
        if (!report.imageEvidenceUrl.isNullOrBlank()) {
            Text(s.collectionEvidence, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF616161))
            NetworkImage(
                url = report.imageEvidenceUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
        }

        HorizontalDivider(color = Color(0xFFEEEEEE))
    }
}

@Composable
private fun ActionButton(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = color, disabledContainerColor = color.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(10.dp),
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

@Composable
private fun StatusBadge(label: String, textColor: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(label, fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}

private fun formatDate(iso: String): String =
    if (iso.length >= 10) iso.substring(0, 10) else iso
