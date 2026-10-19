package com.vodang.greenmind.time

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val LOCAL_ZONE = ZoneId.systemDefault()

actual fun formatDateTimeLocal(iso: String): String = try {
    val instant = Instant.parse(iso)
    val localDateTime = instant.atZone(LOCAL_ZONE).toLocalDateTime()
    localDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
} catch (_: Throwable) { iso }

actual fun formatChatTime(iso: String): String = try {
    val instant = Instant.parse(iso)
    val localDateTime = instant.atZone(LOCAL_ZONE).toLocalDateTime()
    localDateTime.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
} catch (_: Throwable) { iso }

actual fun todayLocalIsoDate(): String = LocalDate.now(LOCAL_ZONE).toString()
