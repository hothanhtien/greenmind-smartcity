package com.vodang.greenmind.platform

import android.content.Intent
import android.net.Uri
import com.vodang.greenmind.store.AndroidNotificationContext
import com.vodang.greenmind.util.AppLogger

actual object UrlOpener {
    actual fun openInBrowser(url: String) {
        val ctx = AndroidNotificationContext.context
        if (ctx == null) {
            AppLogger.e("UrlOpener", "Android context not initialized")
            return
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            ctx.startActivity(intent)
        } catch (e: Throwable) {
            AppLogger.e("UrlOpener", "openInBrowser failed: ${e.message}")
        }
    }
}
