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
    bg: Color = Color(0xFFFCFCFC)
) = lightColorScheme(
    primary = primary, onPrimary = Color.White,
    primaryContainer = primaryContainer, onPrimaryContainer = Color.White,
    secondary = secondary, onSecondary = Color.White,
    secondaryContainer = secondaryContainer, onSecondaryContainer = Color.White,
    tertiary = tertiary, onTertiary = Color.White,
    tertiaryContainer = tertiaryContainer, onTertiaryContainer = Color.White,
    background = bg, onBackground = Color(0xFF1A1C1A),
    surface = bg, onSurface = Color(0xFF1A1C1A),
    surfaceVariant = Color(0xFFEAEAEA), onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E), outlineVariant = Color(0xFFCAC4D0),
    error = Color(0xFFBA1A1A), onError = Color.White,
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
    surfaceTint = primary
)

fun createDarkScheme(
    primary: Color, primaryContainer: Color,
    secondary: Color, secondaryContainer: Color,
    tertiary: Color, tertiaryContainer: Color,
    bg: Color = Color(0xFF121212)
) = darkColorScheme(
    primary = primary, onPrimary = Color(0xFF1A1C1A),
    primaryContainer = primaryContainer, onPrimaryContainer = Color(0xFF1A1C1A),
    secondary = secondary, onSecondary = Color(0xFF1A1C1A),
    secondaryContainer = secondaryContainer, onSecondaryContainer = Color(0xFF1A1C1A),
    tertiary = tertiary, onTertiary = Color(0xFF1A1C1A),
    tertiaryContainer = tertiaryContainer, onTertiaryContainer = Color(0xFF1A1C1A),
    background = bg, onBackground = Color(0xFFE2E2E2),
    surface = bg, onSurface = Color(0xFFE2E2E2),
    surfaceVariant = Color(0xFF2E2E2E), onSurfaceVariant = Color(0xFFC4C7C5),
    outline = Color(0xFF8E918F), outlineVariant = Color(0xFF444746),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
    surfaceTint = primary
)

val BlueLight = createLightScheme(Color(0xFF0085FF), Color(0xFFD0E4FF), Color(0xFF004F99), Color(0xFFD2E4FF), Color(0xFF004F99), Color(0xFFD2E4FF))
val BlueDark = createDarkScheme(Color(0xFF8CBFFF), Color(0xFF004F99), Color(0xFFA1CADF), Color(0xFF004F99), Color(0xFFA1CADF), Color(0xFF004F99))

val GreenLight = createLightScheme(Color(0xFF00BA34), Color(0xFF8FFFA9), Color(0xFF006F1F), Color(0xFF91FDB2), Color(0xFF006F1F), Color(0xFF91FDB2))
val GreenDark = createDarkScheme(Color(0xFF56E074), Color(0xFF006F1F), Color(0xFF8CE09A), Color(0xFF006F1F), Color(0xFF8CE09A), Color(0xFF006F1F))

val OrangeLight = createLightScheme(Color(0xFFF98600), Color(0xFFFFDAB6), Color(0xFF955000), Color(0xFFFFDCBE), Color(0xFF955000), Color(0xFFFFDCBE))
val OrangeDark = createDarkScheme(Color(0xFFFFB482), Color(0xFF955000), Color(0xFFFFBE90), Color(0xFF955000), Color(0xFFFFBE90), Color(0xFF955000))

val RedLight = createLightScheme(Color(0xFFE92C2C), Color(0xFFFFDAD6), Color(0xFF8B1A1A), Color(0xFFFFDBDD), Color(0xFF8B1A1A), Color(0xFFFFDBDD))
val RedDark = createDarkScheme(Color(0xFFFFB4A9), Color(0xFF8B1A1A), Color(0xFFFFB3B8), Color(0xFF8B1A1A), Color(0xFFFFB3B8), Color(0xFF8B1A1A))

@Composable
fun ScholarTheme(
    themeMode: String = "System",
    themeColor: String = "Default",
    pureBlackMode: Boolean = false,
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
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> run {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> when (themeColor) {
            "Blue" -> if (isDark) BlueDark else BlueLight
            "Green" -> if (isDark) GreenDark else GreenLight
            "Orange" -> if (isDark) OrangeDark else OrangeLight
            "Red" -> if (isDark) RedDark else RedLight
            else -> if (isDark) BlueDark else BlueLight
        }
    }

    if (isDark && pureBlackMode) {
        colorScheme = colorScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = Color(0xFF1E1E1E)
        )
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
