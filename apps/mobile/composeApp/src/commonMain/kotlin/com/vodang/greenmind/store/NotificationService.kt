package com.vodang.greenmind.store

expect class NotificationService() {
    fun showChatNotification(campaignId: String, campaignName: String, senderName: String, content: String)
}
