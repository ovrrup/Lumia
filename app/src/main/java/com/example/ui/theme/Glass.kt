package com.example.ui.theme

import android.os.Build
import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.composed

/**
 * Modern soft liquid glass effect
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    tintColor: Color = Color.White,
    tintAlpha: Float = 0.15f,
    blurRadius: Float = 40f,
    isDark: Boolean = false,
    borderColor: Color = Color.White
): Modifier = this
    .clip(shape)
    .then(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.graphicsLayer {
                renderEffect = RenderEffect
                    .createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)
                    .asComposeRenderEffect()
            }
        } else Modifier
    )
    .background(
        brush = Brush.linearGradient(
            colors = if (isDark) listOf(
                tintColor.copy(alpha = tintAlpha + 0.05f),
                tintColor.copy(alpha = tintAlpha)
            ) else listOf(
                tintColor.copy(alpha = tintAlpha + 0.20f),
                tintColor.copy(alpha = tintAlpha + 0.05f)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        shape = shape
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = if (isDark) listOf(
                borderColor.copy(alpha = 0.16f),
                borderColor.copy(alpha = 0.03f)
            ) else listOf(
                borderColor.copy(alpha = 0.45f),
                borderColor.copy(alpha = 0.08f)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        shape = shape
    )

fun Modifier.glassCard(shape: Shape = RoundedCornerShape(24.dp)): Modifier = composed {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.5f
    val dynamic = LocalGlassDynamic.current
    if (dynamic) {
        val tint = LocalGlassTint.current
        val tintColor = if (isDark) tint.mix(Color.Black, 0.10f) else tint.mix(Color.White, 0.15f)
        val borderColor = if (isDark) tint.mix(Color.White, 0.30f) else tint.mix(Color.White, 0.70f)
        liquidGlass(
            shape = shape,
            tintAlpha = if (isDark) 0.12f else 0.20f,
            blurRadius = 45f,
            isDark = isDark,
            tintColor = tintColor,
            borderColor = borderColor
        )
    } else {
        liquidGlass(
            shape = shape,
            tintAlpha = if (isDark) 0.08f else 0.15f,
            blurRadius = 50f,
            isDark = isDark,
            tintColor = if (isDark) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White,
            borderColor = androidx.compose.ui.graphics.Color.White
        )
    }
}

fun Modifier.glassHero(shape: Shape = RoundedCornerShape(24.dp)): Modifier = composed {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.5f
    val dynamic = LocalGlassDynamic.current
    if (dynamic) {
        val tint = LocalGlassTint.current
        val tintColor = if (isDark) tint.mix(Color.Black, 0.15f) else tint.mix(Color.White, 0.25f)
        val borderColor = if (isDark) tint.mix(Color.White, 0.40f) else tint.mix(Color.White, 0.80f)
        liquidGlass(
            shape = shape,
            tintAlpha = if (isDark) 0.16f else 0.28f,
            blurRadius = 60f,
            isDark = isDark,
            tintColor = tintColor,
            borderColor = borderColor
        )
    } else {
        liquidGlass(
            shape = shape,
            tintAlpha = if (isDark) 0.12f else 0.3f,
            blurRadius = 70f,
            isDark = isDark,
            tintColor = if (isDark) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White,
            borderColor = androidx.compose.ui.graphics.Color.White
        )
    }
}

fun Modifier.glassBar(shape: Shape = RoundedCornerShape(0.dp)): Modifier = composed {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.5f
    val dynamic = LocalGlassDynamic.current
    if (dynamic) {
        val tint = LocalGlassTint.current
        val tintColor = if (isDark) tint.mix(Color.Black, 0.08f) else tint.mix(Color.White, 0.12f)
        val borderColor = if (isDark) tint.mix(Color.White, 0.20f) else tint.mix(Color.White, 0.60f)
        liquidGlass(
            shape = shape,
            tintAlpha = if (isDark) 0.08f else 0.15f,
            blurRadius = 30f,
            isDark = isDark,
            tintColor = tintColor,
            borderColor = borderColor
        )
    } else {
        liquidGlass(
            shape = shape,
            tintAlpha = if (isDark) 0.05f else 0.1f,
            blurRadius = 30f,
            isDark = isDark,
            tintColor = if (isDark) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White,
            borderColor = androidx.compose.ui.graphics.Color.White
        )
    }
}

fun Modifier.glassPill(shape: Shape = RoundedCornerShape(50.dp)): Modifier = composed {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.5f
    val dynamic = LocalGlassDynamic.current
    if (dynamic) {
        val tint = LocalGlassTint.current
        val tintColor = if (isDark) tint.mix(Color.Black, 0.12f) else tint.mix(Color.White, 0.18f)
        val borderColor = if (isDark) tint.mix(Color.White, 0.35f) else tint.mix(Color.White, 0.75f)
        liquidGlass(
            shape = shape,
            tintAlpha = if (isDark) 0.14f else 0.24f,
            blurRadius = 25f,
            isDark = isDark,
            tintColor = tintColor,
            borderColor = borderColor
        )
    } else {
        liquidGlass(
            shape = shape,
            tintAlpha = if (isDark) 0.1f else 0.2f,
            blurRadius = 25f,
            isDark = isDark,
            tintColor = if (isDark) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White,
            borderColor = androidx.compose.ui.graphics.Color.White
        )
    }
}
