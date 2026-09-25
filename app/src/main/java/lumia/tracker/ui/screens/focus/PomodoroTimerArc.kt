package lumia.tracker.ui.screens.focus

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.theme.bouncyClick
import java.util.Locale

/**
 * PomodoroTimerArc - Modernized circular countdown gauge.
 * Features sleek rounded stroke caps, subtle glowing blur auras, clean minimalist
 * typography, capsule status pill, and tactile on-the-fly duration nudges.
 */
@ValueScore(
    score = 98,
    importance = Importance.CRITICAL,
    description = "Modernized circular timer arc with rounded stroke caps, glowing blur aura, clean minimalist typography, and capsule duration nudges",
    category = "Focus"
)
@Composable
fun PomodoroTimerArc(
    timeLeftSeconds: Int,
    originalTimeSeconds: Int,
    statusLabel: String,
    ringColor: Color,
    modifier: Modifier = Modifier,
    sessionsCompleted: Int = 0,
    periodSessions: Int = 4,
    isRunning: Boolean = false,
    isPaused: Boolean = false,
    onAdjustTime: ((Int) -> Unit)? = null
) {
    val totalTime = if (originalTimeSeconds > 0) originalTimeSeconds.toFloat() else (25 * 60f)
    val progressFraction = (timeLeftSeconds.toFloat() / totalTime).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing),
        label = "pomodoro_arc_progress"
    )

    // Breathing glow aura pulse when running
    val infiniteTransition = rememberInfiniteTransition(label = "pomodoro_arc_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = if (isRunning && !isPaused) 0.28f else 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pomodoro_glow_pulse"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Circular Countdown Gauge (260dp Canvas)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(260.dp)
        ) {
            // 1. Subtle Ambient Glowing Blur Aura
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .blur(radius = 42.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                    .background(
                        color = ringColor.copy(alpha = glowAlpha),
                        shape = CircleShape
                    )
            )

            // 2. High-Precision Arc Gauge with Rounded Stroke Caps
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                val strokeWidthPx = 9.dp.toPx()
                val arcSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)
                val topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)
                val radius = (size.minDimension - strokeWidthPx) / 2f
                val sweepAngleDeg = animatedProgress * 360f

                // Sleek Track Ring
                drawCircle(
                    color = ringColor.copy(alpha = 0.10f),
                    radius = radius,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )

                // Subtle Glowing Arc Halo
                if (animatedProgress > 0.005f) {
                    drawArc(
                        color = ringColor.copy(alpha = if (isRunning && !isPaused) 0.22f else 0.10f),
                        startAngle = -90f,
                        sweepAngle = sweepAngleDeg,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx * 1.8f, cap = StrokeCap.Round)
                    )

                    // Crisp Foreground Progress Arc with Sleek Rounded Stroke Cap
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngleDeg,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )
                }
            }

            // Central Minimalist Information Display
            val mins = timeLeftSeconds / 60
            val secs = timeLeftSeconds % 60
            val timeString = String.format(Locale.US, "%02d:%02d", mins, secs)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Minimalist Countdown Typography (clean tabular monospace digits)
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-1.5).sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Modern Capsule Status Badge
                Surface(
                    shape = CircleShape,
                    color = ringColor.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, ringColor.copy(alpha = 0.20f)),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isRunning && !isPaused) ringColor else ringColor.copy(alpha = 0.45f))
                        )
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.4.sp,
                            color = ringColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cycle Session Progress Dots with Capsule Active Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentInCycle = (sessionsCompleted % periodSessions) + 1
                    for (i in 1..periodSessions) {
                        val isFilled = i <= currentInCycle
                        val isActive = isFilled && isRunning && !isPaused && i == currentInCycle
                        Box(
                            modifier = Modifier
                                .size(
                                    width = if (isActive) 12.dp else 6.dp,
                                    height = 6.dp
                                )
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) ringColor else ringColor.copy(alpha = 0.20f)
                                )
                        )
                    }
                }
            }
        }

        // Tactile On-The-Fly Minute Nudges
        if (onAdjustTime != null) {
            PomodoroDurationNudgeRow(onAdjustTime = onAdjustTime)
        }
    }
}

/**
 * PomodoroDurationNudgeRow - Reusable row for quick on-the-fly time adjustments.
 */
@ValueScore(
    score = 80,
    importance = Importance.HIGH,
    description = "Clean reusable tactile row for quick minute adjustments during active sessions",
    category = "Focus"
)
@Composable
fun PomodoroDurationNudgeRow(
    onAdjustTime: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickNudgePill(label = "-5m", onClick = { onAdjustTime(-300) })
        QuickNudgePill(label = "-1m", onClick = { onAdjustTime(-60) })
        QuickNudgePill(label = "+1m", onClick = { onAdjustTime(60) })
        QuickNudgePill(label = "+5m", onClick = { onAdjustTime(300) })
    }
}

/**
 * QuickNudgePill - Clean tactile minute adjustment capsule chip.
 */
@ValueScore(
    score = 70,
    importance = Importance.MEDIUM,
    description = "Clean tactile minute adjustment capsule pill with subtle border",
    category = "Focus"
)
@Composable
fun QuickNudgePill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)),
        modifier = modifier.bouncyClick(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
