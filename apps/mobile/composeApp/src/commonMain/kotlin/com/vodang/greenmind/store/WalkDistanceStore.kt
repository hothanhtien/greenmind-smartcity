package com.vodang.greenmind.store

import com.vodang.greenmind.api.location.getDistanceToday
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val POLL_INTERVAL_MS = 15_000L

object WalkDistanceStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var pollingJob: Job? = null

    private val _distanceMeters = MutableStateFlow(0)
    val distanceMeters: StateFlow<Int> = _distanceMeters.asStateFlow()

    fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = scope.launch {
            while (isActive) {
                val token = SettingsStore.getAccessToken()
                if (token != null) {
                    try {
                        val response = getDistanceToday(token)
                        _distanceMeters.value = response.data.totalDistance.toInt()
                    } catch (_: Throwable) {
                        // keep last known value and retry next cycle
                    }
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    /** Refresh distance immediately from API */
    fun refresh() {
        val token = SettingsStore.getAccessToken() ?: return
        scope.launch {
            try {
                val response = getDistanceToday(token)
                _distanceMeters.value = response.data.totalDistance.toInt()
            } catch (_: Throwable) {}
        }
    }
}
