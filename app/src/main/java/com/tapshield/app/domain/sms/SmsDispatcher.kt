package com.tapshield.app.domain.sms

import com.tapshield.app.domain.model.Contact
import com.tapshield.app.domain.model.DeliveryStatus
import com.tapshield.app.domain.model.LocationPoint
import com.tapshield.app.domain.model.SosSession

data class RecipientSmsResult(
    val contactId: Long,
    val contactName: String,
    val phoneNumber: String,
    val status: DeliveryStatus,
    val detail: String? = null
)

data class SmsDispatchResult(
    val overallStatus: DeliveryStatus,
    val summaryMessage: String,
    val recipientResults: List<RecipientSmsResult> = emptyList()
)

interface SmsDispatcher {
    /**
     * Checks if the device has SMS capability and the app possesses runtime SEND_SMS permission.
     */
    fun canSendDirectSms(): Boolean

    /**
     * Dispatches emergency SMS alerts to eligible trusted contacts.
     */
    suspend fun dispatchEmergencySms(
        session: SosSession,
        contacts: List<Contact>,
        location: LocationPoint?
    ): SmsDispatchResult
}
