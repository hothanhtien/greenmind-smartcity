package com.vodang.greenmind.home.components

import com.vodang.greenmind.fmt
import com.vodang.greenmind.i18n.LocalAppStrings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── PollutionTrendCard ────────────────────────────────────────────────────────
// Area chart showing CO2, CH4, NOx over 1 month (7-day intervals).
// Mock data derived from the YOLO-detect API response shape:
//   pollution.CO2 ≈ 0.693, CH4 = 0.0, NOx = 0.0 at scan time.
// TODO: Replace mock series with rolling historical data from the API.
// ─────────────────────────────────────────────────────────────────────────────

private data class PollutionPoint(
    val label: String,
    val co2: Float,
    val ch4: Float,
    val nox: Float,
)

// 1-month window split into 7-day intervals (5 data points: Mar 1 → Mar 28).
private val mockPollution = listOf(
    PollutionPoint("Mar 1",  0.693f, 0.000f, 0.000f),
    PollutionPoint("Mar 7",  0.850f, 0.050f, 0.020f),
    PollutionPoint("Mar 14", 1.100f, 0.120f, 0.080f),
    PollutionPoint("Mar 21", 0.950f, 0.080f, 0.050f),
    PollutionPoint("Mar 28", 0.750f, 0.030f, 0.010f),
)

private val co2Color = Color(0xFFD32F2F)  // red
private val ch4Color = Color(0xFF1B5E20)  // dark green
private val noxColor = Color(0xFFF57F00)  // dark yellow

@Composable
fun PollutionTrendCard(modifier: Modifier = Modifier) {
    val s = LocalAppStrings.current
    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(22.dp), tint = Color(0xFFC62828))
                }
                Column {
                    Text(
                        s.pollutionTrendTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1B),
                    )
                    Text(
                        s.pollutionTrendSubtitle,
                        fontSize = 11.sp,
                        color = Color.Gray,
                    )
                }
            }

            // Legend
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LegendItem(s.co2Legend, co2Color)
                LegendItem(s.ch4Legend, ch4Color)
                LegendItem(s.noxLegend, noxColor)
            }

            // Area chart
            AreaChart(
                points = mockPollution,
                textMeasurer = textMeasurer,
                modifier = Modifier.fillMaxWidth().height(180.dp),
            )

            // X-axis labels — inset by the Y-axis ruler width (36dp)
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 36.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                mockPollution.forEach { point ->
                    Text(point.label, fontSize = 9.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(14.dp, 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
        Text(label, fontSize = 11.sp, color = Color(0xFF424242))
    }
}

@Composable
private fun AreaChart(
    points: List<PollutionPoint>,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    modifier: Modifier = Modifier,
) {
    val s = LocalAppStrings.current
    Canvas(modifier = modifier) {
        val n = points.size
        if (n == 0) return@Canvas

        val allValues = points.flatMap { listOf(it.co2, it.ch4, it.nox) }
        val maxVal = (allValues.maxOrNull() ?: 0f).coerceAtLeast(0.01f) * 1.25f

        val w = size.width
        val h = size.height
        // Extra top padding to give room for value labels above the highest dot
        val padTop = 28f
        // Left padding for Y-axis ruler
        val padLeft = 36f

        val chartW = w - padLeft

        fun xOf(i: Int): Float = padLeft + if (n <= 1) chartW / 2f else i * chartW / (n - 1).toFloat()
        fun yOf(v: Float): Float = h - (v / maxVal) * (h - padTop)

        // ── Y-axis ruler ──────────────────────────────────────────────────────
        val axisLabelStyle = TextStyle(fontSize = 8.sp, color = Color(0xFF9E9E9E))
        val tickCount = 4
        repeat(tickCount + 1) { g ->
            val fraction = g.toFloat() / tickCount
            val yG = padTop + (1f - fraction) * (h - padTop)
            val value = fraction * maxVal

            // Tick mark
            drawLine(
                color = Color(0xFFBDBDBD),
                start = Offset(padLeft - 4f, yG),
                end = Offset(padLeft, yG),
                strokeWidth = 1f,
            )

            // Value label (right-aligned before the axis)
            val label = s.percentYAxis
            val measured = textMeasurer.measure(label, axisLabelStyle)
            val lw = measured.size.width.toFloat()
            val lh = measured.size.height.toFloat()
            drawText(
                textLayoutResult = measured,
                topLeft = Offset((padLeft - 6f - lw).coerceAtLeast(0f), yG - lh / 2f),
            )
        }

        // Vertical axis line
        drawLine(
            color = Color(0xFFBDBDBD),
            start = Offset(padLeft, padTop),
            end = Offset(padLeft, h),
            strokeWidth = 1.5f,
        )

        // Horizontal grid lines
        repeat(5) { g ->
            val yG = padTop + g * (h - padTop) / 4f
            drawLine(
                color = Color(0xFFEEEEEE),
                start = Offset(padLeft, yG),
                end = Offset(w, yG),
                strokeWidth = 1f,
            )
        }

        // Series definition: draw back-to-front so CO2 (tallest) renders last / on top
        val series = listOf(
            Triple(points.map { it.nox }, noxColor, "NOx"),
            Triple(points.map { it.ch4 }, ch4Color, "CH4"),
            Triple(points.map { it.co2 }, co2Color, "CO2"),
        )

        series.forEach { (values, lineColor, _) ->
            // Filled area
            val areaPath = Path().apply {
                moveTo(xOf(0), h)
                values.forEachIndexed { i, v -> lineTo(xOf(i), yOf(v)) }
                lineTo(xOf(values.size - 1), h)
                close()
            }
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.30f), lineColor.copy(alpha = 0.04f)),
                    startY = 0f,
                    endY = h,
                ),
            )

            // Line on top
            val linePath = Path().apply {
                values.forEachIndexed { i, v ->
                    if (i == 0) moveTo(xOf(i), yOf(v)) else lineTo(xOf(i), yOf(v))
                }
            }
            drawPath(path = linePath, color = lineColor, style = Stroke(width = 3f))

            // Dots + value labels
            val labelStyle = TextStyle(
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = lineColor,
            )
            values.forEachIndexed { i, v ->
                val cx = xOf(i)
                val cy = yOf(v)

                // Dot
                drawCircle(color = lineColor, radius = 5f, center = Offset(cx, cy))
                drawCircle(color = Color.White, radius = 3f, center = Offset(cx, cy))

                // Value label — placed above the dot
                val label = "%.2f".fmt(v)
                val measured = textMeasurer.measure(label, labelStyle)
                val lw = measured.size.width.toFloat()
                val lh = measured.size.height.toFloat()
                // Horizontally centre the label on the dot; clamp to canvas edges
                val lx = (cx - lw / 2f).coerceIn(0f, w - lw)
                // Place label above the dot with a small gap; clamp so it never goes above canvas
                val ly = (cy - lh - 6f).coerceAtLeast(0f)
                drawText(textLayoutResult = measured, topLeft = Offset(lx, ly))
            }
        }
    }
}
