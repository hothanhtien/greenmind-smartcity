package com.vodang.greenmind.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings

@Composable
fun LanguageSwitcher(currentLang: String, onClick: () -> Unit) {
    val s = LocalAppStrings.current
    val flag = if (currentLang == "vi") "🇻🇳" else "🇬🇧"
    val name = if (currentLang == "vi") s.langVietnamese else s.langEnglish
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.DarkGray)
        Text(name, fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.weight(1f))
        Text(flag, fontSize = 20.sp)
        Text("›", fontSize = 18.sp, color = Color.Gray, fontWeight = FontWeight.Light)
    }
}
