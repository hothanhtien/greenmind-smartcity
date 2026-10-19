package com.vodang.greenmind.wasteanalytics.components

import com.vodang.greenmind.fmt
import com.vodang.greenmind.i18n.AppStrings
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.wasteanalytics.PeriodData
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// ── Palette ───────────────────────────────────────────────────────────────────
private val blue700   = Color(0xFF1565C0)
private val blue600   = Color(0xFF1976D2)
private val blue50    = Color(0xFFE3F2FD)
private val red700    = Color(0xFFC62828)
private val amber     = Color(0xFFF57F17)
private val teal600   = Color(0xFF00897B)
private val purple700 = Color(0xFF6A1B9A)

// ── Series data ───────────────────────────────────────────────────────────────

data class SeriesData(val label: String, val color: Color, val values: List<Float>)

// ── Legend item ───────────────────────────────────────────────────────────────

@Composable
fun LegendItem(
    label: String,
    color: Color,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val effectiveColor = if (enabled) color else Color(0xFFBDBDBD)
    Row(
        modifier = if (onClick != null) Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
        else Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(14.dp, 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(effectiveColor)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (enabled) Color(0xFF424242) else Color(0xFFBDBDBD),
            fontWeight = FontWeight.Normal,
            style = androidx.compose.ui.text.TextStyle(
                textDecoration = if (enabled) null else androidx.compose.ui.text.style.TextDecoration.LineThrough
            )
        )
    }
}

// ── Waste volume card (single-series smooth bezier area) ──────────────────────

@Composable
internal fun WasteVolumeCard(data: PeriodData) {
    val s = LocalAppStrings.current
    var enabled by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(blue50),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Delete, contentDescription = null, tint = blue700, modifier = Modifier.size(20.dp)) }
                Column(modifier = Modifier.weight(1f)) {
                    Text(s.wasteVolume, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                    Text(s.kilograms, fontSize = 11.sp, color = Color.Gray)
                }
                // Single-series toggle
                LegendItem(label = s.valueLabelKg(1.0), color = blue600, enabled = enabled, onClick = { enabled = !enabled })
            }

            if (!enabled) {
                Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                    Text(s.seriesHidden, fontSize = 12.sp, color = Color.Gray)
                }
            } else {
            val values = data.wasteKg
            val n = values.size

            BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                val density  = LocalDensity.current
                val widthPx  = with(density) { maxWidth.toPx() }
                val heightPx = with(density) { maxHeight.toPx() }
                val topPad   = with(density) { 24.dp.toPx() }
                val botPad   = with(density) { 20.dp.toPx() }
                val chartH   = heightPx - topPad - botPad
                val maxVal   = values.max()
                val minVal   = (values.min() * 0.85f).coerceAtLeast(0f)
                val range    = (maxVal - minVal).coerceAtLeast(0.01f)

                fun xOf(i: Int) = widthPx * (i + 0.5f) / n
                fun yOf(v: Float) = topPad + chartH * (1f - (v - minVal) / range)

                val pts = values.mapIndexed { i, v -> Offset(xOf(i), yOf(v)) }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Filled area (cubic bezier)
                    val fill = Path().apply {
                        moveTo(pts[0].x, pts[0].y)
                        for (i in 1 until pts.size) {
                            val mx = (pts[i - 1].x + pts[i].x) / 2f
                            cubicTo(mx, pts[i - 1].y, mx, pts[i].y, pts[i].x, pts[i].y)
                        }
                        lineTo(pts.last().x, heightPx - botPad)
                        lineTo(pts.first().x, heightPx - botPad)
                        close()
                    }
                    drawPath(fill, Brush.verticalGradient(
                        listOf(blue600.copy(alpha = 0.25f), blue600.copy(alpha = 0.02f)),
                        startY = topPad, endY = heightPx - botPad
                    ))
                    // Smooth line
                    val line = Path().apply {
                        moveTo(pts[0].x, pts[0].y)
                        for (i in 1 until pts.size) {
                            val mx = (pts[i - 1].x + pts[i].x) / 2f
                            cubicTo(mx, pts[i - 1].y, mx, pts[i].y, pts[i].x, pts[i].y)
                        }
                    }
                    drawPath(line, blue600, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                    // Dots
                    pts.forEachIndexed { i, pt ->
                        val isLast = i == pts.lastIndex
                        drawCircle(Color.White, 5.dp.toPx(), pt)
                        drawCircle(
                            if (isLast) blue700 else blue600,
                            if (isLast) 5.5.dp.toPx() else 4.5.dp.toPx(),
                            pt,
                            style = Stroke(2.dp.toPx())
                        )
                        if (isLast) drawCircle(blue600.copy(alpha = 0.18f), 10.dp.toPx(), pt)
                    }
                }

                // Value labels — only show if not too crowded (≤8 pts)
                if (n <= 8) {
                    values.forEachIndexed { i, v ->
                        val isLast = i == values.lastIndex
                        Text(
                            text = "${v.fmt(1)}kg",
                            modifier = Modifier.offset {
                                IntOffset(
                                    x = (xOf(i) - 14.dp.toPx()).roundToInt(),
                                    y = (yOf(v) - 20.dp.toPx()).roundToInt()
                                )
                            },
                            fontSize = 9.sp,
                            fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                            color = blue600
                        )
                    }
                }

                Row(
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val displayLabels = if (n <= 7) data.labels else {
                        (0 until 7).map { i ->
                            val idx = ((i.toFloat() * (n - 1).toFloat()) / 6f).roundToInt().coerceIn(0, n - 1)
                            data.labels[idx]
                        }
                    }
                    displayLabels.forEach { Text(it, fontSize = 9.sp, color = Color.Gray) }
                }
            }
            } // end else (enabled)
        }
    }
}

// ── Multi-series area chart card (shared for impact + pollutants) ─────────────

@Composable
fun MultiSeriesCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    seriesList: List<SeriesData>,
    xLabels: List<String>,
    defaultActiveCount: Int = seriesList.size,
) {
    val s = LocalAppStrings.current
    val textMeasurer = rememberTextMeasurer()
    val n = xLabels.size

    // Track which series are enabled; reset when the series list changes (period switch)
    var enabledLabels by remember(seriesList) { mutableStateOf(seriesList.take(defaultActiveCount).map { it.label }.toSet()) }
    val active = seriesList.filter { it.label in enabledLabels }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(seriesList.first().color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) { Icon(icon, contentDescription = null, tint = seriesList.first().color, modifier = Modifier.size(20.dp)) }
                Column {
                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                    Text(subtitle, fontSize = 11.sp, color = Color.Gray)
                }
            }

            // Clickable legend — tap to toggle each series
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Batch toggle controls
                LegendItem(label = "All", color = Color(0xFF2E7D32), enabled = enabledLabels.size == seriesList.size, onClick = {
                    enabledLabels = seriesList.map { it.label }.toSet()
                })
                LegendItem(label = "None", color = Color(0xFF9CA3AF), enabled = enabledLabels.isEmpty(), onClick = {
                    enabledLabels = emptySet()
                })
                seriesList.forEach { s ->
                    val enabled = s.label in enabledLabels
                    LegendItem(
                        label   = s.label,
                        color   = s.color,
                        enabled = enabled,
                        onClick = {
                            if (enabled && enabledLabels.size > 1) enabledLabels = enabledLabels - s.label
                            else if (!enabled)                      enabledLabels = enabledLabels + s.label
                        }
                    )
                }
            }

            if (active.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                    Text(s.noSeriesSelected, fontSize = 12.sp, color = Color.Gray)
                }
            } else {
                // Chart canvas
                Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                    val allValues = active.flatMap { it.values }
                    val maxVal = (allValues.maxOrNull() ?: 0f).coerceAtLeast(0.01f) * 1.25f
                    val w = size.width
                    val h = size.height
                    val padTop  = 28f
                    val padLeft = 36f
                    val chartW  = w - padLeft

                    fun xOf(i: Int) = padLeft + if (n <= 1) chartW / 2f else i * chartW / (n - 1).toFloat()
                    fun yOf(v: Float) = h - (v / maxVal) * (h - padTop)

                    // Y-axis ruler
                    val axisStyle = TextStyle(fontSize = 8.sp, color = Color(0xFF9E9E9E))
                    repeat(5) { g ->
                        val frac = g.toFloat() / 4f
                        val yG   = padTop + (1f - frac) * (h - padTop)
                        val tick = frac * maxVal
                        drawLine(Color(0xFFBDBDBD), Offset(padLeft - 4f, yG), Offset(padLeft, yG), 1f)
                        val lbl = "%.2f".fmt(tick)
                        val msr = textMeasurer.measure(lbl, axisStyle)
                        drawText(msr, topLeft = Offset((padLeft - 6f - msr.size.width).coerceAtLeast(0f), yG - msr.size.height / 2f))
                    }
                    drawLine(Color(0xFFBDBDBD), Offset(padLeft, padTop), Offset(padLeft, h), 1.5f)

                    // Grid
                    repeat(5) { g ->
                        drawLine(Color(0xFFEEEEEE), Offset(padLeft, padTop + g * (h - padTop) / 4f), Offset(w, padTop + g * (h - padTop) / 4f), 1f)
                    }

                    // Draw active series back-to-front
                    active.reversed().forEach { series ->
                        val vals = series.values
                        val c    = series.color

                        val area = Path().apply {
                            moveTo(xOf(0), h)
                            vals.forEachIndexed { i, v -> lineTo(xOf(i), yOf(v)) }
                            lineTo(xOf(vals.size - 1), h)
                            close()
                        }
                        drawPath(area, Brush.verticalGradient(listOf(c.copy(alpha = 0.28f), c.copy(alpha = 0.03f)), 0f, h))

                        val line = Path().apply {
                            vals.forEachIndexed { i, v -> if (i == 0) moveTo(xOf(i), yOf(v)) else lineTo(xOf(i), yOf(v)) }
                        }
                        drawPath(line, c, style = Stroke(width = 3f))

                        val labelStyle = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = c)
                        vals.forEachIndexed { i, v ->
                            val cx = xOf(i); val cy = yOf(v)
                            drawCircle(c, 5f, Offset(cx, cy))
                            drawCircle(Color.White, 3f, Offset(cx, cy))
                            if (n <= 7) {
                                val lbl = "%.2f".fmt(v)
                                val msr = textMeasurer.measure(lbl, labelStyle)
                                val lx = (cx - msr.size.width / 2f).coerceIn(0f, w - msr.size.width)
                                val ly = (cy - msr.size.height - 6f).coerceAtLeast(0f)
                                drawText(msr, topLeft = Offset(lx, ly))
                            }
                        }
                    }
                }

                // X-axis labels
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 36.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val displayLabels = if (n <= 7) xLabels else {
                        (0 until 7).map { i ->
                            val idx = ((i.toFloat() * (n - 1).toFloat()) / 6f).roundToInt().coerceIn(0, n - 1)
                            xLabels[idx]
                        }
                    }
                    displayLabels.forEach { Text(it, fontSize = 9.sp, color = Color.Gray) }
                }
            }
        }
    }
}
