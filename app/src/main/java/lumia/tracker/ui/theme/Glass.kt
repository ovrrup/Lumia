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
    val primaryColor = MaterialTheme.colorScheme.primary
    
    val backdropStyle = if (isPureBlack) "Solid" else (backdropStyleOverride ?: LocalGlassBackdropStyle.current)
    val opacitySetting = if (isPureBlack) 1.0f else (opacityOverride ?: LocalGlassOpacityValue.current)
    
    val baseAlpha1 = if (isDarkTheme) (0.24f + (tintAlpha * 0.10f)) else (0.42f + (tintAlpha * 0.10f))
    val baseAlpha2 = if (isDarkTheme) (0.10f + (tintAlpha * 0.05f)) else (0.22f + (tintAlpha * 0.06f))

    val finalAlpha1 = when (backdropStyle) {
        "Opaque", "Solid" -> 1.0f
        "Transparent", "Clear" -> 0.00f
        else -> (baseAlpha1 * opacitySetting).coerceIn(0f, 1f)
    }
    val finalAlpha2 = when (backdropStyle) {
        "Opaque", "Solid" -> 1.0f
        "Transparent", "Clear" -> 0.00f
        else -> (baseAlpha2 * opacitySetting).coerceIn(0f, 1f)
    }

    // Precise physical tint mixing for a natural translucent crystal surface
    val backColor1 = if (backdropStyle == "Opaque" || backdropStyle == "Solid") {
        if (isDarkTheme) surfaceColor.mix(primaryColor, 0.85f).mix(tintColor, 0.88f) else surfaceColor.mix(tintColor, 0.90f)
    } else {
        if (isDarkTheme) surfaceColor.mix(primaryColor, 0.80f).mix(tintColor, 0.82f) else surfaceColor.mix(tintColor, 0.84f)
    }
    val backColor2 = if (backdropStyle == "Opaque" || backdropStyle == "Solid") {
        if (isDarkTheme) surfaceColor.mix(primaryColor, 0.90f).mix(tintColor, 0.92f) else surfaceColor.mix(tintColor, 0.94f)
    } else {
        if (isDarkTheme) surfaceColor.mix(primaryColor, 0.86f).mix(tintColor, 0.88f) else surfaceColor.mix(tintColor, 0.90f)
    }

    // Clean vertical translucency fill
    val backBrush = Brush.verticalGradient(
        colors = listOf(
            backColor1.copy(alpha = finalAlpha1),
            backColor2.copy(alpha = finalAlpha2)
        )
    )

    // Directional specular glare simulating top-left light source on physical bevels
    val glossBrush = Brush.linearGradient(
        colors = if (isDarkTheme) {
            listOf(
                Color.White.copy(alpha = 0.10f),
                Color.White.copy(alpha = 0.02f),
                Color.Transparent,
                primaryColor.copy(alpha = 0.03f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.28f),
                Color.White.copy(alpha = 0.08f),
                Color.Transparent,
                primaryColor.copy(alpha = 0.04f)
            )
        },
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    
    // Crisp, ultra-thin high-precision glass rim outline
    val borderBrush = Brush.linearGradient(
        colors = if (isDarkTheme) {
            listOf(
                Color.White.copy(alpha = 0.18f),
                primaryColor.copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.06f),
                Color.Transparent,
                Color.White.copy(alpha = 0.04f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.35f),
                primaryColor.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.10f),
                Color.Transparent,
                Color.White.copy(alpha = 0.06f)
            )
        },
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    val frostGlass = LocalFrostGlass.current
    val frostModifier = if (frostGlass && backdropStyle != "Solid" && backdropStyle != "Opaque") {
        val frostColor = if (isDarkTheme) Color(0xFF18181B).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.25f)
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
