package com.vodang.greenmind.api.wastereport

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
 * Shared DTO for all waste report responses.
 * Fields that differ between create vs get (assignedCollectorId vs assignedTo,
 * presence of imageKey/imageUrl) are all nullable — the JSON config uses
 * ignoreUnknownKeys = true so extra fields are safely discarded.
 */
@Serializable
data class WasteReportDto(
    val id: String = "",
    val code: String = "",
    val wardName: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val description: String = "",
    val status: String = "",
    val createdAt: String = "",
    val imageUrl: String? = null,
    val reportedBy: String? = null,
    val reportedByUserId: String? = null,
    val imageEvidenceUrl: String? = null,
    val resolvedAt: String? = null,
    val campaignId: String? = null,
    val segmentedImageUrl: String? = null,
    val depthImageUrl: String? = null,
    val heatmapUrl: String? = null,
    val segmentRatio: Double? = null,
    val pollutionScore: Double? = null,
    val pollutionLevel: String? = null,
)

/** Paginated list wrapper used by GET /waste-reports/ and GET /waste-reports/my */
@Serializable
data class WasteReportPageResponse(
    val data: List<WasteReportDto>,
    val total: Int,
    val page: Int,
    val limit: Int,
)

/** Wrapper for POST /waste-reports response: { message, data } */
@Serializable
data class CreateWasteReportResponseWrapper(
    val message: String,
    val data: WasteReportDto,
)

// ── Requests ─────────────────────────────────────────────────────────────────

@Serializable
data class CreateWasteReportRequest(
    val wardName: String,
    val lat: Double,
    val lng: Double,
    val description: String,
    val imageUrl: String,
)

@Serializable
data class UpdateWasteReportRequest(
    val description: String,
    val imageUrl: String,
)

// ── API calls ─────────────────────────────────────────────────────────────────

/** POST /waste-reports — create a new report (response wraps DTO in {message, data}) */
suspend fun createWasteReport(
    accessToken: String,
    request: CreateWasteReportRequest,
): WasteReportDto {
    AppLogger.i("WasteReport", "createWasteReport ward=${request.wardName} imageUrl=${request.imageUrl}")
    try {
        val resp = httpClient.post("$BASE_URL/waste-reports") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        AppLogger.d("WasteReport", "createWasteReport → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            val raw = resp.bodyAsText()
            AppLogger.d("WasteReport", "createWasteReport raw body (${raw.length} chars): ${raw.take(800)}")
            try {
                val wrapper = resp.body<CreateWasteReportResponseWrapper>()
                wrapper.data
            } catch (e: Throwable) {
                AppLogger.e("WasteReport", "createWasteReport parse error on: $raw")
                throw e
            }
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("WasteReport", "createWasteReport failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("WasteReport", "createWasteReport error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /waste-monitoring — all reports (public/monitoring) */
suspend fun getAllWasteReports(accessToken: String): WasteReportPageResponse {
    AppLogger.i("WasteReport", "getAllWasteReports")
    try {
        val resp = httpClient.get("$BASE_URL/waste-monitoring") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("WasteReport", "getAllWasteReports → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            val raw = resp.bodyAsText()
            AppLogger.d("WasteReport", "getAllWasteReports raw body (${raw.length} chars): ${raw.take(500)}")
            try {
                resp.body()
            } catch (e: Throwable) {
                AppLogger.e("WasteReport", "getAllWasteReports parse error on: $raw")
                throw e
            }
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("WasteReport", "getAllWasteReports failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("WasteReport", "getAllWasteReports error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /waste-reports/my — current user's reports */
suspend fun getMyWasteReports(accessToken: String): WasteReportPageResponse {
    AppLogger.i("WasteReport", "getMyWasteReports")
    try {
        val resp = httpClient.get("$BASE_URL/waste-reports/my") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("WasteReport", "getMyWasteReports → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            val raw = resp.bodyAsText()
            AppLogger.d("WasteReport", "getMyWasteReports raw body (${raw.length} chars): ${raw.take(500)}")
            try {
                resp.body()
            } catch (e: Throwable) {
                AppLogger.e("WasteReport", "getMyWasteReports parse error on: $raw")
                throw e
            }
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("WasteReport", "getMyWasteReports failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("WasteReport", "getMyWasteReports error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /waste-reports/{id} */
suspend fun getWasteReportById(accessToken: String, id: String): WasteReportDto {
    AppLogger.i("WasteReport", "getWasteReportById id=$id")
    try {
        val resp = httpClient.get("$BASE_URL/waste-reports/$id") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("WasteReport", "getWasteReportById → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            val raw = resp.bodyAsText()
            AppLogger.d("WasteReport", "getWasteReportById raw body (${raw.length} chars): ${raw.take(500)}")
            try {
                resp.body()
            } catch (e: Throwable) {
                AppLogger.e("WasteReport", "getWasteReportById parse error on: $raw")
                throw e
            }
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("WasteReport", "getWasteReportById failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("WasteReport", "getWasteReportById error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** PATCH /waste-reports/{id} */
suspend fun updateWasteReport(
    accessToken: String,
    id: String,
    request: UpdateWasteReportRequest,
): WasteReportDto {
    AppLogger.i("WasteReport", "updateWasteReport id=$id")
    try {
        val resp = httpClient.patch("$BASE_URL/waste-reports/$id") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        AppLogger.d("WasteReport", "updateWasteReport → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            val raw = resp.bodyAsText()
            AppLogger.d("WasteReport", "updateWasteReport raw body (${raw.length} chars): ${raw.take(500)}")
            try {
                resp.body()
            } catch (e: Throwable) {
                AppLogger.e("WasteReport", "updateWasteReport parse error on: $raw")
                throw e
            }
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("WasteReport", "updateWasteReport failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("WasteReport", "updateWasteReport error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** DELETE /waste-reports/{id} */
suspend fun deleteWasteReport(accessToken: String, id: String): WasteReportDto {
    AppLogger.i("WasteReport", "deleteWasteReport id=$id")
    try {
        val resp = httpClient.delete("$BASE_URL/waste-reports/$id") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("WasteReport", "deleteWasteReport → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            val raw = resp.bodyAsText()
            AppLogger.d("WasteReport", "deleteWasteReport raw body (${raw.length} chars): ${raw.take(500)}")
            try {
                resp.body()
            } catch (e: Throwable) {
                AppLogger.e("WasteReport", "deleteWasteReport parse error on: $raw")
                throw e
            }
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("WasteReport", "deleteWasteReport failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("WasteReport", "deleteWasteReport error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
