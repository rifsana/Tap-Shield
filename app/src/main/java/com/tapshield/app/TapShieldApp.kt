package com.tapshield.app

import android.app.Application
import com.tapshield.app.di.AppContainer
import com.tapshield.app.di.DefaultAppContainer
import com.tapshield.app.widget.TapShieldWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TapShieldApp : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = DefaultAppContainer(this)

        // Automatically push state changes to home-screen widgets
        applicationScope.launch {
            container.sosCoordinator.state.collectLatest {
                TapShieldWidgetProvider.updateAllWidgets(this@TapShieldApp)
            }
        }
    }

    companion object {
        lateinit var instance: TapShieldApp
            private set
    }
}
