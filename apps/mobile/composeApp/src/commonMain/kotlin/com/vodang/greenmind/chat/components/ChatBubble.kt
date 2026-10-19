package com.vodang.greenmind.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.chat.ChatMessage
import com.vodang.greenmind.time.formatChatTime

private val sentBubble = Color(0xFFDCF8C6)
private val receivedBubble = Color(0xFFF0F0F0)
private val systemBg = Color(0xFFFFF8E1)
private val gray400 = Color(0xFF9CA3AF)
private val gray600 = Color(0xFF6B7280)

@Composable
fun ChatBubble(message: ChatMessage) {
    if (message.isSystem) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.clip(RoundedCornerShape(16.dp)).background(systemBg).padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(message.content, fontSize = 12.sp, color = gray600, lineHeight = 17.sp)
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp),
        horizontalAlignment = if (message.isOwn) Alignment.End else Alignment.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            // Sender name for group chat
            if (!message.isOwn && message.senderName.isNotEmpty() && message.senderName != "Me") {
                Text(message.senderName, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E7D32), modifier = Modifier.padding(start = 12.dp, bottom = 2.dp))
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (message.isOwn) 16.dp else 4.dp,
                        bottomEnd = if (message.isOwn) 4.dp else 16.dp,
                    ))
                    .background(if (message.isOwn) sentBubble else receivedBubble)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column(horizontalAlignment = if (message.isOwn) Alignment.End else Alignment.Start) {
                    Text(message.content, fontSize = 14.sp, color = Color(0xFF1C1B1F), lineHeight = 20.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(formatChatTime(message.createdAt), fontSize = 10.sp, color = gray400)
                }
            }
        }
    }
}
