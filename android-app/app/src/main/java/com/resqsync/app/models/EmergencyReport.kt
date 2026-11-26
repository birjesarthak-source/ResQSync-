package com.resqsync.app.models

import kotlinx.serialization.Serializable

@Serializable
data class EmergencyReport(
    val reportId: String,
    val reporterDeviceId: String,
    val incidentType: IncidentType,
    val description: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val location: EmergencyLocation = EmergencyLocation(
        latitude = latitude,
        longitude = longitude,
        source = if (latitude != null && longitude != null) LocationSource.GPS else LocationSource.UNAVAILABLE
    ),
    val urgency: UrgencyLevel,
    val peopleAffected: Int? = null,
    val verificationStatus: VerificationStatus,
    val createdAt: Long,
    val expiresAt: Long,
    val voiceBase64: String? = null
)
