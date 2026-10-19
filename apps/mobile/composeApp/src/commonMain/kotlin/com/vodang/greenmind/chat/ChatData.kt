package com.vodang.greenmind.chat

import com.vodang.greenmind.api.chat.ChatListDto
import com.vodang.greenmind.api.chat.MessageDto
import com.vodang.greenmind.time.formatChatTime

data class ChatThread(
    val id: String,
    val name: String,
    val initials: String,
    val lastMessage: String?,
    val lastMessageAt: String?,
    val unreadCount: Int = 0,
    val isGroup: Boolean = true,
    val campaignStatus: String = "",
    val participantCount: Int = 0,
)

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val createdAt: String,
    val isOwn: Boolean = false,
    val isSystem: Boolean = false,
)

// ── Mapping ───────────────────────────────────────────────────────────────────

fun ChatListDto.toChatThread(): ChatThread {
    val abbr = campaignName.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("").ifBlank { campaignName.take(2).uppercase() }
    return ChatThread(
        id = campaignId,
        name = campaignName,
        initials = abbr,
        lastMessage = lastMessage?.content,
        lastMessageAt = lastMessage?.createdAt?.let { formatChatTime(it) },
        unreadCount = messageCount,
        isGroup = true,
        campaignStatus = campaignStatus,
    )
}

fun MessageDto.toChatMessage(currentUserId: String): ChatMessage = ChatMessage(
    id = id,
    senderId = sender.id,
    senderName = sender.fullName,
    content = content,
    createdAt = formatChatTime(createdAt),
    isOwn = sender.id == currentUserId,
)
