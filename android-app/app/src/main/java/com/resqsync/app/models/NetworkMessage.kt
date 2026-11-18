
package com.resqsync.app.models

data class NetworkMessage(
    val messageId: String,
    val reportId: String,
    val senderDeviceId: String,
    val destinationId: String?,
    val createdAt: Long,
    val hopCount: Int,
    val maxHops: Int,
    val ttlExpiresAt: Long,
    val visitedDeviceIds: List<String>,
    val payload: EmergencyReport
)
