package com.tapshield.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapshield.app.ui.theme.EmergencyCoral
import com.tapshield.app.ui.theme.EmergencyCoralDark
import com.tapshield.app.ui.theme.EmergencyCoralPulse
import com.tapshield.app.ui.theme.SapphireBlue
import com.tapshield.app.ui.theme.SapphireBlueDark

@Composable
fun EmergencyButton(
    isArmed: Boolean,
    isCountingDown: Boolean,
    onTrigger: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCountingDown) 1.2f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isCountingDown) 500 else 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val primaryColor = if (isCountingDown) EmergencyCoral else SapphireBlue
    val primaryDark = if (isCountingDown) EmergencyCoralDark else SapphireBlueDark
    val pulseColor = if (isCountingDown) EmergencyCoralPulse else SapphireBlue.copy(alpha = 0.12f)

    Box(
        modifier = modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient ring
        Box(
            modifier = Modifier
                .size(190.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(pulseColor)
        )

        // Outer crisp hairline border
        Box(
            modifier = Modifier
                .size(162.dp)
                .clip(CircleShape)
                .border(
                    BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.35f)),
                    CircleShape
                )
        )

        // Tactile interactive button core
        Box(
            modifier = Modifier
                .size(142.dp)
                .scale(if (isPressed) 0.94f else 1f)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(primaryColor, primaryDark)
                    )
                )
                .border(BorderStroke(2.dp, Color.White.copy(alpha = 0.25f)), CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        onTrigger()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isCountingDown) Icons.Default.Warning else Icons.Default.Shield,
                    contentDescription = "Trigger Emergency SOS",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isCountingDown) "ALERT" else "SOS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}
