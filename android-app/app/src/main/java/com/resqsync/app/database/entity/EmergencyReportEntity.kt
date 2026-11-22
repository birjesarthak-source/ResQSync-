package com.resqsync.app.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.resqsync.app.models.EmergencyLocation
import com.resqsync.app.models.EmergencyReport
import com.resqsync.app.models.IncidentType
import com.resqsync.app.models.LocationSource
import com.resqsync.app.models.UrgencyLevel
import com.resqsync.app.models.VerificationStatus

@Entity(tableName = "emergency_reports")
data class EmergencyReportEntity(

    @PrimaryKey
    val reportId: String,

    val reporterDeviceId: String,

    val incidentType: String,

    val description: String,

    val latitude: Double?,

    val longitude: Double?,

    val urgency: String,

    val peopleAffected: Int?,

    val verificationStatus: String,

    val createdAt: Long,

    val expiresAt: Long,

    val isSyncedToCloud: Boolean = false,

    val aiSummary: String? = null,

    val locationSource: String = "UNAVAILABLE",

    val locationAccuracyMeters: Float? = null,

    val locationAltitudeMeters: Double? = null,

    val locationTimestamp: Long? = null,

    val locationAddress: String? = null,

    val locationLandmark: String? = null,

    val locationSourceDeviceId: String? = null
)

fun EmergencyReportEntity.toEmergencyReport(): EmergencyReport {
    val sourceEnum = try { LocationSource.valueOf(locationSource) } catch (e: Exception) { LocationSource.UNAVAILABLE }
    val emergencyLoc = EmergencyLocation(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = locationAccuracyMeters,
        altitudeMeters = locationAltitudeMeters,
        timestamp = locationTimestamp,
        source = sourceEnum,
        address = locationAddress,
        landmark = locationLandmark,
        sourceDeviceId = locationSourceDeviceId
    )

    return EmergencyReport(
        reportId = reportId,
        reporterDeviceId = reporterDeviceId,
        incidentType = try { IncidentType.valueOf(incidentType) } catch (e: Exception) { IncidentType.OTHER },
        description = description,
        latitude = latitude,
        longitude = longitude,
        location = emergencyLoc,
        urgency = try { UrgencyLevel.valueOf(urgency) } catch (e: Exception) { UrgencyLevel.HIGH },
        peopleAffected = peopleAffected,
        verificationStatus = try { VerificationStatus.valueOf(verificationStatus) } catch (e: Exception) { VerificationStatus.UNVERIFIED },
        createdAt = createdAt,
        expiresAt = expiresAt
    )
}

fun EmergencyReport.toEntity(isSyncedToCloud: Boolean = false, aiSummary: String? = null): EmergencyReportEntity {
    return EmergencyReportEntity(
        reportId = reportId,
        reporterDeviceId = reporterDeviceId,
        incidentType = incidentType.name,
        description = description,
        latitude = location.latitude ?: latitude,
        longitude = location.longitude ?: longitude,
        urgency = urgency.name,
        peopleAffected = peopleAffected,
        verificationStatus = verificationStatus.name,
        createdAt = createdAt,
        expiresAt = expiresAt,
        isSyncedToCloud = isSyncedToCloud,
        aiSummary = aiSummary,
        locationSource = location.source.name,
        locationAccuracyMeters = location.accuracyMeters,
        locationAltitudeMeters = location.altitudeMeters,
        locationTimestamp = location.timestamp,
        locationAddress = location.address,
        locationLandmark = location.landmark,
        locationSourceDeviceId = location.sourceDeviceId
    )
}
