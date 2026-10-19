package com.vodang.greenmind

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vodang.greenmind.api.ocean.OceanMetricType
import com.vodang.greenmind.components.AppScaffold
import com.vodang.greenmind.home.components.LanguagePickerModal
import com.vodang.greenmind.home.components.OceanScoreCard
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.navigation.AppScreen
import com.vodang.greenmind.store.GpsTick
import com.vodang.greenmind.store.LocationLogStore
import com.vodang.greenmind.store.OceanStore
import com.vodang.greenmind.permission.PermissionGroup
import com.vodang.greenmind.permission.PermissionRequester
import com.vodang.greenmind.store.LocationTrackingStore
import com.vodang.greenmind.store.NotificationService
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.theme.Green800
import com.vodang.greenmind.theme.Green50
import com.vodang.greenmind.theme.SurfaceGray
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val green800 = Green800
private val green50  = Green50

/** Row descriptor for the metrics auto-update section. */
private data class MetricRow(
    val key: String,
    val emoji: String,
    val label: String,
    val metric: OceanMetricType?,
)

@Composable
fun SettingsScreen() {
    val s = LocalAppStrings.current

    val intervalMs      by SettingsStore.locationIntervalMs.collectAsState()
    val minMove         by SettingsStore.minMoveMeters.collectAsState()
    val maxSpeed        by SettingsStore.maxWalkSpeedMs.collectAsState()
    val trackingEnabled by SettingsStore.locationEnabled.collectAsState()
    val language        by SettingsStore.language.collectAsState()
    val roleSwitcherEnabled by SettingsStore.roleSwitcherEnabled.collectAsState()
    val recentTicks     by LocationTrackingStore.recentTicks.collectAsState()
    val locationLogs    by LocationLogStore.entries.collectAsState()

    // Local slider state — committed to store only on finger-lift to avoid excessive writes
    var sliderMove by remember(minMove) { mutableStateOf(minMove) }
    var showLangPicker by remember { mutableStateOf(false) }

    // Metrics auto-update state (persisted in SettingsStore)
    val metricsAutoEnabled  by SettingsStore.metricsAutoEnabled.collectAsState()
    val metricsUpdateHour   by SettingsStore.metricsUpdateHour.collectAsState()
    val metricsUpdateMinute by SettingsStore.metricsUpdateMinute.collectAsState()
    val refreshingMetrics   by OceanStore.refreshingMetrics.collectAsState()
    var showTimePicker      by remember { mutableStateOf(false) }
    var stubRefreshing      by remember { mutableStateOf(emptySet<String>()) }
    val metricsScope = rememberCoroutineScope()

    val intervalOptions = listOf(
        15_000L  to s.interval15s,
        30_000L  to s.interval30s,
        55_000L  to s.interval55s,
        120_000L to s.interval2m,
        300_000L to s.interval5m,
    )

    // Speed presets: value (m/s) → label
    val speedPresets = listOf(
        2.5f  to "${s.speedWalk} ${s.settingsSpeedWalk}",
        7.0f  to "${s.speedRun} ${s.settingsSpeedRun}",
        15.0f to "${s.speedCycle} ${s.settingsSpeedCycle}",
    )
    val selectedSpeedIdx = speedPresets.indices.minByOrNull {
        kotlin.math.abs(speedPresets[it].first - maxSpeed)
    } ?: 1

    val currentLangLabel = if (language == "vi") "🇻🇳 ${s.langVietnamese}" else "🇬🇧 ${s.langEnglish}"

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceGray)
            .verticalScroll(scrollState),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            // ── Location Tracking ─────────────────────────────────────────
            SettingsSectionHeader(s.settingsLocation, Icons.Filled.LocationOn)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {

                    // Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.settingsTrackingEnabled, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                            Text(s.settingsTrackingEnabledDesc, fontSize = 12.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = trackingEnabled,
                            onCheckedChange = { SettingsStore.setLocationEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = green800),
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 8.dp))

                    // Interval
                    Text(s.settingsInterval, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                    Text(s.settingsIntervalDesc, fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        intervalOptions.forEach { (ms, label) ->
                            val selected = intervalMs == ms
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (selected) green800 else Color(0xFFF5F5F5),
                                modifier = Modifier.clickable { SettingsStore.setLocationInterval(ms) },
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) Color.White else Color.DarkGray,
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 8.dp))

                    // Stationary threshold slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(s.settingsMinMove, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121), modifier = Modifier.weight(1f))
                        Text(
                            "${sliderMove.toInt()} m",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = green800,
                        )
                    }
                    Text(s.settingsMinMoveDesc(sliderMove.toInt()), fontSize = 12.sp, color = Color.Gray)
                    Slider(
                        value = sliderMove,
                        onValueChange = { sliderMove = it },
                        onValueChangeFinished = { SettingsStore.setMinMoveMeters(sliderMove) },
                        valueRange = 5f..100f,
                        steps = 18,  // every 5m
                        colors = SliderDefaults.colors(thumbColor = green800, activeTrackColor = green800, inactiveTrackColor = green50),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 8.dp))

                    // Speed filter
                    Text(s.settingsSpeedFilter, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                    Text(s.settingsSpeedFilterDesc(speedPresets[selectedSpeedIdx].first), fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        speedPresets.forEachIndexed { idx, (speed, label) ->
                            val selected = idx == selectedSpeedIdx
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (selected) green800 else Color(0xFFF5F5F5),
                                modifier = Modifier.clickable { SettingsStore.setMaxWalkSpeedMs(speed) },
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) Color.White else Color.DarkGray,
                                )
                            }
                        }
                    }
                }
            }

            OceanScoreCard()

            // ── Metrics Auto Update ───────────────────────────────────────
            SettingsSectionHeader(s.metricsAutoUpdate, Icons.Filled.Analytics)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {

                    // Enable toggle
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.autoUpdateMetrics, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                            Text(s.autoUpdateMetricsDesc, fontSize = 12.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = metricsAutoEnabled,
                            onCheckedChange = { SettingsStore.setMetricsAutoEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = green800),
                        )
                    }

                    // Time picker row (only when enabled)
                    if (metricsAutoEnabled) {
                        HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showTimePicker = true }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(s.updateTime, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                                Text(s.updateTimeDesc, fontSize = 12.sp, color = Color.Gray)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = green50,
                            ) {
                                Text(
                                    text = "%02d:%02d".fmt(metricsUpdateHour, metricsUpdateMinute),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = green800,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 8.dp))

                    // Update All button
                    // UI-key → backend OceanMetricType (null = no backend endpoint yet)
                    val metrics = listOf(
                        MetricRow("daily_spending",       "💸", s.dailySpending,       OceanMetricType.AVG_DAILY_SPEND),
                        MetricRow("night_out",            "🌙", s.nightOut,             null),
                        MetricRow("spend_variability",    "📊", s.spendVariability,     OceanMetricType.SPEND_VARIABILITY),
                        MetricRow("brand_novelty",        "🛍️", s.brandNovelty,         null),
                        MetricRow("list_adherence",       "📋", s.listAdherence,        OceanMetricType.LIST_ADHERENCE),
                        MetricRow("daily_distance",       "🚶", s.dailyDistance,        OceanMetricType.DAILY_DISTANCE_KM),
                        MetricRow("novel_location_ratio", "📍", s.novelLocationRatio,   null),
                        MetricRow("public_transit_ratio", "🚌", s.publicTransitRatio,   null),
                    )
                    // "Refreshing" if either the OceanStore (real API) or stub set marks it busy.
                    fun isMetricRefreshing(row: MetricRow): Boolean {
                        val apiPath = row.metric?.path
                        return (apiPath != null && apiPath in refreshingMetrics) || row.key in stubRefreshing
                    }
                    val supportedPaths = OceanMetricType.values().map { it.path }.toSet()
                    val isRefreshingAll = supportedPaths.all { it in refreshingMetrics } && supportedPaths.isNotEmpty()
                    Button(
                        onClick = {
                            if (!isRefreshingAll) {
                                // Fires all 4 supported metric endpoints in parallel, then
                                // refetches the ocean record once all complete.
                                OceanStore.refreshAllMetrics()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = green800),
                        enabled = !isRefreshingAll,
                    ) {
                        if (isRefreshingAll) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(s.updating, fontSize = 13.sp)
                        } else {
                            Text(s.updateAll, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 4.dp))

                    // Metric rows
                    metrics.forEachIndexed { idx, row ->
                        if (idx > 0) HorizontalDivider(color = Color(0xFFF5F5F5))
                        val isRefreshing = isMetricRefreshing(row)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(row.emoji, fontSize = 18.sp)
                            Text(
                                text = row.label,
                                fontSize = 13.sp,
                                color = Color(0xFF212121),
                                modifier = Modifier.weight(1f),
                            )
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = green800,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = green50,
                                    modifier = Modifier.clickable {
                                        val m = row.metric
                                        if (m != null) {
                                            // Real backend call + refetch ocean.
                                            OceanStore.refreshMetric(m)
                                        } else {
                                            // No backend endpoint yet — stub spinner so the UI still feels responsive.
                                            metricsScope.launch {
                                                stubRefreshing = stubRefreshing + row.key
                                                delay(1500)
                                                stubRefreshing = stubRefreshing - row.key
                                            }
                                        }
                                    },
                                ) {
                                    Text(
                                        text = "↺",
                                        fontSize = 16.sp,
                                        color = green800,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Location Log ───────────────────────────────────────────────────────────
            SettingsSectionHeader("Location Log", Icons.Filled.LocationOn)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (locationLogs.isEmpty()) {
                        Text(s.noLocationEvents, fontSize = 13.sp, color = Color.Gray)
                    } else {
                        locationLogs.asReversed().forEachIndexed { idx, log ->
                            if (idx > 0) HorizontalDivider(color = Color(0xFFF0F0F0))
                            val totalSec = (log.timestampMs / 1000L).toInt()
                            val hh = (totalSec / 3600) % 24
                            val mm = (totalSec / 60) % 60
                            val ss = totalSec % 60
                            val timeLabel = "%02d:%02d:%02d".fmt(hh, mm, ss)

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Surface(shape = RoundedCornerShape(4.dp), color = green50) {
                                    Text(timeLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = green800, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                }
                                Surface(shape = RoundedCornerShape(4.dp), color = if (log.action == "SENT") Color(0xFFE8F5E9) else Color(0xFFFFF8E1)) {
                                    Text(log.action, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (log.action == "SENT") green800 else Color(0xFFF57C00), modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                }
                                Text(log.detail, fontSize = 11.sp, color = Color(0xFF424242), modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // ── Recent GPS ticks ──────────────────────────────────────────
            SettingsSectionHeader("${s.settingsLocation} records", Icons.Filled.LocationOn)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    if (recentTicks.isEmpty()) {
                        Text(
                            s.noGpsRecords,
                            fontSize = 13.sp,
                            color = Color.Gray,
                        )
                    } else {
                        recentTicks.asReversed().forEachIndexed { idx, tick ->
                            if (idx > 0) HorizontalDivider(color = Color(0xFFF0F0F0))
                            GpsTickRow(tick)
                        }
                    }
                }
            }

            // ── Error Log ─────────────────────────────────────────────────
            ErrorLogSection()

            // ── Network Log ───────────────────────────────────────────────
            NetworkLogSection()

            // ── General ───────────────────────────────────────────────────
            SettingsSectionHeader(s.settingsGeneral, Icons.Filled.Settings)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    SettingsNavRow(
                        icon = "🌐",
                        title = s.language,
                        subtitle = currentLangLabel,
                        onClick = { showLangPicker = true },
                    )
                    // Role switcher toggle
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.settingsEnableRoleSwitcher, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                            Text(s.settingsEnableRoleSwitcherDesc, fontSize = 12.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = roleSwitcherEnabled,
                            onCheckedChange = { SettingsStore.setRoleSwitcherEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = green800),
                        )
                    }
                }
            }

            // ── About ─────────────────────────────────────────────────────
            SettingsSectionHeader(s.settingsAbout, Icons.Filled.Info)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Eco, contentDescription = null, modifier = Modifier.size(24.dp), tint = green800)
                        Spacer(Modifier.width(10.dp))
                        Text(s.greenMind, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = green800, modifier = Modifier.weight(1f))
                        Text(s.version, fontSize = 13.sp, color = Color.Gray)
                    }

                    HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 8.dp))

                    Text(s.settingsHowWorks, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = s.settingsHowWorksBody(
                            (intervalMs / 1000).toInt(),
                            minMove.toInt(),
                            maxSpeed,
                        ),
                        fontSize = 13.sp,
                        color = Color(0xFF616161),
                        lineHeight = 20.sp,
                    )
                }
            }

            // ── Test Notification ─────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(s.testNotification, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                    Spacer(Modifier.height(8.dp))
                    val notifGranted by PermissionRequester.grantedFlow(PermissionGroup.NOTIFICATION).collectAsState()
                    Button(
                        onClick = {
                            if (notifGranted) {
                                NotificationService().showChatNotification(
                                    campaignId = "test-campaign-id",
                                    campaignName = "Test Campaign",
                                    senderName = "Test User",
                                    content = "This is a test notification from Settings!",
                                )
                            } else {
                                PermissionRequester.request(PermissionGroup.NOTIFICATION)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                    ) {
                        Text(s.sendTestNotification, color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showTimePicker) {
        MetricsTimePickerDialog(
            hour = metricsUpdateHour,
            minute = metricsUpdateMinute,
            onConfirm = { h, m ->
                SettingsStore.setMetricsUpdateTime(h, m)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
        )
    }

    if (showLangPicker) {
        LanguagePickerModal(
            currentLang = language,
            onSelect = { SettingsStore.setLanguage(it) },
            onDismiss = { showLangPicker = false },
        )
    }
}

// ── Private helpers ───────────────────────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = green800)
        }
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF424242),
        )
    }
}

@Composable
private fun GpsTickRow(tick: GpsTick) {
    val s = LocalAppStrings.current
    // Format HH:mm:ss from epoch millis (UTC — avoids expect/actual for time formatting)
    val totalSec = (tick.timestampMs / 1000L).toInt()
    val hh = (totalSec / 3600) % 24
    val mm = (totalSec / 60)   % 60
    val ss = totalSec % 60
    val timeLabel = "%02d:%02d:%02d".fmt(hh, mm, ss)

    val distLabel = if (tick.distanceMeters < 1.0) s.stationaryLabel
                    else "${tick.distanceMeters.fmt(0)} m"

    val addrShort = if (tick.address.length > 40) tick.address.take(37) + "…" else tick.address

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Time badge
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFE8F5E9),
        ) {
            Text(
                timeLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = green800,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "%.5f, %.5f".fmt(tick.latitude, tick.longitude),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121),
            )
            if (addrShort.isNotBlank()) {
                Text(addrShort, fontSize = 11.sp, color = Color.Gray)
            }
        }
        Text(
            distLabel,
            fontSize = 11.sp,
            color = if (tick.distanceMeters < 1.0) Color(0xFF9E9E9E) else green800,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun MetricsTimePickerDialog(
    hour: Int,
    minute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val s = LocalAppStrings.current
    var h by remember { mutableStateOf(hour) }
    var m by remember { mutableStateOf(minute) }
    val minuteOptions = listOf(0, 15, 30, 45)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(s.updateTime, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))

                // Hour selector
                Text(s.hour, fontSize = 13.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = green50,
                        modifier = Modifier.clickable { h = (h - 1 + 24) % 24 }
                    ) {
                        Text("−", fontSize = 20.sp, color = green800, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                    }
                    Text(
                        text = "%02d".fmt(h),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = green800,
                        modifier = Modifier.width(52.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = green50,
                        modifier = Modifier.clickable { h = (h + 1) % 24 }
                    ) {
                        Text("+", fontSize = 20.sp, color = green800, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                    }
                }

                // Minute selector
                Text(s.minute, fontSize = 13.sp, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    minuteOptions.forEach { opt ->
                        val selected = m == opt
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) green800 else green50,
                            modifier = Modifier.clickable { m = opt },
                        ) {
                            Text(
                                text = "%02d".fmt(opt),
                                fontSize = 14.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) Color.White else green800,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            )
                        }
                    }
                }

                // Confirm / Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    TextButton(onClick = onDismiss) { Text(s.cancel, color = Color.Gray) }
                    Button(
                        onClick = { onConfirm(h, m) },
                        colors = ButtonDefaults.buttonColors(containerColor = green800),
                        shape = RoundedCornerShape(10.dp),
                    ) { Text(s.save) }
                }
            }
        }
    }
}

@Composable
private fun SettingsNavRow(icon: String, title: String, subtitle: String, onClick: () -> Unit) {
    val s = LocalAppStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(icon, fontSize = 20.sp)
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF212121), modifier = Modifier.weight(1f))
        Text(subtitle, fontSize = 13.sp, color = Color.Gray)
        Text(s.chevronRight, fontSize = 18.sp, color = Color.LightGray)
    }
}
