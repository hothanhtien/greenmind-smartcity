package com.vodang.greenmind.wastereport

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.skia.Image as SkiaImage
import java.util.Base64

@Composable
actual fun Base64Image(base64: String, modifier: Modifier) {
    val bitmap = remember(base64) {
        runCatching {
            val clean = if (base64.contains(',')) base64.substringAfter(',') else base64
            val bytes = Base64.getDecoder().decode(clean)
            SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
        }.getOrNull()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier,
        )
    } else {
        Box(modifier.background(Color(0xFFEEEEEE)))
    }
}
