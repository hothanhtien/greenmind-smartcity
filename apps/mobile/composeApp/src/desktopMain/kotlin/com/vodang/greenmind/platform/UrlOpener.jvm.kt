package com.vodang.greenmind.platform

import java.awt.Desktop
import java.net.URI

actual object UrlOpener {
    actual fun openInBrowser(url: String) {
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(URI(url))
    }
}
