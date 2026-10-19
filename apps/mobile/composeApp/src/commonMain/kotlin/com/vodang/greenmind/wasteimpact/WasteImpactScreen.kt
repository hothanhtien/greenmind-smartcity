package com.vodang.greenmind.wasteimpact

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.households.DetectTrashHistoryDto
import com.vodang.greenmind.api.households.GreenScoreEntryDto
import com.vodang.greenmind.api.households.getDetectHistoryByUser
import com.vodang.greenmind.api.households.getGreenScoreByHousehold
import com.vodang.greenmind.api.wastedetect.WasteDetectImpact
import com.vodang.greenmind.api.wastedetect.WasteDetectItem
import com.vodang.greenmind.api.wastedetect.WasteDetectResponse
import com.vodang.greenmind.fmt
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.platform.BackHandler
import com.vodang.greenmind.scandetail.DisplayMode
import com.vodang.greenmind.scandetail.toScanDetailData
import com.vodang.greenmind.scandetail.components.ScanDetailView
import com.vodang.greenmind.store.HouseholdStore
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.store.WasteSortStore
import com.vodang.greenmind.wasteimpact.components.HeroStat
import com.vodang.greenmind.wasteimpact.components.ImpactMeter
import com.vodang.greenmind.wasteimpact.components.ImpactSummary
import com.vodang.greenmind.wasteimpact.components.PollutantBar
import com.vodang.greenmind.wasteimpact.components.WasteImpactScanDetail
import com.vodang.greenmind.wasteimpact.components.ScanHistoryRow
import com.vodang.greenmind.wasteimpact.components.ecoScoreColor
import com.vodang.greenmind.wasteimpact.components.ecoScoreLabel
import com.vodang.greenmind.wastesort.WasteSortEntry
import com.vodang.greenmind.wastesort.WasteSortScanScreen
import kotlin.math.roundToInt

// ── Palette (re-used from components; duplicated here to avoid circular imports) ──
private val orange700  = Color(0xFFE65100)
private val orange600  = Color(0xFFF4511E)
private val orange400  = Color(0xFFFF7043)
private val orange50   = Color(0xFFFFF3E0)
private val green800   = Color(0xFF2E7D32)
private val green50    = Color(0xFFE8F5E9)
private val red600     = Color(0xFFE53935)
private val blue600    = Color(0xFF1976D2)
private val amber      = Color(0xFFF57F17)
private val bgGray     = Color(0xFFF5F5F5)

/** Friendly display names for raw pollution keys. */
private val pollutantLabel = mapOf(
    "CO2"               to "CO₂",
    "dioxin"            to "Dioxin",
    "microplastic"      to "Microplastic",
    "toxic_chemicals"   to "Toxic chemicals",
    "non_biodegradable" to "Non-biodegradable",
    "NOx"               to "NOₓ",
    "SO2"               to "SO₂",
    "CH4"               to "CH₄",
    "PM2.5"             to "PM2.5",
    "Pb"                to "Lead (Pb)",
    "Hg"                to "Mercury (Hg)",
    "Cd"                to "Cadmium (Cd)",
    "nitrate"           to "Nitrate",
    "chemical_residue"  to "Chemical residue",
    "styrene"           to "Styrene",
)

// ── Converter ─────────────────────────────────────────────────────────────────

private fun DetectTrashHistoryDto.toWasteSortEntry(): WasteSortEntry {
    val pollutionMap: Map<String, Double>? = pollution?.let { p ->
        buildMap {
            p.cd?.let          { put("Cd", it) }
            p.hg?.let          { put("Hg", it) }
            p.pb?.let          { put("Pb", it) }
            p.ch4?.let         { put("CH4", it) }
            p.co2?.let         { put("CO2", it) }
            p.nox?.let         { put("NOx", it) }
            p.so2?.let         { put("SO2", it) }
            p.pm25?.let        { put("PM2.5", it) }
            p.dioxin?.let      { put("dioxin", it) }
            p.nitrate?.let     { put("nitrate", it) }
            p.styrene?.let     { put("styrene", it) }
            p.microplastic?.let { put("microplastic", it) }
            p.toxicChemicals?.let   { put("toxic_chemicals", it) }
            p.chemicalResidue?.let  { put("chemical_residue", it) }
            p.nonBiodegradable?.let { put("non_biodegradable", it) }
        }
    }
    val impactData = impact
    val pollutantResult: WasteDetectResponse? =
        if (pollutionMap != null && impactData != null) {
            WasteDetectResponse(
                items        = items?.map { WasteDetectItem(it.name, it.quantity, it.area) } ?: emptyList(),
                totalObjects = totalObjects ?: 0,
                imageUrl     = aiAnalysis ?: imageUrl,
                pollution    = pollutionMap,
                impact       = WasteDetectImpact(
                    airPollution   = impactData.airPollution   ?: 0.0,
                    waterPollution = impactData.waterPollution ?: 0.0,
                    soilPollution  = impactData.soilPollution  ?: 0.0,
                ),
            )
        } else null

    return WasteSortEntry(
        id           = id,
        imageUrl     = aiAnalysis ?: imageUrl,
        totalObjects = totalObjects ?: 0,
        grouped      = segments?.let { seg ->
            buildMap {
                if (seg.recyclable.isNotEmpty()) put("recyclable", seg.recyclable)
                if (seg.residual.isNotEmpty()) put("residual", seg.residual)
            }
        } ?: emptyMap(),
        createdAt    = createdAt?.take(10) ?: "",
        scannedBy    = detectedBy?.fullName ?: detectedBy?.username ?: "",
        pollutantResult = pollutantResult,
        detectType      = detectType,
    )
}

// ── Impact aggregation ────────────────────────────────────────────────────────

private val allPollutantKeys = listOf(
    "CO2", "CH4", "PM2.5", "NOx", "SO2",
    "Pb", "Hg", "Cd",
    "nitrate", "chemical_residue", "microplastic",
    "dioxin", "toxic_chemicals", "non_biodegradable", "styrene",
)

/**
 * Derives [ImpactSummary] from a list of scan entries.
 *
 * Current algorithm: simple arithmetic mean across all scans that carry
 * pollution data.  Replace only the lines marked [ALGO] to plug in a
 * weighted, decay-based, or model-driven approach later.
 */
private fun aggregateImpact(entries: List<WasteSortEntry>): ImpactSummary {
    val withImpact = entries.filter { it.pollutantResult != null }

    // TODO(ALGO): replace AVG with a smarter eco score formula (e.g. weighted by scan recency)
    val ecoScore = if (withImpact.isEmpty()) null
                   else withImpact.mapNotNull { it.pollutantResult?.ecoScore }
                       .average().roundToInt()

    // TODO(ALGO): replace AVG with a better impact aggregation (e.g. decay-weighted, model-driven)
    fun avgImpact(selector: (com.vodang.greenmind.api.wastedetect.WasteDetectImpact) -> Double?): Double? {
        if (withImpact.isEmpty()) return null
        val values = withImpact.mapNotNull { it.pollutantResult?.impact?.let(selector) }
        return if (values.isEmpty()) null else values.average()
    }
    val air   = avgImpact { it.airPollution }
    val water = avgImpact { it.waterPollution }
    val soil  = avgImpact { it.soilPollution }

    // TODO(ALGO): replace AVG with a better per-pollutant aggregation (e.g. peak, percentile, trend)
    val pollutants = mutableMapOf<String, Double>()
    allPollutantKeys.forEach { k -> pollutants[k] = 0.0 }
    if (withImpact.isNotEmpty()) {
        withImpact.forEach { entry ->
            entry.pollutantResult?.pollution?.forEach { (k, v) ->
                pollutants[k] = (pollutants[k] ?: 0.0) + v
            }
        }
        val count = withImpact.size.toDouble()
        pollutants.keys.toList().forEach { k -> pollutants[k] = (pollutants[k] ?: 0.0) / count }
    }

    return ImpactSummary(
        totalScans = entries.size,
        totalItems = entries.sumOf { it.totalObjects },
        ecoScore   = ecoScore,
        air        = air,
        water      = water,
        soil       = soil,
        pollutants = pollutants,
    )
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun WasteImpactScreen() {
    val s     = LocalAppStrings.current
    val token = SettingsStore.getAccessToken()

    var apiEntries    by remember { mutableStateOf<List<WasteSortEntry>>(emptyList()) }
    var greenScoreEntries by remember { mutableStateOf<List<GreenScoreEntryDto>>(emptyList()) }
    var isLoading     by remember { mutableStateOf(false) }
    var error         by remember { mutableStateOf<String?>(null) }
    var refreshKey    by remember { mutableIntStateOf(0) }
    var selectedEntry by remember { mutableStateOf<WasteSortEntry?>(null) }

    // Scan flow state — same mechanism as WasteSortScreen
    var showScan   by remember { mutableStateOf(false) }
    var useGallery by remember { mutableStateOf(false) }
    var fabExpanded by remember { mutableStateOf(false) }

    val household by HouseholdStore.household.collectAsState()

    // Fetch user's scan history + green score from the server
    LaunchedEffect(refreshKey, token, household) {
        val t = token ?: return@LaunchedEffect
        isLoading = true
        error = null
        try {
            apiEntries = getDetectHistoryByUser(t).data
                .filter { it.pollution != null || it.impact != null }
                .map { it.toWasteSortEntry() }
                .sortedByDescending { it.createdAt }
            // Fetch green score if household is available
            val hId = household?.id
            if (hId != null) {
                val scoreResp = runCatching { getGreenScoreByHousehold(t, hId) }
                scoreResp.getOrNull()?.data?.greenScores?.let { greenScoreEntries = it }
            }
        } catch (e: Throwable) {
            error = e.message ?: s.wasteImpactError
        } finally {
            isLoading = false
        }
    }

    val mergedEntries = apiEntries

    BackHandler(enabled = selectedEntry != null) { selectedEntry = null }
    BackHandler(enabled = showScan) { showScan = false }

    val current = selectedEntry
    if (current != null) {
        ScanDetailView(
            data = current.toScanDetailData(),
            onBack = { selectedEntry = null },
            displayMode = DisplayMode.FULL_SCREEN,
        )
        return
    }

    if (showScan) {
        WasteSortScanScreen(
            onResult = { entry ->
                // Persist into WasteSortStore (same as WasteSortScreen) and
                // trigger this screen to refetch the detect history so the
                // new scan shows up in the impact aggregation immediately.
                WasteSortStore.add(entry)
                WasteSortStore.triggerRefresh()
                refreshKey++
                showScan = false
            },
            onBack = { showScan = false },
            useGallery = useGallery,
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGray)
    ) {
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = orange600)
            }

            error != null && mergedEntries.isEmpty() -> Column(
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(48.dp), tint = orange600)
                Text(error!!, fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
                Button(
                    onClick = { refreshKey++ },
                    colors = ButtonDefaults.buttonColors(containerColor = orange600),
                    shape = RoundedCornerShape(10.dp),
                ) { Text(s.wasteImpactRetry) }
            }

            mergedEntries.isEmpty() -> Column(
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(Icons.Filled.Analytics, contentDescription = null, modifier = Modifier.size(48.dp))
                Text(s.wasteImpactNoData, fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
            }

            else -> WasteImpactContent(
                entries           = mergedEntries,
                greenScoreEntries = greenScoreEntries,
                onEntryClick      = { selectedEntry = it },
                onRefresh         = { refreshKey++ },
            )
        }

        // ── Scan FAB ─────────────────────────────────────────────────────────
        // Same mechanism as the Waste Sort screen — expandable into camera /
        // gallery mini-FABs that launch WasteSortScanScreen and reuse the
        // shared detect-trash API.
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp),
        ) {
            AnimatedVisibility(
                visible = fabExpanded,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit  = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ImpactMiniFabRow(
                        icon = "🖼",
                        label = s.uploadImage,
                        onClick = {
                            fabExpanded = false
                            useGallery = true
                            showScan = true
                        },
                    )
                    ImpactMiniFabRow(
                        icon = "📷",
                        label = s.takePhoto,
                        onClick = {
                            fabExpanded = false
                            useGallery = false
                            showScan = true
                        },
                    )
                }
            }

            FloatingActionButton(
                onClick = { fabExpanded = !fabExpanded },
                containerColor = green800,
                contentColor = Color.White,
                shape = CircleShape,
            ) {
                Text(
                    if (fabExpanded) s.fabCollapse else s.fabExpand,
                    fontSize = if (fabExpanded) 20.sp else 28.sp,
                    fontWeight = FontWeight.Light,
                )
            }
        }
    }
}

@Composable
private fun ImpactMiniFabRow(icon: String, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(label, fontSize = 13.sp, color = Color(0xFF1B1B1B), fontWeight = FontWeight.Medium)
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(green800),
            contentAlignment = Alignment.Center,
        ) {
            Text(icon, fontSize = 18.sp)
        }
    }
}

// ── Content ────────────────────────────────────────────────────────────────────

@Composable
private fun WasteImpactContent(
    entries: List<WasteSortEntry>,
    greenScoreEntries: List<GreenScoreEntryDto> = emptyList(),
    onEntryClick: (WasteSortEntry) -> Unit,
    onRefresh: () -> Unit = {},
) {
    val s = LocalAppStrings.current
    var showAllPollutants by remember { mutableStateOf(false) }

    val summary = aggregateImpact(entries)

    val totalScans       = summary.totalScans
    val totalItems       = summary.totalItems
    val latestGreenScore = greenScoreEntries.lastOrNull()
    val avgEcoScore      = latestGreenScore?.finalScore ?: summary.ecoScore
    val totalAir         = summary.air
    val totalWater       = summary.water
    val totalSoil        = summary.soil
    val aggregatedMap    = summary.pollutants
    val withImpact       = entries.filter { it.pollutantResult != null }

    val aggregatedPollutants: List<Pair<String, Double>> =
        aggregatedMap.entries
            .filter { it.value > 0.0 }
            .sortedByDescending { it.value }
            .take(6)
            .map { it.key to it.value }

    val allActivePollutants: List<Pair<String, Double>> =
        aggregatedMap.entries
            .filter { it.value > 0.0 }
            .sortedByDescending { it.value }
            .map { it.key to it.value }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Hero card ─────────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFF1B5E20), Color(0xFF388E3C), Color(0xFF43A047))))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(s.environmentalImpact, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Refresh, contentDescription = s.refresh, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HeroStat("$totalScans", s.scans, Modifier.weight(1f).fillMaxHeight())
                        HeroStat("$totalItems", s.itemsDetected, Modifier.weight(1f).fillMaxHeight())
                        HeroStat(
                            value = if (avgEcoScore != null) "$avgEcoScore" else s.noEcoScore,
                            label = if (latestGreenScore != null) s.greenScoreTitle else s.avgEcoScore,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                }
            }
        }

        // ── Eco score + Impact ────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Eco score
                if (avgEcoScore != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(ecoScoreColor(avgEcoScore).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(s.ecoScorePercent(avgEcoScore), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ecoScoreColor(avgEcoScore))
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(ecoScoreLabel(avgEcoScore), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ecoScoreColor(avgEcoScore))
                                if (latestGreenScore != null) {
                                    val delta = latestGreenScore.delta
                                    val dc = if (delta >= 0) green800 else red600
                                    Text(if (delta >= 0) "+$delta" else "$delta", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dc)
                                }
                            }
                            LinearProgressIndicator(
                                progress = { avgEcoScore / 100f },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = ecoScoreColor(avgEcoScore),
                                trackColor = ecoScoreColor(avgEcoScore).copy(alpha = 0.15f)
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                }

                // Impact meters
                if (totalAir != null && totalWater != null && totalSoil != null) {
                    Text(s.averagePollutionImpact, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF424242))
                    ImpactMeter(s.airIcon, s.air, totalAir.toFloat())
                    ImpactMeter("💧", s.waterPollutionLabel, totalWater.toFloat())
                    ImpactMeter("🌱", s.soilPollutionLabel, totalSoil.toFloat())
                }
            }
        }

        // ── Pollutants ────────────────────────────────────────────────────────
        if (allActivePollutants.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(s.topPollutantsAvg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF424242))
                        Text(s.activePollutants(allActivePollutants.size), fontSize = 11.sp, color = Color.Gray)
                    }
                    val displayList = if (showAllPollutants) allActivePollutants else aggregatedPollutants
                    displayList.forEachIndexed { idx, (key, value) ->
                        val barColor = when {
                            value < 0.5  -> green800
                            value <= 0.7 -> amber
                            else         -> red600
                        }
                        PollutantBar(rank = idx + 1, label = pollutantLabel[key] ?: key, value = value, barColor = barColor)
                    }
                    if (allActivePollutants.size > 6) {
                        TextButton(
                            onClick = { showAllPollutants = !showAllPollutants },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 0.dp),
                        ) {
                            Text(
                                if (showAllPollutants) s.showLess else s.showAllPollutants(allActivePollutants.size),
                                fontSize = 12.sp, color = orange700
                            )
                        }
                    }
                }
            }
        }

        // ── Green Score History ────────────────────────────────────────────────
        if (greenScoreEntries.size > 1) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(s.greenScoreHistory, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF424242))
                    greenScoreEntries.reversed().forEach { entry ->
                        val isPositive = entry.delta >= 0
                        val dc = if (isPositive) green800 else red600
                        val db = if (isPositive) green50 else Color(0xFFFEF2F2)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(entry.createdAt.take(10), fontSize = 11.sp, color = Color.Gray)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(s.scoreTransition(entry.previousScore, entry.finalScore), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF424242))
                                Box(Modifier.clip(RoundedCornerShape(6.dp)).background(db).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text(if (isPositive) "+${entry.delta}" else "${entry.delta}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dc)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Scan history ─────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(s.scanHistoryTitle, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF424242))
                Spacer(Modifier.height(4.dp))
                entries.forEachIndexed { idx, entry ->
                    ScanHistoryRow(entry = entry, onClick = { onEntryClick(entry) })
                    if (idx < entries.lastIndex) HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                }
            }
        }

        // ── Tip footer ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(green50).padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(s.wasteImpactTip, fontSize = 12.sp, color = green800, lineHeight = 18.sp)
        }

        Spacer(Modifier.navigationBarsPadding())
    }
}
