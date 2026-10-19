package com.vodang.greenmind.store

import com.russhwolf.settings.Settings
import com.vodang.greenmind.api.auth.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val KEY_ACCESS_TOKEN       = "access_token"
private const val KEY_REFRESH_TOKEN      = "refresh_token"
private const val KEY_USER               = "user"
private const val KEY_LANGUAGE           = "language"
private const val KEY_LOCATION_INTERVAL  = "location_interval_ms"
private const val KEY_MIN_MOVE_METERS    = "min_move_meters"
private const val KEY_MAX_WALK_SPEED     = "max_walk_speed_ms"
private const val KEY_LOCATION_ENABLED   = "location_enabled"
private const val KEY_ENABLE_ROLE_SWITCHER = "enable_role_switcher"
private const val KEY_METRICS_AUTO_ENABLED = "metrics_auto_enabled"
private const val KEY_METRICS_UPDATE_HOUR  = "metrics_update_hour"
private const val KEY_METRICS_UPDATE_MIN   = "metrics_update_minute"
private const val KEY_METRICS_LAST_RUN_MS  = "metrics_last_run_ms"

object SettingsStore {

    private val settings: Settings = Settings()
    private val json = Json { ignoreUnknownKeys = true }

    // ── Auth ─────────────────────────────────────────────────────────────────
    private val _accessToken  = MutableStateFlow<String?>(settings.getStringOrNull(KEY_ACCESS_TOKEN))
    private val _refreshToken = MutableStateFlow<String?>(settings.getStringOrNull(KEY_REFRESH_TOKEN))
    private val _user = MutableStateFlow<UserDto?>(
        settings.getStringOrNull(KEY_USER)
            ?.let { runCatching { json.decodeFromString<UserDto>(it) }.getOrNull() }
    )
    private val _language = MutableStateFlow(settings.getStringOrNull(KEY_LANGUAGE) ?: "vi")

    val accessToken:  StateFlow<String?>  = _accessToken.asStateFlow()
    val refreshToken: StateFlow<String?>  = _refreshToken.asStateFlow()
    val user:         StateFlow<UserDto?> = _user.asStateFlow()
    val language:     StateFlow<String>   = _language.asStateFlow()

    fun getAccessToken():  String?  = _accessToken.value
    fun getRefreshToken(): String?  = _refreshToken.value
    fun getUser():         UserDto? = _user.value

    fun setAccessToken(token: String?) {
        if (token == null) settings.remove(KEY_ACCESS_TOKEN) else settings.putString(KEY_ACCESS_TOKEN, token)
        _accessToken.value = token
        if (token != null) {
            HouseholdStore.fetchHousehold()
        } else {
            HouseholdStore.clearHousehold()
        }
    }
    fun setRefreshToken(token: String?) {
        if (token == null) settings.remove(KEY_REFRESH_TOKEN) else settings.putString(KEY_REFRESH_TOKEN, token)
        _refreshToken.value = token
    }
    fun setUser(userDto: UserDto?) {
        if (userDto == null) settings.remove(KEY_USER) else settings.putString(KEY_USER, json.encodeToString(userDto))
        _user.value = userDto
    }
    fun setLanguage(lang: String) {
        settings.putString(KEY_LANGUAGE, lang)
        _language.value = lang
    }
    fun clearAll() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_USER)
        _accessToken.value  = null
        _refreshToken.value = null
        _user.value         = null
    }

    // ── Location tracking settings ────────────────────────────────────────────

    private val _locationIntervalMs = MutableStateFlow(
        settings.getLongOrNull(KEY_LOCATION_INTERVAL) ?: 55_000L
    )
    private val _minMoveMeters = MutableStateFlow(
        settings.getFloatOrNull(KEY_MIN_MOVE_METERS) ?: 20f
    )
    private val _maxWalkSpeedMs = MutableStateFlow(
        settings.getFloatOrNull(KEY_MAX_WALK_SPEED) ?: 7f
    )
    private val _locationEnabled = MutableStateFlow(
        settings.getBooleanOrNull(KEY_LOCATION_ENABLED) ?: true
    )

    private val _roleSwitcherEnabled = MutableStateFlow(
        settings.getBooleanOrNull(KEY_ENABLE_ROLE_SWITCHER) ?: true
    )

    val locationIntervalMs: StateFlow<Long>    = _locationIntervalMs.asStateFlow()
    val minMoveMeters:      StateFlow<Float>   = _minMoveMeters.asStateFlow()
    val maxWalkSpeedMs:     StateFlow<Float>   = _maxWalkSpeedMs.asStateFlow()
    val locationEnabled:    StateFlow<Boolean> = _locationEnabled.asStateFlow()
    val roleSwitcherEnabled: StateFlow<Boolean> = _roleSwitcherEnabled.asStateFlow()

    fun setLocationInterval(ms: Long) {
        settings.putLong(KEY_LOCATION_INTERVAL, ms)
        _locationIntervalMs.value = ms
    }
    fun setMinMoveMeters(meters: Float) {
        settings.putFloat(KEY_MIN_MOVE_METERS, meters)
        _minMoveMeters.value = meters
    }
    fun setMaxWalkSpeedMs(speed: Float) {
        settings.putFloat(KEY_MAX_WALK_SPEED, speed)
        _maxWalkSpeedMs.value = speed
    }
    fun setLocationEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_LOCATION_ENABLED, enabled)
        _locationEnabled.value = enabled
    }
    fun setRoleSwitcherEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_ENABLE_ROLE_SWITCHER, enabled)
        _roleSwitcherEnabled.value = enabled
    }

    // ── Ocean metrics auto-update ────────────────────────────────────────────

    private val _metricsAutoEnabled = MutableStateFlow(
        settings.getBooleanOrNull(KEY_METRICS_AUTO_ENABLED) ?: true
    )
    private val _metricsUpdateHour = MutableStateFlow(
        settings.getIntOrNull(KEY_METRICS_UPDATE_HOUR) ?: 21
    )
    private val _metricsUpdateMinute = MutableStateFlow(
        settings.getIntOrNull(KEY_METRICS_UPDATE_MIN) ?: 0
    )
    private val _metricsLastRunMs = MutableStateFlow(
        settings.getLongOrNull(KEY_METRICS_LAST_RUN_MS) ?: 0L
    )

    val metricsAutoEnabled:  StateFlow<Boolean> = _metricsAutoEnabled.asStateFlow()
    val metricsUpdateHour:   StateFlow<Int>     = _metricsUpdateHour.asStateFlow()
    val metricsUpdateMinute: StateFlow<Int>     = _metricsUpdateMinute.asStateFlow()
    val metricsLastRunMs:    StateFlow<Long>    = _metricsLastRunMs.asStateFlow()

    fun setMetricsAutoEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_METRICS_AUTO_ENABLED, enabled)
        _metricsAutoEnabled.value = enabled
    }
    fun setMetricsUpdateTime(hour: Int, minute: Int) {
        settings.putInt(KEY_METRICS_UPDATE_HOUR, hour)
        settings.putInt(KEY_METRICS_UPDATE_MIN, minute)
        _metricsUpdateHour.value = hour
        _metricsUpdateMinute.value = minute
    }
    fun setMetricsLastRunMs(ms: Long) {
        settings.putLong(KEY_METRICS_LAST_RUN_MS, ms)
        _metricsLastRunMs.value = ms
    }

    init {
        // Fetch household on app startup if already logged in
        if (_accessToken.value != null) {
            HouseholdStore.fetchHousehold()
        }
    }
}
