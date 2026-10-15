package com.tapshield.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.tapshield.app.ui.theme.AppRadius
import com.tapshield.app.ui.theme.AppSpacing
import com.tapshield.app.ui.theme.AttentionAmber
import com.tapshield.app.ui.theme.SapphireBlue
import com.tapshield.app.ui.theme.SuccessGreen
import com.tapshield.app.ui.viewmodel.SafetySetupViewModel

@Composable
fun SafetySetupScreen(
    viewModel: SafetySetupViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.setupState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        checkCurrentPermissions(context, viewModel)
    }

    LaunchedEffect(Unit) {
        checkCurrentPermissions(context, viewModel)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.base)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Safety Setup",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Verify device permissions and quick triggers for maximum emergency reliability",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(AppSpacing.base))

        // Statutory Notice (Emergency 112 India)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = AppRadius.shapeMd,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, SapphireBlue.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(AppSpacing.base),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SapphireBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(AppSpacing.sm))
                Column {
                    Text(
                        text = "Emergency Services Notice (India)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SapphireBlue
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "TapShield supplements and does not replace official national emergency response. For life-threatening emergencies, always dial 112 directly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.xl))

        Text(
            text = "REQUIRED PERMISSIONS",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = SapphireBlue
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))

        // 1. High-Accuracy GPS
        PermissionRow(
            title = "High-Accuracy GPS",
            description = "Acquires real-time coordinates to stream to your guardians during an emergency",
            isGranted = state.hasFineLocationPermission,
            icon = Icons.Default.LocationOn,
            onRequest = {
                val perms = mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                permissionLauncher.launch(perms.toTypedArray())
            }
        )

        Spacer(modifier = Modifier.height(AppSpacing.sm))

        // 2. Persistent Notifications
        PermissionRow(
            title = "Emergency Notifications",
            description = "Maintains persistent foreground service to track location while device screen is locked",
            isGranted = state.hasNotificationPermission,
            icon = Icons.Default.Notifications,
            onRequest = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                }
            }
        )

        Spacer(modifier = Modifier.height(AppSpacing.sm))

        // 3. Cellular SMS Dispatch
        PermissionRow(
            title = "Cellular SMS Alerts",
            description = "Dispatches SMS emergency alert with live GPS coordinates to guardians when offline",
            isGranted = state.hasSmsPermission,
            icon = Icons.AutoMirrored.Filled.Send,
            onRequest = {
                permissionLauncher.launch(arrayOf(Manifest.permission.SEND_SMS))
            }
        )

        Spacer(modifier = Modifier.height(AppSpacing.xl))

        Text(
            text = "BACKGROUND RELIABILITY",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = SapphireBlue
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = AppRadius.shapeMd,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(AppSpacing.base)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = null,
                        tint = if (state.isBatteryOptimizationIgnored) SuccessGreen else AttentionAmber
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Battery Optimization Exemption",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (state.isBatteryOptimizationIgnored)
                                "Unrestricted background running granted"
                            else "Device power saver may restrict tracking when screen is locked",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!state.isBatteryOptimizationIgnored) {
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SapphireBlue,
                            contentColor = Color.White
                        ),
                        shape = AppRadius.shapeSm
                    ) {
                        Text("Disable Battery Limits", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.xl))

        Text(
            text = "QUICK EMERGENCY ENTRY POINTS",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = SapphireBlue
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))

        // Home Screen Widget
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = AppRadius.shapeMd,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.base),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = null,
                    tint = SapphireBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(AppSpacing.md))
                Column {
                    Text(
                        text = "Home Screen Widget",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Long press home space -> Widgets -> TapShield SOS for instant one-tap trigger.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.sm))

        // Quick Settings Tile
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = AppRadius.shapeMd,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.base),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = SapphireBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(AppSpacing.md))
                Column {
                    Text(
                        text = "Quick Settings Tile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Swipe down notifications -> Tap Edit (Pencil) -> Add TapShield SOS tile for locked-screen emergency access.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.xl))

        // Experimental Shake Trigger
        Text(
            text = "EXPERIMENTAL GESTURE TRIGGER",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = SapphireBlue
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = AppRadius.shapeMd,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(AppSpacing.base)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = SapphireBlue
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.md))
                        Column {
                            Text(
                                text = "Shake-to-Trigger",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Multi-axis accelerometer emergency trigger",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = state.shakeTriggerEnabled,
                        onCheckedChange = { viewModel.toggleShakeTrigger(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = SapphireBlue)
                    )
                }

                if (state.shakeTriggerEnabled) {
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    val sensitivityText = String.format(java.util.Locale.US, "%.1f", state.shakeSensitivity)
                    Text(
                        text = "Sensitivity Threshold: $sensitivityText G",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = state.shakeSensitivity,
                        onValueChange = { viewModel.updateShakeSensitivity(it) },
                        valueRange = 1.5f..4.5f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = SapphireBlue,
                            activeTrackColor = SapphireBlue
                        )
                    )
                    Text(
                        text = "Higher thresholds prevent false triggers from jogging or bumpy commutes.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.xxl))
    }
}

private fun checkCurrentPermissions(context: Context, viewModel: SafetySetupViewModel) {
    val fineLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val bgLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    } else true

    val notifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else true

    val sms = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.SEND_SMS
    ) == PackageManager.PERMISSION_GRANTED

    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    val isBatteryIgnored = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true

    viewModel.updatePermissions(
        fineLocation = fineLocation,
        bgLocation = bgLocation,
        notifications = notifications,
        sms = sms,
        batteryIgnored = isBatteryIgnored
    )
}

@Composable
private fun PermissionRow(
    title: String,
    description: String,
    isGranted: Boolean,
    icon: ImageVector,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppRadius.shapeMd,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
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
                    .background(
                        if (isGranted) SuccessGreen.copy(alpha = 0.12f)
                        else AttentionAmber.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGranted) SuccessGreen else AttentionAmber,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(AppSpacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isGranted) {
                Surface(
                    shape = AppRadius.shapePill,
                    color = SuccessGreen.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "GRANTED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }
            } else {
                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SapphireBlue,
                        contentColor = Color.White
                    ),
                    shape = AppRadius.shapeSm
                ) {
                    Text("Grant", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
