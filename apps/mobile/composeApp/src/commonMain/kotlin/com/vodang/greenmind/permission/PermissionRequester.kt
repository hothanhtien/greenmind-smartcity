package com.vodang.greenmind.permission

import kotlinx.coroutines.flow.StateFlow

enum class PermissionGroup { LOCATION, CAMERA, NOTIFICATION }

expect object PermissionRequester {
    fun grantedFlow(group: PermissionGroup): StateFlow<Boolean>
    fun request(group: PermissionGroup)
}
