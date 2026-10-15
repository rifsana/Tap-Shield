package com.tapshield.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ShieldCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF9CF0FF),
    secondary = ShieldReadyGreen,
    onSecondary = Color(0xFF003822),
    secondaryContainer = ShieldGreenMuted,
    onSecondaryContainer = Color(0xFF67FEB6),
    tertiary = EmergencyRedLight,
    onTertiary = Color(0xFF5E000B),
    tertiaryContainer = EmergencyRedContainer,
    onTertiaryContainer = Color(0xFFFFDAD8),
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevatedDark,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorderDark
)

private val LightColorScheme = darkColorScheme(
    primary = ShieldCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF9CF0FF),
    secondary = ShieldReadyGreen,
    onSecondary = Color(0xFF003822),
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevatedDark,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorderDark
)

@Composable
fun TapShieldTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // TapShield enforces a sleek, discreet dark theme by default for tactical discretion & battery conservation
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}