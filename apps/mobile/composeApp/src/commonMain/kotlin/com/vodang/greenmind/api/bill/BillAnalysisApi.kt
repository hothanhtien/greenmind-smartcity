package com.vodang.greenmind.api.bill

import com.vodang.greenmind.api.ocr.OcrResponse
import com.vodang.greenmind.api.ocr.ecoScore
import com.vodang.greenmind.api.ocr.ocrBill
import com.vodang.greenmind.api.ocr.totalItems
import com.vodang.greenmind.api.wastedetect.WasteDetectImpact
import com.vodang.greenmind.api.wastedetect.WasteDetectItem
import com.vodang.greenmind.api.wastedetect.WasteDetectResponse

// ── App-facing data model ─────────────────────────────────────────────────────
// Used by BillStore and BillListScreen to persist and display scan history.

data class BillItem(val name: String, val amount: Double, val isGreen: Boolean)

data class BillAnalysisResult(
    val storeName: String,
    val totalAmount: Double,
    val greenAmount: Double,
    val greenRatio: Int,
    val items: List<BillItem>,
)

// ── Mapping ───────────────────────────────────────────────────────────────────

/**
 * Maps the raw OCR response to the simplified [BillAnalysisResult] used by the
 * rest of the app (BillStore, BillListScreen, green-ratio feedback).
 *
 * Green ratio = eco score derived from environmental impact (air/water/soil).
 */
fun OcrResponse.toBillAnalysisResult(): BillAnalysisResult {
    val grandTotal = totals?.grandTotal ?: 0.0
    val ratio      = ecoScore()
    val greenAmount = grandTotal * ratio / 100.0

    return BillAnalysisResult(
        storeName   = vendor?.name ?: "",
        totalAmount = grandTotal,
        greenAmount = greenAmount,
        greenRatio  = ratio,
        items = items?.map { item ->
            BillItem(
                name    = item.rawName ?: "Unknown",
                amount  = item.lineTotal ?: 0.0,
                isGreen = false,
            )
        } ?: emptyList(),
    )
}

// ── Public API ────────────────────────────────────────────────────────────────

/**
 * Convenience wrapper used by callers that only need [BillAnalysisResult]
 * and don't need the full [OcrResponse] detail (e.g. iOS stub, tests).
 *
 * For the Android scan screen use [ocrBill] directly so the rich fields
 * (datetime, totals breakdown, per-item quantity/price) are available.
 */
suspend fun analyzeBill(accessToken: String, imageBytes: ByteArray): BillAnalysisResult =
    ocrBill(accessToken, imageBytes, "bill_${imageBytes.size}.jpg").toBillAnalysisResult()

/**
 * Adapter so [com.vodang.greenmind.home.components.EnvImpactCard] (which expects a
 * [WasteDetectResponse]) can render the pollution + impact returned by `/ocr`.
 */
fun OcrResponse.toWasteDetectResponse() = WasteDetectResponse(
    items        = items.orEmpty().map {
        WasteDetectItem(
            name      = it.rawName ?: "",
            quantity  = (it.quantity ?: 0.0).toInt(),
            area      = 0,
            className = "",
        )
    },
    totalObjects = totalItems(),
    imageUrl     = "",
    pollution    = pollution?.toMap().orEmpty(),
    impact       = WasteDetectImpact(
        airPollution   = impact?.air ?: 0.0,
        waterPollution = impact?.water ?: 0.0,
        soilPollution  = impact?.soil ?: 0.0,
    ),
)
