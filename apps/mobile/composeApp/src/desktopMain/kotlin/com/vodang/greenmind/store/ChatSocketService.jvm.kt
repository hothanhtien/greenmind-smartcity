package com.vodang.greenmind.store

import com.vodang.greenmind.chat.ChatMessage

actual class ChatSocketService actual constructor() {
    // Realtime campaign chat uses the Android Socket.IO client — no-op on desktop
    actual fun setUserId(id: String) {}
    actual fun connect(token: String, onNewMessage: (campaignId: String, ChatMessage) -> Unit) {}
    actual fun joinCampaign(campaignId: String) {}
    actual fun sendMessage(campaignId: String, content: String) {}
    actual fun leaveCampaign(campaignId: String) {}
    actual fun disconnect() {}
}
