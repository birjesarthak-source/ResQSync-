package com.resqsync.app.database.entity

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.resqsync.app.models.EmergencyLocation
import com.resqsync.app.models.EmergencyReport
import com.resqsync.app.models.IncidentType
import com.resqsync.app.models.LocationSource
import com.resqsync.app.models.UrgencyLevel
import com.resqsync.app.models.VerificationStatus
import org.json.JSONArray
import org.json.JSONException

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

    val locationSourceDeviceId: String? = null,

    val voiceBase64: String? = null,

    @ColumnInfo(defaultValue = "'[]'")
    val photoPaths: String = "[]"
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
        expiresAt = expiresAt,
        voiceBase64 = voiceBase64,
        localPhotoPaths = parsePhotoPaths(photoPaths)
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
        locationSourceDeviceId = location.sourceDeviceId,
        voiceBase64 = voiceBase64,
        photoPaths = JSONArray(localPhotoPaths).toString()
    )
}

private fun parsePhotoPaths(json: String): List<String> {
    if (json.isBlank()) return emptyList()

    return try {
        val paths = JSONArray(json)
        List(paths.length()) { index -> paths.getString(index) }
    } catch (e: JSONException) {
        Log.e("ResQSync", "Failed to read local photo attachments from report", e)
        emptyList()
    }
}
