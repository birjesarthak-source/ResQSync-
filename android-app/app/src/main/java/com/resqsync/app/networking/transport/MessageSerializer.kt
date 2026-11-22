package com.resqsync.app.networking.transport

import com.resqsync.app.models.NetworkMessage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MessageSerializer {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    fun serializePacket(packet: TransferPacket): String {
        return json.encodeToString(packet)
    }

    fun deserializePacket(data: String): TransferPacket {
        return try {
            json.decodeFromString<TransferPacket>(data)
        } catch (e: Exception) {
            // Fallback for legacy raw NetworkMessage JSON payload
            val message = json.decodeFromString<NetworkMessage>(data)
            TransferPacket(
                packetId = message.messageId,
                packetType = PacketType.EMERGENCY_REPORT,
                senderDeviceId = message.senderDeviceId,
                createdAt = message.createdAt,
                networkMessage = message
            )
        }
    }

    fun serialize(message: NetworkMessage): String {
        return json.encodeToString(message)
    }

    fun deserialize(data: String): NetworkMessage {
        return json.decodeFromString<NetworkMessage>(data)
    }
}
