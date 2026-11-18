
package com.resqsync.app.models

enum class IncidentType {
    FLOOD,
    FIRE,
    EARTHQUAKE,
    LANDSLIDE,
    BUILDING_COLLAPSE,
    MEDICAL_EMERGENCY,
    OTHER
}

enum class UrgencyLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL,
    UNKNOWN
}

enum class VerificationStatus {
    UNVERIFIED,
    CORROBORATED,
    HUMAN_REVIEWED,
    DISPUTED
}

enum class DeliveryStatus {
    CREATED,
    QUEUED,
    SENT_TO_PEER,
    RECEIVED,
    FORWARDED,
    DELIVERED,
    EXPIRED,
    FAILED
}

enum class DeviceRole {
    PUBLIC_USER,
    VOLUNTEER,
    RESCUE_TEAM
}

enum class AnalysisSource {
    ON_DEVICE,
    CLOUD,
    RULES_ENGINE,
    UNAVAILABLE
}
