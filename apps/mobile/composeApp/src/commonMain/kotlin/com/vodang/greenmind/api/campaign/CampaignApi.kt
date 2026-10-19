package com.vodang.greenmind.api.campaign

import com.vodang.greenmind.api.BASE_URL
import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.auth.ErrorResponse
import com.vodang.greenmind.api.participantcampaign.ParticipantCampaignDto
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

// ── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class CampaignDto(
    val id: String,
    val name: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val lat: Double,
    val lng: Double,
    val radius: Int,
    val status: String,
    val createdByUserId: String,
    val createdAt: String,
    val updatedAt: String,
    val reports: List<CampaignReport> = emptyList(),
    val createdBy: CampaignCreator? = null,
    val participants: List<CampaignParticipant> = emptyList(),
    val participantsCount: Int = 0,
)

@Serializable
data class CampaignReport(
    val id: String,
    val code: String,
    val description: String,
    val imageUrl: String,
    val segmentedImageUrl: String? = null,
    val depthImageUrl: String? = null,
    val heatmapUrl: String? = null,
    val segmentRatio: Double? = null,
    val pollutionScore: Double? = null,
    val pollutionLevel: String? = null,
    val lat: Double,
    val lng: Double,
    val wardName: String,
    val status: String,
    val reportedByUserId: String,
    val imageEvidenceUrl: String? = null,
    val createdAt: String,
    val resolvedAt: String? = null,
    val campaignId: String,
)

@Serializable
data class CampaignCreator(
    val id: String,
    val fullName: String,
)

@Serializable
data class CampaignParticipant(
    val id: String,
    val status: String,
    val checkInTime: String? = null,
    val checkOutTime: String? = null,
    val user: CampaignParticipantUser,
)

@Serializable
data class CampaignParticipantUser(
    val id: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String? = null,
)

@Serializable
data class CreateCampaignRequest(
    val name: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val lat: Double,
    val lng: Double,
    val radius: Int,
    val reportIds: List<String>,
)

@Serializable
data class UpdateCampaignRequest(
    val name: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val lat: Double,
    val lng: Double,
)

@Serializable
data class UpdateCampaignStatusRequest(
    val status: String,
)

@Serializable
data class CampaignDetailDto(
    val name: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val lat: Double,
    val lng: Double,
    val radius: Int,
    val reportIds: List<String>,
    val status: String,
)

@Serializable
data class CampaignAccessDeniedResponse(
    val message: String = "You must be an approved participant to view campaign details"
)

@Serializable
data class ParticipantDto(
    val id: String,
    val userId: String,
    val status: String,
    val checkInTime: String? = null,
    val checkOutTime: String? = null,
    val user: ParticipantUserDto,
    val createdAt: String,
)

@Serializable
data class ParticipantUserDto(
    val id: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String? = null,
)

@Serializable
data class GetParticipantsResponse(
    val participants: List<ParticipantDto>,
)

@Serializable
data class ApproveRejectResponse(
    val message: String,
    val participant: ParticipantDto,
)

// ── API calls ─────────────────────────────────────────────────────────────────

/** GET /campaigns — returns all campaigns */
suspend fun getAllCampaigns(accessToken: String): List<CampaignDto> {
    AppLogger.i("Campaign", "getAllCampaigns")
    try {
        val resp = httpClient.get("$BASE_URL/campaigns") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("Campaign", "getAllCampaigns → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Campaign", "getAllCampaigns failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("Campaign", "getAllCampaigns error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /campaigns/{id} */
suspend fun getCampaignById(accessToken: String, id: String): CampaignDetailDto {
    AppLogger.i("Campaign", "getCampaignById id=$id")
    try {
        val resp = httpClient.get("$BASE_URL/campaigns/$id") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("Campaign", "getCampaignById → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("Campaign", "getCampaignById error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** POST /campaigns */
suspend fun createCampaign(accessToken: String, request: CreateCampaignRequest): CampaignDto {
    AppLogger.i("Campaign", "createCampaign name=${request.name}")
    try {
        val resp = httpClient.post("$BASE_URL/campaigns") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        AppLogger.d("Campaign", "createCampaign → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("Campaign", "createCampaign error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** PUT /campaigns/{id} */
suspend fun updateCampaign(accessToken: String, id: String, request: UpdateCampaignRequest): CampaignDto {
    AppLogger.i("Campaign", "updateCampaign id=$id")
    try {
        val resp = httpClient.put("$BASE_URL/campaigns/$id") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        AppLogger.d("Campaign", "updateCampaign → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("Campaign", "updateCampaign error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** DELETE /campaigns/{id} */
suspend fun deleteCampaign(accessToken: String, id: String): CampaignDto {
    AppLogger.i("Campaign", "deleteCampaign id=$id")
    try {
        val resp = httpClient.delete("$BASE_URL/campaigns/$id") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("Campaign", "deleteCampaign → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("Campaign", "deleteCampaign error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** POST /campaigns/{id}/status */
suspend fun updateCampaignStatus(accessToken: String, id: String, status: String) {
    AppLogger.i("Campaign", "updateCampaignStatus id=$id status=$status")
    try {
        val resp = httpClient.post("$BASE_URL/campaigns/$id/status") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(UpdateCampaignStatusRequest(status))
        }
        AppLogger.d("Campaign", "updateCampaignStatus → HTTP ${resp.status.value}")
        if (!resp.status.isSuccess()) {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("Campaign", "updateCampaignStatus error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** POST /campaigns/{id}/cancel */
suspend fun cancelCampaign(accessToken: String, id: String) {
    AppLogger.i("Campaign", "cancelCampaign id=$id")
    try {
        val resp = httpClient.post("$BASE_URL/campaigns/$id/cancel") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("Campaign", "cancelCampaign → HTTP ${resp.status.value}")
        if (!resp.status.isSuccess()) {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("Campaign", "cancelCampaign error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /campaigns/{id}/participants */
suspend fun getCampaignParticipants(accessToken: String, campaignId: String): GetParticipantsResponse {
    AppLogger.i("Campaign", "getCampaignParticipants campaignId=$campaignId")
    try {
        val resp = httpClient.get("$BASE_URL/campaigns/$campaignId/participants") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("Campaign", "getCampaignParticipants → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("Campaign", "getCampaignParticipants error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /campaigns/{id}/participants/pending */
suspend fun getPendingParticipants(accessToken: String, campaignId: String): List<ParticipantDto> {
    AppLogger.i("Campaign", "getPendingParticipants campaignId=$campaignId")
    try {
        val resp = httpClient.get("$BASE_URL/campaigns/$campaignId/participants/pending") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("Campaign", "getPendingParticipants → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("Campaign", "getPendingParticipants error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** POST /campaigns/{id}/participants/{participantId}/approve */
suspend fun approveParticipant(
    accessToken: String,
    campaignId: String,
    participantId: String,
): ApproveRejectResponse {
    AppLogger.i("Campaign", "approveParticipant campaignId=$campaignId participantId=$participantId")
    try {
        val resp = httpClient.post("$BASE_URL/campaigns/$campaignId/participants/$participantId/approve") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("Campaign", "approveParticipant → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("Campaign", "approveParticipant error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** POST /campaigns/{id}/participants/{participantId}/reject */
suspend fun rejectParticipant(
    accessToken: String,
    campaignId: String,
    participantId: String,
): ApproveRejectResponse {
    AppLogger.i("Campaign", "rejectParticipant campaignId=$campaignId participantId=$participantId")
    try {
        val resp = httpClient.post("$BASE_URL/campaigns/$campaignId/participants/$participantId/reject") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("Campaign", "rejectParticipant → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("Campaign", "rejectParticipant error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

// ── Chat DTOs ───────────────────────────────────────────────────────────────────

@Serializable
data class ChatMessageSender(
    val id: String,
    val fullName: String,
    val role: String,
)

@Serializable
data class ChatMessageDto(
    val id: String,
    val campaignId: String,
    val sender: ChatMessageSender,
    val content: String,
    val createdAt: String,
)

@Serializable
data class ChatMessageLastMessage(
    val content: String,
    val senderId: String,
    val createdAt: String,
)

@Serializable
data class ChatListItemDto(
    val campaignId: String,
    val campaignName: String,
    val campaignStatus: String,
    val messageCount: Int,
    val lastMessage: ChatMessageLastMessage,
    val campaignCreatedAt: String,
    val isCreator: Boolean,
)

@Serializable
data class FetchMessagesResponse(
    val data: List<ChatMessageDto>,
    val total: Int,
    val skip: Int,
    val take: Int,
)

// ── API calls ─────────────────────────────────────────────────────────────────

/** GET /campaigns/{id}/messages — returns chat history for a campaign */
suspend fun getChatMessages(
    accessToken: String,
    campaignId: String,
    take: Int? = null,
    skip: Int? = null,
): FetchMessagesResponse {
    AppLogger.i("Campaign", "getChatMessages campaignId=$campaignId take=$take skip=$skip")
    try {
        val resp = httpClient.get("$BASE_URL/campaigns/$campaignId/messages") {
            header("Authorization", "Bearer $accessToken")
            take?.let { parameter("take", it) }
            skip?.let { parameter("skip", it) }
        }
        AppLogger.d("Campaign", "getChatMessages → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Campaign", "getChatMessages failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("Campaign", "getChatMessages error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /campaigns/chat-list — returns chat list for current user */
suspend fun getChatList(accessToken: String): List<ChatListItemDto> {
    AppLogger.i("Campaign", "getChatList")
    try {
        val resp = httpClient.get("$BASE_URL/campaigns/chat-list") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("Campaign", "getChatList → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Campaign", "getChatList failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("Campaign", "getChatList error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** Converts ParticipantCampaignDto (API response) to CampaignParticipant (UI model) */
fun ParticipantCampaignDto.toCampaignParticipant(user: CampaignParticipantUser) = CampaignParticipant(
    id = id,
    status = status,
    checkInTime = checkInTime,
    checkOutTime = checkOutTime,
    user = user,
)
