package com.vodang.greenmind.api.wastesort

import com.vodang.greenmind.api.AI_SERVICE_URL
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.buildHttpClient
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json

private val lenientJson = Json { ignoreUnknownKeys = true; isLenient = true }

private val AI_BASE_URL = AI_SERVICE_URL

@Serializable
data class DetectTrashResponse(
    @SerialName("total_objects") val totalObjects: Int,
    @SerialName("image_url")     val imageUrl: String,
    val grouped: Map<String, List<String>>,
    /** Backend record ID — set programmatically after detect-trash call, not from JSON. */
    @Transient val backendId: String? = null,
)

/** Nullable mirror of [DetectTrashResponse] used to safely parse AI responses whose
 *  shape may vary (e.g. wrapped envelope, missing fields on error). */
@Serializable
private data class DetectTrashResponseDto(
    @SerialName("total_objects") val totalObjects: Int? = null,
    @SerialName("image_url")     val imageUrl: String? = null,
    val grouped: Map<String, List<String>>? = null,
) {
    fun toResponse(): DetectTrashResponse? {
        val t = totalObjects ?: return null
        val u = imageUrl?.takeIf { it.isNotBlank() } ?: return null
        val g = grouped?.takeIf { it.isNotEmpty() } ?: return null
        return DetectTrashResponse(totalObjects = t, imageUrl = u, grouped = g)
    }
}

private val detectHttpClient = buildHttpClient(
    tag              = "DetectTrash",
    requestTimeoutMs = 300_000L,
    connectTimeoutMs = 30_000L,
    socketTimeoutMs  = 300_000L,
)

/**
 * POST /predict_trash_seg — sends raw image bytes and returns per-category segment crops.
 *
 * Response [DetectTrashResponse.grouped] maps category names ("recyclable", "residual") to
 * lists of Cloudinary crop URLs — one URL per detected object segment.
 *
 * @param imageBytes  Raw bytes of the captured image (JPEG).
 * @param filename    File name sent in the multipart form (default: "scan.jpg").
 */
suspend fun predictTrashSeg(
    imageBytes: ByteArray,
    filename: String = "scan.jpg",
): DetectTrashResponse {
    AppLogger.i("DetectTrash", "predictTrashSeg filename=$filename bytes=${imageBytes.size}")
    try {
        val response = detectHttpClient.post("$AI_BASE_URL/predict_trash_seg") {
            timeout {
                requestTimeoutMillis = 300_000L
                socketTimeoutMillis = 300_000L
            }
            setBody(MultiPartFormDataContent(formData {
                append("file", imageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                })
            }))
        }
        val rawBody = response.bodyAsText()
        if (!response.status.isSuccess()) {
            AppLogger.e("DetectTrash", "predictTrashSeg http ${response.status.value}: $rawBody")
            throw ApiException(response.status.value, rawBody)
        }
        AppLogger.i("DetectTrash", "predictTrashSeg raw: $rawBody")
        val dto = lenientJson.decodeFromString<DetectTrashResponseDto>(rawBody)
        return dto.toResponse()
            ?: throw ApiException(0, "predictTrashSeg: incomplete response — raw=$rawBody")
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("DetectTrash", "predictTrashSeg error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/**
 * POST /detect-trash-ver2 — uploads an image and returns the waste detection result.
 *
 * @param imageBytes  Raw bytes of the captured image (JPEG).
 * @param filename    File name sent in the multipart form (default: "scan.jpg").
 */
suspend fun detectTrash(
    imageBytes: ByteArray,
    filename: String = "scan.jpg",
): DetectTrashResponse {
    AppLogger.i("DetectTrash", "detectTrash filename=$filename bytes=${imageBytes.size}")
    try {
        val response = detectHttpClient.post("$AI_BASE_URL/detect-trash-ver2") {
            timeout {
                requestTimeoutMillis = 300_000L
                socketTimeoutMillis = 300_000L
            }
            setBody(MultiPartFormDataContent(formData {
                append("file", imageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                })
            }))
        }
        val rawBody = response.bodyAsText()
        if (!response.status.isSuccess()) {
            AppLogger.e("DetectTrash", "detectTrash http ${response.status.value}: $rawBody")
            throw ApiException(response.status.value, rawBody)
        }
        AppLogger.i("DetectTrash", "detectTrash raw: $rawBody")
        val dto = lenientJson.decodeFromString<DetectTrashResponseDto>(rawBody)
        return dto.toResponse()
            ?: throw ApiException(0, "detectTrash: incomplete response — raw=$rawBody")
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("DetectTrash", "detectTrash error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
