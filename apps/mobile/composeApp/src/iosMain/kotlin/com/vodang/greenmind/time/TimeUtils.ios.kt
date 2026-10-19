package com.vodang.greenmind.time

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.localTimeZone
import platform.Foundation.timeZoneWithName

private fun parseIsoUtc(iso: String): NSDate? {
    val formatter = NSDateFormatter()
    formatter.timeZone = NSTimeZone.timeZoneWithName("UTC")!!
    formatter.locale = NSLocale("en_US_POSIX")
    // Try with millis first, then fallback without millis.
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
    )
    for (p in patterns) {
        formatter.dateFormat = p
        val parsed = formatter.dateFromString(iso)
        if (parsed != null) return parsed
    }
    return null
}

private fun formatLocal(date: NSDate, pattern: String): String {
    val formatter = NSDateFormatter()
    formatter.timeZone = NSTimeZone.localTimeZone
    formatter.locale = NSLocale("en_US_POSIX")
    formatter.dateFormat = pattern
    return formatter.stringFromDate(date)
}

actual fun formatDateTimeLocal(iso: String): String {
    val date = parseIsoUtc(iso) ?: return iso
    return formatLocal(date, "dd/MM/yyyy HH:mm")
}

actual fun formatChatTime(iso: String): String {
    val date = parseIsoUtc(iso) ?: return iso
    return formatLocal(date, "dd/MM HH:mm")
}

actual fun todayLocalIsoDate(): String {
    return formatLocal(NSDate(), "yyyy-MM-dd")
}
