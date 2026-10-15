package com.tapshield.app.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.tapshield.app.MainActivity
import com.tapshield.app.TapShieldApp
import com.tapshield.app.domain.coordinator.SosCoordinator
import com.tapshield.app.domain.model.TriggerSource
import com.tapshield.app.domain.state.SosState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SosTileService : TileService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var stateObserverJob: Job? = null

    private val coordinator: SosCoordinator?
        get() = (applicationContext as? TapShieldApp)?.container?.sosCoordinator

    override fun onStartListening() {
        super.onStartListening()
        stateObserverJob?.cancel()
        stateObserverJob = serviceScope.launch {
            coordinator?.state?.collectLatest { sosState ->
                updateTileUi(sosState)
            }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        stateObserverJob?.cancel()
        stateObserverJob = null
    }

    override fun onClick() {
        super.onClick()
        val currentCoordinator = coordinator ?: return
        val currentState = currentCoordinator.state.value

        when (currentState) {
            is SosState.Active -> {
                // Already active: open the app to allow the user to view telemetry or mark safe
                openActiveSosScreen()
            }
            is SosState.Countdown -> {
                // Tapping during countdown cancels the emergency activation
                currentCoordinator.cancelCountdown("Cancelled from Quick Settings Tile")
            }
            is SosState.Idle, is SosState.SafeResolved, is SosState.Cancelled -> {
                // Trigger SOS with 3s safety countdown
                currentCoordinator.triggerSos(
                    source = TriggerSource.QUICK_SETTINGS_TILE,
                    instant = false,
                    countdownSeconds = 3
                )
                openActiveSosScreen()
            }
        }
    }

    private fun updateTileUi(state: SosState) {
        val tile = qsTile ?: return

        when (state) {
            is SosState.Idle -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = "TapShield SOS"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Standby"
                }
            }
            is SosState.Countdown -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = "Cancel SOS (${state.secondsRemaining}s)"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Tap to abort"
                }
            }
            is SosState.Active -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = "TapShield SOS"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "ACTIVE • BROADCASTING"
                }
            }
            is SosState.SafeResolved -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = "TapShield SOS"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Resolved • Safe"
                }
            }
            is SosState.Cancelled -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = "TapShield SOS"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Standby"
                }
            }
        }

        try {
            tile.updateTile()
        } catch (_: Exception) {
            // Best effort update
        }
    }

    private fun openActiveSosScreen() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(SosForegroundService.EXTRA_NAV_ROUTE, "active_sos")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
