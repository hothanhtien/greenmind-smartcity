package com.vodang.greenmind.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.fmt
import kotlin.math.roundToInt

private val blue600 = Color(0xFF1976D2)

@Composable
fun HouseholdElectricityCard() {
    val s = LocalAppStrings.current
    val values = listOf(3.8f, 4.1f, 3.5f, 5.2f, 4.8f, 6.1f, 4.2f)
    val labels = s.weekDays

    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Lightbulb, contentDescription = null, modifier = Modifier.size(22.dp), tint = blue600)
            Spacer(Modifier.width(8.dp))
            Text(s.electricityWeekTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            Text("kWh", fontSize = 11.sp, color = Color.Gray)
        }
        Spacer(Modifier.height(12.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }
            val n = values.size
            val topPadPx = with(density) { 24.dp.toPx() }
            val bottomPadPx = with(density) { 20.dp.toPx() }
            val chartH = heightPx - topPadPx - bottomPadPx
            val maxVal = values.max()
            val minVal = values.min() * 0.85f
            val range = maxVal - minVal

            fun xOf(i: Int) = widthPx * (i + 0.5f) / n
            fun yOf(v: Float) = topPadPx + chartH * (1f - (v - minVal) / range)

            val pts = values.mapIndexed { i, v -> Offset(xOf(i), yOf(v)) }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val fillPath = Path().apply {
                    moveTo(pts[0].x, pts[0].y)
                    for (i in 1 until pts.size) {
                        val midX = (pts[i - 1].x + pts[i].x) / 2f
                        cubicTo(midX, pts[i - 1].y, midX, pts[i].y, pts[i].x, pts[i].y)
                    }
                    lineTo(pts.last().x, heightPx - bottomPadPx)
                    lineTo(pts.first().x, heightPx - bottomPadPx)
                    close()
                }
                drawPath(fillPath, brush = Brush.verticalGradient(listOf(blue600.copy(alpha = 0.25f), blue600.copy(alpha = 0.02f)), startY = topPadPx, endY = heightPx - bottomPadPx))

                val linePath = Path().apply {
                    moveTo(pts[0].x, pts[0].y)
                    for (i in 1 until pts.size) {
                        val midX = (pts[i - 1].x + pts[i].x) / 2f
                        cubicTo(midX, pts[i - 1].y, midX, pts[i].y, pts[i].x, pts[i].y)
                    }
                }
                drawPath(linePath, color = blue600, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

                pts.forEach { pt ->
                    drawCircle(Color.White, radius = 5.dp.toPx(), center = pt)
                    drawCircle(blue600, radius = 4.5.dp.toPx(), center = pt, style = Stroke(width = 2.dp.toPx()))
                }
            }

            values.forEachIndexed { i, v ->
                val xPx = xOf(i)
                val yPx = yOf(v)
                Text(
                    text = v.fmt(1),
                    modifier = Modifier.offset { IntOffset(x = (xPx - 12.dp.toPx()).roundToInt(), y = (yPx - 20.dp.toPx()).roundToInt()) },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = blue600
                )
            }

            Row(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                labels.forEach { label -> Text(label, fontSize = 10.sp, color = Color.Gray) }
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(s.electricityWeekSummary, fontSize = 11.sp, color = Color.Gray)
    }
}
