package com.tapshield.app

import android.app.Application
import com.tapshield.app.di.AppContainer
import com.tapshield.app.di.DefaultAppContainer

class TapShieldApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = DefaultAppContainer(this)
    }

    companion object {
        lateinit var instance: TapShieldApp
            private set
    }
}
