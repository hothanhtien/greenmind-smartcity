package com.vodang.greenmind.wastereport

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.vodang.greenmind.api.httpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image as SkiaImage

@Composable
actual fun NetworkImage(url: String, modifier: Modifier, contentScale: ContentScale) {
    var bitmap by remember(url) { mutableStateOf<ImageBitmap?>(null) }
    var loading by remember(url) { mutableStateOf(true) }

    LaunchedEffect(url) {
        loading = true
        bitmap = null
        if (url.isBlank()) { loading = false; return@LaunchedEffect }
        withContext(Dispatchers.IO) {
            runCatching {
                val bytes = httpClient.get(url).readBytes()
                SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
            }.getOrNull()
        }.also {
            bitmap = it
            loading = false
        }
    }

    when {
        loading -> Box(modifier.background(Color(0xFFEEEEEE)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(strokeWidth = 2.dp, color = Color(0xFF2E7D32))
        }
        bitmap != null -> Image(
            bitmap = bitmap!!,
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier,
        )
        else -> Box(modifier.background(Color(0xFFEEEEEE)))
    }
}
