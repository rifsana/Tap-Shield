package com.tapshield.app.domain.location

import com.tapshield.app.domain.model.LocationPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeLocationTracker : LocationTracker {
    private val locationFlow = MutableSharedFlow<LocationResult<LocationPoint>>(replay = 1)
    var isTrackingStarted = false
        private set
    var isTrackingStopped = false
        private set

    var currentLocationResult: LocationResult<LocationPoint> = LocationResult.Error(
        LocationErrorType.UNAVAILABLE, "No location available"
    )

    fun emitLocation(result: LocationResult<LocationPoint>) {
        locationFlow.tryEmit(result)
    }

    override fun startTracking(): Flow<LocationResult<LocationPoint>> {
        isTrackingStarted = true
        isTrackingStopped = false
        return locationFlow
    }

    override fun stopTracking() {
        isTrackingStopped = true
        isTrackingStarted = false
    }

    override suspend fun getCurrentLocation(): LocationResult<LocationPoint> {
        return currentLocationResult
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LocationTrackerTest {

    private lateinit var fakeTracker: FakeLocationTracker

    @Before
    fun setup() {
        fakeTracker = FakeLocationTracker()
    }

    @Test
    fun `tracking emits success location point accurately`() = runTest {
        val testPoint = LocationPoint(
            latitude = 12.9716,
            longitude = 77.5946,
            accuracyMeters = 4.2f,
            timestamp = 1600000000000L,
            provider = "gps"
        )
        fakeTracker.emitLocation(LocationResult.Success(testPoint))

        val flow = fakeTracker.startTracking()
        val result = flow.first()

        assertTrue(result is LocationResult.Success)
        val data = (result as LocationResult.Success).data
        assertEquals(12.9716, data.latitude, 0.0001)
        assertEquals(77.5946, data.longitude, 0.0001)
        assertEquals(4.2f, data.accuracyMeters, 0.01f)
        assertTrue(fakeTracker.isTrackingStarted)
    }

    @Test
    fun `tracking emits permission denied error when permissions missing`() = runTest {
        fakeTracker.emitLocation(
            LocationResult.Error(
                LocationErrorType.PERMISSION_DENIED,
                "Location permissions not granted"
            )
        )

        val result = fakeTracker.startTracking().first()
        assertTrue(result is LocationResult.Error)
        val error = result as LocationResult.Error
        assertEquals(LocationErrorType.PERMISSION_DENIED, error.type)
        assertEquals("Location permissions not granted", error.message)
    }

    @Test
    fun `tracking emits gps disabled error when location hardware turned off`() = runTest {
        fakeTracker.emitLocation(
            LocationResult.Error(
                LocationErrorType.GPS_DISABLED,
                "Location services (GPS) are disabled on this device"
            )
        )

        val result = fakeTracker.startTracking().first()
        assertTrue(result is LocationResult.Error)
        val error = result as LocationResult.Error
        assertEquals(LocationErrorType.GPS_DISABLED, error.type)
    }

    @Test
    fun `tracking emits timeout error on satellite lock timeout`() = runTest {
        fakeTracker.emitLocation(
            LocationResult.Error(
                LocationErrorType.TIMEOUT,
                "Timed out waiting for GPS fix"
            )
        )

        val result = fakeTracker.startTracking().first()
        assertTrue(result is LocationResult.Error)
        val error = result as LocationResult.Error
        assertEquals(LocationErrorType.TIMEOUT, error.type)
    }

    @Test
    fun `tracking emits stale fix error when location is outdated`() = runTest {
        fakeTracker.emitLocation(
            LocationResult.Error(
                LocationErrorType.STALE_FIX,
                "Refreshing GPS fix..."
            )
        )

        val result = fakeTracker.startTracking().first()
        assertTrue(result is LocationResult.Error)
        val error = result as LocationResult.Error
        assertEquals(LocationErrorType.STALE_FIX, error.type)
    }

    @Test
    fun `stopTracking cleans up and flags tracker as stopped`() {
        fakeTracker.startTracking()
        assertTrue(fakeTracker.isTrackingStarted)

        fakeTracker.stopTracking()
        assertTrue(fakeTracker.isTrackingStopped)
    }

    @Test
    fun `getCurrentLocation returns configured result`() = runTest {
        val testPoint = LocationPoint(28.7041, 77.1025, accuracyMeters = 5.0f)
        fakeTracker.currentLocationResult = LocationResult.Success(testPoint)

        val res = fakeTracker.getCurrentLocation()
        assertTrue(res is LocationResult.Success)
        assertEquals(28.7041, (res as LocationResult.Success).data.latitude, 0.0001)
    }
}
