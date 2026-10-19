package com.vodang.greenmind.api.wastedetect

import com.vodang.greenmind.api.AI_SERVICE_URL
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.buildHttpClient
import com.vodang.greenmind.api.auth.ErrorResponse
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val AI_BASE_URL = AI_SERVICE_URL

// ── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class WasteDetectItem(
    val name: String,
    val quantity: Int,
    val area: Int,
    val className: String = "",
)

@Serializable
data class WasteDetectImpact(
    @SerialName("air_pollution")   val airPollution: Double,
    @SerialName("water_pollution") val waterPollution: Double,
    @SerialName("soil_pollution")  val soilPollution: Double,
)

@Serializable
data class WasteDetectResponse(
    val items: List<WasteDetectItem>,
    @SerialName("total_objects") val totalObjects: Int,
    @SerialName("image_url")     val imageUrl: String,
    val pollution: Map<String, Double>,
    val impact: WasteDetectImpact,
) {
    /** Active pollutants — keys with value > 0, sorted descending. */
    val activePollutants: List<Pair<String, Double>>
        get() = pollution.entries
            .filter { it.value > 0.0 }
            .sortedByDescending { it.value }
            .map { it.key to it.value }

    /** Eco score 0-100: 100 = zero impact, 0 = maximum impact. */
    val ecoScore: Int
        get() {
            val avg = (impact.airPollution + impact.waterPollution + impact.soilPollution) / 3.0
            return ((1.0 - avg) * 100).toInt().coerceIn(0, 100)
        }

    /** Total number of individual items (sum of quantities). */
    val totalItems: Int
        get() = items.sumOf { it.quantity }
}

// ── HTTP client ───────────────────────────────────────────────────────────────

private val wasteDetectClient = buildHttpClient(
    tag              = "WasteDetect",
    requestTimeoutMs = 300_000L,
    connectTimeoutMs = 30_000L,
    socketTimeoutMs  = 300_000L,
)

// ── API call ──────────────────────────────────────────────────────────────────

/**
 * POST /predict-pollutant-impact — detects waste items and returns their
 * pollution contributions and environmental impact scores.
 *
 * @param imageBytes  Raw bytes of the captured image (JPEG).
 * @param filename    File name sent in the multipart form (default: "waste.jpg").
 */
suspend fun detectWaste(
    imageBytes: ByteArray,
    filename: String = "waste.jpg",
): WasteDetectResponse {
    AppLogger.i("WasteDetect", "detectWaste filename=$filename bytes=${imageBytes.size}")
    try {
        val response = wasteDetectClient.post("$AI_BASE_URL/predict-pollutant-impact") {
            timeout {
                requestTimeoutMillis = 300_000L
                socketTimeoutMillis = 300_000L
            }
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
            AppLogger.e("WasteDetect", "detectWaste failed: ${response.status.value} $msg")
            throw ApiException(response.status.value, msg)
        }
        return response.body<WasteDetectResponse>().also {
            AppLogger.i("WasteDetect", "detectWaste success items=${it.items.size} eco=${it.ecoScore}")
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("WasteDetect", "detectWaste error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
