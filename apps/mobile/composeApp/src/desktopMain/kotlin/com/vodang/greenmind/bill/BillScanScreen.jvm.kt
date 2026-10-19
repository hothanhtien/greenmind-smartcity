package com.vodang.greenmind.bill

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.bill.BillAnalysisResult

@Composable
actual fun BillScanScreen(
    onScanComplete: (result: BillAnalysisResult, storeName: String, imageUrl: String?) -> Unit,
    onBack: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("🧾", fontSize = 48.sp)
            Text("Bill scan is not available on desktop.")
            Button(onClick = onBack) { Text("Back") }
        }
    }
}
