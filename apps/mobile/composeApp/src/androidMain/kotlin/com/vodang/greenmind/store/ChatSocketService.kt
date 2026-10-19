package com.vodang.greenmind.store

import com.vodang.greenmind.api.BASE_URL
import com.vodang.greenmind.chat.ChatMessage
import com.vodang.greenmind.util.AppLogger
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.json.JSONObject

actual class ChatSocketService {
    private var myUserId: String = ""
    private var socket: Socket? = null
    private var onNewMessage: ((String, ChatMessage) -> Unit)? = null

    actual fun setUserId(id: String) { myUserId = id }

    actual fun connect(token: String, onNewMessage: (String, ChatMessage) -> Unit) {
        this.onNewMessage = onNewMessage
        try {
            val opts = IO.Options().apply {
                auth = mapOf("token" to token)
                forceNew = true
                reconnection = true
            }
            socket = IO.socket(BASE_URL, opts)

            socket?.on(Socket.EVENT_CONNECT) { AppLogger.i("ChatSocket", "Connected") }
            socket?.on(Socket.EVENT_DISCONNECT) { AppLogger.i("ChatSocket", "Disconnected") }

            socket?.on("new_message") { args ->
                try {
                    if (args.isEmpty()) return@on
                    val jsonStr = args[0].toString()
                    val json = Json { ignoreUnknownKeys = true }
                    val obj = json.parseToJsonElement(jsonStr).jsonObject
                    val id = obj["id"]?.jsonPrimitive?.content ?: return@on
                    val campaignId = obj["campaignId"]?.jsonPrimitive?.content ?: return@on
                    val senderObj = obj["sender"]?.jsonObject ?: return@on
                    val senderId = senderObj["id"]?.jsonPrimitive?.content ?: ""
                    val senderName = senderObj["fullName"]?.jsonPrimitive?.content ?: ""
                    val content = obj["content"]?.jsonPrimitive?.content ?: ""
                    val createdAt = obj["createdAt"]?.jsonPrimitive?.content ?: ""

                    onNewMessage(campaignId, ChatMessage(
                        id = id, senderId = senderId, senderName = senderName,
                        content = content, createdAt = createdAt,
                        isOwn = senderId == myUserId,
                    ))
                } catch (e: Throwable) { AppLogger.e("ChatSocket", "Parse: ${e.message}") }
            }

            socket?.on("error") { args ->
                AppLogger.e("ChatSocket", "Error: ${if (args.isNotEmpty()) args[0].toString() else "Unknown"}")
            }
            socket?.connect()
        } catch (e: Throwable) { AppLogger.e("ChatSocket", "Connect: ${e.message}") }
    }

    actual fun joinCampaign(campaignId: String) { socket?.emit("join_campaign", JSONObject().apply { put("campaignId", campaignId) }) }
    actual fun sendMessage(campaignId: String, content: String) { socket?.emit("send_message", JSONObject().apply { put("campaignId", campaignId); put("content", content) }) }
    actual fun leaveCampaign(campaignId: String) { socket?.emit("leave_campaign", JSONObject().apply { put("campaignId", campaignId) }) }
    actual fun disconnect() { socket?.disconnect(); socket = null; onNewMessage = null }
}
