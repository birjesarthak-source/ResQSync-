package com.resqsync.app.networking.connection

import android.content.Context
import android.util.Log

import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate

import com.resqsync.app.models.EmergencyLocation
import com.resqsync.app.models.NetworkMessage
import com.resqsync.app.networking.transport.MessageSerializer
import com.resqsync.app.networking.transport.PacketType
import com.resqsync.app.networking.transport.TransferPacket
import java.util.UUID

class NearbyConnectionManager(
    context: Context,
    private val onConnectionRequest: (
        endpointId: String,
        deviceName: String,
        authenticationDigits: String
    ) -> Unit = { _, _, _ -> }
) {

    private val connectionsClient =
        Nearby.getConnectionsClient(context.applicationContext)

    private val serializer = MessageSerializer()

    // Called when a connection is successfully established or disconnected
    var onConnectionStateChanged:
            ((endpointId: String, connected: Boolean, message: String) -> Unit)? = null

    // Called when an emergency message is received
    var onMessageReceived:
            ((endpointId: String, message: NetworkMessage) -> Unit)? = null

    // Called when any transfer packet (including DELIVERY_ACK or LOCATION_REQUEST) is received
    var onPacketReceived:
            ((endpointId: String, packet: TransferPacket) -> Unit)? = null


    // Shared callback for outgoing and incoming connection lifecycle events
    val connectionLifecycleCallback =
        object : ConnectionLifecycleCallback() {

            override fun onConnectionInitiated(
                endpointId: String,
                connectionInfo: ConnectionInfo
            ) {

                Log.d(
                    "ResQSyncNetwork",
                    "Connection initiated with ${connectionInfo.endpointName}"
                )

                onConnectionRequest(
                    endpointId,
                    connectionInfo.endpointName,
                    connectionInfo.authenticationDigits
                )
            }

            override fun onConnectionResult(
                endpointId: String,
                result: ConnectionResolution
            ) {

                when (result.status.statusCode) {

                    ConnectionsStatusCodes.STATUS_OK -> {

                        Log.d(
                            "ResQSyncNetwork",
                            "Connected successfully: $endpointId"
                        )

                        onConnectionStateChanged?.invoke(
                            endpointId,
                            true,
                            "Connected successfully"
                        )
                    }

                    else -> {

                        Log.e(
                            "ResQSyncNetwork",
                            "Connection failed: ${result.status.statusMessage}"
                        )

                        onConnectionStateChanged?.invoke(
                            endpointId,
                            false,
                            "Connection failed: ${result.status.statusMessage}"
                        )
                    }
                }
            }

            override fun onDisconnected(endpointId: String) {

                Log.d(
                    "ResQSyncNetwork",
                    "Device disconnected: $endpointId"
                )

                onConnectionStateChanged?.invoke(
                    endpointId,
                    false,
                    "Device disconnected"
                )
            }
        }


    // Handles incoming data payloads
    private val payloadCallback =
        object : PayloadCallback() {

            override fun onPayloadReceived(
                endpointId: String,
                payload: Payload
            ) {

                if (payload.type != Payload.Type.BYTES) {
                    Log.w(
                        "ResQSyncNetwork",
                        "Unsupported payload type received"
                    )
                    return
                }

                val receivedBytes = payload.asBytes() ?: return
                val receivedJson = String(
                    receivedBytes,
                    Charsets.UTF_8
                )

                try {

                    val packet = serializer.deserializePacket(receivedJson)

                    Log.d(
                        "ResQSyncNetwork",
                        "Transfer packet received: ${packet.packetId}, type: ${packet.packetType}"
                    )

                    onPacketReceived?.invoke(endpointId, packet)

                    if (packet.packetType == PacketType.EMERGENCY_REPORT && packet.networkMessage != null) {
                        onMessageReceived?.invoke(endpointId, packet.networkMessage)
                    }

                } catch (exception: Exception) {

                    Log.e(
                        "ResQSyncNetwork",
                        "Failed to decode incoming packet",
                        exception
                    )
                }
            }

            override fun onPayloadTransferUpdate(
                endpointId: String,
                update: PayloadTransferUpdate
            ) {

                Log.d(
                    "ResQSyncNetwork",
                    "Payload transfer update: ${update.status}"
                )

                if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                    Log.d("ResQSyncNetwork", "Message transfer completed")
                }

                if (update.status == PayloadTransferUpdate.Status.FAILURE) {
                    Log.e("ResQSyncNetwork", "Message transfer failed")
                }
            }
        }


    // Request a connection to a discovered endpoint
    fun startConnection(
        deviceName: String,
        endpointId: String
    ) {

        connectionsClient.requestConnection(
            deviceName,
            endpointId,
            connectionLifecycleCallback
        )
            .addOnSuccessListener {

                Log.d(
                    "ResQSyncNetwork",
                    "Connection request sent to $endpointId"
                )
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "ResQSyncNetwork",
                    "Unable to request connection",
                    exception
                )

                onConnectionStateChanged?.invoke(
                    endpointId,
                    false,
                    "Request failed: ${exception.message}"
                )
            }
    }


    // Accept a connection after the user verifies the code
    fun acceptConnection(endpointId: String) {

        connectionsClient.acceptConnection(
            endpointId,
            payloadCallback
        )
            .addOnSuccessListener {

                Log.d(
                    "ResQSyncNetwork",
                    "Connection accepted: $endpointId"
                )
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "ResQSyncNetwork",
                    "Unable to accept connection",
                    exception
                )
            }
    }


    // Reject a connection request
    fun rejectConnection(endpointId: String) {

        connectionsClient.rejectConnection(endpointId)
            .addOnSuccessListener {

                Log.d(
                    "ResQSyncNetwork",
                    "Connection rejected: $endpointId"
                )
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "ResQSyncNetwork",
                    "Unable to reject connection",
                    exception
                )
            }
    }


    // Send a TransferPacket directly
    fun sendPacket(
        endpointId: String,
        packet: TransferPacket
    ) {
        try {
            val jsonPacket = serializer.serializePacket(packet)
            val payload = Payload.fromBytes(jsonPacket.toByteArray(Charsets.UTF_8))

            connectionsClient.sendPayload(endpointId, payload)
                .addOnSuccessListener {
                    Log.d("ResQSyncNetwork", "Packet submitted for transfer: ${packet.packetId}")
                }
                .addOnFailureListener { exception ->
                    Log.e("ResQSyncNetwork", "Packet sending failed", exception)
                }
        } catch (exception: Exception) {
            Log.e("ResQSyncNetwork", "Unable to serialize packet", exception)
        }
    }

    // Send an emergency report wrapped in a TransferPacket
    fun sendMessage(
        endpointId: String,
        message: NetworkMessage
    ) {
        val packet = TransferPacket(
            packetId = UUID.randomUUID().toString(),
            packetType = PacketType.EMERGENCY_REPORT,
            senderDeviceId = message.senderDeviceId,
            createdAt = System.currentTimeMillis(),
            networkMessage = message
        )
        sendPacket(endpointId, packet)
    }

    // Send a delivery acknowledgement receipt
    fun sendDeliveryAck(
        endpointId: String,
        acknowledgedMessageId: String,
        senderDeviceId: String
    ) {
        val ackPacket = TransferPacket(
            packetId = UUID.randomUUID().toString(),
            packetType = PacketType.DELIVERY_ACK,
            senderDeviceId = senderDeviceId,
            createdAt = System.currentTimeMillis(),
            acknowledgedMessageId = acknowledgedMessageId
        )
        sendPacket(endpointId, ackPacket)
    }

    // Send location assistance request to a nearby device
    fun sendLocationRequest(
        endpointId: String,
        senderDeviceId: String
    ) {
        val reqPacket = TransferPacket(
            packetId = UUID.randomUUID().toString(),
            packetType = PacketType.LOCATION_REQUEST,
            senderDeviceId = senderDeviceId,
            createdAt = System.currentTimeMillis()
        )
        sendPacket(endpointId, reqPacket)
    }

    // Send location response with explicitly shared GPS position
    fun sendLocationResponse(
        endpointId: String,
        location: EmergencyLocation,
        senderDeviceId: String
    ) {
        val respPacket = TransferPacket(
            packetId = UUID.randomUUID().toString(),
            packetType = PacketType.LOCATION_RESPONSE,
            senderDeviceId = senderDeviceId,
            createdAt = System.currentTimeMillis(),
            requestedLocation = location
        )
        sendPacket(endpointId, respPacket)
    }


    // Disconnect from one endpoint
    fun disconnectDevice(endpointId: String) {

        connectionsClient.disconnectFromEndpoint(endpointId)

        Log.d(
            "ResQSyncNetwork",
            "Disconnect requested: $endpointId"
        )
    }


    // Disconnect all endpoints
    fun disconnectAll() {

        connectionsClient.stopAllEndpoints()

        Log.d(
            "ResQSyncNetwork",
            "All nearby endpoints stopped"
        )
    }
}
