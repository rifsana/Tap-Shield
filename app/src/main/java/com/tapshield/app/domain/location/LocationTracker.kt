package com.tapshield.app.domain.location

import com.tapshield.app.domain.model.LocationPoint
import kotlinx.coroutines.flow.Flow

interface LocationTracker {
    /**
     * Emits continuous location points or errors during an active tracking session.
     */
    fun startTracking(): Flow<LocationResult<LocationPoint>>

    /**
     * Stops continuous location updates and releases hardware resources.
     */
    fun stopTracking()

    /**
     * Acquires a one-shot current location fix.
     */
    suspend fun getCurrentLocation(): LocationResult<LocationPoint>
}
