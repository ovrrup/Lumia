package lumia.tracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

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
