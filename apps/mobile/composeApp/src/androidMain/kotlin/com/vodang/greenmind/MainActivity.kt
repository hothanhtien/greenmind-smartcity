package com.vodang.greenmind

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.Modifier
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.vodang.greenmind.permission.PermissionGroup
import com.vodang.greenmind.permission.PermissionRequester

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // ── Geolocation ──────────────────────────────────────────────
        com.vodang.greenmind.location.Geo.service.initialize(this)
        // ── Accounts repository
        com.vodang.greenmind.accounts.AccountsRepository.initialize(this)
        // ── Notification context
        com.vodang.greenmind.store.AndroidNotificationContext.context = this

        val locationLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                          results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            PermissionRequester.updateGranted(PermissionGroup.LOCATION, granted)
        }
        PermissionRequester.registerLauncher(PermissionGroup.LOCATION) {
            locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
        PermissionRequester.updateGranted(
            PermissionGroup.LOCATION,
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )

        // ── Camera ───────────────────────────────────────────────────
        com.vodang.greenmind.camera.Camera.service.initialize(this)

        val cameraLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            PermissionRequester.updateGranted(PermissionGroup.CAMERA, granted)
        }
        PermissionRequester.registerLauncher(PermissionGroup.CAMERA) {
            cameraLauncher.launch(Manifest.permission.CAMERA)
        }
        PermissionRequester.updateGranted(
            PermissionGroup.CAMERA,
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )

        // ── Notification ──────────────────────────────────────────────
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notifLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                PermissionRequester.updateGranted(PermissionGroup.NOTIFICATION, granted)
            }
            PermissionRequester.registerLauncher(PermissionGroup.NOTIFICATION) {
                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            PermissionRequester.updateGranted(
                PermissionGroup.NOTIFICATION,
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            )
        }

        setContent {
            Box(Modifier.fillMaxSize().imePadding()) {
                App()
            }
        }

        // Handle deeplink if app was launched via VIEW intent
        handlePaymentDeeplink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePaymentDeeplink(intent)
    }

    private fun handlePaymentDeeplink(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "greenmind") {
            com.vodang.greenmind.platform.PaymentDeeplinkBus.publish(data.toString())
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}