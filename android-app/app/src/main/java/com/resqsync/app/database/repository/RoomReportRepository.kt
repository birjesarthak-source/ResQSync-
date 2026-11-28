package com.resqsync.app.database.repository

import android.util.Log
import com.resqsync.app.database.dao.EmergencyReportDao
import com.resqsync.app.database.dao.NetworkMessageDao
import com.resqsync.app.database.entity.toEmergencyReport
import com.resqsync.app.database.entity.toEntity
import com.resqsync.app.database.entity.toNetworkMessage
import com.resqsync.app.database.entity.toNetworkMessageEntity
import com.resqsync.app.models.DeliveryStatus
import com.resqsync.app.models.EmergencyReport
import com.resqsync.app.models.NetworkMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class RoomReportRepository(
    private val emergencyReportDao: EmergencyReportDao,
    private val networkMessageDao: NetworkMessageDao
) : ReportRepository {

    override suspend fun saveReport(report: EmergencyReport) {
        emergencyReportDao.insertReport(report.toEntity())
    }

    override suspend fun getAllReports(): List<EmergencyReport> {
        return emergencyReportDao.getAllReports().map { it.toEmergencyReport() }
    }

    override suspend fun getReportById(reportId: String): EmergencyReport? {
        return emergencyReportDao.getReportById(reportId)?.toEmergencyReport()
    }

    override suspend fun getUnsyncedReports(): List<EmergencyReport> {
        return emergencyReportDao.getUnsyncedReports().map { it.toEmergencyReport() }
    }

    override suspend fun markReportSynced(reportId: String) {
        emergencyReportDao.markReportSynced(reportId)
    }

    override suspend fun updateReportAISummary(reportId: String, summary: String) {
        emergencyReportDao.updateReportAISummary(reportId, summary)
    }

    override suspend fun saveNetworkMessage(
        message: NetworkMessage,
        status: DeliveryStatus
    ) {
        networkMessageDao.insertMessage(message.toNetworkMessageEntity(status))
    }

    override suspend fun getPendingMessages(): List<NetworkMessage> {
        return networkMessageDao
            .getPendingMessages(System.currentTimeMillis())
            .map { it.toNetworkMessage() }
    }

    override suspend fun updateMessageStatus(messageId: String, status: DeliveryStatus) {
        networkMessageDao.updateMessageStatus(messageId, status.name)
    }

    override suspend fun updateMessageStatusByReportId(reportId: String, status: DeliveryStatus) {
        networkMessageDao.updateMessageStatusByReportId(reportId, status.name)
    }

    override suspend fun cleanupExpiredRecords(): Int {
        val now = System.currentTimeMillis()
        val expiredReportEntities = emergencyReportDao.getExpiredReports(now)
        val deletedReports = emergencyReportDao.deleteExpiredReports(now)
        expiredReportEntities
            .flatMap { it.toEmergencyReport().localPhotoPaths }
            .forEach { path ->
                val photoFile = File(path)
                if (photoFile.exists() && !photoFile.delete()) {
                    Log.e("ResQSync", "Failed to remove expired report photo: $path")
                }
            }
        val expiredMsgs = networkMessageDao.deleteExpiredMessages(now)
        return deletedReports + expiredMsgs
    }

    override fun observeReports(): Flow<List<EmergencyReport>> {
        return emergencyReportDao.observeAllReports().map { entities ->
            entities.map { it.toEmergencyReport() }
        }
    }

    override fun observeMessages(): Flow<List<NetworkMessage>> {
        return networkMessageDao.observeAllMessages().map { entities ->
            entities.map { it.toNetworkMessage() }
        }
    }
}
