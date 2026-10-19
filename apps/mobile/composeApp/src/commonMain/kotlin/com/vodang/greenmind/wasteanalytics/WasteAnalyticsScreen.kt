package com.vodang.greenmind.wasteanalytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.scandetail.allPollutantKeys
import com.vodang.greenmind.scandetail.getPollutantLabel
import kotlin.math.roundToInt
import com.vodang.greenmind.fmt
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.register.pickerMillisToIsoDate
import com.vodang.greenmind.time.currentTimeMillis
import com.vodang.greenmind.time.todayLocalIsoDate
import com.vodang.greenmind.wasteanalytics.components.MultiSeriesCard
import com.vodang.greenmind.wasteanalytics.components.PeriodChip
import com.vodang.greenmind.wasteanalytics.components.MetricChip
import com.vodang.greenmind.wasteanalytics.components.SeriesData
import com.vodang.greenmind.wasteanalytics.components.SummaryChip
import com.vodang.greenmind.wasteanalytics.components.WasteVolumeCard

private val bgGray    = Color(0xFFF5F5F5)
private val blue700   = Color(0xFF1565C0)
private val blue600   = Color(0xFF1976D2)
private val blue50    = Color(0xFFE3F2FD)
private val red700    = Color(0xFFC62828)
private val amber     = Color(0xFFF57F17)
private val teal600  = Color(0xFF00897B)
private val purple700 = Color(0xFF6A1B9A)

// ── Data Processing ─────────────────────────────────────────────────────────────

internal data class PeriodData(
    val labels: List<String>,
    val wasteKg: List<Float>,
    val airPollution: List<Float>,
    val waterPollution: List<Float>,
    val soilPollution: List<Float>,
    val pollutants: Map<String, List<Float>>,
)

private data class AnalyticsFilterState(
    val hourDate: ParsedDate,
    val dayStart: ParsedDate,
    val dayEnd: ParsedDate,
    val weekMonth: Int,
    val weekYear: Int,
    val monthYear: Int,
    val yearStart: Int,
    val yearEnd: Int,
)

private data class ParsedDate(val year: Int, val month: Int, val day: Int, val hour: Int)

private fun parseDate(iso: String?): ParsedDate {
    if (iso == null || iso.length < 19) return ParsedDate(0, 0, 0, 0)
    return try {
        val year = iso.substring(0, 4).toInt()
        val month = iso.substring(5, 7).toInt()
        val day = iso.substring(8, 10).toInt()
        val hour = iso.substring(11, 13).toInt()
        // Parse timezone offset: "+07:00" → +7, "-05:30" → -5, "Z" → 0
        val offsetHours = if (iso.length >= 25 && iso[19] in "+-") {
            val sign = if (iso[19] == '+') 1 else -1
            val offH = iso.substring(20, 22).toInt()
            val offM = iso.substring(23, 25).toInt()
            sign * (offH + offM / 60)
        } else 0
        val adjustedHour = (hour + offsetHours) % 24
        val adjustedDay = if (adjustedHour < 0) day - 1 else day
        val adjustedMonth = if (adjustedDay < 1) month - 1 else month
        ParsedDate(year, adjustedMonth, if (adjustedDay < 1) 1 else adjustedDay, (adjustedHour + 24) % 24)
    } catch (e: Exception) {
        ParsedDate(0, 0, 0, 0)
    }
}

private fun List<Double>.avgOrZero(): Float {
    if (this.isEmpty()) return 0f
    val avg = this.average()
    return if (avg.isNaN()) 0f else avg.toFloat()
}

private fun buildPeriodData(labels: List<String>, buckets: Array<MutableList<com.vodang.greenmind.api.households.DetectTrashHistoryDto>>): PeriodData {
    return PeriodData(
        labels = labels,
        wasteKg = buckets.map { list -> list.sumOf { it.totalMassKg ?: 0.0 }.toFloat() },
        airPollution = buckets.map { list -> list.mapNotNull { it.impact?.airPollution }.avgOrZero() },
        waterPollution = buckets.map { list -> list.mapNotNull { it.impact?.waterPollution }.avgOrZero() },
        soilPollution = buckets.map { list -> list.mapNotNull { it.impact?.soilPollution }.avgOrZero() },
        pollutants = allPollutantKeys.associate { key ->
            val values = buckets.map { list ->
                list.mapNotNull { it.pollution?.getPollutantValue(key) }.avgOrZero()
            }
            key to values
        },
    )
}

private fun com.vodang.greenmind.api.households.DetectPollutionDto.getPollutantValue(key: String): Double? = when (key) {
    "CO2" -> co2
    "dioxin" -> dioxin
    "microplastic" -> microplastic
    "toxic_chemicals" -> toxicChemicals
    "non_biodegradable" -> nonBiodegradable
    "NOx" -> nox
    "SO2" -> so2
    "CH4" -> ch4
    "PM2.5" -> pm25
    "Pb" -> pb
    "Hg" -> hg
    "Cd" -> cd
    "nitrate" -> nitrate
    "chemical_residue" -> chemicalResidue
    "styrene" -> styrene
    else -> null
}

private fun sampleEvenly(items: List<ParsedDate>, limit: Int): List<ParsedDate> {
    if (items.size <= limit) return items
    if (limit <= 1) return listOf(items.first())
    return (0 until limit).map { i ->
        val idx = ((i.toFloat() * (items.lastIndex.toFloat())) / (limit - 1).toFloat()).roundToInt().coerceIn(0, items.lastIndex)
        items[idx]
    }.distinctBy { "${it.year}-${it.month}-${it.day}" }
}

private fun processData(
    raw: List<com.vodang.greenmind.api.households.DetectTrashHistoryDto>,
    period: String,
    filters: AnalyticsFilterState,
): PeriodData {
    val emptyPeriod = PeriodData(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyMap())
    if (raw.isEmpty()) return emptyPeriod
    
    val sorted = raw.sortedBy { it.createdAt.orEmpty() }
    
    return when (period) {
        "Hour" -> {
            val labels = (0..23).map { h -> "%02d:00".format(h) }
            val buckets = Array(24) { mutableListOf<com.vodang.greenmind.api.households.DetectTrashHistoryDto>() }
            sorted.forEach {
                val pd = parseDate(it.createdAt)
                if (pd.year == filters.hourDate.year && pd.month == filters.hourDate.month && pd.day == filters.hourDate.day) {
                    val idx = pd.hour.coerceIn(0, 23)
                    buckets[idx].add(it)
                }
            }
            buildPeriodData(labels, buckets)
        }
        "Day" -> {
            val start = filters.dayStart
            val end = filters.dayEnd
            val startKeyRaw = start.year * 10000 + start.month * 100 + start.day
            val endKeyRaw = end.year * 10000 + end.month * 100 + end.day
            val startKey = minOf(startKeyRaw, endKeyRaw)
            val endKey = maxOf(startKeyRaw, endKeyRaw)
            val inRange = sorted.filter {
                val pd = parseDate(it.createdAt)
                val key = pd.year * 10000 + pd.month * 100 + pd.day
                key in startKey..endKey
            }
            val allDays = inRange.map { parseDate(it.createdAt) }
                .distinctBy { "${it.year}-${it.month}-${it.day}" }
                .sortedWith(compareBy({ it.year }, { it.month }, { it.day }))
            val sampled = sampleEvenly(allDays, 7)
            val labels = sampled.map { "%02d/%02d".format(it.day, it.month) }
            val buckets = Array(sampled.size) { mutableListOf<com.vodang.greenmind.api.households.DetectTrashHistoryDto>() }
            inRange.forEach { item ->
                val pd = parseDate(item.createdAt)
                val idx = sampled.indexOfFirst { it.year == pd.year && it.month == pd.month && it.day == pd.day }
                if (idx != -1) buckets[idx].add(item)
            }
            if (labels.isEmpty()) buildPeriodData(listOf("No Data"), Array(1) { mutableListOf() }) else buildPeriodData(labels, buckets)
        }
        "Week" -> {
            val labels = listOf("W1", "W2", "W3", "W4")
            val buckets = Array(4) { mutableListOf<com.vodang.greenmind.api.households.DetectTrashHistoryDto>() }
            sorted.forEach {
                val pd = parseDate(it.createdAt)
                if (pd.year == filters.weekYear && pd.month == filters.weekMonth) {
                    val idx = ((pd.day - 1) / 7).coerceIn(0, 3)
                    buckets[idx].add(it)
                }
            }
            buildPeriodData(labels, buckets)
        }
        "Month" -> {
            val labels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val buckets = Array(12) { mutableListOf<com.vodang.greenmind.api.households.DetectTrashHistoryDto>() }
            sorted.forEach {
                val pd = parseDate(it.createdAt)
                if (pd.year == filters.monthYear) {
                    val idx = (pd.month - 1).coerceIn(0, 11)
                    buckets[idx].add(it)
                }
            }
            buildPeriodData(labels, buckets)
        }
        "Year" -> {
            val yStart = minOf(filters.yearStart, filters.yearEnd)
            val yEnd = maxOf(filters.yearStart, filters.yearEnd)
            val years = (yStart..yEnd).toList()
            val sampledYears = if (years.size <= 5) years else {
                (0 until 5).map { i ->
                    val idx = ((i.toFloat() * (years.lastIndex.toFloat())) / 4f).roundToInt().coerceIn(0, years.lastIndex)
                    years[idx]
                }.distinct()
            }
            val labels = sampledYears.map { it.toString() }
            val buckets = Array(sampledYears.size) { mutableListOf<com.vodang.greenmind.api.households.DetectTrashHistoryDto>() }
            sorted.forEach { item ->
                val pd = parseDate(item.createdAt)
                val idx = sampledYears.indexOf(pd.year)
                if (idx != -1) buckets[idx].add(item)
            }
            buildPeriodData(labels, buckets)
        }
        else -> emptyPeriod
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WasteAnalyticsScreen(onBack: () -> Unit = {}) {
    val s = LocalAppStrings.current
    var selectedPeriod by remember { mutableStateOf("Day") }
    var selectedMetric by remember { mutableStateOf("All") }

    val today = remember { parseDate("${todayLocalIsoDate()}T00:00:00") }
    var hourDate by remember { mutableStateOf(today) }
    var dayStart by remember { mutableStateOf(
        parseDate("${pickerMillisToIsoDate(currentTimeMillis() - 7L * 86400000L)}T00:00:00")
    ) }
    var dayEnd by remember { mutableStateOf(today) }
    var weekMonth by remember { mutableIntStateOf(today.month.coerceIn(1, 12)) }
    var weekYear by remember { mutableIntStateOf(today.year.coerceAtLeast(2000)) }
    var monthYear by remember { mutableIntStateOf(today.year.coerceAtLeast(2000)) }
    var yearStart by remember { mutableIntStateOf((today.year - 4).coerceAtLeast(2000)) }
    var yearEnd by remember { mutableIntStateOf(today.year.coerceAtLeast(2000)) }

    var showHourDatePicker by remember { mutableStateOf(false) }
    var showDayStartPicker by remember { mutableStateOf(false) }
    var showDayEndPicker by remember { mutableStateOf(false) }
    
    var isLoading by remember { mutableStateOf(true) }
    var rawData by remember { mutableStateOf<List<com.vodang.greenmind.api.households.DetectTrashHistoryDto>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        val token = com.vodang.greenmind.store.SettingsStore.getAccessToken()
        if (token != null) {
            try {
                isLoading = true
                val response = com.vodang.greenmind.api.households.getDetectsMonthly(token)
                rawData = response.data
            } catch (e: Exception) {
                // Ignore or handle error
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }
    
    val minYear = remember(rawData) { rawData.map { parseDate(it.createdAt).year }.filter { it > 0 }.minOrNull() ?: today.year }
    val maxYear = remember(rawData) { rawData.map { parseDate(it.createdAt).year }.filter { it > 0 }.maxOrNull() ?: today.year }

    val filters = remember(hourDate, dayStart, dayEnd, weekMonth, weekYear, monthYear, yearStart, yearEnd) {
        AnalyticsFilterState(
            hourDate = hourDate,
            dayStart = dayStart,
            dayEnd = dayEnd,
            weekMonth = weekMonth,
            weekYear = weekYear,
            monthYear = monthYear,
            yearStart = yearStart,
            yearEnd = yearEnd,
        )
    }

    val data = remember(rawData, selectedPeriod, filters) { processData(rawData, selectedPeriod, filters) }

    val showWaste      = selectedMetric == "All" || selectedMetric == "Waste (kg)"
    val showImpact     = selectedMetric == "All" || selectedMetric == "Impact"
    val showPollutants = selectedMetric == "All" || selectedMetric == "Pollutants"

    val totalWaste = data.wasteKg.sum()
    val avgAir     = data.airPollution.average().toFloat().takeIf { !it.isNaN() } ?: 0f
    val avgCo2     = data.pollutants["CO2"]?.average()?.toFloat()?.takeIf { !it.isNaN() } ?: 0f
    val peakKg     = data.wasteKg.maxOrNull() ?: 0f
    val peakLabel  = data.labels.getOrNull(data.wasteKg.indexOfFirst { it == peakKg }.coerceAtLeast(0)) ?: ""

    Box(modifier = Modifier.fillMaxSize().background(bgGray)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back, tint = blue700)
                }
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(s.wasteAnalyticsTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = blue700)
                    Text(s.householdTrends(selectedPeriod), fontSize = 11.sp, color = Color.Gray)
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = blue600)
                }
                return@Column
            }

            if (data.labels.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                    Text(s.wasteImpactNoData, color = Color.Gray, fontSize = 14.sp)
                }
                return@Column
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ── Period selector ───────────────────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Hour", "Day", "Week", "Month", "Year").forEach { p ->
                        PeriodChip(label = p, selected = selectedPeriod == p) { selectedPeriod = p }
                    }
                }

                when (selectedPeriod) {
                    "Hour" -> {
                        OutlinedButton(onClick = { showHourDatePicker = true }) {
                            Text("Date: %02d/%02d/%04d".format(hourDate.day, hourDate.month, hourDate.year))
                        }
                    }
                    "Day" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { showDayStartPicker = true }, modifier = Modifier.weight(1f)) {
                                Text("From: %02d/%02d/%04d".format(dayStart.day, dayStart.month, dayStart.year))
                            }
                            OutlinedButton(onClick = { showDayEndPicker = true }, modifier = Modifier.weight(1f)) {
                                Text("To: %02d/%02d/%04d".format(dayEnd.day, dayEnd.month, dayEnd.year))
                            }
                        }
                    }
                    "Week" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var monthExpanded by remember { mutableStateOf(false) }
                            var yearExpanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(onClick = { monthExpanded = true }) { Text("Month: $weekMonth") }
                                DropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false }) {
                                    (1..12).forEach { m ->
                                        DropdownMenuItem(text = { Text(m.toString()) }, onClick = { weekMonth = m; monthExpanded = false })
                                    }
                                }
                            }
                            Box {
                                OutlinedButton(onClick = { yearExpanded = true }) { Text("Year: $weekYear") }
                                DropdownMenu(expanded = yearExpanded, onDismissRequest = { yearExpanded = false }) {
                                    (minYear..maxYear).forEach { y ->
                                        DropdownMenuItem(text = { Text(y.toString()) }, onClick = { weekYear = y; yearExpanded = false })
                                    }
                                }
                            }
                        }
                    }
                    "Month" -> {
                        var yearExpanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(onClick = { yearExpanded = true }) { Text("Year: $monthYear") }
                            DropdownMenu(expanded = yearExpanded, onDismissRequest = { yearExpanded = false }) {
                                (minYear..maxYear).forEach { y ->
                                    DropdownMenuItem(text = { Text(y.toString()) }, onClick = { monthYear = y; yearExpanded = false })
                                }
                            }
                        }
                    }
                    "Year" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var fromExpanded by remember { mutableStateOf(false) }
                            var toExpanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(onClick = { fromExpanded = true }) { Text("From: $yearStart") }
                                DropdownMenu(expanded = fromExpanded, onDismissRequest = { fromExpanded = false }) {
                                    (minYear..maxYear).forEach { y ->
                                        DropdownMenuItem(text = { Text(y.toString()) }, onClick = { yearStart = y; fromExpanded = false })
                                    }
                                }
                            }
                            Box {
                                OutlinedButton(onClick = { toExpanded = true }) { Text("To: $yearEnd") }
                                DropdownMenu(expanded = toExpanded, onDismissRequest = { toExpanded = false }) {
                                    (minYear..maxYear).forEach { y ->
                                        DropdownMenuItem(text = { Text(y.toString()) }, onClick = { yearEnd = y; toExpanded = false })
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Metric filter ─────────────────────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Waste (kg)", "Impact", "Pollutants").forEach { m ->
                        MetricChip(label = m, selected = selectedMetric == m) { selectedMetric = m }
                    }
                }

                // ── Summary chips ─────────────────────────────────────────────
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryChip(
                        s.totalWaste,
                        "${totalWaste.fmt(1)} kg",
                        blue600,
                        Modifier.weight(1f)
                    )
                    SummaryChip(
                        s.peak(peakLabel),
                        "${peakKg.fmt(1)} kg",
                        Color(0xFFE65100),
                        Modifier.weight(1f)
                    )
                    SummaryChip(
                        s.avgAir,
                        "${(avgAir * 100).roundToInt()}",
                        red700,
                        Modifier.weight(1f)
                    )
                }

                // ── Waste volume chart ────────────────────────────────────────
                if (showWaste) {
                    WasteVolumeCard(data = data)
                }

                // ── Impact trends chart ───────────────────────────────────────
                if (showImpact) {
                    MultiSeriesCard(
                        title    = s.pollutionImpactTrends,
                        subtitle = s.airWaterSoil,
                        icon     = Icons.Filled.Analytics,
                        seriesList = listOf(
                            SeriesData(s.seriesAir,   red700,  data.airPollution),
                            SeriesData(s.seriesWater, blue600, data.waterPollution),
                            SeriesData(s.seriesSoil,  amber,   data.soilPollution),
                        ),
                        xLabels = data.labels,
                    )
                }

                // ── Pollutants chart ──────────────────────────────────────────
                if (showPollutants) {
                    val pollutantSeries = data.pollutants.entries
                        .sortedByDescending { (_, values) -> values.average().toFloat() }
                        .map { (key, values) ->
                            val color = when (key) {
                                "CO2" -> red700; "CH4" -> Color(0xFF795548)
                                "PM2.5" -> Color(0xFF607D8B); "NOx" -> Color(0xFF9C27B0)
                                "SO2" -> Color(0xFFFF9800); "Pb" -> Color(0xFF3F51B5)
                                "Hg" -> Color(0xFF009688); "Cd" -> Color(0xFFE91E63)
                                "nitrate" -> Color(0xFF4CAF50); "chemical_residue" -> Color(0xFFFF5722)
                                "microplastic" -> teal600; "dioxin" -> purple700
                                "toxic_chemicals" -> Color(0xFF673AB7)
                                "non_biodegradable" -> Color(0xFF795548)
                                "styrene" -> Color(0xFF2196F3)
                                else -> Color.Gray
                            }
                            SeriesData(getPollutantLabel(key), color, values)
                        }
                    MultiSeriesCard(
                        title    = s.keyPollutants,
                        subtitle = "All pollutants (3 most significant shown by default)",
                        icon     = Icons.Filled.Cloud,
                        seriesList = pollutantSeries,
                        xLabels = data.labels,
                        defaultActiveCount = pollutantSeries.size,
                    )
                }

                // ── Avg CO₂ insight box ───────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(blue50)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(s.insight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = blue700)
                        Text(
                            buildString {
                                append("Over this $selectedPeriod, avg CO₂ was ${avgCo2.fmt(2)}, ")
                                append("total waste reached ${totalWaste.fmt(1)} kg, ")
                                append("with peak on $peakLabel at ${peakKg.fmt(1)} kg.")
                            },
                            fontSize = 11.sp,
                            color = blue700.copy(alpha = 0.85f),
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }

    if (showHourDatePicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showHourDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        val p = parseDate("${pickerMillisToIsoDate(it)}T00:00:00")
                        hourDate = p
                    }
                    showHourDatePicker = false
                }) { Text(s.select) }
            },
            dismissButton = { TextButton(onClick = { showHourDatePicker = false }) { Text(s.cancel) } },
        ) { DatePicker(state = state) }
    }

    if (showDayStartPicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDayStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        dayStart = parseDate("${pickerMillisToIsoDate(it)}T00:00:00")
                    }
                    showDayStartPicker = false
                }) { Text(s.select) }
            },
            dismissButton = { TextButton(onClick = { showDayStartPicker = false }) { Text(s.cancel) } },
        ) { DatePicker(state = state) }
    }

    if (showDayEndPicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDayEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        dayEnd = parseDate("${pickerMillisToIsoDate(it)}T00:00:00")
                    }
                    showDayEndPicker = false
                }) { Text(s.select) }
            },
            dismissButton = { TextButton(onClick = { showDayEndPicker = false }) { Text(s.cancel) } },
        ) { DatePicker(state = state) }
    }
}
