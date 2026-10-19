package com.vodang.greenmind.store

import com.vodang.greenmind.chat.ChatMessage
import com.vodang.greenmind.util.AppLogger

actual class ChatSocketService {
    actual fun setUserId(id: String) { }
    actual fun connect(token: String, onNewMessage: (String, ChatMessage) -> Unit) {
        AppLogger.i("ChatSocket", "iOS connect — placeholder. Socket.IO not yet available on iOS.")
    }
    actual fun joinCampaign(campaignId: String) {
        AppLogger.i("ChatSocket", "iOS joinCampaign $campaignId — placeholder")
    }
    actual fun sendMessage(campaignId: String, content: String) {
        AppLogger.i("ChatSocket", "iOS sendMessage to $campaignId — placeholder")
    }
    actual fun leaveCampaign(campaignId: String) {
        AppLogger.i("ChatSocket", "iOS leaveCampaign $campaignId — placeholder")
    }
    actual fun disconnect() {
        AppLogger.i("ChatSocket", "iOS disconnect — placeholder")
    }
}
