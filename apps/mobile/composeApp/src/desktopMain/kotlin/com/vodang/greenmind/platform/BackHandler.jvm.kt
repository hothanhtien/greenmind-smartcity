package com.vodang.greenmind.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

class BackHandlerEntry {
    var enabled: Boolean = true
    var onBack: () -> Unit = {}
}

val backHandlerStack = mutableListOf<BackHandlerEntry>()

fun invokeTopBackHandler(): Boolean {
    val top = backHandlerStack.lastOrNull { it.enabled } ?: return false
    top.onBack()
    return true
}

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    val entry = remember { BackHandlerEntry() }
    entry.enabled = enabled
    entry.onBack = onBack

    DisposableEffect(Unit) {
        backHandlerStack.add(entry)
        onDispose {
            backHandlerStack.remove(entry)
        }
    }
}
