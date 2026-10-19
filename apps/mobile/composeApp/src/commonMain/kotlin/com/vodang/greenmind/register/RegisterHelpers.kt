package com.vodang.greenmind.register

import kotlin.math.floor

private const val THANH_HOA_LAT = 19.8
private const val PHU_YEN_LAT = 13.1

fun classifyVietnamRegionByLatitude(latitude: Double): String {
    return when {
        latitude >= THANH_HOA_LAT -> "North"
        latitude >= PHU_YEN_LAT -> "Central"
        else -> "South"
    }
}

fun pickerMillisToIsoDate(millis: Long): String {
    val epochDay = floor(millis.toDouble() / 86_400_000.0).toLong()
    val (year, month, day) = epochDayToCivilDate(epochDay)
    return "%04d-%02d-%02d".format(year, month, day)
}

private fun epochDayToCivilDate(epochDay: Long): Triple<Int, Int, Int> {
    var z = epochDay + 719468
    val era = if (z >= 0) z / 146097 else (z - 146096) / 146097
    val doe = z - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = doy - (153 * mp + 2) / 5 + 1
    val m = mp + if (mp < 10) 3 else -9
    val year = y + if (m <= 2) 1 else 0
    return Triple(year.toInt(), m.toInt(), d.toInt())
}
