package com.vodang.greenmind.qr

import androidx.compose.runtime.Composable

expect @Composable fun QrCodeScannerScreen(
    onScanResult: (result: String) -> Unit,
    onBack: () -> Unit,
)
