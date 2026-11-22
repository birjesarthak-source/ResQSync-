package com.resqsync.app.models

import kotlinx.serialization.Serializable

@Serializable
enum class IncidentType {
    FLOOD, FIRE, EARTHQUAKE, LANDSLIDE,
    BUILDING_COLLAPSE, MEDICAL_EMERGENCY, OTHER
}

@Serializable
enum class UrgencyLevel {
    LOW, MEDIUM, HIGH, CRITICAL, UNKNOWN
}

@Serializable
enum class VerificationStatus {
    UNVERIFIED, CORROBORATED, HUMAN_REVIEWED, DISPUTED
}

@Serializable
enum class DeliveryStatus {
    CREATED, QUEUED, SENT_TO_PEER, RECEIVED,
    FORWARDED, DELIVERED, EXPIRED, FAILED
}

@Serializable
enum class DeviceRole {
    PUBLIC_USER, VOLUNTEER, RESCUE_TEAM
}

@Serializable
enum class AnalysisSource {
    ON_DEVICE, CLOUD, RULES_ENGINE, UNAVAILABLE
}