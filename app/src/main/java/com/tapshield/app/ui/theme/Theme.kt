package com.tapshield.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SapphireBlue,
    onPrimary = Color.White,
    primaryContainer = MidnightSurfaceElevated,
    onPrimaryContainer = SapphireLight,
    secondary = SuccessGreen,
    onSecondary = Color.White,
    secondaryContainer = SuccessGreenDark,
    onSecondaryContainer = SuccessGreenLight,
    tertiary = EmergencyCoral,
    onTertiary = Color.White,
    tertiaryContainer = EmergencyCoralContainerDark,
    onTertiaryContainer = EmergencyCoralLight,
    background = MidnightNavy,
    onBackground = TextPrimaryDark,
    surface = MidnightSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = MidnightSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = MidnightBorder,
    outlineVariant = MidnightSurfaceElevated
)

private val LightColorScheme = lightColorScheme(
    primary = SapphireBlue,
    onPrimary = Color.White,
    primaryContainer = SapphireLight,
    onPrimaryContainer = SapphireBlueDark,
    secondary = SuccessGreen,
    onSecondary = Color.White,
    secondaryContainer = SuccessGreenLight,
    onSecondaryContainer = SuccessGreenDark,
    tertiary = EmergencyCoral,
    onTertiary = Color.White,
    tertiaryContainer = EmergencyCoralLight,
    onTertiaryContainer = EmergencyCoralDark,
    background = PorcelainBackground,
    onBackground = TextPrimaryLight,
    surface = PorcelainSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = PorcelainSurfaceElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = PorcelainBorder,
    outlineVariant = PorcelainBorder
)

@Composable
fun TapShieldTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}