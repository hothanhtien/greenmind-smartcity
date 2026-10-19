package com.vodang.greenmind.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings

private val green50 = Color(0xFFE8F5E9)
private val green800 = Color(0xFF2E7D32)

@Composable
fun CollectionPointRow(id: Int, address: String, zone: String, collected: Boolean, bags: Int) {
    val s = LocalAppStrings.current
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (collected) green50 else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (collected) 0.dp else 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).background(if (collected) green800 else Color(0xFFEEEEEE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(if (collected) "v" else "$id", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (collected) Color.White else Color.Gray)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(address, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text("$zone · $bags ${s.bagsUnit}", fontSize = 11.sp, color = Color.Gray)
            }
            Surface(shape = RoundedCornerShape(20.dp), color = if (collected) green800 else Color(0xFFFFB300)) {
                Text(
                    if (collected) s.collectedLabel else s.notCollectedLabel,
                    fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
