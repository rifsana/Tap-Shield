package com.tapshield.app.di

import android.content.Context
import com.tapshield.app.data.local.TapShieldDatabase
import com.tapshield.app.data.location.FusedLocationTracker
import com.tapshield.app.data.repository.ContactRepository
import com.tapshield.app.data.repository.ContactRepositoryImpl
import com.tapshield.app.data.repository.SosRepository
import com.tapshield.app.data.repository.SosRepositoryImpl
import com.tapshield.app.domain.coordinator.SosCoordinator
import com.tapshield.app.domain.location.LocationTracker
import com.tapshield.app.domain.service.SosServiceLauncher
import com.tapshield.app.service.AndroidSosServiceLauncher

import com.tapshield.app.data.sms.AndroidSmsDispatcher
import com.tapshield.app.domain.sms.SmsDispatcher

interface AppContainer {
    val database: TapShieldDatabase
    val contactRepository: ContactRepository
    val sosRepository: SosRepository
    val locationTracker: LocationTracker
    val sosServiceLauncher: SosServiceLauncher
    val smsDispatcher: SmsDispatcher
    val sosCoordinator: SosCoordinator
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: TapShieldDatabase by lazy {
        TapShieldDatabase.getInstance(context)
    }

    override val contactRepository: ContactRepository by lazy {
        ContactRepositoryImpl(database.contactDao())
    }

    override val sosRepository: SosRepository by lazy {
        SosRepositoryImpl(database.sosSessionDao(), database.sosEventDao())
    }

    override val locationTracker: LocationTracker by lazy {
        FusedLocationTracker(context)
    }

    override val sosServiceLauncher: SosServiceLauncher by lazy {
        AndroidSosServiceLauncher(context)
    }

    override val smsDispatcher: SmsDispatcher by lazy {
        AndroidSmsDispatcher(context)
    }

    override val sosCoordinator: SosCoordinator by lazy {
        SosCoordinator(
            sosRepository = sosRepository,
            contactRepository = contactRepository,
            smsDispatcher = smsDispatcher,
            sosServiceLauncher = sosServiceLauncher
        )
    }
}
