package com.tapshield.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tapshield.app.domain.model.Contact

@Entity(tableName = "trusted_contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val relationship: String = "",
    val isPrimary: Boolean = false,
    val notifyBySms: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Contact = Contact(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        relationship = relationship,
        isPrimary = isPrimary,
        notifyBySms = notifyBySms,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(contact: Contact): ContactEntity = ContactEntity(
            id = contact.id,
            name = contact.name,
            phoneNumber = contact.phoneNumber,
            relationship = contact.relationship,
            isPrimary = contact.isPrimary,
            notifyBySms = contact.notifyBySms,
            createdAt = contact.createdAt
        )
    }
}
