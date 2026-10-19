package com.vodang.greenmind.camera

import android.content.Context
import android.graphics.ImageFormat
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume

// Shared mutable state between CameraService and CameraPreview composable
internal object CameraServiceInternal {
    var surfaceProvider: androidx.camera.core.Preview.SurfaceProvider? = null
}

actual class CameraService actual constructor() {
    private val _photoFlow = MutableSharedFlow<Photo>(replay = 1)
    actual val photoFlow: Flow<Photo> = _photoFlow

    private val _frameFlow = MutableSharedFlow<Frame>(replay = 1)
    actual val frameFlow: Flow<Frame> = _frameFlow

    private var context: Context? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var cameraProvider: ProcessCameraProvider? = null
    internal var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var cameraExecutor: ExecutorService? = null

    actual fun initialize(platformContext: Any?) {
        context = platformContext as? Context
        lifecycleOwner = platformContext as? LifecycleOwner
    }

    actual fun startPreview() {
        val ctx = context ?: return
        cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindUseCases()
        }, ContextCompat.getMainExecutor(ctx))
    }

    private fun bindUseCases() {
        val ctx = context ?: return
        val owner = lifecycleOwner ?: return
        val provider = cameraProvider ?: return
        provider.unbindAll()

        val preview = Preview.Builder().build().also { p ->
            CameraServiceInternal.surfaceProvider?.let { p.setSurfaceProvider(it) }
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalyzer?.setAnalyzer(cameraExecutor!!) { imageProxy ->
            try {
                val bytes = imageProxyToJpeg(imageProxy)
                val frame = Frame(bytes, imageProxy.width, imageProxy.height, System.currentTimeMillis())
                _frameFlow.tryEmit(frame)
            } catch (_: Exception) {}
            imageProxy.close()
        }

        try {
            provider.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture, imageAnalyzer)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    actual fun stopPreview() {
        cameraProvider?.unbindAll()
        cameraExecutor?.shutdown()
        cameraExecutor = null
    }

    actual suspend fun takePhoto(): Photo? = suspendCancellableCoroutine { cont ->
        val capture = imageCapture ?: run { cont.resume(null); return@suspendCancellableCoroutine }
        val executor = cameraExecutor ?: Executors.newSingleThreadExecutor()
        capture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                try {
                    val bytes = imageProxyToJpeg(image)
                    val photo = Photo(bytes, System.currentTimeMillis())
                    _photoFlow.tryEmit(photo)
                    cont.resume(photo)
                } catch (e: Exception) {
                    cont.resume(null)
                } finally {
                    image.close()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                cont.resume(null)
            }
        })
    }

    private fun imageProxyToJpeg(image: ImageProxy): ByteArray {
        val plane = image.planes[0]
        val buffer: ByteBuffer = plane.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        // If format is JPEG, planes[0] contains JPEG bytes
        if (image.format == ImageFormat.JPEG) return bytes

        // Otherwise, convert YUV to NV21 then compress to JPEG
        val nv21 = yuv420888ToNv21(image)
        val yuvImage = android.graphics.YuvImage(nv21, android.graphics.ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, image.width, image.height), 80, out)
        return out.toByteArray()
    }

    private fun yuv420888ToNv21(image: ImageProxy): ByteArray {
        val width = image.width
        val height = image.height
        val ySize = width * height
        val uvSize = width * height / 4
        val nv21 = ByteArray(ySize + uvSize * 2)

        val yBuffer = image.planes[0].buffer // Y
        val uBuffer = image.planes[1].buffer // U
        val vBuffer = image.planes[2].buffer // V

        var rowStride = image.planes[0].rowStride
        var pos = 0
        val y = ByteArray(yBuffer.remaining())
        yBuffer.get(y)
        System.arraycopy(y, 0, nv21, 0, y.size)
        pos += y.size

        val u = ByteArray(uBuffer.remaining())
        val v = ByteArray(vBuffer.remaining())
        uBuffer.get(u)
        vBuffer.get(v)

        // NV21 format is VU interleaved
        var i = 0
        while (i < u.size && pos + 1 < nv21.size) {
            nv21[pos++] = v[i]
            nv21[pos++] = u[i]
            i++
        }

        return nv21
    }
}
