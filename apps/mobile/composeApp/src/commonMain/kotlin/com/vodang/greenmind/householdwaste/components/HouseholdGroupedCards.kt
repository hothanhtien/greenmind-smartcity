package com.vodang.greenmind.householdwaste.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vodang.greenmind.api.households.DetectTrashHistoryDto
import com.vodang.greenmind.api.households.bringOutDetectTrash
import com.vodang.greenmind.fmt
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.util.AppLogger
import com.vodang.greenmind.wastereport.NetworkImage
import com.vodang.greenmind.wastesort.components.EcoScoreCard
import com.vodang.greenmind.wastesort.WasteSortStatus
import com.vodang.greenmind.wastesort.label
import com.vodang.greenmind.wastesort.components.LifecycleProgressBar
import com.vodang.greenmind.householdwaste.parseWasteSortStatus
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.wastesort.categoryBg
import com.vodang.greenmind.wastesort.categoryColor
import com.vodang.greenmind.wastesort.categoryEmoji
import com.vodang.greenmind.wastesort.categoryLabel
import com.vodang.greenmind.wastesort.components.SegmentGrid
import kotlinx.coroutines.launch

private val green700  = Color(0xFF2E7D32)
private val gray700c  = Color(0xFF374151)
private val gray400c  = Color(0xFF9CA3AF)

// ── GroupedDetectScanCard ───────────────────────────────────────────────────────

@Composable
internal fun GroupedDetectScanCard(records: List<DetectTrashHistoryDto>, onClick: () -> Unit) {
    val primary     = records.minByOrNull { it.createdAt ?: "" } ?: records.first()
    val s = LocalAppStrings.current
    val detectTrash = records.find { it.detectType == "detect_trash" }
    val totalMass   = records.find { it.detectType == "total_mass" }
    val hasMass     = totalMass?.totalMassKg != null
    val objCount    = detectTrash?.totalObjects ?: primary.totalObjects

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NetworkImage(
                    url = primary.imageUrl,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusBadge(parseWasteSortStatus(primary.status))
                        Spacer(Modifier.weight(1f))
                        Text(primary.createdAt?.take(10) ?: "", fontSize = 11.sp, color = gray400c)
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                if (objCount != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(14.dp), tint = gray400c)
                        Text(s.detectedItemsCount(objCount), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = gray700c)
                    }
                }
                if (hasMass) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Scale, contentDescription = null, modifier = Modifier.size(14.dp), tint = green700)
                        Text(s.massKg(totalMass!!.totalMassKg), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = green700)
                    }
                }
            }

            (detectTrash ?: primary).items?.take(3)?.joinToString(" · ") {
                if (it.massKg != null) "${s.massKg(it.massKg!!)} ${it.name}"
                else "${it.quantity}× ${it.name}"
            }?.let {
                Text(it, fontSize = 11.sp, color = gray400c, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ── GroupedDetectScanDetailSheet ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GroupedDetectScanDetailSheet(records: List<DetectTrashHistoryDto>, onDismiss: () -> Unit) {
    val s = LocalAppStrings.current
    val sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var previewUrl       by remember { mutableStateOf<String?>(null) }

    val primary      = records.minByOrNull { it.createdAt ?: "" } ?: records.first()
    val greenScore   = records.firstNotNullOfOrNull { it.greenScore }
    var currentStatusString by remember(primary.id) { mutableStateOf(primary.status) }
    val detectTrash  = records.find { it.detectType == "detect_trash" }
    val pollutant    = records.find { it.detectType == "predict_pollutant_impact" }
    val totalMassRec = records.find { it.detectType == "total_mass" }

    // Unified fields — each sourced from the correct record type
    val totalObjects   = detectTrash?.totalObjects
    val items          = detectTrash?.items
    val totalMassKg    = totalMassRec?.totalMassKg
    val itemsMass      = totalMassRec?.itemsMass
    val depthMapUrl    = totalMassRec?.depthMapUrl
    val pollution      = pollutant?.pollution
    val impact         = pollutant?.impact
    val aiAnalysisUrl  = detectTrash?.aiAnalysis

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Lifecycle progress bar
            LifecycleProgressBar(currentStatus = parseWasteSortStatus(currentStatusString))
            HorizontalDivider(color = Color(0xFFE0E0E0))

            // Eco score (per-detection, same card as post-scan detail)
            EcoScoreCard(latestEntry = greenScore)
            HorizontalDivider(color = Color(0xFFE0E0E0))

            // Header
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(s.scanReport, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = gray700c)
                Text(primary.createdAt?.take(10) ?: "", fontSize = 12.sp, color = gray400c)
            }

            // Source image
            NetworkImage(
                url = primary.imageUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { previewUrl = primary.imageUrl },
            )

            // Segments
            val primarySegments = records.firstNotNullOfOrNull { it.segments }
            if (primarySegments != null && (primarySegments.recyclable.isNotEmpty() || primarySegments.residual.isNotEmpty())) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text(s.segments, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray700c)
                val segCategories = buildList {
                    if (primarySegments.recyclable.isNotEmpty()) add("recyclable")
                    if (primarySegments.residual.isNotEmpty()) add("residual")
                }
                var segSelected by remember { mutableStateOf(segCategories.firstOrNull() ?: "") }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    segCategories.forEach { cat ->
                        val sel = cat == segSelected
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (sel) categoryColor(cat) else categoryBg(cat))
                                .clickable { segSelected = cat }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    categoryEmoji(cat), contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (sel) Color.White else categoryColor(cat)
                                )
                                Text(
                                    categoryLabel(cat),
                                    fontSize = 13.sp,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (sel) Color.White else categoryColor(cat),
                                )
                            }
                        }
                    }
                }
                val segUrls = when (segSelected) {
                    "recyclable" -> primarySegments.recyclable
                    "residual" -> primarySegments.residual
                    else -> emptyList()
                }
                SegmentGrid(imageUrls = segUrls, category = segSelected)
            } else {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text(s.segments, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray700c)
                Text("N/A", fontSize = 12.sp, color = gray400c)
            }

            // Summary chips — scroll horizontally on small screens
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                totalObjects?.let { DetectStatChip("$it objects", Color(0xFFE8F5E9), green700, icon = Icons.Filled.Search) }
                totalMassKg?.let  { DetectStatChip(s.massKg(it), Color(0xFFEFF6FF), Color(0xFF1D4ED8), icon = Icons.Filled.Scale) }
            }

            // Detected items (from detect_trash)
            if (!items.isNullOrEmpty()) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text(s.detectedItems, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray700c)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items.forEach { item ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(s.itemBullet(item.name), fontSize = 12.sp, color = gray700c, modifier = Modifier.weight(1f))
                            val detail = when {
                                item.massKg != null -> s.massKgPrecision3(item.massKg!!)
                                item.quantity > 0   -> "×${item.quantity}"
                                else                -> ""
                            }
                            if (detail.isNotEmpty()) {
                                Text(detail, fontSize = 12.sp, color = gray400c, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Item masses (from total_mass)
            if (!itemsMass.isNullOrEmpty()) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text(s.itemMassesLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray700c)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    itemsMass.forEach { item ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(s.itemBullet(item.name), fontSize = 12.sp, color = gray700c, modifier = Modifier.weight(1f))
                            Text(s.massKgPrecision3(item.massKg), fontSize = 12.sp, color = gray400c, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Pollution (from predict_pollutant_impact)
            if (pollution != null) {
                val pollutants = listOfNotNull(
                    pollution.co2?.let              { "CO₂"               to it },
                    pollution.ch4?.let              { "CH₄"               to it },
                    pollution.nox?.let              { "NOₓ"               to it },
                    pollution.so2?.let              { "SO₂"               to it },
                    pollution.pm25?.let             { "PM2.5"             to it },
                    pollution.microplastic?.let     { "Microplastic"      to it },
                    pollution.dioxin?.let           { "Dioxin"            to it },
                    pollution.nonBiodegradable?.let { "Non-biodegradable" to it },
                    pollution.toxicChemicals?.let   { "Toxic chemicals"   to it },
                    pollution.nitrate?.let          { "Nitrate"           to it },
                    pollution.styrene?.let          { "Styrene"           to it },
                    pollution.chemicalResidue?.let  { "Chemical residue"  to it },
                    pollution.cd?.let               { "Cd"                to it },
                    pollution.hg?.let               { "Hg"                to it },
                    pollution.pb?.let               { "Pb"                to it },
                ).filter { it.second > 0.0 }
                if (pollutants.isNotEmpty()) {
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    Text(s.pollutionLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray700c)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        pollutants.forEach { (label, value) ->
                            DetectImpactBar("• $label", value)
                        }
                    }
                }
            }

            // Environmental impact (from predict_pollutant_impact)
            if (impact != null) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text(s.envImpact, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray700c)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    impact.airPollution?.let   { DetectImpactBar(s.airPollution,   it, icon = Icons.Filled.Air) }
                    impact.soilPollution?.let  { DetectImpactBar(s.soilPollution,  it, icon = Icons.Filled.Eco) }
                    impact.waterPollution?.let { DetectImpactBar(s.waterPollution, it, icon = Icons.Filled.WaterDrop) }
                }
            }

            // AI analysis image (from detect_trash)
            if (!aiAnalysisUrl.isNullOrBlank()) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text(s.aiAnalysis, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray700c)
                NetworkImage(
                    url = aiAnalysisUrl,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp))
                        .clickable { previewUrl = aiAnalysisUrl },
                )
            }

            // Depth map (from total_mass)
            if (!depthMapUrl.isNullOrBlank()) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text(s.depthMap, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray700c)
                NetworkImage(
                    url = depthMapUrl,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp))
                        .clickable { previewUrl = depthMapUrl },
                )
            }

            // Detected by
            primary.detectedBy?.let { by ->
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text(s.detectedBy, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray700c)
                Text("${by.fullName} (${by.username})", fontSize = 12.sp, color = gray400c)
            }

            HorizontalDivider(color = Color(0xFFE0E0E0))
            DetectActionButton(status = parseWasteSortStatus(currentStatusString), recordId = primary.id, onStatusChange = { currentStatusString = it })
        }
    }

    previewUrl?.let { url ->
        Dialog(
            onDismissRequest = { previewUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                com.vodang.greenmind.wastereport.ZoomableImagePreview(
                    url = url,
                    modifier = Modifier.fillMaxSize(),
                    onTap = { previewUrl = null }
                )
            }
        }
    }
}

// ── DetectActionButton ─────────────────────────────────────────────────────────

@Composable
internal fun DetectActionButton(
    status: WasteSortStatus,
    recordId: String,
    onStatusChange: (String) -> Unit,
) {
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()
    var isBusy by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val stepDone = Color(0xFF2E7D32)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Status badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(s.status, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = gray700c)
            StatusChip(status = status)
        }

        HorizontalDivider(color = Color(0xFFE0E0E0))

        when (status) {
            WasteSortStatus.SORTED -> {
                Button(
                    onClick = {
                        isBusy = true
                        errorMsg = null
                        scope.launch {
                            try {
                                val token = SettingsStore.getAccessToken() ?: throw IllegalStateException("Not signed in")
                                bringOutDetectTrash(token, recordId)
                                onStatusChange("brought_out")
                            } catch (e: Throwable) {
                                AppLogger.e("DetectAction", "bring-out failed: ${e.message}")
                                errorMsg = e.message ?: "Failed to update status"
                            } finally {
                                isBusy = false
                            }
                        }
                    },
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(s.updatingLabel, color = Color.White, fontWeight = FontWeight.Bold)
                    } else {
                        Text(s.markAsBroughtOut, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                errorMsg?.let {
                    Text(it, color = Color(0xFFD32F2F), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
                }
            }

            WasteSortStatus.BRINGOUTED -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF5F5F5)).padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.HourglassEmpty, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF757575))
                    Spacer(Modifier.width(6.dp))
                    Text(s.waitingCollectorPickup, fontSize = 14.sp, color = Color(0xFF757575), fontWeight = FontWeight.Medium)
                }
            }

            WasteSortStatus.COLLECTED -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE8F5E9)).padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = stepDone)
                    Spacer(Modifier.width(6.dp))
                    Text(s.collectedCycleComplete, fontSize = 14.sp, color = stepDone, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: WasteSortStatus) {
    val (bgColor, textColor, label) = when (status) {
        WasteSortStatus.SORTED -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "Sorted")
        WasteSortStatus.BRINGOUTED -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Brought Out")
        WasteSortStatus.COLLECTED -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Collected")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
        )
    }
}
