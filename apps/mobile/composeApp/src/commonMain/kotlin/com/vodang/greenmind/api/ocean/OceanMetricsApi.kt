package com.vodang.greenmind.api.ocean

import com.vodang.greenmind.api.BASE_URL
import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.auth.ErrorResponse
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

// ── DTOs ─────────────────────────────────────────────────────────────────────

/**
 * Minimal envelope for ocean-metrics POST responses.
 *
 * The actual `data` payload differs per metric type. The metrics endpoints are
 * fire-and-forget from the client's perspective: success means the backend has
 * persisted the new metric record and updated the user's OCEAN scores; the
 * client should then refetch the ocean record via [getOcean].
 */
@Serializable
data class OceanMetricResult(
    val id: String? = null,
    val userId: String? = null,
    val type: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

/** Supported ocean-metrics endpoints (per backend OpenAPI spec). */
enum class OceanMetricType(val path: String) {
    AVG_DAILY_SPEND("avg_daily_spend"),
    SPEND_VARIABILITY("spend_variability"),
    DAILY_DISTANCE_KM("daily_distance_km"),
    LIST_ADHERENCE("list_adherence"),
}

// ── API calls ─────────────────────────────────────────────────────────────────

/** POST /ocean-metrics/{metric.path} */
suspend fun postOceanMetric(accessToken: String, metric: OceanMetricType): OceanMetricResult {
    AppLogger.i("Ocean", "postOceanMetric ${metric.path}")
    try {
        val resp = httpClient.post("$BASE_URL/ocean-metrics/${metric.path}") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Ocean", "postOceanMetric ${metric.path} failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Ocean", "postOceanMetric ${metric.path} error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
