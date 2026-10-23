package com.nissshh.wichat.core.network.protocol

import org.json.JSONObject

enum class ProtocolMessageType {
    HANDSHAKE,
    HANDSHAKE_ACK,
    CONNECTION_REQUEST,
    REQUEST_RESPONSE,
    CHAT_MESSAGE,
    MESSAGE_ACK,
    FILE_OFFER,
    FILE_CHUNK,
    FILE_CHUNK_ACK,
    STORY_UPDATE,
    HEARTBEAT
}

data class WiChatPacket(
    val version: Int = 1,
    val type: ProtocolMessageType,
    val packetId: String,
    val senderPeerId: String,
    val payloadJson: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun serialize(): String {
        val json = JSONObject()
        json.put("v", version)
        json.put("t", type.name)
        json.put("id", packetId)
        json.put("s", senderPeerId)
        json.put("p", payloadJson)
        json.put("ts", timestamp)
        return json.toString()
    }

    companion object {
        fun deserialize(raw: String): WiChatPacket? {
            return try {
                val json = JSONObject(raw)
                val version = json.optInt("v", 1)
                val typeName = json.getString("t")
                val type = ProtocolMessageType.valueOf(typeName)
                val id = json.getString("id")
                val sender = json.getString("s")
                val payload = json.optString("p", "")
                val ts = json.optLong("ts", System.currentTimeMillis())
                WiChatPacket(version, type, id, sender, payload, ts)
            } catch (e: Exception) {
                null
            }
        }
    }
}
