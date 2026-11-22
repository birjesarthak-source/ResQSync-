package com.resqsync.app.models

import kotlinx.serialization.Serializable

@Serializable
data class EmergencyLocation(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val altitudeMeters: Double? = null,
    val timestamp: Long? = null,
    val source: LocationSource = LocationSource.UNAVAILABLE,
    val address: String? = null,
    val landmark: String? = null,
    val sourceDeviceId: String? = null
) {
    init {
        if (latitude != null) {
            require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90" }
        }
        if (longitude != null) {
            require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180" }
        }
        if (accuracyMeters != null) {
            require(accuracyMeters >= 0f) { "Accuracy must be non-negative" }
        }
    }
}
