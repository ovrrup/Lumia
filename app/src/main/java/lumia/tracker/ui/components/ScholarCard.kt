package lumia.tracker.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import lumia.tracker.ui.theme.bouncyClick

/**
 * ScholarCard - Lumia's standard modern Material 3 / iOS Inset Grouped card container.
 * Features tactile spring animations, subtle border stroke, animateContentSize, and customizable rounding.
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
        width = 0.75.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    )
    
    val cardModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .bouncyClick(onClick = onClick)
    } else {
        modifier.clip(shape)
    }

    Surface(
        modifier = cardModifier,
        shape = shape,
        color = targetColor,
        border = targetBorder,
        shadowElevation = 0.5.dp,
        tonalElevation = 1.dp
    ) {
        Box(
            modifier = Modifier.animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
            content = content
        )
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
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    )
    
    val cardModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .bouncyClick(onClick = onClick)
    } else {
        modifier.clip(shape)
    }

    Surface(
        modifier = cardModifier,
        shape = shape,
        color = targetColor,
        border = targetBorder,
        shadowElevation = 1.5.dp,
        tonalElevation = 3.dp
    ) {
        Box(
            modifier = Modifier.animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
            content = content
        )
    }
}
