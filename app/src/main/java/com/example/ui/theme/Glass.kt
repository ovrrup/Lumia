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
 * Modern soft liquid or classical frost glass effect
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    tintColor: Color = Color.White,
    tintAlpha: Float = 0.15f,
    blurRadius: Float = 40f,
    isDark: Boolean = false,
    borderColor: Color = Color.White
): Modifier = composed {
    val frostGlass = LocalFrostGlass.current
    
    if (frostGlass) {
        // Classic Frosted Glass Theme: Satin finish, pristine high-contrast legibility, no child blurs
        this
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDark) listOf(
                        tintColor.mix(Color.Black, 0.12f).copy(alpha = tintAlpha + 0.18f),
                        tintColor.mix(Color.Black, 0.22f).copy(alpha = tintAlpha + 0.10f)
                    ) else listOf(
                        tintColor.mix(Color.White, 0.25f).copy(alpha = tintAlpha + 0.24f),
                        tintColor.mix(Color.White, 0.12f).copy(alpha = tintAlpha + 0.12f)
                    )
                ),
                shape = shape
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = if (isDark) listOf(
                        Color.White.copy(alpha = 0.20f),
                        Color.White.copy(alpha = 0.04f)
                    ) else listOf(
                        Color.White.copy(alpha = 0.60f),
                        Color.White.copy(alpha = 0.15f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = shape
            )
    } else {
        // Liquid Glass Theme: Multi-shine premium dynamic liquid gloss
        this
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = if (isDark) listOf(
                        tintColor.copy(alpha = tintAlpha + 0.12f),
                        tintColor.copy(alpha = tintAlpha + 0.04f),
                        tintColor.copy(alpha = tintAlpha + 0.22f) // Specular highlight
                    ) else listOf(
                        tintColor.copy(alpha = tintAlpha + 0.24f),
                        tintColor.copy(alpha = tintAlpha + 0.08f),
                        tintColor.copy(alpha = tintAlpha + 0.38f) // Specular highlight
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
                        borderColor.copy(alpha = 0.30f),
                        borderColor.copy(alpha = 0.10f),
                        borderColor.copy(alpha = 0.45f) // Metallic rim reflection
                    ) else listOf(
                        borderColor.copy(alpha = 0.65f),
                        borderColor.copy(alpha = 0.25f),
                        borderColor.copy(alpha = 0.85f) // Metallic rim reflection
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = shape
            )
    }
}

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
            blurRadius = 15f,
            isDark = isDark,
            tintColor = tintColor,
            borderColor = borderColor
        )
    } else {
        liquidGlass(
            shape = shape,
            tintAlpha = if (isDark) 0.08f else 0.15f,
            blurRadius = 18f,
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
            blurRadius = 20f,
            isDark = isDark,
            tintColor = tintColor,
            borderColor = borderColor
        )
    } else {
        liquidGlass(
            shape = shape,
            tintAlpha = if (isDark) 0.12f else 0.3f,
            blurRadius = 22f,
            isDark = isDark,
            tintColor = if (isDark) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White,
            borderColor = androidx.compose.ui.graphics.Color.White
        )
    }
}

fun Modifier.glassBar(shape: Shape = RoundedCornerShape(0.dp)): Modifier = composed {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.5f
    val dynamic = LocalGlassDynamic.current
    
    // Primary navigation bar: NEVER use RenderEffect blur on containers to keep navigation text/icons 100% visible
    if (dynamic) {
        val tint = LocalGlassTint.current
        val tintColor = if (isDark) tint.mix(Color.Black, 0.10f) else tint.mix(Color.White, 0.15f)
        val borderColor = if (isDark) tint.mix(Color.White, 0.25f) else tint.mix(Color.White, 0.65f)
        
        // Use high-contrast frosted translucent colors
        this
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDark) listOf(
                        tintColor.mix(Color.Black, 0.15f).copy(alpha = 0.90f),
                        tintColor.mix(Color.Black, 0.25f).copy(alpha = 0.82f)
                    ) else listOf(
                        tintColor.mix(Color.White, 0.30f).copy(alpha = 0.94f),
                        tintColor.mix(Color.White, 0.15f).copy(alpha = 0.85f)
                    )
                ),
                shape = shape
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = if (isDark) listOf(
                        borderColor.copy(alpha = 0.25f),
                        borderColor.copy(alpha = 0.08f)
                    ) else listOf(
                        borderColor.copy(alpha = 0.65f),
                        borderColor.copy(alpha = 0.20f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = shape
            )
    } else {
        val baseColor = if (isDark) Color(0xFF101210) else Color(0xFFF9FAF9)
        this
            .clip(shape)
            .background(
                color = baseColor.copy(alpha = if (isDark) 0.88f else 0.93f),
                shape = shape
            )
            .border(
                width = 1.dp,
                color = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                shape = shape
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
            tintAlpha = if (isDark) 0.24f else 0.34f, // Higher alpha for floating nav-pills to guarantee readability
            blurRadius = 12f,
            isDark = isDark,
            tintColor = tintColor,
            borderColor = borderColor
        )
    } else {
        liquidGlass(
            shape = shape,
            tintAlpha = if (isDark) 0.20f else 0.28f,
            blurRadius = 14f,
            isDark = isDark,
            tintColor = if (isDark) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White,
            borderColor = androidx.compose.ui.graphics.Color.White
        )
    }
}
