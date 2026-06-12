package com.example.ui.components

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
import com.example.ui.theme.glassCard
import androidx.compose.foundation.background
import com.example.ui.theme.glassHero
import com.example.ui.theme.LocalGlassMode

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
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        ) {
            Box(modifier = Modifier.matchParentSize().glassCard(androidx.compose.ui.graphics.RectangleShape))
            val tint = containerColor?.copy(alpha = 0.3f) ?: Color.Transparent
            Box(modifier = Modifier.matchParentSize().background(tint))
            content()
        }
    } else {
        if (onClick != null) {
            Surface(
                modifier = modifier,
                shape = shape,
                color = containerColor ?: MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = if (containerColor == null) 2.dp else 0.dp,
                onClick = onClick
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
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        ) {
            Box(modifier = Modifier.matchParentSize().glassHero(androidx.compose.ui.graphics.RectangleShape))
            content()
        }
    } else {
        if (onClick != null) {
            Surface(
                modifier = modifier,
                shape = shape,
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 0.dp,
                onClick = onClick
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
