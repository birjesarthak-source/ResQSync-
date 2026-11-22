
package com.resqsync.app.networking.routing

import com.resqsync.app.models.NetworkMessage
import com.resqsync.app.networking.transport.MessageSender
import com.resqsync.app.networking.transport.MessageSerializer

class MessageForwarder(
    private val messageSender: MessageSender,
    private val messageSerializer: MessageSerializer,
    private val messageRouter: MessageRouter
) {

    fun forwardMessage(
        message: NetworkMessage,
        currentDeviceId: String,
        connectedDevices: List<String>
    ) {

        if (!messageRouter.shouldForward(message, currentDeviceId)) {
            return
        }

        val forwardedMessage =
            messageRouter.createForwardedMessage(
                message,
                currentDeviceId
            )

        messageRouter.markAsProcessed(message.messageId)

        val serializedMessage =
            messageSerializer.serialize(forwardedMessage)

        connectedDevices
            .filter { it != message.senderDeviceId }
            .filter { it !in forwardedMessage.visitedDeviceIds }
            .forEach { endpointId ->
                messageSender.sendMessage(
                    endpointId,
                    serializedMessage
                )

            }
    }
}
