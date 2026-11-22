package com.resqsync.app.networking.routing

import com.resqsync.app.models.NetworkMessage

class MessageRouter {

    private val processedMessages = mutableSetOf<String>()

    fun shouldForward(
        message: NetworkMessage,
        currentDeviceId: String
    ): Boolean {

        // Don't forward an expired message
        if (System.currentTimeMillis() >= message.ttlExpiresAt) {
            return false
        }

        // Don't forward if maximum hops reached
        if (message.hopCount >= message.maxHops) {
            return false
        }

        // Don't forward to a device already visited
        if (currentDeviceId in message.visitedDeviceIds) {
            return false
        }

        // Don't process the same message twice
        if (message.messageId in processedMessages) {
            return false
        }

        return true
    }

    fun markAsProcessed(messageId: String) {
        processedMessages.add(messageId)
    }

    fun isProcessed(messageId: String): Boolean {
        return messageId in processedMessages
    }

    fun createForwardedMessage(
        message: NetworkMessage,
        currentDeviceId: String
    ): NetworkMessage {

        return message.copy(
            senderDeviceId = currentDeviceId,
            hopCount = message.hopCount + 1,
            visitedDeviceIds = message.visitedDeviceIds + currentDeviceId
        )
    }
}
