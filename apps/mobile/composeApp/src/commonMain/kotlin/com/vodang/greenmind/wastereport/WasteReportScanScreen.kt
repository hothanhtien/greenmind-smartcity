package com.vodang.greenmind.wastereport

import androidx.compose.runtime.Composable

/** All data collected during the report flow, ready to send to the API. */
data class WasteReportFormData(
    val imageUrl: String,
    val description: String,
    val lat: Double,
    val lng: Double,
    val wardName: String,
)

@Composable
expect fun WasteReportScanScreen(
    onStartSubmit: (WasteReportFormData) -> Unit,
    onBack: () -> Unit,
    launchCamera: Boolean = true,
    onSubmitDone: () -> Unit = {},
    isSubmitting: Boolean = false,
)
