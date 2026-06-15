package ovrrup.lumia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.material3.Typography
import ovrrup.lumia.R

val LocalGlassMode = staticCompositionLocalOf { false }
val LocalMoreRounds = staticCompositionLocalOf { false }

private val gmsProvider = GoogleFont.Provider(
    "com.google.android.gms.fonts",
    "com.google.android.gms",
    R.array.com_google_android_gms_fonts_certs
)

fun getFontFamily(fontName: String, fallback: FontFamily = FontFamily.Default): FontFamily {
    return try {
        val googleFont = GoogleFont(fontName)
        FontFamily(
            Font(googleFont = googleFont, fontProvider = gmsProvider, weight = FontWeight.Normal),
            Font(googleFont = googleFont, fontProvider = gmsProvider, weight = FontWeight.Medium),
            Font(googleFont = googleFont, fontProvider = gmsProvider, weight = FontWeight.SemiBold),
            Font(googleFont = googleFont, fontProvider = gmsProvider, weight = FontWeight.Bold),
            Font(googleFont = googleFont, fontProvider = gmsProvider, weight = FontWeight.ExtraBold)
        )
    } catch (e: Exception) {
        fallback
    }
}

fun createPairTypography(displayFont: FontFamily, bodyFont: FontFamily): Typography {
    val defaultTypo = Typography()
    return Typography(
        displayLarge = defaultTypo.displayLarge.copy(fontFamily = displayFont),
        displayMedium = defaultTypo.displayMedium.copy(fontFamily = displayFont),
        displaySmall = defaultTypo.displaySmall.copy(fontFamily = displayFont),
        headlineLarge = defaultTypo.headlineLarge.copy(fontFamily = displayFont),
        headlineMedium = defaultTypo.headlineMedium.copy(fontFamily = displayFont),
        headlineSmall = defaultTypo.headlineSmall.copy(fontFamily = displayFont),
        titleLarge = defaultTypo.titleLarge.copy(fontFamily = displayFont, fontWeight = FontWeight.Bold),
        titleMedium = defaultTypo.titleMedium.copy(fontFamily = displayFont, fontWeight = FontWeight.SemiBold),
        titleSmall = defaultTypo.titleSmall.copy(fontFamily = displayFont, fontWeight = FontWeight.Medium),
        bodyLarge = defaultTypo.bodyLarge.copy(fontFamily = bodyFont),
        bodyMedium = defaultTypo.bodyMedium.copy(fontFamily = bodyFont),
        bodySmall = defaultTypo.bodySmall.copy(fontFamily = bodyFont),
        labelLarge = defaultTypo.labelLarge.copy(fontFamily = bodyFont),
        labelMedium = defaultTypo.labelMedium.copy(fontFamily = bodyFont),
        labelSmall = defaultTypo.labelSmall.copy(fontFamily = bodyFont)
    )
}

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

    val selectedTypography = remember(betaDynamicTypography, manualFontFamily, themeColor, isDark, customFonts) {
        val activeCustomFont = customFonts.firstOrNull { font ->
            font.isActive && 
            (font.conditionTheme == "Any" || font.conditionTheme.equals(themeColor, ignoreCase = true)) &&
            (font.conditionMode == "Any" || (font.conditionMode.equals("Dark", ignoreCase = true) && isDark) || (font.conditionMode.equals("Light", ignoreCase = true) && !isDark))
        }

        val (displayF, bodyF) = when {
            activeCustomFont != null -> {
                val f = getFontFamily(activeCustomFont.fontName)
                Pair(f, f)
            }
            betaDynamicTypography -> {
                when (themeColor) {
                    "Emerald", "Sage" -> {
                        Pair(getFontFamily("Montserrat"), getFontFamily("Quicksand"))
                    }
                    "Rose" -> {
                        Pair(getFontFamily("Quicksand"), getFontFamily("Quicksand"))
                    }
                    "Amber" -> {
                        Pair(getFontFamily("Fredoka"), getFontFamily("Outfit"))
                    }
                    "Amethyst" -> {
                        Pair(getFontFamily("Space Grotesk"), getFontFamily("Urbanist"))
                    }
                    "Ocean" -> {
                        Pair(getFontFamily("Poppins"), getFontFamily("Inter"))
                    }
                    else -> {
                        Pair(getFontFamily("Poppins"), getFontFamily("Inter"))
                    }
                }
            }
            manualFontFamily != "Default" && manualFontFamily.isNotBlank() -> {
                val f = getFontFamily(manualFontFamily)
                Pair(f, f)
            }
            else -> {
                Pair(FontFamily.Default, FontFamily.Default)
            }
        }
        createPairTypography(displayF, bodyF)
    }

    CompositionLocalProvider(
        LocalGlassMode provides glassMode,
        LocalMoreRounds provides moreRounds
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = selectedTypography,
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
