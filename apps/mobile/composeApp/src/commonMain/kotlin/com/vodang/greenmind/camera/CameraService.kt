package com.vodang.greenmind.camera

import kotlinx.coroutines.flow.Flow

import com.vodang.greenmind.time.currentTimeMillis

data class Photo(val bytes: ByteArray, val timestampMillis: Long = currentTimeMillis())

data class Frame(val bytes: ByteArray, val width: Int, val height: Int, val timestampMillis: Long = currentTimeMillis())

expect class CameraService() {
    val photoFlow: Flow<Photo>
    val frameFlow: Flow<Frame>
    fun initialize(platformContext: Any?)
    fun startPreview()
    fun stopPreview()
    suspend fun takePhoto(): Photo?
}

object Camera {
    val service: CameraService = CameraService()
}
