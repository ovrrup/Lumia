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
    primaryContainer = primaryContainer, onPrimaryContainer = Color(0xFF101010),
    secondary = secondary, onSecondary = Color.White,
    secondaryContainer = secondaryContainer, onSecondaryContainer = Color(0xFF101010),
    tertiary = tertiary, onTertiary = Color.White,
    tertiaryContainer = tertiaryContainer, onTertiaryContainer = Color(0xFF101010),
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
    primaryContainer = primaryContainer, onPrimaryContainer = Color(0xFFE2E2E2),
    secondary = secondary, onSecondary = Color(0xFF1A1C1A),
    secondaryContainer = secondaryContainer, onSecondaryContainer = Color(0xFFE2E2E2),
    tertiary = tertiary, onTertiary = Color.White,
    tertiaryContainer = tertiaryContainer, onTertiaryContainer = Color(0xFFE2E2E2),
    background = bg, onBackground = Color(0xFFE2E2E2),
    surface = bg, onSurface = Color(0xFFE2E2E2),
    surfaceVariant = Color(0xFF2E2E2E), onSurfaceVariant = Color(0xFFC4C7C5),
    outline = Color(0xFF8E918F), outlineVariant = Color(0xFF444746),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
    surfaceTint = primary
)

val DefaultLight = createLightScheme(Color(0xFF6750A4), Color(0xFFEADDFF), Color(0xFF625B71), Color(0xFFE8DEF8), Color(0xFF7D5260), Color(0xFFFFD8E4), Color(0xFFFFFBFE))
val DefaultDark = createDarkScheme(Color(0xFFD0BCFF), Color(0xFF4F378B), Color(0xFFCCC2DC), Color(0xFF4A4458), Color(0xFFEFB8C8), Color(0xFF633B48), Color(0xFF141218))

val OceanLight = createLightScheme(Color(0xFF006493), Color(0xFFCAE6FF), Color(0xFF50606E), Color(0xFFD3E5F5), Color(0xFF65587B), Color(0xFFEBDDFF), Color(0xFFF8FDFF))
val OceanDark = createDarkScheme(Color(0xFF8DCDFF), Color(0xFF004B70), Color(0xFFB7C9D9), Color(0xFF384956), Color(0xFFD0C1EA), Color(0xFF4D4162), Color(0xFF0F1417))

val ForestLight = createLightScheme(Color(0xFF1A6B30), Color(0xFFA4F5AE), Color(0xFF516351), Color(0xFFD3E8D2), Color(0xFF39656B), Color(0xFFBCEBF0), Color(0xFFF7FDF6))
val ForestDark = createDarkScheme(Color(0xFF89D893), Color(0xFF00531A), Color(0xFFB7CCB7), Color(0xFF394B3B), Color(0xFFA0CED4), Color(0xFF1F4D53), Color(0xFF101410))

val RoseLight = createLightScheme(Color(0xFF9C3F60), Color(0xFFFFD9E2), Color(0xFF74565F), Color(0xFFFFD9E2), Color(0xFF7C5635), Color(0xFFFFDCBE), Color(0xFFFFF8F8))
val RoseDark = createDarkScheme(Color(0xFFFFB1C8), Color(0xFF7D2848), Color(0xFFE3BDC6), Color(0xFF5B3F47), Color(0xFFEFBD94), Color(0xFF603F20), Color(0xFF1A1113))

val SunsetLight = createLightScheme(Color(0xFF9B4429), Color(0xFFFFDBD1), Color(0xFF77574E), Color(0xFFFFDBD1), Color(0xFF6C5D2F), Color(0xFFF5E1A7), Color(0xFFFFF8F6))
val SunsetDark = createDarkScheme(Color(0xFFFFB5A0), Color(0xFF7C2E15), Color(0xFFE7BDB2), Color(0xFF5D3F37), Color(0xFFD8C58D), Color(0xFF53461A), Color(0xFF1A110F))

val PurpleLight = createLightScheme(Color(0xFF7B4D9F), Color(0xFFF4D8FF), Color(0xFF675B6F), Color(0xFFEEDCFF), Color(0xFF805158), Color(0xFFFFD9DF), Color(0xFFFCF8FE))
val PurpleDark = createDarkScheme(Color(0xFFE2B6FF), Color(0xFF623485), Color(0xFFCFBFE3), Color(0xFF4E4356), Color(0xFFF3B7C1), Color(0xFF653A41), Color(0xFF141217))

@Composable
fun ScholarTheme(
    themeMode: String = "System",
    themeColor: String = "Default",
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "Dark" -> true
        "Light" -> false
        else -> darkTheme
    }

    val colorScheme = when (themeColor) {
        "Ocean" -> if (isDark) OceanDark else OceanLight
        "Forest" -> if (isDark) ForestDark else ForestLight
        "Rose" -> if (isDark) RoseDark else RoseLight
        "Sunset" -> if (isDark) SunsetDark else SunsetLight
        "Purple" -> if (isDark) PurpleDark else PurpleLight
        else -> if (isDark) DefaultDark else DefaultLight
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
