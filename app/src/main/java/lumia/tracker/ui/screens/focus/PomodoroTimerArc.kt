package lumia.tracker.ui.screens.focus

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lumia.tracker.ui.theme.bouncyClick
import java.util.Locale

/**
 * PomodoroTimerArc - Premium circular countdown arc with sweeping gradient progress,
 * bold non-jittering monospace typography, active pulsing indicator, cycle progress dots,
 * and quick on-the-fly minute nudges (+5m, +1m, -1m, -5m).
 */
@Composable
fun PomodoroTimerArc(
    timeLeftSeconds: Int,
    originalTimeSeconds: Int,
    statusLabel: String,
    ringColor: Color,
    modifier: Modifier = Modifier,
    sessionsCompleted: Int = 0,
    periodSessions: Int = 4,
    onAdjustTime: ((Int) -> Unit)? = null
) {
    val totalTime = if (originalTimeSeconds > 0) originalTimeSeconds.toFloat() else (25 * 60f)
    val progressFraction = (timeLeftSeconds.toFloat() / totalTime).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "pomodoro_arc_progress"
    )

    // Pulse animation for active running state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Main Circular Countdown Gauge (280dp Canvas)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(280.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                val strokeWidthPx = 16.dp.toPx()
                val glowWidthPx = 22.dp.toPx()
                val arcSize = Size(size.width - glowWidthPx, size.height - glowWidthPx)
                val topLeft = Offset(glowWidthPx / 2, glowWidthPx / 2)

                // Background Track Ring
                drawCircle(
                    color = ringColor.copy(alpha = 0.10f),
                    radius = (size.minDimension - glowWidthPx) / 2,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )

                // Ambient Soft Glow Layer behind active arc
                if (animatedProgress > 0.01f) {
                    drawArc(
                        color = ringColor.copy(alpha = 0.20f),
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = glowWidthPx, cap = StrokeCap.Round)
                    )
                }

                // Sweeping Gradient Progress Arc
                if (animatedProgress > 0.005f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            0.0f to ringColor.copy(alpha = 0.70f),
                            0.5f to ringColor,
                            1.0f to ringColor.copy(alpha = 0.95f)
                        ),
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )
                }
            }

            // Central Information Display
            val mins = timeLeftSeconds / 60
            val secs = timeLeftSeconds % 60
            val timeString = String.format(Locale.US, "%02d:%02d", mins, secs)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large Bold Monospace Countdown Display
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1.5).sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Status Label Pill with Pulsing Live Dot
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ringColor.copy(alpha = 0.14f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ringColor.copy(alpha = pulseAlpha))
                        )
                        Text(
                            text = statusLabel.uppercase(Locale.US),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = ringColor,
                            letterSpacing = 1.2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cycle Session Progress Dots (e.g. 4 dots showing completed intervals)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentInCycle = (sessionsCompleted % periodSessions) + 1
                    for (i in 1..periodSessions) {
                        val isFilled = i <= currentInCycle
                        Box(
                            modifier = Modifier
                                .size(if (isFilled) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) ringColor else ringColor.copy(alpha = 0.22f)
                                )
                        )
                    }
                }
            }
        }

        // 2. Quick On-The-Fly Minute Nudges (-5m, -1m, +1m, +5m)
        if (onAdjustTime != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickNudgePill(label = "-5m", onClick = { onAdjustTime(-300) })
                QuickNudgePill(label = "-1m", onClick = { onAdjustTime(-60) })
                QuickNudgePill(label = "+1m", onClick = { onAdjustTime(60) })
                QuickNudgePill(label = "+5m", onClick = { onAdjustTime(300) })
            }
        }
    }
}

/**
 * QuickNudgePill - Tactile minute adjustment chip with subtle spring bounce.
 */
@Composable
private fun QuickNudgePill(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 0.5.dp,
        modifier = Modifier.bouncyClick(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
