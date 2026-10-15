package com.tapshield.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapshield.app.domain.model.DeliveryChannel
import com.tapshield.app.domain.model.DeliveryStatus
import com.tapshield.app.domain.state.SosState
import com.tapshield.app.ui.theme.AppRadius
import com.tapshield.app.ui.theme.AppSpacing
import com.tapshield.app.ui.theme.AttentionAmber
import com.tapshield.app.ui.theme.EmergencyCoral
import com.tapshield.app.ui.theme.SapphireBlue
import com.tapshield.app.ui.theme.SuccessGreen
import com.tapshield.app.ui.viewmodel.ActiveSosViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActiveSosScreen(
    viewModel: ActiveSosViewModel,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sosState by viewModel.sosState.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showSafeConfirmDialog by remember { mutableStateOf(false) }

    when (val state = sosState) {
        is SosState.Idle, is SosState.Countdown, is SosState.Cancelled -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(AppSpacing.xl),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.base))

                    Text(
                        text = "System Standby",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.xs))

                    Text(
                        text = "No emergency session is currently active. TapShield is armed and ready for one-tap dispatch from the home screen, widget, or Quick Settings tile.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.xl))

                    Button(
                        onClick = onNavigateHome,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SapphireBlue,
                            contentColor = Color.White
                        ),
                        shape = AppRadius.shapeSm
                    ) {
                        Text("Return to Dashboard", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        is SosState.SafeResolved -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(AppSpacing.xl),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.base))

                    Text(
                        text = "Emergency Resolved",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.xs))

                    Text(
                        text = "You are marked safe. Live location tracking has stopped, and your safe resolution status has been persisted.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.xl))

                    Button(
                        onClick = {
                            viewModel.resetToIdle()
                            onNavigateHome()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessGreen,
                            contentColor = Color.White
                        ),
                        shape = AppRadius.shapeSm
                    ) {
                        Text("Return to Standby", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        is SosState.Active -> {
            val session = state.session
            val minutes = elapsedSeconds / 60
            val seconds = elapsedSeconds % 60
            val formattedTime = String.format(Locale.US, "%02d:%02d", minutes, seconds)
            val shareUrl = "https://tapshield.live/s/${session.publicShareToken ?: "session"}"

            val infiniteTransition = rememberInfiniteTransition(label = "activePulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.base),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Emergency Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppRadius.shapeMd,
                    colors = CardDefaults.cardColors(containerColor = EmergencyCoral),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.base),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.sm))

                        Text(
                            text = "SOS BROADCAST ACTIVE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "Elapsed Time: $formattedTime",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Triggered via ${session.triggerSource.name.replace("_", " ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.base))

                // GPS Telemetry Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppRadius.shapeMd,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(AppSpacing.base)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = SapphireBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.sm))
                                Text(
                                    text = "Live GPS Telemetry",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            val loc = session.lastLocation
                            Surface(
                                shape = AppRadius.shapePill,
                                color = if (loc != null) SuccessGreen.copy(alpha = 0.12f) else AttentionAmber.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (loc != null) "SATELLITE LOCK" else "ACQUIRING FIX",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (loc != null) SuccessGreen else AttentionAmber,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.sm))

                        val loc = session.lastLocation
                        if (loc != null) {
                            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(loc.timestamp))
                            Text(
                                text = "Coordinates: ${String.format(Locale.US, "%.5f", loc.latitude)}, ${String.format(Locale.US, "%.5f", loc.longitude)}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.xs))
                            Text(
                                text = "Accuracy: ±${loc.accuracyMeters.toInt()}m • Fix: $timeStr • Stream count: ${session.locationHistoryCount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Acquiring high-precision GPS lock from satellites and cell towers. Coordinates will stream automatically once acquired.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.base))

                // Multi-Channel Delivery Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppRadius.shapeMd,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(AppSpacing.base)) {
                        Text(
                            text = "Emergency Delivery Channels",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.md))

                        // 1. Local SQLite Persistence
                        val localState = session.deliveryStates[DeliveryChannel.LOCAL_PERSISTENCE]
                        DeliveryChannelRow(
                            icon = Icons.Default.Storage,
                            label = "Local Storage (Room)",
                            statusText = localState?.message ?: "Persisted to secure offline SQLite",
                            statusColor = SuccessGreen,
                            badge = "COMMITTED"
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = AppSpacing.sm),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        // 2. Cloud Realtime Tracking Link
                        DeliveryChannelRow(
                            icon = Icons.Default.Cloud,
                            label = "Guardian Web Viewer",
                            statusText = shareUrl,
                            statusColor = SapphireBlue,
                            badge = "ACTIVE",
                            trailingAction = {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(shareUrl))
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Tracking Link",
                                        tint = SapphireBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = AppSpacing.sm),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        // 3. Real Cellular SMS Channel
                        val smsDelivery = session.deliveryStates[DeliveryChannel.SMS]
                        val (smsColor, smsBadge) = when (smsDelivery?.status) {
                            DeliveryStatus.SENT, DeliveryStatus.DELIVERED -> SuccessGreen to "SUBMITTED"
                            DeliveryStatus.SKIPPED_NO_PERMISSION -> AttentionAmber to "PERMISSION NEEDED"
                            DeliveryStatus.FAILED -> EmergencyCoral to "FAILED"
                            DeliveryStatus.PENDING -> SapphireBlue to "IN PROGRESS"
                            else -> MaterialTheme.colorScheme.onSurfaceVariant to "PENDING"
                        }

                        DeliveryChannelRow(
                            icon = Icons.AutoMirrored.Filled.Message,
                            label = "Cellular SMS Alert",
                            statusText = smsDelivery?.message ?: "Preparing emergency SMS broadcast...",
                            statusColor = smsColor,
                            badge = smsBadge
                        )

                        // If direct SMS was not permitted or failed, offer quick manual fallback intent
                        if (smsDelivery?.status == DeliveryStatus.SKIPPED_NO_PERMISSION || smsDelivery?.status == DeliveryStatus.FAILED) {
                            Spacer(modifier = Modifier.height(AppSpacing.sm))
                            OutlinedButton(
                                onClick = {
                                    val sendIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("smsto:")
                                        putExtra(
                                            "sms_body",
                                            "EMERGENCY SOS: I need help! Live tracker: $shareUrl - Sent via TapShield"
                                        )
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(sendIntent)
                                },
                                shape = AppRadius.shapeSm,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Open System SMS App Fallback", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.xl))

                // Primary Safety Resolution Action: "I AM SAFE"
                Button(
                    onClick = { showSafeConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen,
                        contentColor = Color.White
                    ),
                    shape = AppRadius.shapeSm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                    Text(
                        text = "I AM SAFE (STOP TRACKING)",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                // Emergency Call Action: 112 India
                OutlinedButton(
                    onClick = {
                        val callIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:112")
                        }
                        context.startActivity(callIntent)
                    },
                    border = BorderStroke(1.dp, EmergencyCoral),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmergencyCoral),
                    shape = AppRadius.shapeSm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                    Text("Call National Emergency (112)", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(AppSpacing.xl))
            }

            // Safe Confirmation Dialog
            if (showSafeConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showSafeConfirmDialog = false },
                    title = {
                        Text(
                            text = "Resolve Emergency Session?",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text("This will stop live GPS tracking, notify your guardians that you are safe, and conclude the emergency broadcast.")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSafeConfirmDialog = false
                                viewModel.markSafe("User marked safe from Active SOS screen")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SuccessGreen,
                                contentColor = Color.White
                            ),
                            shape = AppRadius.shapeSm
                        ) {
                            Text("Confirm Safe", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSafeConfirmDialog = false }) {
                            Text("Keep Active")
                        }
                    },
                    shape = AppRadius.shapeMd
                )
            }
        }
    }
}

@Composable
private fun DeliveryChannelRow(
    icon: ImageVector,
    label: String,
    statusText: String,
    statusColor: Color,
    badge: String,
    trailingAction: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(statusColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(AppSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = AppRadius.shapePill,
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (trailingAction != null) {
            Spacer(modifier = Modifier.width(AppSpacing.xs))
            trailingAction()
        }
    }
}
