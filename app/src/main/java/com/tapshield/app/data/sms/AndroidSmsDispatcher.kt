package com.tapshield.app.data.sms

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.tapshield.app.domain.model.Contact
import com.tapshield.app.domain.model.DeliveryStatus
import com.tapshield.app.domain.model.LocationPoint
import com.tapshield.app.domain.model.SosSession
import com.tapshield.app.domain.sms.RecipientSmsResult
import com.tapshield.app.domain.sms.SmsDispatchResult
import com.tapshield.app.domain.sms.SmsDispatcher
import java.util.Locale

class AndroidSmsDispatcher(
    private val context: Context
) : SmsDispatcher {

    companion object {
        const val ACTION_SMS_SENT = "com.tapshield.app.action.SMS_SENT"
        const val ACTION_SMS_DELIVERED = "com.tapshield.app.action.SMS_DELIVERED"
        const val EXTRA_SESSION_ID = "extra_session_id"
        const val EXTRA_CONTACT_ID = "extra_contact_id"
        const val EXTRA_PHONE_NUMBER = "extra_phone_number"
    }

    override fun canSendDirectSms(): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        val hasTelephony = context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)

        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val hasSim = telephonyManager?.simState == TelephonyManager.SIM_STATE_READY

        return hasPermission && hasTelephony && hasSim
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override suspend fun dispatchEmergencySms(
        session: SosSession,
        contacts: List<Contact>,
        location: LocationPoint?
    ): SmsDispatchResult {
        // 1. Permission verification
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            return SmsDispatchResult(
                overallStatus = DeliveryStatus.SKIPPED_NO_PERMISSION,
                summaryMessage = "SMS permission not granted. Request SEND_SMS in Safety Setup or use SMS fallback.",
                recipientResults = contacts.map {
                    RecipientSmsResult(
                        contactId = it.id,
                        contactName = it.name,
                        phoneNumber = it.phoneNumber,
                        status = DeliveryStatus.SKIPPED_NO_PERMISSION,
                        detail = "Permission SEND_SMS denied"
                    )
                }
            )
        }

        // 2. Hardware and Telephony verification
        val hasTelephony = context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
        if (!hasTelephony) {
            return SmsDispatchResult(
                overallStatus = DeliveryStatus.FAILED,
                summaryMessage = "Device lacks cellular telephony hardware for SMS dispatch.",
                recipientResults = emptyList()
            )
        }

        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        if (telephonyManager?.simState != TelephonyManager.SIM_STATE_READY) {
            return SmsDispatchResult(
                overallStatus = DeliveryStatus.FAILED,
                summaryMessage = "No active SIM card detected or cellular radio is disconnected.",
                recipientResults = emptyList()
            )
        }

        // 3. Eligible recipients
        val eligibleContacts = contacts.filter { it.notifyBySms }
        if (eligibleContacts.isEmpty()) {
            return SmsDispatchResult(
                overallStatus = DeliveryStatus.PENDING,
                summaryMessage = "No trusted contacts configured with SMS alerts enabled.",
                recipientResults = emptyList()
            )
        }

        // 4. Compose emergency message
        val shareToken = session.publicShareToken ?: "session"
        val messageText = buildString {
            append("EMERGENCY SOS: I need help!\n")
            append("Live Tracker: https://tapshield.live/s/$shareToken\n")
            if (location != null) {
                val latStr = String.format(Locale.US, "%.5f", location.latitude)
                val lngStr = String.format(Locale.US, "%.5f", location.longitude)
                append("GPS: $latStr, $lngStr (±${location.accuracyMeters.toInt()}m)\n")
            } else {
                append("GPS: Acquiring satellite lock...\n")
            }
            append("- Sent via TapShield")
        }

        // 5. Obtain SmsManager safely across API levels
        val smsManager = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
        } catch (e: Exception) {
            return SmsDispatchResult(
                overallStatus = DeliveryStatus.FAILED,
                summaryMessage = "Failed to initialize Android SmsManager: ${e.message}",
                recipientResults = emptyList()
            )
        }

        if (smsManager == null) {
            return SmsDispatchResult(
                overallStatus = DeliveryStatus.FAILED,
                summaryMessage = "SmsManager service unavailable on this device",
                recipientResults = emptyList()
            )
        }

        val results = mutableListOf<RecipientSmsResult>()
        var successCount = 0

        for (contact in eligibleContacts) {
            val destination = contact.phoneNumber.trim()
            if (destination.isBlank()) {
                results.add(
                    RecipientSmsResult(
                        contactId = contact.id,
                        contactName = contact.name,
                        phoneNumber = destination,
                        status = DeliveryStatus.FAILED,
                        detail = "Invalid empty phone number"
                    )
                )
                continue
            }

            try {
                val sentIntent = Intent(ACTION_SMS_SENT).apply {
                    setPackage(context.packageName)
                    putExtra(EXTRA_SESSION_ID, session.sessionId)
                    putExtra(EXTRA_CONTACT_ID, contact.id)
                    putExtra(EXTRA_PHONE_NUMBER, destination)
                }
                val sentPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (contact.id * 1000 + (System.currentTimeMillis() % 1000)).toInt(),
                    sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val deliveryIntent = Intent(ACTION_SMS_DELIVERED).apply {
                    setPackage(context.packageName)
                    putExtra(EXTRA_SESSION_ID, session.sessionId)
                    putExtra(EXTRA_CONTACT_ID, contact.id)
                    putExtra(EXTRA_PHONE_NUMBER, destination)
                }
                val deliveryPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (contact.id * 1000 + (System.currentTimeMillis() % 1000) + 1).toInt(),
                    deliveryIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val parts = smsManager.divideMessage(messageText)
                if (parts.size > 1) {
                    val sentIntents = ArrayList<PendingIntent>().apply {
                        add(sentPendingIntent)
                        for (i in 1 until parts.size) add(sentPendingIntent)
                    }
                    val deliveryIntents = ArrayList<PendingIntent>().apply {
                        add(deliveryPendingIntent)
                        for (i in 1 until parts.size) add(deliveryPendingIntent)
                    }
                    smsManager.sendMultipartTextMessage(
                        destination,
                        null,
                        parts,
                        sentIntents,
                        deliveryIntents
                    )
                } else {
                    smsManager.sendTextMessage(
                        destination,
                        null,
                        messageText,
                        sentPendingIntent,
                        deliveryPendingIntent
                    )
                }

                results.add(
                    RecipientSmsResult(
                        contactId = contact.id,
                        contactName = contact.name,
                        phoneNumber = destination,
                        status = DeliveryStatus.SENT,
                        detail = "Submitted to carrier"
                    )
                )
                successCount++
            } catch (e: Exception) {
                results.add(
                    RecipientSmsResult(
                        contactId = contact.id,
                        contactName = contact.name,
                        phoneNumber = destination,
                        status = DeliveryStatus.FAILED,
                        detail = "Exception: ${e.message}"
                    )
                )
            }
        }

        val overallStatus = when {
            successCount == eligibleContacts.size -> DeliveryStatus.SENT
            successCount > 0 -> DeliveryStatus.SENT
            else -> DeliveryStatus.FAILED
        }

        val summary = if (successCount > 0) {
            "Submitted to carrier for $successCount of ${eligibleContacts.size} guardian(s)"
        } else {
            "Failed to submit SMS alerts to carrier"
        }

        return SmsDispatchResult(
            overallStatus = overallStatus,
            summaryMessage = summary,
            recipientResults = results
        )
    }

    /**
     * Fallback Intent to open default Android SMS app with pre-filled emergency coordinates.
     * Complies fully with Google Play store policy if direct SEND_SMS is not permitted.
     */
    fun createSystemSmsIntent(phoneNumber: String, messageText: String): Intent {
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${Uri.encode(phoneNumber)}")
            putExtra("sms_body", messageText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}
