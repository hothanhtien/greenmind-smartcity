package com.vodang.greenmind.permission

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual object PermissionRequester {
    // Desktop has no runtime permission model — treat all groups as always granted
    private val alwaysGranted = MutableStateFlow(true)

    actual fun grantedFlow(group: PermissionGroup): StateFlow<Boolean> = alwaysGranted

    actual fun request(group: PermissionGroup) {
        // No-op on desktop
    }
}
