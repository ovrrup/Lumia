package lumia.tracker.ui.screens.focus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lumia.tracker.ui.components.BouncyIconButton
import java.util.Locale

/**
 * PomodoroZenOverlay - Pure OLED black immersion overlay for hyper-focused study sessions.
 * Displays only the essential glowing countdown timer and minimizes all visual distractions.
 */
@Composable
fun PomodoroZenOverlay(
    timeLeftSeconds: Int,
    originalTimeSeconds: Int,
    statusLabel: String,
    ringColor: Color,
    isRunning: Boolean,
    isPaused: Boolean,
    onPauseResume: () -> Unit,
    onClose: () -> Unit
) {
    var showControls by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { showControls = !showControls }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Center Focus Gauge
        PomodoroTimerArc(
            timeLeftSeconds = timeLeftSeconds,
            originalTimeSeconds = originalTimeSeconds,
            statusLabel = statusLabel,
            ringColor = ringColor
        )

        // Top-right Close Button
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            BouncyIconButton(
                onClick = onClose
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Exit Zen Mode",
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Bottom Quick Controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp)
        ) {
            FilledTonalButton(
                onClick = onPauseResume,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isPaused) "Resume" else "Pause", fontWeight = FontWeight.Bold)
            }
        }
    }
}
