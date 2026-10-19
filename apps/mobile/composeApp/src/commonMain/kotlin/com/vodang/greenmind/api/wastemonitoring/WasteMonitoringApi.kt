package com.vodang.greenmind.api.wastemonitoring

import com.vodang.greenmind.api.BASE_URL
import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.auth.ErrorResponse
import com.vodang.greenmind.api.wastereport.WasteReportPageResponse
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

// ── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class WasteMonitoringCollectorDto(
    val id: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val activeReports: Int,
)

@Serializable
data class WasteMonitoringCollectorListResponse(
    val data: List<WasteMonitoringCollectorDto>,
    val total: Int,
    val page: Int,
    val limit: Int,
)

@Serializable
data class WasteMonitoringReportDto(
    val id: String,
    val code: String,
    val status: String,
    val wardName: String,
    val lat: Double,
    val lng: Double,
    val wasteKg: Double,
    val wasteType: String,
    val description: String,
    val reportedByUserId: String,
    val reportedBy: String,
    val createdAt: String,
    val resolvedAt: String? = null,
)

@Serializable
data class WasteMonitoringCollectorInfo(
    val id: String,
    val fullName: String,
)

@Serializable
data class WasteMonitoringReportsByCollectorResponse(
    val collector: WasteMonitoringCollectorInfo,
    val data: List<WasteMonitoringReportDto>,
    val total: Int,
    val page: Int,
    val limit: Int,
)

@Serializable
data class AssignCollectorRequest(
    val collectorId: String,
)

@Serializable
data class AssignCollectorResponse(
    val id: String,
    val code: String,
    val status: String,
    val wardName: String,
    val reportedByUserId: String,
    val reportedBy: String,
    val assignedTo: String,
    val collectorId: String,
    val createdAt: String,
)

// ── API calls ─────────────────────────────────────────────────────────────────

/** GET /waste-monitoring — all waste reports (admin/monitoring) */
suspend fun getAllMonitoringReports(accessToken: String): WasteReportPageResponse {
    AppLogger.i("WasteMonitoring", "getAllMonitoringReports")
    try {
        val resp = httpClient.get("$BASE_URL/waste-monitoring") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("WasteMonitoring", "getAllMonitoringReports → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("WasteMonitoring", "getAllMonitoringReports failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("WasteMonitoring", "getAllMonitoringReports error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /waste-monitoring/collectors — list all collectors with active report counts */
suspend fun getAllCollectors(accessToken: String): WasteMonitoringCollectorListResponse {
    AppLogger.i("WasteMonitoring", "getAllCollectors")
    try {
        val resp = httpClient.get("$BASE_URL/waste-monitoring/collectors") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("WasteMonitoring", "getAllCollectors → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("WasteMonitoring", "getAllCollectors failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("WasteMonitoring", "getAllCollectors error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /waste-monitoring/collector/{collectorId} — reports assigned to a specific collector */
suspend fun getReportsByCollectorId(
    accessToken: String,
    collectorId: String,
): WasteMonitoringReportsByCollectorResponse {
    AppLogger.i("WasteMonitoring", "getReportsByCollectorId collectorId=$collectorId")
    try {
        val resp = httpClient.get("$BASE_URL/waste-monitoring/collector/$collectorId") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("WasteMonitoring", "getReportsByCollectorId → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("WasteMonitoring", "getReportsByCollectorId failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("WasteMonitoring", "getReportsByCollectorId error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** POST /waste-monitoring/{reportId}/assign — assign a collector to a report */
suspend fun assignCollector(
    accessToken: String,
    reportId: String,
    request: AssignCollectorRequest,
): AssignCollectorResponse {
    AppLogger.i("WasteMonitoring", "assignCollector reportId=$reportId collectorId=${request.collectorId}")
    try {
        val resp = httpClient.post("$BASE_URL/waste-monitoring/$reportId/assign") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        AppLogger.d("WasteMonitoring", "assignCollector → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("WasteMonitoring", "assignCollector failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("WasteMonitoring", "assignCollector error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
