package com.resqsync.app.notifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.resqsync.app.MainActivity
import com.resqsync.app.models.EmergencyReport
import com.resqsync.app.models.RescueUpdate
import com.resqsync.app.models.UrgencyLevel

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_CRITICAL_ALERTS = "resqsync_critical_alerts"
        const val CHANNEL_MESH_REPORTS = "resqsync_mesh_reports"
        const val CHANNEL_RESCUE_UPDATES = "resqsync_rescue_updates"
        const val CHANNEL_FOREGROUND_SERVICE = "resqsync_foreground_mesh"

        const val FOREGROUND_SERVICE_NOTIFICATION_ID = 1001
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val criticalChannel = NotificationChannel(
            CHANNEL_CRITICAL_ALERTS,
            "Critical Emergency SOS Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "High priority notifications for life-threatening emergency reports"
            enableVibration(true)
        }

        val meshChannel = NotificationChannel(
            CHANNEL_MESH_REPORTS,
            "Mesh Network Reports",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications when emergency reports are relayed from nearby mesh devices"
        }

        val updatesChannel = NotificationChannel(
            CHANNEL_RESCUE_UPDATES,
            "Official Rescue Broadcasts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Official rescue team announcements and status updates"
        }

        val fgChannel = NotificationChannel(
            CHANNEL_FOREGROUND_SERVICE,
            "Mesh Background Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Persistent notification keeping offline disaster mesh active in background"
        }

        notificationManager.createNotificationChannels(
            listOf(criticalChannel, meshChannel, updatesChannel, fgChannel)
        )
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    @SuppressLint("MissingPermission")
    fun showReportSubmittedNotification(report: EmergencyReport) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            report.reportId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MESH_REPORTS)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("🆘 SOS Emergency Report Saved")
            .setContentText("${report.incidentType.name.replace('_', ' ')}: Saved locally & broadcasting to nearby peers.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(report.reportId.hashCode(), notification)
        } catch (e: SecurityException) {
            // Permission denied
        }
    }

    @SuppressLint("MissingPermission")
    fun showDeliveryAckNotification(acknowledgedMessageId: String) {
        if (!hasNotificationPermission()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_MESH_REPORTS)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle("🤝 Nearby Device Receipt Acknowledged")
            .setContentText("A nearby peer device saved your emergency report into Room DB.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(acknowledgedMessageId.hashCode(), notification)
        } catch (e: SecurityException) {
            // Permission denied
        }
    }

    @SuppressLint("MissingPermission")
    fun showCloudSyncNotification(syncedCount: Int) {
        if (!hasNotificationPermission() || syncedCount <= 0) return

        val notification = NotificationCompat.Builder(context, CHANNEL_MESH_REPORTS)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle("☁️ Synced with Rescue Command Server")
            .setContentText("Successfully synced $syncedCount report(s) to Supabase cloud database.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(syncedCount.hashCode(), notification)
        } catch (e: SecurityException) {
            // Permission denied
        }
    }

    @SuppressLint("MissingPermission")
    fun showReportReceivedNotification(report: EmergencyReport) {
        if (!hasNotificationPermission()) return

        val isCritical = report.urgency == UrgencyLevel.CRITICAL
        val channelId = if (isCritical) CHANNEL_CRITICAL_ALERTS else CHANNEL_MESH_REPORTS

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            report.reportId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isCritical) "🚨 CRITICAL EMERGENCY ALERT" else "📡 New Mesh Emergency Report"
        val body = "${report.incidentType.name.replace('_', ' ')} (${report.urgency.name}): ${report.description.take(80)}"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(if (isCritical) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(report.reportId.hashCode(), notification)
        } catch (e: SecurityException) {
            // Permission denied
        }
    }

    @SuppressLint("MissingPermission")
    fun showRescueUpdateNotification(update: RescueUpdate) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            update.updateId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_RESCUE_UPDATES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("♧ Rescue Team Broadcast")
            .setContentText(update.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(update.updateId.hashCode(), notification)
        } catch (e: SecurityException) {
            // Permission denied
        }
    }

    fun getForegroundServiceNotification(connectedPeersCount: Int): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusText = if (connectedPeersCount > 0) {
            "Active Mesh Network: $connectedPeersCount peer(s) connected"
        } else {
            "Disaster Mesh Active: Scanning for nearby peers..."
        }

        return NotificationCompat.Builder(context, CHANNEL_FOREGROUND_SERVICE)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("ResQSync AI - Offline Mesh Active")
            .setContentText(statusText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }
}
