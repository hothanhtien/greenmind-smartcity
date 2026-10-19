package com.vodang.greenmind.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.chat.ChatThread

private val sentBubble = Color(0xFFDCF8C6)
private val unreadRed = Color(0xFFE53935)
private val gray400 = Color(0xFF9CA3AF)
private val gray600 = Color(0xFF6B7280)
private val gray800 = Color(0xFF374151)

@Composable
fun ChatThreadCard(thread: ChatThread, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (thread.isGroup) {
                    Box(Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFFE5E7EB)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Group, contentDescription = null, modifier = Modifier.size(24.dp), tint = gray600)
                    }
                } else {
                    Box(Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFFC8E6C9)), contentAlignment = Alignment.Center) {
                        Text(thread.initials, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(thread.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = gray800, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    Spacer(Modifier.width(8.dp))
                    Text(thread.lastMessageAt ?: "", fontSize = 11.sp, color = gray400)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        thread.lastMessage ?: "",
                        fontSize = 13.sp,
                        color = if (thread.unreadCount > 0) gray800 else gray400,
                        fontWeight = if (thread.unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (thread.unreadCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.size(10.dp).clip(CircleShape).background(unreadRed))
                    }
                }
                if (thread.isGroup && thread.participantCount > 0) {
                    Text("${thread.participantCount} participants", fontSize = 11.sp, color = gray400)
                }
            }
        }
    }
}
