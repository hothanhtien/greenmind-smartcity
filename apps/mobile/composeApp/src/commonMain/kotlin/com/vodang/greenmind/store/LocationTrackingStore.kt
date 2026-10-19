package com.vodang.greenmind.store

import com.vodang.greenmind.api.location.CreateLocationRequest
import com.vodang.greenmind.api.location.LocationDto
import com.vodang.greenmind.api.location.createLocation
import com.vodang.greenmind.api.location.getLatestLocation
import com.vodang.greenmind.api.nominatim.ReverseOptions
import com.vodang.greenmind.api.nominatim.nominatimReverse
import com.vodang.greenmind.fmt
import com.vodang.greenmind.location.Geo
import com.vodang.greenmind.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val NOMINATIM_THRESHOLD_METERS = 100.0
private const val MAX_RECENT_TICKS = 5
private const val LOCATION_FETCH_INTERVAL_TICKS = 6
private const val MIN_ACCURACY_METERS = 50f
private const val GPS_TIMEOUT_MS = 2000L

data class GpsTick(
    val timestampMs: Long,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Double,
    val address: String,
)

object LocationTrackingStore {

    private val _recentTicks = MutableStateFlow<List<GpsTick>>(emptyList())
    val recentTicks: StateFlow<List<GpsTick>> = _recentTicks.asStateFlow()

    private var cachedLastSaved: LocationDto? = null
    private var tickCountSinceLastFetch = 0

    private var cachedAddress: String = ""
    private var cacheAtLat: Double = Double.NaN
    private var cacheAtLon: Double = Double.NaN

    private var lastSentLat: Double = Double.NaN
    private var lastSentLon: Double = Double.NaN
    private var lastSentTimestamp: Long = 0L

    suspend fun tick() {
        if (!SettingsStore.locationEnabled.value) {
            LocationLogStore.log("DISABLED", "Location tracking is disabled")
            return
        }
        val token  = SettingsStore.getAccessToken() ?: run {
            LocationLogStore.log("NO_TOKEN", "No access token available")
            return
        }
        val userId = SettingsStore.getUser()?.id ?: run {
            LocationLogStore.log("NO_USER", "No user ID available")
            return
        }

        val current = getCurrentLocation() ?: run {
            LocationLogStore.log("NO_GPS", "No valid GPS fix")
            return
        }

        if (current.accuracy != null && current.accuracy > MIN_ACCURACY_METERS) {
            LocationLogStore.log("LOW_ACCURACY", "Accuracy ${current.accuracy}m exceeds ${MIN_ACCURACY_METERS}m")
            return
        }

        if (isDuplicateLocation(current)) {
            LocationLogStore.log("DUPLICATE", "Same location within 5s and < 10m")
            return
        }

        if (cachedLastSaved == null || tickCountSinceLastFetch >= LOCATION_FETCH_INTERVAL_TICKS) {
            refreshLastSaved(token)
        }
        tickCountSinceLastFetch++

        val lastSaved = cachedLastSaved

        val distanceMeters = if (lastSaved != null) {
            haversineMeters(
                current.latitude, current.longitude,
                lastSaved.latitude, lastSaved.longitude
            )
        } else {
            0.0
        }

        val minMove = SettingsStore.minMoveMeters.value.toDouble()
        val lengthToPrevious = if (distanceMeters < minMove) 0.0001 else distanceMeters

        val address = reverseCached(current.latitude, current.longitude)

        val tick = GpsTick(
            timestampMs    = current.timestampMillis,
            latitude       = current.latitude,
            longitude      = current.longitude,
            distanceMeters = distanceMeters,
            address        = address,
        )
        _recentTicks.value = (_recentTicks.value + tick).takeLast(MAX_RECENT_TICKS)

        try {
            createLocation(
                token, CreateLocationRequest(
                    userId = userId,
                    address = address,
                    latitude = current.latitude,
                    longitude = current.longitude,
                    lengthToPreviousLocation = lengthToPrevious
                )
            )
            lastSentLat = current.latitude
            lastSentLon = current.longitude
            lastSentTimestamp = current.timestampMillis
            LocationLogStore.log("SENT", "lat=${current.latitude.fmt(6)}, lon=${current.longitude.fmt(6)}, dist=${distanceMeters.fmt(1)}m")
            AppLogger.i("Location", "Sent: lat=${current.latitude}, lon=${current.longitude}, dist=${distanceMeters}m")
        } catch (e: Throwable) {
            LocationLogStore.log("API_ERROR", "Failed: ${e.message}")
            AppLogger.e("Location", "Failed to create location: ${e.message}")
        }
    }

    private suspend fun getCurrentLocation(): Location? {
        return withTimeoutOrNull(GPS_TIMEOUT_MS) {
            Geo.service.locationUpdates.first()
        }
    }

    private fun isDuplicateLocation(current: Location): Boolean {
        if (lastSentTimestamp == 0L) return false
        val timeDiff = current.timestampMillis - lastSentTimestamp
        if (timeDiff < 5000) {
            val dist = haversineMeters(current.latitude, current.longitude, lastSentLat, lastSentLon)
            if (dist < 10.0) return true
        }
        return false
    }

    private suspend fun refreshLastSaved(token: String) {
        tickCountSinceLastFetch = 0
        try {
            cachedLastSaved = getLatestLocation(token).data
            LocationLogStore.log("FETCHED_LAST", "lat=${cachedLastSaved?.latitude?.fmt(6)}, lon=${cachedLastSaved?.longitude?.fmt(6)}")
            AppLogger.d("Location", "Refreshed lastSaved: ${cachedLastSaved?.latitude}, ${cachedLastSaved?.longitude}")
        } catch (e: Throwable) {
            LocationLogStore.log("FETCH_ERROR", "Failed to fetch: ${e.message}")
            AppLogger.w("Location", "Failed to refresh lastSaved: ${e.message}")
        }
    }

    private suspend fun reverseCached(lat: Double, lon: Double): String {
        if (cachedAddress.isNotBlank() && !cacheAtLat.isNaN()) {
            if (haversineMeters(lat, lon, cacheAtLat, cacheAtLon) < NOMINATIM_THRESHOLD_METERS) {
                return cachedAddress
            }
        }
        return try {
            val place = nominatimReverse(lat, lon, ReverseOptions(zoom = 18, addressDetails = false))
            cachedAddress = place.display_name
            cacheAtLat = lat
            cacheAtLon = lon
            cachedAddress
        } catch (e: Throwable) {
            LocationLogStore.log("NOMINATIM_FAIL", "${e.message}")
            AppLogger.w("Location", "Nominatim failed: ${e.message}")
            cachedAddress.ifBlank { "$lat,$lon" }
        }
    }

    fun reset() {
        cachedLastSaved = null
        tickCountSinceLastFetch = 0
        lastSentLat = Double.NaN
        lastSentLon = Double.NaN
        lastSentTimestamp = 0L
        LocationLogStore.log("RESET", "Tracking state cleared")
    }
}

fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6_371_000.0
    val dLat = (lat2 - lat1).toRad()
    val dLon = (lon2 - lon1).toRad()
    val a = sin(dLat / 2).pow(2) + cos(lat1.toRad()) * cos(lat2.toRad()) * sin(dLon / 2).pow(2)
    return R * 2 * atan2(sqrt(a), sqrt(1 - a))
}

fun Double.toRad() = this * PI / 180.0

typealias Location = com.vodang.greenmind.location.Location