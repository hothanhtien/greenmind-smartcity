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
import com.vodang.greenmind.store.NetworkCaptureStore
import com.vodang.greenmind.store.NetworkEntry
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

private val green800 = Color(0xFF2E7D32)
private val green50  = Color(0xFFE8F5E9)

private val prettyJson = Json { prettyPrint = true }

private fun tryPrettyJson(text: String): String = try {
    prettyJson.encodeToString(JsonElement.serializer(), Json.parseToJsonElement(text))
} catch (_: Throwable) { text }

// ── Method badge colors ───────────────────────────────────────────────────────

private fun methodColor(method: String): Color = when (method.uppercase()) {
    "GET"    -> Color(0xFF1565C0)
    "POST"   -> Color(0xFF2E7D32)
    "PUT"    -> Color(0xFFE65100)
    "PATCH"  -> Color(0xFF6A1B9A)
    "DELETE" -> Color(0xFFC62828)
    else     -> Color(0xFF546E7A)
}

private fun statusColor(code: Int): Color = when {
    code in 200..299 -> Color(0xFF2E7D32)
    code in 300..399 -> Color(0xFF1565C0)
    code in 400..499 -> Color(0xFFE65100)
    code >= 500      -> Color(0xFFC62828)
    else             -> Color(0xFF9E9E9E)
}

private fun urlPath(url: String): String = try {
    val parsed = Url(url)
    parsed.encodedPath.ifBlank { "/" }
} catch (_: Throwable) { url }

// ── Public section composable ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkLogSection() {
    val entries by NetworkCaptureStore.entries.collectAsState()
    var detailEntry by remember { mutableStateOf<NetworkEntry?>(null) }

    // Section header row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "🌐  Network Log (${entries.size})",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF424242),
            modifier = Modifier.weight(1f),
        )
        if (entries.isNotEmpty()) {
            TextButton(
                onClick = { NetworkCaptureStore.clear() },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            ) {
                Text("Clear", fontSize = 12.sp, color = Color(0xFFB71C1C))
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
                    "No requests captured yet.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            } else {
                entries.asReversed().forEachIndexed { idx, entry ->
                    if (idx > 0) HorizontalDivider(color = Color(0xFFF0F0F0))
                    NetworkEntryRow(entry, onClick = { detailEntry = entry })
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
            NetworkEntryDetail(entry = selected)
        }
    }
}

// ── Entry row ─────────────────────────────────────────────────────────────────

@Composable
private fun NetworkEntryRow(entry: NetworkEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Method badge
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = methodColor(entry.method).copy(alpha = 0.12f),
        ) {
            Text(
                text = entry.method,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = methodColor(entry.method),
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            )
        }
        // Status badge
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = statusColor(entry.statusCode).copy(alpha = 0.12f),
        ) {
            Text(
                text = if (entry.statusCode == 0) "ERR" else entry.statusCode.toString(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor(entry.statusCode),
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            )
        }
        // Path
        Text(
            text = urlPath(entry.url),
            fontSize = 12.sp,
            color = Color(0xFF212121),
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
        // Duration
        Text(
            text = "${entry.durationMs}ms",
            fontSize = 11.sp,
            color = Color.Gray,
        )
        Text("›", fontSize = 16.sp, color = Color.LightGray)
    }
}

// ── Detail sheet ──────────────────────────────────────────────────────────────

@Composable
private fun NetworkEntryDetail(entry: NetworkEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(shape = RoundedCornerShape(6.dp), color = methodColor(entry.method).copy(alpha = 0.12f)) {
                Text(
                    entry.method,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = methodColor(entry.method),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            Surface(shape = RoundedCornerShape(6.dp), color = statusColor(entry.statusCode).copy(alpha = 0.12f)) {
                Text(
                    if (entry.statusCode == 0) "ERR" else entry.statusCode.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor(entry.statusCode),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            Text("${entry.durationMs}ms", fontSize = 12.sp, color = Color.Gray)
        }

        // URL
        CodeBlock(entry.url)

        HorizontalDivider(color = Color(0xFFEEEEEE))

        // Request section
        DetailSectionHeader("REQUEST")
        DetailSubHeader("Headers")
        if (entry.requestHeaders.isEmpty()) {
            Text("(none)", fontSize = 12.sp, color = Color.Gray)
        } else {
            HeadersBlock(entry.requestHeaders)
        }
        DetailSubHeader("Body")
        CodeBlock(tryPrettyJson(entry.requestBody).ifBlank { "(empty)" })

        HorizontalDivider(color = Color(0xFFEEEEEE))

        // Response section
        DetailSectionHeader("RESPONSE")
        DetailSubHeader("Headers")
        if (entry.responseHeaders.isEmpty()) {
            Text("(none)", fontSize = 12.sp, color = Color.Gray)
        } else {
            HeadersBlock(entry.responseHeaders)
        }
        DetailSubHeader("Body")
        CodeBlock(
            text = tryPrettyJson(entry.responseBody).ifBlank { "(empty)" },
            maxChars = 4000,
        )
    }
}

@Composable
private fun DetailSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = green800,
        letterSpacing = 1.sp,
    )
}

@Composable
private fun DetailSubHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF424242),
    )
}

@Composable
private fun HeadersBlock(headers: List<Pair<String, String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        headers.forEach { (key, value) ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "$key:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF546E7A),
                    fontFamily = FontFamily.Monospace,
                )
                Text(
                    text = value,
                    fontSize = 11.sp,
                    color = Color(0xFF212121),
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CodeBlock(text: String, maxChars: Int = Int.MAX_VALUE) {
    val display = if (text.length > maxChars) text.take(maxChars) + "\n…(truncated)" else text
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
