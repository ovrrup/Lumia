package lumia.tracker.ui.screens.focus

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.theme.bouncyClick

/**
 * PomodoroZenOverlay - Pure OLED black immersion overlay for deep focus sessions.
 * Features 100% #000000 true black background, minimalist timer arc display,
 * tap-to-toggle HUD, and deliberate touch-and-hold to exit.
 */
@ValueScore(
    score = 92,
    importance = Importance.CRITICAL,
    description = "Minimalist OLED Pure Black immersion overlay with touch-and-hold exit bar",
    category = "Focus"
)
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
    val coroutineScope = rememberCoroutineScope()
    var showControls by remember { mutableStateOf(true) }

    // Screen Keep Awake while Zen Mode is active
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Touch-and-Hold Progress Animatable
    val holdProgress = remember { Animatable(0f) }

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
        // Center Countdown Arc (Crisp & Clean)
        PomodoroTimerArc(
            timeLeftSeconds = timeLeftSeconds,
            originalTimeSeconds = originalTimeSeconds,
            statusLabel = statusLabel,
            ringColor = ringColor,
            isRunning = isRunning,
            isPaused = isPaused
        )

        // Top Navigation: Minimal Close Button
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { -it / 2 },
            exit = fadeOut() + slideOutVertically { -it / 2 },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.12f)
            ) {
                BouncyIconButton(
                    onClick = onClose,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Exit Zen Mode",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Bottom HUD: Pause/Resume + Touch-and-Hold to Exit Bar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Secondary Pause/Resume Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.bouncyClick(onClick = onPauseResume)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isPaused) "Resume" else "Pause",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Touch-and-Hold to Exit Interactive Progress Bar
                HoldToExitBar(
                    progress = holdProgress.value,
                    ringColor = ringColor,
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .height(44.dp)
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                down.consume()
                                val holdJob = coroutineScope.launch {
                                    holdProgress.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
                                    )
                                    if (holdProgress.value >= 0.99f) {
                                        onClose()
                                    }
                                }
                                val upOrCancel = waitForUpOrCancellation()
                                if (upOrCancel == null || holdProgress.value < 0.99f) {
                                    holdJob.cancel()
                                    coroutineScope.launch {
                                        holdProgress.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioLowBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                    }
                                }
                            }
                        }
                )

                Text(
                    text = "Tap screen to toggle controls",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.35f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * HoldToExitBar - Clean progress bar filling up as the user holds down.
 */
@ValueScore(
    score = 78,
    importance = Importance.HIGH,
    description = "Touch-and-hold progress bar for deliberate Zen mode exit",
    category = "Focus"
)
@Composable
private fun HoldToExitBar(
    progress: Float,
    ringColor: Color,
    modifier: Modifier = Modifier
) {
    val isHolding = progress > 0.05f

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(
            1.dp,
            if (isHolding) ringColor.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.15f)
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            // Smooth Progress Fill Layer
            if (progress > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(ringColor.copy(alpha = 0.35f))
                )
            }

            // Central Informational Label
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        tint = if (isHolding) Color.White else Color.White.copy(alpha = 0.70f),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (isHolding) "Hold to Exit..." else "Hold to Exit",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isHolding) Color.White else Color.White.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

