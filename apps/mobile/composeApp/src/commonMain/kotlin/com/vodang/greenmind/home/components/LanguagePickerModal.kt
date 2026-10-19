package com.vodang.greenmind.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings

private val green800 = Color(0xFF2E7D32)
private val green50 = Color(0xFFE8F5E9)

private val languages = listOf(
    "vi" to ("🇻🇳" to "langVietnamese"),
    "en" to ("🇬🇧" to "langEnglish"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePickerModal(
    currentLang: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalAppStrings.current
    val options = listOf(
        "vi" to ("🇻🇳" to s.langVietnamese),
        "en" to ("🇬🇧" to s.langEnglish),
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(s.language, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            options.forEach { (code, flagAndName) ->
                val (flag, name) = flagAndName
                val selected = currentLang == code
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) green50 else Color.Transparent)
                        .clickable { onSelect(code); onDismiss() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(flag, fontSize = 28.sp)
                    Text(
                        name,
                        fontSize = 15.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) green800 else Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    if (selected) Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = green800)
                }
            }
        }
    }
}
