package com.vodang.greenmind.api.auth

import com.vodang.greenmind.api.BASE_URL
import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Request ──────────────────────────────────────────────────────────────────

@Serializable
data class RegisterEmailRequest(
    val email: String,
    val password: String,
    @SerialName("confirm_password") val confirmPassword: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("date_of_birth") val dateOfBirth: String,
    val location: String,
    val gender: String,
    val region: String,
)

// ── Response ──────────────────────────────────────────────────────────────────

@Serializable
data class RegisterEmailResponse(
    val message: String,
    val user: UserDto,
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val fullName: String,
    // The backend may omit some fields (age/dateOfBirth/region/gender). Make them optional.
    @SerialName("dateOfBirth") val dateOfBirth: String? = null,
    val location: String? = null,
    val region: String? = null,
    val gender: String? = null,
    val role: String,
    @SerialName("age") val age: Int? = null,
)

@Serializable
data class ErrorResponse(val message: String)

class ApiException(val code: Int, override val message: String) : Exception(message)

// ── API call ──────────────────────────────────────────────────────────────────

/**
 * POST /auth/register/email
 *
 * Throws [io.ktor.client.plugins.ResponseException] for 4xx/5xx responses.
 * Callers can catch and inspect [response.body<Map<String,String>>()]["message"]
 * for error details (e.g. "Email already exists" on 400).
 */
suspend fun registerWithEmail(request: RegisterEmailRequest): RegisterEmailResponse {
    AppLogger.i("Auth", "registerWithEmail email=${request.email}")
    try {
        val resp = httpClient.post("$BASE_URL/auth/register/email") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Auth", "registerWithEmail failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Auth", "registerWithEmail error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

// ── Login ─────────────────────────────────────────────────────────────────────

@Serializable
data class LoginEmailRequest(
    val email: String,
    val password: String,
)

@Serializable
data class LoginEmailResponse(
    val message: String? = null,
    val user: UserDto,
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
)

suspend fun loginWithEmail(request: LoginEmailRequest): LoginEmailResponse {
    AppLogger.i("Auth", "loginWithEmail email=${request.email}")
    try {
        val resp = httpClient.post("$BASE_URL/auth/login/email") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Auth", "loginWithEmail failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Auth", "loginWithEmail error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
