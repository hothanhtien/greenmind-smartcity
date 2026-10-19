package com.vodang.greenmind.survey

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.survey.QuestionSetDto
import com.vodang.greenmind.api.survey.SurveyAnswerDto
import com.vodang.greenmind.api.survey.SurveyDetailDto
import com.vodang.greenmind.api.survey.SurveyQuestionDto
import com.vodang.greenmind.api.survey.SubmitUserAnswersRequest
import com.vodang.greenmind.api.survey.UserAnswerItem
import com.vodang.greenmind.api.survey.getQuestionSets
import com.vodang.greenmind.api.survey.submitUserAnswers
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.util.AppLogger
import kotlinx.coroutines.launch

private val green800 = Color(0xFF2E7D32)
private val greenBg  = Color(0xFFF1F8E9)

private fun QuestionSetDto.toSurveyDetail(): SurveyDetailDto = SurveyDetailDto(
    id = id,
    title = name,
    description = description,
    questions = items.map { item ->
        SurveyQuestionDto(
            id = item.id,
            text = item.question,
            type = if (item.questionOptions.isNotEmpty()) "single_choice" else "text",
            options = item.questionOptions.sortedBy { it.order }.map { it.text }
        )
    }
)

/**
 * Builds a lookup: questionId → (optionText → optionValue).
 * Used to convert the displayed label back to the value the backend expects.
 */
private fun QuestionSetDto.buildOptionValueMap(): Map<String, Map<String, String>> =
    items.associate { item ->
        item.id to item.questionOptions.associate { opt -> opt.text to opt.value }
    }

private fun List<SurveyAnswerDto>.toUserAnswerItems(
    optionValueMap: Map<String, Map<String, String>>
): List<UserAnswerItem> =
    mapNotNull { dto ->
        val raw = dto.textValue ?: dto.selectedOptions.firstOrNull() ?: return@mapNotNull null
        // Map display text → stored value; fall back to raw text if no mapping found
        val answer = optionValueMap[dto.questionId]?.get(raw) ?: raw
        UserAnswerItem(questionId = dto.questionId, answer = answer)
    }

@Composable
fun SurveyScreen() {
    var sets by remember { mutableStateOf<List<QuestionSetDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedSet by remember { mutableStateOf<QuestionSetDto?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val token = SettingsStore.getAccessToken()
        if (token != null) {
            try {
                sets = getQuestionSets(token).data
            } catch (e: Throwable) {
                error = e.message ?: "Failed to load question sets"
            }
        }
        isLoading = false
    }

    val selected = selectedSet
    if (selected != null) {
        SurveyTakingScreen(
            survey = selected.toSurveyDetail(),
            onBack = { selectedSet = null },
            onSubmit = { answers ->
                val optionValueMap = selected.buildOptionValueMap()
                scope.launch {
                    // Read fresh from store inside the coroutine to avoid stale captures
                    val token = SettingsStore.getAccessToken()
                    val userId = SettingsStore.getUser()?.id
                    if (token != null && userId != null) {
                        try {
                            submitUserAnswers(
                                accessToken = token,
                                request = SubmitUserAnswersRequest(
                                    userId = userId,
                                    answers = answers.toUserAnswerItems(optionValueMap)
                                )
                            )
                            AppLogger.i("Survey", "Submitted ${answers.size} answers for set ${selected.id}")
                        } catch (e: Throwable) {
                            AppLogger.e("Survey", "Submit failed: ${e.message}")
                        }
                    } else {
                        AppLogger.e("Survey", "Submit skipped — token=$token userId=$userId")
                    }
                }
            }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(greenBg)) {
            when {
                isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = green800)
                }

                error != null -> Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error!!,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                else -> SurveyListScreen(
                    sets = sets,
                    onSelectSet = { selectedSet = it }
                )
            }
    }
}
