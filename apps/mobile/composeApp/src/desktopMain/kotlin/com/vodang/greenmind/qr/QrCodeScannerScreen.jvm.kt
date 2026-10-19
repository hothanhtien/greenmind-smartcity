package com.vodang.greenmind.qr

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun QrCodeScannerScreen(
    onScanResult: (result: String) -> Unit,
    onBack: () -> Unit,
) {
    // QR scanning needs a camera — not available on desktop
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("QR scanner not available on desktop")
    }
}
