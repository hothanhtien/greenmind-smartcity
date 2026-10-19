package com.vodang.greenmind.householdwaste

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
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
import com.vodang.greenmind.api.households.DetectTrashHistoryDto
import com.vodang.greenmind.api.households.getHistoryByHouseholdByType
import com.vodang.greenmind.householdwaste.components.DateFilter
import com.vodang.greenmind.householdwaste.components.WasteStatusCard
import com.vodang.greenmind.householdwaste.components.WasteStatusFilterSheet
import com.vodang.greenmind.scandetail.DisplayMode
import com.vodang.greenmind.scandetail.ScanDetailData
import com.vodang.greenmind.scandetail.toScanDetailData
import com.vodang.greenmind.scandetail.components.ScanDetailView
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.store.WasteSortStore
import com.vodang.greenmind.theme.Green50
import com.vodang.greenmind.theme.Green700
import com.vodang.greenmind.theme.Green800
import com.vodang.greenmind.theme.SurfaceGray
import com.vodang.greenmind.theme.SurfaceWhite
import com.vodang.greenmind.theme.TextPrimary
import com.vodang.greenmind.theme.TextSecondary
import com.vodang.greenmind.platform.BackHandler
import com.vodang.greenmind.wastesort.WasteSortStatus

@Composable
fun WasteStatusScreen(
    onBack: () -> Unit,
) {
    val s = com.vodang.greenmind.i18n.LocalAppStrings.current

    var allScans by remember { mutableStateOf<List<ScanDetailData>>(emptyList()) }
    var filteredScans by remember { mutableStateOf<List<ScanDetailData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var selectedStatus by remember { mutableStateOf<WasteSortStatus?>(null) }
    var selectedDateFilter by remember { mutableStateOf(DateFilter.ALL_TIME) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedScanGroup by remember { mutableStateOf<List<DetectTrashHistoryDto>?>(null) }
    val refreshTrigger by WasteSortStore.refreshTrigger.collectAsState()

    LaunchedEffect(Unit) {
        val token = SettingsStore.getAccessToken() ?: return@LaunchedEffect
        isLoading = true
        try {
            val history = getHistoryByHouseholdByType(token, "analyze_all").data
            allScans = history.map { it.toScanDetailData() }
            applyFilters(allScans, selectedStatus, selectedDateFilter) { filteredScans = it }
        } catch (e: Throwable) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            val token = SettingsStore.getAccessToken() ?: return@LaunchedEffect
            try {
                val history = getHistoryByHouseholdByType(token, "analyze_all").data
                allScans = history.map { it.toScanDetailData() }
                applyFilters(allScans, selectedStatus, selectedDateFilter) { filteredScans = it }
            } catch (e: Throwable) { }
        }
    }

    val groupedByStatus = remember(filteredScans) {
        filteredScans.sortedByDescending { it.createdAt }.groupBy { it.status }
    }

    var sortedExpanded by remember { mutableStateOf(true) }
    var broughtOutExpanded by remember { mutableStateOf(true) }
    var collectedExpanded by remember { mutableStateOf(true) }

    val sortedSection = groupedByStatus[WasteSortStatus.SORTED] ?: emptyList()
    val broughtOutSection = groupedByStatus[WasteSortStatus.BRINGOUTED] ?: emptyList()
    val collectedSection = groupedByStatus[WasteSortStatus.COLLECTED] ?: emptyList()

    Box(modifier = Modifier.fillMaxSize().background(SurfaceGray)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Custom Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite)
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.06f))
                                .clickable(onClick = onBack),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Text(
                            s.wasteStatus,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                        )
                    }
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            Icons.Filled.FilterList,
                            contentDescription = "Filter",
                            tint = Green800,
                        )
                    }
                }
            }

            // Content
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green700)
                }
            } else if (error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $error", color = Color.Red, fontSize = 14.sp)
                }
            } else if (filteredScans.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(s.noScans, fontSize = 15.sp, color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (selectedStatus == null || selectedStatus == WasteSortStatus.SORTED) {
                        if (sortedSection.isNotEmpty()) {
                            item {
                                StatusSectionHeader(
                                    title = "Sorted",
                                    count = sortedSection.size,
                                    bgColor = Color(0xFFE3F2FD),
                                    textColor = Color(0xFF1565C0),
                                    expanded = sortedExpanded,
                                    onToggle = { sortedExpanded = !sortedExpanded },
                                )
                            }
                            if (sortedExpanded) {
                                items(sortedSection, key = { it.id }) { scan ->
                                    WasteStatusCard(
                                        scan = scan,
                                        onClick = {
                                            val group = allScans.filter { it.imageUrl == scan.imageUrl }
                                            selectedScanGroup = group.map { dto ->
                                                DetectTrashHistoryDto(
                                                    id = dto.id,
                                                    imageUrl = dto.imageUrl,
                                                    status = when (dto.status) {
                                                        WasteSortStatus.SORTED -> "sorted"
                                                        WasteSortStatus.BRINGOUTED -> "brought_out"
                                                        WasteSortStatus.COLLECTED -> "picked_up"
                                                    },
                                                    createdAt = dto.createdAt,
                                                    totalObjects = dto.totalObjects,
                                                    items = dto.items?.map {
                                                        com.vodang.greenmind.api.households.DetectItemDto(
                                                            name = it.name,
                                                            quantity = it.quantity,
                                                            massKg = it.massKg,
                                                        )
                                                    },
                                                    totalMassKg = dto.totalMassKg,
                                                    detectedBy = null,
                                                )
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }

                    if (selectedStatus == null || selectedStatus == WasteSortStatus.BRINGOUTED) {
                        if (broughtOutSection.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                StatusSectionHeader(
                                    title = "Brought Out",
                                    count = broughtOutSection.size,
                                    bgColor = Color(0xFFFEF3C7),
                                    textColor = Color(0xFFB45309),
                                    expanded = broughtOutExpanded,
                                    onToggle = { broughtOutExpanded = !broughtOutExpanded },
                                )
                            }
                            if (broughtOutExpanded) {
                                items(broughtOutSection, key = { it.id }) { scan ->
                                    WasteStatusCard(
                                        scan = scan,
                                        onClick = {
                                            val group = allScans.filter { it.imageUrl == scan.imageUrl }
                                            selectedScanGroup = group.map { dto ->
                                                DetectTrashHistoryDto(
                                                    id = dto.id,
                                                    imageUrl = dto.imageUrl,
                                                    status = when (dto.status) {
                                                        WasteSortStatus.SORTED -> "sorted"
                                                        WasteSortStatus.BRINGOUTED -> "brought_out"
                                                        WasteSortStatus.COLLECTED -> "picked_up"
                                                    },
                                                    createdAt = dto.createdAt,
                                                    totalObjects = dto.totalObjects,
                                                    items = dto.items?.map {
                                                        com.vodang.greenmind.api.households.DetectItemDto(
                                                            name = it.name,
                                                            quantity = it.quantity,
                                                            massKg = it.massKg,
                                                        )
                                                    },
                                                    totalMassKg = dto.totalMassKg,
                                                    detectedBy = null,
                                                )
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }

                    if (selectedStatus == null || selectedStatus == WasteSortStatus.COLLECTED) {
                        if (collectedSection.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                StatusSectionHeader(
                                    title = "Collected",
                                    count = collectedSection.size,
                                    bgColor = Color(0xFFE8F5E9),
                                    textColor = Color(0xFF2E7D32),
                                    expanded = collectedExpanded,
                                    onToggle = { collectedExpanded = !collectedExpanded },
                                )
                            }
                            if (collectedExpanded) {
                                items(collectedSection, key = { it.id }) { scan ->
                                    WasteStatusCard(
                                        scan = scan,
                                        onClick = {
                                            val group = allScans.filter { it.imageUrl == scan.imageUrl }
                                            selectedScanGroup = group.map { dto ->
                                                DetectTrashHistoryDto(
                                                    id = dto.id,
                                                    imageUrl = dto.imageUrl,
                                                    status = when (dto.status) {
                                                        WasteSortStatus.SORTED -> "sorted"
                                                        WasteSortStatus.BRINGOUTED -> "brought_out"
                                                        WasteSortStatus.COLLECTED -> "picked_up"
                                                    },
                                                    createdAt = dto.createdAt,
                                                    totalObjects = dto.totalObjects,
                                                    items = dto.items?.map {
                                                        com.vodang.greenmind.api.households.DetectItemDto(
                                                            name = it.name,
                                                            quantity = it.quantity,
                                                            massKg = it.massKg,
                                                        )
                                                    },
                                                    totalMassKg = dto.totalMassKg,
                                                    detectedBy = null,
                                                )
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }

        // Filter Bottom Sheet
        if (showFilterSheet) {
            WasteStatusFilterSheet(
                selectedStatus = selectedStatus,
                selectedDateFilter = selectedDateFilter,
                onStatusSelected = { selectedStatus = it },
                onDateFilterSelected = { selectedDateFilter = it },
                onApply = {
                    applyFilters(allScans, selectedStatus, selectedDateFilter) { filteredScans = it }
                    showFilterSheet = false
                },
                onDismiss = { showFilterSheet = false },
            )
        }

        // Detail View
        selectedScanGroup?.let { group ->
            BackHandler(enabled = true) { selectedScanGroup = null }
            Box(modifier = Modifier.fillMaxSize()) {
                var scanData by remember(group) { mutableStateOf(group.toScanDetailData()) }
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
                    Text("✕", fontSize = 14.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun StatusSectionHeader(
    title: String,
    count: Int,
    bgColor: Color,
    textColor: Color,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = textColor.copy(alpha = 0.15f),
            ) {
                Text(
                    "  $count  ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
            }
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = textColor,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private fun applyFilters(
    scans: List<ScanDetailData>,
    status: WasteSortStatus?,
    dateFilter: DateFilter,
    onResult: (List<ScanDetailData>) -> Unit,
) {
    var result = scans

    status?.let { s ->
        result = result.filter { it.status == s }
    }

    val now = java.time.LocalDate.now()
    val cutoff = when (dateFilter) {
        DateFilter.TODAY -> now
        DateFilter.LAST_7_DAYS -> now.minusDays(7)
        DateFilter.LAST_30_DAYS -> now.minusDays(30)
        DateFilter.ALL_TIME -> null
    }
    if (cutoff != null) {
        result = result.filter { scan ->
            scan.createdAt?.take(10)?.let { dateStr ->
                try {
                    java.time.LocalDate.parse(dateStr) >= cutoff
                } catch (e: Throwable) {
                    true
                }
            } ?: true
        }
    }

    onResult(result.sortedByDescending { it.createdAt })
}