package com.vodang.greenmind.location

import com.vodang.greenmind.time.currentTimeMillis

data class Location(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val timestampMillis: Long = currentTimeMillis()
)
