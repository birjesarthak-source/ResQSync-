
package com.resqsync.app.models

data class DeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val deviceRole: DeviceRole,
    val isConnected: Boolean,
    val lastSeenAt: Long
)
