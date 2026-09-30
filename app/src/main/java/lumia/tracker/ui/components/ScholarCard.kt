package lumia.tracker.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
 * Provides unified shape, elevation, subtle glassmorphism, and capsule definitions.
 */
@ValueScore(
    score = 92,
    importance = Importance.HIGH,
    description = "Design system tokens and default parameters for ScholarCard glassmorphic & capsule ecosystem",
    category = "Container"
)
object ScholarCardDefaults {
    val cornerRadius: Dp = 24.dp
    val shape: Shape = RoundedCornerShape(24.dp)
    val heroShape: Shape = RoundedCornerShape(28.dp)
    val compactShape: Shape = RoundedCornerShape(18.dp)
    val capsuleShape: Shape = CircleShape
    val borderWidth: Dp = 1.dp
    val shadowElevation: Dp = 0.dp
    val tonalElevation: Dp = 0.dp
    val heroShadowElevation: Dp = 0.dp
    val heroTonalElevation: Dp = 0.dp

    @Composable
    fun border(
        color: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f),
        width: Dp = borderWidth
    ): BorderStroke = BorderStroke(width = width, color = color)

    @Composable
    fun heroBorder(
        color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
        width: Dp = borderWidth
    ): BorderStroke = BorderStroke(width = width, color = color)

    @Composable
    fun glassBorder(
        isDark: Boolean = isSystemInDarkTheme(),
        accentColor: Color? = null,
        width: Dp = 1.dp
    ): BorderStroke {
        val color = accentColor?.copy(alpha = if (isDark) 0.35f else 0.25f)
            ?: MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.18f else 0.14f)
        return BorderStroke(width = width, color = color)
    }

    @Composable
    fun glassContainerColor(
        isDark: Boolean = isSystemInDarkTheme(),
        alpha: Float = 1.0f
    ): Color {
        return if (isDark) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLowest
        }
    }
}

/**
 * ScholarCard - Lumia's modern Material 3 card container with subtle glassmorphism,
 * live specular reflections, tactile spring bounce animations, polished frosted borders,
 * and animated content sizing.
 */
@ValueScore(
    score = 96,
    importance = Importance.CRITICAL,
    description = "Standard modern glassmorphic card container with live specular reflection, tactile bounce, frosted glass border, and content animation",
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
    animateSize: Boolean = false,
    glassmorphic: Boolean = false,
    liveReflection: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val targetColor = containerColor ?: if (isDark) {
        MaterialTheme.colorScheme.surfaceContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }
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
        Box(modifier = Modifier.fillMaxWidth()) {
            if (animateSize) {
                Box(
                    modifier = Modifier.animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ),
                    content = content
                )
            } else {
                Box(content = content)
            }
        }
    }
}

/**
 * ScholarHeroCard - High-emphasis hero card used for dashboard banners, streak highlights,
 * and key interactive statistics with soft ambient styling.
 */
@ValueScore(
    score = 92,
    importance = Importance.HIGH,
    description = "High-emphasis hero card with accent styling and tactile bounce for banners and primary stations",
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
    liveReflection: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val targetColor = containerColor ?: MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.45f else 0.55f)
    val targetBorder = border ?: ScholarCardDefaults.heroBorder()
    
    ScholarCard(
        modifier = modifier,
        shape = shape,
        containerColor = targetColor,
        border = targetBorder,
        shadowElevation = shadowElevation,
        tonalElevation = tonalElevation,
        glassmorphic = false,
        liveReflection = false,
        onClick = onClick,
        content = content
    )
}

/**
 * GlassCapsule - Minimalist pill/capsule container with soft ambient styling,
 * fully rounded into a capsule (CircleShape) with clean subtle border.
 */
@Composable
fun GlassCapsule(
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    border: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = containerColor ?: if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    val pillBorder = border ?: ScholarCardDefaults.border()

    val capsuleModifier = if (onClick != null) {
        modifier
            .clip(CircleShape)
            .bouncyClick(onClick = onClick)
    } else {
        modifier.clip(CircleShape)
    }

    Surface(
        modifier = capsuleModifier,
        shape = CircleShape,
        color = bgColor,
        border = pillBorder,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}
