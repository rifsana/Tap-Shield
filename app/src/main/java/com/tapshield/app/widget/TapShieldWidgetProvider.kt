package com.tapshield.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.tapshield.app.MainActivity
import com.tapshield.app.R
import com.tapshield.app.TapShieldApp
import com.tapshield.app.domain.model.TriggerSource
import com.tapshield.app.domain.state.SosState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TapShieldWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_WIDGET_SOS -> {
                handleWidgetSosTrigger(context)
            }
            ACTION_UPDATE_WIDGET_STATE -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val ids = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, TapShieldWidgetProvider::class.java)
                )
                onUpdate(context, appWidgetManager, ids)
            }
        }
    }

    private fun handleWidgetSosTrigger(context: Context) {
        try {
            val app = context.applicationContext as? TapShieldApp
            val coordinator = app?.container?.sosCoordinator
            if (coordinator != null) {
                val triggered = coordinator.triggerSos(
                    source = TriggerSource.WIDGET,
                    instant = false,
                    countdownSeconds = 3
                )
                // If already triggered or newly triggered, launch app into active screen
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                context.startActivity(launchIntent)
            } else {
                // If app process was dead, start activity directly to initialize
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                context.startActivity(launchIntent)
            }
        } catch (_: Exception) {
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(launchIntent)
        }

        updateAllWidgets(context)
    }

    companion object {
        const val ACTION_WIDGET_SOS = "com.tapshield.app.action.WIDGET_SOS"
        const val ACTION_UPDATE_WIDGET_STATE = "com.tapshield.app.action.UPDATE_WIDGET_STATE"

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, TapShieldWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_WIDGET_STATE
            }
            context.sendBroadcast(intent)
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_tapshield)

            // Intent to open Main App when tapping widget background
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

            // Intent for SOS trigger button
            val sosIntent = Intent(context, TapShieldWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_SOS
            }
            val sosPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                sosIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_sos, sosPendingIntent)

            // Determine current session status
            val app = context.applicationContext as? TapShieldApp
            val coordinator = app?.container?.sosCoordinator
            val sosState = coordinator?.state?.value ?: SosState.Idle

            when (sosState) {
                is SosState.Idle -> {
                    views.setTextViewText(R.id.widget_status_badge, "READY • PROTECTED")
                    views.setTextColor(R.id.widget_status_badge, Color.parseColor("#18865B"))
                    views.setTextViewText(R.id.widget_subtext, "Tap SOS for 3s safety dispatch")
                    views.setInt(R.id.widget_btn_sos, "setBackgroundResource", R.drawable.widget_sos_btn)
                    views.setTextViewText(R.id.widget_sos_text, "SOS")
                }
                is SosState.Countdown -> {
                    views.setTextViewText(R.id.widget_status_badge, "DISPATCHING IN ${sosState.secondsRemaining}s")
                    views.setTextColor(R.id.widget_status_badge, Color.parseColor("#E5484D"))
                    views.setTextViewText(R.id.widget_subtext, "Tap to open and cancel")
                    views.setInt(R.id.widget_btn_sos, "setBackgroundResource", R.drawable.widget_sos_btn_active)
                    views.setTextViewText(R.id.widget_sos_text, "${sosState.secondsRemaining}")
                }
                is SosState.Active -> {
                    views.setTextViewText(R.id.widget_status_badge, "SOS ACTIVE • LIVE GPS")
                    views.setTextColor(R.id.widget_status_badge, Color.parseColor("#E5484D"))
                    views.setTextViewText(R.id.widget_subtext, "Emergency broadcast running")
                    views.setInt(R.id.widget_btn_sos, "setBackgroundResource", R.drawable.widget_sos_btn_active)
                    views.setTextViewText(R.id.widget_sos_text, "ACTIVE")
                }
                is SosState.SafeResolved -> {
                    views.setTextViewText(R.id.widget_status_badge, "RESOLVED • SAFE")
                    views.setTextColor(R.id.widget_status_badge, Color.parseColor("#18865B"))
                    views.setTextViewText(R.id.widget_subtext, "Emergency session resolved")
                    views.setInt(R.id.widget_btn_sos, "setBackgroundResource", R.drawable.widget_sos_btn)
                    views.setTextViewText(R.id.widget_sos_text, "SOS")
                }
                is SosState.Cancelled -> {
                    views.setTextViewText(R.id.widget_status_badge, "READY • STANDBY")
                    views.setTextColor(R.id.widget_status_badge, Color.parseColor("#18865B"))
                    views.setTextViewText(R.id.widget_subtext, "Cancelled • Ready to protect")
                    views.setInt(R.id.widget_btn_sos, "setBackgroundResource", R.drawable.widget_sos_btn)
                    views.setTextViewText(R.id.widget_sos_text, "SOS")
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
