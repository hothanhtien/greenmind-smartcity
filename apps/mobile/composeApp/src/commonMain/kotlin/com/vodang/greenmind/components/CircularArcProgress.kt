package com.vodang.greenmind.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun CircularArcProgress(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 8f,
    backgroundColor: Color = color.copy(alpha = 0.15f),
    startAngle: Float = 135f,
    sweepAngle: Float = 270f,
) {
    Canvas(modifier = modifier) {
        val diameter = size.minDimension
        val radius = (diameter - strokeWidth) / 2
        val topLeft = Offset(
            (size.width - diameter + strokeWidth) / 2,
            (size.height - diameter + strokeWidth) / 2
        )

        // Draw background arc
        drawArc(
            color = backgroundColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter - strokeWidth, diameter - strokeWidth),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Draw progress arc
        val progressSweep = sweepAngle * progress.coerceIn(0f, 1f)
        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = progressSweep,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter - strokeWidth, diameter - strokeWidth),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
