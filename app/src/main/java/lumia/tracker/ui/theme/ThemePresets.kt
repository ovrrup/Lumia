package lumia.tracker.ui.theme

import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color

fun Color.mix(other: Color, weight: Float): Color {
    return Color(
        red = this.red * weight + other.red * (1f - weight),
        green = this.green * weight + other.green * (1f - weight),
        blue = this.blue * weight + other.blue * (1f - weight),
        alpha = this.alpha * weight + other.alpha * (1f - weight)
    )
}

fun createLightScheme(
    primary: Color, primaryContainer: Color,
    secondary: Color, secondaryContainer: Color,
    tertiary: Color, tertiaryContainer: Color,
    bg: Color = Color(0xFFF2F2F7),
    surface: Color = Color(0xFFFFFFFF),
    onSurfaceText: Color = Color(0xFF1C1C1E)
) = lightColorScheme(
    primary = primary, onPrimary = Color.White,
    primaryContainer = primaryContainer, onPrimaryContainer = Color.Black.mix(primaryContainer, 0.85f),
    secondary = secondary, onSecondary = Color.White,
    secondaryContainer = secondaryContainer, onSecondaryContainer = Color.Black.mix(secondaryContainer, 0.85f),
    tertiary = tertiary, onTertiary = Color.White,
    tertiaryContainer = tertiaryContainer, onTertiaryContainer = Color.Black.mix(tertiaryContainer, 0.85f),
    background = bg, onBackground = onSurfaceText,
    surface = surface, onSurface = onSurfaceText,
    surfaceVariant = Color(0xFFE5E5EA).mix(bg, 0.5f), onSurfaceVariant = onSurfaceText.mix(bg, 0.35f),
    outline = Color(0xFF8E8E93).mix(bg, 0.5f), outlineVariant = Color(0xFFC7C7CC).mix(bg, 0.5f),
    error = Color(0xFFFF3B30), onError = Color.White,
    errorContainer = Color(0xFFFFE5E5), onErrorContainer = Color(0xFF800000),
    surfaceTint = primary
)

fun createDarkScheme(
    primary: Color, primaryContainer: Color,
    secondary: Color, secondaryContainer: Color,
    tertiary: Color, tertiaryContainer: Color,
    bg: Color = Color(0xFF000000),
    surface: Color = Color(0xFF1C1C1E),
    onSurfaceText: Color = Color(0xFFF2F2F7)
) = darkColorScheme(
    primary = primary, onPrimary = Color(0xFF000000),
    primaryContainer = primaryContainer, onPrimaryContainer = Color.White.mix(primaryContainer, 0.85f),
    secondary = secondary, onSecondary = Color(0xFF000000),
    secondaryContainer = secondaryContainer, onSecondaryContainer = Color.White.mix(secondaryContainer, 0.85f),
    tertiary = tertiary, onTertiary = Color(0xFF000000),
    tertiaryContainer = tertiaryContainer, onTertiaryContainer = Color.White.mix(tertiaryContainer, 0.85f),
    background = bg, onBackground = onSurfaceText,
    surface = surface, onSurface = onSurfaceText,
    surfaceVariant = Color(0xFF2C2C2E).mix(bg, 0.5f), onSurfaceVariant = onSurfaceText.mix(bg, 0.35f),
    outline = Color(0xFF8E8E93).mix(bg, 0.5f), outlineVariant = Color(0xFF3A3A3C).mix(bg, 0.5f),
    error = Color(0xFFFF453A), onError = Color.Black,
    errorContainer = Color(0xFF4A0002), onErrorContainer = Color(0xFFFFD6D6),
    surfaceTint = primary
)

val OceanLight = createLightScheme(
    primary = Color(0xFF0061A4), primaryContainer = Color(0xFFD1E4FF), 
    secondary = Color(0xFF535F70), secondaryContainer = Color(0xFFD7E3F7), 
    tertiary = Color(0xFF6B5778), tertiaryContainer = Color(0xFFF2DAFF)
)
val OceanDark = createDarkScheme(
    primary = Color(0xFF9ECAFF), primaryContainer = Color(0xFF00497D), 
    secondary = Color(0xFFBBC7DB), secondaryContainer = Color(0xFF3B4858), 
    tertiary = Color(0xFFD6BEE4), tertiaryContainer = Color(0xFF523F5F)
)

val EmeraldLight = createLightScheme(
    primary = Color(0xFF006D36), primaryContainer = Color(0xFF95F9B7), 
    secondary = Color(0xFF506353), secondaryContainer = Color(0xFFD2E8D3), 
    tertiary = Color(0xFF3A656F), tertiaryContainer = Color(0xFFBDEAF6)
)
val EmeraldDark = createDarkScheme(
    primary = Color(0xFF79DC9C), primaryContainer = Color(0xFF005227), 
    secondary = Color(0xFFB6CCB8), secondaryContainer = Color(0xFF384B3C), 
    tertiary = Color(0xFFA1CED9), tertiaryContainer = Color(0xFF204D56)
)

val GoldLight = createLightScheme(
    primary = Color(0xFF7D5700), primaryContainer = Color(0xFFFFDE9C), 
    secondary = Color(0xFF6C5D3F), secondaryContainer = Color(0xFFF5E0BB), 
    tertiary = Color(0xFF4B6546), tertiaryContainer = Color(0xFFBCDEB2)
)
val GoldDark = createDarkScheme(
    primary = Color(0xFFFABD00), primaryContainer = Color(0xFF5E4000), 
    secondary = Color(0xFFD8C4A0), secondaryContainer = Color(0xFF53452A), 
    tertiary = Color(0xFFB1CEA8), tertiaryContainer = Color(0xFF344D30)
)

val RoseLight = createLightScheme(
    primary = Color(0xFFBF0031), primaryContainer = Color(0xFFFFDAD9), 
    secondary = Color(0xFF775656), secondaryContainer = Color(0xFFFFDAD9), 
    tertiary = Color(0xFF755A2F), tertiaryContainer = Color(0xFFFFDDAF)
)
val RoseDark = createDarkScheme(
    primary = Color(0xFFFFB3B4), primaryContainer = Color(0xFF8E0021), 
    secondary = Color(0xFFE7BDBE), secondaryContainer = Color(0xFF5D3F3F), 
    tertiary = Color(0xFFE5C18D), tertiaryContainer = Color(0xFF5C421A)
)

val SageLight = createLightScheme(
    primary = Color(0xFF3B6939), primaryContainer = Color(0xFFBCF0B4), 
    secondary = Color(0xFF53634F), secondaryContainer = Color(0xFFD6E8CE), 
    tertiary = Color(0xFF38656A), tertiaryContainer = Color(0xFFBCEBF0)
)
val SageDark = createDarkScheme(
    primary = Color(0xFFA1D39A), primaryContainer = Color(0xFF225024), 
    secondary = Color(0xFFBACCB3), secondaryContainer = Color(0xFF3C4B38), 
    tertiary = Color(0xFFA0CFD4), tertiaryContainer = Color(0xFF1F4D52)
)

val TwilightLight = createLightScheme(
    primary = Color(0xFF5B53A8), primaryContainer = Color(0xFFE3DFFF), 
    secondary = Color(0xFF5D5D72), secondaryContainer = Color(0xFFE3E0F9), 
    tertiary = Color(0xFF795369), tertiaryContainer = Color(0xFFFFD8EC)
)
val TwilightDark = createDarkScheme(
    primary = Color(0xFFC4C0FF), primaryContainer = Color(0xFF433B8E), 
    secondary = Color(0xFFC7C4DC), secondaryContainer = Color(0xFF454559), 
    tertiary = Color(0xFFEBB9D6), tertiaryContainer = Color(0xFF5F3C51)
)
