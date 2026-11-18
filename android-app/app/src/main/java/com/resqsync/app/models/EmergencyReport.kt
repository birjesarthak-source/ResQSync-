
package com.resqsync.app.models

data class EmergencyReport(
    val reportId: String,
    val reporterDeviceId: String,
    val incidentType: IncidentType,
    val description: String,
    val latitude: Double?,
    val longitude: Double?,
    val urgency: UrgencyLevel,
    val peopleAffected: Int?,
    val verificationStatus: VerificationStatus,
    val createdAt: Long,
    val expiresAt: Long
)
