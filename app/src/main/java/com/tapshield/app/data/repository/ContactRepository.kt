package com.tapshield.app.data.repository

import com.tapshield.app.data.local.dao.ContactDao
import com.tapshield.app.data.local.entity.ContactEntity
import com.tapshield.app.domain.model.Contact
import com.tapshield.app.domain.validation.ContactValidationResult
import com.tapshield.app.domain.validation.ContactValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ContactRepository {
    fun getAllContacts(): Flow<List<Contact>>
    suspend fun getAllContactsSync(): List<Contact>
    suspend fun getContactById(id: Long): Contact?
    fun getContactCountFlow(): Flow<Int>
    suspend fun addContact(contact: Contact): Result<Long>
    suspend fun updateContact(contact: Contact): Result<Unit>
    suspend fun deleteContact(contact: Contact)
    suspend fun deleteContactById(id: Long)
    suspend fun setPrimaryContact(id: Long)
}

class ContactRepositoryImpl(
    private val contactDao: ContactDao
) : ContactRepository {

    override fun getAllContacts(): Flow<List<Contact>> {
        return contactDao.getAllContacts().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getAllContactsSync(): List<Contact> {
        return contactDao.getAllContactsSync().map { it.toDomain() }
    }

    override suspend fun getContactById(id: Long): Contact? {
        return contactDao.getContactById(id)?.toDomain()
    }

    override fun getContactCountFlow(): Flow<Int> {
        return contactDao.getContactCountFlow()
    }

    override suspend fun addContact(contact: Contact): Result<Long> {
        val validation = ContactValidator.validate(contact.name, contact.phoneNumber)
        if (validation is ContactValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException(validation.error))
        }

        val entity = ContactEntity.fromDomain(contact)
        val id = contactDao.insertContact(entity)
        if (contact.isPrimary) {
            contactDao.setSinglePrimary(id)
        }
        return Result.success(id)
    }

    override suspend fun updateContact(contact: Contact): Result<Unit> {
        val validation = ContactValidator.validate(contact.name, contact.phoneNumber)
        if (validation is ContactValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException(validation.error))
        }

        val entity = ContactEntity.fromDomain(contact)
        contactDao.updateContact(entity)
        if (contact.isPrimary) {
            contactDao.setSinglePrimary(contact.id)
        }
        return Result.success(Unit)
    }

    override suspend fun deleteContact(contact: Contact) {
        contactDao.deleteContact(ContactEntity.fromDomain(contact))
    }

    override suspend fun deleteContactById(id: Long) {
        contactDao.deleteContactById(id)
    }

    override suspend fun setPrimaryContact(id: Long) {
        contactDao.setSinglePrimary(id)
    }
}
