package com.vodang.greenmind.api.survey

import com.vodang.greenmind.api.BASE_URL
import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.auth.ErrorResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

// ── Survey DTOs (used by SurveyTakingScreen) ─────────────────────────────────

@Serializable
data class SurveyDto(
    val id: String,
    val title: String,
    val description: String,
    val questionCount: Int,
    val estimatedMinutes: Int,
    val isCompleted: Boolean = false
)

@Serializable
data class SurveyDetailDto(
    val id: String,
    val title: String,
    val description: String,
    val questions: List<SurveyQuestionDto>
)

@Serializable
data class SurveyQuestionDto(
    val id: String,
    val text: String,
    val type: String, // "single_choice", "multiple_choice", "text"
    val options: List<String> = emptyList()
)

@Serializable
data class SurveySubmitRequest(
    val surveyId: String,
    val answers: List<SurveyAnswerDto>
)

@Serializable
data class SurveyAnswerDto(
    val questionId: String,
    val selectedOptions: List<String> = emptyList(),
    val textValue: String? = null
)

// ── Question Set DTOs ─────────────────────────────────────────────────────────

@Serializable
data class QuestionOptionDto(
    val id: String,
    val text: String,
    val value: String,
    val order: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class QuestionSetItemDto(
    val id: String,
    val question: String,
    val templateId: String,
    val behaviorInput: String,
    val behaviorNormalized: String,
    val normalizeScore: String? = null,
    val trait: String? = null,
    val ownerId: String,
    val createdAt: String,
    val updatedAt: String,
    val questionOptions: List<QuestionOptionDto> = emptyList(),
)

@Serializable
data class QuestionSetOwnerDto(
    val id: String,
    val username: String,
    val email: String,
    val fullName: String,
    val role: String,
)

@Serializable
data class QuestionSetDto(
    val id: String,
    val name: String,
    val description: String,
    val ownerId: String,
    val createdAt: String,
    val updatedAt: String,
    val owner: QuestionSetOwnerDto,
    val items: List<QuestionSetItemDto> = emptyList(),
)

@Serializable
data class GetQuestionSetsResponse(
    val message: String,
    val data: List<QuestionSetDto>,
    val count: Int,
)

// ── User Answer DTOs ─────────────────────────────────────────────────────────

@Serializable
data class UserAnswerItem(
    val questionId: String,
    val answer: String,
)

@Serializable
data class SubmitUserAnswersRequest(
    val userId: String,
    val answers: List<UserAnswerItem>,
)

@Serializable
data class SubmitUserAnswersResponse(
    val message: String,
    val totalAnswered: Int,
)

// ── API calls ─────────────────────────────────────────────────────────────────

/** POST /user-answers/submit */
suspend fun submitUserAnswers(accessToken: String, request: SubmitUserAnswersRequest): SubmitUserAnswersResponse {
    try {
        val resp = httpClient.post("$BASE_URL/user-answers/submit") {
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

/** GET /question-sets/my-sets */
suspend fun getQuestionSets(accessToken: String): GetQuestionSetsResponse {
    try {
        val resp = httpClient.get("$BASE_URL/question-sets/my-sets") {
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
