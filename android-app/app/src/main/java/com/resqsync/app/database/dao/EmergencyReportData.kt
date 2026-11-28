package com.resqsync.app.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.resqsync.app.database.entity.EmergencyReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyReportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: EmergencyReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<EmergencyReportEntity>)

    @Query("SELECT * FROM emergency_reports ORDER BY createdAt DESC")
    suspend fun getAllReports(): List<EmergencyReportEntity>

    @Query("SELECT * FROM emergency_reports ORDER BY createdAt DESC")
    fun observeAllReports(): Flow<List<EmergencyReportEntity>>

    @Query("SELECT * FROM emergency_reports WHERE reportId = :reportId")
    suspend fun getReportById(reportId: String): EmergencyReportEntity?

    @Query("SELECT * FROM emergency_reports WHERE isSyncedToCloud = 0")
    suspend fun getUnsyncedReports(): List<EmergencyReportEntity>

    @Query("UPDATE emergency_reports SET isSyncedToCloud = 1 WHERE reportId = :reportId")
    suspend fun markReportSynced(reportId: String)

    @Query("UPDATE emergency_reports SET verificationStatus = :status WHERE reportId = :reportId")
    suspend fun updateReportVerification(reportId: String, status: String)

    @Query("UPDATE emergency_reports SET aiSummary = :summary WHERE reportId = :reportId")
    suspend fun updateReportAISummary(reportId: String, summary: String)

    @Query("DELETE FROM emergency_reports WHERE reportId = :reportId")
    suspend fun deleteReport(reportId: String)

    @Query("DELETE FROM emergency_reports WHERE expiresAt <= :currentTime")
    suspend fun deleteExpiredReports(currentTime: Long): Int

    @Query("SELECT * FROM emergency_reports WHERE expiresAt <= :currentTime")
    suspend fun getExpiredReports(currentTime: Long): List<EmergencyReportEntity>

    @Query("DELETE FROM emergency_reports")
    suspend fun deleteAllReports()
}
