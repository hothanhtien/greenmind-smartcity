package com.vodang.greenmind.api.location

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
data class LocationUserDto(
    val id: String,
    val username: String,
    val email: String,
    val phoneNumber: String? = null,
    val fullName: String,
    val gender: String,
    val location: String,
    val region: String,
    val role: String,
    val householdId: String? = null,
    val dateOfBirth: String,
    val segmentId: String? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LocationDto(
    val id: String,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val type: String,
    @SerialName("location_name") val locationName: String? = null,
    val lengthToPreviousLocation: Double? = null,
    val createdAt: String,
    val updatedAt: String,
)

/** LocationDto with nested user — returned by POST /locations. */
@Serializable
data class LocationWithUserDto(
    val id: String,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val type: String,
    @SerialName("location_name") val locationName: String? = null,
    val lengthToPreviousLocation: Double? = null,
    val user: LocationUserDto,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class DistanceTodayDto(
    @SerialName("total_distance") val totalDistance: Double,
    val user: LocationUserDto,
)

// ── Requests ─────────────────────────────────────────────────────────────────

@Serializable
data class CreateLocationRequest(
    val userId: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("length_to_previous_location") val lengthToPreviousLocation: Double? = null,
)

// ── Responses ─────────────────────────────────────────────────────────────────

@Serializable
data class CreateLocationResponse(
    val message: String,
    val data: LocationWithUserDto,
)

@Serializable
data class GetLocationsResponse(
    val message: String,
    val data: LocationDto,
)

@Serializable
data class GetLatestLocationResponse(
    val message: String,
    val data: LocationDto,
)

@Serializable
data class GetDistanceTodayResponse(
    val message: String,
    val data: DistanceTodayDto,
)

// ── API calls ─────────────────────────────────────────────────────────────────

/** POST /locations */
suspend fun createLocation(accessToken: String, request: CreateLocationRequest): CreateLocationResponse {
    AppLogger.i("Location", "createLocation lat=${request.latitude} lon=${request.longitude}")
    try {
        val resp = httpClient.post("$BASE_URL/locations") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Location", "createLocation failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Location", "createLocation error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /locations — returns the latest location for the authenticated user. */
suspend fun getLocationsByUser(accessToken: String): GetLocationsResponse {
    AppLogger.i("Location", "getLocationsByUser")
    try {
        val resp = httpClient.get("$BASE_URL/locations") {
            header("Authorization", "Bearer $accessToken")
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Location", "getLocationsByUser failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Location", "getLocationsByUser error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /locations/latest */
suspend fun getLatestLocation(accessToken: String): GetLatestLocationResponse {
    AppLogger.i("Location", "getLatestLocation")
    try {
        val resp = httpClient.get("$BASE_URL/locations/latest") {
            header("Authorization", "Bearer $accessToken")
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Location", "getLatestLocation failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Location", "getLatestLocation error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /locations/distanceToday */
suspend fun getDistanceToday(accessToken: String): GetDistanceTodayResponse {
    AppLogger.i("Location", "getDistanceToday")
    try {
        val resp = httpClient.get("$BASE_URL/locations/distanceToday") {
            header("Authorization", "Bearer $accessToken")
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Location", "getDistanceToday failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Location", "getDistanceToday error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
