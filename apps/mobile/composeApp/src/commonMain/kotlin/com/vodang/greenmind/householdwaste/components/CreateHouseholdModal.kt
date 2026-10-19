package com.vodang.greenmind.householdwaste.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.nominatim.ReverseOptions
import com.vodang.greenmind.api.nominatim.nominatimReverse
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.location.Geo
import com.vodang.greenmind.location.Location
import com.vodang.greenmind.permission.PermissionGroup
import com.vodang.greenmind.permission.PermissionRequester
import com.vodang.greenmind.store.LocationTrackingStore
import com.vodang.greenmind.store.HouseholdStore
import com.vodang.greenmind.store.SettingsStore
import kotlinx.coroutines.launch


@Composable
fun CreateHouseholdForm(onSuccess: () -> Unit, onCancel: () -> Unit) {
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()
    var houseNumber by remember { mutableStateOf("") }
    var nominatimAddress by remember { mutableStateOf("") }
    var isResolvingAddress by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Final address sent to the API: house number (if any) prepended to the Nominatim result.
    val address = buildString {
        if (houseNumber.isNotBlank()) append("${houseNumber.trim()} ")
        append(nominatimAddress)
    }

    val recentTicks by LocationTrackingStore.recentTicks.collectAsState()
    val latestTick = recentTicks.lastOrNull()
    val locationGranted by PermissionRequester.grantedFlow(PermissionGroup.LOCATION).collectAsState()
    val trackingEnabled by SettingsStore.locationEnabled.collectAsState()

    // Direct GPS fix — falls back to this when the 55-second tick store is still empty.
    var directLocation by remember { mutableStateOf<Location?>(null) }
    LaunchedEffect(locationGranted) {
        if (locationGranted) {
            Geo.service.locationUpdates.collect { loc ->
                if (directLocation == null) directLocation = loc
            }
        }
    }

    // Effective coordinates: prefer the tracking store tick, fall back to direct fix.
    val effectiveLat = latestTick?.latitude ?: directLocation?.latitude
    val effectiveLon = latestTick?.longitude ?: directLocation?.longitude

    // Keep address synced with the latest GPS point.
    LaunchedEffect(effectiveLat, effectiveLon) {
        val lat = effectiveLat ?: return@LaunchedEffect
        val lon = effectiveLon ?: return@LaunchedEffect
        isResolvingAddress = true
        try {
            val place = nominatimReverse(lat, lon, ReverseOptions(zoom = 18))
            nominatimAddress = place.display_name
        } catch (_: Exception) {
            if (nominatimAddress.isBlank()) nominatimAddress = "$lat,$lon"
        } finally {
            isResolvingAddress = false
        }
    }

    Text(
        text = s.setLocation,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF2E7D32)
    )

    OutlinedTextField(
        value = houseNumber,
        onValueChange = { houseNumber = it },
        label = { Text(s.houseNumberOptional) },
        placeholder = { Text(s.houseNumberPlaceholder) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )

    OutlinedTextField(
        value = nominatimAddress,
        onValueChange = {},
        readOnly = true,
        label = { Text(s.addressFromGps) },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            if (isResolvingAddress) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            }
        }
    )

    if (effectiveLat != null && effectiveLon != null) {
        Text(
            text = "${s.coordinatesFromGps(effectiveLat, effectiveLon)}",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = s.locationRequired,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE65100)
                )
                if (!locationGranted) {
                    Text(
                        text = s.locationPermissionNotGranted,
                        fontSize = 12.sp,
                        color = Color(0xFF6D4C41)
                    )
                    Button(
                        onClick = { PermissionRequester.request(PermissionGroup.LOCATION) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                    ) {
                        Text(s.grantLocationPermission)
                    }
                } else if (!trackingEnabled) {
                    Text(
                        text = s.locationTrackingOff,
                        fontSize = 12.sp,
                        color = Color(0xFF6D4C41)
                    )
                    Button(
                        onClick = { SettingsStore.setLocationEnabled(true) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                    ) {
                        Text(s.enableLocationTracking)
                    }
                } else {
                    Text(
                        text = s.waitingForGps,
                        fontSize = 12.sp,
                        color = Color(0xFF6D4C41)
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally),
                        color = Color(0xFFFF9800),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }

    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
            Text(s.backArrow)
        }
        Button(
            onClick = {
                scope.launch {
                    val lat = effectiveLat ?: return@launch
                    val lon = effectiveLon ?: return@launch
                    isSubmitting = true
                    val success = HouseholdStore.createNewHousehold(address, lat, lon)
                    isSubmitting = false
                    if (success) onSuccess()
                }
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
            enabled = !isSubmitting && nominatimAddress.isNotBlank() && effectiveLat != null && effectiveLon != null
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text(s.save)
            }
        }
    }
}
