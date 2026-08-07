package lumia.tracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import lumia.tracker.ui.theme.LocalDarkTheme
import lumia.tracker.ui.theme.LocalPureBlackMode
import lumia.tracker.ui.theme.liquidGlass

@Composable
fun TranslucentMeshGradientBox(
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    content: @Composable BoxScope.() -> Unit
) {
    val isPureBlack = LocalPureBlackMode.current
    val isDark = LocalDarkTheme.current

    val transition = rememberInfiniteTransition(label = "MeshPillAnimation")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val c1 = MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.28f else 0.38f)
    val c2 = MaterialTheme.colorScheme.secondary.copy(alpha = if (isDark) 0.22f else 0.32f)
    val c3 = MaterialTheme.colorScheme.tertiary.copy(alpha = if (isDark) 0.25f else 0.35f)
    val baseSurface = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = if (isDark) 0.50f else 0.60f)

    Box(
        modifier = modifier
            .clip(shape)
            .liquidGlass(shape = shape, tintAlpha = 0.12f, opacityOverride = 0.65f)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.35f)
                    )
                ),
                shape = shape
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            drawRect(color = baseSurface)

            if (!isPureBlack) {
                val cx1 = width * 0.3f + (width * 0.3f) * kotlin.math.cos(phase)
                val cy1 = height * 0.5f + (height * 0.4f) * kotlin.math.sin(phase * 0.8f)

                val cx2 = width * 0.7f - (width * 0.3f) * kotlin.math.cos(phase * 0.7f)
                val cy2 = height * 0.5f - (height * 0.4f) * kotlin.math.sin(phase)

                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(c1, c2.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(cx1, cy1),
                        radius = width * 0.75f
                    )
                )

                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(c3, c1.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(cx2, cy2),
                        radius = width * 0.75f
                    )
                )
            }
        }
        content()
    }
}
