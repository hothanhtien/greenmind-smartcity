package com.vodang.greenmind.util

import com.vodang.greenmind.store.ErrorLevel
import com.vodang.greenmind.store.ErrorLogEntry
import com.vodang.greenmind.store.ErrorLogStore
import com.vodang.greenmind.time.currentTimeMillis

/**
 * Common logger with a fixed tag for easy logcat filtering.
 *
 * All [w] and [e] calls are also forwarded to [ErrorLogStore] so they
 * appear in the in-app error log on the Settings page.
 *
 * Usage:  `adb logcat | grep GreenMind`
 */
object AppLogger {
    const val TAG = "GreenMind"

    fun d(subtag: String, message: String) {
        println("[$TAG][$subtag] $message")
    }

    fun i(subtag: String, message: String) {
        println("[$TAG][$subtag] $message")
    }

    fun w(subtag: String, message: String) {
        println("[$TAG][$subtag] ⚠ $message")
        ErrorLogStore.add(
            ErrorLogEntry(
                timestampMs = currentTimeMillis(),
                level       = ErrorLevel.W,
                tag         = subtag,
                message     = message,
            )
        )
    }

    fun e(subtag: String, message: String, throwable: Throwable? = null) {
        println("[$TAG][$subtag] ✖ $message")
        throwable?.let { println("[$TAG][$subtag] ✖ ${it.stackTraceToString()}") }
        ErrorLogStore.add(
            ErrorLogEntry(
                timestampMs = currentTimeMillis(),
                level       = ErrorLevel.E,
                tag         = subtag,
                message     = message,
                stackTrace  = throwable?.stackTraceToString(),
            )
        )
    }
}
