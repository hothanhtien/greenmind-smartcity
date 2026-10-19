package com.vodang.greenmind.api.todo

import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.buildHttpClient
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

private const val SPLIT_URL = "https://task-splitter-worker.nbk2124-z.workers.dev/split"

// ── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class SplitRequest(
    val task: String,
    val level: String = "mid",
)

@Serializable
data class SplitResult(
    val task: String,
    val subtasks: List<String>,
)

@Serializable
data class SplitResponse(
    val success: Boolean,
    val result: SplitResult,
)

// ── HTTP client ───────────────────────────────────────────────────────────────

private val splitClient = buildHttpClient(
    tag              = "TodoSplit",
    requestTimeoutMs = 60_000,
    connectTimeoutMs = 15_000,
    socketTimeoutMs  = 60_000,
)

// ── API call ──────────────────────────────────────────────────────────────────

/**
 * POST /split — generates a list of subtask strings for [task].
 *
 * @param task        The task title to break down.
 * @param level       Difficulty/detail level: "easy", "mid", "hard" (default: "mid").
 * @param accessToken Optional Bearer token (endpoint accepts anonymous requests too).
 */
suspend fun splitTask(
    task: String,
    level: String = "mid",
    accessToken: String? = null,
): SplitResponse {
    AppLogger.i("TodoSplit", "splitTask task=$task level=$level")
    try {
        val response = splitClient.post(SPLIT_URL) {
            if (accessToken != null) header(HttpHeaders.Authorization, "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(SplitRequest(task = task, level = level))
        }
        if (!response.status.isSuccess()) {
            val msg = response.bodyAsText()
            AppLogger.e("TodoSplit", "splitTask failed: ${response.status.value} $msg")
            throw ApiException(response.status.value, msg)
        }
        return response.body<SplitResponse>().also {
            AppLogger.i("TodoSplit", "splitTask success subtasks=${it.result.subtasks.size}")
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("TodoSplit", "splitTask error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
