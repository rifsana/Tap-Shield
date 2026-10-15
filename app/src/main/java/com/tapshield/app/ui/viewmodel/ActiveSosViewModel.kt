package com.tapshield.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tapshield.app.domain.coordinator.SosCoordinator
import com.tapshield.app.domain.state.SosState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ActiveSosViewModel(
    private val sosCoordinator: SosCoordinator
) : ViewModel() {

    val sosState: StateFlow<SosState> = sosCoordinator.state

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                val current = sosState.value
                if (current is SosState.Active) {
                    val duration = (System.currentTimeMillis() - current.session.startedAt) / 1000
                    _elapsedSeconds.value = duration.coerceAtLeast(0L)
                } else {
                    _elapsedSeconds.value = 0L
                }
                delay(1000L)
            }
        }
    }

    fun markSafe(reason: String = "Confirmed safe by user") {
        sosCoordinator.resolveSafe(reason)
    }

    fun resetToIdle() {
        sosCoordinator.resetToIdle()
    }
}
