package com.resqsync.app.networking

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.resqsync.app.notifications.NotificationHelper

class MeshForegroundService : Service() {

    inner class LocalBinder : Binder() {
        fun getService(): MeshForegroundService = this@MeshForegroundService
    }

    private val binder = LocalBinder()
    private lateinit var notificationHelper: NotificationHelper

    companion object {
        const val ACTION_START_FOREGROUND = "com.resqsync.app.ACTION_START_FOREGROUND"
        const val ACTION_STOP_FOREGROUND = "com.resqsync.app.ACTION_STOP_FOREGROUND"

        fun startService(context: Context) {
            val intent = Intent(context, MeshForegroundService::class.java).apply {
                action = ACTION_START_FOREGROUND
            }
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, MeshForegroundService::class.java).apply {
                action = ACTION_STOP_FOREGROUND
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(applicationContext)
        Log.d("ResQSyncService", "Mesh Foreground Service Created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_FOREGROUND -> {
                val notification = notificationHelper.getForegroundServiceNotification(0)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NotificationHelper.FOREGROUND_SERVICE_NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                    )
                } else {
                    startForeground(NotificationHelper.FOREGROUND_SERVICE_NOTIFICATION_ID, notification)
                }
                Log.d("ResQSyncService", "Mesh Foreground Service Started")
            }
            ACTION_STOP_FOREGROUND -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                Log.d("ResQSyncService", "Mesh Foreground Service Stopped")
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    fun updateConnectedPeersCount(count: Int) {
        val notification = notificationHelper.getForegroundServiceNotification(count)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(NotificationHelper.FOREGROUND_SERVICE_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission denied
        }
    }
}
