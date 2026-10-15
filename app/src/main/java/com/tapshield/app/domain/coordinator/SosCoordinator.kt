package com.tapshield.app.domain.coordinator

import com.tapshield.app.data.repository.SosRepository
import com.tapshield.app.domain.model.ChannelDeliveryState
import com.tapshield.app.domain.model.DeliveryChannel
import com.tapshield.app.domain.model.DeliveryStatus
import com.tapshield.app.domain.model.LocationPoint
import com.tapshield.app.domain.model.SosSession
import com.tapshield.app.domain.model.SosStatus
import com.tapshield.app.domain.model.TriggerSource
import com.tapshield.app.domain.service.SosServiceLauncher
import com.tapshield.app.domain.state.SosState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class SosCoordinator(
    private val sosRepository: SosRepository,
    private val sosServiceLauncher: SosServiceLauncher? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    private val _state = MutableStateFlow<SosState>(SosState.Idle)
    val state: StateFlow<SosState> = _state.asStateFlow()

    private var countdownJob: Job? = null

    init {
        // Safe recovery on startup: check if there's an ongoing active session from DB
        scope.launch(dispatcher) {
            val active = sosRepository.getActiveSession()
            if (active != null && active.status == SosStatus.ACTIVE) {
                _state.value = SosState.Active(active)
            }
        }
    }

    /**
     * Triggers an SOS session.
     * Returns true if successfully started, or false if an emergency is already active (prevents duplicates).
     */
    fun triggerSos(
        source: TriggerSource = TriggerSource.IN_APP_BUTTON,
        instant: Boolean = false,
        countdownSeconds: Int = 3
    ): Boolean {
        synchronized(this) {
            val current = _state.value
            if (current is SosState.Active || current is SosState.Countdown) {
                // Prevent duplicate session!
                return false
            }

            val sessionId = "sos_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"

            if (instant || countdownSeconds <= 0) {
                activateSession(sessionId, source)
            } else {
                startCountdown(sessionId, source, countdownSeconds)
            }
            return true
        }
    }

    private fun startCountdown(sessionId: String, source: TriggerSource, countdownSeconds: Int) {
        countdownJob?.cancel()
        countdownJob = scope.launch(dispatcher) {
            for (sec in countdownSeconds downTo 1) {
                _state.value = SosState.Countdown(
                    secondsRemaining = sec,
                    source = source,
                    sessionId = sessionId
                )
                delay(1000L)
            }
            // Once countdown completes without cancellation, activate session
            activateSession(sessionId, source)
        }
    }

    fun cancelCountdown(reason: String = "User cancelled countdown") {
        synchronized(this) {
            val current = _state.value
            if (current is SosState.Countdown) {
                countdownJob?.cancel()
                countdownJob = null
                sosServiceLauncher?.stopSosService()
                _state.value = SosState.Cancelled(reason)
            }
        }
    }

    private fun activateSession(sessionId: String, source: TriggerSource) {
        val shareToken = UUID.randomUUID().toString().replace("-", "").take(16)
        val initialDelivery = mapOf(
            DeliveryChannel.LOCAL_PERSISTENCE to ChannelDeliveryState(
                channel = DeliveryChannel.LOCAL_PERSISTENCE,
                status = DeliveryStatus.DELIVERED,
                message = "Session persisted to secure offline storage",
                lastAttemptTimestamp = System.currentTimeMillis()
            ),
            DeliveryChannel.FIREBASE_REALTIME to ChannelDeliveryState(
                channel = DeliveryChannel.FIREBASE_REALTIME,
                status = DeliveryStatus.PENDING,
                lastAttemptTimestamp = System.currentTimeMillis()
            ),
            DeliveryChannel.SMS to ChannelDeliveryState(
                channel = DeliveryChannel.SMS,
                status = DeliveryStatus.PENDING,
                lastAttemptTimestamp = System.currentTimeMillis()
            )
        )

        val session = SosSession(
            sessionId = sessionId,
            status = SosStatus.ACTIVE,
            startedAt = System.currentTimeMillis(),
            triggerSource = source,
            deliveryStates = initialDelivery,
            publicShareToken = shareToken
        )

        _state.value = SosState.Active(session)
        sosServiceLauncher?.startSosService(sessionId)

        scope.launch(dispatcher) {
            sosRepository.saveSession(session)
            sosRepository.recordPendingEvent(
                sessionId = sessionId,
                eventType = "SOS_TRIGGERED",
                payloadJson = """{"source":"${source.name}","token":"$shareToken"}"""
            )
        }
    }

    fun updateLocation(location: LocationPoint) {
        val current = _state.value
        if (current is SosState.Active) {
            val updatedSession = current.session.copy(
                lastLocation = location,
                locationHistoryCount = current.session.locationHistoryCount + 1
            )
            _state.value = SosState.Active(updatedSession)

            scope.launch(dispatcher) {
                sosRepository.updateLocation(current.session.sessionId, location)
                sosRepository.recordPendingEvent(
                    sessionId = current.session.sessionId,
                    eventType = "LOCATION_UPDATE",
                    payloadJson = """{"lat":${location.latitude},"lng":${location.longitude},"acc":${location.accuracyMeters},"time":${location.timestamp}}"""
                )
            }
        }
    }

    fun updateDeliveryStatus(
        channel: DeliveryChannel,
        status: DeliveryStatus,
        message: String? = null
    ) {
        val current = _state.value
        if (current is SosState.Active) {
            val newMap = current.session.deliveryStates.toMutableMap()
            newMap[channel] = ChannelDeliveryState(
                channel = channel,
                status = status,
                message = message,
                lastAttemptTimestamp = System.currentTimeMillis()
            )
            val updated = current.session.copy(deliveryStates = newMap)
            _state.value = SosState.Active(updated)
        }
    }

    fun resolveSafe(reason: String = "I am safe and out of danger") {
        synchronized(this) {
            val current = _state.value
            countdownJob?.cancel()
            countdownJob = null
            sosServiceLauncher?.stopSosService()

            if (current is SosState.Active) {
                val endedSession = current.session.copy(
                    status = SosStatus.RESOLVED_SAFE,
                    endedAt = System.currentTimeMillis(),
                    resolutionReason = reason
                )
                _state.value = SosState.SafeResolved(endedSession, reason)

                scope.launch(dispatcher) {
                    sosRepository.endSession(
                        sessionId = current.session.sessionId,
                        status = SosStatus.RESOLVED_SAFE,
                        reason = reason
                    )
                    sosRepository.recordPendingEvent(
                        sessionId = current.session.sessionId,
                        eventType = "SAFE_RESOLVED",
                        payloadJson = """{"reason":"$reason"}"""
                    )
                }
            } else if (current is SosState.Countdown) {
                _state.value = SosState.Cancelled("Cancelled before activation")
            }
        }
    }

    fun resetToIdle() {
        _state.value = SosState.Idle
    }
}
