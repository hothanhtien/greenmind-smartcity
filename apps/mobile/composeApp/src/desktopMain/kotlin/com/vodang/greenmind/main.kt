package com.vodang.greenmind

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.vodang.greenmind.platform.invokeTopBackHandler

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "GreenMind",
        onPreviewKeyEvent = { event ->
            if (event.key == Key.Escape && event.type == KeyEventType.KeyDown) {
                invokeTopBackHandler()
            } else {
                false
            }
        }
    ) {
        App()
    }
}
