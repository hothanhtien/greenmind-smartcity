package com.vodang.greenmind.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual object UrlOpener {
    actual fun openInBrowser(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl)
    }
}
