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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.theme.bouncyClick

/**
 * ScholarCardDefaults - Standard design tokens for Lumia's card system.
 * Provides unified shape, elevation, and border definitions to eliminate boilerplate.
 */
object ScholarCardDefaults {
    val shape: Shape = RoundedCornerShape(20.dp)
    val heroShape: Shape = RoundedCornerShape(24.dp)
    val compactShape: Shape = RoundedCornerShape(16.dp)
    val shadowElevation: Dp = 0.5.dp
    val tonalElevation: Dp = 1.dp
    val heroShadowElevation: Dp = 1.5.dp
    val heroTonalElevation: Dp = 3.dp

    @Composable
    fun border(
        color: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        width: Dp = 0.75.dp
    ): BorderStroke = BorderStroke(width = width, color = color)

    @Composable
    fun heroBorder(
        color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
        width: Dp = 0.8.dp
    ): BorderStroke = BorderStroke(width = width, color = color)
}

/**
 * ScholarCard - Lumia's standard modern Material 3 / iOS Inset Grouped card container.
 * Features tactile spring animations, subtle border stroke, animateContentSize, and customizable rounding.
 */
@ValueScore(
    score = 92,
    importance = Importance.CRITICAL,
    description = "Standard modern Material 3 / iOS Inset Grouped card container with spring bounce and content animation",
    category = "Container"
)
@Composable
fun ScholarCard(
    modifier: Modifier = Modifier,
    shape: Shape = ScholarCardDefaults.shape,
    containerColor: Color? = null,
    border: BorderStroke? = null,
    shadowElevation: Dp = ScholarCardDefaults.shadowElevation,
    tonalElevation: Dp = ScholarCardDefaults.tonalElevation,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val targetColor = containerColor ?: MaterialTheme.colorScheme.surface
    val targetBorder = border ?: ScholarCardDefaults.border()
    
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
        shadowElevation = shadowElevation,
        tonalElevation = tonalElevation
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
@ValueScore(
    score = 85,
    importance = Importance.HIGH,
    description = "High-emphasis hero card for dashboard banners, streak highlights, and key interactive statistics",
    category = "Container"
)
@Composable
fun ScholarHeroCard(
    modifier: Modifier = Modifier,
    shape: Shape = ScholarCardDefaults.heroShape,
    containerColor: Color? = null,
    border: BorderStroke? = null,
    shadowElevation: Dp = ScholarCardDefaults.heroShadowElevation,
    tonalElevation: Dp = ScholarCardDefaults.heroTonalElevation,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val targetColor = containerColor ?: MaterialTheme.colorScheme.primaryContainer
    val targetBorder = border ?: ScholarCardDefaults.heroBorder()
    
    ScholarCard(
        modifier = modifier,
        shape = shape,
        containerColor = targetColor,
        border = targetBorder,
        shadowElevation = shadowElevation,
        tonalElevation = tonalElevation,
        onClick = onClick,
        content = content
    )
}
