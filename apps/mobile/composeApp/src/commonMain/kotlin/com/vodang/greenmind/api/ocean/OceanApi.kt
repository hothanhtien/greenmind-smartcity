package com.vodang.greenmind.api.ocean

import com.vodang.greenmind.api.BASE_URL
import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.auth.ErrorResponse
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class OceanScores(
    @SerialName("O") val O: Int,
    @SerialName("C") val C: Int,
    @SerialName("E") val E: Int,
    @SerialName("A") val A: Int,
    @SerialName("N") val N: Int,
)

/** Input scores — floats allowed by the create endpoint (e.g. 33.33). */
@Serializable
data class OceanScoresInput(
    @SerialName("O") val O: Double,
    @SerialName("C") val C: Double,
    @SerialName("E") val E: Double,
    @SerialName("A") val A: Double,
    @SerialName("N") val N: Double,
)

@Serializable
data class OceanDto(
    @SerialName("user_id") val userId: String,
    val scores: OceanScores,
)

@Serializable
data class OceanResponse(
    val message: String,
    val data: OceanDto,
)

// ── Requests ─────────────────────────────────────────────────────────────────

@Serializable
data class CreateOceanRequest(
    @SerialName("user_id") val userId: String,
    val scores: OceanScoresInput,
)

@Serializable
data class UpdateOceanRequest(
    val scores: OceanScoresInput,
)

// ── API calls ─────────────────────────────────────────────────────────────────

/** POST /big-five */
suspend fun createOcean(accessToken: String, request: CreateOceanRequest): OceanResponse {
    AppLogger.i("Ocean", "createOcean userId=${request.userId}")
    try {
        val resp = httpClient.post("$BASE_URL/big-five") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Ocean", "createOcean failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Ocean", "createOcean error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /big-five/user/{userId} */
suspend fun getOcean(accessToken: String, userId: String): OceanDto {
    AppLogger.i("Ocean", "getOcean userId=$userId")
    try {
        val resp = httpClient.get("$BASE_URL/big-five/user/$userId") {
            header("Authorization", "Bearer $accessToken")
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Ocean", "getOcean failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Ocean", "getOcean error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** PUT /big-five/user/{userId} */
suspend fun updateOcean(accessToken: String, userId: String, request: UpdateOceanRequest): OceanResponse {
    AppLogger.i("Ocean", "updateOcean userId=$userId")
    try {
        val resp = httpClient.put("$BASE_URL/big-five/user/$userId") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Ocean", "updateOcean failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Ocean", "updateOcean error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
