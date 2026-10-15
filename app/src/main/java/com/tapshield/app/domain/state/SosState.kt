package com.tapshield.app.domain.state

import com.tapshield.app.domain.model.SosSession
import com.tapshield.app.domain.model.TriggerSource

sealed class SosState {
    data object Idle : SosState()

    data class Countdown(
        val secondsRemaining: Int,
        val source: TriggerSource,
        val sessionId: String
    ) : SosState()

    data class Active(
        val session: SosSession
    ) : SosState()

    data class SafeResolved(
        val session: SosSession,
        val message: String
    ) : SosState()

    data class Cancelled(
        val reason: String
    ) : SosState()
}
