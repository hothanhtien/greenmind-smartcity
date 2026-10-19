package com.vodang.greenmind.store

import com.vodang.greenmind.api.households.DetectTrashHistoryDto
import com.vodang.greenmind.api.households.GreenScoreEntryDto
import com.vodang.greenmind.api.households.getDetectsMonthly
import com.vodang.greenmind.api.wastedetect.WasteDetectImpact
import com.vodang.greenmind.api.wastedetect.WasteDetectItem
import com.vodang.greenmind.api.wastedetect.WasteDetectResponse
import com.vodang.greenmind.wastesort.WasteSortEntry
import com.vodang.greenmind.wastesort.WasteSortStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.vodang.greenmind.time.todayLocalIsoDate

object WasteSortStore {
    val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _entries = MutableStateFlow<List<WasteSortEntry>>(emptyList())
    val entries: StateFlow<List<WasteSortEntry>> = _entries.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger.asStateFlow()

    private val _greenScoreTrigger = MutableStateFlow(0)
    val greenScoreTrigger: StateFlow<Int> = _greenScoreTrigger.asStateFlow()

    fun notifyRefresh() {
        _refreshTrigger.value += 1
    }

    private val _userHistory = MutableStateFlow<List<DetectTrashHistoryDto>>(emptyList())
    val userHistory: StateFlow<List<DetectTrashHistoryDto>> = _userHistory.asStateFlow()

    private val _todayTotalKg = MutableStateFlow(0.0)
    val todayTotalKg: StateFlow<Double> = _todayTotalKg.asStateFlow()

    private fun DetectTrashHistoryDto.toEntry(fallbackDate: String): WasteSortEntry {
        val pollutionMap = pollution?.let { p ->
            buildMap<String, Double> {
                p.cd?.let               { put("Cd", it) }
                p.hg?.let               { put("Hg", it) }
                p.pb?.let               { put("Pb", it) }
                p.ch4?.let              { put("CH4", it) }
                p.co2?.let              { put("CO2", it) }
                p.nox?.let              { put("NOx", it) }
                p.so2?.let              { put("SO2", it) }
                p.pm25?.let             { put("PM2.5", it) }
                p.dioxin?.let           { put("dioxin", it) }
                p.nitrate?.let          { put("nitrate", it) }
                p.styrene?.let          { put("styrene", it) }
                p.microplastic?.let     { put("microplastic", it) }
                p.toxicChemicals?.let   { put("toxic_chemicals", it) }
                p.chemicalResidue?.let  { put("chemical_residue", it) }
                p.nonBiodegradable?.let { put("non_biodegradable", it) }
            }
        }
        val pollutantResult = if (pollution != null && impact != null) {
            WasteDetectResponse(
                items = items?.map { WasteDetectItem(it.name, it.quantity, it.area) } ?: emptyList(),
                totalObjects = totalObjects ?: 0,
                imageUrl = aiAnalysis ?: imageUrl,
                pollution = pollutionMap ?: emptyMap(),
                impact = WasteDetectImpact(
                    airPollution   = impact.airPollution ?: 0.0,
                    waterPollution = impact.waterPollution ?: 0.0,
                    soilPollution  = impact.soilPollution ?: 0.0,
                ),
            )
        } else null
        return WasteSortEntry(
            id = id,
            backendId = id,
            imageUrl = aiAnalysis ?: imageUrl,
            totalObjects = totalObjects ?: 0,
            grouped = segments?.let { seg ->
                buildMap {
                    if (seg.recyclable.isNotEmpty()) put("recyclable", seg.recyclable)
                    if (seg.residual.isNotEmpty()) put("residual", seg.residual)
                }
            } ?: emptyMap(),
            createdAt = createdAt?.take(10) ?: fallbackDate,
            scannedBy = detectedBy?.fullName ?: "Me",
            status = when (status?.lowercase()) {
                "detected", "sorted"        -> WasteSortStatus.SORTED
                "brought_out", "broughtout" -> WasteSortStatus.BRINGOUTED
                "picked_up", "pickedup",
                "collected"                  -> WasteSortStatus.COLLECTED
                else                         -> WasteSortStatus.SORTED
            },
            pollutantResult = pollutantResult,
            greenScoreResult = greenScore,
            totalMassKg = totalMassKg,
            detectType = detectType,
        )
    }

    fun loadFromApi() {
        val token = SettingsStore.getAccessToken() ?: return
        storeScope.launch {
            try {
                val history = getDetectsMonthly(token).data
                val today = todayLocalIsoDate()
                _entries.value = history.map { it.toEntry(today) }
                _todayTotalKg.value = _entries.value
                    .filter { it.createdAt.startsWith(today) }
                    .sumOf { it.totalMassKg ?: 0.0 }
            } catch (e: Throwable) {
                // Silent fail
            }
        }
    }

    fun fetchUserScans(token: String) {
        storeScope.launch {
            try {
                val response = getDetectsMonthly(token)
                _userHistory.value = response.data
                val today = todayLocalIsoDate()
                _entries.value = response.data.map { it.toEntry(today) }
                _todayTotalKg.value = _entries.value
                    .filter { it.createdAt.startsWith(today) }
                    .sumOf { it.totalMassKg ?: 0.0 }
            } catch (e: Throwable) {
                // Silent fail
            }
        }
    }

    fun triggerRefresh() {
        _refreshTrigger.value += 1
        loadFromApi()
    }

    fun add(entry: WasteSortEntry) {
        _entries.value = listOf(entry) + _entries.value
        val today = todayLocalIsoDate()
        _todayTotalKg.value = _entries.value
            .filter { it.createdAt.startsWith(today) }
            .sumOf { it.totalMassKg ?: 0.0 }
    }

    fun updateStatus(id: String, status: WasteSortStatus) {
        _entries.value = _entries.value.map { entry ->
            if (entry.id == id || entry.backendId == id) entry.copy(status = status) else entry
        }
    }

    fun updatePollutant(id: String, pollutant: WasteDetectResponse) {
        _entries.value = _entries.value.map { entry ->
            if (entry.id == id) entry.copy(pollutantResult = pollutant) else entry
        }
    }

    fun updateGreenScore(id: String, greenScore: GreenScoreEntryDto) {
        _entries.value = _entries.value.map { entry ->
            if (entry.id == id) entry.copy(greenScoreResult = greenScore) else entry
        }
        _greenScoreTrigger.value += 1
        _refreshTrigger.value += 1
    }
}
