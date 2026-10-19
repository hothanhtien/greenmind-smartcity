package com.vodang.greenmind.store

import com.vodang.greenmind.chat.ChatMessage

expect class ChatSocketService() {
    fun setUserId(id: String)
    fun connect(token: String, onNewMessage: (campaignId: String, ChatMessage) -> Unit)
    fun joinCampaign(campaignId: String)
    fun sendMessage(campaignId: String, content: String)
    fun leaveCampaign(campaignId: String)
    fun disconnect()
}
