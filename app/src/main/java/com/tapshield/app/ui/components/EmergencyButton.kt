package com.tapshield.app.ui.components

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tapshield.app.ui.theme.EmergencyRed
import com.tapshield.app.ui.theme.EmergencyRedPulse
import com.tapshield.app.ui.theme.ShieldCyan

@Composable
fun EmergencyButton(
    isArmed: Boolean,
    isCountingDown: Boolean,
    onTrigger: () -> Unit,
    modifier: Modifier = Modifier,
    isStealthMode: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCountingDown) 1.25f else 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isCountingDown) 600 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val mainColor = when {
        isCountingDown -> EmergencyRed
        isStealthMode -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> ShieldCyan
    }

    val glowColor = when {
        isCountingDown -> EmergencyRedPulse
        isStealthMode -> Color.Transparent
        else -> ShieldCyan.copy(alpha = 0.2f)
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing ring
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(glowColor)
        )

        // Middle ring border
        Box(
            modifier = Modifier
                .size(165.dp)
                .clip(CircleShape)
                .border(
                    BorderStroke(
                        2.dp,
                        Brush.radialGradient(
                            listOf(mainColor.copy(alpha = 0.8f), Color.Transparent)
                        )
                    ),
                    CircleShape
                )
        )

        // Main inner interactive circle
        Box(
            modifier = Modifier
                .size(135.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .border(BorderStroke(2.dp, mainColor), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onTrigger
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isCountingDown) Icons.Default.Warning else Icons.Default.Shield,
                    contentDescription = "Activate Shield",
                    tint = mainColor,
                    modifier = Modifier.size(42.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isCountingDown) "ALERT" else if (isStealthMode) "SCAN" else "SOS",
                    style = MaterialTheme.typography.titleMedium,
                    color = mainColor
                )
            }
        }
    }
}
