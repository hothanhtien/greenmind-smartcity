package com.vodang.greenmind.permission

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual object PermissionRequester {
    private val flows = mapOf(
        PermissionGroup.LOCATION to MutableStateFlow(false),
        PermissionGroup.CAMERA  to MutableStateFlow(false),
        PermissionGroup.NOTIFICATION to MutableStateFlow(
            android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU
        ),
    )
    private val launchers = mutableMapOf<PermissionGroup, () -> Unit>()

    actual fun grantedFlow(group: PermissionGroup): StateFlow<Boolean> =
        flows[group] ?: MutableStateFlow(false)

    fun updateGranted(group: PermissionGroup, value: Boolean) {
        flows[group]?.value = value
    }

    fun registerLauncher(group: PermissionGroup, launcher: () -> Unit) {
        launchers[group] = launcher
    }

    actual fun request(group: PermissionGroup) {
        launchers[group]?.invoke()
    }
}
