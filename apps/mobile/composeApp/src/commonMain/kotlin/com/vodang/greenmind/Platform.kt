package com.vodang.greenmind

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

/** Reads a runtime override for configuration such as service URLs; only the desktop target supports it. */
expect fun envOverride(name: String): String?