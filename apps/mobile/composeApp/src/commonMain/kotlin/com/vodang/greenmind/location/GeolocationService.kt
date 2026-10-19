package com.vodang.greenmind.location

import kotlinx.coroutines.flow.Flow

expect class GeolocationService() {
    val locationUpdates: Flow<Location>
    fun initialize(platformContext: Any?)
    fun start()
    fun stop()
}
