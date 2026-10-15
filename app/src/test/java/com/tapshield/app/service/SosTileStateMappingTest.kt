package com.tapshield.app.service

import com.tapshield.app.domain.coordinator.FakeSosRepository
import com.tapshield.app.domain.coordinator.SosCoordinator
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
class SosTileStateMappingTest {

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
    fun `quick settings trigger activates countdown and prevents duplicate session`() = testScope.runTest {
        // Assert initial idle state
        assertTrue(coordinator.state.value is SosState.Idle)

        // Trigger via Quick Settings Tile with 3s countdown
        val success = coordinator.triggerSos(
            source = TriggerSource.QUICK_SETTINGS_TILE,
            instant = false,
            countdownSeconds = 3
        )
        assertTrue(success)
        testDispatcher.scheduler.advanceTimeBy(100)

        val countdownState = coordinator.state.value as SosState.Countdown
        assertEquals(3, countdownState.secondsRemaining)
        assertEquals(TriggerSource.QUICK_SETTINGS_TILE, countdownState.source)

        // Second click on tile while countdown is active should be rejected as duplicate trigger
        val duplicateAttempt = coordinator.triggerSos(
            source = TriggerSource.QUICK_SETTINGS_TILE,
            instant = false
        )
        assertFalse(duplicateAttempt)
    }

    @Test
    fun `tile cancellation during countdown returns state to Cancelled`() = testScope.runTest {
        coordinator.triggerSos(
            source = TriggerSource.QUICK_SETTINGS_TILE,
            instant = false,
            countdownSeconds = 3
        )
        testDispatcher.scheduler.advanceTimeBy(100)
        assertTrue(coordinator.state.value is SosState.Countdown)

        // User taps tile again to abort
        coordinator.cancelCountdown("Cancelled from Quick Settings Tile")
        advanceUntilIdle()

        val cancelledState = coordinator.state.value as SosState.Cancelled
        assertEquals("Cancelled from Quick Settings Tile", cancelledState.reason)
    }

    @Test
    fun `active SOS cannot be re-triggered by Quick Settings Tile`() = testScope.runTest {
        coordinator.triggerSos(source = TriggerSource.QUICK_SETTINGS_TILE, instant = true)
        advanceUntilIdle()

        assertTrue(coordinator.state.value is SosState.Active)

        val duplicate = coordinator.triggerSos(source = TriggerSource.QUICK_SETTINGS_TILE)
        assertFalse(duplicate)
    }
}
