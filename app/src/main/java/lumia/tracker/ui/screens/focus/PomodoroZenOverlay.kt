package lumia.tracker.ui.screens.focus

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.theme.bouncyClick

/**
 * PomodoroZenOverlay - Pure OLED black AMOLED immersion overlay for deep focus sessions.
 * Features 100% #000000 true black background, ambient breathing pulse glow, minimal non-distracting typography,
 * tap-to-toggle HUD, and instant tap-to-exit.
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
    val context = LocalContext.current
    var showControls by remember { mutableStateOf(true) }

    // Screen Keep Awake while Zen Mode is active
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Breathing pulse animation for active timer
    val infiniteTransition = rememberInfiniteTransition(label = "zen_breathing_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zen_scale"
    )
    val pulseGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zen_glow_alpha"
    )

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
        // Subtle Breathing Glow Aura behind the center gauge
        Box(
            modifier = Modifier
                .size(320.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(ringColor.copy(alpha = pulseGlowAlpha))
        )

        // Center Countdown Arc
        Box(modifier = Modifier.scale(if (isRunning && !isPaused) pulseScale else 1.0f)) {
            PomodoroTimerArc(
                timeLeftSeconds = timeLeftSeconds,
                originalTimeSeconds = originalTimeSeconds,
                statusLabel = statusLabel,
                ringColor = ringColor
            )
        }

        // Top Navigation Bar: Minimalist Close Button
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { -it / 2 },
            exit = fadeOut() + slideOutVertically { -it / 2 },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f))
            ) {
                BouncyIconButton(
                    onClick = onClose,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Exit Zen Mode",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Bottom Controls HUD: Frosted Pause/Resume Pill & Status Hint
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 36.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                    modifier = Modifier.bouncyClick(onClick = onPauseResume)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = if (isPaused) "Resume" else "Pause",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = "Tap screen to toggle controls • Tap × to exit",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.35f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
