package com.vodang.greenmind.api.preappsurvey

import com.vodang.greenmind.api.BASE_URL
import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.auth.ErrorResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

// ── Request DTOs ──────────────────────────────────────────────────────────────

@Serializable
data class PreAppSurveyAnswers(
    val daily_spending: String,
    val spending_variation: String,
    val brand_trial: String,
    val shopping_list: String,
    val daily_distance: String,
    val new_places: String,
    val public_transport: String,
    val stable_schedule: String,
    val night_outings: String,
    val healthy_eating: String,
    val social_media: String,
    val goal_setting: String,
    val mood_swings: String,
)

@Serializable
data class SubmitPreAppSurveyRequest(
    val userId: String,
    val answers: PreAppSurveyAnswers,
    val isCompleted: Boolean,
    val completedAt: String,
)

// ── Response DTOs ─────────────────────────────────────────────────────────────

@Serializable
data class PreAppSurveyUserDto(
    val id: String,
    val username: String,
    val email: String,
    val phoneNumber: String? = null,
    val fullName: String,
    val gender: String,
    val location: String,
    val region: String,
    val role: String,
    val roleId: String? = null,
    val householdId: String? = null,
    val dateOfBirth: String,
    val segmentId: String? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class PreAppSurveyDto(
    val id: String,
    val userId: String,
    val dailySpending: String,
    val dailySpendingSigmoid: Double? = null,
    val dailySpendingWeight: Double? = null,
    val dailySpendingDirection: Double? = null,
    val dailySpendingAlpha: Double? = null,
    val spendingVariation: Int,
    val spendingVariationSigmoid: Double? = null,
    val spendingVariationWeight: Double? = null,
    val spendingVariationDirection: Double? = null,
    val spendingVariationAlpha: Double? = null,
    val brandTrial: Int,
    val brandTrialSigmoid: Double? = null,
    val brandTrialWeight: Double? = null,
    val brandTrialDirection: Double? = null,
    val brandTrialAlpha: Double? = null,
    val shoppingList: Int,
    val shoppingListSigmoid: Double? = null,
    val shoppingListWeight: Double? = null,
    val shoppingListDirection: Double? = null,
    val shoppingListAlpha: Double? = null,
    val dailyDistance: String,
    val dailyDistanceSigmoid: Double? = null,
    val dailyDistanceWeight: Double? = null,
    val dailyDistanceDirection: Double? = null,
    val dailyDistanceAlpha: Double? = null,
    val newPlaces: Int,
    val newPlacesSigmoid: Double? = null,
    val newPlacesWeight: Double? = null,
    val newPlacesDirection: Double? = null,
    val newPlacesAlpha: Double? = null,
    val publicTransport: Int,
    val publicTransportSigmoid: Double? = null,
    val publicTransportWeight: Double? = null,
    val publicTransportDirection: Double? = null,
    val publicTransportAlpha: Double? = null,
    val stableSchedule: Int,
    val stableScheduleSigmoid: Double? = null,
    val stableScheduleWeight: Double? = null,
    val stableScheduleDirection: Double? = null,
    val stableScheduleAlpha: Double? = null,
    val nightOutings: Int,
    val nightOutingsSigmoid: Double? = null,
    val nightOutingsWeight: Double? = null,
    val nightOutingsDirection: Double? = null,
    val nightOutingsAlpha: Double? = null,
    val healthyEating: Int,
    val healthyEatingSigmoid: Double? = null,
    val healthyEatingWeight: Double? = null,
    val healthyEatingDirection: Double? = null,
    val healthyEatingAlpha: Double? = null,
    val socialMedia: Int,
    val goalSetting: Int,
    val moodSwings: Int,
    val isCompleted: Boolean,
    val completedAt: String,
    val createdAt: String,
    val updatedAt: String,
    val user: PreAppSurveyUserDto? = null,
)

@Serializable
data class SubmitPreAppSurveyResponse(
    val message: String,
    val data: PreAppSurveyDto,
)

// ── API calls ─────────────────────────────────────────────────────────────────

/** POST /pre-app-survey/submit */
suspend fun submitPreAppSurvey(accessToken: String, request: SubmitPreAppSurveyRequest): SubmitPreAppSurveyResponse {
    try {
        val resp = httpClient.post("$BASE_URL/pre-app-survey/submit") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /pre-app-survey/{userId} */
suspend fun getPreAppSurveyByUser(accessToken: String, userId: String): PreAppSurveyDto {
    try {
        val resp = httpClient.get("$BASE_URL/pre-app-survey/$userId") {
            header("Authorization", "Bearer $accessToken")
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /pre-app-survey/parameters */
suspend fun updatePreAppParameters(accessToken: String) {
    try {
        val resp = httpClient.get("$BASE_URL/pre-app-survey/parameters") {
            header("Authorization", "Bearer $accessToken")
        }
        if (!resp.status.isSuccess()) {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        throw ApiException(0, e.message ?: "Network error")
    }
}
