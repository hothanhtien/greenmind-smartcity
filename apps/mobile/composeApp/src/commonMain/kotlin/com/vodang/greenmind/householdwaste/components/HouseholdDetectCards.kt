package com.vodang.greenmind.householdwaste.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vodang.greenmind.api.households.DetectTrashHistoryDto
import com.vodang.greenmind.fmt
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.wastereport.NetworkImage
import com.vodang.greenmind.wastesort.WasteSortStatus
import com.vodang.greenmind.wastesort.label
import com.vodang.greenmind.wastesort.components.EcoScoreCard
import com.vodang.greenmind.wastesort.components.LifecycleProgressBar
import com.vodang.greenmind.householdwaste.parseWasteSortStatus
import com.vodang.greenmind.householdwaste.components.DetectActionButton

private val green700  = Color(0xFF2E7D32)
private val green50c  = Color(0xFFE8F5E9)
private val gray700c  = Color(0xFF374151)
private val gray400c  = Color(0xFF9CA3AF)

// ── StatusBadge ─────────────────────────────────────────────────────────────────

@Composable
internal fun StatusBadge(status: WasteSortStatus) {
    val bg = when(status) {
        WasteSortStatus.SORTED -> Color(0xFFE3F2FD)
        WasteSortStatus.BRINGOUTED -> Color(0xFFFEF3C7)
        WasteSortStatus.COLLECTED -> Color(0xFFE8F5E9)
    }
    val fg = when(status) {
        WasteSortStatus.SORTED -> Color(0xFF1565C0)
        WasteSortStatus.BRINGOUTED -> Color(0xFFB45309)
        WasteSortStatus.COLLECTED -> Color(0xFF2E7D32)
    }
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(status.label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

// ── DetectScanCard ───────────────────────────────────────────────────────────────

@Composable
internal fun DetectScanCard(record: DetectTrashHistoryDto, onClick: () -> Unit) {
    val s = LocalAppStrings.current

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NetworkImage(
                url = record.imageUrl,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(10.dp))
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(parseWasteSortStatus(record.status))
                    Text(record.createdAt?.take(10) ?: "", fontSize = 10.sp, color = gray400c)
                }
                Text(
                    s.objectsDetected(record.totalObjects ?: 0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = gray700c
                )
                record.items?.take(3)?.joinToString(" · ") {
                    if (it.massKg != null) "${s.massKg(it.massKg!!)} ${it.name}"
                    else "${it.quantity}× ${it.name}"
                }?.let {
                    Text(it, fontSize = 11.sp, color = gray400c, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                record.totalMassKg?.let {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Category, contentDescription = null, modifier = Modifier.size(14.dp), tint = green700)
                        Text(s.massKg(it), fontSize = 11.sp, color = green700)
                    }
                }
            }
        }
    }
}

// ── DetectScanDetailSheet ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DetectScanDetailSheet(scan: DetectTrashHistoryDto, onDismiss: () -> Unit) {
    val s = LocalAppStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var previewUrl       by remember { mutableStateOf<String?>(null) }
    var currentStatusString by remember { mutableStateOf(scan.status) }

    val detectLabel = when (scan.detectType) {
        "detect_trash"             -> "Detect Trash"
        "predict_pollutant_impact" -> "Pollutant Impact"
        "total_mass"               -> "Total Mass"
        else                       -> scan.detectType ?: "Unknown"
    }

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
            EcoScoreCard(latestEntry = scan.greenScore)
            HorizontalDivider(color = Color(0xFFE0E0E0))

            // Header
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(detectLabel, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = gray700c)
                Text(scan.createdAt?.take(10) ?: "", fontSize = 12.sp, color = gray400c)
            }

            // Source image
            if (!scan.imageUrl.isNullOrBlank()) {
                NetworkImage(
                    url = scan.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { previewUrl = scan.imageUrl },
                )
            }

            // Summary row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                scan.totalObjects?.let {
                    DetectStatChip("$it objects", green50c, green700, icon = Icons.Filled.Search)
                }
                scan.totalMassKg?.let {
                    DetectStatChip(s.massKg(it), Color(0xFFEFF6FF), Color(0xFF1D4ED8), icon = Icons.Filled.Category)
                }
            }

            // Detected items
            if (!scan.items.isNullOrEmpty()) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text(s.detectedItems, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray700c)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    scan.items.forEach { item ->
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

            // AI analysis image
            if (!scan.aiAnalysis.isNullOrBlank()) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text(s.aiAnalysis, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray700c)
                NetworkImage(
                    url = scan.aiAnalysis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { previewUrl = scan.aiAnalysis },
                )
            }

            // Depth map
            if (!scan.depthMapUrl.isNullOrBlank()) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text(s.depthMap, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray700c)
                NetworkImage(
                    url = scan.depthMapUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { previewUrl = scan.depthMapUrl },
                )
            }

            // Pollution
            val p = scan.pollution
            if (p != null) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text(s.pollutionLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray700c)
                val pollutants = listOfNotNull(
                    p.co2?.let              { "CO₂"               to it },
                    p.ch4?.let              { "CH₄"               to it },
                    p.nox?.let              { "NOₓ"               to it },
                    p.so2?.let              { "SO₂"               to it },
                    p.pm25?.let             { "PM2.5"             to it },
                    p.microplastic?.let     { "Microplastic"      to it },
                    p.dioxin?.let           { "Dioxin"            to it },
                    p.nonBiodegradable?.let { "Non-biodegradable" to it },
                    p.toxicChemicals?.let   { "Toxic chemicals"   to it },
                    p.nitrate?.let          { "Nitrate"           to it },
                    p.styrene?.let          { "Styrene"           to it },
                    p.chemicalResidue?.let  { "Chemical residue"  to it },
                    p.cd?.let               { "Cd"                to it },
                    p.hg?.let               { "Hg"                to it },
                    p.pb?.let               { "Pb"                to it },
                ).filter { it.second > 0.0 }

                if (pollutants.isEmpty()) {
                    Text(s.noSignificantPollutants, fontSize = 12.sp, color = gray400c)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        pollutants.forEach { (label, value) ->
                            DetectImpactBar("• $label", value)
                        }
                    }
                }
            }

            // Impact
            val imp = scan.impact
            if (imp != null) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text(s.envImpact, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray700c)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    imp.airPollution?.let   { DetectImpactBar(s.airPollution,   it, icon = Icons.Filled.Air) }
                    imp.soilPollution?.let  { DetectImpactBar(s.soilPollution,  it, icon = Icons.Filled.Eco) }
                    imp.waterPollution?.let { DetectImpactBar(s.waterPollution, it, icon = Icons.Filled.WaterDrop) }
                }
            }

            // Detected by
            scan.detectedBy?.let { by ->
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text(s.detectedBy, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray700c)
                Text("${by.fullName} (${by.username})", fontSize = 12.sp, color = gray400c)
            }

            HorizontalDivider(color = Color(0xFFE0E0E0))
            DetectActionButton(status = parseWasteSortStatus(currentStatusString), recordId = scan.id, onStatusChange = { currentStatusString = it })
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

// ── DetectStatChip ─────────────────────────────────────────────────────────────

@Composable
internal fun DetectStatChip(label: String, bg: Color, fg: Color, icon: ImageVector? = null) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = fg)
            }
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = fg)
        }
    }
}

// ── DetectImpactBar ─────────────────────────────────────────────────────────────

@Composable
internal fun DetectImpactBar(label: String, value: Double, icon: ImageVector? = null) {
    val progress = value.toFloat().coerceIn(0f, 1f)
    val barColor = when {
        value < 0.5  -> Color(0xFF2E7D32)
        value <= 0.7 -> Color(0xFFF57F17)
        else         -> Color(0xFFE53935)
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = gray700c)
                }
                Text(label, fontSize = 12.sp, color = gray700c)
            }
            Text("%.4f".fmt(value), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = barColor)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = barColor.copy(alpha = 0.12f),
        )
    }
}
