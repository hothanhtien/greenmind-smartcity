package com.vodang.greenmind.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
actual fun CheckInScanFlow(
    allPoints: List<WastePoint>,
    reportId: String?,
    accessToken: String,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Text("Camera check-in is not available on iOS yet.", color = Color.Gray, fontSize = 15.sp)
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = Color(0xFFFFB300))
            }
        }
    }
}
