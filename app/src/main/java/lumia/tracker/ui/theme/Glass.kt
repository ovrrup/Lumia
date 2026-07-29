package lumia.tracker.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
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
    val isDarkTheme = LocalDarkTheme.current
    val isPureBlack = LocalPureBlackMode.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    
    val backdropStyle = if (isPureBlack) "Solid" else (backdropStyleOverride ?: LocalGlassBackdropStyle.current)
    val opacitySetting = if (isPureBlack) 1.0f else (opacityOverride ?: LocalGlassOpacityValue.current)
    
    val baseAlpha1 = if (isDarkTheme) (0.20f + (tintAlpha * 0.12f)) else (0.38f + (tintAlpha * 0.12f))
    val baseAlpha2 = if (isDarkTheme) (0.08f + (tintAlpha * 0.06f)) else (0.18f + (tintAlpha * 0.08f))

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

    // Mix much more vibrant primary hue into the background to amplify underlying bleeding colors
    val backColor1 = if (backdropStyle == "Opaque" || backdropStyle == "Solid") {
        if (isDarkTheme) {
            surfaceColor.mix(MaterialTheme.colorScheme.primary, 0.82f).mix(tintColor, 0.85f)
        } else {
            surfaceColor.mix(tintColor, 0.88f)
        }
    } else {
        if (isDarkTheme) {
            surfaceColor.mix(MaterialTheme.colorScheme.primary, 0.78f).mix(tintColor, 0.80f)
        } else {
            surfaceColor.mix(tintColor, 0.82f)
        }
    }
    val backColor2 = if (backdropStyle == "Opaque" || backdropStyle == "Solid") {
        if (isDarkTheme) {
            surfaceColor.mix(MaterialTheme.colorScheme.primary, 0.88f).mix(tintColor, 0.90f)
        } else {
            surfaceColor.mix(tintColor, 0.92f)
        }
    } else {
        if (isDarkTheme) {
            surfaceColor.mix(MaterialTheme.colorScheme.primary, 0.85f).mix(tintColor, 0.86f)
        } else {
            surfaceColor.mix(tintColor, 0.88f)
        }
    }

    // Smooth vertically blended glass filling.
    val backBrush = Brush.verticalGradient(
        colors = listOf(
            backColor1.copy(alpha = finalAlpha1),
            backColor2.copy(alpha = finalAlpha2)
        )
    )

    // Secondary diagonal satin glossy shimmer brush to reflect and amplify light
    val glossBrush = Brush.linearGradient(
        colors = if (isDarkTheme) {
            listOf(
                Color.White.copy(alpha = 0.08f),
                Color.Transparent,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.16f),
                Color.Transparent,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
            )
        },
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    
    // Ultra-fine border highlight mimicking physical glass physics
    // Dual-tone high-specular glisten reflecting bright ambient colors
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val borderBrush = Brush.linearGradient(
        colors = if (isDarkTheme) {
            listOf(
                Color.White.copy(alpha = 0.14f), // Extremely soft, elegant edge glare
                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                Color.White.copy(alpha = 0.05f),
                Color.Transparent,
                Color.White.copy(alpha = 0.03f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.22f), // Beautiful, soft edge glare on light mode
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.08f),
                Color.Transparent,
                Color.White.copy(alpha = 0.04f)
            )
        },
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    // Optional physical frost depth emulation layer
    val frostGlass = LocalFrostGlass.current
    val frostModifier = if (frostGlass) {
        val frostColor = if (isDarkTheme) Color(0xFF1E1E1E).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.35f)
        Modifier.background(color = frostColor, shape = shape)
    } else {
        Modifier
    }

    this
        .clip(shape)
        .background(brush = backBrush, shape = shape)
        .then(frostModifier)
        .background(brush = glossBrush, shape = shape)
        .border(width = 0.75.dp, brush = borderBrush, shape = shape)
}

fun Modifier.glassCard(shape: Shape = RoundedCornerShape(24.dp)): Modifier = composed {
    val isDark = LocalDarkTheme.current
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
    val isDark = LocalDarkTheme.current
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
    val isDark = LocalDarkTheme.current
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
    val isDark = LocalDarkTheme.current
    
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
    val isDark = LocalDarkTheme.current
    
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
