package lumia.tracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import lumia.tracker.ui.theme.LocalDarkTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AmbientBackgroundCanvas(
    enabled: Boolean,
    lightBrightness: Float,
    darkBrightness: Float
) {
    if (!enabled) return

    val infiniteTransition = rememberInfiniteTransition(label = "bg_transition")
    val bgPhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(28000, easing = LinearEasing), RepeatMode.Restart),
        label = "bg_phase"
    )
    val colorScheme = MaterialTheme.colorScheme
    val isDark = LocalDarkTheme.current

    Canvas(modifier = Modifier.fillMaxSize()) {
        val colors = listOf(colorScheme.primaryContainer, colorScheme.secondaryContainer, colorScheme.tertiaryContainer)
        val alphaScale = if (isDark) darkBrightness else lightBrightness

        val cx1 = size.width * (0.28f + 0.14f * cos(bgPhase.toDouble()).toFloat())
        val cy1 = size.height * (0.20f + 0.10f * sin(bgPhase.toDouble()).toFloat())

        val cx2 = size.width * (0.78f - 0.12f * cos(bgPhase.toDouble() * 0.8).toFloat())
        val cy2 = size.height * (0.62f - 0.10f * sin(bgPhase.toDouble() * 0.8).toFloat())

        val cx3 = size.width * (0.85f - 0.14f * sin(bgPhase.toDouble() * 0.6).toFloat())
        val cy3 = size.height * (0.32f + 0.08f * cos(bgPhase.toDouble() * 0.6).toFloat())

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(colors[0].copy(alpha = alphaScale.coerceIn(0f, 1f) * 1.0f), colors[0].copy(alpha = alphaScale.coerceIn(0f, 1f) * 0.25f), Color.Transparent),
                center = Offset(cx1, cy1),
                radius = size.width * 1.6f
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(colors[2].copy(alpha = alphaScale.coerceIn(0f, 1f) * 0.85f), colors[2].copy(alpha = alphaScale.coerceIn(0f, 1f) * 0.15f), Color.Transparent),
                center = Offset(cx2, cy2),
                radius = size.width * 1.5f
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(colors[1].copy(alpha = alphaScale.coerceIn(0f, 1f) * 0.65f), Color.Transparent),
                center = Offset(cx3, cy3),
                radius = size.width * 1.3f
            )
        )
    }
}
