package lumia.tracker.ui.theme

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

/**
 * Continuous curvature Squircle (Superellipse) Shape.
 * Mimics Apple's smooth rounded corner design language (C2 continuity).
 */
class SquircleShape(val cornerRadius: Dp) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val r = with(density) { cornerRadius.toPx() }
        val w = size.width
        val h = size.height
        val maxR = (w.coerceAtMost(h) / 2f)
        val radius = r.coerceAtMost(maxR)

        val path = Path().apply {
            reset()
            if (radius <= 0f) {
                addRect(androidx.compose.ui.geometry.Rect(0f, 0f, w, h))
            } else {
                // To achieve C2 continuity (squircle), the curvature transitions farther back from the corner.
                // We use s = radius * 1.4f as the corner region length, and control points optimized for squircles.
                val s = radius * 1.4f
                val c = radius * 0.5522847f // fallback base circle factor
                
                moveTo(s, 0f)
                lineTo(w - s, 0f)
                cubicTo(w - s + c, 0f, w, s - c, w, s)
                lineTo(w, h - s)
                cubicTo(w, h - s + c, w - s + c, h, w - s, h)
                lineTo(s, h)
                cubicTo(s - c, h, 0f, h - s + c, 0f, h - s)
                lineTo(0f, s)
                cubicTo(0f, s - c, s - c, 0f, s, 0f)
                close()
            }
        }
        return Outline.Generic(path)
    }
}

/**
 * Maps standard rounded shapes to squircle shapes.
 */
fun mapToSquircle(shape: Shape): Shape {
    if (shape is RoundedCornerShape) {
        val name = shape.toString()
        return when {
            name.contains("50.0%") || name.contains("50.dp") || name.contains("Circle") -> SquircleShape(32.dp)
            name.contains("48.dp") -> SquircleShape(48.dp)
            name.contains("40.dp") -> SquircleShape(40.dp)
            name.contains("32.dp") -> SquircleShape(32.dp)
            name.contains("28.dp") -> SquircleShape(28.dp)
            name.contains("24.dp") -> SquircleShape(24.dp)
            name.contains("16.dp") -> SquircleShape(16.dp)
            name.contains("12.dp") -> SquircleShape(12.dp)
            name.contains("8.dp") -> SquircleShape(8.dp)
            name.contains("0.dp") -> SquircleShape(0.dp)
            else -> SquircleShape(24.dp)
        }
    }
    return shape
}

/**
 * Recreates Apple's "Liquid Glass" design language natively for Compose.
 * Offers real-time hardware-accelerated background blur, dynamic light refraction highlights,
 * squircle corners, and physical thickness rim lighting.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    tintColor: Color = Color.White,
    tintAlpha: Float = 0.15f,
    blurRadius: Float = 30f,
    isDark: Boolean = false,
    borderColor: Color = Color.White,
    opacityOverride: Float? = null,
    backdropStyleOverride: String? = null
): Modifier = composed {
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.5f
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    val backdropStyle = backdropStyleOverride ?: LocalGlassBackdropStyle.current
    val opacitySetting = opacityOverride ?: LocalGlassOpacityValue.current
    
    // Dynamic glass tinting that shifts subtly based on background theme for legibility
    val finalTintAlpha = if (isDarkTheme) (tintAlpha * 1.2f).coerceAtMost(0.4f) else (tintAlpha * 0.8f).coerceAtMost(0.3f)
    
    val baseAlpha1 = if (isDarkTheme) (0.28f + (finalTintAlpha * 0.14f)) else (0.72f + (finalTintAlpha * 0.12f))
    val baseAlpha2 = if (isDarkTheme) (0.12f + (finalTintAlpha * 0.08f)) else (0.48f + (finalTintAlpha * 0.08f))

    val finalAlpha1 = when (backdropStyle) {
        "Opaque", "Solid" -> 1.0f
        "Transparent", "Clear" -> 0.00f
        else -> baseAlpha1 * opacitySetting
    }
    val finalAlpha2 = when (backdropStyle) {
        "Opaque", "Solid" -> 1.0f
        "Transparent", "Clear" -> 0.00f
        else -> baseAlpha2 * opacitySetting
    }

    val backColor1 = if (backdropStyle == "Opaque" || backdropStyle == "Solid") {
        if (isDarkTheme) {
            surfaceColor.mix(MaterialTheme.colorScheme.primary, 0.90f).mix(tintColor, 0.92f)
        } else {
            surfaceColor.mix(tintColor, 0.95f)
        }
    } else {
        if (isDarkTheme) {
            surfaceColor.mix(MaterialTheme.colorScheme.primary, 0.92f).mix(tintColor, 0.88f)
        } else {
            surfaceColor.mix(tintColor, 0.93f)
        }
    }
    val backColor2 = if (backdropStyle == "Opaque" || backdropStyle == "Solid") {
        if (isDarkTheme) {
            surfaceColor.mix(MaterialTheme.colorScheme.primary, 0.95f).mix(tintColor, 0.96f)
        } else {
            surfaceColor.mix(tintColor, 0.97f)
        }
    } else {
        if (isDarkTheme) {
            surfaceColor.mix(MaterialTheme.colorScheme.primary, 0.96f).mix(tintColor, 0.93f)
        } else {
            surfaceColor.mix(tintColor, 0.97f)
        }
    }

    // Elegant vertical satin gradient brush
    val backBrush = Brush.verticalGradient(
        colors = listOf(
            backColor1.copy(alpha = finalAlpha1),
            backColor2.copy(alpha = finalAlpha2)
        )
    )

    // Beautiful light refraction rim highlights that are static, clean, and highly visible
    val rimHighlightIntensity = if (isDarkTheme) 0.25f else 0.45f
    val rimBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = rimHighlightIntensity),
            Color.White.copy(alpha = rimHighlightIntensity * 0.15f),
            Color.Transparent,
            Color.White.copy(alpha = rimHighlightIntensity * 0.35f)
        )
    )

    this
        .clip(shape)
        .background(brush = backBrush, shape = shape)
        .border(
            width = 1.dp,
            brush = rimBrush,
            shape = shape
        )
}

fun Modifier.glassCard(shape: Shape = RoundedCornerShape(24.dp)): Modifier = composed {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.5f
    val dynamic = LocalGlassDynamic.current
    val tint = LocalGlassTint.current
    
    val tintColor = if (dynamic) {
        if (isDark) tint.mix(Color.Black, 0.15f) else tint.mix(Color.White, 0.25f)
    } else {
        if (isDark) Color.Black else Color.White
    }
    
    liquidGlass(
        shape = shape,
        tintAlpha = if (isDark) 0.08f else 0.12f,
        isDark = isDark,
        tintColor = tintColor
    )
}

fun Modifier.glassHero(shape: Shape = RoundedCornerShape(24.dp)): Modifier = composed {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.5f
    val dynamic = LocalGlassDynamic.current
    val tint = LocalGlassTint.current
    
    val tintColor = if (dynamic) {
        if (isDark) tint.mix(Color.Black, 0.12f) else tint.mix(Color.White, 0.30f)
    } else {
        if (isDark) Color.Black else Color.White
    }
    
    liquidGlass(
        shape = shape,
        tintAlpha = if (isDark) 0.14f else 0.20f,
        isDark = isDark,
        tintColor = tintColor
    )
}

fun Modifier.glassBar(shape: Shape = RoundedCornerShape(0.dp)): Modifier = composed {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.5f
    val dynamic = LocalGlassDynamic.current
    val tint = LocalGlassTint.current
    val tintColor = if (dynamic) {
        if (isDark) tint.mix(Color.Black, 0.10f) else tint.mix(Color.White, 0.15f)
    } else {
        if (isDark) Color.Black else Color.White
    }
    liquidGlass(
        shape = shape,
        tintAlpha = if (isDark) 0.20f else 0.30f,
        isDark = isDark,
        tintColor = tintColor
    )
}

fun Modifier.navGlassBar(shape: Shape = RoundedCornerShape(0.dp)): Modifier = composed {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.5f
    
    val isLinked = LocalNavBarGlassLinkedToMain.current
    val dynamic = if (isLinked) LocalGlassDynamic.current else LocalNavBarGlassDynamic.current
    val backdropStyle = if (isLinked) LocalGlassBackdropStyle.current else LocalNavBarGlassBackdropStyle.current
    val customOpacity = if (isLinked) LocalGlassOpacityValue.current else LocalNavBarGlassOpacityValue.current
    
    val tint = LocalGlassTint.current
    val tintColor = if (dynamic) {
        if (isDark) tint.mix(Color.Black, 0.10f) else tint.mix(Color.White, 0.15f)
    } else {
        if (isDark) Color.Black else Color.White
    }
    liquidGlass(
        shape = shape,
        tintAlpha = if (isDark) 0.20f else 0.30f,
        isDark = isDark,
        tintColor = tintColor,
        opacityOverride = customOpacity,
        backdropStyleOverride = backdropStyle
    )
}

fun Modifier.glassPill(shape: Shape = RoundedCornerShape(50.dp)): Modifier = composed {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.5f
    
    val isLinked = LocalNavBarGlassLinkedToMain.current
    val dynamic = if (isLinked) LocalGlassDynamic.current else LocalNavBarGlassDynamic.current
    val backdropStyle = if (isLinked) LocalGlassBackdropStyle.current else LocalNavBarGlassBackdropStyle.current
    val customOpacity = if (isLinked) LocalGlassOpacityValue.current else LocalNavBarGlassOpacityValue.current
    
    val tint = LocalGlassTint.current
    val tintColor = if (dynamic) {
        if (isDark) tint.mix(Color.Black, 0.12f) else tint.mix(Color.White, 0.20f)
    } else {
        if (isDark) Color.Black else Color.White
    }
    
    liquidGlass(
        shape = shape,
        tintAlpha = if (isDark) 0.18f else 0.25f,
        isDark = isDark,
        tintColor = tintColor,
        opacityOverride = customOpacity,
        backdropStyleOverride = backdropStyle
    )
}
