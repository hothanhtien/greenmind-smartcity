package com.vodang.greenmind.householdwaste

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.households.DetectTrashHistoryDto
import com.vodang.greenmind.api.households.GreenScoreEntryDto
import com.vodang.greenmind.api.households.getHistoryByHouseholdByType
import com.vodang.greenmind.api.households.getGreenScoreByHousehold
import com.vodang.greenmind.api.wastereport.WasteReportDto
import com.vodang.greenmind.api.wastereport.getMyWasteReports
import com.vodang.greenmind.householdwaste.components.*
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.scandetail.DisplayMode
import com.vodang.greenmind.scandetail.toScanDetailData
import com.vodang.greenmind.scandetail.components.ScanDetailView
import com.vodang.greenmind.store.HouseholdStore
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.store.WasteSortStore
import com.vodang.greenmind.wastereport.WasteReportCard
import com.vodang.greenmind.wastereport.WasteReportDetailSheet
import com.vodang.greenmind.platform.BackHandler
import com.vodang.greenmind.theme.Green50
import com.vodang.greenmind.theme.Green500
import com.vodang.greenmind.theme.Green600
import com.vodang.greenmind.theme.Green700
import com.vodang.greenmind.theme.SurfaceGray
import com.vodang.greenmind.theme.TextPrimary
import com.vodang.greenmind.theme.TextSecondary
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private val Red600     = Color(0xFFDC2626)
private val Red50      = Color(0xFFFEE2E2)
private val Blue600    = Color(0xFF2563EB)
private val Blue50     = Color(0xFFDBEAFE)
private val Orange50   = Color(0xFFFFF3E0)
private val Orange700  = Color(0xFFF57C00)
private val Purple50   = Color(0xFFF3E5F5)
private val Purple700  = Color(0xFF7B1FA2)

@Composable
fun HouseholdWasteScreen(
    onBack: () -> Unit = {},
    onNavigateToWasteImpact: () -> Unit = {},
    onNavigateToWasteStat: () -> Unit = {},
    scrollState: ScrollState? = null,
    onSettingsClick: () -> Unit = {}
) {
    val s = LocalAppStrings.current
    val household by HouseholdStore.household.collectAsState()
    val userHistory by WasteSortStore.userHistory.collectAsState()
    val hasFetched by HouseholdStore.hasFetched.collectAsState()
    val isFetching by HouseholdStore.isFetching.collectAsState()

    var householdHistory   by remember { mutableStateOf<List<DetectTrashHistoryDto>>(emptyList()) }
    var greenScoreEntries by remember { mutableStateOf<List<GreenScoreEntryDto>>(emptyList()) }
    var wasteReports      by remember { mutableStateOf<List<WasteReportDto>>(emptyList()) }
    var selectedReport    by remember { mutableStateOf<WasteReportDto?>(null) }
    var selectedScanGroup by remember { mutableStateOf<List<DetectTrashHistoryDto>?>(null) }
    var selectedScore     by remember { mutableStateOf<GreenScoreEntryDto?>(null) }
    var isLoadingHouseholdScans by remember { mutableStateOf(false) }
    var isLoadingUserScans      by remember { mutableStateOf(false) }
    var isLoadingScore          by remember { mutableStateOf(false) }
    var isLoadingReports        by remember { mutableStateOf(false) }
    var viewAllMode       by remember { mutableStateOf(ViewAllMode.NONE) }
    val refreshTrigger    by com.vodang.greenmind.store.WasteSortStore.refreshTrigger.collectAsState()

    LaunchedEffect(Unit) { HouseholdStore.fetchHousehold() }

    LaunchedEffect(household, refreshTrigger) {
        val h = household ?: return@LaunchedEffect
        val token = SettingsStore.getAccessToken() ?: return@LaunchedEffect
        launch {
            isLoadingHouseholdScans = true
            try {
                val allHouseholdScans = mutableListOf<DetectTrashHistoryDto>()
                try { allHouseholdScans.addAll(getHistoryByHouseholdByType(token, "analyze_all").data) } catch (e: Throwable) { }
                try { allHouseholdScans.addAll(getHistoryByHouseholdByType(token, "total_mass").data) } catch (e: Throwable) { }
                householdHistory = allHouseholdScans
            } finally { isLoadingHouseholdScans = false }
        }

        launch {
            isLoadingUserScans = true
            try { WasteSortStore.fetchUserScans(token) } catch (e: Throwable) { }
            finally { isLoadingUserScans = false }
        }

        launch {
            isLoadingScore = true
            try {
                val scoreResp = getGreenScoreByHousehold(token, h.id)
                greenScoreEntries = scoreResp.data.greenScores
            } catch (e: Throwable) { }
            finally { isLoadingScore = false }
        }

        launch {
            isLoadingReports = true
            try {
                val reportsResp = getMyWasteReports(token)
                wasteReports = reportsResp.data
            } catch (e: Throwable) { }
            finally { isLoadingReports = false }
        }
    }

    if (!hasFetched || isFetching) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Green700)
        }
        return
    }

    if (household == null) {
        CreateHouseholdScreen(onBack = onBack, onSuccess = { HouseholdStore.fetchHousehold() })
        return
    }

    val h = household!!
    val currentScore = greenScoreEntries.lastOrNull()?.finalScore ?: h.scoreGreen

    if (viewAllMode != ViewAllMode.NONE) {
        BackHandler { viewAllMode = ViewAllMode.NONE }
        val householdGroupsList = remember(householdHistory) { householdHistory.groupBy { it.imageUrl }.values.toList() }
        val userGroupsList = remember(userHistory) { userHistory.groupBy { it.imageUrl }.values.toList() }
        ViewAllRecordsScreen(
            mode = viewAllMode,
            householdGroups = householdGroupsList,
            userGroups = userGroupsList,
            wasteReports = wasteReports,
            greenScores = greenScoreEntries,
            onBack = { viewAllMode = ViewAllMode.NONE },
            onReportClick = { selectedReport = it },
            onScanGroupClick = { selectedScanGroup = it },
            onScoreClick = { selectedScore = it }
        )
        return
    }

    val localScrollState = scrollState ?: rememberScrollState()
    val householdGroups = householdHistory.groupBy { it.imageUrl }
    val totalHouseholdScans = householdGroups.size
    val userGroups = userHistory.groupBy { it.imageUrl }
    val totalUserScans = userGroups.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceGray)
            .verticalScroll(localScrollState)
    ) {
        Spacer(Modifier.height(12.dp))

        HeroCard(
            address = h.address ?: "",
            score = currentScore,
            memberCount = h.members?.size ?: 0,
            onSettings = onSettingsClick,
            onBack = onBack
        )

        Spacer(Modifier.height(16.dp))

        StatsGrid(
            greenScore = currentScore,
            memberCount = h.members?.size ?: 0,
            scanCount = totalHouseholdScans,
            reportCount = wasteReports.size
        )

        Spacer(Modifier.height(16.dp))

        Text(s.viewWasteImpactAnalysis, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(10.dp))
        ActionCards(
            onNavigateToWasteImpact = onNavigateToWasteImpact,
            onNavigateToWasteStat = onNavigateToWasteStat
        )

        Spacer(Modifier.height(20.dp))

        // ── Green Score ────────────────────────────────────────────────────
        SectionHeader(title = s.greenScoreSection, count = greenScoreEntries.size)
        Spacer(Modifier.height(8.dp))
        if (isLoadingScore) {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Green600) }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                if (greenScoreEntries.isEmpty()) {
                    EmptyStateMessage(icon = Icons.Outlined.Eco, message = s.noScoreHistory)
                } else {
                    GreenScoreCard(entry = greenScoreEntries.last(), onClick = { viewAllMode = ViewAllMode.GREEN_SCORES })
                }
                if (greenScoreEntries.size > 1) {
                    ViewAllButton(text = s.viewAllHistory, onClick = { viewAllMode = ViewAllMode.GREEN_SCORES })
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Household Scans ────────────────────────────────────────────────
        SectionHeader(title = s.householdScansSection, count = totalHouseholdScans)
        Spacer(Modifier.height(8.dp))
        if (isLoadingHouseholdScans) {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Green600) }
        } else {
            if (totalHouseholdScans == 0) {
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    EmptyStateMessage(icon = Icons.Outlined.CameraAlt, message = s.noHouseholdScans)
                }
            } else {
                householdGroups.values.take(2).forEach { records ->
                    GroupedDetectScanCard(records = records, onClick = { selectedScanGroup = records })
                    Spacer(Modifier.height(8.dp))
                }
                if (totalHouseholdScans > 2) {
                    ViewAllButton(text = s.viewAllItems, onClick = { viewAllMode = ViewAllMode.HOUSEHOLD_SCANS })
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── My Scan Reports ────────────────────────────────────────────────
        SectionHeader(title = s.myScanReports, count = totalUserScans)
        Spacer(Modifier.height(8.dp))
        if (isLoadingUserScans) {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Green600) }
        } else {
            if (totalUserScans == 0) {
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    EmptyStateMessage(icon = Icons.Outlined.Person, message = s.myScanReports)
                }
            } else {
                userGroups.values.take(2).forEach { records ->
                    GroupedDetectScanCard(records = records, onClick = { selectedScanGroup = records })
                    Spacer(Modifier.height(8.dp))
                }
                if (totalUserScans > 2) {
                    ViewAllButton(text = s.viewAllItems, onClick = { viewAllMode = ViewAllMode.USER_SCANS })
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Waste Reports ──────────────────────────────────────────────────
        SectionHeader(title = s.myWasteReports, count = wasteReports.size)
        Spacer(Modifier.height(8.dp))
        if (isLoadingReports) {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Green600) }
        } else {
            if (wasteReports.isEmpty()) {
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    EmptyStateMessage(icon = Icons.Outlined.Warning, message = s.noWasteReports)
                }
            } else {
                wasteReports.take(2).forEach { report ->
                    WasteReportCard(report = report, onClick = { selectedReport = report })
                    Spacer(Modifier.height(8.dp))
                }
                if (wasteReports.size > 2) {
                    ViewAllButton(text = s.viewAllItems, onClick = { viewAllMode = ViewAllMode.WASTE_REPORTS })
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    selectedReport?.let { report ->
        WasteReportDetailSheet(report = report, onDismiss = { selectedReport = null })
    }
    selectedScanGroup?.let { group ->
        BackHandler(enabled = true) { selectedScanGroup = null }
        var scanData by remember(group) { mutableStateOf(group.toScanDetailData()) }
        Box(Modifier.fillMaxSize()) {
            ScanDetailView(
                data = scanData,
                onBack = { selectedScanGroup = null },
                onStatusChange = { newStatus ->
                    scanData = scanData.copy(status = newStatus)
                    WasteSortStore.triggerRefresh()
                },
                displayMode = DisplayMode.FULL_SCREEN,
            )
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { selectedScanGroup = null }
                    .align(Alignment.TopStart),
                contentAlignment = Alignment.Center,
            ) {
                Text(s.backArrow, fontSize = 18.sp, color = Color.White)
            }
        }
    }
    selectedScore?.let { score ->
        GreenScoreDetailSheet(entry = score, onDismiss = { selectedScore = null })
    }
}

// ── Hero Card ─────────────────────────────────────────────────────────────────

@Composable
private fun HeroCard(
    address: String,
    score: Int,
    memberCount: Int,
    onSettings: () -> Unit,
    onBack: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { (score / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxSize(),
                    color = Green600,
                    trackColor = Green50,
                    strokeWidth = 6.dp,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$score", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Green700)
                    Text("pts", fontSize = 9.sp, color = Green500)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(address, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Group, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Text("$memberCount members", fontSize = 12.sp, color = TextSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Eco, contentDescription = null, tint = Green600, modifier = Modifier.size(14.dp))
                    Text("Green Score", fontSize = 12.sp, color = TextSecondary)
                }
            }
            IconButton(onClick = onSettings, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Settings, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(22.dp))
            }
        }
    }
}

// ── Stats Grid ────────────────────────────────────────────────────────────────

@Composable
private fun StatsGrid(
    greenScore: Int,
    memberCount: Int,
    scanCount: Int,
    reportCount: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(icon = Icons.Filled.Eco, label = "Green Score", value = "$greenScore", color = Green700, bg = Green50, modifier = Modifier.weight(1f))
        StatCard(icon = Icons.Filled.Group, label = "Members", value = "$memberCount", color = Blue600, bg = Blue50, modifier = Modifier.weight(1f))
    }
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(icon = Icons.Filled.CameraAlt, label = "Scans", value = "$scanCount", color = Purple700, bg = Purple50, modifier = Modifier.weight(1f))
        StatCard(icon = Icons.Filled.Warning, label = "Reports", value = "$reportCount", color = Orange700, bg = Orange50, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    bg: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(label, fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}

// ── Action Cards ──────────────────────────────────────────────────────────────

@Composable
private fun ActionCards(
    onNavigateToWasteImpact: () -> Unit,
    onNavigateToWasteStat: () -> Unit
) {
    val s = LocalAppStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f).clickable(onClick = onNavigateToWasteImpact),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Green50),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Insights, contentDescription = null, tint = Green700, modifier = Modifier.size(28.dp))
                Text(s.viewWasteImpactAnalysis, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Green700, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(s.environmentalImpactShort, fontSize = 11.sp, color = Green700.copy(alpha = 0.7f))
            }
        }
        Card(
            modifier = Modifier.weight(1f).clickable(onClick = onNavigateToWasteStat),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Blue50),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Analytics, contentDescription = null, tint = Blue600, modifier = Modifier.size(28.dp))
                Text(s.wasteStatTitle, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Blue600, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(s.wasteStatShort, fontSize = 11.sp, color = Blue600.copy(alpha = 0.7f))
            }
        }
    }
}

// ── Section Header ────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.width(4.dp))
        if (count > 0) {
            Surface(shape = RoundedCornerShape(20.dp), color = Green50) {
                Text("  $count  ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Green700, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

// ── Reusable Sub-Composables ──────────────────────────────────────────────────

@Composable
private fun GreenScoreCard(entry: GreenScoreEntryDto, onClick: () -> Unit) {
    val score = entry.finalScore
    val scoreIcon = when {
        score >= 70 -> Icons.Filled.EmojiEvents
        score >= 40 -> Icons.Filled.Eco
        else -> Icons.Filled.Warning
    }
    val iconTint = when {
        score >= 70 -> Color(0xFFFFD700)
        score >= 40 -> Green700
        else -> Red600
    }
    val levelText = when {
        score >= 70 -> "Xuất sắc"
        score >= 40 -> "Khá tốt"
        else -> "Cần cải thiện"
    }

    Box(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { (score / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxSize(),
                    color = Green600,
                    trackColor = Green50,
                    strokeWidth = 5.dp,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    "$score",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green700
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(scoreIcon, contentDescription = null, modifier = Modifier.size(20.dp), tint = iconTint)
                    Text(levelText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Text("$score pts — Green Score", fontSize = 12.sp, color = TextSecondary)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun EmptyStateMessage(icon: ImageVector, message: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF5F5F5)).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = TextSecondary.copy(alpha = 0.6f))
        Text(message, fontSize = 14.sp, color = TextSecondary.copy(alpha = 0.7f))
    }
}

@Composable
private fun ViewAllButton(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(text, color = Green700, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
}

@Composable
internal fun RemainingCard(remaining: Int, label: String, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF5F5F5)).clickable(onClick = onClick).padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("+ $remaining more $label${if (remaining != 1) "s" else ""}  ·  View all", fontSize = 12.sp, color = TextSecondary.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
    }
}
