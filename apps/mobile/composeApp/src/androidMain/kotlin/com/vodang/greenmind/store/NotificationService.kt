package com.vodang.greenmind.store

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.vodang.greenmind.MainActivity

object AndroidNotificationContext {
    var context: Context? = null
}

actual class NotificationService {
    companion object {
        private const val CHANNEL_ID = "chat_messages"
        private var notifId = 2000
    }

    actual fun showChatNotification(campaignId: String, campaignName: String, senderName: String, content: String) {
        val ctx = AndroidNotificationContext.context ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Chat Messages", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "New messages from campaign chats"
                setShowBadge(true)
            }
            (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }

        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            ctx, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "$senderName • $campaignName"
        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(notifId++, notification)
    }
}
