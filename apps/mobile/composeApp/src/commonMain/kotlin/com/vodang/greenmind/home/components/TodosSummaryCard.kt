package com.vodang.greenmind.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.TodoStore
import com.vodang.greenmind.store.countAll
import com.vodang.greenmind.store.countDone

private val green50 = Color(0xFFE8F5E9)
private val green800 = Color(0xFF2E7D32)
private val green100 = Color(0xFFC8E6C9)

@Composable
fun TodosSummaryCard() {
    val s = LocalAppStrings.current
    val todos by TodoStore.todos.collectAsState()
    val total = todos.sumOf { it.countAll() }
    val done = todos.sumOf { it.countDone() }
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("✅", fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(s.todosCardTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(s.todosCardProgress(done, total), fontSize = 12.sp, color = Color.Gray)
            }
            Box(modifier = Modifier.size(48.dp).background(green50, CircleShape), contentAlignment = Alignment.Center) {
                Text("$done", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = green800)
            }
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else done.toFloat() / total },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = green800,
            trackColor = green100
        )
    }
}
