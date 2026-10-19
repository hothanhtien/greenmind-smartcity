package com.vodang.greenmind.preappsurvey

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.preappsurvey.PreAppSurveyAnswers
import com.vodang.greenmind.api.preappsurvey.SubmitPreAppSurveyRequest
import com.vodang.greenmind.api.preappsurvey.submitPreAppSurvey
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.time.nowIso8601
import kotlinx.coroutines.launch

// ── Design tokens ─────────────────────────────────────────────────────────────
private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF388E3C)
private val green400 = Color(0xFF66BB6A)
private val green50  = Color(0xFFE8F5E9)
private val surfaceColor = Color(0xFFF9FBF9)

// ── Main screen ───────────────────────────────────────────────────────────────

@Composable
fun PreAppSurveyScreen(onCompleted: () -> Unit = {}) {
    val s = LocalAppStrings.current
    val isVi = s.langCode == "vi"
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var currentStep by remember { mutableStateOf(0) }
    val answers = remember { mutableStateMapOf<String, String>() }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isDone by remember { mutableStateOf(false) }

    val total = questions.size

    if (isDone) {
        DoneScreen(s = s, onContinue = onCompleted)
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(surfaceColor)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Progress indicator (inside content) ──────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = s.preAppSurveyStep(currentStep + 1, total),
                        fontSize = 13.sp,
                        color = Color.Gray,
                    )
                    Text(
                        text = "${((currentStep + 1) * 100 / total)}%",
                        fontSize = 13.sp,
                        color = green800,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (currentStep + 1).toFloat() / total },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = green800,
                    trackColor = Color(0xFFE0E0E0),
                )
            }

            // ── Question card ─────────────────────────────────────────────────
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    val forward = targetState > initialState
                    (slideInHorizontally(tween(300)) { if (forward) it else -it } + fadeIn(tween(300))) togetherWith
                        (slideOutHorizontally(tween(300)) { if (forward) -it else it } + fadeOut(tween(300)))
                },
                modifier = Modifier.weight(1f),
                label = "QuestionSlide"
            ) { step ->
                val q = questions[step]
                val selected = answers[q.key]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Spacer(Modifier.height(4.dp))
                    // Emoji + question text
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = green50),
                        elevation = CardDefaults.cardElevation(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(q.emoji, fontSize = 32.sp)
                            Text(
                                text = if (isVi) q.questionVi else q.questionEn,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = green800,
                                lineHeight = 22.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (q.isNumericInput) {
                        // Free numeric text input
                        OutlinedTextField(
                            value = selected ?: "",
                            onValueChange = { v ->
                                // only allow digits
                                if (v.all { it.isDigit() }) answers[q.key] = v
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    if (isVi) q.inputHintVi else q.inputHintEn,
                                    color = Color.Gray
                                )
                            },
                            suffix = {
                                Text(
                                    if (isVi) q.inputSuffixVi else q.inputSuffixEn,
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = green800,
                                unfocusedBorderColor = Color(0xFFBDBDBD),
                                focusedLabelColor = green800,
                            ),
                        )
                    } else {
                        // Option selection rows
                        q.options.forEach { opt ->
                            val isSelected = selected == opt.value
                            OptionRow(
                                emoji = opt.emoji,
                                label = if (isVi) viOptionLabel(q.key, opt.value) else opt.label,
                                isSelected = isSelected,
                                onClick = { answers[q.key] = opt.value }
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }

            // ── Error msg ─────────────────────────────────────────────────────
            if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    color = Color(0xFFC62828),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // ── Navigation buttons ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStep > 0) {
                    OutlinedButton(
                        onClick = { currentStep--; errorMsg = null },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = green800),
                    ) {
                        Text(s.preAppSurveyPrev)
                    }
                }

                val isLast = currentStep == total - 1
                val currentQ = questions[currentStep]
                val currentKey = currentQ.key
                val rawAnswer = answers[currentKey]
                val hasAnswer = if (currentQ.isNumericInput)
                    rawAnswer != null && rawAnswer.isNotBlank() && (rawAnswer.toLongOrNull() ?: 0L) > 0L
                else
                    rawAnswer != null

                Button(
                    onClick = {
                        if (!hasAnswer) {
                            errorMsg = s.pleaseSelectAnswer
                            return@Button
                        }
                        errorMsg = null
                        if (isLast) {
                            scope.launch {
                                isSubmitting = true
                                try {
                                    val token = SettingsStore.getAccessToken() ?: ""
                                    val userId = SettingsStore.getUser()?.id ?: ""
                                    val ans = PreAppSurveyAnswers(
                                        daily_spending     = answers["daily_spending"]     ?: "50000",
                                        spending_variation = answers["spending_variation"] ?: "3",
                                        brand_trial        = answers["brand_trial"]        ?: "3",
                                        shopping_list      = answers["shopping_list"]      ?: "3",
                                        daily_distance     = answers["daily_distance"]     ?: "3",
                                        new_places         = answers["new_places"]         ?: "3",
                                        public_transport   = answers["public_transport"]   ?: "3",
                                        stable_schedule    = answers["stable_schedule"]    ?: "3",
                                        night_outings      = answers["night_outings"]      ?: "1",
                                        healthy_eating     = answers["healthy_eating"]     ?: "3",
                                        social_media       = answers["social_media"]       ?: "3",
                                        goal_setting       = answers["goal_setting"]       ?: "3",
                                        mood_swings        = answers["mood_swings"]        ?: "3",
                                    )
                                    submitPreAppSurvey(
                                        accessToken = token,
                                        request = SubmitPreAppSurveyRequest(
                                            userId = userId,
                                            answers = ans,
                                            isCompleted = true,
                                            completedAt = nowIso8601(),
                                        )
                                    )
                                    isDone = true
                                } catch (e: ApiException) {
                                    errorMsg = e.message.ifBlank { s.errorSubmitting }
                                } catch (e: Throwable) {
                                    errorMsg = s.networkError
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        } else {
                            currentStep++
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasAnswer) green800 else green50,
                        contentColor = if (hasAnswer) Color.White else green600,
                    ),
                    enabled = !isSubmitting,
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(if (isLast) s.preAppSurveySubmit else s.preAppSurveyNext, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ── Option row ────────────────────────────────────────────────────────────────

@Composable
private fun OptionRow(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) green800 else Color.White
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 2.dp else 0.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(emoji, fontSize = 22.sp)
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color(0xFF1B1B1B),
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = green800)
                }
            }
        }
    }
}