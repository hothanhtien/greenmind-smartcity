package com.vodang.greenmind.location

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

actual class GeolocationService actual constructor() {
    private val _updates = MutableSharedFlow<Location>(replay = 1)
    actual val locationUpdates: Flow<Location> = _updates.asSharedFlow()

    actual fun initialize(platformContext: Any?) {}
    actual fun start() {}
    actual fun stop() {}
}
