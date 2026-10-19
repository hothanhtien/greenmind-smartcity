package com.vodang.greenmind.location

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.Flow

actual class GeolocationService actual constructor() {
    // Delegate directly to the service's companion flow — no broadcast needed
    actual val locationUpdates: Flow<Location> = LocationForegroundService.locationFlow

    private var ctx: Context? = null

    actual fun initialize(platformContext: Any?) {
        ctx = platformContext as? Context
    }

    actual fun start() {
        val context = ctx ?: return
        val fine = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) return
        val intent = Intent(context, LocationForegroundService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (_: Throwable) {
            context.startService(intent)
        }
    }

    actual fun stop() {
        val context = ctx ?: return
        context.stopService(Intent(context, LocationForegroundService::class.java))
    }
}
