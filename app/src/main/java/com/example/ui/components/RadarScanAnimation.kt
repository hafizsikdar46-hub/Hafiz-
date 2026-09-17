package com.example.ui.components

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiFind
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlue

@Composable
fun RadarScanAnimation(
    modifier: Modifier = Modifier,
    stageText: String = "Scanning Wi-Fi radio frequencies..."
) {
    val transition = rememberInfiniteTransition(label = "radar_transition")

    val pulseRing1 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_ring_1"
    )

    val pulseRing2 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, delayMillis = 600, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_ring_2"
    )

    val pulseRing3 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, delayMillis = 1200, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_ring_3"
    )

    Column(
        modifier = modifier
            .padding(vertical = 16.dp)
            .testTag("radar_scan_column"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(160.dp)) {
                val maxRadius = size.minDimension / 2f

                // Draw pulsing rings
                listOf(pulseRing1, pulseRing2, pulseRing3).forEach { progress ->
                    val ringRadius = maxRadius * progress
                    val ringAlpha = (1.0f - progress).coerceIn(0f, 1f)
                    drawCircle(
                        color = ElectricBlue.copy(alpha = ringAlpha * 0.7f),
                        radius = ringRadius,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // Center fixed ring
                drawCircle(
                    color = ElectricBlue.copy(alpha = 0.25f),
                    radius = maxRadius * 0.35f,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            Icon(
                imageVector = Icons.Default.WifiFind,
                contentDescription = "Wi-Fi Scanning",
                tint = ElectricBlue,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.5.dp,
            color = ElectricBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stageText,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.2.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Listening for 802.11 beacon frames...",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
