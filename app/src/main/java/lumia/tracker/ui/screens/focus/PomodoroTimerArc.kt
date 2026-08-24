package lumia.tracker.ui.screens.focus

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lumia.tracker.ui.theme.bouncyClick
import java.util.Locale

/**
 * PomodoroTimerArc - Premium circular countdown arc with glowing progress sweep,
 * live session cycle progress dots, and on-the-fly nudge adjustment buttons.
 */
@Composable
fun PomodoroTimerArc(
    timeLeftSeconds: Int,
    originalTimeSeconds: Int,
    statusLabel: String,
    ringColor: Color,
    sessionsCompleted: Int = 0,
    periodSessions: Int = 4,
    onAdjustTime: ((Int) -> Unit)? = null
) {
    val totalTime = if (originalTimeSeconds > 0) originalTimeSeconds.toFloat() else (25 * 60f)
    val progressFraction = (timeLeftSeconds.toFloat() / totalTime).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "pomodoro_progress")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Circular Countdown Gauge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(260.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                // Background Track Circle
                drawCircle(
                    color = ringColor.copy(alpha = 0.12f),
                    style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                )

                // Outer Glowing Sweep Arc
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(
                            ringColor.copy(alpha = 0.8f),
                            ringColor,
                            ringColor.copy(alpha = 0.6f),
                            ringColor
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Central Information Column
            val mins = timeLeftSeconds / 60
            val secs = timeLeftSeconds % 60
            val timeString = String.format(Locale.US, "%02d:%02d", mins, secs)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Time Countdown Text
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 54.sp),
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-1).sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Status Label Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ringColor.copy(alpha = 0.15f),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = statusLabel.uppercase(Locale.US),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = ringColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cycle dots indicator (e.g. 4 dots showing current session in cycle)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                                    if (isFilled) ringColor else ringColor.copy(alpha = 0.25f)
                                )
                        )
                    }
                }
            }
        }

        // Quick On-The-Fly Time Nudge Adjusters (-5m, -1m, +1m, +5m)
        if (onAdjustTime != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NudgeButton(label = "-5m", onClick = { onAdjustTime(-300) })
                NudgeButton(label = "-1m", onClick = { onAdjustTime(-60) })
                NudgeButton(label = "+1m", onClick = { onAdjustTime(60) })
                NudgeButton(label = "+5m", onClick = { onAdjustTime(300) })
            }
        }
    }
}

/**
 * NudgeButton - Compact tactile pill for adjusting focus time without resetting the timer.
 */
@Composable
private fun NudgeButton(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .bouncyClick(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
