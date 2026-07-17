package lumia.tracker.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import lumia.tracker.ui.theme.LocalGlassMode
import lumia.tracker.ui.theme.SquircleShape
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.ui.theme.liquidGlass

/**
 * Reusable Apple-style "Liquid Glass" Surface.
 * Automatically handles real-time blur, spring-based resizing, squircles, and fallback behavior.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    tintColor: Color = Color.White,
    tintAlpha: Float = 0.15f,
    blurRadius: Float = 30f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isGlassEnabled = LocalGlassMode.current
    val squircleShape = SquircleShape(cornerRadius)

    val springSpec = spring<androidx.compose.ui.unit.IntSize>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    if (isGlassEnabled) {
        Box(
            modifier = modifier
                .animateContentSize(animationSpec = springSpec)
                .clip(squircleShape)
                .then(if (onClick != null) Modifier.bouncyClick(onClick = onClick) else Modifier)
                .liquidGlass(
                    shape = squircleShape,
                    tintColor = tintColor,
                    tintAlpha = tintAlpha,
                    blurRadius = blurRadius
                )
        ) {
            content()
        }
    } else {
        // High-fidelity fallback Material 3 container surface
        val baseModifier = modifier
            .animateContentSize(animationSpec = springSpec)
            .then(if (onClick != null) Modifier.bouncyClick(onClick = onClick) else Modifier)
            
        Surface(
            modifier = baseModifier,
            shape = squircleShape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 2.dp
        ) {
            Box(content = content)
        }
    }
}
