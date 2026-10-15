package com.tapshield.app.data.repository

import com.tapshield.app.data.local.dao.SosEventDao
import com.tapshield.app.data.local.dao.SosSessionDao
import com.tapshield.app.data.local.entity.SosEventEntity
import com.tapshield.app.data.local.entity.SosSessionEntity
import com.tapshield.app.domain.model.LocationPoint
import com.tapshield.app.domain.model.SosSession
import com.tapshield.app.domain.model.SosStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SosRepository {
    fun getAllSessions(): Flow<List<SosSession>>
    fun observeActiveSession(): Flow<SosSession?>
    suspend fun getActiveSession(): SosSession?
    suspend fun getSessionById(sessionId: String): SosSession?
    suspend fun saveSession(session: SosSession)
    suspend fun updateLocation(sessionId: String, location: LocationPoint)
    suspend fun endSession(sessionId: String, status: SosStatus, reason: String?)
    suspend fun recordPendingEvent(
        sessionId: String,
        eventType: String,
        payloadJson: String
    ): String
    fun getPendingEventsCountFlow(): Flow<Int>
}

class SosRepositoryImpl(
    private val sessionDao: SosSessionDao,
    private val eventDao: SosEventDao
) : SosRepository {

    override fun getAllSessions(): Flow<List<SosSession>> {
        return sessionDao.getAllSessions().map { list -> list.map { it.toDomain() } }
    }

    override fun observeActiveSession(): Flow<SosSession?> {
        return sessionDao.observeActiveSession().map { it?.toDomain() }
    }

    override suspend fun getActiveSession(): SosSession? {
        return sessionDao.getActiveSession()?.toDomain()
    }

    override suspend fun getSessionById(sessionId: String): SosSession? {
        return sessionDao.getSessionById(sessionId)?.toDomain()
    }

    override suspend fun saveSession(session: SosSession) {
        sessionDao.insertSession(SosSessionEntity.fromDomain(session))
    }

    override suspend fun updateLocation(sessionId: String, location: LocationPoint) {
        val existing = sessionDao.getSessionById(sessionId)
        if (existing != null) {
            val updated = existing.copy(
                lastLatitude = location.latitude,
                lastLongitude = location.longitude,
                lastAccuracy = location.accuracyMeters,
                lastAltitude = location.altitudeMeters,
                lastSpeed = location.speedMps,
                lastLocationTimestamp = location.timestamp,
                locationHistoryCount = existing.locationHistoryCount + 1
            )
            sessionDao.updateSession(updated)
        }
    }

    override suspend fun endSession(sessionId: String, status: SosStatus, reason: String?) {
        sessionDao.updateSessionStatus(
            sessionId = sessionId,
            status = status.name,
            endedAt = System.currentTimeMillis(),
            reason = reason
        )
    }

    override suspend fun recordPendingEvent(
        sessionId: String,
        eventType: String,
        payloadJson: String
    ): String {
        val eventId = "evt_${System.currentTimeMillis()}_${(1000..9999).random()}"
        val entity = SosEventEntity(
            eventId = eventId,
            sessionId = sessionId,
            eventType = eventType,
            payloadJson = payloadJson,
            timestamp = System.currentTimeMillis()
        )
        eventDao.insertEvent(entity)
        return eventId
    }

    override fun getPendingEventsCountFlow(): Flow<Int> {
        return eventDao.getPendingCountFlow()
    }
}
