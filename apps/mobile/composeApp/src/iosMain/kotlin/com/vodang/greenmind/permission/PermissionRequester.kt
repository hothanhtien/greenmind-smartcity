package com.vodang.greenmind.permission

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual object PermissionRequester {
    private val flows = mapOf(
        PermissionGroup.LOCATION to MutableStateFlow(false),
        PermissionGroup.CAMERA  to MutableStateFlow(false),
    )

    actual fun grantedFlow(group: PermissionGroup): StateFlow<Boolean> =
        flows[group] ?: MutableStateFlow(false)

    actual fun request(group: PermissionGroup) {
        // On iOS, permissions are requested by CLLocationManager / AVCaptureDevice directly
        // when `start()` is called. This is a no-op hook for future UI guidance.
    }
}
