package com.tapshield.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tapshield.app.domain.model.LocationPoint
import com.tapshield.app.domain.model.SosSession
import com.tapshield.app.domain.model.SosStatus
import com.tapshield.app.domain.model.TriggerSource

@Entity(tableName = "sos_sessions")
data class SosSessionEntity(
    @PrimaryKey
    val sessionId: String,
    val status: String,
    val startedAt: Long,
    val triggerSource: String,
    val lastLatitude: Double?,
    val lastLongitude: Double?,
    val lastAccuracy: Float?,
    val lastAltitude: Double?,
    val lastSpeed: Float?,
    val lastLocationTimestamp: Long?,
    val locationHistoryCount: Int = 0,
    val publicShareToken: String?,
    val endedAt: Long?,
    val resolutionReason: String?
) {
    fun toDomain(): SosSession {
        val lastLoc = if (lastLatitude != null && lastLongitude != null) {
            LocationPoint(
                latitude = lastLatitude,
                longitude = lastLongitude,
                accuracyMeters = lastAccuracy ?: 0f,
                altitudeMeters = lastAltitude ?: 0.0,
                speedMps = lastSpeed ?: 0f,
                timestamp = lastLocationTimestamp ?: startedAt
            )
        } else null

        return SosSession(
            sessionId = sessionId,
            status = runCatching { SosStatus.valueOf(status) }.getOrDefault(SosStatus.IDLE),
            startedAt = startedAt,
            triggerSource = runCatching { TriggerSource.valueOf(triggerSource) }.getOrDefault(TriggerSource.IN_APP_BUTTON),
            lastLocation = lastLoc,
            locationHistoryCount = locationHistoryCount,
            publicShareToken = publicShareToken,
            endedAt = endedAt,
            resolutionReason = resolutionReason
        )
    }

    companion object {
        fun fromDomain(session: SosSession): SosSessionEntity = SosSessionEntity(
            sessionId = session.sessionId,
            status = session.status.name,
            startedAt = session.startedAt,
            triggerSource = session.triggerSource.name,
            lastLatitude = session.lastLocation?.latitude,
            lastLongitude = session.lastLocation?.longitude,
            lastAccuracy = session.lastLocation?.accuracyMeters,
            lastAltitude = session.lastLocation?.altitudeMeters,
            lastSpeed = session.lastLocation?.speedMps,
            lastLocationTimestamp = session.lastLocation?.timestamp,
            locationHistoryCount = session.locationHistoryCount,
            publicShareToken = session.publicShareToken,
            endedAt = session.endedAt,
            resolutionReason = session.resolutionReason
        )
    }
}
