package com.vodang.greenmind.householdwaste.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vodang.greenmind.api.households.getCurrentUserHousehold
import com.vodang.greenmind.api.households.HouseholdResponse
import com.vodang.greenmind.store.HouseholdStore
import com.vodang.greenmind.store.SettingsStore

@Composable
fun CreateHouseholdScreen(onBack: () -> Unit, onSuccess: () -> Unit) {
    var isChecking by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val token = SettingsStore.getAccessToken()
        if (token != null) {
            try {
                val response = getCurrentUserHousehold(token)
                val household = response.data.toHouseholdDto()
                if (household != null) {
                    HouseholdStore.setHousehold(household)
                    return@LaunchedEffect
                }
            } catch (_: Exception) { }
        }
        isChecking = false
    }

    if (isChecking) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        Modifier.fillMaxSize().background(Color(0xFFF5F5F5)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CreateHouseholdForm(onSuccess = onSuccess, onCancel = onBack)
    }
}