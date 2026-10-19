package com.vodang.greenmind.platform

import android.app.Activity

actual val exitApp: () -> Unit = {
    val activity = Activity::class.java.getDeclaredMethod("finish")
    activity.invoke(null)
}