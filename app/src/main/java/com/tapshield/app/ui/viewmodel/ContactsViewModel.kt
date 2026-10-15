package com.tapshield.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tapshield.app.data.repository.ContactRepository
import com.tapshield.app.domain.model.Contact
import com.tapshield.app.domain.validation.ContactValidationResult
import com.tapshield.app.domain.validation.ContactValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ContactFormState(
    val id: Long = 0,
    val name: String = "",
    val phoneNumber: String = "",
    val relationship: String = "",
    val isPrimary: Boolean = false,
    val notifyBySms: Boolean = true,
    val nameError: String? = null,
    val phoneError: String? = null,
    val isEditing: Boolean = false
)

class ContactsViewModel(
    private val contactRepository: ContactRepository
) : ViewModel() {

    val contacts: StateFlow<List<Contact>> = contactRepository.getAllContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _formState = MutableStateFlow<ContactFormState?>(null)
    val formState: StateFlow<ContactFormState?> = _formState.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun openAddContactDialog() {
        val currentCount = contacts.value.size
        _formState.value = ContactFormState(
            isPrimary = currentCount == 0 // First contact is automatically suggested as primary
        )
    }

    fun openEditContactDialog(contact: Contact) {
        _formState.value = ContactFormState(
            id = contact.id,
            name = contact.name,
            phoneNumber = contact.phoneNumber,
            relationship = contact.relationship,
            isPrimary = contact.isPrimary,
            notifyBySms = contact.notifyBySms,
            isEditing = true
        )
    }

    fun dismissDialog() {
        _formState.value = null
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun updateName(name: String) {
        val current = _formState.value ?: return
        val err = when (val res = ContactValidator.validateName(name)) {
            is ContactValidationResult.Invalid -> res.error
            ContactValidationResult.Valid -> null
        }
        _formState.value = current.copy(name = name, nameError = err)
    }

    fun updatePhone(phone: String) {
        val current = _formState.value ?: return
        val err = when (val res = ContactValidator.validatePhoneNumber(phone)) {
            is ContactValidationResult.Invalid -> res.error
            ContactValidationResult.Valid -> null
        }
        _formState.value = current.copy(phoneNumber = phone, phoneError = err)
    }

    fun updateRelationship(relationship: String) {
        val current = _formState.value ?: return
        _formState.value = current.copy(relationship = relationship)
    }

    fun updateIsPrimary(isPrimary: Boolean) {
        val current = _formState.value ?: return
        _formState.value = current.copy(isPrimary = isPrimary)
    }

    fun updateNotifyBySms(notify: Boolean) {
        val current = _formState.value ?: return
        _formState.value = current.copy(notifyBySms = notify)
    }

    fun saveContact() {
        val current = _formState.value ?: return

        val validation = ContactValidator.validate(current.name, current.phoneNumber)
        if (validation is ContactValidationResult.Invalid) {
            _userMessage.value = validation.error
            return
        }

        val contact = Contact(
            id = current.id,
            name = current.name.trim(),
            phoneNumber = current.phoneNumber.trim(),
            relationship = current.relationship.trim(),
            isPrimary = current.isPrimary,
            notifyBySms = current.notifyBySms
        )

        viewModelScope.launch {
            val result = if (current.isEditing) {
                contactRepository.updateContact(contact)
            } else {
                contactRepository.addContact(contact)
            }

            result.onSuccess {
                _formState.value = null
                _userMessage.value = if (current.isEditing) "Contact updated" else "Contact added"
            }.onFailure { err ->
                _userMessage.value = err.message ?: "Failed to save contact"
            }
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            contactRepository.deleteContact(contact)
            _userMessage.value = "${contact.name} removed"
        }
    }

    fun setPrimaryContact(contactId: Long) {
        viewModelScope.launch {
            contactRepository.setPrimaryContact(contactId)
            _userMessage.value = "Primary contact updated"
        }
    }
}
