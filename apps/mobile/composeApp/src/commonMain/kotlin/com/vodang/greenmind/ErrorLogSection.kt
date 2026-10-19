package com.vodang.greenmind

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.store.ErrorLevel
import com.vodang.greenmind.store.ErrorLogEntry
import com.vodang.greenmind.store.ErrorLogStore
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.fmt

private val red700   = Color(0xFFC62828)
private val red50    = Color(0xFFFFEBEE)
private val amber700 = Color(0xFFF57F17)
private val amber50  = Color(0xFFFFF8E1)
private val gray700  = Color(0xFF424242)

// ── Timestamp formatting ──────────────────────────────────────────────────────

private fun formatTime(epochMs: Long): String {
    val totalSec = (epochMs / 1000L).toInt()
    val hh = (totalSec / 3600) % 24
    val mm = (totalSec / 60) % 60
    val ss = totalSec % 60
    return "%02d:%02d:%02d".fmt(hh, mm, ss)
}

// ── Public section composable ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorLogSection() {
    val s = LocalAppStrings.current
    val entries by ErrorLogStore.entries.collectAsState()
    var detailEntry by remember { mutableStateOf<ErrorLogEntry?>(null) }

    val errorCount   = entries.count { it.level == ErrorLevel.E }
    val warningCount = entries.count { it.level == ErrorLevel.W }

    // Section header row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = s.errorLog(entries.size),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = gray700,
            modifier = Modifier.weight(1f),
        )
        if (entries.isNotEmpty()) {
            TextButton(
                onClick = { ErrorLogStore.clear() },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            ) {
                Text(s.clear, fontSize = 12.sp, color = red700)
            }
        }
    }

    // Summary badges (only when there are entries)
    if (entries.isNotEmpty()) {
        Row(
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (errorCount > 0) {
                Surface(shape = RoundedCornerShape(6.dp), color = red50) {
                    Text(
                        text = "$errorCount error${if (errorCount != 1) "s" else ""}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = red700,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
            if (warningCount > 0) {
                Surface(shape = RoundedCornerShape(6.dp), color = amber50) {
                    Text(
                        text = "$warningCount warning${if (warningCount != 1) "s" else ""}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = amber700,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            if (entries.isEmpty()) {
                Text(
                    s.noErrors,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            } else {
                entries.asReversed().forEachIndexed { idx, entry ->
                    if (idx > 0) HorizontalDivider(color = Color(0xFFF0F0F0))
                    ErrorEntryRow(entry, onClick = { detailEntry = entry })
                }
            }
        }
    }

    // Detail bottom sheet
    val selected = detailEntry
    if (selected != null) {
        ModalBottomSheet(
            onDismissRequest = { detailEntry = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFFFAFAFA),
        ) {
            ErrorEntryDetail(entry = selected)
        }
    }
}

// ── Entry row ─────────────────────────────────────────────────────────────────

@Composable
private fun ErrorEntryRow(entry: ErrorLogEntry, onClick: () -> Unit) {
    val s = LocalAppStrings.current
    val levelColor = if (entry.level == ErrorLevel.E) red700 else amber700
    val levelBg    = if (entry.level == ErrorLevel.E) red50  else amber50
    val levelLabel = if (entry.level == ErrorLevel.E) s.errorLevel else s.warningLevel

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Level badge
        Surface(shape = RoundedCornerShape(4.dp), color = levelBg) {
            Text(
                text = levelLabel,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = levelColor,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            )
        }
        // Timestamp
        Text(
            text = formatTime(entry.timestampMs),
            fontSize = 11.sp,
            color = Color.Gray,
            fontFamily = FontFamily.Monospace,
        )
        // Tag + message
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.tag,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = gray700,
            )
            Text(
                text = entry.message,
                fontSize = 11.sp,
                color = Color(0xFF616161),
                maxLines = 1,
            )
        }
        // Expand indicator — only show when there is a stack trace
        if (entry.stackTrace != null) {
            Text(s.expandIndicator, fontSize = 16.sp, color = Color.LightGray)
        }
    }
}

// ── Detail sheet ──────────────────────────────────────────────────────────────

@Composable
private fun ErrorEntryDetail(entry: ErrorLogEntry) {
    val s = LocalAppStrings.current
    val levelColor = if (entry.level == ErrorLevel.E) red700 else amber700
    val levelBg    = if (entry.level == ErrorLevel.E) red50  else amber50

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Title row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(shape = RoundedCornerShape(6.dp), color = levelBg) {
                Text(
                    text = entry.level.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = levelColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            Text(
                text = formatTime(entry.timestampMs),
                fontSize = 12.sp,
                color = Color.Gray,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = entry.tag,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = gray700,
                modifier = Modifier.weight(1f),
            )
        }

        // Message
        ErrorDetailHeader(s.message)
        ErrorCodeBlock(entry.message)

        // Stack trace (if present)
        if (entry.stackTrace != null) {
            HorizontalDivider(color = Color(0xFFEEEEEE))
            ErrorDetailHeader(s.stackTrace)
            ErrorCodeBlock(entry.stackTrace, maxChars = 6000)
        }
    }
}

@Composable
private fun ErrorDetailHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = red700,
        letterSpacing = 1.sp,
    )
}

@Composable
private fun ErrorCodeBlock(text: String, maxChars: Int = Int.MAX_VALUE) {
    val s = LocalAppStrings.current
    val display = if (text.length > maxChars) text.take(maxChars) + "\n…${s.truncated}" else text
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF263238), RoundedCornerShape(8.dp))
            .padding(10.dp)
            .horizontalScroll(rememberScrollState()),
    ) {
        Text(
            text = display,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFCFD8DC),
            lineHeight = 16.sp,
        )
    }
}
