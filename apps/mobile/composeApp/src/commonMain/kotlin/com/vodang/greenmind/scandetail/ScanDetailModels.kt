package com.vodang.greenmind.scandetail

import com.vodang.greenmind.api.households.DetectImpactDto
import com.vodang.greenmind.api.households.DetectItemDto
import com.vodang.greenmind.api.households.DetectItemMassDto
import com.vodang.greenmind.api.households.DetectPollutionDto
import com.vodang.greenmind.api.households.DetectTrashHistoryDto
import com.vodang.greenmind.api.households.GreenScoreEntryDto
import com.vodang.greenmind.api.wastedetect.WasteDetectItem
import com.vodang.greenmind.householdwaste.groupStatus
import com.vodang.greenmind.householdwaste.parseWasteSortStatus
import com.vodang.greenmind.wastesort.WasteSortEntry
import com.vodang.greenmind.wastesort.WasteSortStatus

// ── Unified data model ─────────────────────────────────────────────────────────

data class ScanDetailData(
    val id: String,
    val imageUrl: String,
    val createdAt: String,
    val scannedBy: String,
    val status: WasteSortStatus,
    val totalObjects: Int? = null,
    val annotatedImageUrl: String? = null,
    val aiAnalysisUrl: String? = null,
    val depthMapUrl: String? = null,
    // legacy detect-trash fields (history flow only)
    val items: List<ScanItem>? = null,
    val pollution: Map<String, Double>? = null,
    val impact: ScanImpact? = null,
    val totalMassKg: Double? = null,
    val itemsMass: List<ScanItemMass>? = null,
    // green score
    val greenScore: GreenScoreEntryDto? = null,
    val isGreenScoreLoading: Boolean = false,
    // segments grouped by category
    val grouped: Map<String, List<String>> = emptyMap(),
    // backend id for API calls
    val backendId: String? = null,
    val detectType: String? = null,
)

data class ScanImpact(
    val air: Double,
    val water: Double,
    val soil: Double,
)

data class ScanItem(
    val name: String,
    val quantity: Int,
    val massKg: Double? = null,
)

data class ScanItemMass(
    val name: String,
    val massKg: Double,
)

enum class DisplayMode { FULL_SCREEN, BOTTOM_SHEET }

// ── Adapters ──────────────────────────────────────────────────────────────────

fun WasteDetectItem.toScanItem() = ScanItem(name = name, quantity = quantity, massKg = null)

fun DetectItemDto.toScanItem() = ScanItem(name = name, quantity = quantity, massKg = massKg)

fun DetectItemMassDto.toScanItemMass() = ScanItemMass(name = name, massKg = massKg)

fun DetectPollutionDto.toFlatMap(): Map<String, Double> = buildMap {
    cd?.let               { put("Cd", it) }
    hg?.let               { put("Hg", it) }
    pb?.let               { put("Pb", it) }
    ch4?.let              { put("CH4", it) }
    co2?.let              { put("CO2", it) }
    nox?.let              { put("NOx", it) }
    so2?.let              { put("SO2", it) }
    pm25?.let             { put("PM2.5", it) }
    dioxin?.let           { put("dioxin", it) }
    nitrate?.let          { put("nitrate", it) }
    styrene?.let          { put("styrene", it) }
    microplastic?.let     { put("microplastic", it) }
    toxicChemicals?.let   { put("toxic_chemicals", it) }
    chemicalResidue?.let  { put("chemical_residue", it) }
    nonBiodegradable?.let { put("non_biodegradable", it) }
}.filterValues { it > 0.0 }

fun DetectImpactDto.toScanImpact() = ScanImpact(
    air = airPollution ?: 0.0,
    water = waterPollution ?: 0.0,
    soil = soilPollution ?: 0.0,
)

/** Local-scan entry → ScanDetailData. Pulls legacy fields off pollutantResult when present. */
fun WasteSortEntry.toScanDetailData(): ScanDetailData {
    val pr = pollutantResult
    return ScanDetailData(
        id = id,
        imageUrl = imageUrl,
        createdAt = createdAt,
        scannedBy = scannedBy,
        status = status,
        totalObjects = totalObjects,
        annotatedImageUrl = null,
        items = pr?.items?.map { it.toScanItem() },
        pollution = pr?.pollution,
        impact = pr?.impact?.let { ScanImpact(it.airPollution, it.waterPollution, it.soilPollution) },
        totalMassKg = totalMassKg,
        greenScore = greenScoreResult,
        isGreenScoreLoading = backendId != null && greenScoreResult == null,
        grouped = grouped,
        backendId = backendId,
        detectType = detectType,
    )
}

/** Single history record → ScanDetailData (full fields). */
fun DetectTrashHistoryDto.toScanDetailData(): ScanDetailData {
    return ScanDetailData(
        id = id,
        imageUrl = imageUrl,
        createdAt = createdAt?.take(10) ?: "",
        scannedBy = detectedBy?.fullName ?: detectedBy?.username ?: "",
        status = parseWasteSortStatus(status),
        totalObjects = totalObjects,
        annotatedImageUrl = null,
        aiAnalysisUrl = aiAnalysis,
        depthMapUrl = depthMapUrl,
        items = items?.map { it.toScanItem() },
        pollution = pollution?.toFlatMap(),
        impact = impact?.toScanImpact(),
        totalMassKg = totalMassKg,
        itemsMass = itemsMass?.map { it.toScanItemMass() },
        grouped = segments?.let { seg ->
            buildMap {
                if (seg.recyclable.isNotEmpty()) put("recyclable", seg.recyclable)
                if (seg.residual.isNotEmpty()) put("residual", seg.residual)
            }
        } ?: emptyMap(),
        backendId = id,
        detectType = detectType,
        greenScore = greenScore,
        isGreenScoreLoading = false,
    )
}

/** Grouped (multi-type) history records → ScanDetailData. */
fun List<DetectTrashHistoryDto>.toScanDetailData(): ScanDetailData {
    val primary = minByOrNull { it.createdAt ?: "" } ?: first()

    val analyzeAll = find { it.detectType == "analyze_all" }
    if (analyzeAll != null) {
        return ScanDetailData(
            id = analyzeAll.id,
            imageUrl = analyzeAll.imageUrl,
            createdAt = analyzeAll.createdAt?.take(10) ?: "",
            scannedBy = analyzeAll.detectedBy?.fullName ?: analyzeAll.detectedBy?.username ?: "",
            status = parseWasteSortStatus(analyzeAll.status),
            totalObjects = analyzeAll.totalObjects ?: 0,
            annotatedImageUrl = null,
            aiAnalysisUrl = analyzeAll.aiAnalysis,
            depthMapUrl = analyzeAll.depthMapUrl,
            items = analyzeAll.items?.map { it.toScanItem() },
            pollution = analyzeAll.pollution?.toFlatMap(),
            impact = analyzeAll.impact?.toScanImpact(),
            totalMassKg = analyzeAll.totalMassKg,
            itemsMass = analyzeAll.itemsMass?.map { it.toScanItemMass() },
            grouped = analyzeAll.segments?.let { seg ->
                buildMap {
                    if (seg.recyclable.isNotEmpty()) put("recyclable", seg.recyclable)
                    if (seg.residual.isNotEmpty()) put("residual", seg.residual)
                }
            } ?: emptyMap(),
            backendId = analyzeAll.id,
            detectType = analyzeAll.detectType,
            greenScore = analyzeAll.greenScore,
            isGreenScoreLoading = false,
        )
    }

    val detectTrash = find { it.detectType == "detect_trash" }
    val pollutant = find { it.detectType == "predict_pollutant_impact" }
    val totalMass = find { it.detectType == "total_mass" }

    val primarySegments = firstNotNullOfOrNull { it.segments }
    return ScanDetailData(
        id = primary.id,
        imageUrl = primary.imageUrl,
        createdAt = primary.createdAt?.take(10) ?: "",
        scannedBy = primary.detectedBy?.fullName ?: primary.detectedBy?.username ?: "",
        status = groupStatus(this),
        totalObjects = detectTrash?.totalObjects ?: primary.totalObjects,
        annotatedImageUrl = null,
        aiAnalysisUrl = detectTrash?.aiAnalysis,
        depthMapUrl = totalMass?.depthMapUrl,
        items = detectTrash?.items?.map { it.toScanItem() },
        pollution = pollutant?.pollution?.toFlatMap(),
        impact = pollutant?.impact?.toScanImpact(),
        totalMassKg = totalMass?.totalMassKg,
        itemsMass = totalMass?.itemsMass?.map { it.toScanItemMass() },
        grouped = primarySegments?.let { seg ->
            buildMap {
                if (seg.recyclable.isNotEmpty()) put("recyclable", seg.recyclable)
                if (seg.residual.isNotEmpty()) put("residual", seg.residual)
            }
        } ?: emptyMap(),
        backendId = primary.id,
        detectType = firstNotNullOfOrNull { it.detectType },
        greenScore = firstNotNullOfOrNull { it.greenScore },
        isGreenScoreLoading = false,
    )
}
