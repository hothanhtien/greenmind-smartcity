package com.vodang.greenmind.store

actual class NotificationService actual constructor() {
    // System notifications not available on desktop — no-op
    actual fun showChatNotification(campaignId: String, campaignName: String, senderName: String, content: String) {}
}
