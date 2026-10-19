package com.vodang.greenmind.qr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
actual fun QrCodeScannerScreen(
    onScanResult: (result: String) -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center,
    ) {
        Text("QR Code scanning is not yet available on iOS.")
    }
}
