package com.vodang.greenmind.camera

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

actual class CameraService actual constructor() {
    private val _photoFlow = MutableSharedFlow<Photo>(replay = 1)
    actual val photoFlow: Flow<Photo> = _photoFlow

    private val _frameFlow = MutableSharedFlow<Frame>(replay = 1)
    actual val frameFlow: Flow<Frame> = _frameFlow

    actual fun initialize(platformContext: Any?) {}
    actual fun startPreview() {}
    actual fun stopPreview() {}
    actual suspend fun takePhoto(): Photo? = null
}
