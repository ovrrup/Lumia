package lumia.tracker.ui.screens.sync.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore

/**
 * Animated radar scanning animation for P2P local network peer discovery.
 */
@ValueScore(
    score = 88,
    importance = Importance.MEDIUM,
    description = "Animated radar scanning canvas with concentric rings and sweeping beam for P2P mesh discovery",
    category = "Sync"
)
@Composable
fun SyncRadarView(
    isScanning: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = MaterialTheme.colorScheme.tertiary,
    content: @Composable () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")

    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse1"
    )

    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha1"
    )

    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse2"
    )

    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha2"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarSweep"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val maxRadius = this.size.minDimension / 2f

            // Static concentric grid circles
            drawCircle(
                color = primaryColor.copy(alpha = 0.1f),
                radius = maxRadius * 0.35f,
                center = center,
                style = Stroke(width = 1.5f)
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.15f),
                radius = maxRadius * 0.70f,
                center = center,
                style = Stroke(width = 1.5f)
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.2f),
                radius = maxRadius * 0.98f,
                center = center,
                style = Stroke(width = 2.0f)
            )

            // Crosshair axes
            drawLine(
                color = primaryColor.copy(alpha = 0.08f),
                start = Offset(center.x - maxRadius * 0.98f, center.y),
                end = Offset(center.x + maxRadius * 0.98f, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = primaryColor.copy(alpha = 0.08f),
                start = Offset(center.x, center.y - maxRadius * 0.98f),
                end = Offset(center.x, center.y + maxRadius * 0.98f),
                strokeWidth = 1.dp.toPx()
            )

            if (isScanning) {
                // Expanding pulse waves
                drawCircle(
                    color = primaryColor.copy(alpha = pulseAlpha1 * 0.4f),
                    radius = maxRadius * pulseScale1,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = accentColor.copy(alpha = pulseAlpha2 * 0.4f),
                    radius = maxRadius * pulseScale2,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Rotating radar beam
                val sweepRad = Math.toRadians(rotationAngle.toDouble())
                val sweepEnd = Offset(
                    (center.x + maxRadius * 0.98f * Math.cos(sweepRad)).toFloat(),
                    (center.y + maxRadius * 0.98f * Math.sin(sweepRad)).toFloat()
                )
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.85f),
                            accentColor.copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        start = center,
                        end = sweepEnd
                    ),
                    start = center,
                    end = sweepEnd,
                    strokeWidth = 3.dp.toPx()
                )
            }
        }

        content()
    }
}