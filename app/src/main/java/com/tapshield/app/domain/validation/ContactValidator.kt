package com.tapshield.app.domain.validation

sealed class ContactValidationResult {
    data object Valid : ContactValidationResult()
    data class Invalid(val error: String) : ContactValidationResult()
}

object ContactValidator {

    fun validateName(name: String): ContactValidationResult {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> ContactValidationResult.Invalid("Name cannot be empty")
            trimmed.length < 2 -> ContactValidationResult.Invalid("Name must be at least 2 characters")
            trimmed.length > 50 -> ContactValidationResult.Invalid("Name cannot exceed 50 characters")
            else -> ContactValidationResult.Valid
        }
    }

    fun validatePhoneNumber(phone: String): ContactValidationResult {
        val trimmed = phone.trim()
        if (trimmed.isEmpty()) {
            return ContactValidationResult.Invalid("Phone number cannot be empty")
        }

        // Normalize by removing spaces, hyphens, parentheses
        val cleanNumber = trimmed.replace(Regex("[\\s\\-\\(\\)]"), "")

        // Check if starts with optional + and followed only by digits
        val phoneRegex = Regex("^\\+?[0-9]{7,15}$")
        return if (!phoneRegex.matches(cleanNumber)) {
            ContactValidationResult.Invalid("Invalid phone number format. Must contain 7 to 15 digits.")
        } else {
            ContactValidationResult.Valid
        }
    }

    fun validate(name: String, phone: String): ContactValidationResult {
        val nameRes = validateName(name)
        if (nameRes is ContactValidationResult.Invalid) return nameRes

        val phoneRes = validatePhoneNumber(phone)
        if (phoneRes is ContactValidationResult.Invalid) return phoneRes

        return ContactValidationResult.Valid
    }
}
