package com.vodang.greenmind.time

/**
 * Convert UTC ISO timestamp to device's local timezone.
 * Input:  "2026-05-03T10:30:00.000Z"
 * Output: "03/05/2026 17:30" (if device is in +07:00 timezone)
 */
expect fun formatDateTimeLocal(iso: String): String

/**
 * Format date only (dd/MM/yyyy) — no timezone conversion needed for dates.
 */
fun formatDateLocal(iso: String): String = try {
    val parts = iso.substringBefore('T').split('-')
    if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
} catch (_: Throwable) { iso }

/**
 * Format for chat bubble display: "03/05 17:30"
 */
expect fun formatChatTime(iso: String): String

/**
 * Returns today's date as ISO string (yyyy-MM-dd) in device local timezone.
 */
expect fun todayLocalIsoDate(): String
