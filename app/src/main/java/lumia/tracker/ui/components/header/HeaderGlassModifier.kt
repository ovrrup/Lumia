package lumia.tracker.ui.components.header

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import lumia.tracker.ui.theme.liquidGlass

@Composable
fun Modifier.glassHeaderCapsule(
    useGlass: Boolean,
    shape: Shape = RoundedCornerShape(32.dp)
): Modifier = composed {
    this
        .liquidGlass(shape = shape, tintAlpha = 0.15f, opacityOverride = 0.60f)
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.40f),
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.20f),
                    Color.White.copy(alpha = 0.30f)
                )
            ),
            shape = shape
        )
}
