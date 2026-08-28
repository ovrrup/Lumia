package lumia.tracker.ui.screens.focus

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
 * PomodoroTimerArc - Crisp, minimal, and modern circular countdown arc.
 * Features a clean single-layer progress stroke, tabular monospace countdown typography,
 * status badge, cycle progress dots, and tactile on-the-fly duration nudges.
 */
@ValueScore(
    score = 95,
    importance = Importance.CRITICAL,
    description = "Crisp, minimal timer arc with clean single-stroke gauge, tabular countdown typography, and duration nudges",
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
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                val strokeWidthPx = 8.dp.toPx()
                val arcSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)
                val topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)
                val radius = (size.minDimension - strokeWidthPx) / 2f
                val sweepAngleDeg = animatedProgress * 360f

                // 1. Crisp Track Ring
                drawCircle(
                    color = ringColor.copy(alpha = 0.12f),
                    radius = radius,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )

                // 2. Crisp Clean Progress Arc
                if (animatedProgress > 0.005f) {
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

            // Central Information Display
            val mins = timeLeftSeconds / 60
            val secs = timeLeftSeconds % 60
            val timeString = String.format(Locale.US, "%02d:%02d", mins, secs)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large Monospace Countdown Display (tabular numbers to prevent jitter)
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Clean Status Label Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ringColor.copy(alpha = 0.10f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isRunning && !isPaused) ringColor else ringColor.copy(alpha = 0.5f))
                        )
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ringColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cycle Session Progress Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentInCycle = (sessionsCompleted % periodSessions) + 1
                    for (i in 1..periodSessions) {
                        val isFilled = i <= currentInCycle
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) ringColor else ringColor.copy(alpha = 0.20f)
                                )
                        )
                    }
                }
            }
        }

        // Tactile On-The-Fly Minute Nudges (-5m, -1m, +1m, +5m)
        if (onAdjustTime != null) {
            PomodoroDurationNudgeRow(onAdjustTime = onAdjustTime)
        }
    }
}

/**
 * PomodoroDurationNudgeRow - Reusable row for quick on-the-fly time adjustments.
 */
@ValueScore(
    score = 78,
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
 * QuickNudgePill - Clean tactile minute adjustment chip.
 */
@ValueScore(
    score = 65,
    importance = Importance.MEDIUM,
    description = "Clean minute nudge pill for quick on-the-fly timer adjustments",
    category = "Focus"
)
@Composable
fun QuickNudgePill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.bouncyClick(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
