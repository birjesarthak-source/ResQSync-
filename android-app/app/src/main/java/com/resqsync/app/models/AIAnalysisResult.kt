
package com.resqsync.app.models

data class AIAnalysisResult(
    val reportId: String,
    val detectedIncidentType: IncidentType,
    val detectedUrgency: UrgencyLevel,
    val peopleAffected: Int?,
    val extractedLocation: String?,
    val summary: String,
    val confidence: Double?,
    val analysisSource: AnalysisSource,
    val analyzedAt: Long
)
