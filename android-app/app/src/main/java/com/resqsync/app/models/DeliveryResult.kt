package com.resqsync.app.models

import kotlinx.serialization.Serializable

@Serializable
data class DeliveryReceipt(
    val messageId: String,
    val deviceId: String,
    val status: DeliveryStatus,
    val receivedAt: Long? = null,
    val forwardedAt: Long? = null
)
