package com.tapshield.app.service

import android.content.Context
import com.tapshield.app.domain.service.SosServiceLauncher

class AndroidSosServiceLauncher(
    private val context: Context
) : SosServiceLauncher {

    override fun startSosService(sessionId: String) {
        SosForegroundService.startService(context, sessionId)
    }

    override fun stopSosService() {
        SosForegroundService.stopService(context)
    }
}
