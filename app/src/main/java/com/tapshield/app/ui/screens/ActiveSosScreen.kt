package com.tapshield.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapshield.app.domain.model.DeliveryChannel
import com.tapshield.app.domain.model.DeliveryStatus
import com.tapshield.app.domain.state.SosState
import com.tapshield.app.ui.theme.AmberWarning
import com.tapshield.app.ui.theme.EmergencyRed
import com.tapshield.app.ui.theme.EmergencyRedContainer
import com.tapshield.app.ui.theme.ShieldCyan
import com.tapshield.app.ui.theme.ShieldReadyGreen
import com.tapshield.app.ui.theme.SurfaceBorderDark
import com.tapshield.app.ui.theme.SurfaceElevatedDark
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
                    .padding(24.dp),
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
                            .background(ShieldReadyGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ShieldReadyGreen,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No Active Emergency",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "TapShield is in standby. If you are in danger, activate SOS from the home screen, widget, or Quick Settings tile.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onNavigateHome,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShieldCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Return to Shield Dashboard", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        is SosState.SafeResolved -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(ShieldReadyGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ShieldReadyGreen,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "EMERGENCY RESOLVED",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = ShieldReadyGreen
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Safe status logged. Location tracking has ceased and guardians notified.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            viewModel.resetToIdle()
                            onNavigateHome()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShieldReadyGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Back to Standby", fontWeight = FontWeight.Bold)
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
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Active Header Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(EmergencyRedContainer, Color(0xFF280505))
                            )
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(EmergencyRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "SOS BROADCAST ACTIVE",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = EmergencyRed
                        )

                        Text(
                            text = "Elapsed Time: $formattedTime",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Trigger: ${session.triggerSource.name}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // GPS Location Data Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevatedDark),
                    border = BorderStroke(1.dp, SurfaceBorderDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = ShieldCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Live Location",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ShieldReadyGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ACQUIRING",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ShieldReadyGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val loc = session.lastLocation
                        if (loc != null) {
                            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(loc.timestamp))
                            Text(
                                text = "Lat: ${String.format(Locale.US, "%.5f", loc.latitude)}, Lng: ${String.format(Locale.US, "%.5f", loc.longitude)}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Accuracy: ±${loc.accuracyMeters.toInt()}m • Updated: $timeStr",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Waiting for initial GPS fix...",
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Multi-Channel Delivery Matrix
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevatedDark),
                    border = BorderStroke(1.dp, SurfaceBorderDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Delivery Channels",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Local SQLite
                        DeliveryRow(
                            icon = Icons.Default.Storage,
                            label = "Local Storage (Room)",
                            status = "SAVED",
                            statusColor = ShieldReadyGreen
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Cloud Live Link
                        DeliveryRow(
                            icon = Icons.Default.Cloud,
                            label = "Cloud Realtime Sync",
                            status = "CONNECTED",
                            statusColor = ShieldCyan
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // SMS Dispatch
                        DeliveryRow(
                            icon = Icons.Default.Message,
                            label = "SMS Alert Fallback",
                            status = "DISPATCHING",
                            statusColor = AmberWarning
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Public Live Location Link Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevatedDark),
                    border = BorderStroke(1.dp, SurfaceBorderDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Emergency Web Tracking Link",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = shareUrl,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ShieldCyan
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(shareUrl))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Copy Link",
                                tint = ShieldCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Emergency Call Action (112)
                Button(
                    onClick = {
                        val callIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:112")
                        }
                        context.startActivity(callIntent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmergencyRedContainer,
                        contentColor = EmergencyRed
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Call Police / Emergency (112)", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // "I AM SAFE" Resolution Action
                Button(
                    onClick = { showSafeConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ShieldReadyGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("I AM SAFE - RESOLVE EMERGENCY", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }

            if (showSafeConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showSafeConfirmDialog = false },
                    title = {
                        Text("Confirm You Are Safe", fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Text("This will stop live GPS tracking and broadcast an \"I Am Safe\" resolution notification to all your trusted contacts.")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSafeConfirmDialog = false
                                viewModel.markSafe()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ShieldReadyGreen,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Yes, I'm Safe", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSafeConfirmDialog = false }) {
                            Text("Cancel")
                        }
                    },
                    containerColor = SurfaceElevatedDark
                )
            }
        }
    }
}

@Composable
private fun DeliveryRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    status: String,
    statusColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(statusColor.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = status,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}
