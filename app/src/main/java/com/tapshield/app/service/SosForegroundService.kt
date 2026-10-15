package com.tapshield.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.tapshield.app.MainActivity
import com.tapshield.app.R
import com.tapshield.app.TapShieldApp
import com.tapshield.app.domain.coordinator.SosCoordinator
import com.tapshield.app.domain.location.LocationResult
import com.tapshield.app.domain.location.LocationTracker
import com.tapshield.app.domain.model.LocationPoint
import com.tapshield.app.domain.state.SosState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SosForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var trackingJob: Job? = null
    private var stateObserverJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private lateinit var sosCoordinator: SosCoordinator
    private lateinit var locationTracker: LocationTracker
    private lateinit var notificationManager: NotificationManager

    private var isTrackingActive = false
    private var currentSessionId: String? = null

    override fun onCreate() {
        super.onCreate()
        val container = TapShieldApp.instance.container
        sosCoordinator = container.sosCoordinator
        locationTracker = container.locationTracker
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel()
        acquireWakeLock()
        observeSosState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == ACTION_STOP_SOS) {
            sosCoordinator.resolveSafe("Resolved via notification action")
            stopTrackingAndFinish()
            return START_NOT_STICKY
        }

        val sessionId = intent?.getStringExtra(EXTRA_SESSION_ID) ?: "active_session"
        currentSessionId = sessionId

        // Start foreground immediately to comply with Android 5-second foreground start SLA
        val initialNotification = buildNotification("Initializing high-accuracy emergency tracking...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                initialNotification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                } else {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                }
            )
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        if (!isTrackingActive) {
            startLocationPipeline()
        }

        return START_STICKY
    }

    private fun startLocationPipeline() {
        isTrackingActive = true
        trackingJob?.cancel()
        trackingJob = serviceScope.launch(Dispatchers.Default) {
            locationTracker.startTracking().collectLatest { result ->
                when (result) {
                    is LocationResult.Success -> {
                        val loc = result.data
                        sosCoordinator.updateLocation(loc)
                        updateNotificationLocation(loc)
                    }
                    is LocationResult.Error -> {
                        val errorStatus = when (result.type) {
                            com.tapshield.app.domain.location.LocationErrorType.GPS_DISABLED -> "GPS is turned off"
                            com.tapshield.app.domain.location.LocationErrorType.PERMISSION_DENIED -> "Location permission missing"
                            com.tapshield.app.domain.location.LocationErrorType.TIMEOUT -> "Acquiring satellite fix..."
                            com.tapshield.app.domain.location.LocationErrorType.UNAVAILABLE -> "Weak GPS signal"
                            com.tapshield.app.domain.location.LocationErrorType.STALE_FIX -> "Refreshing GPS fix..."
                        }
                        updateNotificationText("Status: $errorStatus")
                    }
                }
            }
        }
    }

    private fun observeSosState() {
        stateObserverJob?.cancel()
        stateObserverJob = serviceScope.launch {
            sosCoordinator.state.collectLatest { state ->
                when (state) {
                    is SosState.Active -> {
                        // Keep tracking
                    }
                    is SosState.SafeResolved, is SosState.Cancelled, is SosState.Idle -> {
                        // Session ended
                        stopTrackingAndFinish()
                    }
                    is SosState.Countdown -> {
                        // Wait for countdown
                    }
                }
            }
        }
    }

    private fun updateNotificationLocation(location: LocationPoint) {
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(location.timestamp))
        val text = "GPS Active: ±${location.accuracyMeters.toInt()}m accuracy • $timeStr"
        notificationManager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun updateNotificationText(statusText: String) {
        notificationManager.notify(NOTIFICATION_ID, buildNotification(statusText))
    }

    private fun buildNotification(contentText: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAV_ROUTE, "active_sos")
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, SosForegroundService::class.java).apply {
            action = ACTION_STOP_SOS
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel,
            "I'M SAFE (STOP)",
            stopPendingIntent
        ).build()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TapShield SOS Broadcast Active")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(openPendingIntent)
            .addAction(stopAction)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TapShield Emergency Broadcast",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Active emergency session notifications and live location tracking status"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "TapShield::EmergencyLocationWakeLock"
            )?.apply {
                // Set reasonable timeout safety limit of 4 hours
                acquire(4 * 60 * 60 * 1000L)
            }
        } catch (_: Exception) {
            // Best effort
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {
            // Best effort
        }
        wakeLock = null
    }

    private fun stopTrackingAndFinish() {
        isTrackingActive = false
        trackingJob?.cancel()
        trackingJob = null
        locationTracker.stopTracking()
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTrackingAndFinish()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "tapshield_emergency_tracking_channel"
        const val NOTIFICATION_ID = 9110

        const val ACTION_START_SOS = "com.tapshield.app.action.START_SOS"
        const val ACTION_STOP_SOS = "com.tapshield.app.action.STOP_SOS"

        const val EXTRA_SESSION_ID = "extra_session_id"
        const val EXTRA_NAV_ROUTE = "extra_nav_route"

        fun startService(context: Context, sessionId: String) {
            val intent = Intent(context, SosForegroundService::class.java).apply {
                action = ACTION_START_SOS
                putExtra(EXTRA_SESSION_ID, sessionId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, SosForegroundService::class.java).apply {
                action = ACTION_STOP_SOS
            }
            context.startService(intent)
        }
    }
}
