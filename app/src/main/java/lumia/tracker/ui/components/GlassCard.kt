package lumia.tracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import lumia.tracker.ui.theme.glassCard
import androidx.compose.foundation.background
import lumia.tracker.ui.theme.glassHero
import lumia.tracker.ui.theme.LocalGlassMode
import lumia.tracker.ui.theme.bouncyClick

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    containerColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isGlass = LocalGlassMode.current
    if (isGlass) {
        Box(
            modifier = modifier
                .clip(shape)
                .then(if (onClick != null) Modifier.bouncyClick(onClick = onClick) else Modifier)
        ) {
            Box(modifier = Modifier.matchParentSize().glassCard(shape))
            val tint = containerColor?.copy(alpha = 0.3f) ?: Color.Transparent
            Box(modifier = Modifier.matchParentSize().background(tint))
            content()
        }
    } else {
        if (onClick != null) {
            Surface(
                modifier = modifier.bouncyClick(onClick = onClick),
                shape = shape,
                color = containerColor ?: MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = if (containerColor == null) 2.dp else 0.dp
            ) {
                Box(content = content)
            }
        } else {
            Surface(
                modifier = modifier,
                shape = shape,
                color = containerColor ?: MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = if (containerColor == null) 2.dp else 0.dp
            ) {
                Box(content = content)
            }
        }
    }
}

@Composable
fun GlassHeroCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(32.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isGlass = LocalGlassMode.current
    if (isGlass) {
        Box(
            modifier = modifier
                .clip(shape)
                .then(if (onClick != null) Modifier.bouncyClick(onClick = onClick) else Modifier)
        ) {
            Box(modifier = Modifier.matchParentSize().glassHero(shape))
            content()
        }
    } else {
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
}
