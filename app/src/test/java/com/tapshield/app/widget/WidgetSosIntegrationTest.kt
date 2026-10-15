package com.tapshield.app.widget

import com.tapshield.app.domain.coordinator.FakeSosRepository
import com.tapshield.app.domain.coordinator.FakeSosServiceLauncher
import com.tapshield.app.domain.coordinator.SosCoordinator
import com.tapshield.app.domain.model.SosStatus
import com.tapshield.app.domain.model.TriggerSource
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

@OptIn(ExperimentalCoroutinesApi::class)
class WidgetSosIntegrationTest {

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
    fun `widget trigger delegates to coordinator with TriggerSource WIDGET`() = testScope.runTest {
        advanceUntilIdle()

        val triggered = coordinator.triggerSos(
            source = TriggerSource.WIDGET,
            instant = true
        )

        assertTrue(triggered)
        advanceUntilIdle()

        val state = coordinator.state.value
        assertTrue(state is SosState.Active)
        assertEquals(TriggerSource.WIDGET, (state as SosState.Active).session.triggerSource)
        assertEquals(SosStatus.ACTIVE, state.session.status)
        assertEquals(1, serviceLauncher.startCallCount)
    }

    @Test
    fun `duplicate widget trigger while already active is rejected`() = testScope.runTest {
        advanceUntilIdle()

        val firstTrigger = coordinator.triggerSos(source = TriggerSource.WIDGET, instant = true)
        assertTrue(firstTrigger)
        advanceUntilIdle()

        // Second widget press must not duplicate session
        val secondTrigger = coordinator.triggerSos(source = TriggerSource.WIDGET, instant = true)
        assertFalse(secondTrigger)
        assertEquals(1, serviceLauncher.startCallCount)
    }

    @Test
    fun `widget countdown trigger and cancellation`() = testScope.runTest {
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
        assertEquals(TriggerSource.WIDGET, (state as SosState.Countdown).source)

        // Cancel countdown
        coordinator.cancelCountdown("User cancelled widget countdown")
        advanceUntilIdle()

        val cancelled = coordinator.state.value
        assertTrue(cancelled is SosState.Cancelled)
        assertEquals("User cancelled widget countdown", (cancelled as SosState.Cancelled).reason)
        assertEquals(1, serviceLauncher.stopCallCount)
    }

    @Test
    fun `widget state reflects safe resolution accurately`() = testScope.runTest {
        advanceUntilIdle()
        coordinator.triggerSos(source = TriggerSource.WIDGET, instant = true)
        advanceUntilIdle()

        coordinator.resolveSafe("Resolved safely")
        advanceUntilIdle()

        val state = coordinator.state.value
        assertTrue(state is SosState.SafeResolved)
        assertEquals(SosStatus.RESOLVED_SAFE, (state as SosState.SafeResolved).session.status)
        assertEquals(1, serviceLauncher.stopCallCount)
    }
}
