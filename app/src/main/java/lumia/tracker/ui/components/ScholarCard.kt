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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
        val topHighlight = accentColor?.copy(alpha = if (isDark) 0.30f else 0.40f)
            ?: (if (isDark) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.45f))
        val bottomShadow = accentColor?.copy(alpha = if (isDark) 0.05f else 0.08f)
            ?: MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.10f else 0.15f)
        val brush = Brush.linearGradient(
            colors = listOf(topHighlight, bottomShadow),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
        return BorderStroke(width = width, brush = brush)
    }

    @Composable
    fun glassContainerColor(
        isDark: Boolean = isSystemInDarkTheme(),
        alpha: Float = if (isDark) 0.70f else 0.80f
    ): Color {
        return if (isDark) {
            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = alpha)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = alpha)
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
    glassmorphic: Boolean = true,
    liveReflection: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val targetColor = containerColor ?: if (glassmorphic) {
        ScholarCardDefaults.glassContainerColor(isDark)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    val targetBorder = border ?: if (glassmorphic) {
        ScholarCardDefaults.glassBorder(isDark)
    } else {
        ScholarCardDefaults.border()
    }
    
    val cardModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .bouncyClick(onClick = onClick)
    } else {
        modifier.clip(shape)
    }

    // Live reflection slow ambient sweep
    val infiniteTransition = rememberInfiniteTransition(label = "card_reflection_anim")
    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_progress"
    )

    Surface(
        modifier = cardModifier,
        shape = shape,
        color = targetColor,
        border = targetBorder,
        shadowElevation = shadowElevation,
        tonalElevation = tonalElevation
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (glassmorphic) {
                // 1. Soft Ambient Specular Angle Reflection
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    if (isDark) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.18f),
                                    if (isDark) Color.White.copy(alpha = 0.01f) else Color.White.copy(alpha = 0.05f),
                                    Color.Transparent
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                )

                // 2. Delicate Refractive Top Rim Light
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    if (isDark) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.32f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // 3. Serene Animated Ambient Reflection Sweep
                if (liveReflection) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .drawWithCache {
                                val highlightColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.12f)
                                val brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        highlightColor,
                                        Color.Transparent
                                    ),
                                    start = Offset(size.width * (sweepProgress - 0.4f), 0f),
                                    end = Offset(size.width * (sweepProgress + 0.4f), size.height)
                                )
                                onDrawWithContent {
                                    drawContent()
                                    drawRect(brush = brush)
                                }
                            }
                    )
                }
            }

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
 * and key interactive statistics with live reflection sweeps and glassmorphic depth.
 */
@ValueScore(
    score = 92,
    importance = Importance.HIGH,
    description = "High-emphasis hero card with accent glassmorphism and live reflection sweep for banners and primary stations",
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
    liveReflection: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val targetColor = containerColor ?: MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.35f else 0.45f)
    val targetBorder = border ?: ScholarCardDefaults.glassBorder(isDark, accentColor = MaterialTheme.colorScheme.primary)
    
    ScholarCard(
        modifier = modifier,
        shape = shape,
        containerColor = targetColor,
        border = targetBorder,
        shadowElevation = shadowElevation,
        tonalElevation = tonalElevation,
        glassmorphic = true,
        liveReflection = liveReflection,
        onClick = onClick,
        content = content
    )
}

/**
 * GlassCapsule - Minimalist pill/capsule container with subtle glassmorphic styling,
 * live specular highlight, fully rounded into a capsule (CircleShape) with frosted glass border.
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
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.70f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.80f)
    }
    val pillBorder = border ?: ScholarCardDefaults.glassBorder(isDark)

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
        Box(contentAlignment = Alignment.Center) {
            // Subtle specular glass reflection for capsules
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}
