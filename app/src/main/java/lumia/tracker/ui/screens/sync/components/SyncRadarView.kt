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
    description = "Animated radar scanning canvas with clean concentric rings and sweeping beam for P2P mesh discovery",
    category = "Sync"
)
@Composable
fun SyncRadarView(
    isScanning: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = MaterialTheme.colorScheme.tertiary,
    content: @Composable () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
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

            // Clean concentric grid rings
            drawCircle(
                color = primaryColor.copy(alpha = 0.1f),
                radius = maxRadius * 0.5f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.15f),
                radius = maxRadius * 0.95f,
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )

            if (isScanning) {
                // Expanding pulse wave
                drawCircle(
                    color = primaryColor.copy(alpha = pulseAlpha * 0.35f),
                    radius = maxRadius * pulseScale,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Rotating radar beam
                val sweepRad = Math.toRadians(rotationAngle.toDouble())
                val sweepEnd = Offset(
                    (center.x + maxRadius * 0.95f * Math.cos(sweepRad)).toFloat(),
                    (center.y + maxRadius * 0.95f * Math.sin(sweepRad)).toFloat()
                )
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.7f),
                            accentColor.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        start = center,
                        end = sweepEnd
                    ),
                    start = center,
                    end = sweepEnd,
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        content()
    }
}