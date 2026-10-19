package com.vodang.greenmind.wastesort.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.households.DetectTrashHistoryDto
import com.vodang.greenmind.householdwaste.components.GroupedDetectScanCard
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.wastesort.green600
import com.vodang.greenmind.wastesort.green800

@Composable
fun WasteSortList(
    apiHistory: List<DetectTrashHistoryDto> = emptyList(),
    isLoadingHistory: Boolean = false,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onApiScanClick: (List<DetectTrashHistoryDto>) -> Unit = {},
) {
    val s = LocalAppStrings.current
    var fabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Mini FABs — shown when expanded
                AnimatedVisibility(
                    visible = fabExpanded,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        MiniFabRow(
                            icon = "🖼",
                            label = s.uploadImage,
                            onClick = { fabExpanded = false; onGalleryClick() },
                        )
                        MiniFabRow(
                            icon = "📷",
                            label = s.takePhoto,
                            onClick = { fabExpanded = false; onCameraClick() },
                        )
                    }
                }

                // Main + FAB
                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = green600,
                    contentColor = Color.White,
                    shape = CircleShape,
                ) {
                    Text(
                        if (fabExpanded) s.fabCollapse else s.fabExpand,
                        fontSize = if (fabExpanded) 20.sp else 28.sp,
                        fontWeight = FontWeight.Light,
                    )
                }
            }
        },
        containerColor = Color(0xFFF5F5F5),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 24.dp),
        ) {
            // ── Server scan history section ───────────────────────────────
            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        s.myScanReportsTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1B),
                    )
                    if (isLoadingHistory) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = green800,
                        )
                    } else {
                        val groupCount = apiHistory.groupBy { it.imageUrl }.size
                        Text(
                            s.reportCount(groupCount),
                            fontSize = 12.sp,
                            color = Color.Gray,
                        )
                    }
                }
            }

            if (!isLoadingHistory) {
                val grouped = apiHistory.groupBy { it.imageUrl }
                if (grouped.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                s.noScanReports,
                                fontSize = 13.sp,
                                color = Color.Gray,
                            )
                        }
                    }
                } else {
                    items(grouped.entries.toList(), key = { it.key }) { (_, records) ->
                        GroupedDetectScanCard(
                            records = records,
                            onClick = { onApiScanClick(records) },
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun MiniFabRow(icon: String, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(label, fontSize = 13.sp, color = Color(0xFF1B1B1B), fontWeight = FontWeight.Medium)
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(green800),
            contentAlignment = Alignment.Center,
        ) {
            Text(icon, fontSize = 18.sp)
        }
    }
}
