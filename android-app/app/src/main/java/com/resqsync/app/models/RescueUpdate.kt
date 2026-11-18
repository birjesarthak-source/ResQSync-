
package com.resqsync.app.models

data class RescueUpdate(
    val updateId: String,
    val reportId: String,
    val rescueTeamDeviceId: String,
    val message: String,
    val status: String,
    val createdAt: Long
)
