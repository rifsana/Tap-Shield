package com.tapshield.app.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactValidatorTest {

    @Test
    fun `valid names return Valid`() {
        assertEquals(ContactValidationResult.Valid, ContactValidator.validateName("John Doe"))
        assertEquals(ContactValidationResult.Valid, ContactValidator.validateName("Priya Sharma"))
        assertEquals(ContactValidationResult.Valid, ContactValidator.validateName("A B"))
    }

    @Test
    fun `empty or blank name returns Invalid`() {
        val result1 = ContactValidator.validateName("")
        assertTrue(result1 is ContactValidationResult.Invalid)
        assertEquals("Name cannot be empty", (result1 as ContactValidationResult.Invalid).error)

        val result2 = ContactValidator.validateName("   ")
        assertTrue(result2 is ContactValidationResult.Invalid)
    }

    @Test
    fun `name shorter than 2 chars returns Invalid`() {
        val result = ContactValidator.validateName("J")
        assertTrue(result is ContactValidationResult.Invalid)
        assertEquals("Name must be at least 2 characters", (result as ContactValidationResult.Invalid).error)
    }

    @Test
    fun `valid phone numbers return Valid`() {
        assertEquals(ContactValidationResult.Valid, ContactValidator.validatePhoneNumber("+919876543210"))
        assertEquals(ContactValidationResult.Valid, ContactValidator.validatePhoneNumber("9876543210"))
        assertEquals(ContactValidationResult.Valid, ContactValidator.validatePhoneNumber("+1 (555) 234-5678"))
        assertEquals(ContactValidationResult.Valid, ContactValidator.validatePhoneNumber("1123456789"))
    }

    @Test
    fun `invalid phone numbers return Invalid`() {
        val resultEmpty = ContactValidator.validatePhoneNumber("")
        assertTrue(resultEmpty is ContactValidationResult.Invalid)

        val resultLetters = ContactValidator.validatePhoneNumber("abc12345")
        assertTrue(resultLetters is ContactValidationResult.Invalid)

        val resultTooShort = ContactValidator.validatePhoneNumber("12345")
        assertTrue(resultTooShort is ContactValidationResult.Invalid)
    }

    @Test
    fun `full contact validation success and failure`() {
        assertEquals(
            ContactValidationResult.Valid,
            ContactValidator.validate("Rajesh Kumar", "+919876543210")
        )

        val failName = ContactValidator.validate("", "+919876543210")
        assertTrue(failName is ContactValidationResult.Invalid)

        val failPhone = ContactValidator.validate("Rajesh Kumar", "invalid")
        assertTrue(failPhone is ContactValidationResult.Invalid)
    }
}
