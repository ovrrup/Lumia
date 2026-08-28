package lumia.tracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AmbientBackgroundCanvas(
    enabled: Boolean,
    lightBrightness: Float = 0.5f,
    darkBrightness: Float = 0.5f
) {
    if (!enabled) return

    val isDark = isSystemInDarkTheme()
    val brightness = if (isDark) darkBrightness else lightBrightness
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary

    val infiniteTransition = rememberInfiniteTransition(label = "ambient_bg_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient_phase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val x1 = width * (0.35f + 0.08f * sin(phase))
        val y1 = height * (0.25f + 0.06f * cos(phase))
        val r1 = width * 0.85f

        val x2 = width * (0.65f - 0.08f * cos(phase))
        val y2 = height * (0.75f - 0.06f * sin(phase))
        val r2 = width * 0.8f

        val alphaMultiplier = (brightness * (if (isDark) 0.05f else 0.035f)).coerceIn(0f, 0.12f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = alphaMultiplier), Color.Transparent),
                center = Offset(x1, y1),
                radius = r1
            ),
            center = Offset(x1, y1),
            radius = r1
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(secondaryColor.copy(alpha = alphaMultiplier * 0.7f), Color.Transparent),
                center = Offset(x2, y2),
                radius = r2
            ),
            center = Offset(x2, y2),
            radius = r2
        )
    }
}
