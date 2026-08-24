package lumia.tracker.ui.screens.focus

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lumia.tracker.ui.components.BouncyIconButton
import java.text.SimpleDateFormat
import java.util.*

/**
 * PomodoroAodOverlay - Hardware-efficient, ultra-low power True Always-On Display mode.
 * Features 100% pure OLED black canvas, dynamic pixel burn-in shifting, minimal text luminescence,
 * live focus countdown, and instant tap-to-dismiss.
 */
@Composable
fun PomodoroAodOverlay(
    timeLeftSeconds: Int,
    sessionsCompleted: Int,
    modeString: String,
    isRunning: Boolean,
    isPaused: Boolean,
    onPauseResume: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    var currentTimeStr by remember { mutableStateOf(timeFormat.format(Date())) }

    // Tick clock every minute
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
    val formattedTime = String.format("%02d:%02d", m, s)

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
            // System Real Time
            Text(
                text = currentTimeStr,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 54.sp),
                fontWeight = FontWeight.ExtraLight,
                color = Color.White.copy(alpha = 0.35f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main Pomodoro Countdown Clock
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = 0.70f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mode & Cycle Pill
            Surface(
                color = Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${if (modeString == "WORK") "FOCUS" else modeString.replace("_", " ")} • SESSION #${sessionsCompleted + 1}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "True Always-On Active • Tap Screen to Resume",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.25f),
                textAlign = TextAlign.Center
            )
        }
    }
}
