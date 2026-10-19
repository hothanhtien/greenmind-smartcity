package com.vodang.greenmind.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.components.CircularArcProgress
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.OceanStore
import com.vodang.greenmind.theme.*

@Composable
fun OceanScoreCard() {
    val s = LocalAppStrings.current
    val scores by OceanStore.scores.collectAsState()
    val isLoading by OceanStore.isLoading.collectAsState()

    LaunchedEffect(Unit) { OceanStore.load() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(s.oceanTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            Spacer(Modifier.height(16.dp))

            if (isLoading && scores == null) {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green800, modifier = Modifier.size(28.dp))
                }
            } else {
                val scoreValues = scores?.let {
                    listOf(it.O, it.C, it.E, it.A, it.N)
                } ?: listOf(50, 50, 50, 50, 50)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    oceanColors.zip(scoreValues).forEach { (pair, score) ->
                        val (letter, color) = pair
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.size(56.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularArcProgress(
                                    progress = score / 100f,
                                    color = color,
                                    modifier = Modifier.fillMaxSize(),
                                    strokeWidth = 6f,
                                    backgroundColor = color.copy(alpha = 0.15f)
                                )
                                Text(
                                    letter,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "$score",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = color
                            )
                        }
                    }
                }
            }
        }
    }
}

private val oceanColors = listOf(
    "O" to OceanPurple,
    "C" to OceanBlue,
    "E" to OceanGreen,
    "A" to OceanOrange,
    "N" to OceanRed
)
