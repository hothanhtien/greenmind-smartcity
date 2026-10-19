package com.vodang.greenmind.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.chat.components.ChatBubble
import com.vodang.greenmind.chat.components.ChatInputBar
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.ChatStore
import kotlinx.coroutines.launch

@Composable
fun ChatDetailScreen(
    thread: ChatThread,
    onBack: () -> Unit,
    showHeader: Boolean = true,
) {
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val messages = ChatStore.messages
    val isLoading = ChatStore.isLoading
    val error = ChatStore.error

    LaunchedEffect(thread.id) {
        ChatStore.openCampaign(thread.id)
    }

    DisposableEffect(Unit) {
        onDispose { ChatStore.closeCampaign() }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(messages.size - 1) }
        }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        // Header (only shown when showHeader is true)
        if (showHeader) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(onClick = {
                        ChatStore.closeCampaign()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFC8E6C9)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (thread.isGroup) {
                            Icon(Icons.Filled.Group, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF2E7D32))
                        } else {
                            Text(thread.initials, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }
                    Column {
                        Text(thread.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        if (thread.campaignStatus.isNotEmpty()) {
                            Text(
                                when (thread.campaignStatus) { "IN_PROGRESS" -> "Active"; "COMPLETED" -> "Completed"; else -> thread.campaignStatus },
                                fontSize = 12.sp, color = if (thread.campaignStatus == "IN_PROGRESS") Color(0xFF43A047) else Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
            }
        }

        if (isLoading && messages.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF43A047))
            }
        } else if (error != null && messages.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(error, fontSize = 13.sp, color = Color(0xFF9CA3AF))
                    Button(onClick = { ChatStore.openCampaign(thread.id) }) { Text(s.blogRetry) }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(message = msg)
                }
            }
        }

        ChatInputBar(onSend = { text -> ChatStore.sendMessage(text) })
    }
}
