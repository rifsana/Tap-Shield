package com.tapshield.app.domain.coordinator

import com.tapshield.app.data.repository.ContactRepository
import com.tapshield.app.domain.model.Contact
import com.tapshield.app.domain.model.DeliveryChannel
import com.tapshield.app.domain.model.DeliveryStatus
import com.tapshield.app.domain.model.LocationPoint
import com.tapshield.app.domain.model.SosSession
import com.tapshield.app.domain.model.TriggerSource
import com.tapshield.app.domain.sms.RecipientSmsResult
import com.tapshield.app.domain.sms.SmsDispatchResult
import com.tapshield.app.domain.sms.SmsDispatcher
import com.tapshield.app.domain.state.SosState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeContactRepository : ContactRepository {
    val contactList = mutableListOf<Contact>()

    override fun getAllContacts(): Flow<List<Contact>> = flowOf(contactList.toList())
    override suspend fun getAllContactsSync(): List<Contact> = contactList.toList()
    override suspend fun getContactById(id: Long): Contact? = contactList.find { it.id == id }
    override fun getContactCountFlow(): Flow<Int> = MutableStateFlow(contactList.size)
    override suspend fun addContact(contact: Contact): Result<Long> {
        contactList.add(contact)
        return Result.success(contact.id)
    }
    override suspend fun updateContact(contact: Contact): Result<Unit> {
        val index = contactList.indexOfFirst { it.id == contact.id }
        if (index >= 0) contactList[index] = contact
        return Result.success(Unit)
    }
    override suspend fun deleteContact(contact: Contact) {
        contactList.removeAll { it.id == contact.id }
    }
    override suspend fun deleteContactById(id: Long) {
        contactList.removeAll { it.id == id }
    }
    override suspend fun setPrimaryContact(id: Long) {
        for (i in contactList.indices) {
            contactList[i] = contactList[i].copy(isPrimary = contactList[i].id == id)
        }
    }
}

class FakeSmsDispatcher : SmsDispatcher {
    var canSendSmsResult: Boolean = true
    var dispatchResultToReturn: SmsDispatchResult? = null
    var shouldThrowException: Boolean = false

    val dispatchCalls = mutableListOf<SosSession>()

    override fun canSendDirectSms(): Boolean = canSendSmsResult

    override suspend fun dispatchEmergencySms(
        session: SosSession,
        contacts: List<Contact>,
        location: LocationPoint?
    ): SmsDispatchResult {
        if (shouldThrowException) {
            throw RuntimeException("Cellular radio unavailable")
        }
        dispatchCalls.add(session)
        return dispatchResultToReturn ?: SmsDispatchResult(
            overallStatus = DeliveryStatus.SENT,
            summaryMessage = "Submitted to carrier for ${contacts.size} guardian(s)",
            recipientResults = contacts.map {
                RecipientSmsResult(
                    contactId = it.id,
                    contactName = it.name,
                    phoneNumber = it.phoneNumber,
                    status = DeliveryStatus.SENT
                )
            }
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SosCoordinatorSmsTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var sosRepository: FakeSosRepository
    private lateinit var contactRepository: FakeContactRepository
    private lateinit var smsDispatcher: FakeSmsDispatcher
    private lateinit var coordinator: SosCoordinator

    @Before
    fun setup() {
        sosRepository = FakeSosRepository()
        contactRepository = FakeContactRepository()
        smsDispatcher = FakeSmsDispatcher()

        coordinator = SosCoordinator(
            sosRepository = sosRepository,
            contactRepository = contactRepository,
            smsDispatcher = smsDispatcher,
            scope = testScope,
            dispatcher = testDispatcher
        )
    }

    @Test
    fun `activating SOS dispatches SMS to eligible contacts and updates delivery state to SENT`() = testScope.runTest {
        // Arrange
        contactRepository.contactList.add(
            Contact(id = 1L, name = "Guardian A", phoneNumber = "+919876543210", notifyBySms = true, isPrimary = true)
        )
        contactRepository.contactList.add(
            Contact(id = 2L, name = "Guardian B", phoneNumber = "+919876543211", notifyBySms = false, isPrimary = false)
        )

        // Act
        val triggered = coordinator.triggerSos(source = TriggerSource.IN_APP_BUTTON, instant = true)
        advanceUntilIdle()

        // Assert
        assertTrue(triggered)
        val state = coordinator.state.value as SosState.Active
        val smsState = state.session.deliveryStates[DeliveryChannel.SMS]
        assertNotNull(smsState)
        assertEquals(DeliveryStatus.SENT, smsState?.status)
        assertTrue(smsState?.message?.contains("Submitted to carrier") == true)
        assertEquals(1, smsDispatcher.dispatchCalls.size)
    }

    @Test
    fun `activating SOS when SMS permission is missing updates state to SKIPPED_NO_PERMISSION`() = testScope.runTest {
        // Arrange
        contactRepository.contactList.add(
            Contact(id = 1L, name = "Guardian A", phoneNumber = "+919876543210", notifyBySms = true)
        )
        smsDispatcher.dispatchResultToReturn = SmsDispatchResult(
            overallStatus = DeliveryStatus.SKIPPED_NO_PERMISSION,
            summaryMessage = "SMS permission not granted. Configure in Safety Setup."
        )

        // Act
        coordinator.triggerSos(source = TriggerSource.IN_APP_BUTTON, instant = true)
        advanceUntilIdle()

        // Assert
        val state = coordinator.state.value as SosState.Active
        val smsState = state.session.deliveryStates[DeliveryChannel.SMS]
        assertEquals(DeliveryStatus.SKIPPED_NO_PERMISSION, smsState?.status)
        assertTrue(smsState?.message?.contains("permission not granted") == true)
    }

    @Test
    fun `activating SOS when no contacts have SMS enabled updates state without calling dispatcher`() = testScope.runTest {
        // Arrange
        contactRepository.contactList.add(
            Contact(id = 1L, name = "Guardian No SMS", phoneNumber = "+919876543210", notifyBySms = false)
        )

        // Act
        coordinator.triggerSos(source = TriggerSource.IN_APP_BUTTON, instant = true)
        advanceUntilIdle()

        // Assert
        val state = coordinator.state.value as SosState.Active
        val smsState = state.session.deliveryStates[DeliveryChannel.SMS]
        assertEquals(DeliveryStatus.PENDING, smsState?.status)
        assertEquals("No trusted contacts configured with SMS alerts", smsState?.message)
        assertEquals(0, smsDispatcher.dispatchCalls.size)
    }

    @Test
    fun `SMS dispatch exception reports FAILED status without crashing active session`() = testScope.runTest {
        // Arrange
        contactRepository.contactList.add(
            Contact(id = 1L, name = "Guardian A", phoneNumber = "+919876543210", notifyBySms = true)
        )
        smsDispatcher.shouldThrowException = true

        // Act
        coordinator.triggerSos(source = TriggerSource.IN_APP_BUTTON, instant = true)
        advanceUntilIdle()

        // Assert
        val state = coordinator.state.value as SosState.Active
        val smsState = state.session.deliveryStates[DeliveryChannel.SMS]
        assertEquals(DeliveryStatus.FAILED, smsState?.status)
        assertTrue(smsState?.message?.contains("Cellular radio unavailable") == true)
    }
}
