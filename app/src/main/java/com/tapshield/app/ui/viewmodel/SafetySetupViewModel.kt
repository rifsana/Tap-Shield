package com.tapshield.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SafetySetupState(
    val hasFineLocationPermission: Boolean = false,
    val hasBackgroundLocationPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val hasSmsPermission: Boolean = false,
    val isBatteryOptimizationIgnored: Boolean = false,
    val shakeTriggerEnabled: Boolean = false,
    val shakeSensitivity: Float = 2.5f // Threshold in Gs
)

class SafetySetupViewModel : ViewModel() {

    private val _setupState = MutableStateFlow(SafetySetupState())
    val setupState: StateFlow<SafetySetupState> = _setupState.asStateFlow()

    fun updatePermissions(
        fineLocation: Boolean,
        bgLocation: Boolean,
        notifications: Boolean,
        sms: Boolean,
        batteryIgnored: Boolean
    ) {
        _setupState.value = _setupState.value.copy(
            hasFineLocationPermission = fineLocation,
            hasBackgroundLocationPermission = bgLocation,
            hasNotificationPermission = notifications,
            hasSmsPermission = sms,
            isBatteryOptimizationIgnored = batteryIgnored
        )
    }

    fun toggleShakeTrigger(enabled: Boolean) {
        _setupState.value = _setupState.value.copy(shakeTriggerEnabled = enabled)
    }

    fun updateShakeSensitivity(sensitivity: Float) {
        _setupState.value = _setupState.value.copy(shakeSensitivity = sensitivity)
    }
}
