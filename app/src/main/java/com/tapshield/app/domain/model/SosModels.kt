package com.tapshield.app.domain.model

enum class SosStatus {
    IDLE,
    COUNTDOWN,
    ACTIVE,
    RESOLVED_SAFE,
    CANCELLED
}

enum class TriggerSource {
    IN_APP_BUTTON,
    WIDGET,
    QUICK_SETTINGS_TILE,
    SHAKE_GESTURE,
    TEST_MODE
}

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 0f,
    val altitudeMeters: Double = 0.0,
    val speedMps: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val provider: String = "gps"
)

enum class DeliveryChannel {
    LOCAL_PERSISTENCE,
    FIREBASE_REALTIME,
    SMS
}

enum class DeliveryStatus {
    PENDING,
    SENT,
    DELIVERED,
    FAILED,
    SKIPPED_NO_PERMISSION,
    SKIPPED_NO_NETWORK
}

data class ChannelDeliveryState(
    val channel: DeliveryChannel,
    val status: DeliveryStatus,
    val message: String? = null,
    val lastAttemptTimestamp: Long = 0L
)

data class SosSession(
    val sessionId: String,
    val status: SosStatus,
    val startedAt: Long,
    val triggerSource: TriggerSource,
    val lastLocation: LocationPoint? = null,
    val locationHistoryCount: Int = 0,
    val deliveryStates: Map<DeliveryChannel, ChannelDeliveryState> = emptyMap(),
    val publicShareToken: String? = null,
    val endedAt: Long? = null,
    val resolutionReason: String? = null
)
