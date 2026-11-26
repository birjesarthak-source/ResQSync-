package com.resqsync.app.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.resqsync.app.database.entity.NetworkMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: NetworkMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<NetworkMessageEntity>)

    @Query("SELECT * FROM network_messages ORDER BY createdAt DESC")
    suspend fun getAllMessages(): List<NetworkMessageEntity>

    @Query("SELECT * FROM network_messages ORDER BY createdAt DESC")
    fun observeAllMessages(): Flow<List<NetworkMessageEntity>>

    @Query("SELECT * FROM network_messages WHERE messageId = :messageId")
    suspend fun getMessageById(messageId: String): NetworkMessageEntity?

    @Query(
        """
        SELECT * FROM network_messages
        WHERE deliveryStatus IN ('QUEUED', 'SENT_TO_PEER', 'FORWARDED')
        AND ttlExpiresAt > :currentTime
        ORDER BY createdAt ASC
        """
    )
    suspend fun getPendingMessages(currentTime: Long): List<NetworkMessageEntity>

    @Query("UPDATE network_messages SET deliveryStatus = :status WHERE messageId = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("UPDATE network_messages SET deliveryStatus = :status WHERE reportId = :reportId")
    suspend fun updateMessageStatusByReportId(reportId: String, status: String)

    @Query("DELETE FROM network_messages WHERE ttlExpiresAt <= :currentTime")
    suspend fun deleteExpiredMessages(currentTime: Long): Int

    @Query("DELETE FROM network_messages WHERE messageId = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM network_messages")
    suspend fun deleteAllMessages()
}
