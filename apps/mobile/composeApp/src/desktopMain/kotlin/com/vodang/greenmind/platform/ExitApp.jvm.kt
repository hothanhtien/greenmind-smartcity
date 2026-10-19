package com.vodang.greenmind.platform

actual val exitApp: () -> Unit = {
    kotlin.system.exitProcess(0)
}