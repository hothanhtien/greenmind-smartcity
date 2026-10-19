---

## Camera module (brief)
 
 - Common API: `CameraService` (see `composeApp/src/commonMain/kotlin/com/vodang/greenmind/camera/CameraService.kt`)
 - Models: `Photo(bytes: ByteArray, timestampMillis: Long)` and `Frame(bytes: ByteArray, width: Int, height: Int, timestampMillis: Long)`
 - Android implementation uses CameraX and exposes:
   - `CameraPreview` composable (PreviewView wrapper)
   - `Camera.service.takePhoto()` suspend function returning `Photo?`
   - `Camera.service.frameFlow` that emits frames as `Frame` for custom rendering
 
 Files:
 - `composeApp/src/commonMain/kotlin/com/vodang/greenmind/camera/CameraService.kt`
 - `composeApp/src/androidMain/kotlin/com/vodang/greenmind/camera/AndroidCameraService.kt`
 - `composeApp/src/androidMain/kotlin/com/vodang/greenmind/camera/CameraPreview.kt`
 
 Notes:
 - You must request `android.permission.CAMERA` at runtime before starting preview or taking photos.
 - Frames are emitted as JPEG bytes (converted from ImageProxy). For custom rendering on Compose, decode bytes into `ImageBitmap` or use `android.graphics.BitmapFactory.decodeByteArray` on Android.
 - iOS implementation can be added similarly using `AVCaptureSession` and converting `CMSampleBuffer` to JPEG/bytes.
# Geolocation module — GreenMind

Tổng quan
- Module cung cấp API đa nền tảng (Kotlin Multiplatform) để lấy vị trí theo thời gian thực.
- Common API:
  - `Location(latitude: Double, longitude: Double, accuracy: Float?, timestampMillis: Long)`
  - `GeolocationService` (expect/actual) với: `val locationUpdates: Flow<Location>`, `initialize(platformContext: Any?)`, `start()`, `stop()`
  - `Geo.service` singleton để truy cập service từ common code.

Android
- Implementation: `composeApp/src/androidMain/kotlin/com/vodang/greenmind/location/LocationForegroundService.kt` và `AndroidGeolocationService.kt`.
- Sử dụng `FusedLocationProviderClient` (Play Services) với `Priority.PRIORITY_HIGH_ACCURACY` để đạt độ chính xác tốt nhất.
- Yêu cầu permissions (AndroidManifest):
  - `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`.
- Runtime permissions:
  - App yêu cầu `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` trước khi bắt đầu tracking. `ACCESS_BACKGROUND_LOCATION` phải được request **riêng** (không mix cùng lúc với foreground) theo chính sách Android.
- Lưu ý:
  - Trên Android 13+ cần đăng ký receiver với `RECEIVER_NOT_EXPORTED` hoặc tránh dùng implicit broadcasts. Module đã sử dụng một `MutableSharedFlow` trong `LocationForegroundService.companion` để chuyển dữ liệu từ Service sang code common, tránh vấn đề broadcast.
  - Foreground service có `android:foregroundServiceType="location"` trong manifest.

iOS
- Implementation: `composeApp/src/iosMain/kotlin/com/vodang/greenmind/location/IosGeolocationService.kt`.
- Sử dụng `CLLocationManager` với `desiredAccuracy = kCLLocationAccuracyBest` và `allowsBackgroundLocationUpdates = true`.
- Info.plist keys cần thêm (ví dụ):
  - `NSLocationWhenInUseUsageDescription`: mô tả tại sao cần vị trí khi app foreground.
  - `NSLocationAlwaysAndWhenInUseUsageDescription`: mô tả khi cần vị trí nền.
  - `UIBackgroundModes` includes `location` nếu cần chạy nền.

Accuracy và timestamp
- `accuracy`: bán kính sai số ước tính (mét). Giá trị nhỏ hơn là tốt hơn. Trên iOS, `horizontalAccuracy < 0` nghĩa là không hợp lệ.
- `timestampMillis`: thời điểm nhận vị trí, đơn vị mili-giây kể từ Unix epoch.

Sử dụng trong Compose (ví dụ)
- Khởi tạo (Android): `Geo.service.initialize(context)` (ví dụ trong `MainActivity`).
- Lắng nghe realtime:

```kotlin
Geo.service.locationUpdates.collect { loc ->
  // loc.latitude, loc.longitude, loc.accuracy, loc.timestampMillis
}
```

Gợi ý xử lý chất lượng tín hiệu
- Bỏ qua vị trí khi `accuracy == null` hoặc `accuracy < 0`.
- Tùy ngữ cảnh, chỉ chấp nhận khi `accuracy <= 50f` (hoặc ngưỡng phù hợp).
- Đợi vài cập nhật liên tiếp để độ chính xác ổn định.

Troubleshooting
- Nếu không nhận dữ liệu trên Android: kiểm tra runtime permission, kiểm tra foreground service notification, xem logcat của `LocationForegroundService`.
- Nếu permission dialog không hiện: đảm bảo gọi `requestPermissions` từ Activity và không gọi từ Service.

Tương lai
- Thêm UI/flows để request `ACCESS_BACKGROUND_LOCATION` nhẹ nhàng (show explanation, then request separately).
- Thêm filters/aggregation (distance, speed) nếu cần tiết kiệm pin.

## Models & Schemas

### Kotlin (common)
```kotlin
package com.vodanggreenmind.location

data class Location(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null, // meters
    val timestampMillis: Long = System.currentTimeMillis()
)

expect class GeolocationService() {
    val locationUpdates: Flow<Location>
    fun initialize(platformContext: Any?)
    fun start()
    fun stop()
}

object Geo { val service: GeolocationService = GeolocationService() }
```

### JSON schema (example payload)
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Location",
  "type": "object",
  "properties": {
    "latitude": { "type": "number" },
    "longitude": { "type": "number" },
    "accuracy": { "type": ["number", "null"], "description": "meters" },
    "timestampMillis": { "type": "integer", "description": "epoch ms" }
  },
  "required": ["latitude","longitude","timestampMillis"]
}
```

### REST / Webhook payload (example)
```json
{
  "latitude": 10.762622,
  "longitude": 106.660172,
  "accuracy": 5.3,
  "timestampMillis": 1678491234000
}
```

### TypeScript (client-side) example
```ts
interface Location {
  latitude: number;
  longitude: number;
  accuracy?: number | null; // meters
  timestampMillis: number; // epoch ms
}
```

### Android specifics
- `LocationForegroundService.locationFlow`: `MutableSharedFlow<Location>` in service companion — used internally to stream updates to KMP `GeolocationService` on Android.
- `Location` fields come from Android `android.location.Location`: `getLatitude()`, `getLongitude()`, `getAccuracy()` (meters), `getTime()` (ms since epoch).

### iOS specifics
- `CLLocation.horizontalAccuracy` → mapped to `accuracy` (meters). Negative accuracy means invalid.
- `CLLocation.timestamp` → converted to `timestampMillis` using `timeIntervalSince1970 * 1000`.

### Usage & Validation recommendations
- Ignore points where `accuracy == null` or `accuracy < 0`.
- Constrain accepted points with a threshold: e.g., `accuracy <= 50` (meters) for coarse tracking, `<= 10` for high-precision.
- To smooth jitter, consider simple averaging or discard outliers across a short sliding window (3–5 samples).

## Files of interest
- `composeApp/src/commonMain/kotlin/com/vodang/greenmind/location/Location.kt`
- `composeApp/src/commonMain/kotlin/com/vodang/greenmind/location/GeolocationService.kt`
- `composeApp/src/androidMain/kotlin/com/vodang/greenmind/location/LocationForegroundService.kt`
- `composeApp/src/androidMain/kotlin/com/vodang/greenmind/location/AndroidGeolocationService.kt`
- `composeApp/src/iosMain/kotlin/com/vodang/greenmind/location/IosGeolocationService.kt`

