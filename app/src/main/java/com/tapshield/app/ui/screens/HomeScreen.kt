package com.tapshield.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapshield.app.domain.state.SosState
import com.tapshield.app.ui.components.CountdownOverlay
import com.tapshield.app.ui.components.EmergencyButton
import com.tapshield.app.ui.components.StatusCard
import com.tapshield.app.ui.theme.AmberWarning
import com.tapshield.app.ui.theme.EmergencyRed
import com.tapshield.app.ui.theme.ShieldCyan
import com.tapshield.app.ui.theme.ShieldReadyGreen
import com.tapshield.app.ui.theme.SurfaceElevatedDark
import com.tapshield.app.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToActiveSos: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sosState by viewModel.sosState.collectAsState()
    val contactCount by viewModel.contactCount.collectAsState()
    val pendingEvents by viewModel.pendingEventsCount.collectAsState()
    val isStealth by viewModel.isStealthDisguiseMode.collectAsState()
    val isArmed = contactCount > 0
    val isCountingDown = sosState is SosState.Countdown
    val isActive = sosState is SosState.Active
    val context = LocalContext.current

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Permissions result handled
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Title & Stealth Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isStealth) "System Diagnostics" else "TapShield",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isStealth) "Hardware telemetry active" else "Discreet Personal Safety",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleStealthMode() }
                ) {
                    Icon(
                        imageVector = if (isStealth) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Stealth Disguise",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Active Alert Banner if emergency is live
            if (isActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmergencyRed)
                        .clickable { onNavigateToActiveSos() }
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "SOS SESSION ACTIVE",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Tap to open emergency dashboard",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Readiness Card
            StatusCard(
                title = if (isStealth) "Device Status" else "Shield Readiness",
                subtitle = when {
                    !isArmed -> "Action needed: 0 trusted contacts added"
                    isActive -> "Emergency broadcast live"
                    else -> "Sensors armed • Ready for 1-tap activation"
                },
                icon = if (isArmed) Icons.Default.CheckCircle else Icons.Default.Info,
                statusColor = if (isArmed) ShieldReadyGreen else AmberWarning,
                statusLabel = if (isArmed) "ARMED" else "SETUP NEEDED",
                trailingAction = if (!isArmed) {
                    {
                        Button(
                            onClick = onNavigateToContacts,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberWarning,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Add Contacts", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else null
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Main Emergency Tactical Button
            EmergencyButton(
                isArmed = isArmed,
                isCountingDown = isCountingDown,
                isStealthMode = isStealth,
                onTrigger = {
                    if (isActive) {
                        onNavigateToActiveSos()
                    } else {
                        val fineLocation = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                        if (!fineLocation) {
                            val perms = mutableListOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                perms.add(android.Manifest.permission.POST_NOTIFICATIONS)
                            }
                            permissionLauncher.launch(perms.toTypedArray())
                        }
                        viewModel.triggerSos(instant = false)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isStealth) "Tap to run hardware diagnostic" else "Tap for 3s safety countdown",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Telemetry Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusCard(
                    title = if (isStealth) "Location Service" else "GPS Tracker",
                    subtitle = "High-accuracy fused provider standby",
                    icon = Icons.Default.LocationOn,
                    statusColor = ShieldCyan,
                    statusLabel = "READY"
                )

                StatusCard(
                    title = if (isStealth) "Disk Buffer" else "Offline Queue",
                    subtitle = if (pendingEvents == 0) "All events synced" else "$pendingEvents events queued for retry",
                    icon = Icons.Default.Storage,
                    statusColor = if (pendingEvents == 0) ShieldReadyGreen else AmberWarning,
                    statusLabel = if (pendingEvents == 0) "HEALTHY" else "$pendingEvents PENDING"
                )

                StatusCard(
                    title = if (isStealth) "Contact Gateway" else "Trusted Guardians",
                    subtitle = "$contactCount emergency contacts registered",
                    icon = Icons.Default.Phone,
                    statusColor = if (contactCount > 0) ShieldCyan else AmberWarning,
                    statusLabel = "$contactCount SAVED"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Emergency Helpline Shortcut (112 India)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceElevatedDark)
                    .clickable {
                        val callIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:112")
                        }
                        context.startActivity(callIntent)
                    }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Emergency Call",
                        tint = EmergencyRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Call Emergency Services (112)",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "India national emergency helpline",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Overlay when Countdown is active
        if (sosState is SosState.Countdown) {
            val countdown = sosState as SosState.Countdown
            CountdownOverlay(
                secondsRemaining = countdown.secondsRemaining,
                onCancel = { viewModel.cancelCountdown() }
            )
        }
    }
}
