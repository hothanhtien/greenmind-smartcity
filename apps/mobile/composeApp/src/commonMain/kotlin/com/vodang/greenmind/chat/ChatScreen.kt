package com.vodang.greenmind.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.chat.components.ChatThreadCard
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.ChatStore

@Composable
fun ChatScreen(onBack: () -> Unit, onThreadClick: (ChatThread) -> Unit) {
    val s = LocalAppStrings.current
    val threads = ChatStore.threads
    val isLoading = ChatStore.isLoading
    val error = ChatStore.error

    LaunchedEffect(Unit) { ChatStore.loadThreads() }

    if (isLoading) {
        Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF43A047))
        }
    } else if (error != null && threads.isEmpty()) {
        Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(error, fontSize = 13.sp, color = Color(0xFF9CA3AF))
                Button(onClick = { ChatStore.loadThreads() }) { Text(s.blogRetry) }
            }
        }
    } else if (threads.isEmpty()) {
        Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
            Text(s.chatEmpty, fontSize = 14.sp, color = Color(0xFF9CA3AF))
        }
    } else {
        LazyColumn(Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
            items(threads, key = { it.id }) { thread ->
                ChatThreadCard(thread = thread, onClick = { onThreadClick(thread) })
                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
            }
        }
    }
}
