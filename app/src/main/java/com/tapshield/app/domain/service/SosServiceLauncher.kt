package com.tapshield.app.domain.service

interface SosServiceLauncher {
    fun startSosService(sessionId: String)
    fun stopSosService()
}
