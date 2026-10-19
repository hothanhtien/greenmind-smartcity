package com.vodang.greenmind.api.participantcampaign

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

@Serializable
data class MyCampaignDto(
    val id: String,
    val name: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val status: String,
    val lat: Double,
    val lng: Double,
    val radius: Int,
    val participantStatus: String,
    val checkInTime: String? = null,
    val checkOutTime: String? = null,
    val createdBy: CampaignCreatorDto,
)

@Serializable
data class CampaignCreatorDto(
    val id: String,
    val fullName: String,
)

@Serializable
data class ParticipantCampaignDto(
    val id: String,
    val campaignId: String,
    val userId: String,
    val status: String,
    val checkInTime: String? = null,
    val checkInLat: Double? = null,
    val checkInLng: Double? = null,
    val checkOutTime: String? = null,
    val checkOutLat: Double? = null,
    val checkOutLng: Double? = null,
    val createdAt: String,
    val updatedAt: String,
)

/** Response wrapper for POST /participant-campaigns/{id}/register */
@Serializable
data class RegisterCampaignResponse(
    val message: String,
    val participant: ParticipantCampaignDto,
)

@Serializable
data class LocationRequest(
    val lat: Double,
    val lng: Double,
)

// ── API calls ─────────────────────────────────────────────────────────────────

/** GET /participant-campaigns — returns campaigns the current user has registered for */
suspend fun getMyParticipations(accessToken: String): List<MyCampaignDto> {
    AppLogger.i("ParticipantCampaign", "getMyParticipations")
    try {
        val resp = httpClient.get("$BASE_URL/participant-campaigns") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("ParticipantCampaign", "getMyParticipations → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("ParticipantCampaign", "getMyParticipations failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("ParticipantCampaign", "getMyParticipations error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** POST /participant-campaigns/{id}/register */
suspend fun registerCampaign(accessToken: String, campaignId: String): ParticipantCampaignDto {
    AppLogger.i("ParticipantCampaign", "registerCampaign campaignId=$campaignId")
    try {
        val resp = httpClient.post("$BASE_URL/participant-campaigns/$campaignId/register") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("ParticipantCampaign", "registerCampaign → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body<RegisterCampaignResponse>().participant
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("ParticipantCampaign", "registerCampaign failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("ParticipantCampaign", "registerCampaign error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** POST /participant-campaigns/{id}/checkin */
suspend fun checkInCampaign(
    accessToken: String,
    campaignId: String,
    lat: Double,
    lng: Double,
): ParticipantCampaignDto {
    AppLogger.i("ParticipantCampaign", "checkInCampaign campaignId=$campaignId lat=$lat lng=$lng")
    try {
        val resp = httpClient.post("$BASE_URL/participant-campaigns/$campaignId/checkin") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(LocationRequest(lat, lng))
        }
        AppLogger.d("ParticipantCampaign", "checkInCampaign → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("ParticipantCampaign", "checkInCampaign failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("ParticipantCampaign", "checkInCampaign error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** POST /participant-campaigns/{id}/checkout */
suspend fun checkOutCampaign(
    accessToken: String,
    campaignId: String,
    lat: Double,
    lng: Double,
): ParticipantCampaignDto {
    AppLogger.i("ParticipantCampaign", "checkOutCampaign campaignId=$campaignId lat=$lat lng=$lng")
    try {
        val resp = httpClient.post("$BASE_URL/participant-campaigns/$campaignId/checkout") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(LocationRequest(lat, lng))
        }
        AppLogger.d("ParticipantCampaign", "checkOutCampaign → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("ParticipantCampaign", "checkOutCampaign failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("ParticipantCampaign", "checkOutCampaign error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}