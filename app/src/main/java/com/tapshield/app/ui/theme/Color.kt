package com.tapshield.app.ui.theme

import androidx.compose.ui.graphics.Color

// Brand / Primary Sapphire
val SapphireBlue = Color(0xFF315CE8)
val SapphireBlueDark = Color(0xFF2547B8)
val SapphireLight = Color(0xFFEBF1FF)
val SapphireMuted = Color(0xFF8DA4F7)

// Emergency Coral (Restrained, high-impact)
val EmergencyCoral = Color(0xFFE5484D)
val EmergencyCoralDark = Color(0xFFC73035)
val EmergencyCoralLight = Color(0xFFFFECEE)
val EmergencyCoralContainerDark = Color(0xFF3B1215)
val EmergencyCoralPulse = Color(0x33E5484D)

// Success Green
val SuccessGreen = Color(0xFF18865B)
val SuccessGreenLight = Color(0xFFEAFBF3)
val SuccessGreenDark = Color(0xFF0F5A3C)

// Amber / Caution
val AttentionAmber = Color(0xFFD97706)
val AttentionAmberLight = Color(0xFFFEF3C7)
val AttentionAmberDark = Color(0xFF451A03)

// Dark Theme Surfaces (Midnight Navy)
val MidnightNavy = Color(0xFF101827)
val MidnightSurface = Color(0xFF172235)
val MidnightSurfaceElevated = Color(0xFF1E2B42)
val MidnightBorder = Color(0xFF2B3A54)

// Light Theme Surfaces (Porcelain & Crisp Slate)
val PorcelainBackground = Color(0xFFF6F7FB)
val PorcelainSurface = Color(0xFFFFFFFF)
val PorcelainSurfaceElevated = Color(0xFFF0F3F9)
val PorcelainBorder = Color(0xFFE3E7EF)

// Text Tokens - Dark Theme
val TextPrimaryDark = Color(0xFFF9FAFB)
val TextSecondaryDark = Color(0xFF94A3B8)
val TextMutedDark = Color(0xFF64748B)

// Text Tokens - Light Theme
val TextPrimaryLight = Color(0xFF111827)
val TextSecondaryLight = Color(0xFF4B5563)
val TextMutedLight = Color(0xFF6B7280)

// Backward compatibility aliases during migration
val ShieldCyan = SapphireBlue
val ShieldReadyGreen = SuccessGreen
val EmergencyRed = EmergencyCoral
val EmergencyRedContainer = EmergencyCoralContainerDark
val AmberWarning = AttentionAmber
val SurfaceElevatedDark = MidnightSurfaceElevated
val SurfaceBorderDark = MidnightBorder