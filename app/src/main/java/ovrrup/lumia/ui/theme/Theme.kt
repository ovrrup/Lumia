package ovrrup.lumia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp

val LocalGlassMode = staticCompositionLocalOf { false }
val LocalMoreRounds = staticCompositionLocalOf { false }

fun Modifier.glassBar(shape: Shape): Modifier {
    return this
        .clip(shape)
        .background(Color.White.copy(alpha = 0.08f))
        .border(1.dp, Color.White.copy(alpha = 0.15f), shape)
}

fun Modifier.liquidGlass(): Modifier {
    return this
        .background(Color.White.copy(alpha = 0.05f))
}

@Composable
fun ScholarTheme(
    themeMode: String = "Dark",
    themeColor: String = "Ocean",
    pureBlackMode: Boolean = false,
    glassMode: Boolean = false,
    glassDynamic: Boolean = false,
    frostGlass: Boolean = false,
    glassBackdropStyle: String = "Default",
    glassOpacityValue: Float = 0.4f,
    navBarGlassOpacityValue: Float = 0.4f,
    navBarGlassLinkedToMain: Boolean = true,
    navBarGlassBackdropStyle: String = "Default",
    navBarGlassDynamic: Boolean = false,
    betterTexts: Boolean = true,
    betterTextsPalette: Boolean = true,
    appAnimationMode: String = "Default",
    moreRounds: Boolean = false,
    moreRoundsMode: String = "Default",
    betaDynamicTypography: Boolean = false,
    manualFontFamily: String = "Default",
    customFonts: List<ovrrup.lumia.model.CustomFont> = emptyList(),
    customPrimary: String = "",
    customPrimaryContainer: String = "",
    customBackground: String = "",
    customSurface: String = "",
    customText: String = "",
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemInDarkTheme()
    }

    val primaryColor = try {
        if (customPrimary.startsWith("#") && customPrimary.length == 7) {
            Color(android.graphics.Color.parseColor(customPrimary))
        } else {
            getThemeColorValue(themeColor, isDark)
        }
    } catch (e: Exception) {
        Color(0xFF3197D6)
    }

    val backgroundColor = if (isDark) {
        if (pureBlackMode) Color.Black else Color(0xFF0F0F11)
    } else {
        Color(0xFFF6F8FA)
    }

    val surfaceColor = if (isDark) {
        if (pureBlackMode) Color.Black else Color(0xFF16161A)
    } else {
        Color(0xFFFFFFFF)
    }

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = primaryColor,
            background = backgroundColor,
            surface = surfaceColor,
            onPrimary = Color.White,
            onBackground = Color(0xFFE3E3E6),
            onSurface = Color(0xFFE3E3E6)
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            background = backgroundColor,
            surface = surfaceColor,
            onPrimary = Color.White,
            onBackground = Color(0xFF1B1B1F),
            onSurface = Color(0xFF1B1B1F)
        )
    }

    CompositionLocalProvider(
        LocalGlassMode provides glassMode,
        LocalMoreRounds provides moreRounds
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

private fun getThemeColorValue(themeColor: String, isDark: Boolean): Color {
    return when (themeColor) {
        "Emerald" -> Color(0xFF00A859)
        "Rose" -> Color(0xFFF14668)
        "Amber" -> Color(0xFFFFB300)
        "Amethyst" -> Color(0xFF9B51E0)
        "Ocean" -> Color(0xFF3197D6)
        else -> Color(0xFF3197D6)
    }
}
