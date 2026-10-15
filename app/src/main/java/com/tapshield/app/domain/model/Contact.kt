package com.tapshield.app.domain.model

data class Contact(
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val relationship: String = "",
    val isPrimary: Boolean = false,
    val notifyBySms: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
