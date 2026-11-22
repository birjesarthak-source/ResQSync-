package com.resqsync.app.database.repository

import com.resqsync.app.models.DeliveryStatus
import com.resqsync.app.models.EmergencyReport
import com.resqsync.app.models.NetworkMessage
import kotlinx.coroutines.flow.Flow

interface ReportRepository {

    suspend fun saveReport(report: EmergencyReport)

    suspend fun getAllReports(): List<EmergencyReport>

    suspend fun getReportById(reportId: String): EmergencyReport?

    suspend fun getUnsyncedReports(): List<EmergencyReport>

    suspend fun markReportSynced(reportId: String)

    suspend fun updateReportAISummary(reportId: String, summary: String)

    suspend fun saveNetworkMessage(
        message: NetworkMessage,
        status: DeliveryStatus = DeliveryStatus.QUEUED
    )

    suspend fun getPendingMessages(): List<NetworkMessage>

    suspend fun updateMessageStatus(messageId: String, status: DeliveryStatus)

    suspend fun updateMessageStatusByReportId(reportId: String, status: DeliveryStatus)

    suspend fun cleanupExpiredRecords(): Int

    fun observeReports(): Flow<List<EmergencyReport>>

    fun observeMessages(): Flow<List<NetworkMessage>>
}
