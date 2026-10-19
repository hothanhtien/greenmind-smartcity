package com.vodang.greenmind.survey

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.survey.QuestionSetDto
import com.vodang.greenmind.i18n.LocalAppStrings

private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF388E3C)
private val green50  = Color(0xFFE8F5E9)
private val greenBg  = Color(0xFFF1F8E9)

@Composable
fun SurveyListScreen(
    sets: List<QuestionSetDto>,
    onSelectSet: (QuestionSetDto) -> Unit
) {
    val s = LocalAppStrings.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(greenBg),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = s.surveys,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = green800
                )
                Text(
                    text = s.surveysDesc,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        items(sets) { set ->
            QuestionSetCard(
                set = set,
                onTap = { onSelectSet(set) }
            )
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun QuestionSetCard(set: QuestionSetDto, onTap: () -> Unit) {
    val s = LocalAppStrings.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(green50, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(s.surveyBadgeIcon, fontSize = 20.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = set.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )
                    Text(
                        text = s.surveyQuestions(set.items.size),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = set.description,
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 17.sp
            )

            Spacer(Modifier.height(10.dp))

            HorizontalDivider(color = Color(0xFFEEEEEE))

            Spacer(Modifier.height(10.dp))

            Text(
                text = s.surveyBy(set.owner.fullName),
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}
