package com.tapshield.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tapshield.app.data.local.entity.SosSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SosSessionDao {

    @Query("SELECT * FROM sos_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<SosSessionEntity>>

    @Query("SELECT * FROM sos_sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): SosSessionEntity?

    @Query("SELECT * FROM sos_sessions WHERE status IN ('ACTIVE', 'COUNTDOWN') ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveSession(): SosSessionEntity?

    @Query("SELECT * FROM sos_sessions WHERE status IN ('ACTIVE', 'COUNTDOWN') ORDER BY startedAt DESC LIMIT 1")
    fun observeActiveSession(): Flow<SosSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SosSessionEntity)

    @Update
    suspend fun updateSession(session: SosSessionEntity)

    @Query("UPDATE sos_sessions SET status = :status, endedAt = :endedAt, resolutionReason = :reason WHERE sessionId = :sessionId")
    suspend fun updateSessionStatus(sessionId: String, status: String, endedAt: Long, reason: String?)
}
