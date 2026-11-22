package com.resqsync.app.networking.transport

import com.google.android.gms.nearby.connection.Payload

class MessageReceiver(
    private val serializer: MessageSerializer = MessageSerializer(),
    private val onPacketReceived: (TransferPacket) -> Unit = {}
) {

    fun receivePayload(
        endpointId: String,
        payload: Payload
    ) {
        if (payload.type != Payload.Type.BYTES) {
            return
        }

        val receivedBytes = payload.asBytes() ?: return
        val receivedJson = String(receivedBytes, Charsets.UTF_8)

        try {
            val packet = serializer.deserializePacket(receivedJson)
            onPacketReceived(packet)
        } catch (e: Exception) {
            println("Packet decoding failed: ${e.message}")
        }
    }
}
