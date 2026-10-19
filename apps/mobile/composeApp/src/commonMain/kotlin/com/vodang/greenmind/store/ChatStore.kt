package com.vodang.greenmind.store

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.vodang.greenmind.api.chat.getCampaignMessages
import com.vodang.greenmind.api.chat.getChatList
import com.vodang.greenmind.chat.ChatMessage
import com.vodang.greenmind.chat.ChatThread
import com.vodang.greenmind.chat.toChatMessage
import com.vodang.greenmind.chat.toChatThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object ChatStore {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val socket = ChatSocketService().also {
        it.setUserId(SettingsStore.getUser()?.id ?: "")
    }
    private val notificationService = NotificationService()

    private val pendingNotifications = mutableListOf<PendingNotification>()

    data class PendingNotification(
        val campaignId: String,
        val campaignName: String,
        val senderName: String,
        val content: String,
        val timestamp: Long = System.currentTimeMillis(),
    )

    var threads by mutableStateOf<List<ChatThread>>(emptyList())
    var messages by mutableStateOf<List<ChatMessage>>(emptyList())
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var activeCampaignId by mutableStateOf<String?>(null)

    fun loadThreads() {
        val token = SettingsStore.getAccessToken() ?: return
        isLoading = true
        error = null
        scope.launch {
            try {
                val dtos = getChatList(token)
                threads = dtos.sortedByDescending { it.lastMessage?.createdAt ?: it.campaignCreatedAt }.map { it.toChatThread() }
            } catch (e: Throwable) {
                error = e.message ?: "Failed to load chat threads"
            }
            isLoading = false
        }
    }

    fun openCampaign(campaignId: String) {
        val token = SettingsStore.getAccessToken() ?: return
        val userId = SettingsStore.getUser()?.id ?: ""
        socket.setUserId(userId)
        activeCampaignId = campaignId
        isLoading = true
        error = null
        messages = emptyList()

        // Show pending notifications for other campaigns
        pendingNotifications
            .filter { it.campaignId != campaignId }
            .forEach { notif ->
                notificationService.showChatNotification(
                    notif.campaignId,
                    notif.campaignName,
                    notif.senderName,
                    notif.content,
                )
            }
        pendingNotifications.clear()

        socket.connect(token) { cid, msg ->
            messages = messages.filterNot { it.id.startsWith("temp-") && it.content == msg.content && it.senderId == msg.senderId } + msg
            if (cid != activeCampaignId && !msg.isOwn) {
                val thread = threads.find { it.id == cid }
                pendingNotifications.add(
                    PendingNotification(
                        campaignId = cid,
                        campaignName = thread?.name ?: cid.take(8),
                        senderName = msg.senderName,
                        content = msg.content,
                    )
                )
            }
        }
        socket.joinCampaign(campaignId)

        scope.launch {
            try {
                val resp = getCampaignMessages(token, campaignId)
                messages = resp.data.map { it.toChatMessage(userId) }
            } catch (e: Throwable) {
                error = e.message ?: "Failed to load campaign messages"
            }
            isLoading = false
        }
    }

    fun sendMessage(content: String) {
        val cid = activeCampaignId ?: return
        val userId = SettingsStore.getUser()?.id ?: ""
        val userName = SettingsStore.getUser()?.fullName ?: "Me"
        val tempMsg = ChatMessage(
            id = "temp-${System.currentTimeMillis()}",
            senderId = userId,
            senderName = userName,
            content = content,
            createdAt = "Sending...",
            isOwn = true,
        )
        messages = messages + tempMsg
        socket.sendMessage(cid, content)
    }

    fun closeCampaign() {
        activeCampaignId?.let { socket.leaveCampaign(it) }
        socket.disconnect()
        activeCampaignId = null
        messages = emptyList()
    }
}
