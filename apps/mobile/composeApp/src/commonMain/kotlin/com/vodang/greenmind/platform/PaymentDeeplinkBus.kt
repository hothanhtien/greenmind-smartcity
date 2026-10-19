package com.vodang.greenmind.platform

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class PaymentDeeplinkResult { SUCCESS, CANCEL }

object PaymentDeeplinkBus {
    private val _events = MutableSharedFlow<PaymentDeeplinkResult>(extraBufferCapacity = 4)
    val events: SharedFlow<PaymentDeeplinkResult> = _events.asSharedFlow()

    /** Called by platform deeplink handlers. Accepts the full URL string. */
    fun publish(url: String) {
        val result = when {
            url.contains("/success", ignoreCase = true) -> PaymentDeeplinkResult.SUCCESS
            url.contains("/cancel", ignoreCase = true) -> PaymentDeeplinkResult.CANCEL
            else -> return
        }
        _events.tryEmit(result)
    }
}
