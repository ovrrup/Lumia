package lumia.tracker.ui.components

import androidx.compose.foundation.BorderStroke
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
 * ScholarCard - Lumia's standard modern iOS Inset Grouped card container.
 * Features tactile spring animations, subtle border stroke, and customizable rounding.
 */
@Composable
fun ScholarCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    containerColor: Color? = null,
    border: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val targetColor = containerColor ?: MaterialTheme.colorScheme.surface
    val targetBorder = border ?: BorderStroke(
        width = 0.6.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
    
    if (onClick != null) {
        Surface(
            modifier = modifier.bouncyClick(onClick = onClick),
            shape = shape,
            color = targetColor,
            border = targetBorder,
            shadowElevation = 0.5.dp
        ) {
            Box(content = content)
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = targetColor,
            border = targetBorder,
            shadowElevation = 0.5.dp
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
    shape: Shape = RoundedCornerShape(24.dp),
    containerColor: Color? = null,
    border: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val targetColor = containerColor ?: MaterialTheme.colorScheme.primaryContainer
    val targetBorder = border ?: BorderStroke(
        width = 0.8.dp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    )
    
    if (onClick != null) {
        Surface(
            modifier = modifier.bouncyClick(onClick = onClick),
            shape = shape,
            color = targetColor,
            border = targetBorder,
            shadowElevation = 1.dp
        ) {
            Box(content = content)
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = targetColor,
            border = targetBorder,
            shadowElevation = 1.dp
        ) {
            Box(content = content)
        }
    }
}
