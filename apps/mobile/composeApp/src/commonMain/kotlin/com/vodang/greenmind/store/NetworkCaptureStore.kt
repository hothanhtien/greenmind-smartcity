package com.vodang.greenmind.store

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.http.content.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.TimeSource

data class NetworkEntry(
    val id: Long = System.nanoTime(),
    val method: String,
    val url: String,
    val statusCode: Int = 0,
    val statusText: String = "",
    val durationMs: Long = 0,
    val isError: Boolean = false,
    val errorMessage: String = "",
    val requestHeaders: List<Pair<String, String>> = emptyList(),
    val requestBody: String = "",
    val responseHeaders: List<Pair<String, String>> = emptyList(),
    val responseBody: String = "",
)

object NetworkCaptureStore {
    private val _entries = MutableStateFlow<List<NetworkEntry>>(emptyList())
    val entries: StateFlow<List<NetworkEntry>> = _entries.asStateFlow()

    fun add(entry: NetworkEntry) {
        _entries.value = (_entries.value + entry).takeLast(100)
    }

    fun clear() {
        _entries.value = emptyList()
    }
}

// Attach network capture to an HttpClient
fun HttpClient.attachNetworkCapture(tag: String = "HTTP") {
    plugin(HttpSend).intercept { request ->
        val mark = TimeSource.Monotonic.markNow()

        val reqBody = try {
            when (val b = request.body) {
                is OutgoingContent.ByteArrayContent -> {
                    val ct = b.contentType?.toString() ?: ""
                    if (ct.startsWith("image/") || ct.startsWith("application/octet")) {
                        "[binary ${b.bytes().size} bytes — $ct]"
                    } else {
                        b.bytes().decodeToString().take(2000)
                    }
                }
                is OutgoingContent.WriteChannelContent -> "[${b.contentType ?: "stream"}]"
                is OutgoingContent.ReadChannelContent  -> "[${b.contentType ?: "stream"}]"
                is OutgoingContent.NoContent           -> ""
                else -> ""
            }
        } catch (_: Throwable) { "[parse error]" }

        val reqHeaders = try {
            request.headers.entries().map { (k, v) -> k to v.joinToString(", ") }
        } catch (_: Throwable) { emptyList() }

        val call = execute(request)
        val durationMs = mark.elapsedNow().inWholeMilliseconds

        val respBody = try { call.response.bodyAsText().take(2000) } catch (_: Throwable) { "" }
        val respHeaders = try {
            call.response.headers.entries().map { (k, v) -> k to v.joinToString(", ") }
        } catch (_: Throwable) { emptyList() }

        NetworkCaptureStore.add(
            NetworkEntry(
                method          = request.method.value,
                url             = request.url.toString(),
                statusCode      = call.response.status.value,
                statusText      = call.response.status.description,
                durationMs      = durationMs,
                requestHeaders  = reqHeaders,
                requestBody     = reqBody,
                responseHeaders = respHeaders,
                responseBody    = respBody,
            )
        )

        call
    }
}