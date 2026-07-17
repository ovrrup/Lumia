package lumia.tracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * High-fidelity card using the custom "Liquid Glass" design language.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    containerColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val cornerRadius = if (shape is RoundedCornerShape) {
        val name = shape.toString()
        when {
            name.contains("32.dp") -> 32.dp
            name.contains("24.dp") -> 24.dp
            name.contains("16.dp") -> 16.dp
            name.contains("12.dp") -> 12.dp
            name.contains("8.dp") -> 8.dp
            else -> 24.dp
        }
    } else {
        24.dp
    }

    LiquidGlassSurface(
        modifier = modifier,
        cornerRadius = cornerRadius,
        tintColor = containerColor ?: Color.White,
        tintAlpha = 0.12f,
        onClick = onClick,
        content = content
    )
}

/**
 * Specially tuned Glass Card for premium/hero focal points.
 */
@Composable
fun GlassHeroCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(32.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val cornerRadius = if (shape is RoundedCornerShape) {
        val name = shape.toString()
        when {
            name.contains("32.dp") -> 32.dp
            name.contains("24.dp") -> 24.dp
            name.contains("16.dp") -> 16.dp
            else -> 32.dp
        }
    } else {
        32.dp
    }

    LiquidGlassSurface(
        modifier = modifier,
        cornerRadius = cornerRadius,
        tintColor = MaterialTheme.colorScheme.primaryContainer,
        tintAlpha = 0.22f,
        onClick = onClick,
        content = content
    )
}
