package com.vodang.greenmind.wastesort

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import com.vodang.greenmind.api.households.DetectTrashHistoryDto
import com.vodang.greenmind.api.households.GreenScoreEntryDto
import com.vodang.greenmind.api.wastedetect.WasteDetectResponse
import com.vodang.greenmind.platform.BackHandler
import com.vodang.greenmind.scandetail.DisplayMode
import com.vodang.greenmind.scandetail.ScanDetailData
import com.vodang.greenmind.scandetail.toScanDetailData
import com.vodang.greenmind.scandetail.components.ScanDetailView
import com.vodang.greenmind.store.HouseholdStore
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.store.WasteSortStore
import com.vodang.greenmind.wastesort.components.WasteSortList
import com.vodang.greenmind.i18n.AppStrings

// ── Palette ───────────────────────────────────────────────────────────────────

val green800 = Color(0xFF2E7D32)
val green600 = Color(0xFF388E3C)
val green50  = Color(0xFFE8F5E9)

// ── Data model ────────────────────────────────────────────────────────────────

/** Lifecycle stages for a scanned entry. */
enum class WasteSortStatus { SORTED, BRINGOUTED, COLLECTED }

val WasteSortStatus.label: String
    get() = when (this) {
        WasteSortStatus.SORTED     -> "Sorted"
        WasteSortStatus.BRINGOUTED -> "Brought Out"
        WasteSortStatus.COLLECTED  -> "Collected"
    }

fun WasteSortStatus.i18nLabel(s: AppStrings) = when (this) {
    WasteSortStatus.SORTED     -> s.wasteSorted
    WasteSortStatus.BRINGOUTED -> s.wasteBroughtOut
    WasteSortStatus.COLLECTED  -> s.wasteCollected
}

data class WasteSortEntry(
    val id: String,
    /** Backend DB record ID — used for API calls like bring-out. Null for legacy/offline entries. */
    val backendId: String? = null,
    val imageUrl: String,
    val totalObjects: Int,
    val grouped: Map<String, List<String>>,
    val createdAt: String,   // display string e.g. "Apr 2, 2026"
    val scannedBy: String,
    val status: WasteSortStatus = WasteSortStatus.SORTED,
    val greenScoreResult: GreenScoreEntryDto? = null,
    /** Legacy field populated only from `/detect-trash` history records; null for `/analyze-image` scans. */
    val pollutantResult: WasteDetectResponse? = null,
    /** Legacy field populated only from `/detect-trash` history records; null for `/analyze-image` scans. */
    val totalMassKg: Double? = null,
    val detectType: String? = null,
)

// ── Category helpers ──────────────────────────────────────────────────────────

fun categoryColor(cat: String) = when (cat.lowercase()) {
    "recyclable" -> Color(0xFF1565C0)
    "residual"   -> Color(0xFF6D4C41)
    "organic"    -> Color(0xFF2E7D32)
    "hazardous"  -> Color(0xFFD32F2F)
    else         -> Color(0xFF616161)
}

fun categoryBg(cat: String) = when (cat.lowercase()) {
    "recyclable" -> Color(0xFFE3F2FD)
    "residual"   -> Color(0xFFEFEBE9)
    "organic"    -> Color(0xFFE8F5E9)
    "hazardous"  -> Color(0xFFFFEBEE)
    else         -> Color(0xFFF5F5F5)
}

fun categoryEmoji(cat: String): ImageVector = when (cat.lowercase()) {
    "recyclable" -> Icons.Filled.Cached
    "residual"   -> Icons.Filled.Delete
    "organic"    -> Icons.Filled.Eco
    "hazardous"  -> Icons.Filled.Warning
    else         -> Icons.Filled.Inventory2
}

fun categoryLabel(cat: String) = cat.replaceFirstChar { it.uppercase() }

// ── Screen root ───────────────────────────────────────────────────────────────

@Composable
fun WasteSortScreen(onScanClick: () -> Unit = {}) {
    val apiHistory by WasteSortStore.userHistory.collectAsState()
    val entries by WasteSortStore.entries.collectAsState()
    var selectedApiGroup by remember { mutableStateOf<List<DetectTrashHistoryDto>?>(null) }
    var showScan        by remember { mutableStateOf(false) }
    var useGallery      by remember { mutableStateOf(false) }
    var isLoadingHistory by remember { mutableStateOf(false) }

    val refreshTrigger by WasteSortStore.refreshTrigger.collectAsState()

    var selectedEntryId by remember { mutableStateOf<String?>(null) }
    var detailData by remember { mutableStateOf<ScanDetailData?>(null) }

    val greenScoreTrigger by WasteSortStore.greenScoreTrigger.collectAsState()

    LaunchedEffect(selectedEntryId, refreshTrigger, greenScoreTrigger) {
        selectedEntryId?.let { id ->
            entries.find { it.id == id }?.let { entry ->
                detailData = entry.toScanDetailData()
            }
        }
    }

    LaunchedEffect(refreshTrigger) {
        HouseholdStore.fetchHousehold()
        val token = SettingsStore.getAccessToken() ?: return@LaunchedEffect
        isLoadingHistory = true
        try {
            WasteSortStore.fetchUserScans(token)
        } finally {
            isLoadingHistory = false
        }
    }

    BackHandler(enabled = detailData != null || selectedEntryId != null) {
        selectedEntryId = null
        detailData = null
    }
    BackHandler(enabled = showScan) { showScan = false }
    BackHandler(enabled = selectedApiGroup != null) { selectedApiGroup = null }

    when {
        showScan -> {
            WasteSortScanScreen(
                onResult = { entry ->
                    WasteSortStore.add(entry)
                    WasteSortStore.triggerRefresh()
                    selectedEntryId = entry.id
                    // Set loading state if green score hasn't been fetched yet
                    detailData = entry.toScanDetailData().copy(
                        isGreenScoreLoading = entry.backendId != null && entry.greenScoreResult == null
                    )
                    showScan = false
                },
                onBack = { showScan = false },
                useGallery = useGallery,
            )
        }
        selectedApiGroup != null -> {
            val group = selectedApiGroup!!
            var scanData by remember(group) { mutableStateOf(group.toScanDetailData()) }
            ScanDetailView(
                data = scanData,
                onBack = { selectedApiGroup = null },
                onStatusChange = { newStatus ->
                    scanData = scanData.copy(status = newStatus)
                    // Use notifyRefresh() to update UI without triggering API reload (avoids race condition)
                    WasteSortStore.notifyRefresh()
                },
                displayMode = DisplayMode.FULL_SCREEN,
            )
        }
        detailData != null -> {
            ScanDetailView(
                data = detailData!!,
                onBack = {
                    selectedEntryId = null
                    detailData = null
                },
                onStatusChange = { newStatus ->
                    // OPTIMISTIC: Update detailData immediately for responsive UI
                    detailData = detailData?.copy(status = newStatus)
                    // PERSIST: Update store in background
                    selectedEntryId?.let { id ->
                        WasteSortStore.updateStatus(id, newStatus)
                    }
                },
                displayMode = DisplayMode.FULL_SCREEN,
            )
        }
        else -> {
            WasteSortList(
                apiHistory = apiHistory.filter { it.detectType == "analyze_all" },
                isLoadingHistory = isLoadingHistory,
                onCameraClick = { useGallery = false; showScan = true },
                onGalleryClick = { useGallery = true; showScan = true },
                onApiScanClick = { selectedApiGroup = it },
            )
        }
    }
}
