package com.resqsync.app.database

import android.content.Context
import com.resqsync.app.database.repository.ReportRepository
import com.resqsync.app.database.repository.RoomReportRepository

class DatabaseManager(context: Context) {

    private val database =
        AppDatabase.getDatabase(context.applicationContext)

    val reportRepository: ReportRepository =
        RoomReportRepository(
            emergencyReportDao = database.emergencyReportDao(),
            networkMessageDao = database.networkMessageDao()
        )
}