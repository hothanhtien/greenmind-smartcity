package com.vodang.greenmind.location

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

actual class GeolocationService actual constructor() {
    actual val locationUpdates: Flow<Location> = emptyFlow()

    actual fun initialize(platformContext: Any?) {}

    actual fun start() {}

    actual fun stop() {}
}
