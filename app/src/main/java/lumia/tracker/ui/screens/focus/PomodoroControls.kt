package lumia.tracker.ui.screens.focus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.theme.bouncyClick

/**
 * PomodoroControls - Primary tactile interaction suite for focus sessions.
 * Features start/pause/resume/skip/stop actions alongside ambient audio, fullscreen zen, and settings.
 */
@Composable
fun PomodoroControls(
    isRunning: Boolean,
    isPaused: Boolean,
    isAlarmActive: Boolean,
    onStart: () -> Unit,
    onPauseResume: () -> Unit,
    onSkip: () -> Unit,
    onStop: () -> Unit,
    onStopAlarm: () -> Unit,
    onOpenZenMode: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Alarm Dismiss Button (if alarm is firing)
        if (isAlarmActive) {
            Button(
                onClick = onStopAlarm,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Rounded.NotificationsOff, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dismiss Alarm", fontWeight = FontWeight.Bold)
            }
        }

        // 2. Primary Action Row (Start / Pause / Resume / Skip / Stop)
        if (!isRunning) {
            BouncyButton(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Focus Session", fontWeight = FontWeight.ExtraBold)
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause / Resume Main Button
                Button(
                    onClick = onPauseResume,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPaused) "Resume" else "Pause", fontWeight = FontWeight.Bold)
                }

                // Skip Button
                FilledTonalButton(
                    onClick = onSkip,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Skip Session")
                }

                // Stop Button
                FilledTonalButton(
                    onClick = onStop,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Rounded.Stop, contentDescription = "Stop Timer")
                }
            }
        }

        // 3. Secondary Utility Row: Zen Fullscreen Mode, Timer Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Zen Fullscreen Immersion Mode
            FilterChip(
                selected = false,
                onClick = onOpenZenMode,
                label = { Text("Zen Fullscreen", fontWeight = FontWeight.SemiBold) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Fullscreen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(16.dp)
            )

            // Timer Settings
            FilterChip(
                selected = false,
                onClick = onOpenSettings,
                label = { Text("Intervals & Targets", fontWeight = FontWeight.SemiBold) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
