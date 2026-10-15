package com.tapshield.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tapshield.app.data.local.entity.SosEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SosEventDao {

    @Query("SELECT * FROM sos_pending_events WHERE isDeliveredFirebase = 0 OR isDeliveredSms = 0 ORDER BY timestamp ASC")
    fun getPendingEvents(): Flow<List<SosEventEntity>>

    @Query("SELECT * FROM sos_pending_events WHERE (isDeliveredFirebase = 0 OR isDeliveredSms = 0) AND retryCount < maxRetries ORDER BY timestamp ASC")
    suspend fun getEligibleRetryEvents(): List<SosEventEntity>

    @Query("SELECT COUNT(*) FROM sos_pending_events WHERE isDeliveredFirebase = 0 OR isDeliveredSms = 0")
    fun getPendingCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: SosEventEntity)

    @Update
    suspend fun updateEvent(event: SosEventEntity)

    @Query("UPDATE sos_pending_events SET isDeliveredFirebase = 1 WHERE eventId = :eventId")
    suspend fun markDeliveredFirebase(eventId: String)

    @Query("UPDATE sos_pending_events SET isDeliveredSms = 1 WHERE eventId = :eventId")
    suspend fun markDeliveredSms(eventId: String)

    @Query("DELETE FROM sos_pending_events WHERE isDeliveredFirebase = 1 AND isDeliveredSms = 1")
    suspend fun clearCompletedEvents()
}
