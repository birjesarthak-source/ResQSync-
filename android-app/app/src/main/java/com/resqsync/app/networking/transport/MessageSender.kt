package com.resqsync.app.networking.transport

import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload

class MessageSender(
    private val connectionsClient: ConnectionsClient,
    private val serializer: MessageSerializer = MessageSerializer()
) {

    fun sendPacket(
        endpointId: String,
        packet: TransferPacket
    ) {
        val serialized = serializer.serializePacket(packet)
        val payload = Payload.fromBytes(serialized.toByteArray(Charsets.UTF_8))
        connectionsClient.sendPayload(endpointId, payload)
    }

    fun sendMessage(
        endpointId: String,
        message: String
    ) {
        val payload = Payload.fromBytes(message.toByteArray(Charsets.UTF_8))
        connectionsClient.sendPayload(endpointId, payload)
    }
}
