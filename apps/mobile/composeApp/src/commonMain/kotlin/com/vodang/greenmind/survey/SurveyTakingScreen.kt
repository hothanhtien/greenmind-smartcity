package com.vodang.greenmind.survey

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.survey.SurveyAnswerDto
import com.vodang.greenmind.api.survey.SurveyDetailDto
import com.vodang.greenmind.api.survey.SurveyQuestionDto
import com.vodang.greenmind.components.AppScaffold
import com.vodang.greenmind.home.components.BottomNavTab
import com.vodang.greenmind.home.components.UserType
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.SettingsStore

private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF388E3C)
private val green50  = Color(0xFFE8F5E9)
private val greenBg  = Color(0xFFF1F8E9)

@Composable
fun SurveyTakingScreen(
    survey: SurveyDetailDto,
    onBack: () -> Unit,
    onSubmit: (List<SurveyAnswerDto>) -> Unit
) {
    val s = LocalAppStrings.current
    val questions = survey.questions
    var currentIndex by remember { mutableIntStateOf(0) }
    var submitted by remember { mutableStateOf(false) }

    // answers[questionId] = list of selected options (or single text)
    val answers = remember { mutableStateMapOf<String, List<String>>() }

    if (submitted) {
        SurveyThankYouScreen(title = survey.title, onDone = onBack)
        return
    }

    val question = questions[currentIndex]
    val progress = (currentIndex + 1).toFloat() / questions.size

    val user by SettingsStore.user.collectAsState()
    val userType = UserType.HOUSEHOLD

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(greenBg)
    ) {
        // Custom header
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = green800)
            }
            Spacer(Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(survey.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = green800)
                Text(s.surveyQuestion(currentIndex + 1, questions.size), fontSize = 12.sp, color = Color.Gray)
            }
        }

        // Progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = green600,
            trackColor = Color(0xFFE0E0E0)
        )

        // Question content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = question.text,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray,
                        lineHeight = 22.sp
                    )
                    if (question.type == "multiple_choice") {
                        Spacer(Modifier.height(4.dp))
                        Text(s.surveySelectMultiple, fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            when (question.type) {
                "single_choice" -> SingleChoiceOptions(
                    options = question.options,
                    selected = answers[question.id]?.firstOrNull(),
                    onSelect = { answers[question.id] = listOf(it) }
                )
                "multiple_choice" -> MultipleChoiceOptions(
                    options = question.options,
                    selected = answers[question.id] ?: emptyList(),
                    onToggle = { option ->
                        val current = answers[question.id]?.toMutableList() ?: mutableListOf()
                        if (option in current) current.remove(option) else current.add(option)
                        answers[question.id] = current
                    }
                )
                "text" -> TextAnswerInput(
                    value = answers[question.id]?.firstOrNull() ?: "",
                    placeholder = s.surveyTypeAnswer,
                    onValueChange = { answers[question.id] = listOf(it) }
                )
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (currentIndex > 0) {
                OutlinedButton(
                    onClick = { currentIndex-- },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = green800)
                ) {
                    Text(s.surveyPrev, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Spacer(Modifier.weight(1f))
            }

            val isLast = currentIndex == questions.lastIndex
            Button(
                onClick = {
                    if (isLast) {
                        val result = questions.map { q ->
                            val selected = answers[q.id] ?: emptyList()
                            if (q.type == "text") SurveyAnswerDto(q.id, textValue = selected.firstOrNull())
                            else SurveyAnswerDto(q.id, selectedOptions = selected)
                        }
                        onSubmit(result)
                        submitted = true
                    } else {
                        currentIndex++
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = green800)
            ) {
                Text(
                    if (isLast) s.surveySubmit else s.surveyNext,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SingleChoiceOptions(
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = option == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) green50 else Color.White)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) green600 else Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelect(option) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onSelect(option) },
                    colors = RadioButtonDefaults.colors(selectedColor = green800),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(option, fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}

@Composable
private fun MultipleChoiceOptions(
    options: List<String>,
    selected: List<String>,
    onToggle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = option in selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) green50 else Color.White)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) green600 else Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onToggle(option) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggle(option) },
                    colors = CheckboxDefaults.colors(checkedColor = green800),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(option, fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}

@Composable
private fun TextAnswerInput(value: String, placeholder: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        placeholder = { Text(placeholder, color = Color.LightGray) },
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = green600,
            unfocusedBorderColor = Color(0xFFE0E0E0),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@Composable
private fun SurveyThankYouScreen(title: String, onDone: () -> Unit) {
    val s = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(greenBg)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Check,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = green800
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = s.surveySubmitted,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = green800,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = s.surveySubmittedDesc,
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onDone,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = green800),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(s.surveyBackToList, fontWeight = FontWeight.SemiBold)
        }
    }
}
