package com.tapshield.app.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult as GmsLocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.tapshield.app.domain.location.LocationErrorType
import com.tapshield.app.domain.location.LocationResult
import com.tapshield.app.domain.location.LocationTracker
import com.tapshield.app.domain.model.LocationPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FusedLocationTracker(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
) : LocationTracker {

    private var activeCallback: LocationCallback? = null

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun isGpsEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    override fun startTracking(): Flow<LocationResult<LocationPoint>> = callbackFlow {
        if (!hasLocationPermission()) {
            trySend(LocationResult.Error(LocationErrorType.PERMISSION_DENIED, "Location permissions not granted"))
            close()
            return@callbackFlow
        }

        if (!isGpsEnabled()) {
            trySend(LocationResult.Error(LocationErrorType.GPS_DISABLED, "Location services (GPS) are disabled on this device"))
        }

        // Emit fresh last-known location if available while waiting for satellite fix
        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
            if (lastLoc != null) {
                val ageMillis = System.currentTimeMillis() - lastLoc.time
                // Only use last-known if reasonably fresh (under 60 seconds)
                if (ageMillis < 60_000) {
                    trySend(LocationResult.Success(lastLoc.toLocationPoint()))
                }
            }
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(3000L)
            .setMinUpdateDistanceMeters(2.0f)
            .setMaxUpdateDelayMillis(8000L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: GmsLocationResult) {
                val last = result.lastLocation
                if (last != null) {
                    trySend(LocationResult.Success(last.toLocationPoint()))
                }
            }

            override fun onLocationAvailability(availability: com.google.android.gms.location.LocationAvailability) {
                if (!availability.isLocationAvailable) {
                    if (!isGpsEnabled()) {
                        trySend(LocationResult.Error(LocationErrorType.GPS_DISABLED, "Location hardware unavailable or disabled"))
                    } else {
                        trySend(LocationResult.Error(LocationErrorType.UNAVAILABLE, "GPS signal weak or unavailable"))
                    }
                }
            }
        }

        activeCallback = callback
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        ).addOnFailureListener { err ->
            trySend(LocationResult.Error(LocationErrorType.UNAVAILABLE, err.message ?: "Failed to request location updates"))
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
            activeCallback = null
        }
    }

    override fun stopTracking() {
        activeCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            activeCallback = null
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): LocationResult<LocationPoint> {
        if (!hasLocationPermission()) {
            return LocationResult.Error(LocationErrorType.PERMISSION_DENIED, "Location permissions not granted")
        }
        if (!isGpsEnabled()) {
            return LocationResult.Error(LocationErrorType.GPS_DISABLED, "Location services (GPS) are disabled")
        }

        val cancellationSource = CancellationTokenSource()
        return try {
            val loc: Location? = withTimeoutOrNull(10000L) {
                suspendCancellableCoroutine { continuation ->
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationSource.token
                    ).addOnSuccessListener { location: Location? ->
                        continuation.resume(location)
                    }.addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }.addOnCanceledListener {
                        continuation.cancel()
                    }
                }
            }

            if (loc != null) {
                LocationResult.Success(loc.toLocationPoint())
            } else {
                LocationResult.Error(LocationErrorType.TIMEOUT, "Timed out waiting for GPS fix")
            }
        } catch (e: Exception) {
            LocationResult.Error(LocationErrorType.UNAVAILABLE, e.message ?: "Failed to acquire location")
        } finally {
            cancellationSource.cancel()
        }
    }

    private fun Location.toLocationPoint(): LocationPoint {
        return LocationPoint(
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = if (hasAccuracy()) accuracy else 0f,
            altitudeMeters = if (hasAltitude()) altitude else 0.0,
            speedMps = if (hasSpeed()) speed else 0f,
            timestamp = time,
            provider = provider ?: "fused"
        )
    }
}
