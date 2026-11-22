package com.resqsync.app.networking.transport

import com.resqsync.app.models.EmergencyLocation
import kotlinx.serialization.Serializable
import com.resqsync.app.models.NetworkMessage

@Serializable
enum class PacketType {
    EMERGENCY_REPORT,
    DELIVERY_ACK,
    LOCATION_REQUEST,
    LOCATION_RESPONSE
}

@Serializable
data class TransferPacket(

    val packetId: String,

    val packetType: PacketType,

    val senderDeviceId: String,

    val createdAt: Long,

    // Used when sending an emergency report
    val networkMessage: NetworkMessage? = null,

    // Used when acknowledging a received report
    val acknowledgedMessageId: String? = null,

    // Used for nearby location assistance requests and responses
    val requestedLocation: EmergencyLocation? = null
)
