package lumia.tracker.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    tintColor: Color = Color.Unspecified,
    tintAlpha: Float = 1.0f,
    blurRadius: Float = 0f,
    isDark: Boolean = false,
    borderColor: Color = Color.Transparent,
    opacityOverride: Float? = null,
    backdropStyleOverride: String? = null
): Modifier = composed {
    val containerColor = if (tintColor != Color.Unspecified) tintColor else MaterialTheme.colorScheme.surfaceContainer
    this
        .clip(shape)
        .background(color = containerColor, shape = shape)
}

fun Modifier.glassCard(shape: Shape = RoundedCornerShape(24.dp)): Modifier = composed {
    this
        .clip(shape)
        .background(color = MaterialTheme.colorScheme.surfaceContainer, shape = shape)
}

fun Modifier.glassHero(shape: Shape = RoundedCornerShape(24.dp)): Modifier = composed {
    this
        .clip(shape)
        .background(color = MaterialTheme.colorScheme.primaryContainer, shape = shape)
}

fun Modifier.glassBar(shape: Shape = RoundedCornerShape(0.dp)): Modifier = composed {
    this
        .clip(shape)
        .background(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = shape)
}

fun Modifier.navGlassBar(shape: Shape = RoundedCornerShape(0.dp)): Modifier = composed {
    this
        .clip(shape)
        .background(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = shape)
}

fun Modifier.glassPill(shape: Shape = RoundedCornerShape(50.dp)): Modifier = composed {
    this
        .clip(shape)
        .background(color = MaterialTheme.colorScheme.secondaryContainer, shape = shape)
}
