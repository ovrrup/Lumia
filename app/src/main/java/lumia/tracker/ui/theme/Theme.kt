package lumia.tracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalGlassTint = compositionLocalOf { Color.White }
val LocalDarkTheme = compositionLocalOf { false }
val LocalPureBlackMode = compositionLocalOf { false }
val LocalGlassMode = compositionLocalOf { false }
val LocalGlassDynamic = compositionLocalOf { true }
val LocalFrostGlass = compositionLocalOf { true }
val LocalGlassBackdropStyle = compositionLocalOf { "Translucent" }
val LocalGlassOpacityValue = compositionLocalOf { 0.6f }
val LocalNavBarGlassOpacityValue = compositionLocalOf { 0.6f }
val LocalNavBarGlassLinkedToMain = compositionLocalOf { true }
val LocalNavBarGlassBackdropStyle = compositionLocalOf { "Translucent" }
val LocalNavBarGlassDynamic = compositionLocalOf { true }
val LocalAppAnimationMode = compositionLocalOf { "Normal" }
val LocalMoreRounds = compositionLocalOf { false }
val LocalMoreRoundsMode = compositionLocalOf { "Pastel" }

@Composable
fun ScholarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeMode: String = "System",
    themeColor: String = "Ocean",
    themePreset: String = "Ocean",
    pureBlackMode: Boolean = false,
    glassMode: Boolean = false,
    glassDynamic: Boolean = true,
    frostGlass: Boolean = true,
    glassBackdropStyle: String = "Translucent",
    glassOpacityValue: Float = 0.6f,
    navBarGlassOpacityValue: Float = 0.6f,
    navBarGlassLinkedToMain: Boolean = true,
    navBarGlassBackdropStyle: String = "Translucent",
    navBarGlassDynamic: Boolean = true,
    betterTexts: Boolean = false,
    betterTextsPalette: Boolean = true,
    appAnimationMode: String = "Normal",
    moreRounds: Boolean = false,
    moreRoundsMode: String = "Pastel",
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
        else -> darkTheme
    }
    val effectivePreset = if (themeColor.isNotBlank()) themeColor else themePreset

    val colorScheme = when (effectivePreset) {
        "Emerald" -> if (isDark) EmeraldDark else EmeraldLight
        "Gold" -> if (isDark) GoldDark else GoldLight
        "Rose" -> if (isDark) RoseDark else RoseLight
        "Sage" -> if (isDark) SageDark else SageLight
        "Twilight" -> if (isDark) TwilightDark else TwilightLight
        else -> if (isDark) OceanDark else OceanLight
    }

    val finalColorScheme = if (isDark && pureBlackMode) {
        colorScheme.copy(background = Color.Black, surface = Color(0xFF121212))
    } else {
        colorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(
        LocalDarkTheme provides isDark,
        LocalPureBlackMode provides pureBlackMode,
        LocalGlassMode provides glassMode,
        LocalGlassDynamic provides glassDynamic,
        LocalFrostGlass provides frostGlass,
        LocalGlassBackdropStyle provides glassBackdropStyle,
        LocalGlassOpacityValue provides glassOpacityValue,
        LocalNavBarGlassOpacityValue provides navBarGlassOpacityValue,
        LocalNavBarGlassLinkedToMain provides navBarGlassLinkedToMain,
        LocalNavBarGlassBackdropStyle provides navBarGlassBackdropStyle,
        LocalNavBarGlassDynamic provides navBarGlassDynamic,
        LocalAppAnimationMode provides appAnimationMode,
        LocalMoreRounds provides moreRounds,
        LocalMoreRoundsMode provides moreRoundsMode
    ) {
        MaterialTheme(
            colorScheme = finalColorScheme,
            content = content
        )
    }
}
