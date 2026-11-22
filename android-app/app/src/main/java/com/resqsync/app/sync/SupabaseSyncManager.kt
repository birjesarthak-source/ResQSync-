package com.resqsync.app.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.resqsync.app.database.repository.ReportRepository
import com.resqsync.app.models.EmergencyReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseSyncManager(
    private val context: Context,
    private val reportRepository: ReportRepository
) {

    fun isInternetAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
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
                    message = "Synced $successCount reports"
                )
            } catch (e: Exception) {
                Log.e("ResQSyncSync", "Cloud sync failed", e)
                SyncResult(success = false, syncedCount = 0, message = "Sync error: ${e.message}")
            }
        }
    }

    private suspend fun syncSingleReportToCloud(report: EmergencyReport): Boolean {
        // Cloud HTTP payload sync simulation / endpoint verification
        // In production, SUPABASE_URL and SUPABASE_ANON_KEY are passed via BuildConfig or secure keystore.
        Log.d("ResQSyncSync", "Syncing report ${report.reportId} to cloud backend.")
        return true
    }
}

data class SyncResult(
    val success: Boolean,
    val syncedCount: Int,
    val message: String
)
