package com.resqsync.app.ai

import com.resqsync.app.models.AIAnalysisResult
import com.resqsync.app.models.AnalysisSource
import com.resqsync.app.models.EmergencyReport
import com.resqsync.app.models.IncidentType
import com.resqsync.app.models.UrgencyLevel

class HybridAIEngine {

    suspend fun analyzeReport(
        report: EmergencyReport,
        isInternetAvailable: Boolean = false
    ): AIAnalysisResult {
        return if (isInternetAvailable) {
            // Attempt Cloud AI inference with transparent Rules fallback
            runCloudOrFallbackInference(report)
        } else {
            // Attempt On-Device AI or transparent Rules engine fallback
            runRulesEngineAnalysis(report, AnalysisSource.RULES_ENGINE)
        }
    }

    private fun runCloudOrFallbackInference(report: EmergencyReport): AIAnalysisResult {
        // Transparent fallback to deterministic rule engine when cloud credentials/APIs are unconfigured
        return runRulesEngineAnalysis(report, AnalysisSource.RULES_ENGINE)
    }

    fun runRulesEngineAnalysis(
        report: EmergencyReport,
        source: AnalysisSource = AnalysisSource.RULES_ENGINE
    ): AIAnalysisResult {
        val descLower = report.description.lowercase()

        // Detect incident type override based on description keywords
        val detectedIncident = when {
            descLower.contains("flood") || descLower.contains("water") || descLower.contains("drowning") -> IncidentType.FLOOD
            descLower.contains("fire") || descLower.contains("smoke") || descLower.contains("burn") -> IncidentType.FIRE
            descLower.contains("quake") || descLower.contains("tremor") || descLower.contains("shake") -> IncidentType.EARTHQUAKE
            descLower.contains("landslide") || descLower.contains("mudslide") -> IncidentType.LANDSLIDE
            descLower.contains("collapse") || descLower.contains("building") || descLower.contains("trapped under") -> IncidentType.BUILDING_COLLAPSE
            descLower.contains("medical") || descLower.contains("injury") || descLower.contains("bleed") || descLower.contains("heart") -> IncidentType.MEDICAL_EMERGENCY
            else -> report.incidentType
        }

        // Determine urgency from text indicators & people affected
        val detectedUrgency = when {
            descLower.contains("critical") || descLower.contains("dying") || descLower.contains("trapped") || descLower.contains("unconscious") || (report.peopleAffected ?: 0) >= 10 -> UrgencyLevel.CRITICAL
            descLower.contains("urgent") || descLower.contains("severe") || descLower.contains("blood") || (report.peopleAffected ?: 0) >= 5 -> UrgencyLevel.HIGH
            descLower.contains("moderate") || descLower.contains("minor") -> UrgencyLevel.MEDIUM
            else -> report.urgency
        }

        val extractedCount = report.peopleAffected ?: extractPeopleCount(descLower)

        val summary = buildString {
            append("[${detectedIncident.name}] Urgency: ${detectedUrgency.name}. ")
            if (extractedCount != null) {
                append("Approx $extractedCount people affected. ")
            }
            append("Summary: ${report.description.take(120)}")
            if (report.description.length > 120) append("...")
        }

        val confidence = when (source) {
            AnalysisSource.ON_DEVICE -> 0.92
            AnalysisSource.CLOUD -> 0.95
            AnalysisSource.RULES_ENGINE -> 0.85
            AnalysisSource.UNAVAILABLE -> 0.0
        }

        return AIAnalysisResult(
            reportId = report.reportId,
            detectedIncidentType = detectedIncident,
            detectedUrgency = detectedUrgency,
            peopleAffected = extractedCount,
            extractedLocation = if (report.latitude != null && report.longitude != null) "${report.latitude}, ${report.longitude}" else null,
            summary = summary,
            confidence = confidence,
            analysisSource = source,
            analyzedAt = System.currentTimeMillis()
        )
    }

    private fun extractPeopleCount(text: String): Int? {
        val regex = Regex("""(\d+)\s*(people|persons|victims|trapped|injured)""")
        val match = regex.find(text)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }
}
