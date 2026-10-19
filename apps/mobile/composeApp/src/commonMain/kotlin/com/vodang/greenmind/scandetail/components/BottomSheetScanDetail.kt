package com.vodang.greenmind.scandetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.households.bringOutDetectTrash
import com.vodang.greenmind.scandetail.DisplayMode
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.scandetail.ScanDetailData
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.util.AppLogger
import com.vodang.greenmind.wastesort.WasteSortStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetScanDetail(
    data: ScanDetailData,
    onDismiss: () -> Unit,
    onStatusChange: (WasteSortStatus) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()
    var isBusy by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var currentStatus by remember { mutableStateOf(data.status) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
        ) {
            ScanDetailView(
                data = data.copy(status = currentStatus),
                onBack = onDismiss,
                onStatusChange = { newStatus -> currentStatus = newStatus },
                displayMode = DisplayMode.BOTTOM_SHEET,
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider(color = Color(0xFFE0E0E0))

            // Status section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(s.status, fontSize = 14.sp, color = Color(0xFF374151))
                StatusChip(status = currentStatus)
            }

            // Action buttons
                when (currentStatus) {
                    WasteSortStatus.SORTED -> {
                        Button(
                            onClick = {
                                val backendId = data.backendId
                                if (backendId == null) {
                                    currentStatus = WasteSortStatus.BRINGOUTED
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
                                        currentStatus = WasteSortStatus.BRINGOUTED
                                        onStatusChange(WasteSortStatus.BRINGOUTED)
                                    } catch (e: Throwable) {
                                        AppLogger.e("BottomSheet", "bring-out failed: ${e.message}")
                                        errorMsg = e.message ?: "Failed"
                                    } finally {
                                        isBusy = false
                                    }
                                }
                            },
                            enabled = !isBusy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        ) {
                            if (isBusy) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text(s.updatingLabel, color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            } else {
                                Text(s.markAsBroughtOut, color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                        }
                        errorMsg?.let {
                            Text(it, color = Color(0xFFD32F2F), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                        }
                    }
                    WasteSortStatus.BRINGOUTED -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(s.waitingForCollector, fontSize = 14.sp, color = Color(0xFF757575), fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                    }
                }
                WasteSortStatus.COLLECTED -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(s.collectedComplete, fontSize = 14.sp, color = Color(0xFF2E7D32), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: WasteSortStatus) {
    val (bgColor, textColor, label) = when (status) {
        WasteSortStatus.SORTED -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "Sorted")
        WasteSortStatus.BRINGOUTED -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Brought Out")
        WasteSortStatus.COLLECTED -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Collected")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            color = textColor,
        )
    }
}