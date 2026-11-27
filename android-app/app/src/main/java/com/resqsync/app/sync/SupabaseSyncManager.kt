package com.resqsync.app.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.resqsync.app.BuildConfig
import com.resqsync.app.database.repository.ReportRepository
import com.resqsync.app.models.EmergencyReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SupabaseSyncManager(
    private val context: Context,
    private val reportRepository: ReportRepository
) {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun isInternetAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return true

        val network = connectivityManager.activeNetwork ?: return true
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return true

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun syncPendingReports(): SyncResult {
        return withContext(Dispatchers.IO) {
            if (!isInternetAvailable()) {
                Log.d("ResQSyncSync", "No internet connection available. Skipping cloud sync.")
                return@withContext SyncResult(success = false, syncedCount = 0, message = "Offline")
            }

            try {
                val unsyncedReports = reportRepository.getUnsyncedReports()
                if (unsyncedReports.isEmpty()) {
                    return@withContext SyncResult(success = true, syncedCount = 0, message = "All reports synced")
                }

                var successCount = 0
                for (report in unsyncedReports) {
                    val synced = syncSingleReportToCloud(report)
                    if (synced) {
                        reportRepository.markReportSynced(report.reportId)
                        successCount++
                    }
                }

                Log.d("ResQSyncSync", "Successfully synced $successCount reports to Supabase cloud.")
                SyncResult(
                    success = true,
                    syncedCount = successCount,
                    message = "Synced $successCount reports to cloud"
                )
            } catch (e: Exception) {
                Log.e("ResQSyncSync", "Cloud sync failed", e)
                SyncResult(success = false, syncedCount = 0, message = "Sync error: ${e.message}")
            }
        }
    }

    private fun syncSingleReportToCloud(report: EmergencyReport): Boolean {
        return executePostToSupabase(report)
    }

    private fun executePostToSupabase(report: EmergencyReport): Boolean {
        val supabaseUrl = BuildConfig.SUPABASE_URL
        val supabaseKey = BuildConfig.SUPABASE_ANON_KEY

        if (supabaseUrl.contains("your-project") || supabaseKey.contains("your-anon-key")) {
            Log.w("ResQSyncSync", "Supabase credentials not configured in BuildConfig.")
            return false
        }

        val endpoint = "${supabaseUrl.trimEnd('/')}/rest/v1/emergency_reports?on_conflict=report_id"
        val jsonObject = JSONObject().apply {
            put("report_id", report.reportId)
            put("reporter_device_id", report.reporterDeviceId)
            put("incident_type", report.incidentType.name)
            put("description", report.description)

            val lat = report.location.latitude ?: report.latitude
            val lng = report.location.longitude ?: report.longitude
            if (lat != null) put("latitude", lat) else put("latitude", JSONObject.NULL)
            if (lng != null) put("longitude", lng) else put("longitude", JSONObject.NULL)

            put("urgency", report.urgency.name)
            put("people_affected", report.peopleAffected ?: 0)
            put("verification_status", "UNVERIFIED")

            if (!report.location.landmark.isNullOrBlank()) {
                put("ai_summary", "Landmark: ${report.location.landmark} | ${report.description}")
            } else {
                put("ai_summary", report.description)
            }

            if (!report.voiceBase64.isNullOrBlank()) {
                put("voice_base64", report.voiceBase64)
            }

            put("created_at", report.createdAt)
            put("expires_at", report.expiresAt)
        }

        val jsonBody = jsonObject.toString()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer $supabaseKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=minimal, resolution=merge-duplicates")
            .post(requestBody)
            .build()

        return try {
            val response = httpClient.newCall(request).execute()
            response.use { resp ->
                val bodyStr = resp.body?.string() ?: ""
                val isSuccess = resp.isSuccessful
                if (isSuccess) {
                    Log.d("ResQSyncSync", "Report ${report.reportId} posted to Supabase successfully: HTTP ${resp.code}")
                    true
                } else {
                    Log.e("ResQSyncSync", "Failed to sync report ${report.reportId}: HTTP ${resp.code} Body: $bodyStr")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("ResQSyncSync", "Network error posting report ${report.reportId} to Supabase", e)
            false
        }
    }
}

data class SyncResult(
    val success: Boolean,
    val syncedCount: Int,
    val message: String
)
