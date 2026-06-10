package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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

val BlueLight = createLightScheme(Color(0xFF0085FF), Color(0xFF006ACC), Color(0xFF004F99), Color(0xFF003566), Color(0xFF004F99), Color(0xFF006ACC))
val BlueDark = createDarkScheme(Color(0xFF168FFF), Color(0xFF44A5FF), Color(0xFF73BBFF), Color(0xFFA1D2FF), Color(0xFF73BBFF), Color(0xFF44A5FF))

val GreenLight = createLightScheme(Color(0xFF00BA34), Color(0xFF009429), Color(0xFF006F1F), Color(0xFF004A14), Color(0xFF006F1F), Color(0xFF009429))
val GreenDark = createDarkScheme(Color(0xFF17C849), Color(0xFF45D36D), Color(0xFF73DE91), Color(0xFFA2E9B6), Color(0xFF73DE91), Color(0xFF45D36D))

val OrangeLight = createLightScheme(Color(0xFFF98600), Color(0xFFC76B00), Color(0xFF955000), Color(0xFF633500), Color(0xFF955000), Color(0xFFC76B00))
val OrangeDark = createDarkScheme(Color(0xFFFF9F2D), Color(0xFFFFB257), Color(0xFFFFC581), Color(0xFFFFD8AB), Color(0xFFFFC581), Color(0xFFFFB257))

val RedLight = createLightScheme(Color(0xFFE92C2C), Color(0xFFBA2323), Color(0xFF8B1A1A), Color(0xFF5D1111), Color(0xFF8B1A1A), Color(0xFFBA2323))
val RedDark = createDarkScheme(Color(0xFFF74141), Color(0xFFF86767), Color(0xFFFA8D8D), Color(0xFFFBB3B3), Color(0xFFFA8D8D), Color(0xFFF86767))

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

    var colorScheme = when (themeColor) {
        "Blue" -> if (isDark) BlueDark else BlueLight
        "Green" -> if (isDark) GreenDark else GreenLight
        "Orange" -> if (isDark) OrangeDark else OrangeLight
        "Red" -> if (isDark) RedDark else RedLight
        else -> if (isDark) BlueDark else BlueLight
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
            var context = view.context
            while (context is android.content.ContextWrapper && context !is Activity) {
                context = context.baseContext
            }
            if (context is Activity) {
                val window = context.window
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
