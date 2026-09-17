package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.PowerStatus
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricYellow
import com.example.ui.theme.StatusOff
import com.example.ui.theme.StatusOn
import com.example.ui.theme.StatusUncertain

@Composable
fun LightningAnimation(
    modifier: Modifier = Modifier,
    isScanning: Boolean = false,
    status: PowerStatus? = null,
    size: Dp = 100.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lightning_infinite")

    // Pulsing aura scale
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = if (isScanning) 1.25f else 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isScanning) 600 else 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_scale"
    )

    // Alpha shimmer
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (isScanning) 0.8f else 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isScanning) 500 else 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_alpha"
    )

    // Lightning scale
    val boltScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isScanning) 1.15f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isScanning) 400 else 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bolt_scale"
    )

    val primaryGlowColor = when {
        isScanning -> ElectricBlue
        status == PowerStatus.PROBABLY_ON -> StatusOn
        status == PowerStatus.PROBABLY_OFF -> StatusOff
        status == PowerStatus.UNCERTAIN -> StatusUncertain
        else -> ElectricYellow
    }

    val iconColor = when {
        isScanning -> ElectricYellow
        status == PowerStatus.PROBABLY_ON -> StatusOn
        status == PowerStatus.PROBABLY_OFF -> StatusOff
        status == PowerStatus.UNCERTAIN -> StatusUncertain
        else -> ElectricYellow
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag("lightning_animation_box"),
        contentAlignment = Alignment.Center
    ) {
        // Glowing radial canvas
        Canvas(
            modifier = Modifier
                .size(size * 1.3f)
                .scale(auraScale)
        ) {
            val radius = this.size.minDimension / 2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryGlowColor.copy(alpha = auraAlpha),
                        primaryGlowColor.copy(alpha = auraAlpha * 0.4f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }

        // Central Electric Bolt Icon
        Icon(
            imageVector = Icons.Filled.Bolt,
            contentDescription = "Electric Bolt Indicator",
            tint = iconColor,
            modifier = Modifier
                .size(size * 0.72f)
                .scale(boltScale)
        )
    }
}
