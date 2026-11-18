
package com.resqsync.app.models

data class DeliveryReceipt(
    val messageId: String,
    val deviceId: String,
    val status: DeliveryStatus,
    val receivedAt: Long?,
    val forwardedAt: Long?
)
