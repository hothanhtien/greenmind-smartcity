package com.vodang.greenmind.api.chat

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

// ── Chat List DTOs ────────────────────────────────────────────────────────────

@Serializable
data class ChatListDto(
    val campaignId: String,
    val campaignName: String,
    val campaignStatus: String,
    val messageCount: Int,
    val lastMessage: ChatLastMessageDto? = null,
    val campaignCreatedAt: String,
)

@Serializable
data class ChatLastMessageDto(
    val content: String,
    val senderId: String,
    val createdAt: String,
)

// ── Message DTOs ──────────────────────────────────────────────────────────────

@Serializable
data class MessageDto(
    val id: String,
    val campaignId: String,
    val sender: MessageSenderDto,
    val content: String,
    val createdAt: String,
)

@Serializable
data class MessageSenderDto(
    val id: String,
    val fullName: String,
    val role: String,
)

@Serializable
data class GetMessagesResponse(
    val data: List<MessageDto>,
    val total: Int,
    val skip: Int,
    val take: Int,
)

// ── API Calls ─────────────────────────────────────────────────────────────────

/** GET /campaigns/chat-list */
suspend fun getChatList(accessToken: String): List<ChatListDto> {
    AppLogger.i("Chat", "getChatList")
    try {
        val resp = httpClient.get("$BASE_URL/campaigns/chat-list") {
            header("Authorization", "Bearer $accessToken")
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Chat", "getChatList failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Chat", "getChatList error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /campaigns/{id}/messages */
suspend fun getCampaignMessages(accessToken: String, campaignId: String): GetMessagesResponse {
    AppLogger.i("Chat", "getCampaignMessages campaignId=$campaignId")
    try {
        val resp = httpClient.get("$BASE_URL/campaigns/$campaignId/messages") {
            header("Authorization", "Bearer $accessToken")
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Chat", "getCampaignMessages failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Chat", "getCampaignMessages error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
