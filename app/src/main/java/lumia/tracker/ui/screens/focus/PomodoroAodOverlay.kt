package lumia.tracker.ui.screens.focus

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import java.text.SimpleDateFormat
import java.util.*

/**
 * PomodoroAodOverlay - Minimalist, ultra-low power True Always-On Display mode.
 * Optimized for pure OLED black efficiency with ultra-clean modern typography,
 * minimalist capsule progress indicators, anti-burn-in pixel shifting, and a decluttered layout.
 */
@ValueScore(
    score = 95,
    importance = Importance.HIGH,
    description = "Pure OLED black AOD overlay with modern typography, minimalist capsule progress indicators, and pixel shifting",
    category = "Focus"
)
@Composable
fun PomodoroAodOverlay(
    timeLeftSeconds: Int,
    sessionsCompleted: Int,
    modeString: String,
    isRunning: Boolean,
    isPaused: Boolean,
    onPauseResume: () -> Unit,
    onClose: () -> Unit,
    originalTimeSeconds: Int = 25 * 60
) {
    val context = LocalContext.current
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    var currentTimeStr by remember { mutableStateOf(timeFormat.format(Date())) }

    // Tick clock every 10 seconds
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeStr = timeFormat.format(Date())
            kotlinx.coroutines.delay(10000L)
        }
    }

    // Force ultra-low brightness and keep screen on while AOD is active
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        val originalBrightness = window?.attributes?.screenBrightness ?: -1f
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val lp = window?.attributes
        if (lp != null) {
            lp.screenBrightness = 0.01f
            window.attributes = lp
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (lp != null) {
                lp.screenBrightness = originalBrightness
                window.attributes = lp
            }
        }
    }

    // Dynamic Anti-Burn-In Pixel Shifter (shifts every 10 seconds)
    val burnInOffset = remember(timeLeftSeconds / 10) {
        val tick = ((timeLeftSeconds / 10) % 8)
        val x = when (tick) {
            0 -> 0.dp; 1 -> 4.dp; 2 -> 0.dp; 3 -> (-4).dp
            4 -> 2.dp; 5 -> (-2).dp; 6 -> 3.dp; else -> (-3).dp
        }
        val y = when (tick) {
            0 -> 0.dp; 1 -> (-4).dp; 2 -> 4.dp; 3 -> 0.dp
            4 -> (-2).dp; 5 -> 2.dp; 6 -> (-3).dp; else -> 3.dp
        }
        Pair(x, y)
    }

    val m = timeLeftSeconds / 60
    val s = timeLeftSeconds % 60
    val formattedTime = String.format(Locale.US, "%02d:%02d", m, s)
    val totalTime = if (originalTimeSeconds > 0) originalTimeSeconds.toFloat() else (25 * 60f)
    val progressFraction = (timeLeftSeconds.toFloat() / totalTime).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClose
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .offset(x = burnInOffset.first, y = burnInOffset.second)
        ) {
            // System Real Time (Clean modern tracking)
            Text(
                text = currentTimeStr,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 1.2.sp,
                    fontFeatureSettings = "tnum"
                ),
                color = Color.White.copy(alpha = 0.32f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Main Pomodoro Countdown Clock (Ultra-clean modern tabular typography)
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 76.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1.5).sp,
                    fontFeatureSettings = "tnum"
                ),
                color = Color.White.copy(alpha = 0.72f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Minimalist Capsule Progress Indicator (Session Countdown)
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progressFraction)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.40f))
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Decluttered Mode Capsule Chip (CircleShape)
            val modeLabel = if (modeString == "WORK") "Focus" else modeString.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
            val statusText = if (isPaused) "$modeLabel • Paused" else modeLabel
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPaused) Color.White.copy(alpha = 0.25f)
                                else Color.White.copy(alpha = 0.65f)
                            )
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = Color.White.copy(alpha = 0.45f),
                        letterSpacing = 0.4.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Minimalist Capsule Cycle Progress Indicators (4-session cycle segments)
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentInCycle = (sessionsCompleted % 4) + 1
                for (i in 1..4) {
                    val isCompleted = i < currentInCycle
                    val isCurrent = i == currentInCycle
                    val alpha = when {
                        isCompleted -> 0.45f
                        isCurrent -> if (isRunning && !isPaused) 0.65f else 0.30f
                        else -> 0.10f
                    }
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}


