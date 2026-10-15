package com.tapshield.app.domain.coordinator

import com.tapshield.app.data.repository.SosRepository
import com.tapshield.app.domain.model.DeliveryChannel
import com.tapshield.app.domain.model.DeliveryStatus
import com.tapshield.app.domain.model.LocationPoint
import com.tapshield.app.domain.model.SosSession
import com.tapshield.app.domain.model.SosStatus
import com.tapshield.app.domain.model.TriggerSource
import com.tapshield.app.domain.state.SosState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeSosRepository : SosRepository {
    private val sessions = mutableMapOf<String, SosSession>()
    private val events = mutableListOf<String>()

    override fun getAllSessions(): Flow<List<SosSession>> = flowOf(sessions.values.toList())
    override fun observeActiveSession(): Flow<SosSession?> = flowOf(sessions.values.find { it.status == SosStatus.ACTIVE })
    override suspend fun getActiveSession(): SosSession? = sessions.values.find { it.status == SosStatus.ACTIVE }
    override suspend fun getSessionById(sessionId: String): SosSession? = sessions[sessionId]
    override suspend fun saveSession(session: SosSession) {
        sessions[session.sessionId] = session
    }
    override suspend fun updateLocation(sessionId: String, location: LocationPoint) {
        sessions[sessionId]?.let {
            sessions[sessionId] = it.copy(lastLocation = location, locationHistoryCount = it.locationHistoryCount + 1)
        }
    }
    override suspend fun endSession(sessionId: String, status: SosStatus, reason: String?) {
        sessions[sessionId]?.let {
            sessions[sessionId] = it.copy(status = status, endedAt = System.currentTimeMillis(), resolutionReason = reason)
        }
    }
    override suspend fun recordPendingEvent(sessionId: String, eventType: String, payloadJson: String): String {
        val id = "evt_${System.currentTimeMillis()}"
        events.add(id)
        return id
    }
    override fun getPendingEventsCountFlow(): Flow<Int> = MutableStateFlow(events.size)
}

@OptIn(ExperimentalCoroutinesApi::class)
class SosCoordinatorTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var repository: FakeSosRepository
    private lateinit var coordinator: SosCoordinator

    @Before
    fun setup() {
        repository = FakeSosRepository()
        coordinator = SosCoordinator(
            sosRepository = repository,
            scope = testScope,
            dispatcher = testDispatcher
        )
    }

    @Test
    fun `initial state is Idle`() = testScope.runTest {
        advanceUntilIdle()
        assertTrue(coordinator.state.value is SosState.Idle)
    }

    @Test
    fun `triggering instant SOS transitions immediately to Active`() = testScope.runTest {
        advanceUntilIdle()
        val triggered = coordinator.triggerSos(
            source = TriggerSource.IN_APP_BUTTON,
            instant = true
        )

        assertTrue(triggered)
        advanceUntilIdle()

        val state = coordinator.state.value
        assertTrue(state is SosState.Active)
        assertEquals(TriggerSource.IN_APP_BUTTON, (state as SosState.Active).session.triggerSource)
        assertEquals(SosStatus.ACTIVE, state.session.status)
    }

    @Test
    fun `duplicate trigger while Active is rejected`() = testScope.runTest {
        advanceUntilIdle()
        val firstTrigger = coordinator.triggerSos(instant = true)
        assertTrue(firstTrigger)
        advanceUntilIdle()

        // Second trigger attempt should be rejected to prevent duplicate session
        val secondTrigger = coordinator.triggerSos(instant = true)
        assertFalse(secondTrigger)
    }

    @Test
    fun `countdown trigger starts countdown state and can be cancelled`() = testScope.runTest {
        advanceUntilIdle()
        val triggered = coordinator.triggerSos(
            source = TriggerSource.WIDGET,
            instant = false,
            countdownSeconds = 3
        )

        assertTrue(triggered)
        testDispatcher.scheduler.advanceTimeBy(100)

        val state = coordinator.state.value
        assertTrue(state is SosState.Countdown)

        coordinator.cancelCountdown("Accidental widget tap")
        advanceUntilIdle()

        val cancelledState = coordinator.state.value
        assertTrue(cancelledState is SosState.Cancelled)
        assertEquals("Accidental widget tap", (cancelledState as SosState.Cancelled).reason)
    }

    @Test
    fun `updating location in Active state updates session`() = testScope.runTest {
        advanceUntilIdle()
        coordinator.triggerSos(instant = true)
        advanceUntilIdle()

        val loc = LocationPoint(
            latitude = 28.6139,
            longitude = 77.2090,
            accuracyMeters = 8.5f,
            timestamp = System.currentTimeMillis()
        )
        coordinator.updateLocation(loc)
        advanceUntilIdle()

        val state = coordinator.state.value as SosState.Active
        assertEquals(loc.latitude, state.session.lastLocation?.latitude ?: 0.0, 0.0001)
        assertEquals(loc.longitude, state.session.lastLocation?.longitude ?: 0.0, 0.0001)
        assertEquals(1, state.session.locationHistoryCount)
    }

    @Test
    fun `resolving safe transitions to SafeResolved`() = testScope.runTest {
        advanceUntilIdle()
        coordinator.triggerSos(instant = true)
        advanceUntilIdle()

        coordinator.resolveSafe("Reached police station safely")
        advanceUntilIdle()

        val state = coordinator.state.value
        assertTrue(state is SosState.SafeResolved)
        assertEquals("Reached police station safely", (state as SosState.SafeResolved).message)
        assertEquals(SosStatus.RESOLVED_SAFE, state.session.status)
    }

    @Test
    fun `delivery status update reflects on active channels`() = testScope.runTest {
        advanceUntilIdle()
        coordinator.triggerSos(instant = true)
        advanceUntilIdle()

        coordinator.updateDeliveryStatus(
            channel = DeliveryChannel.SMS,
            status = DeliveryStatus.DELIVERED,
            message = "SMS dispatched to 2 guardians"
        )

        val state = coordinator.state.value as SosState.Active
        val smsState = state.session.deliveryStates[DeliveryChannel.SMS]
        assertEquals(DeliveryStatus.DELIVERED, smsState?.status)
        assertEquals("SMS dispatched to 2 guardians", smsState?.message)
    }
}
