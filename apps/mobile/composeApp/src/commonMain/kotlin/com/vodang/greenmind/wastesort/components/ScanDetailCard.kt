package com.vodang.greenmind.wastesort.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.unit.times
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import com.vodang.greenmind.api.households.GreenScoreEntryDto
import com.vodang.greenmind.api.households.bringOutDetectTrash
import com.vodang.greenmind.api.households.getGreenScoreHistory
import com.vodang.greenmind.api.households.submitGreenScoreByDetectId
import com.vodang.greenmind.store.HouseholdStore
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.store.WasteSortStore
import com.vodang.greenmind.wastereport.NetworkImage
import com.vodang.greenmind.wastesort.WasteSortEntry
import com.vodang.greenmind.wastesort.WasteSortStatus
import com.vodang.greenmind.wastesort.label
import com.vodang.greenmind.wastesort.categoryBg
import com.vodang.greenmind.wastesort.categoryColor
import com.vodang.greenmind.wastesort.categoryEmoji
import com.vodang.greenmind.wastesort.categoryLabel
import com.vodang.greenmind.util.AppLogger
import com.vodang.greenmind.i18n.LocalAppStrings
import kotlinx.coroutines.launch

private val stepDone    = Color(0xFF2E7D32)
private val stepPending = Color(0xFFBDBDBD)
private val stepBg      = Color(0xFFE8F5E9)

internal val STEPS = listOf(
    WasteSortStatus.SORTED,
    WasteSortStatus.BRINGOUTED,
    WasteSortStatus.COLLECTED,
)

// ── Lifecycle progress bar ────────────────────────────────────────────────────

@Composable
internal fun LifecycleProgressBar(currentStatus: WasteSortStatus, modifier: Modifier = Modifier) {
    val currentIdx = STEPS.indexOf(currentStatus)
    // 0.0 at first step → 1.0 at last step
    val targetFraction = currentIdx.toFloat() / (STEPS.size - 1).toFloat()
    val animFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(500),
        label = "progressFill",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // ── Track + dots ──────────────────────────────────────────────────────
        // Use BoxWithConstraints so the track starts/ends at the exact column
        // centers, keeping dots and labels in the same equal-weight columns.
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(26.dp)) {
            val colWidth   = maxWidth / STEPS.size
            val trackStart = colWidth / 2          // center of first dot column
            val trackLen   = maxWidth - colWidth   // first-center to last-center

            // Gray track
            Box(
                modifier = Modifier
                    .offset(x = trackStart, y = 10.dp)
                    .width(trackLen)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(stepPending.copy(alpha = 0.25f)),
            )
            // Green fill (animated from first dot center)
            Box(
                modifier = Modifier
                    .offset(x = trackStart, y = 10.dp)
                    .width(trackLen * animFraction)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(stepDone),
            )
            // Dots — one per equal-weight column, centered
            Row(modifier = Modifier.fillMaxWidth().height(26.dp)) {
                STEPS.forEachIndexed { idx, _ ->
                    val done   = idx <= currentIdx
                    val active = idx == currentIdx
                    val dotColor by animateColorAsState(
                        targetValue = if (done) stepDone else stepPending.copy(alpha = 0.45f),
                        animationSpec = tween(400),
                        label = "dot$idx",
                    )
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (active) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(stepDone.copy(alpha = 0.15f)),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(if (active) 14.dp else 10.dp)
                                .clip(CircleShape)
                                .background(dotColor),
                        )
                    }
                }
            }
        }

        // ── Labels — same equal-weight columns as dots ────────────────────────
        Row(modifier = Modifier.fillMaxWidth()) {
            STEPS.forEachIndexed { idx, step ->
                val done   = idx <= currentIdx
                val active = idx == currentIdx
                Text(
                    text = step.label,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp),
                    fontSize = 10.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    color = if (done) stepDone else stepPending,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ── Eco score card ────────────────────────────────────────────────────────────

@Composable
internal fun EcoScoreCard(latestEntry: GreenScoreEntryDto?) {
    val s = LocalAppStrings.current
    val green700 = Color(0xFF2E7D32)
    val green50  = Color(0xFFE8F5E9)
    val red600   = Color(0xFFDC2626)
    val red50    = Color(0xFFFEF2F2)
    val gray400  = Color(0xFF9CA3AF)
    val gray700  = Color(0xFF374151)

    val hasDetail = latestEntry != null &&
        (!latestEntry.items.isNullOrEmpty() || !latestEntry.reasons.isNullOrEmpty())
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
    ) {
        // ── Summary row ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (hasDetail) Modifier.clickable { expanded = !expanded } else Modifier)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(s.ecoScore, fontSize = 12.sp, color = gray400)
                    if (hasDetail) {
                        Text(if (expanded) "▲" else "▼", fontSize = 9.sp, color = gray400)
                    }
                }
                if (latestEntry == null) {
                    Text(s.noEcoScore, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = gray400)
                    Text(s.noScoreYet, fontSize = 11.sp, color = gray400)
                } else {
                    Text(
                        "${latestEntry.finalScore}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = green700,
                    )
                    val deltaColor  = if (latestEntry.delta >= 0) green700 else red600
                    val deltaStr = s.deltaPrefix(latestEntry.delta)
                    Text(
                        deltaStr,
                        fontSize = 11.sp,
                        color = deltaColor,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            latestEntry == null          -> Color(0xFFF5F5F5)
                            latestEntry.finalScore >= 50 -> green50
                            else                         -> red50
                        }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    when {
                        latestEntry == null              -> "🌱"
                        latestEntry.finalScore >= 70     -> "🌟"
                        latestEntry.finalScore >= 40     -> "🌿"
                        else                             -> "⚠️"
                    },
                    fontSize = 24.sp,
                )
            }
        }

        // ── Expanded detail ───────────────────────────────────────────────────
        if (expanded && latestEntry != null) {
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Items
                if (!latestEntry.items.isNullOrEmpty()) {
                    Text(s.detectedItems, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = gray700)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        latestEntry.items.forEach { item ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("• ${item.name}", fontSize = 12.sp, color = gray700, modifier = Modifier.weight(1f))
                                Text("×${item.quantity}", fontSize = 12.sp, color = gray400, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // Reasons
                if (!latestEntry.reasons.isNullOrEmpty()) {
                    if (!latestEntry.items.isNullOrEmpty()) HorizontalDivider(color = Color(0xFFEEEEEE))
                    Text(s.scoreBreakdown, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = gray700)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        latestEntry.reasons.forEach { reason ->
                            val isPositive = reason.contains("→ +") || reason.contains("(tốt)")
                            val isNegative = reason.contains("→ -") || reason.contains("(gây hại)")
                            val textColor = when {
                                isPositive -> green700
                                isNegative -> red600
                                else       -> gray700
                            }
                            Text("• $reason", fontSize = 12.sp, color = textColor, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

// ── Action button ─────────────────────────────────────────────────────────────

@Composable
private fun ActionButton(
    entry: WasteSortEntry,
    onStatusChange: (WasteSortStatus) -> Unit,
) {
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()
    var isBusy by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (entry.status) {
            WasteSortStatus.SORTED -> {
                Button(
                    onClick = {
                        val backendId = entry.backendId
                        if (backendId == null) {
                            // No backend record — advance locally only
                            onStatusChange(WasteSortStatus.BRINGOUTED)
                            return@Button
                        }
                        isBusy = true
                        errorMsg = null
                        scope.launch {
                            try {
                                val token = SettingsStore.getAccessToken()
                                    ?: throw IllegalStateException("Not signed in")
                                bringOutDetectTrash(token, backendId)
                                onStatusChange(WasteSortStatus.BRINGOUTED)
                            } catch (e: Throwable) {
                                AppLogger.e("ScanDetail", "bring-out failed: ${e.message}")
                                errorMsg = e.message ?: "Failed to update status"
                            } finally {
                                isBusy = false
                            }
                        }
                    },
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Updating…", fontWeight = FontWeight.Bold)
                    } else {
                        Text(s.markAsBroughtOut, fontWeight = FontWeight.Bold)
                    }
                }
                errorMsg?.let {
                    Text(it, color = Color(0xFFD32F2F), fontSize = 12.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                }
            }

            WasteSortStatus.BRINGOUTED -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        s.waitingForCollector,
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            WasteSortStatus.COLLECTED -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(stepBg)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        s.collectedComplete,
                        fontSize = 14.sp,
                        color = stepDone,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

// ── Main screen ───────────────────────────────────────────────────────────────

@Composable
fun ScanDetailCard(
    entry: WasteSortEntry,
    onBack: () -> Unit,
    onStatusChange: (WasteSortStatus) -> Unit = {},
) {
    val s = LocalAppStrings.current
    val entries by WasteSortStore.entries.collectAsState()

    // Always read from live store entry so background updates flow in
    val liveEntry = entries.find { it.id == entry.id } ?: entry

    val categories = liveEntry.grouped.keys.toList()
    var selectedCategory by remember(liveEntry.id) {
        mutableStateOf(categories.firstOrNull() ?: "")
    }
    var greenScoreLoading by remember { mutableStateOf(liveEntry.greenScoreResult == null) }

    // Fetch green score using detectId, fall back to history
    LaunchedEffect(liveEntry.backendId, liveEntry.id) {
        val backendId = liveEntry.backendId ?: return@LaunchedEffect
        val token = SettingsStore.getAccessToken() ?: return@LaunchedEffect

        try {
            val resp = submitGreenScoreByDetectId(token, backendId)
            WasteSortStore.updateGreenScore(liveEntry.id, resp.data)
            AppLogger.i("ScanDetail", "submitGreenScoreByDetectId ok id=${resp.data.id}")
        } catch (e: Throwable) {
            AppLogger.e("ScanDetail", "submitGreenScoreByDetectId failed: ${e.message}")
            // Fallback: try history
            try {
                val historyResp = getGreenScoreHistory(token)
                val latest = historyResp.data.lastOrNull()
                if (latest != null) {
                    WasteSortStore.updateGreenScore(liveEntry.id, latest)
                }
            } catch (e2: Throwable) {
                AppLogger.e("ScanDetail", "getGreenScoreHistory fallback failed: ${e2.message}")
            }
        } finally {
            greenScoreLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Scrollable content
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                contentPadding = PaddingValues(bottom = 8.dp),
            ) {
                // Lifecycle progress bar
                item {
                    LifecycleProgressBar(currentStatus = liveEntry.status)
                    HorizontalDivider(color = Color(0xFFE0E0E0))
                }

                // Eco score — skeleton while loading
                item {
                    if (greenScoreLoading && liveEntry.greenScoreResult == null) {
                        SkeletonEcoScoreCard()
                    } else {
                        EcoScoreCard(latestEntry = liveEntry.greenScoreResult)
                    }
                    HorizontalDivider(color = Color(0xFFE0E0E0))
                }

                // Detected / annotated image with back button
                item {
                    Box {
                        NetworkImage(
                            url = liveEntry.imageUrl,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3f),
                        )
                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f))
                                .clickable(onClick = onBack),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(s.backArrow, fontSize = 18.sp, color = Color.White)
                        }
                    }
                }

                // Category tabs + segment grid (right under annotated image)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            s.byCategory,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B1B1B),
                        )
                        if (liveEntry.grouped.isNotEmpty()) {
                            // Category tab pills — scrollable on small screens
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                categories.forEach { cat ->
                                    val count    = liveEntry.grouped[cat]?.size ?: 0
                                    val selected = cat == selectedCategory
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(
                                                if (selected) categoryColor(cat) else categoryBg(cat)
                                            )
                                            .clickable { selectedCategory = cat }
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Icon(
                                                categoryEmoji(cat),
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = if (selected) Color.White else categoryColor(cat)
                                            )
                                            Text(
                                                s.categoryTab(categoryLabel(cat), count),
                                                fontSize = 13.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (selected) Color.White else categoryColor(cat),
                                            )
                                        }
                                    }
                                }
                            }

                            // Segment image grid
                            val imageUrls = liveEntry.grouped[selectedCategory] ?: emptyList()
                            SegmentGrid(imageUrls = imageUrls, category = selectedCategory)
                        } else {
                            Text("N/A", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }

            }

            // Sticky action bar at bottom
            HorizontalDivider(color = Color(0xFFE0E0E0))
            ActionButton(entry = liveEntry, onStatusChange = onStatusChange)
        }
    }
}
