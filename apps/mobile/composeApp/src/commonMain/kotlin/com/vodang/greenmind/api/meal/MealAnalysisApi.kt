package com.vodang.greenmind.api.meal

import com.vodang.greenmind.api.AI_SERVICE_URL
import com.vodang.greenmind.api.BASE_URL
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
data class PlantAnalysisResponse(
    @SerialName("id")                       val id: String,
    @SerialName("vegetable_area")           val vegetableArea: Int,
    @SerialName("dish_area")               val dishArea: Double,
    @SerialName("vegetable_ratio_percent") val vegetableRatioPercent: Double,
    @SerialName("plant_image_url")         val plantImageUrl: String,
    @SerialName("created_at")              val createdAt: String,
)

// ── App-facing data model ─────────────────────────────────────────────────────

data class MealAnalysisResult(
    val plantRatio: Int,
    val description: String,
    val plantImageBase64: String? = null,
    val dishImageBase64: String? = null,
)

// ── Dedicated HTTP client ─────────────────────────────────────────────────────
// AI inference can be slow — use a longer timeout than the default client.

private val mealAiHttpClient = buildHttpClient(
    tag              = "MealAI",
    requestTimeoutMs = 120_000,
    connectTimeoutMs = 30_000,
    socketTimeoutMs  = 120_000,
)

@Serializable
private data class AnalyzeMealRequest(
    @SerialName("imageUrl") val imageUrl: String,
)

private val API_BASE_URL = BASE_URL

/**
 * POST /plant-analysis/analyze — sends an image URL and returns plant ratio analysis.
 *
 * @param imageUrl    URL of the uploaded meal image.
 * @param accessToken JWT token for authenticated endpoint.
 */
suspend fun analyzeMealByUrl(
    imageUrl: String,
    accessToken: String,
): MealAnalysisResult {
    AppLogger.i("MealAI", "analyzeMealByUrl imageUrl=$imageUrl")
    try {
        val response = mealAiHttpClient.post("$API_BASE_URL/plant-analysis/analyze") {
            setBody(AnalyzeMealRequest(imageUrl))
        }
        if (!response.status.isSuccess()) {
            val msg = try { response.body<ErrorResponse>().message }
                      catch (_: Throwable) { response.bodyAsText() }
            AppLogger.e("MealAI", "analyzeMealByUrl failed: ${response.status.value} $msg")
            throw ApiException(response.status.value, msg)
        }
        val dto = response.body<PlantAnalysisResponse>()
        AppLogger.i("MealAI", "analyzeMealByUrl success ratio=${dto.vegetableRatioPercent}")
        return MealAnalysisResult(
            plantRatio       = dto.vegetableRatioPercent.toInt().coerceIn(0, 100),
            description      = "",
            plantImageBase64 = dto.plantImageUrl,
            dishImageBase64  = null,
        )
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("MealAI", "analyzeMealByUrl error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
