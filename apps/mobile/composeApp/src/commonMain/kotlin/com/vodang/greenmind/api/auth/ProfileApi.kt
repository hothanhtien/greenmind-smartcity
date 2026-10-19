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

// ── Response ──────────────────────────────────────────────────────────────────

@Serializable
data class ProfileDto(
    val id: String,
    val username: String,
    val email: String,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("full_name") val fullName: String,
    val gender: String,
    val role: String,
    @SerialName("date_of_birth") val dateOfBirth: String,
    val age: Int,
    val location: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

// ── Mapping ──────────────────────────────────────────────────────────────────

fun ProfileDto.toUserDto() = UserDto(
    id = id,
    username = username,
    email = email,
    fullName = fullName,
    dateOfBirth = dateOfBirth,
    location = location,
    gender = gender,
    role = role,
    age = age,
)

// ── API call ──────────────────────────────────────────────────────────────────

/**
 * GET /auth/profile
 *
 * Requires a valid JWT access token.
 * Throws [ApiException] on 401 or other non-2xx responses.
 */
suspend fun getProfile(accessToken: String): ProfileDto {
    AppLogger.i("Profile", "getProfile")
    try {
        val resp = httpClient.get("$BASE_URL/auth/profile") {
            header("Authorization", "Bearer $accessToken")
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Profile", "getProfile failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Profile", "getProfile error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
