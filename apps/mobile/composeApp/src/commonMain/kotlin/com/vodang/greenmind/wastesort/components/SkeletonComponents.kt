package com.vodang.greenmind.wastesort.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val shimmerBase = Color(0xFFE0E0E0)
private val shimmerHighlight = Color(0xFFF5F5F5)

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    fixedWidth: Dp? = null,
    height: Dp = 12.dp,
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )
    val brush = Brush.linearGradient(
        colors = listOf(shimmerBase, shimmerHighlight, shimmerBase),
        start = Offset(translateAnim - 500f, 0f),
        end = Offset(translateAnim, 0f),
    )

    Box(
        modifier = modifier
            .then(
                if (fixedWidth != null) Modifier.width(fixedWidth) else Modifier
            )
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .background(brush),
    )
}

@Composable
fun SkeletonEcoScoreCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SkeletonBox(fixedWidth = 60.dp, height = 10.dp)
                Spacer(Modifier.height(2.dp))
                SkeletonBox(fixedWidth = 70.dp, height = 28.dp)
                Spacer(Modifier.height(2.dp))
                SkeletonBox(fixedWidth = 40.dp, height = 10.dp)
            }
            SkeletonBox(fixedWidth = 56.dp, height = 56.dp)
        }
    }
}

@Composable
fun SkeletonCategoryCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SkeletonBox(fixedWidth = 100.dp, height = 14.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonBox(fixedWidth = 80.dp, height = 28.dp)
            SkeletonBox(fixedWidth = 80.dp, height = 28.dp)
        }
        // Grid skeleton
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) {
                SkeletonBox(modifier = Modifier.weight(1f), height = 80.dp)
            }
        }
    }
}

@Composable
fun LoadingDotsRow() {
    val transition = rememberInfiniteTransition(label = "dots")
    val dots by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Restart,
        ),
        label = "dotsAnim",
    )
    val dotsStr = when (dots.toInt()) {
        0    -> "."
        1    -> ".."
        else -> "..."
    }
    Text(
        text = "Loading$dotsStr",
        color = Color.Gray,
        fontSize = 12.sp,
    )
}
