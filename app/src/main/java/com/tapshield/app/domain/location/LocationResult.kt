package com.tapshield.app.domain.location

sealed class LocationResult<out T> {
    data class Success<T>(val data: T) : LocationResult<T>()
    data class Error(val type: LocationErrorType, val message: String) : LocationResult<Nothing>()
}

enum class LocationErrorType {
    PERMISSION_DENIED,
    GPS_DISABLED,
    TIMEOUT,
    UNAVAILABLE,
    STALE_FIX
}
