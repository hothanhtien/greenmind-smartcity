package com.vodang.greenmind.store

import com.vodang.greenmind.util.AppLogger

actual class NotificationService {
    actual fun showChatNotification(campaignId: String, campaignName: String, senderName: String, content: String) {
        AppLogger.i("Notification", "iOS: [$campaignName] $senderName: $content")
    }
}
