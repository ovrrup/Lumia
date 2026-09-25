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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.theme.bouncyClick

/**
 * PomodoroZenOverlay - Pure OLED black immersion overlay for deep focus sessions.
 * Features 100% #000000 true black background, ambient breathing glow,
 * minimalist timer arc, capsule control chips (CircleShape), and glassmorphic quick-quit pills.
 */
@ValueScore(
    score = 96,
    importance = Importance.CRITICAL,
    description = "Ultra-minimalist OLED immersion overlay with subtle ambient breathing glow, capsule controls, and glassmorphic quick-quit pills",
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

    // Subtle Ambient Breathing Glow Animation (meditative organic cycle)
    val infiniteTransition = rememberInfiniteTransition(label = "zen_ambient_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.04f,
        targetValue = 0.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zen_glow_alpha"
    )
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zen_glow_scale"
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
        // Subtle Ambient Breathing Glow Behind Timer
        Canvas(
            modifier = Modifier.size(360.dp)
        ) {
            val activeAlpha = if (isRunning && !isPaused) glowAlpha else 0.05f
            val activeScale = if (isRunning && !isPaused) glowScale else 0.95f
            val radius = (size.minDimension / 2f) * activeScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ringColor.copy(alpha = activeAlpha),
                        ringColor.copy(alpha = activeAlpha * 0.35f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }

        // Center Countdown Arc (Crisp & Clean)
        PomodoroTimerArc(
            timeLeftSeconds = timeLeftSeconds,
            originalTimeSeconds = originalTimeSeconds,
            statusLabel = statusLabel,
            ringColor = ringColor,
            isRunning = isRunning,
            isPaused = isPaused
        )

        // Top Navigation: Glassmorphic Quick-Quit Pill
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(220)) { -it / 2 },
            exit = fadeOut(animationSpec = tween(180)) + slideOutVertically(animationSpec = tween(180)) { -it / 2 },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.08f),
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    )
                ),
                modifier = Modifier
                    .clip(CircleShape)
                    .bouncyClick(onClick = onClose)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Exit Zen Mode",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Exit",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.85f),
                        letterSpacing = 0.2.sp
                    )
                }
            }
        }

        // Bottom Controls: Capsule Control Chips (CircleShape) & Glassmorphic Quick-Quit Pills
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(220)) { it / 2 },
            exit = fadeOut(animationSpec = tween(180)) + slideOutVertically(animationSpec = tween(180)) { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Capsule Control Chip: Pause / Resume (CircleShape)
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.10f),
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.22f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        )
                    ),
                    modifier = Modifier
                        .clip(CircleShape)
                        .bouncyClick(onClick = onPauseResume)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = if (isPaused) "Resume" else "Pause",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            letterSpacing = 0.2.sp
                        )
                    }
                }

                // Interactive Glassmorphic Quick-Quit Hold Pill (CircleShape)
                HoldToExitPill(
                    progress = holdProgress.value,
                    ringColor = ringColor,
                    modifier = Modifier
                        .fillMaxWidth(0.70f)
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
            }
        }
    }
}

/**
 * HoldToExitPill - Glassmorphic quick-quit capsule pill filling up as the user holds down.
 */
@ValueScore(
    score = 82,
    importance = Importance.HIGH,
    description = "Glassmorphic capsule quick-quit pill with touch-and-hold progress fill",
    category = "Focus"
)
@Composable
private fun HoldToExitPill(
    progress: Float,
    ringColor: Color,
    modifier: Modifier = Modifier
) {
    val isHolding = progress > 0.05f

    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.07f),
        border = BorderStroke(
            width = 1.dp,
            brush = if (isHolding) {
                Brush.verticalGradient(
                    listOf(
                        ringColor.copy(alpha = 0.70f),
                        ringColor.copy(alpha = 0.30f)
                    )
                )
            } else {
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.20f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            }
        ),
        modifier = modifier.clip(CircleShape)
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
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    ringColor.copy(alpha = 0.30f),
                                    ringColor.copy(alpha = 0.55f)
                                )
                            )
                        )
                )
            }

            // Central Minimal Informational Label
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
                        tint = if (isHolding) Color.White else Color.White.copy(alpha = 0.65f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isHolding) "Release to cancel" else "Hold to exit",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isHolding) Color.White else Color.White.copy(alpha = 0.70f),
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}


