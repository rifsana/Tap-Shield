package com.tapshield.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tapshield.app.TapShieldApp

@Suppress("UNCHECKED_CAST")
class TapShieldViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = TapShieldApp.instance
        val container = app.container

        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    sosCoordinator = container.sosCoordinator,
                    contactRepository = container.contactRepository,
                    sosRepository = container.sosRepository
                ) as T
            }
            modelClass.isAssignableFrom(ContactsViewModel::class.java) -> {
                ContactsViewModel(
                    contactRepository = container.contactRepository
                ) as T
            }
            modelClass.isAssignableFrom(ActiveSosViewModel::class.java) -> {
                ActiveSosViewModel(
                    sosCoordinator = container.sosCoordinator
                ) as T
            }
            modelClass.isAssignableFrom(SafetySetupViewModel::class.java) -> {
                SafetySetupViewModel() as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
