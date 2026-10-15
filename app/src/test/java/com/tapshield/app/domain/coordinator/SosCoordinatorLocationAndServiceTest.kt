package com.tapshield.app.domain.coordinator

import com.tapshield.app.domain.model.LocationPoint
import com.tapshield.app.domain.model.SosStatus
import com.tapshield.app.domain.model.TriggerSource
import com.tapshield.app.domain.service.SosServiceLauncher
import com.tapshield.app.domain.state.SosState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeSosServiceLauncher : SosServiceLauncher {
    var startedSessionId: String? = null
        private set
    var startCallCount = 0
        private set
    var stopCallCount = 0
        private set

    override fun startSosService(sessionId: String) {
        startedSessionId = sessionId
        startCallCount++
    }

    override fun stopSosService() {
        stopCallCount++
    }

    fun reset() {
        startedSessionId = null
        startCallCount = 0
        stopCallCount = 0
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SosCoordinatorLocationAndServiceTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var repository: FakeSosRepository
    private lateinit var serviceLauncher: FakeSosServiceLauncher
    private lateinit var coordinator: SosCoordinator

    @Before
    fun setup() {
        repository = FakeSosRepository()
        serviceLauncher = FakeSosServiceLauncher()
        coordinator = SosCoordinator(
            sosRepository = repository,
            sosServiceLauncher = serviceLauncher,
            scope = testScope,
            dispatcher = testDispatcher
        )
    }

    @Test
    fun `activating SOS starts foreground service with session ID`() = testScope.runTest {
        advanceUntilIdle()
        coordinator.triggerSos(instant = true)
        advanceUntilIdle()

        val state = coordinator.state.value as SosState.Active
        assertEquals(1, serviceLauncher.startCallCount)
        assertEquals(state.session.sessionId, serviceLauncher.startedSessionId)
    }

    @Test
    fun `duplicate SOS triggers do not start service a second time`() = testScope.runTest {
        advanceUntilIdle()
        val firstResult = coordinator.triggerSos(instant = true)
        assertTrue(firstResult)
        advanceUntilIdle()

        val secondResult = coordinator.triggerSos(instant = true)
        assertFalse(secondResult)
        advanceUntilIdle()

        assertEquals(1, serviceLauncher.startCallCount)
    }

    @Test
    fun `resolving safe stops foreground service and updates status`() = testScope.runTest {
        advanceUntilIdle()
        coordinator.triggerSos(instant = true)
        advanceUntilIdle()

        coordinator.resolveSafe("User is safe")
        advanceUntilIdle()

        assertEquals(1, serviceLauncher.stopCallCount)
        val state = coordinator.state.value as SosState.SafeResolved
        assertEquals(SosStatus.RESOLVED_SAFE, state.session.status)
        assertEquals("User is safe", state.message)
    }

    @Test
    fun `cancelling countdown stops foreground service`() = testScope.runTest {
        advanceUntilIdle()
        coordinator.triggerSos(instant = false, countdownSeconds = 3)
        testDispatcher.scheduler.advanceTimeBy(100)

        coordinator.cancelCountdown("Cancelled by user")
        advanceUntilIdle()

        assertEquals(1, serviceLauncher.stopCallCount)
        assertTrue(coordinator.state.value is SosState.Cancelled)
    }

    @Test
    fun `sequential location updates persist and increment history count`() = testScope.runTest {
        advanceUntilIdle()
        coordinator.triggerSos(instant = true)
        advanceUntilIdle()

        val p1 = LocationPoint(19.0760, 72.8777, accuracyMeters = 10f, timestamp = 1000L)
        val p2 = LocationPoint(19.0765, 72.8780, accuracyMeters = 5f, timestamp = 2000L)
        val p3 = LocationPoint(19.0770, 72.8785, accuracyMeters = 3f, timestamp = 3000L)

        coordinator.updateLocation(p1)
        advanceUntilIdle()

        coordinator.updateLocation(p2)
        advanceUntilIdle()

        coordinator.updateLocation(p3)
        advanceUntilIdle()

        val state = coordinator.state.value as SosState.Active
        assertEquals(3, state.session.locationHistoryCount)
        assertEquals(p3.latitude, state.session.lastLocation?.latitude ?: 0.0, 0.0001)
        assertEquals(3f, state.session.lastLocation?.accuracyMeters ?: 0f, 0.01f)

        // Verify repository persistence
        val persisted = repository.getSessionById(state.session.sessionId)
        assertEquals(3, persisted?.locationHistoryCount)
        assertEquals(p3.latitude, persisted?.lastLocation?.latitude ?: 0.0, 0.0001)
    }
}
