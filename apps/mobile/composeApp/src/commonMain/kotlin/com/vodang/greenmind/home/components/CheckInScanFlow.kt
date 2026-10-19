package com.vodang.greenmind.home.components

import androidx.compose.runtime.Composable

/**
 * Platform-specific full-screen flow.
 * Must always be in the composition tree so the activity-result launcher stays registered.
 * Pass a non-null [reportId] to activate the flow:
 *   1. Open camera
 *   2. Upload photo via requestAndUpload
 *   3. PATCH /waste-collector/{reportId}/status  { status: "done", imageEvidenceUrl }
 *
 * Calls [onSuccess] after the patch succeeds, [onDismiss] if the user cancels.
 * When [reportId] is null the composable is a no-op.
 */
@Composable
expect fun CheckInScanFlow(
    allPoints: List<WastePoint>,
    reportId: String?,
    accessToken: String,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
)
