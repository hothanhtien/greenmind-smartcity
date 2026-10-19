package com.vodang.greenmind.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.WalkDistanceStore
import com.vodang.greenmind.theme.*

@Composable
fun WalkDistanceCard(onBack: () -> Unit) {
    val s = LocalAppStrings.current
    val distanceMeters by WalkDistanceStore.distanceMeters.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp),
                color = Green800
            )
            Text(
                text = s.walkDistance,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Green800
            )
        }

        // Content
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(72.dp), tint = Green800)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "${distanceMeters / 1000.0} km",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Green800
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = s.today,
                fontSize = 16.sp,
                color = TextSecondary
            )
        }
    }
}
