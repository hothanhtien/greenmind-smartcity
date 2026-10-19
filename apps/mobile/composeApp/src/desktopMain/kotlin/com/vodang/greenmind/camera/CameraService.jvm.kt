package com.vodang.greenmind.camera

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

actual class CameraService actual constructor() {
    actual val photoFlow: Flow<Photo> = emptyFlow()
    actual val frameFlow: Flow<Frame> = emptyFlow()

    actual fun initialize(platformContext: Any?) {}

    actual fun startPreview() {}

    actual fun stopPreview() {}

    actual suspend fun takePhoto(): Photo? = null
}
