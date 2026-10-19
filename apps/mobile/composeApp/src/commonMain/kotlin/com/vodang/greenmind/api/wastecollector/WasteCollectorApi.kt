package com.vodang.greenmind.api.wastecollector

import com.vodang.greenmind.api.BASE_URL
import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.auth.ErrorResponse
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.awaitAll
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

// ── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class WasteCollectorReportDto(
    val id: String,
    val code: String,
    val reportedBy: String,
    val lat: Double,
    val lng: Double,
    val wasteKg: Double,
    val wasteType: String,
    val description: String,
    val status: String,
    val createdAt: String,
    val resolvedAt: String? = null,
)

@Serializable
data class WasteCollectorListResponse(
    val data: List<WasteCollectorReportDto>,
    val total: Int,
)

@Serializable
data class WasteCollectorReportDetailDto(
    val id: String,
    val code: String,
    val description: String,
    val imageKey: String,
    val imageUrl: String,
    val lat: Double,
    val lng: Double,
    val wasteKg: Int,
    val wasteType: String,
    val wardName: String,
    val status: String,
    val reportedByUserId: String,
    val assignedCollectorId: String,
    val imageEvidenceUrl: String? = null,
    val createdAt: String,
    val resolvedAt: String? = null,
)

@Serializable
data class UpdateStatusRequest(
    val status: String,
    val imageEvidenceUrl: String,
)

@Serializable
data class UpdateStatusResponse(
    val id: String,
    val code: String,
    val status: String,
    val wardName: String,
    val reportedByUserId: String,
    val reportedBy: String,
    val collectorId: String,
    val imageEvidenceUrl: String,
    val resolvedAt: String,
)

@Serializable
data class CollectorDetectUserDto(
    val id: String,
    val fullName: String
)

@Serializable
data class CollectorDetectHouseholdDto(
    val id: String,
    val address: String,
    val lat: String,
    val lng: String
)

@Serializable
data class CollectorDetectItemDto(
    val name: String,
    val area: Int? = null,
    val quantity: Int? = null,
    @SerialName("mass_kg") val massKg: Double? = null
)

@Serializable
data class CollectorDetectRecordDto(
    val id: String,
    val imageUrl: String,
    val items: List<CollectorDetectItemDto>? = null,
    val totalObjects: Int? = null,
    val totalMassKg: Double? = null,
    val detectType: String,
    val status: String? = null,
    val createdAt: String,
    val detectedBy: CollectorDetectUserDto? = null,
    val collectedBy: CollectorDetectUserDto? = null,
    val household: CollectorDetectHouseholdDto? = null
)

@Serializable
data class CollectorDetectListResponse(
    val message: String,
    val data: List<CollectorDetectRecordDto>
)

@Serializable
data class CollectorCheckinRequest(
    val imageUrl: String
)

@Serializable
data class CollectorCheckinResponse(
    val message: String,
    val data: CollectorDetectRecordDto
)

// ── API calls ─────────────────────────────────────────────────────────────────

/** GET /waste-collector — all reports assigned to the current collector */
suspend fun getAssignedReports(accessToken: String): WasteCollectorListResponse {
    AppLogger.i("WasteCollector", "getAssignedReports")
    try {
        val resp = httpClient.get("$BASE_URL/waste-collector") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("WasteCollector", "getAssignedReports → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("WasteCollector", "getAssignedReports failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("WasteCollector", "getAssignedReports error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /waste-collector/{id} — detail of one assigned report */
suspend fun getAssignedReportById(accessToken: String, id: String): WasteCollectorReportDetailDto {
    AppLogger.i("WasteCollector", "getAssignedReportById id=$id")
    try {
        val resp = httpClient.get("$BASE_URL/waste-collector/$id") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("WasteCollector", "getAssignedReportById → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("WasteCollector", "getAssignedReportById failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("WasteCollector", "getAssignedReportById error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** PATCH /waste-collector/{id}/status — update collection status with evidence image */
suspend fun updateReportStatus(
    accessToken: String,
    id: String,
    request: UpdateStatusRequest,
): UpdateStatusResponse {
    AppLogger.i("WasteCollector", "updateReportStatus id=$id status=${request.status}")
    try {
        val resp = httpClient.patch("$BASE_URL/waste-collector/$id/status") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        AppLogger.d("WasteCollector", "updateReportStatus → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("WasteCollector", "updateReportStatus failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("WasteCollector", "updateReportStatus error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /households/detects/{type} — all detects by type */
suspend fun getAllDetectsByType(accessToken: String, type: String): CollectorDetectListResponse {
    AppLogger.i("WasteCollector", "getAllDetectsByType type=$type")
    try {
        val resp = httpClient.get("$BASE_URL/households/detects/$type") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("WasteCollector", "getAllDetectsByType → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("WasteCollector", "getAllDetectsByType failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("WasteCollector", "getAllDetectsByType error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /collectors/get-brought-out */
suspend fun getBroughtOut(accessToken: String): CollectorDetectListResponse {
    AppLogger.i("WasteCollector", "getBroughtOut")
    try {
        val resp = httpClient.get("$BASE_URL/collectors/get-brought-out") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("WasteCollector", "getBroughtOut → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("WasteCollector", "getBroughtOut failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("WasteCollector", "getBroughtOut error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** POST /collectors/pickups/{id}/checkin */
suspend fun checkinPickup(accessToken: String, id: String, request: CollectorCheckinRequest): CollectorCheckinResponse {
    AppLogger.i("WasteCollector", "checkinPickup id=$id")
    try {
        val resp = httpClient.post("$BASE_URL/collectors/pickups/$id/checkin") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        AppLogger.d("WasteCollector", "checkinPickup → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("WasteCollector", "checkinPickup failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("WasteCollector", "checkinPickup error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** POST /collectors/pickups/{id}/checkin — batch variant for multiple points at same address */
suspend fun checkinPickupBatch(
    accessToken: String,
    points: List<Pair<String, CollectorCheckinRequest>>,
): List<Result<CollectorCheckinResponse>> = coroutineScope {
    points.map { (id, request) ->
        async {
            runCatching { checkinPickup(accessToken, id, request) }
        }
    }.awaitAll()
}
