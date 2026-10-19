package com.vodang.greenmind.wastereport

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
actual fun WasteReportScanScreen(
    onStartSubmit: (WasteReportFormData) -> Unit,
    onBack: () -> Unit,
    launchCamera: Boolean,
    onSubmitDone: () -> Unit,
    isSubmitting: Boolean,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("📷", fontSize = 48.sp)
            Text("Waste Report scan is not available on desktop.")
            Button(onClick = onBack) { Text("Back") }
        }
    }
}
