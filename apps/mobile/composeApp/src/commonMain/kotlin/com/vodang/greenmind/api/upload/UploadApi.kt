package com.vodang.greenmind.api.upload

import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.auth.ErrorResponse
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

private const val BUCKET_URL = "https://greenmind-bucket.khoav4.com"
private const val WORKER_URL = "https://upload-worker.nbk2124-z.workers.dev"

// ── Result type ───────────────────────────────────────────────────────────────

/**
 * Result of a completed upload.
 * @param key      Storage key (e.g. "uploads/uuid/image.jpg")
 * @param imageUrl Public-readable URL: BUCKET_URL + "/" + key
 */
data class UploadResult(val key: String, val imageUrl: String)

// ── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class RequestUploadUrlRequest(
    val filename: String,
    val contentType: String,
)

@Serializable
data class RequestUploadUrlResponse(
    val uploadUrl: String,
    val key: String,
    val contentType: String,
)

// ── API calls ─────────────────────────────────────────────────────────────────

/**
 * Two-step upload:
 * 1. POST [WORKER_URL]/upload-url → get actual upload endpoint
 * 2. POST to that endpoint with multipart file → returns { success, key }
 * 3. Build imageUrl as BUCKET_URL/key
 */
suspend fun requestAndUpload(
    accessToken: String,
    filename: String,
    fileBytes: ByteArray,
    contentType: String,
): UploadResult {
    AppLogger.i("Upload", "requestAndUpload filename=$filename bytes=${fileBytes.size}")
    try {

    // Step 1: get upload URL from worker
    val urlResp = httpClient.post("$WORKER_URL/upload-url") {
        header("Authorization", "Bearer $accessToken")
        contentType(ContentType.Application.Json)
        setBody(RequestUploadUrlRequest(filename = filename, contentType = contentType))
    }
    if (!urlResp.status.isSuccess()) {
        val text = try { urlResp.body<ErrorResponse>().message } catch (_: Throwable) { urlResp.bodyAsText() }
        AppLogger.e("Upload", "requestUploadUrl failed: ${urlResp.status.value} $text")
        throw ApiException(urlResp.status.value, text)
    }
    val uploadUrlResp = urlResp.body<RequestUploadUrlResponse>()
    AppLogger.i("Upload", "got uploadUrl key=${uploadUrlResp.key}")

    // Step 2: PUT raw bytes directly to the pre-signed R2 URL
    val uploadResp = httpClient.put(uploadUrlResp.uploadUrl) {
        header(HttpHeaders.ContentType, uploadUrlResp.contentType)
        setBody(fileBytes)
    }
    if (!uploadResp.status.isSuccess()) {
        val text = try { uploadResp.bodyAsText() } catch (_: Throwable) { "Upload failed" }
        AppLogger.e("Upload", "uploadFile failed: ${uploadResp.status.value} $text")
        throw ApiException(uploadResp.status.value, text)
    }
    val key = uploadUrlResp.key
    val imageUrl = "$BUCKET_URL/$key"
    AppLogger.i("Upload", "upload success key=$key")
    return UploadResult(key = key, imageUrl = imageUrl)

    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Upload", "requestAndUpload error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
