package com.resqsync.app.models

import kotlinx.serialization.Serializable

@Serializable
data class AIAnalysisResult(
    val reportId: String,
    val detectedIncidentType: IncidentType,
    val detectedUrgency: UrgencyLevel,
    val peopleAffected: Int? = null,
    val extractedLocation: String? = null,
    val summary: String,
    val confidence: Double? = null,
    val analysisSource: AnalysisSource,
    val analyzedAt: Long
)
