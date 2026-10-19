package com.vodang.greenmind.bill

import androidx.compose.runtime.Composable
import com.vodang.greenmind.api.bill.BillAnalysisResult

expect @Composable fun BillScanScreen(
    onScanComplete: (result: BillAnalysisResult, storeName: String, imageUrl: String?) -> Unit,
    onBack: () -> Unit,
)
