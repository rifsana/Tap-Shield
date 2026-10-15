package com.tapshield.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sos_pending_events")
data class SosEventEntity(
    @PrimaryKey
    val eventId: String,
    val sessionId: String,
    val eventType: String, // "SOS_TRIGGERED", "LOCATION_UPDATE", "SAFE_RESOLVED", "CANCELLED"
    val payloadJson: String,
    val timestamp: Long,
    val retryCount: Int = 0,
    val maxRetries: Int = 5,
    val isDeliveredFirebase: Boolean = false,
    val isDeliveredSms: Boolean = false,
    val lastError: String? = null
)
