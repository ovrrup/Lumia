package lumia.tracker.ui.screens.focus

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.cos
import kotlin.math.sin

/**
 * PomodoroZenOverlay - Pure OLED black AMOLED immersion overlay for deep focus sessions.
 * Features 100% #000000 true black background, subtle orbital clock simulation, ambient breathing pulse aura,
 * tap-to-toggle HUD, and touch-and-hold to exit with a smooth progress bar.
 */
@ValueScore(
    score = 88,
    importance = Importance.HIGH,
    description = "AMOLED Pure Black immersion overlay with subtle orbital clock and touch-and-hold exit progress bar",
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
        initialValue = 0.04f,
        targetValue = 0.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zen_glow_alpha"
    )

    // Orbital celestial particle continuous rotation (30s full revolution)
    val orbitalAngleDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbital_angle"
    )

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
        // 1. Subtle Orbital Clock Ring & Rotating Celestial Particle
        Canvas(
            modifier = Modifier
                .size(330.dp)
        ) {
            val trackRadius = (size.minDimension / 2f) - 6.dp.toPx()
            val orbitalRad = Math.toRadians((orbitalAngleDeg - 90.0)).toFloat()
            val particleX = center.x + trackRadius * cos(orbitalRad)
            val particleY = center.y + trackRadius * sin(orbitalRad)

            // Faint orbital orbit path
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = trackRadius,
                style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
            )

            // Orbital orbiting particle
            drawCircle(
                color = ringColor.copy(alpha = 0.30f),
                radius = 7.dp.toPx(),
                center = Offset(particleX, particleY)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = 2.5.dp.toPx(),
                center = Offset(particleX, particleY)
            )
        }

        // 2. Subtle Breathing Glow Aura behind the center gauge
        Box(
            modifier = Modifier
                .size(310.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(ringColor.copy(alpha = pulseGlowAlpha))
        )

        // 3. Center Countdown Arc
        Box(modifier = Modifier.scale(if (isRunning && !isPaused) pulseScale else 1.0f)) {
            PomodoroTimerArc(
                timeLeftSeconds = timeLeftSeconds,
                originalTimeSeconds = originalTimeSeconds,
                statusLabel = statusLabel,
                ringColor = ringColor,
                isRunning = isRunning,
                isPaused = isPaused
            )
        }

        // 4. Top Navigation Bar: Minimalist Fast Close Button
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

        // 5. Bottom HUD: Frosted Pause/Resume + Touch-and-Hold to Exit Bar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 28.dp, start = 24.dp, end = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Secondary Pause/Resume Pill
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
                    modifier = Modifier.bouncyClick(onClick = onPauseResume)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isPaused) "Resume" else "Pause",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Touch-and-Hold to Exit Interactive Progress Bar
                HoldToExitBar(
                    progress = holdProgress.value,
                    ringColor = ringColor,
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .height(48.dp)
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                down.consume()
                                val holdJob = coroutineScope.launch {
                                    holdProgress.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(durationMillis = 1200, easing = LinearEasing)
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
                    color = Color.White.copy(alpha = 0.30f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * HoldToExitBar - Interactive progress bar filling up as the user holds down.
 */
@ValueScore(
    score = 75,
    importance = Importance.HIGH,
    description = "Touch-and-hold progress bar for deliberate, non-accidental Zen mode exit",
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
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(
            1.dp,
            if (isHolding) ringColor.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.18f)
        ),
        modifier = modifier.scale(if (isHolding) 1.02f else 1.0f)
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
                        .clip(RoundedCornerShape(24.dp))
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        tint = if (isHolding) Color.White else Color.White.copy(alpha = 0.70f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isHolding) "Hold to Exit..." else "Hold to Exit Zen",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isHolding) Color.White else Color.White.copy(alpha = 0.75f),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
