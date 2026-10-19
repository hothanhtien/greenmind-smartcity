package com.vodang.greenmind.api

import com.vodang.greenmind.envOverride
import com.vodang.greenmind.store.NetworkCaptureStore
import com.vodang.greenmind.store.attachNetworkCapture
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.logging.Logger
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

val BASE_URL: String = envOverride("GREENMIND_API_URL") ?: "https://vodang-api.gauas.com"
val AI_SERVICE_URL: String = envOverride("GREENMIND_AI_URL") ?: "https://ai-greenmind.khoav4.com"

// ── Client factory ────────────────────────────────────────────────────────────

/**
 * Creates an [HttpClient] with:
 *  - JSON content negotiation (lenient, ignores unknown keys)
 *  - Per-tag logcat logging
 *  - Configurable timeouts
 *  - The shared NetworkCaptureStore interceptor
 *
 * All API modules should use this factory instead of constructing their own
 * HttpClient so that every network call appears in the in-app network log.
 */
fun buildHttpClient(
    tag: String = "HTTP",
    requestTimeoutMs: Long = 30_000,
    connectTimeoutMs: Long = 15_000,
    socketTimeoutMs: Long  = 30_000,
): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = requestTimeoutMs
        connectTimeoutMillis = connectTimeoutMs
        socketTimeoutMillis  = socketTimeoutMs
    }
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) { AppLogger.d(tag, message) }
        }
        level = LogLevel.HEADERS
    }
    install(HttpCallValidator) {
        validateResponse { response ->
            val method = response.call.request.method.value
            val url    = response.call.request.url.encodedPath
            val status = response.status.value
            AppLogger.i(tag, "$method $url → $status")
        }
    }
}.also { it.attachNetworkCapture(tag) }

// ── Default shared client (backend REST API) ──────────────────────────────────

val httpClient: HttpClient = buildHttpClient(tag = "HTTP")