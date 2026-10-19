package com.vodang.greenmind.time

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSTimeZone
import platform.Foundation.timeZoneWithName

actual fun nowIso8601(): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    formatter.timeZone = NSTimeZone.timeZoneWithName("UTC")!!
    return formatter.stringFromDate(NSDate())
}
