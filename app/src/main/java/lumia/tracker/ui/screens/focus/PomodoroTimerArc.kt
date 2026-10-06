package lumia.tracker.ui.screens.focus

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import lumia.tracker.ui.components.ScholarCardDefaults
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.theme.bouncyClick
import java.util.Locale

/**
 * PomodoroTimerArc - Reimagined soft ambient circular countdown gauge.
 * Features an organic curved gauge, clean tabular time typography,
 * pill status badge, session round capsule indicators, and tactile minute nudges.
 */
@ValueScore(
    score = 98,
    importance = Importance.CRITICAL,
    description = "Reimagined soft ambient countdown gauge with rounded progress arc, serene typography, and session round indicators",
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
        // Main Circular Countdown Gauge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(268.dp)
        ) {
            // High-Precision Soft Progress Arc with Rounded Stroke Caps
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                val strokeWidthPx = 10.dp.toPx()
                val diameter = size.minDimension - strokeWidthPx
                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                val arcSize = Size(diameter, diameter)
                val radius = diameter / 2f
                val sweepAngleDeg = animatedProgress * 360f

                // Track Ring
                drawCircle(
                    color = ringColor.copy(alpha = 0.12f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )

                // Foreground Progress Arc
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
            val hours = mins / 60
            val displayMins = if (hours > 0) mins % 60 else mins
            val timeString = if (hours > 0) {
                String.format(Locale.US, "%d:%02d:%02d", hours, displayMins, secs)
            } else {
                String.format(Locale.US, "%02d:%02d", mins, secs)
            }
            val digitFontSize = if (hours > 0) 38.sp else 50.sp

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Countdown Digits
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = digitFontSize,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-1).sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Modern Capsule Status Badge
                Surface(
                    shape = CircleShape,
                    color = ringColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, ringColor.copy(alpha = 0.25f)),
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
                            letterSpacing = 0.3.sp,
                            color = ringColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cycle Session Progress Capsules
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val safePeriodSessions = maxOf(1, periodSessions)
                    val completedInCycle = if (sessionsCompleted > 0 && sessionsCompleted % safePeriodSessions == 0) {
                        safePeriodSessions
                    } else {
                        sessionsCompleted % safePeriodSessions
                    }
                    for (i in 1..safePeriodSessions) {
                        val isFilled = i <= completedInCycle
                        val isActive = isRunning && !isPaused && (i == completedInCycle + 1)
                        Box(
                            modifier = Modifier
                                .size(
                                    width = if (isActive) 14.dp else 7.dp,
                                    height = 7.dp
                                )
                                .clip(CircleShape)
                                .background(
                                    if (isFilled || isActive) ringColor else ringColor.copy(alpha = 0.20f)
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
 * PomodoroDurationNudgeRow - Quick on-the-fly time adjustments with tactile capsule pills.
 */
@ValueScore(
    score = 80,
    importance = Importance.HIGH,
    description = "Clean tactile row for quick minute adjustments during active sessions",
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
    val isDark = isSystemInDarkTheme()
    val pillBg = if (isDark) MaterialTheme.colorScheme.surfaceContainerLow
                 else MaterialTheme.colorScheme.surfaceContainerLowest

    Surface(
        shape = CircleShape,
        color = pillBg,
        border = ScholarCardDefaults.border(),
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
