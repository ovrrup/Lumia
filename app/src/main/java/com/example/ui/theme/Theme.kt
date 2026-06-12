package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

fun createLightScheme(
    primary: Color, primaryContainer: Color,
    secondary: Color, secondaryContainer: Color,
    tertiary: Color, tertiaryContainer: Color,
    bg: Color = Color(0xFFFAFAFA),
    surface: Color = Color(0xFFFFFFFF),
    onSurfaceText: Color = Color(0xFF1A1C1A)
) = lightColorScheme(
    primary = primary, onPrimary = Color.White,
    primaryContainer = primaryContainer, onPrimaryContainer = Color.Black.mix(primaryContainer, 0.85f),
    secondary = secondary, onSecondary = Color.White,
    secondaryContainer = secondaryContainer, onSecondaryContainer = Color.Black.mix(secondaryContainer, 0.85f),
    tertiary = tertiary, onTertiary = Color.White,
    tertiaryContainer = tertiaryContainer, onTertiaryContainer = Color.Black.mix(tertiaryContainer, 0.85f),
    background = bg, onBackground = onSurfaceText,
    surface = surface, onSurface = onSurfaceText,
    surfaceVariant = Color(0xFFEAEAEA).mix(bg, 0.5f), onSurfaceVariant = onSurfaceText.mix(bg, 0.35f),
    outline = Color(0xFF79747E).mix(bg, 0.5f), outlineVariant = Color(0xFFCAC4D0).mix(bg, 0.5f),
    error = Color(0xFFBA1A1A), onError = Color.White,
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
    surfaceTint = primary
)

fun createDarkScheme(
    primary: Color, primaryContainer: Color,
    secondary: Color, secondaryContainer: Color,
    tertiary: Color, tertiaryContainer: Color,
    bg: Color = Color(0xFF101010),
    surface: Color = Color(0xFF1A1A1A),
    onSurfaceText: Color = Color(0xFFE2E2E2)
) = darkColorScheme(
    primary = primary, onPrimary = Color(0xFF101010),
    primaryContainer = primaryContainer, onPrimaryContainer = Color.White.mix(primaryContainer, 0.85f),
    secondary = secondary, onSecondary = Color(0xFF101010),
    secondaryContainer = secondaryContainer, onSecondaryContainer = Color.White.mix(secondaryContainer, 0.85f),
    tertiary = tertiary, onTertiary = Color(0xFF101010),
    tertiaryContainer = tertiaryContainer, onTertiaryContainer = Color.White.mix(tertiaryContainer, 0.85f),
    background = bg, onBackground = onSurfaceText,
    surface = surface, onSurface = onSurfaceText,
    surfaceVariant = Color(0xFF2E2E2E).mix(bg, 0.5f), onSurfaceVariant = onSurfaceText.mix(bg, 0.35f),
    outline = Color(0xFF8E918F).mix(bg, 0.5f), outlineVariant = Color(0xFF444746).mix(bg, 0.5f),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
    surfaceTint = primary
)

val OceanLight = createLightScheme(
    primary = Color(0xFF0061A4), primaryContainer = Color(0xFFD1E4FF), 
    secondary = Color(0xFF535F70), secondaryContainer = Color(0xFFD7E3F7), 
    tertiary = Color(0xFF6B5778), tertiaryContainer = Color(0xFFF2DAFF),
    bg = Color(0xFFF3F6FA), surface = Color(0xFFFFFFFF)
)
val OceanDark = createDarkScheme(
    primary = Color(0xFF9ECAFF), primaryContainer = Color(0xFF00497D), 
    secondary = Color(0xFFBBC7DB), secondaryContainer = Color(0xFF3B4858), 
    tertiary = Color(0xFFD6BEE4), tertiaryContainer = Color(0xFF523F5F),
    bg = Color(0xFF0B121A), surface = Color(0xFF121922)
)

val EmeraldLight = createLightScheme(
    primary = Color(0xFF006D36), primaryContainer = Color(0xFF95F9B7), 
    secondary = Color(0xFF506353), secondaryContainer = Color(0xFFD2E8D3), 
    tertiary = Color(0xFF3A656F), tertiaryContainer = Color(0xFFBDEAF6),
    bg = Color(0xFFF2FAF4), surface = Color(0xFFFFFFFF)
)
val EmeraldDark = createDarkScheme(
    primary = Color(0xFF79DC9C), primaryContainer = Color(0xFF005227), 
    secondary = Color(0xFFB6CCB8), secondaryContainer = Color(0xFF384B3C), 
    tertiary = Color(0xFFA1CED9), tertiaryContainer = Color(0xFF204D56),
    bg = Color(0xFF09120D), surface = Color(0xFF101914)
)

val GoldLight = createLightScheme(
    primary = Color(0xFF7D5700), primaryContainer = Color(0xFFFFDE9C), 
    secondary = Color(0xFF6C5D3F), secondaryContainer = Color(0xFFF5E0BB), 
    tertiary = Color(0xFF4B6546), tertiaryContainer = Color(0xFFBCDEB2),
    bg = Color(0xFFFBF9F1), surface = Color(0xFFFFFFFF)
)
val GoldDark = createDarkScheme(
    primary = Color(0xFFFABD00), primaryContainer = Color(0xFF5E4000), 
    secondary = Color(0xFFD8C4A0), secondaryContainer = Color(0xFF53452A), 
    tertiary = Color(0xFFB1CEA8), tertiaryContainer = Color(0xFF344D30),
    bg = Color(0xFF13110A), surface = Color(0xFF1A1810)
)

val RoseLight = createLightScheme(
    primary = Color(0xFFBF0031), primaryContainer = Color(0xFFFFDAD9), 
    secondary = Color(0xFF775656), secondaryContainer = Color(0xFFFFDAD9), 
    tertiary = Color(0xFF755A2F), tertiaryContainer = Color(0xFFFFDDAF),
    bg = Color(0xFFFFF2F2), surface = Color(0xFFFFFFFF)
)
val RoseDark = createDarkScheme(
    primary = Color(0xFFFFB3B4), primaryContainer = Color(0xFF8E0021), 
    secondary = Color(0xFFE7BDBE), secondaryContainer = Color(0xFF5D3F3F), 
    tertiary = Color(0xFFE5C18D), tertiaryContainer = Color(0xFF5C421A),
    bg = Color(0xFF170D0E), surface = Color(0xFF1F1213)
)

val SageLight = createLightScheme(
    primary = Color(0xFF3B6939), primaryContainer = Color(0xFFBCF0B4), 
    secondary = Color(0xFF53634F), secondaryContainer = Color(0xFFD6E8CE), 
    tertiary = Color(0xFF38656A), tertiaryContainer = Color(0xFFBCEBF0), 
    bg = Color(0xFFF5F9F3), surface = Color(0xFFFFFFFF)
)
val SageDark = createDarkScheme(
    primary = Color(0xFFA1D39A), primaryContainer = Color(0xFF225024), 
    secondary = Color(0xFFBACCB3), secondaryContainer = Color(0xFF3C4B38), 
    tertiary = Color(0xFFA0CFD4), tertiaryContainer = Color(0xFF1F4D52), 
    bg = Color(0xFF0F140E), surface = Color(0xFF151B14)
)

val TwilightLight = createLightScheme(
    primary = Color(0xFF5B53A8), primaryContainer = Color(0xFFE3DFFF), 
    secondary = Color(0xFF5D5D72), secondaryContainer = Color(0xFFE3E0F9), 
    tertiary = Color(0xFF795369), tertiaryContainer = Color(0xFFFFD8EC), 
    bg = Color(0xFFF7F4FB), surface = Color(0xFFFFFFFF)
)
val TwilightDark = createDarkScheme(
    primary = Color(0xFFC4C0FF), primaryContainer = Color(0xFF433B8E), 
    secondary = Color(0xFFC7C4DC), secondaryContainer = Color(0xFF454559), 
    tertiary = Color(0xFFEBB9D6), tertiaryContainer = Color(0xFF5F3C51), 
    bg = Color(0xFF111016), surface = Color(0xFF17161F)
)

fun Color.mix(other: Color, weight: Float): Color {
    return Color(
        red = this.red * weight + other.red * (1f - weight),
        green = this.green * weight + other.green * (1f - weight),
        blue = this.blue * weight + other.blue * (1f - weight),
        alpha = this.alpha * weight + other.alpha * (1f - weight)
    )
}

val LocalGlassTint = androidx.compose.runtime.compositionLocalOf { Color.White }
val LocalGlassMode = androidx.compose.runtime.compositionLocalOf { false }
val LocalGlassDynamic = androidx.compose.runtime.compositionLocalOf { true }
val LocalFrostGlass = androidx.compose.runtime.compositionLocalOf { true }
val LocalGlassBackdropStyle = androidx.compose.runtime.compositionLocalOf { "Translucent" }
val LocalGlassOpacityValue = androidx.compose.runtime.compositionLocalOf { 0.6f }

@Composable
fun ScholarTheme(
    themeMode: String = "System",
    themeColor: String = "Default",
    customPrimary: String = "",
    customPrimaryContainer: String = "",
    customBackground: String = "",
    customSurface: String = "",
    customText: String = "",
    pureBlackMode: Boolean = false,
    glassMode: Boolean = false,
    glassDynamic: Boolean = true,
    frostGlass: Boolean = true,
    glassBackdropStyle: String = "Translucent",
    glassOpacityValue: Float = 0.6f,
    betterTexts: Boolean = false,
    betterTextsPalette: Boolean = true,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "Dark" -> true
        "Light" -> false
        else -> darkTheme
    }

    val context = LocalContext.current
    var colorScheme = when {
        themeColor == "Dynamic" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        else -> when (themeColor) {
            "Ocean" -> if (isDark) OceanDark else OceanLight
            "Emerald" -> if (isDark) EmeraldDark else EmeraldLight
            "Gold" -> if (isDark) GoldDark else GoldLight
            "Rose" -> if (isDark) RoseDark else RoseLight
            "Sage" -> if (isDark) SageDark else SageLight
            "Twilight" -> if (isDark) TwilightDark else TwilightLight
            "Custom" -> {
                fun safeColor(hex: String, fallback: Color): Color {
                    if (hex.isBlank()) return fallback
                    return try { Color(android.graphics.Color.parseColor(hex)) } catch(e: Exception) { fallback }
                }
                
                val p = safeColor(customPrimary, Color(0xFF3197D6))
                val pc = safeColor(customPrimaryContainer, Color(0xFFDAF1FF))
                val bgIn = safeColor(customBackground, if (isDark) Color(0xFF101010) else Color(0xFFFAFAFA))
                val sfIn = safeColor(customSurface, if (isDark) Color(0xFF1A1A1A) else Color(0xFFFFFFFF))
                val txtIn = safeColor(customText, if (isDark) Color(0xFFE2E2E2) else Color(0xFF1A1C1A))

                // Safe luminance calculation helper
                val bgInLum = bgIn.red * 0.299f + bgIn.green * 0.587f + bgIn.blue * 0.114f
                val sfInLum = sfIn.red * 0.299f + sfIn.green * 0.587f + sfIn.blue * 0.114f
                val txtInLum = txtIn.red * 0.299f + txtIn.green * 0.587f + txtIn.blue * 0.114f

                // Intelligent adaptive custom colors to prevent contrast collisions:
                val bg = if (isDark) {
                    if (bgInLum > 0.35f) bgIn.mix(Color.Black, 0.08f) else bgIn
                } else {
                    if (bgInLum < 0.65f) bgIn.mix(Color.White, 0.96f) else bgIn
                }

                val sf = if (isDark) {
                    if (sfInLum > 0.35f) sfIn.mix(Color.Black, 0.13f) else sfIn
                } else {
                    if (sfInLum < 0.65f) sfIn.mix(Color.White, 0.99f) else sfIn
                }

                val txt = if (isDark) {
                    if (txtInLum <= 0.6f) p.mix(Color.White, 0.88f) else txtIn
                } else {
                    if (txtInLum >= 0.4f) p.mix(Color.Black, 0.15f) else txtIn
                }

                // Adjust Custom Primary based on theme mode requirement
                val pLum = p.red * 0.299f + p.green * 0.587f + p.blue * 0.114f
                val pDark = if (pLum < 0.5f) p.mix(Color.White, 0.65f) else p
                val pLight = if (pLum > 0.5f) p.mix(Color.Black, 0.65f) else p
                
                val pcLum = pc.red * 0.299f + pc.green * 0.587f + pc.blue * 0.114f
                val pcDark = if (pcLum > 0.3f) pc.mix(Color.Black, 0.5f) else pc
                val pcLight = if (pcLum < 0.7f) pc.mix(Color.White, 0.5f) else pc

                if (isDark) {
                     createDarkScheme(primary = pDark, primaryContainer = pcDark, secondary = pDark, secondaryContainer = pcDark, tertiary = pDark, tertiaryContainer = pcDark, bg = bg, surface = sf, onSurfaceText = txt)
                } else {
                     createLightScheme(primary = pLight, primaryContainer = pcLight, secondary = pLight, secondaryContainer = pcLight, tertiary = pLight, tertiaryContainer = pcLight, bg = bg, surface = sf, onSurfaceText = txt)
                }
            }
            else -> if (isDark) OceanDark else OceanLight
        }
    }

    if (isDark && pureBlackMode) {
        colorScheme = colorScheme.copy(
            background = Color.Black,
            surface = Color(0xCC000000),
            surfaceVariant = Color(0xFF1E1E1E)
        )
    }

    // Apply highly polished and harmonious Material 3 tones:
    // We use a subtle blend of background with primary (2% on primary color, 98% base) for an elegant, non-muddy backdrop.
    // Each component container mixes with its own respective core hue (primaryContainer with primary, secondaryContainer with secondary, etc.)
    // to preserve unique branding and prevent monocolor dilution, while creating soft cohesiveness.
    colorScheme = colorScheme.copy(
        background = if (isDark && pureBlackMode) Color.Black else colorScheme.background.mix(colorScheme.primary, 0.98f),
        surface = if (isDark && pureBlackMode) Color(0xCC000000) else colorScheme.surface.mix(colorScheme.primary, 0.97f),
        surfaceVariant = colorScheme.surfaceVariant.mix(colorScheme.secondary, 0.95f),
        primaryContainer = colorScheme.primaryContainer.mix(colorScheme.primary, 0.93f),
        secondaryContainer = colorScheme.secondaryContainer.mix(colorScheme.secondary, 0.93f),
        tertiaryContainer = colorScheme.tertiaryContainer.mix(colorScheme.tertiary, 0.93f),
        surfaceContainer = if (isDark && pureBlackMode) Color(0xFF121212) else colorScheme.surface.mix(colorScheme.primary, 0.92f),
        surfaceContainerLow = if (isDark && pureBlackMode) Color(0xFF0A0A0A) else colorScheme.surface.mix(colorScheme.primary, 0.95f),
        surfaceContainerHigh = if (isDark && pureBlackMode) Color(0xFF1E1E1E) else colorScheme.surface.mix(colorScheme.primary, 0.88f),
        surfaceContainerLowest = if (isDark && pureBlackMode) Color.Black else colorScheme.surface.mix(colorScheme.primary, 0.99f),
        surfaceContainerHighest = if (isDark && pureBlackMode) Color(0xFF282828) else colorScheme.surface.mix(colorScheme.primary, 0.83f)
    )

    if (betterTexts) {
        val baseTextSourceColor = if (themeColor == "Custom" && customText.isNotBlank()) {
            try { Color(android.graphics.Color.parseColor(customText)) } catch(e: Exception) { colorScheme.primary }
        } else {
            colorScheme.primary
        }

        if (betterTextsPalette) {
            if (isDark) {
                // Use lighter shades of baseTextSourceColor for text with extremely high contrast (AAA standard compatible)
                val highContrastOnSurface = baseTextSourceColor.mix(Color.White, 0.15f)
                colorScheme = colorScheme.copy(
                    onSurface = highContrastOnSurface,
                    onBackground = highContrastOnSurface,
                    onSurfaceVariant = baseTextSourceColor.mix(Color.White, 0.25f).copy(alpha = 0.88f),
                    onPrimaryContainer = highContrastOnSurface,
                    onSecondaryContainer = colorScheme.secondary.mix(Color.White, 0.15f),
                    onTertiaryContainer = colorScheme.tertiary.mix(Color.White, 0.15f)
                )
            } else {
                // Use darker shades of baseTextSourceColor for text with extremely high contrast (AAA standard compatible)
                val highContrastOnSurface = baseTextSourceColor.mix(Color.Black, 0.15f)
                colorScheme = colorScheme.copy(
                    onSurface = highContrastOnSurface,
                    onBackground = highContrastOnSurface,
                    onSurfaceVariant = baseTextSourceColor.mix(Color.Black, 0.25f).copy(alpha = 0.88f),
                    onPrimaryContainer = highContrastOnSurface,
                    onSecondaryContainer = colorScheme.secondary.mix(Color.Black, 0.15f),
                    onTertiaryContainer = colorScheme.tertiary.mix(Color.Black, 0.15f)
                )
            }
        } else {
            if (isDark) {
                // Use white colour
                colorScheme = colorScheme.copy(
                    onSurface = Color.White,
                    onBackground = Color.White,
                    onSurfaceVariant = Color.White.copy(alpha = 0.85f),
                    onPrimaryContainer = Color.White,
                    onSecondaryContainer = Color.White,
                    onTertiaryContainer = Color.White
                )
            } else {
                // Use black colour
                colorScheme = colorScheme.copy(
                    onSurface = Color.Black,
                    onBackground = Color.Black,
                    onSurfaceVariant = Color.Black.copy(alpha = 0.85f),
                    onPrimaryContainer = Color.Black,
                    onSecondaryContainer = Color.Black,
                    onTertiaryContainer = Color.Black
                )
            }
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            var contextActivity = view.context
            while (contextActivity is android.content.ContextWrapper && contextActivity !is Activity) {
                contextActivity = contextActivity.baseContext
            }
            if (contextActivity is Activity) {
                val window = contextActivity.window
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalGlassTint provides colorScheme.primary,
        LocalGlassMode provides glassMode,
        LocalGlassDynamic provides glassDynamic,
        LocalFrostGlass provides frostGlass,
        LocalGlassBackdropStyle provides glassBackdropStyle,
        LocalGlassOpacityValue provides glassOpacityValue
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
