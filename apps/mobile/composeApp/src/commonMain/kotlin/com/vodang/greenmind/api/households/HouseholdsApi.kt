package com.vodang.greenmind.api.households

import com.vodang.greenmind.api.BASE_URL
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.auth.ErrorResponse
import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Models ────────────────────────────────────────────────────────────────────

@Serializable
data class CreateHouseholdRequest(
    val address: String,
    val lat: Double,
    val lng: Double
)

@Serializable
data class UpdateHouseholdRequest(
    val address: String,
    val lat: Double,
    val lng: Double,
    val userId: String
)

@Serializable
data class HouseholdMemberDto(
    val id: String,
    val username: String,
    val email: String,
    val phoneNumber: String? = null,
    val fullName: String,
    val gender: String,
    val location: String,
    val region: String,
    val role: String,
    val roleId: String? = null,
    val householdId: String,
    val dateOfBirth: String,
    val segmentId: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val household: HouseholdDto? = null,
)

@Serializable
data class HouseholdDto(
    val id: String = "",
    val address: String = "",
    val urbanAreaId: String? = null,
    val lat: String = "",
    val lng: String = "",
    val scoreGreen: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = "",
    val members: List<HouseholdMemberDto>? = null
) {
    val isValid: Boolean get() = id.isNotBlank()
}

/**
 * The backend returns two different shapes from GET /households:
 *  Shape 1 (user is household admin): flat fields at root of `data` — no `holdhousehold` key.
 *  Shape 2 (user is a member):        `holdhousehold` nested object + `greenScore`.
 * All fields are optional so either shape deserializes correctly.
 */
@Serializable
data class HouseholdResponseData(
    // Shape 2 fields
    val holdhousehold: HouseholdDto? = null,
    val greenScore: Int = 0,
    // Shape 1 flat fields
    val id: String? = null,
    val address: String? = null,
    val urbanAreaId: String? = null,
    val lat: String? = null,
    val lng: String? = null,
    val scoreGreen: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val members: List<HouseholdMemberDto>? = null,
) {
    /** Resolves to a [HouseholdDto] regardless of which response shape was returned. */
    fun toHouseholdDto(): HouseholdDto? {
        // Shape 2: nested holdhousehold
        holdhousehold?.takeIf { it.isValid }?.let { return it }
        // Shape 1: flat fields promoted to HouseholdDto
        if (!id.isNullOrBlank()) {
            return HouseholdDto(
                id          = id,
                address     = address ?: "",
                urbanAreaId = urbanAreaId,
                lat         = lat ?: "",
                lng         = lng ?: "",
                scoreGreen  = scoreGreen ?: 0,
                createdAt   = createdAt ?: "",
                updatedAt   = updatedAt ?: "",
                members     = members,
            )
        }
        return null
    }
}

@Serializable
data class HouseholdResponse(
    val data: HouseholdResponseData
)

@Serializable
data class AllHouseholdsResponse(
    val data: List<HouseholdDto>
)

@Serializable
data class GreenScoreEntryDto(
    val id: String,
    val previousScore: Int,
    val delta: Int,
    val finalScore: Int,
    val householdId: String,
    val items: List<DetectItemDto>? = null,
    val reasons: List<String>? = null,
    val level: String? = null,
    val wasteDetectionId: String? = null,
    val createdAt: String
)

@Serializable
data class GreenScoreHouseholdDto(
    val id: String,
    val address: String,
    val urbanAreaId: String? = null,
    val lat: String,
    val lng: String,
    val createdAt: String,
    val updatedAt: String,
    val greenScores: List<GreenScoreEntryDto>
)

@Serializable
data class GreenScoreByDetectResponse(
    val message: String? = null,
    val data: GreenScoreEntryDto
)

@Serializable
data class GreenScoreHistoryResponse(
    val message: String? = null,
    val data: List<GreenScoreEntryDto>
)

@Serializable
data class GreenScoreResponse(
    val message: String? = null,
    val data: GreenScoreHouseholdDto
)

@Serializable
data class HouseholdMessageResponse(
    val message: String? = null
)

@Serializable
data class DetectImageUrlRequest(
    val imageUrl: String
)

@Serializable
data class SegmentsDto(
    val recyclable: List<String> = emptyList(),
    val residual: List<String> = emptyList(),
)

@Serializable
data class DetectItemDto(
    val name: String,
    val area: Int = 0,
    val quantity: Int = 0,
    @SerialName("mass_kg") val massKg: Double? = null,
)

@Serializable
data class DetectPollutionDto(
    @SerialName("Cd") val cd: Double? = null,
    @SerialName("Hg") val hg: Double? = null,
    @SerialName("Pb") val pb: Double? = null,
    @SerialName("CH4") val ch4: Double? = null,
    @SerialName("CO2") val co2: Double? = null,
    @SerialName("NOx") val nox: Double? = null,
    @SerialName("SO2") val so2: Double? = null,
    @SerialName("PM2.5") val pm25: Double? = null,
    val dioxin: Double? = null,
    val nitrate: Double? = null,
    val styrene: Double? = null,
    val microplastic: Double? = null,
    @SerialName("toxic_chemicals") val toxicChemicals: Double? = null,
    @SerialName("chemical_residue") val chemicalResidue: Double? = null,
    @SerialName("non_biodegradable") val nonBiodegradable: Double? = null
)

@Serializable
data class DetectImpactDto(
    @SerialName("air_pollution") val airPollution: Double? = null,
    @SerialName("soil_pollution") val soilPollution: Double? = null,
    @SerialName("water_pollution") val waterPollution: Double? = null
)

@Serializable
data class DetectItemMassDto(
    val name: String,
    @SerialName("mass_kg") val massKg: Double
)

@Serializable
data class DetectTrashHistoryDto(
    val id: String,
    val imageUrl: String = "",
    val items: List<DetectItemDto>? = null,
    val pollution: DetectPollutionDto? = null,
    val impact: DetectImpactDto? = null,
    val totalObjects: Int? = null,
    val totalMassKg: Double? = null,
    val annotatedImageUrl: String? = null,
    val depthMapUrl: String? = null,
    val itemsMass: List<DetectItemMassDto>? = null,
    val segments: SegmentsDto? = null,
    val aiAnalysis: String? = null,
    val householdId: String? = null,
    val detectType: String? = null,
    /** Lifecycle status: "detected", "brought_out", "picked_up" */
    val status: String? = null,
    val pickupProofImageUrl: String? = null,
    val collectorId: String? = null,
    val pickedUpAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val detectedBy: HouseholdMemberDto? = null,   // required in most endpoints
    val household: HouseholdDto? = null,           // optional — absent from historyByUser required array
    val greenScore: GreenScoreEntryDto? = null     // per-detection green score (added to /detects/monthly)
)

@Serializable
data class DetectTrashHistoryResponse(
    val message: String? = null,
    val data: List<DetectTrashHistoryDto>
)

@Serializable
data class DetectTrashResultResponse(
    val message: String? = null,
    val data: DetectTrashHistoryDto
)

// ── Analyze Image (Unified) ───────────────────────────────────────────────────

@Serializable
data class AnalyzeImageRequest(
    val imageUrl: String,
    val type: String = "DETECT_TRASH"
)

@Serializable
data class AnalyzeImageResponse(
    val message: String? = null,
    val data: AnalyzeImageDataDto
)

@Serializable
data class AnalyzeImageDataDto(
    val id: String? = null,
    val imageUrl: String? = null,
    val segments: SegmentsDto? = null,
    val householdId: String? = null,
    val userId: String? = null,
    val detectType: String? = null,
    val status: String? = null,
    val household: HouseholdDto? = null,
    val detectedBy: HouseholdMemberDto? = null,
    val items: List<DetectItemDto>? = null,
    val pollution: DetectPollutionDto? = null,
    val impact: DetectImpactDto? = null,
    val totalObjects: Int? = null,
    val totalMassKg: Double? = null,
    val annotatedImageUrl: String? = null,
    val depthMapUrl: String? = null,
    val aiAnalysis: String? = null,
    val pickupProofImageUrl: String? = null,
    val collectorId: String? = null,
    val pickedUpAt: String? = null,
    val billAmount: Double? = null,
    val paidAt: String? = null,
    val isPaid: Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

// ── API calls ─────────────────────────────────────────────────────────────────

/**
 * POST /households
 */
suspend fun createHousehold(accessToken: String, request: CreateHouseholdRequest) {
    AppLogger.i("HouseholdsApi", "createHousehold")
    executeUnitRequest {
        httpClient.post("$BASE_URL/households") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}

/**
 * PUT /households
 */
suspend fun updateHousehold(accessToken: String, request: UpdateHouseholdRequest) {
    AppLogger.i("HouseholdsApi", "updateHousehold")
    executeUnitRequest {
        httpClient.put("$BASE_URL/households") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}

/**
 * GET /households
 */
suspend fun getCurrentUserHousehold(accessToken: String): HouseholdResponse {
    AppLogger.i("HouseholdsApi", "getCurrentUserHousehold")
    return executeRequest<HouseholdResponse> {
        httpClient.get("$BASE_URL/households") {
            header("Authorization", "Bearer $accessToken")
        }
    }
}

/**
 * DELETE /households/{id}
 */
suspend fun removeMemberFromHousehold(accessToken: String, id: String): HouseholdMessageResponse {
    AppLogger.i("HouseholdsApi", "removeMemberFromHousehold id=$id")
    return executeRequest<HouseholdMessageResponse> {
        httpClient.delete("$BASE_URL/households/$id") {
            header("Authorization", "Bearer $accessToken")
        }
    }
}

/**
 * POST /households/detect-trash
 */
suspend fun detectTrashOnly(accessToken: String, request: DetectImageUrlRequest): DetectTrashResultResponse {
    AppLogger.i("HouseholdsApi", "detectTrashOnly")
    return executeRequest<DetectTrashResultResponse> {
        httpClient.post("$BASE_URL/households/detect-trash") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}

/**
 * POST /households/predict-pollutant
 */
suspend fun predictPollutant(accessToken: String, request: DetectImageUrlRequest): DetectTrashResultResponse {
    AppLogger.i("HouseholdsApi", "predictPollutant")
    return executeRequest<DetectTrashResultResponse> {
        httpClient.post("$BASE_URL/households/predict-pollutant") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}

/**
 * GET /households/detect-trash/historyByUser
 */
suspend fun getDetectHistoryByUser(accessToken: String): DetectTrashHistoryResponse {
    AppLogger.i("HouseholdsApi", "getDetectHistoryByUser")
    return executeRequest<DetectTrashHistoryResponse> {
        httpClient.get("$BASE_URL/households/detect-trash/historyByUser") {
            header("Authorization", "Bearer $accessToken")
        }
    }
}

/**
 * GET /households/detect-trash/historyByHousehold
 */
suspend fun getDetectHistoryByHousehold(accessToken: String): DetectTrashHistoryResponse {
    AppLogger.i("HouseholdsApi", "getDetectHistoryByHousehold")
    return executeRequest<DetectTrashHistoryResponse> {
        httpClient.get("$BASE_URL/households/detect-trash/historyByHousehold") {
            header("Authorization", "Bearer $accessToken")
        }
    }
}

/**
 * GET /households/get-all-households
 */
suspend fun getAllHouseholds(accessToken: String): AllHouseholdsResponse {
    AppLogger.i("HouseholdsApi", "getAllHouseholds")
    return executeRequest<AllHouseholdsResponse> {
        httpClient.get("$BASE_URL/households/get-all-households") {
            header("Authorization", "Bearer $accessToken")
        }
    }
}

/**
 * GET /households/get-detect-by-household/:id
 */
suspend fun getDetectByHouseholdId(accessToken: String, id: String): DetectTrashHistoryResponse {
    AppLogger.i("HouseholdsApi", "getDetectByHouseholdId id=$id")
    return executeRequest<DetectTrashHistoryResponse> {
        httpClient.get("$BASE_URL/households/get-detect-by-household/$id") {
            header("Authorization", "Bearer $accessToken")
        }
    }
}

/**
 * PUT /households  (add a member by userId, keeping existing address/coordinates)
 */
suspend fun addMemberToHousehold(accessToken: String, household: HouseholdDto, userId: String) {
    AppLogger.i("HouseholdsApi", "addMemberToHousehold userId=$userId")
    updateHousehold(
        accessToken,
        UpdateHouseholdRequest(
            address = household.address,
            lat     = household.lat.toDoubleOrNull() ?: 0.0,
            lng     = household.lng.toDoubleOrNull() ?: 0.0,
            userId  = userId,
        )
    )
}

/**
 * POST /households/total-mass
 */
suspend fun detectTotalMass(accessToken: String, request: DetectImageUrlRequest): DetectTrashResultResponse {
    AppLogger.i("HouseholdsApi", "detectTotalMass")
    return executeRequest<DetectTrashResultResponse> {
        httpClient.post("$BASE_URL/households/total-mass") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
            timeout {
                requestTimeoutMillis = 500_000
                socketTimeoutMillis  = 500_000
                connectTimeoutMillis = 30_000
            }
        }
    }
}

/**
 * POST /households/detect-trash/{id}/bring-out — marks the record as brought out for pickup.
 */
suspend fun bringOutDetectTrash(accessToken: String, id: String): DetectTrashResultResponse {
    AppLogger.i("HouseholdsApi", "bringOutDetectTrash id=$id")
    return executeRequest<DetectTrashResultResponse> {
        httpClient.post("$BASE_URL/households/detect-trash/$id/bring-out") {
            header("Authorization", "Bearer $accessToken")
        }
    }
}

/**
 * GET /households/detect-trash/{type}
 * type: total_mass, predict_pollutant_impact, detect_trash
 */
suspend fun getDetectTrashByType(accessToken: String, type: String): DetectTrashHistoryResponse {
    AppLogger.i("HouseholdsApi", "getDetectTrashByType type=$type")
    return executeRequest<DetectTrashHistoryResponse> {
        httpClient.get("$BASE_URL/households/detect-trash/$type") {
            header("Authorization", "Bearer $accessToken")
        }
    }
}

/**
 * GET /households/detect-trash/historyByHousehold/{type}
 */
suspend fun getHistoryByHouseholdByType(accessToken: String, type: String): DetectTrashHistoryResponse {
    AppLogger.i("HouseholdsApi", "getHistoryByHouseholdByType type=$type")
    return executeRequest<DetectTrashHistoryResponse> {
        httpClient.get("$BASE_URL/households/detect-trash/historyByHousehold/$type") {
            header("Authorization", "Bearer $accessToken")
        }
    }
}

/**
 * GET /households/green-score/{householdId}
 */
suspend fun getGreenScoreByHousehold(accessToken: String, householdId: String): GreenScoreResponse {
    AppLogger.i("HouseholdsApi", "getGreenScoreByHousehold householdId=$householdId")
    return executeRequest<GreenScoreResponse> {
        httpClient.get("$BASE_URL/households/green-score/$householdId") {
            header("Authorization", "Bearer $accessToken")
        }
    }
}

/**
 * POST /households/green-score/{detectId} — calculates green score for a specific scan.
 */
suspend fun submitGreenScoreByDetectId(accessToken: String, detectId: String): GreenScoreByDetectResponse {
    AppLogger.i("HouseholdsApi", "submitGreenScoreByDetectId detectId=$detectId")
    return executeRequest<GreenScoreByDetectResponse> {
        httpClient.post("$BASE_URL/households/green-score/$detectId") {
            header("Authorization", "Bearer $accessToken")
        }
    }
}

/**
 * GET /households/green-score/history — fallback green score from history.
 */
suspend fun getGreenScoreHistory(accessToken: String): GreenScoreHistoryResponse {
    AppLogger.i("HouseholdsApi", "getGreenScoreHistory")
    return executeRequest<GreenScoreHistoryResponse> {
        httpClient.get("$BASE_URL/households/green-score/history") {
            header("Authorization", "Bearer $accessToken")
        }
    }
}

/**
 * POST /households/analyze-image — unified endpoint for image analysis.
 */
suspend fun analyzeImage(accessToken: String, request: AnalyzeImageRequest): AnalyzeImageResponse {
    AppLogger.i("HouseholdsApi", "analyzeImage")
    return executeRequest<AnalyzeImageResponse> {
        httpClient.post("$BASE_URL/households/analyze-image") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
            timeout {
                requestTimeoutMillis = 300_000L
                socketTimeoutMillis = 300_000L
            }
        }
    }
}

/**
 * GET /households/admin/detects/monthly — monthly detections for admin dashboard.
 */
suspend fun getMonthlyDetects(accessToken: String): MonthlyDetectsResponse {
    AppLogger.i("HouseholdsApi", "getMonthlyDetects")
    return executeRequest<MonthlyDetectsResponse> {
        httpClient.get("$BASE_URL/households/admin/detects/monthly") {
            header("Authorization", "Bearer $accessToken")
        }
    }
}

/**
 * GET /households/detects/monthly — monthly detections for mobile.
 */
suspend fun getDetectsMonthly(accessToken: String): MonthlyDetectsResponse {
    AppLogger.i("HouseholdsApi", "getDetectsMonthly")
    return executeRequest<MonthlyDetectsResponse> {
        httpClient.get("$BASE_URL/households/detects/monthly") {
            header("Authorization", "Bearer $accessToken")
        }
    }
}

@Serializable
data class MonthlyDetectsResponse(
    val message: String? = null,
    val count: Int = 0,
    val data: List<DetectTrashHistoryDto>
)

// ── Generic Request Execution ───────────────────────────────────────────────────

private suspend inline fun <reified T> executeRequest(crossinline action: suspend () -> HttpResponse): T {
    try {
        val resp = action()
        return if (resp.status.isSuccess()) {
            if (Unit is T) {
                Unit as T
            } else {
                resp.body()
            }
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("HouseholdsApi", "Request failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("HouseholdsApi", "Request error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

// Overload for Unit responses
private suspend inline fun executeUnitRequest(crossinline action: suspend () -> HttpResponse) {
    try {
        val resp = action()
        if (!resp.status.isSuccess()) {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("HouseholdsApi", "Request failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("HouseholdsApi", "Request error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
