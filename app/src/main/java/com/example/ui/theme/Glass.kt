package ovrrup.lumia.ui.theme

import android.os.Build
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
 * Highly refined, elegant "OG" Frosted Glass UI effect.
 * Completely replaces old plastic-looking liquid glass themes with pristine,
 * harmoniously blended satin translucency.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    tintColor: Color = Color.White,
    tintAlpha: Float = 0.15f,
    blurRadius: Float = 40f, // Kept for backwards compatibility
    isDark: Boolean = false, // Kept for backwards compatibility
    borderColor: Color = Color.White, // Kept for backwards compatibility
    opacityOverride: Float? = null,
    backdropStyleOverride: String? = null
): Modifier = composed {
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.5f
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    
    val backdropStyle = backdropStyleOverride ?: LocalGlassBackdropStyle.current
    val opacitySetting = opacityOverride ?: LocalGlassOpacityValue.current
    
    val baseAlpha1 = if (isDarkTheme) (0.50f + (tintAlpha * 0.12f)) else (0.76f + (tintAlpha * 0.15f))
    val baseAlpha2 = if (isDarkTheme) (0.30f + (tintAlpha * 0.08f)) else (0.52f + (tintAlpha * 0.10f))

    val finalAlpha1 = when (backdropStyle) {
        "Opaque", "Solid" -> 1.0f
        "Transparent", "Clear" -> 0.00f
        else -> {
            // Refined blend: allow opacity to reach near-unity if set to 100%
            val factor = (opacitySetting - 0.70f).coerceAtLeast(0f) / 0.30f
            (baseAlpha1 + (1.0f - baseAlpha1) * factor) * opacitySetting
        }
    }
    val finalAlpha2 = when (backdropStyle) {
        "Opaque", "Solid" -> 1.0f
        "Transparent", "Clear" -> 0.00f
        else -> {
            val factor = (opacitySetting - 0.70f).coerceAtLeast(0f) / 0.30f
            (baseAlpha2 + (1.0f - baseAlpha2) * factor) * opacitySetting
        }
    }

    val backColor1 = if (backdropStyle == "Opaque" || backdropStyle == "Solid") {
        if (isDarkTheme) surfaceColor.mix(tintColor, 0.96f) else surfaceColor.mix(tintColor, 0.95f)
    } else {
        surfaceColor.mix(tintColor, if (isDarkTheme) 0.94f else 0.93f)
    }
    val backColor2 = if (backdropStyle == "Opaque" || backdropStyle == "Solid") {
        if (isDarkTheme) surfaceColor.mix(tintColor, 0.98f) else surfaceColor.mix(tintColor, 0.97f)
    } else {
        surfaceColor.mix(tintColor, 0.97f)
    }

    // Smooth vertically blended glass filling.
    val backBrush = Brush.verticalGradient(
        colors = listOf(
            backColor1.copy(alpha = finalAlpha1),
            backColor2.copy(alpha = finalAlpha2)
        )
    )
    
    // Ultra-fine border highlight mimicking physical glass physics
    // Utilizing harmonized theme-conforming colors to completely avoid jarring stark white borders
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val borderBrush = Brush.linearGradient(
        colors = if (isDarkTheme) {
            listOf(
                outlineVariant.copy(alpha = 0.15f),
                outlineVariant.copy(alpha = 0.04f)
            )
        } else {
            listOf(
                outlineVariant.copy(alpha = 0.22f),
                outlineVariant.copy(alpha = 0.05f)
            )
        },
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    this
        .clip(shape)
        .background(brush = backBrush, shape = shape)
        .border(width = 0.8.dp, brush = borderBrush, shape = shape)
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
