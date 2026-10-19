package com.vodang.greenmind.meal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.MealRecord
import com.vodang.greenmind.store.MealStore

private val green800 = Color(0xFF2E7D32)
private val green50 = Color(0xFFE8F5E9)

private fun ratioColor(ratio: Int): Color = when {
    ratio >= 70 -> green800
    ratio >= 40 -> Color(0xFFFB8C00)
    else -> Color(0xFFC62828)
}

@Composable
fun MealListScreen(onScanClick: () -> Unit, onCardClick: (MealRecord) -> Unit = {}) {
    val s = LocalAppStrings.current
    val meals by MealStore.meals.collectAsState()

    if (meals.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Filled.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = Color(0xFFBDBDBD)
                    )
                    Text(s.mealListEmpty, color = Color.Gray, fontSize = 15.sp)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(green800)
                            .clickable { onScanClick() }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Text(s.mealScanTitle, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            items(meals, key = { it.id }) { meal ->
                MealRecordCard(meal, onClick = { onCardClick(meal) })
            }
        }
    }
}

@Composable
private fun MealRecordCard(meal: MealRecord, onClick: () -> Unit) {
    val s = LocalAppStrings.current
    val color = ratioColor(meal.plantRatio)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text("${meal.plantRatio}%", color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(meal.description, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1B5E20))
                Text(s.mealPlantRatio(meal.plantRatio), fontSize = 12.sp, color = color)
            }
        }
    }
}
