package com.vodang.greenmind.scandetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.fmt
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.scandetail.ScanItem
import com.vodang.greenmind.scandetail.ScanItemMass

@Composable
fun ItemsSection(items: List<ScanItem>?) {
    if (items.isNullOrEmpty()) return
    val s = LocalAppStrings.current
    val gray400 = Color(0xFF9CA3AF)
    val gray700 = Color(0xFF374151)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(s.detectedItemsList, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = gray700)
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("• ${item.name}", fontSize = 12.sp, color = gray700, modifier = Modifier.weight(1f))
                Text("×${item.quantity}", fontSize = 12.sp, color = gray400, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun MassSection(totalMassKg: Double?, itemsMass: List<ScanItemMass>?) {
    if (totalMassKg == null && itemsMass.isNullOrEmpty()) return
    val s = LocalAppStrings.current
    val gray400 = Color(0xFF9CA3AF)
    val gray700 = Color(0xFF374151)
    val blue = Color(0xFF1565C0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (totalMassKg != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(s.totalMass, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = gray700)
                Text("%.2f kg".fmt(totalMassKg), fontSize = 12.sp, color = blue, fontWeight = FontWeight.Bold)
            }
        }
        if (!itemsMass.isNullOrEmpty()) {
            if (totalMassKg != null) HorizontalDivider(color = Color(0xFFEEEEEE))
            Text(s.massAndItems, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = gray700)
            itemsMass.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("• ${item.name}", fontSize = 12.sp, color = gray700, modifier = Modifier.weight(1f))
                    Text("%.2f kg".fmt(item.massKg), fontSize = 12.sp, color = gray400, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
