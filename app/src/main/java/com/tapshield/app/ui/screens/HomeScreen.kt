package com.tapshield.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.core.content.ContextCompat
import com.tapshield.app.domain.state.SosState
import com.tapshield.app.ui.components.CountdownOverlay
import com.tapshield.app.ui.components.EmergencyButton
import com.tapshield.app.ui.theme.AppRadius
import com.tapshield.app.ui.theme.AppSpacing
import com.tapshield.app.ui.theme.AttentionAmber
import com.tapshield.app.ui.theme.EmergencyCoral
import com.tapshield.app.ui.theme.SapphireBlue
import com.tapshield.app.ui.theme.SuccessGreen
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
    val context = LocalContext.current

    val isArmed = contactCount > 0
    val isCountingDown = sosState is SosState.Countdown
    val isActive = sosState is SosState.Active

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Permissions updated
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.base),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(AppRadius.shapeSm)
                            .background(SapphireBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = SapphireBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSpacing.md))
                    Column {
                        Text(
                            text = "TapShield",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Personal Safety Network",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Readiness Status Pill
                val statusBg = if (isActive) EmergencyCoral.copy(alpha = 0.12f)
                else if (isArmed) SuccessGreen.copy(alpha = 0.12f)
                else AttentionAmber.copy(alpha = 0.12f)

                val statusBorder = if (isActive) EmergencyCoral.copy(alpha = 0.35f)
                else if (isArmed) SuccessGreen.copy(alpha = 0.35f)
                else AttentionAmber.copy(alpha = 0.35f)

                val statusColor = if (isActive) EmergencyCoral
                else if (isArmed) SuccessGreen
                else AttentionAmber

                val statusText = if (isActive) "SOS ACTIVE"
                else if (isArmed) "PROTECTED"
                else "SETUP NEEDED"

                Surface(
                    shape = AppRadius.shapePill,
                    color = statusBg,
                    border = BorderStroke(1.dp, statusBorder),
                    modifier = Modifier.clickable {
                        if (isActive) onNavigateToActiveSos()
                        else if (!isArmed) onNavigateToContacts()
                        else onNavigateToSetup()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.base))

            // Active SOS Urgent Alert Banner (shown only when emergency is running)
            if (isActive) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToActiveSos() },
                    shape = AppRadius.shapeMd,
                    colors = CardDefaults.cardColors(containerColor = EmergencyCoral),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(AppSpacing.base),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(AppSpacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Emergency Session Active",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Live location broadcast in progress • Tap to open",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Active Session",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AppSpacing.base))
            } else if (!isArmed) {
                // Setup warning banner if 0 contacts
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToContacts() },
                    shape = AppRadius.shapeMd,
                    colors = CardDefaults.cardColors(
                        containerColor = AttentionAmber.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, AttentionAmber.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(AppSpacing.base),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = AttentionAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "No trusted contacts added",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Add at least one guardian to receive your location during SOS",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AppSpacing.base))
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // Prominent Centerpiece: SOS Action
            EmergencyButton(
                isArmed = isArmed,
                isCountingDown = isCountingDown,
                onTrigger = {
                    if (isActive) {
                        onNavigateToActiveSos()
                    } else {
                        val fineLocation = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!fineLocation) {
                            val perms = mutableListOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                perms.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            permissionLauncher.launch(perms.toTypedArray())
                        }
                        viewModel.triggerSos(instant = false)
                    }
                }
            )

            Spacer(modifier = Modifier.height(AppSpacing.md))

            Text(
                text = if (isCountingDown) "Tap to abort emergency dispatch"
                else "Tap to initiate 3-second safety sequence",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(AppSpacing.xl))

            // Consolidated Unified Preparedness Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppRadius.shapeMd,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Row 1: Trusted Contacts
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToContacts() }
                            .padding(AppSpacing.base),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SapphireBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = SapphireBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(AppSpacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Trusted Guardians",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (contactCount == 0) "No guardians configured"
                                else "$contactCount guardian${if (contactCount > 1) "s" else ""} will receive emergency alerts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Manage Guardians",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = AppSpacing.base),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    // Row 2: Emergency 112 India Call
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val callIntent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:112")
                                }
                                context.startActivity(callIntent)
                            }
                            .padding(AppSpacing.base),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EmergencyCoral.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = EmergencyCoral,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(AppSpacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Emergency Services (112)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Direct dial to National Emergency Response Center",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Call 112",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Row 3: Offline Sync status (shown if queued events exist)
                    if (pendingEvents > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = AppSpacing.base),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.base),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AttentionAmber.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = AttentionAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(AppSpacing.md))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Offline Persistence",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$pendingEvents event${if (pendingEvents > 1) "s" else ""} queued in SQLite storage",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xl))
        }

        // Countdown Overlay
        if (sosState is SosState.Countdown) {
            val countdown = sosState as SosState.Countdown
            CountdownOverlay(
                secondsRemaining = countdown.secondsRemaining,
                onCancel = { viewModel.cancelCountdown() }
            )
        }
    }
}
