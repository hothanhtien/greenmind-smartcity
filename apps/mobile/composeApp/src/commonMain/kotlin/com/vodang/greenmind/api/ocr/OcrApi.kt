package com.vodang.greenmind.api.ocr

import com.vodang.greenmind.api.BASE_URL
import com.vodang.greenmind.api.buildHttpClient
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.auth.ErrorResponse
import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── DTOs ─────────────────────────────────────────────────────────────────────
// Shape matches the `POST /ocr` response in GreenMind BE OpenAPI spec.
// All fields are nullable / defaulted — the OCR model may omit any field it cannot extract.

@Serializable
data class OcrResponse(
    val doc: OcrDoc? = null,
    val vendor: OcrVendor? = null,
    val datetime: OcrDatetime? = null,
    val items: List<OcrItem>? = null,
    val totals: OcrTotals? = null,
    val pollution: OcrPollution? = null,
    val impact: OcrImpact? = null,
)

@Serializable
data class OcrDoc(
    @SerialName("source_id") val sourceId: String? = null,
    val currency: String? = null,
)

@Serializable
data class OcrVendor(
    val name: String? = null,
)

@Serializable
data class OcrDatetime(
    val date: String? = null,
    val time: String? = null,
)

@Serializable
data class OcrItem(
    @SerialName("raw_name")   val rawName: String? = null,
    val quantity: Double? = null,
    @SerialName("unit_price") val unitPrice: Double? = null,
    @SerialName("line_total") val lineTotal: Double? = null,
)

@Serializable
data class OcrTotals(
    @SerialName("grand_total") val grandTotal: Double? = null,
)

@Serializable
data class OcrImpact(
    val air: Double = 0.0,
    val water: Double = 0.0,
    val soil: Double = 0.0,
)

@Serializable
data class OcrPollution(
    @SerialName("CO2")               val co2: Double = 0.0,
    @SerialName("CH4")               val ch4: Double = 0.0,
    val microplastic: Double = 0.0,
    val dioxin: Double = 0.0,
    @SerialName("non_biodegradable") val nonBiodegradable: Double = 0.0,
    @SerialName("PM2.5")             val pm25: Double = 0.0,
    @SerialName("NOx")               val nox: Double = 0.0,
    @SerialName("SO2")               val so2: Double = 0.0,
    @SerialName("Pb")                val pb: Double = 0.0,
    @SerialName("Hg")                val hg: Double = 0.0,
    @SerialName("Cd")                val cd: Double = 0.0,
    val nitrate: Double = 0.0,
    @SerialName("chemical_residue")  val chemicalResidue: Double = 0.0,
    @SerialName("toxic_chemicals")   val toxicChemicals: Double = 0.0,
    val styrene: Double = 0.0,
) {
    fun toMap(): Map<String, Double> = mapOf(
        "CO2" to co2,
        "CH4" to ch4,
        "microplastic" to microplastic,
        "dioxin" to dioxin,
        "non_biodegradable" to nonBiodegradable,
        "PM2.5" to pm25,
        "NOx" to nox,
        "SO2" to so2,
        "Pb" to pb,
        "Hg" to hg,
        "Cd" to cd,
        "nitrate" to nitrate,
        "chemical_residue" to chemicalResidue,
        "toxic_chemicals" to toxicChemicals,
        "styrene" to styrene,
    )
}

// ── Derived helpers on OcrResponse ────────────────────────────────────────────

fun OcrResponse.ecoScore(): Int {
    val imp = impact ?: return 0
    val avg = (imp.air + imp.water + imp.soil) / 3.0
    return ((1.0 - avg) * 100).toInt().coerceIn(0, 100)
}

fun OcrResponse.totalItems(): Int = items?.sumOf { (it.quantity ?: 0.0).toInt() } ?: 0

fun OcrResponse.activePollutants(): List<Pair<String, Double>> =
    pollution?.toMap().orEmpty().entries
        .filter { it.value > 0.0 }
        .sortedByDescending { it.value }
        .map { it.key to it.value }

// ── Invoice list DTOs ─────────────────────────────────────────────────────────
// GET /ocr/invoices returns totals as strings ("8.0") and pollution/impact as
// loose maps — keep this DTO independent from the strict `/ocr` response shape.

@Serializable
data class InvoiceTotals(
    @SerialName("grand_total") val grandTotal: String? = null,
) {
    fun grandTotalDouble(): Double = grandTotal?.toDoubleOrNull() ?: 0.0
}

@Serializable
data class InvoiceDto(
    val id: String,
    val doc: OcrDoc? = null,
    val vendor: OcrVendor? = null,
    val datetime: OcrDatetime? = null,
    val items: List<OcrItem>? = null,
    val totals: InvoiceTotals? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val pollution: Map<String, Double>? = null,
    val impact: Map<String, Double>? = null,
) {
    /** Eco score derived from environmental impact (0..100). */
    fun ecoScore(): Int {
        val imp = impact ?: return 0
        val air = imp["air"] ?: 0.0
        val water = imp["water"] ?: 0.0
        val soil = imp["soil"] ?: 0.0
        val avg = (air + water + soil) / 3.0
        return ((1.0 - avg) * 100).toInt().coerceIn(0, 100)
    }
}

// ── Dedicated HTTP client ─────────────────────────────────────────────────────
// OCR inference is slow — use a much longer timeout than the default client.

private val ocrHttpClient = buildHttpClient(
    tag              = "OCR",
    requestTimeoutMs = 120_000,
    connectTimeoutMs = 30_000,
    socketTimeoutMs  = 120_000,
)

// ── API call ──────────────────────────────────────────────────────────────────

/**
 * POST /ocr — uploads a bill image and returns the structured OCR result
 * including vendor, items, totals, pollution and environmental impact.
 *
 * @param accessToken  Bearer token for the backend.
 * @param imageBytes   Raw bytes of the captured image (JPEG).
 * @param filename     File name sent in the multipart form (default: "bill.jpg").
 */
suspend fun ocrBill(
    accessToken: String,
    imageBytes: ByteArray,
    filename: String = "bill.jpg",
): OcrResponse {
    AppLogger.i("OCR", "ocrBill filename=$filename bytes=${imageBytes.size}")
    try {
        val response = ocrHttpClient.post("$BASE_URL/ocr") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(MultiPartFormDataContent(formData {
                append("file", imageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                })
            }))
        }
        if (!response.status.isSuccess()) {
            val msg = try { response.body<ErrorResponse>().message }
                      catch (_: Throwable) { response.bodyAsText() }
            AppLogger.e("OCR", "ocrBill failed: ${response.status.value} $msg")
            throw ApiException(response.status.value, msg)
        }
        return response.body<OcrResponse>().also {
            AppLogger.i("OCR", "ocrBill success vendor=${it.vendor?.name} items=${it.items?.size} eco=${it.ecoScore()}")
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("OCR", "ocrBill error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/**
 * GET /ocr/invoices — fetches the user's full bill-scan history from the server.
 * Uses the standard [httpClient] (normal timeout; this is just a JSON list fetch).
 */
suspend fun getInvoices(accessToken: String): List<InvoiceDto> {
    AppLogger.i("OCR", "getInvoices")
    try {
        val response = httpClient.get("$BASE_URL/ocr/invoices") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        if (!response.status.isSuccess()) {
            val msg = try { response.body<ErrorResponse>().message }
                      catch (_: Throwable) { response.bodyAsText() }
            AppLogger.e("OCR", "getInvoices failed: ${response.status.value} $msg")
            throw ApiException(response.status.value, msg)
        }
        return response.body<List<InvoiceDto>>().also {
            AppLogger.i("OCR", "getInvoices loaded ${it.size} invoices")
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("OCR", "getInvoices error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
