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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore

/**
 * Animated radar scanning animation for P2P local network peer discovery.
 * Features dual ripple pulses, a rotating phosphor sweep cone, and minimalist scope markings.
 */
@ValueScore(
    score = 92,
    importance = Importance.HIGH,
    description = "Ultra-clean animated radar scanning canvas with concentric rings, dual ripple pulses, and phosphor sweeping cone for P2P mesh discovery",
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

    // Ripple 1
    val pulse1Scale by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse1Scale"
    )
    val pulse1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse1Alpha"
    )

    // Ripple 2 (Offset phase)
    val pulse2Scale by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse2Scale"
    )
    val pulse2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse2Alpha"
    )

    // Radar Sweeping Cone Rotation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarSweepAngle"
    )

    // Idle breathing ambient glow
    val idleGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdleGlowAlpha"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val maxRadius = this.size.minDimension / 2f

            // 1. Concentric grid rings
            // Inner ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.08f),
                radius = maxRadius * 0.35f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            // Mid dashed ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.12f),
                radius = maxRadius * 0.65f,
                center = center,
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))
                )
            )
            // Outer ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.18f),
                radius = maxRadius * 0.95f,
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )

            // 2. Minimalist Crosshairs / Cardinal notches
            val notchLen = 6.dp.toPx()
            val outerR = maxRadius * 0.95f
            // North
            drawLine(
                color = primaryColor.copy(alpha = 0.25f),
                start = Offset(center.x, center.y - outerR - notchLen / 2),
                end = Offset(center.x, center.y - outerR + notchLen / 2),
                strokeWidth = 1.5.dp.toPx()
            )
            // South
            drawLine(
                color = primaryColor.copy(alpha = 0.25f),
                start = Offset(center.x, center.y + outerR - notchLen / 2),
                end = Offset(center.x, center.y + outerR + notchLen / 2),
                strokeWidth = 1.5.dp.toPx()
            )
            // West
            drawLine(
                color = primaryColor.copy(alpha = 0.25f),
                start = Offset(center.x - outerR - notchLen / 2, center.y),
                end = Offset(center.x - outerR + notchLen / 2, center.y),
                strokeWidth = 1.5.dp.toPx()
            )
            // East
            drawLine(
                color = primaryColor.copy(alpha = 0.25f),
                start = Offset(center.x + outerR - notchLen / 2, center.y),
                end = Offset(center.x + outerR + notchLen / 2, center.y),
                strokeWidth = 1.5.dp.toPx()
            )

            if (isScanning) {
                // 3. Expanding ripple wave 1
                drawCircle(
                    color = primaryColor.copy(alpha = pulse1Alpha * 0.35f),
                    radius = maxRadius * pulse1Scale,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // 4. Expanding ripple wave 2
                drawCircle(
                    color = accentColor.copy(alpha = pulse2Alpha * 0.3f),
                    radius = maxRadius * pulse2Scale,
                    center = center,
                    style = Stroke(width = 1.8.dp.toPx())
                )

                // 5. Sweeping Radar Cone & Leading Beam
                rotate(degrees = rotationAngle, pivot = center) {
                    // Phosphor trailing sector arc (55 degrees trail)
                    val arcRect = androidx.compose.ui.geometry.Rect(
                        center.x - outerR,
                        center.y - outerR,
                        center.x + outerR,
                        center.y + outerR
                    )
                    drawArc(
                        brush = Brush.sweepGradient(
                            0.0f to primaryColor.copy(alpha = 0.28f),
                            0.15f to accentColor.copy(alpha = 0.08f),
                            0.20f to Color.Transparent,
                            1.0f to Color.Transparent,
                            center = center
                        ),
                        startAngle = -55f,
                        sweepAngle = 55f,
                        useCenter = true,
                        topLeft = arcRect.topLeft,
                        size = arcRect.size
                    )

                    // Sharp leading beam line
                    val beamEnd = Offset(center.x + outerR, center.y)
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.9f),
                                accentColor.copy(alpha = 0.6f),
                                Color.Transparent
                            ),
                            start = center,
                            end = beamEnd
                        ),
                        start = center,
                        end = beamEnd,
                        strokeWidth = 2.2.dp.toPx()
                    )
                }
            } else {
                // Ambient idle center halo
                drawCircle(
                    color = primaryColor.copy(alpha = idleGlowAlpha * 0.2f),
                    radius = maxRadius * 0.4f,
                    center = center
                )
            }
        }

        content()
    }
}