package com.vodang.greenmind.store

import com.vodang.greenmind.api.ocean.CreateOceanRequest
import com.vodang.greenmind.api.ocean.OceanMetricType
import com.vodang.greenmind.api.ocean.OceanScores
import com.vodang.greenmind.api.ocean.OceanScoresInput
import com.vodang.greenmind.api.ocean.UpdateOceanRequest
import com.vodang.greenmind.api.ocean.createOcean
import com.vodang.greenmind.api.ocean.getOcean
import com.vodang.greenmind.api.ocean.postOceanMetric
import com.vodang.greenmind.api.ocean.updateOcean
import com.vodang.greenmind.time.currentTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val DEFAULT_SCORES = OceanScoresInput(O = 50.0, C = 50.0, E = 50.0, A = 50.0, N = 50.0)

object OceanStore {

    private val scope = CoroutineScope(SupervisorJob())

    private val _scores = MutableStateFlow<OceanScores?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val scores: StateFlow<OceanScores?> = _scores.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Fetches the ocean scores for the current user.
     * If the fetch fails (e.g. not found), creates a record with default scores then re-fetches.
     */
    fun load() {
        val token = SettingsStore.getAccessToken() ?: return
        val userId = SettingsStore.getUser()?.id ?: return
        scope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _scores.value = getOcean(token, userId).scores
            } catch (_: Throwable) {
                // Record doesn't exist — create with defaults then re-fetch
                try {
                    createOcean(token, CreateOceanRequest(userId = userId, scores = DEFAULT_SCORES))
                    _scores.value = getOcean(token, userId).scores
                } catch (e: Throwable) {
                    _error.value = e.message
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun update(input: OceanScoresInput) {
        val token = SettingsStore.getAccessToken() ?: return
        val userId = SettingsStore.getUser()?.id ?: return
        scope.launch {
            try {
                val resp = updateOcean(token, userId, UpdateOceanRequest(input))
                _scores.value = resp.data.scores
            } catch (e: Throwable) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    // ── Metrics ──────────────────────────────────────────────────────────────

    private val _refreshingMetrics = MutableStateFlow<Set<String>>(emptySet())
    val refreshingMetrics: StateFlow<Set<String>> = _refreshingMetrics.asStateFlow()

    /** Posts a single ocean-metric, then refetches the OCEAN scores. */
    fun refreshMetric(metric: OceanMetricType, onDone: (Throwable?) -> Unit = {}) {
        val token = SettingsStore.getAccessToken() ?: return onDone(IllegalStateException("Not signed in"))
        scope.launch {
            val key = metric.path
            _refreshingMetrics.value = _refreshingMetrics.value + key
            try {
                postOceanMetric(token, metric)
                load()
                onDone(null)
            } catch (e: Throwable) {
                _error.value = e.message
                onDone(e)
            } finally {
                _refreshingMetrics.value = _refreshingMetrics.value - key
            }
        }
    }

    /**
     * Foreground-only "21:00" auto-runner. Triggers [refreshAllMetrics] if the
     * user has enabled auto-update AND it has been ≥ 23 h since the last run.
     *
     * Note: real OS-level scheduling at a precise local time requires
     * WorkManager (Android) / BGTaskScheduler (iOS). This in-app check fires
     * the daily refresh whenever the app comes to the foreground after the
     * cooldown expires, which is sufficient for typical usage.
     */
    fun maybeRunDailyMetrics() {
        if (!SettingsStore.metricsAutoEnabled.value) return
        if (SettingsStore.getAccessToken() == null) return
        val now = currentTimeMillis()
        val last = SettingsStore.metricsLastRunMs.value
        val cooldownMs = 23L * 60L * 60L * 1000L  // ~once per day
        if (now - last < cooldownMs) return
        refreshAllMetrics()
    }

    /**
     * Posts all supported ocean-metric endpoints in parallel, then refetches the
     * OCEAN scores once. Records `metricsLastRunMs` on the SettingsStore so the
     * 21:00 auto-update can avoid double-firing within the same day.
     */
    fun refreshAllMetrics(onDone: (Throwable?) -> Unit = {}): Job? {
        val token = SettingsStore.getAccessToken() ?: return null
        val all = OceanMetricType.values().toList()
        return scope.launch {
            val keys = all.map { it.path }.toSet()
            _refreshingMetrics.value = _refreshingMetrics.value + keys
            try {
                all.map { m ->
                    async {
                        runCatching { postOceanMetric(token, m) }
                    }
                }.awaitAll()
                // Refetch the ocean record so OceanScoreCard updates.
                load()
                SettingsStore.setMetricsLastRunMs(currentTimeMillis())
                onDone(null)
            } catch (e: Throwable) {
                _error.value = e.message
                onDone(e)
            } finally {
                _refreshingMetrics.value = _refreshingMetrics.value - keys
            }
        }
    }
}
