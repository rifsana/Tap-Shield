package com.tapshield.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tapshield.app.data.repository.ContactRepository
import com.tapshield.app.data.repository.SosRepository
import com.tapshield.app.domain.coordinator.SosCoordinator
import com.tapshield.app.domain.model.TriggerSource
import com.tapshield.app.domain.state.SosState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val sosCoordinator: SosCoordinator,
    private val contactRepository: ContactRepository,
    private val sosRepository: SosRepository
) : ViewModel() {

    val sosState: StateFlow<SosState> = sosCoordinator.state

    val contactCount: StateFlow<Int> = contactRepository.getContactCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingEventsCount: StateFlow<Int> = sosRepository.getPendingEventsCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isStealthDisguiseMode = MutableStateFlow(false)
    val isStealthDisguiseMode: StateFlow<Boolean> = _isStealthDisguiseMode.asStateFlow()

    fun triggerSos(instant: Boolean = false) {
        sosCoordinator.triggerSos(
            source = TriggerSource.IN_APP_BUTTON,
            instant = instant,
            countdownSeconds = 3
        )
    }

    fun cancelCountdown() {
        sosCoordinator.cancelCountdown("Cancelled by user on home screen")
    }

    fun toggleStealthMode() {
        _isStealthDisguiseMode.value = !_isStealthDisguiseMode.value
    }
}
