package com.vodang.greenmind.meal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.MealRecord
import com.vodang.greenmind.wastereport.Base64Image
import com.vodang.greenmind.wastereport.NetworkImage

private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF388E3C)
private val green50  = Color(0xFFE8F5E9)
private val orange   = Color(0xFFE65100)
private val red      = Color(0xFFC62828)

private fun ratioColor(ratio: Int) = when {
    ratio >= 70 -> green800
    ratio >= 40 -> orange
    else        -> red
}

@Composable
fun MealDetailScreen(meal: MealRecord, onBack: () -> Unit) {
    val s     = LocalAppStrings.current
    val color = ratioColor(meal.plantRatio)

    val feedback = when {
        meal.plantRatio >= 70 -> s.mealRatioGood
        meal.plantRatio >= 40 -> s.mealRatioOk
        else                  -> s.mealRatioLow
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        // ── Hero image ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            when {
                !meal.plantImageBase64.isNullOrBlank() -> Base64Image(
                    base64   = meal.plantImageBase64,
                    modifier = Modifier.fillMaxSize()
                )
                !meal.imageUrl.isNullOrBlank() -> NetworkImage(
                    url      = meal.imageUrl,
                    modifier = Modifier.fillMaxSize()
                )
                else -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(green800, green600))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Restaurant, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.White.copy(alpha = 0.7f))
                }
            }

            // Scrim + back button
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text(s.backArrow, fontSize = 18.sp, color = Color.White)
            }

            // Ratio badge over image
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Text(
                        "${meal.plantRatio}%",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        s.plant,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // ── Content card ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Name
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        meal.description.ifBlank { s.unnamedMeal },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1B)
                    )
                    Text(
                        feedback,
                        fontSize = 13.sp,
                        color = color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Plant ratio card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        s.mealPlantRatio(meal.plantRatio),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = color
                    )
                    LinearProgressIndicator(
                        progress = { meal.plantRatio / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = color,
                        trackColor = color.copy(alpha = 0.15f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RatioThreshold(label = "Low",  threshold = s.thresholdLow,  active = meal.plantRatio < 40,  activeColor = red)
                        RatioThreshold(label = "OK",   threshold = s.thresholdOk,   active = meal.plantRatio in 40..69, activeColor = orange)
                        RatioThreshold(label = "Good", threshold = s.thresholdGood,  active = meal.plantRatio >= 70, activeColor = green800)
                    }
                }
            }

            // Tip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(green50)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    s.aimForPlant,
                    fontSize = 12.sp,
                    color = green800,
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun RatioThreshold(label: String, threshold: String, active: Boolean, activeColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (active) activeColor.copy(alpha = 0.12f) else Color(0xFFF5F5F5))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                color = if (active) activeColor else Color.Gray
            )
        }
        Text(threshold, fontSize = 10.sp, color = Color.Gray)
    }
}
