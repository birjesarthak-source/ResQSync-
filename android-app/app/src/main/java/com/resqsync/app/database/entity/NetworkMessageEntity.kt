package com.resqsync.app.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.resqsync.app.models.DeliveryStatus
import com.resqsync.app.models.NetworkMessage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "network_messages")
data class NetworkMessageEntity(

    @PrimaryKey
    val messageId: String,

    val reportId: String,

    val senderDeviceId: String,

    val createdAt: Long,

    val ttlExpiresAt: Long,

    val messageJson: String,

    val deliveryStatus: String = "QUEUED"
)

private val messageJsonFormat = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
}

fun NetworkMessage.toNetworkMessageEntity(status: DeliveryStatus = DeliveryStatus.QUEUED): NetworkMessageEntity {
    return NetworkMessageEntity(
        messageId = messageId,
        reportId = reportId,
        senderDeviceId = senderDeviceId,
        createdAt = createdAt,
        ttlExpiresAt = ttlExpiresAt,
        messageJson = messageJsonFormat.encodeToString(this),
        deliveryStatus = status.name
    )
}

fun NetworkMessageEntity.toNetworkMessage(): NetworkMessage {
    return messageJsonFormat.decodeFromString<NetworkMessage>(messageJson)
}
