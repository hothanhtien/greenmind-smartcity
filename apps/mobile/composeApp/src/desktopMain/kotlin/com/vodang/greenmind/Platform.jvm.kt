package com.vodang.greenmind

class DesktopPlatform : Platform {
    override val name: String = "Desktop JVM"
}

actual fun getPlatform(): Platform = DesktopPlatform()

actual fun envOverride(name: String): String? = System.getenv(name)?.takeIf { it.isNotBlank() }
