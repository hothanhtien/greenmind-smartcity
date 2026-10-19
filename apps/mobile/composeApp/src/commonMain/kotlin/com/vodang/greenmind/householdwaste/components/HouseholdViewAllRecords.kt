package com.vodang.greenmind.householdwaste.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.households.DetectTrashHistoryDto
import com.vodang.greenmind.api.households.GreenScoreEntryDto
import com.vodang.greenmind.api.wastereport.WasteReportDto
import com.vodang.greenmind.householdwaste.components.GroupedDetectScanCard
import com.vodang.greenmind.wastereport.WasteReportCard
import com.vodang.greenmind.i18n.LocalAppStrings

private val bgGray   = Color(0xFFF3F4F6)
private val green700 = Color(0xFF2E7D32)

enum class ViewAllMode {
    NONE, HOUSEHOLD_SCANS, GREEN_SCORES, USER_SCANS, WASTE_REPORTS
}

@Composable
fun ViewAllRecordsScreen(
    mode: ViewAllMode,
    householdGroups: List<List<DetectTrashHistoryDto>> = emptyList(),
    userGroups: List<List<DetectTrashHistoryDto>> = emptyList(),
    wasteReports: List<WasteReportDto> = emptyList(),
    greenScores: List<GreenScoreEntryDto> = emptyList(),
    onBack: () -> Unit,
    onReportClick: (WasteReportDto) -> Unit,
    onScanGroupClick: (List<DetectTrashHistoryDto>) -> Unit,
    onScoreClick: (GreenScoreEntryDto) -> Unit,
) {
    val s = LocalAppStrings.current
    var selectedScore by remember { mutableStateOf<GreenScoreEntryDto?>(null) }
    val title = when (mode) {
        ViewAllMode.HOUSEHOLD_SCANS -> s.scanHistoryScreen
        ViewAllMode.USER_SCANS -> s.myScanReports
        ViewAllMode.WASTE_REPORTS -> s.myWasteReports
        ViewAllMode.GREEN_SCORES -> s.scoreHistory
        else -> ""
    }

    Column(Modifier.fillMaxSize().background(bgGray)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (mode) {
                ViewAllMode.HOUSEHOLD_SCANS -> {
                    householdGroups.forEach { records ->
                        GroupedDetectScanCard(records, onClick = { onScanGroupClick(records) })
                    }
                }
                ViewAllMode.USER_SCANS -> {
                    userGroups.forEach { records ->
                        GroupedDetectScanCard(records, onClick = { onScanGroupClick(records) })
                    }
                }
                ViewAllMode.WASTE_REPORTS -> {
                    wasteReports.forEach { report ->
                        WasteReportCard(report, onClick = { onReportClick(report) })
                    }
                }
                ViewAllMode.GREEN_SCORES -> {
                    greenScores.reversed().forEach { score ->
                        ScoreEntryRow(score, onOpenDetail = { selectedScore = it })
                    }
                }
                else -> {}
            }
            Spacer(Modifier.navigationBarsPadding())
        }
    }

    selectedScore?.let { score ->
        GreenScoreDetailSheet(entry = score, onDismiss = { selectedScore = null })
    }
}