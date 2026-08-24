package lumia.tracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import lumia.tracker.ui.theme.bouncyClick

/**
 * ScholarCard - Lumia's standard modern Material 3 card container.
 * Features tactile spring animations, subtle tonal elevation, and customizable rounding.
 */
@Composable
fun ScholarCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    containerColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val targetColor = containerColor ?: MaterialTheme.colorScheme.surfaceContainer
    if (onClick != null) {
        Surface(
            modifier = modifier.bouncyClick(onClick = onClick),
            shape = shape,
            color = targetColor,
            tonalElevation = if (containerColor == null) 2.dp else 0.dp
        ) {
            Box(content = content)
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = targetColor,
            tonalElevation = if (containerColor == null) 2.dp else 0.dp
        ) {
            Box(content = content)
        }
    }
}

/**
 * ScholarHeroCard - High-emphasis hero card used for dashboard banners, streak highlights,
 * and key interactive statistics.
 */
@Composable
fun ScholarHeroCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(32.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    if (onClick != null) {
        Surface(
            modifier = modifier.bouncyClick(onClick = onClick),
            shape = shape,
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 0.dp
        ) {
            Box(content = content)
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 0.dp
        ) {
            Box(content = content)
        }
    }
}
