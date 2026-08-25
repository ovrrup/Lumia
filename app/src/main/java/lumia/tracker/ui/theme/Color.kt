package lumia.tracker.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Color - Lumia Academic Tracker Theme & Color Engine tokens.
 * Provides perceptual color transformations, Material You dynamic color harmonization,
 * OLED pure black surface tokens, and pre-computed high-performance color schemes.
 */

// ============================================================================
// 1. OLED PURE BLACK ARCHITECTURAL TOKENS
// ============================================================================

/** True AMOLED #000000 pure black background token for zero-power display pixels. */
@ValueScore(score = 96, importance = Importance.CRITICAL, description = "Pure AMOLED 0x000000 base background token", category = "Theme")
val OledBackground = Color(0xFF000000)

/** OLED pure black surface token with minimal perceptible luminance for elevated panels. */
@ValueScore(score = 92, importance = Importance.HIGH, description = "OLED surface card token", category = "Theme")
val OledSurface = Color(0xFF0E0E10)

/** OLED surface variant token for sub-containers and header bars. */
@ValueScore(score = 90, importance = Importance.HIGH, description = "OLED surface variant token", category = "Theme")
val OledSurfaceVariant = Color(0xFF18181B)

/** OLED surface container token (Layer 1 elevation on pure black). */
@ValueScore(score = 91, importance = Importance.HIGH, description = "OLED surface container level 1", category = "Theme")
val OledContainerLowest = Color(0xFF000000)

@ValueScore(score = 91, importance = Importance.HIGH, description = "OLED surface container level 2", category = "Theme")
val OledContainerLow = Color(0xFF09090B)

@ValueScore(score = 92, importance = Importance.HIGH, description = "OLED surface container level 3", category = "Theme")
val OledContainer = Color(0xFF121215)

@ValueScore(score = 91, importance = Importance.HIGH, description = "OLED surface container level 4", category = "Theme")
val OledContainerHigh = Color(0xFF1D1D21)

@ValueScore(score = 90, importance = Importance.HIGH, description = "OLED surface container level 5", category = "Theme")
val OledContainerHighest = Color(0xFF27272C)

/** OLED outline token for crisp boundaries without light bleed. */
@ValueScore(score = 88, importance = Importance.MEDIUM, description = "OLED high contrast outline token", category = "Theme")
val OledOutline = Color(0xFF333338)

@ValueScore(score = 86, importance = Importance.MEDIUM, description = "OLED subtle outline variant token", category = "Theme")
val OledOutlineVariant = Color(0xFF222226)

// ============================================================================
// 2. MATHEMATICAL & PERCEPTUAL COLOR UTILITIES
// ============================================================================

/**
 * Mixes two colors linearly by the specified ratio [weight].
 */
@ValueScore(score = 85, importance = Importance.MEDIUM, description = "Linear RGBA color mixing helper", category = "Theme")
fun Color.mix(other: Color, weight: Float): Color {
    val clampedWeight = weight.coerceIn(0f, 1f)
    return Color(
        red = this.red * clampedWeight + other.red * (1f - clampedWeight),
        green = this.green * clampedWeight + other.green * (1f - clampedWeight),
        blue = this.blue * clampedWeight + other.blue * (1f - clampedWeight),
        alpha = this.alpha * clampedWeight + other.alpha * (1f - clampedWeight)
    )
}

/**
 * Calculates standard ITU-R BT.709 perceived luminance of this color.
 */
@ValueScore(score = 88, importance = Importance.HIGH, description = "Perceptual luminance calculator", category = "Theme")
fun Color.perceivedLuminance(): Float {
    return this.red * 0.2126f + this.green * 0.7152f + this.blue * 0.0722f
}

/**
 * Calculates WCAG contrast ratio between this color and [other].
 */
@ValueScore(score = 86, importance = Importance.HIGH, description = "WCAG contrast ratio calculator", category = "Theme")
fun Color.contrastRatioWith(other: Color): Float {
    val l1 = max(this.perceivedLuminance(), other.perceivedLuminance())
    val l2 = min(this.perceivedLuminance(), other.perceivedLuminance())
    return (l1 + 0.05f) / (l2 + 0.05f)
}

/**
 * Converts a Color to standard 6-digit or 8-digit hexadecimal string.
 */
@ValueScore(score = 80, importance = Importance.LOW, description = "Color to hex converter", category = "Theme")
fun Color.toHexString(includeAlpha: Boolean = false): String {
    val argb = this.toArgb()
    return if (includeAlpha) {
        String.format("#%08X", argb)
    } else {
        String.format("#%06X", 0xFFFFFF and argb)
    }
}

/**
 * Safely parses a hexadecimal color string with resilient fallback on error.
 */
@ValueScore(score = 87, importance = Importance.HIGH, description = "Safe hex color parser with fallback", category = "Theme")
fun safeParseColor(hex: String, fallback: Color): Color {
    if (hex.isBlank()) return fallback
    val formatted = hex.trim().let { if (!it.startsWith("#")) "#$it" else it }
    return try {
        Color(android.graphics.Color.parseColor(formatted))
    } catch (_: Exception) {
        fallback
    }
}

/**
 * Harmonizes a target color towards a source color using Material You hue shifting.
 * Rotates the hue of this color towards [source] by [intensity] without losing its intrinsic chroma.
 */
@ValueScore(score = 94, importance = Importance.HIGH, description = "Material You dynamic hue harmonizer", category = "Theme")
fun Color.harmonizeWith(source: Color, intensity: Float = 0.5f): Color {
    val targetHsv = FloatArray(3)
    val sourceHsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), targetHsv)
    android.graphics.Color.colorToHSV(source.toArgb(), sourceHsv)

    var diff = sourceHsv[0] - targetHsv[0]
    if (diff > 180f) diff -= 360f
    if (diff < -180f) diff += 360f

    val rotation = diff * intensity.coerceIn(0f, 1f)
    val newHue = (targetHsv[0] + rotation + 360f) % 360f

    val harmonizedHsv = floatArrayOf(
        newHue,
        (targetHsv[1] * 0.9f + sourceHsv[1] * 0.1f).coerceIn(0f, 1f),
        targetHsv[2]
    )
    val rgb = android.graphics.Color.HSVToColor(harmonizedHsv)
    return Color(rgb).copy(alpha = this.alpha)
}

// ============================================================================
// 3. COLOR SCHEME GENERATOR FACTORIES
// ============================================================================

/**
 * Constructs a comprehensive Material 3 Light ColorScheme with calibrated surface steps.
 */
@ValueScore(score = 93, importance = Importance.HIGH, description = "Material 3 Light ColorScheme builder", category = "Theme")
fun createLightScheme(
    primary: Color, primaryContainer: Color,
    secondary: Color, secondaryContainer: Color,
    tertiary: Color, tertiaryContainer: Color,
    bg: Color = Color(0xFFF6F6F9),
    surface: Color = Color(0xFFFFFFFF),
    onSurfaceText: Color = Color(0xFF1C1C1E)
): ColorScheme = lightColorScheme(
    primary = primary,
    onPrimary = Color.White,
    primaryContainer = primaryContainer,
    onPrimaryContainer = Color.Black.mix(primaryContainer, 0.85f),
    inversePrimary = primary.mix(Color.White, 0.7f),
    secondary = secondary,
    onSecondary = Color.White,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = Color.Black.mix(secondaryContainer, 0.85f),
    tertiary = tertiary,
    onTertiary = Color.White,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = Color.Black.mix(tertiaryContainer, 0.85f),
    background = bg,
    onBackground = onSurfaceText,
    surface = surface,
    onSurface = onSurfaceText,
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF7C7C82),
    surfaceTint = primary,
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F8FC),
    surfaceContainer = surface,
    surfaceContainerHigh = Color(0xFFEEEEF2),
    surfaceContainerHighest = Color(0xFFE5E5EB),
    outline = Color(0xFFD1D1D6),
    outlineVariant = Color(0xFFE5E5EA),
    error = Color(0xFFFF3B30),
    onError = Color.White,
    errorContainer = Color(0xFFFFE5E5),
    onErrorContainer = Color(0xFFD70015)
)

/**
 * Constructs a comprehensive Material 3 Dark ColorScheme with rich deep charcoal layers.
 */
@ValueScore(score = 93, importance = Importance.HIGH, description = "Material 3 Dark ColorScheme builder", category = "Theme")
fun createDarkScheme(
    primary: Color, primaryContainer: Color,
    secondary: Color, secondaryContainer: Color,
    tertiary: Color, tertiaryContainer: Color,
    bg: Color = Color(0xFF121214),
    surface: Color = Color(0xFF1C1C1E),
    onSurfaceText: Color = Color(0xFFFFFFFF)
): ColorScheme = darkColorScheme(
    primary = primary,
    onPrimary = Color.Black.mix(primary, 0.85f),
    primaryContainer = primaryContainer,
    onPrimaryContainer = Color.White.mix(primaryContainer, 0.88f),
    inversePrimary = primary.mix(Color.Black, 0.7f),
    secondary = secondary,
    onSecondary = Color.Black.mix(secondary, 0.85f),
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = Color.White.mix(secondaryContainer, 0.88f),
    tertiary = tertiary,
    onTertiary = Color.Black.mix(tertiary, 0.85f),
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = Color.White.mix(tertiaryContainer, 0.88f),
    background = bg,
    onBackground = onSurfaceText,
    surface = surface,
    onSurface = onSurfaceText,
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFA1A1A6),
    surfaceTint = primary,
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),
    surfaceContainerLowest = Color(0xFF0C0C0E),
    surfaceContainerLow = Color(0xFF151517),
    surfaceContainer = surface,
    surfaceContainerHigh = Color(0xFF252528),
    surfaceContainerHighest = Color(0xFF303034),
    outline = Color(0xFF3E3E42),
    outlineVariant = Color(0xFF2C2C2E),
    error = Color(0xFFFF453A),
    onError = Color.White,
    errorContainer = Color(0xFF5E110E),
    onErrorContainer = Color(0xFFFFB4AB)
)

/**
 * Constructs an OLED Pure Black ColorScheme with 0-lum background and tuned surface steps.
 */
@ValueScore(score = 95, importance = Importance.CRITICAL, description = "Material 3 OLED Pure Black ColorScheme builder", category = "Theme")
fun createOledDarkScheme(
    primary: Color, primaryContainer: Color,
    secondary: Color, secondaryContainer: Color,
    tertiary: Color, tertiaryContainer: Color
): ColorScheme = darkColorScheme(
    primary = primary,
    onPrimary = Color.Black,
    primaryContainer = primaryContainer,
    onPrimaryContainer = Color.White.mix(primaryContainer, 0.90f),
    inversePrimary = primary.mix(Color.Black, 0.7f),
    secondary = secondary,
    onSecondary = Color.Black,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = Color.White.mix(secondaryContainer, 0.90f),
    tertiary = tertiary,
    onTertiary = Color.Black,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = Color.White.mix(tertiaryContainer, 0.90f),
    background = OledBackground,
    onBackground = Color(0xFFF4F4F6),
    surface = OledSurface,
    onSurface = Color(0xFFF4F4F6),
    surfaceVariant = OledSurfaceVariant,
    onSurfaceVariant = Color(0xFFA6A6AC),
    surfaceTint = primary,
    inverseSurface = Color(0xFFE4E4E8),
    inverseOnSurface = Color(0xFF1C1C1E),
    surfaceContainerLowest = OledContainerLowest,
    surfaceContainerLow = OledContainerLow,
    surfaceContainer = OledContainer,
    surfaceContainerHigh = OledContainerHigh,
    surfaceContainerHighest = OledContainerHighest,
    outline = OledOutline,
    outlineVariant = OledOutlineVariant,
    error = Color(0xFFFF453A),
    onError = Color.White,
    errorContainer = Color(0xFF5E110E),
    onErrorContainer = Color(0xFFFFB4AB)
)

// ============================================================================
// 4. CURATED PALETTE DEFINITIONS
// ============================================================================

// Ocean (Lumia Academic Sapphire)
val OceanLight = createLightScheme(
    primary = Color(0xFF007AFF), primaryContainer = Color(0xFFD0E6FF),
    secondary = Color(0xFF5856D6), secondaryContainer = Color(0xFFE5E4FF),
    tertiary = Color(0xFF34C759), tertiaryContainer = Color(0xFFD5F5DD)
)
val OceanDark = createDarkScheme(
    primary = Color(0xFF0A84FF), primaryContainer = Color(0xFF004080),
    secondary = Color(0xFF5E5CE6), secondaryContainer = Color(0xFF282766),
    tertiary = Color(0xFF30D158), tertiaryContainer = Color(0xFF0E5420)
)
val OceanOled = createOledDarkScheme(
    primary = Color(0xFF0A84FF), primaryContainer = Color(0xFF003366),
    secondary = Color(0xFF5E5CE6), secondaryContainer = Color(0xFF1E1E4D),
    tertiary = Color(0xFF30D158), tertiaryContainer = Color(0xFF0B4219)
)

// Emerald (Deep Study Green)
val EmeraldLight = createLightScheme(
    primary = Color(0xFF2E9E4E), primaryContainer = Color(0xFFD5F5DD),
    secondary = Color(0xFF30B0C7), secondaryContainer = Color(0xFFD5F3F7),
    tertiary = Color(0xFF007AFF), tertiaryContainer = Color(0xFFD0E6FF)
)
val EmeraldDark = createDarkScheme(
    primary = Color(0xFF30D158), primaryContainer = Color(0xFF0E5420),
    secondary = Color(0xFF40CBE0), secondaryContainer = Color(0xFF104B54),
    tertiary = Color(0xFF0A84FF), tertiaryContainer = Color(0xFF004080)
)
val EmeraldOled = createOledDarkScheme(
    primary = Color(0xFF30D158), primaryContainer = Color(0xFF0B4219),
    secondary = Color(0xFF40CBE0), secondaryContainer = Color(0xFF0D3B42),
    tertiary = Color(0xFF0A84FF), tertiaryContainer = Color(0xFF003366)
)

// Gold (Scholar Harvest Amber)
val GoldLight = createLightScheme(
    primary = Color(0xFFE08200), primaryContainer = Color(0xFFFFECC4),
    secondary = Color(0xFFFF2D55), secondaryContainer = Color(0xFFFFD5DD),
    tertiary = Color(0xFF34C759), tertiaryContainer = Color(0xFFD5F5DD)
)
val GoldDark = createDarkScheme(
    primary = Color(0xFFFF9F0A), primaryContainer = Color(0xFF6B3E00),
    secondary = Color(0xFFFF375F), secondaryContainer = Color(0xFF660E1F),
    tertiary = Color(0xFF30D158), tertiaryContainer = Color(0xFF0E5420)
)
val GoldOled = createOledDarkScheme(
    primary = Color(0xFFFF9F0A), primaryContainer = Color(0xFF543100),
    secondary = Color(0xFFFF375F), secondaryContainer = Color(0xFF4D0A17),
    tertiary = Color(0xFF30D158), tertiaryContainer = Color(0xFF0B4219)
)

// Rose (Vibrant Velvet Crimson)
val RoseLight = createLightScheme(
    primary = Color(0xFFD91E45), primaryContainer = Color(0xFFFFD5DD),
    secondary = Color(0xFFAF52DE), secondaryContainer = Color(0xFFF2DCFA),
    tertiary = Color(0xFFFF9500), tertiaryContainer = Color(0xFFFFECC4)
)
val RoseDark = createDarkScheme(
    primary = Color(0xFFFF375F), primaryContainer = Color(0xFF660E1F),
    secondary = Color(0xFFBF5AF2), secondaryContainer = Color(0xFF4B1E66),
    tertiary = Color(0xFFFF9F0A), tertiaryContainer = Color(0xFF6B3E00)
)
val RoseOled = createOledDarkScheme(
    primary = Color(0xFFFF375F), primaryContainer = Color(0xFF4D0A17),
    secondary = Color(0xFFBF5AF2), secondaryContainer = Color(0xFF3A174F),
    tertiary = Color(0xFFFF9F0A), tertiaryContainer = Color(0xFF543100)
)

// Sage (Nordic Eucalyptus & Slate)
val SageLight = createLightScheme(
    primary = Color(0xFF2898AB), primaryContainer = Color(0xFFD5F3F7),
    secondary = Color(0xFF34C759), secondaryContainer = Color(0xFFD5F5DD),
    tertiary = Color(0xFF5856D6), tertiaryContainer = Color(0xFFE5E4FF)
)
val SageDark = createDarkScheme(
    primary = Color(0xFF40CBE0), primaryContainer = Color(0xFF104B54),
    secondary = Color(0xFF30D158), secondaryContainer = Color(0xFF0E5420),
    tertiary = Color(0xFF5E5CE6), tertiaryContainer = Color(0xFF282766)
)
val SageOled = createOledDarkScheme(
    primary = Color(0xFF40CBE0), primaryContainer = Color(0xFF0D3B42),
    secondary = Color(0xFF30D158), secondaryContainer = Color(0xFF0B4219),
    tertiary = Color(0xFF5E5CE6), tertiaryContainer = Color(0xFF1E1E4D)
)

// Twilight (Cosmic Iris & Lavender)
val TwilightLight = createLightScheme(
    primary = Color(0xFF5856D6), primaryContainer = Color(0xFFE5E4FF),
    secondary = Color(0xFFAF52DE), secondaryContainer = Color(0xFFF2DCFA),
    tertiary = Color(0xFF007AFF), tertiaryContainer = Color(0xFFD0E6FF)
)
val TwilightDark = createDarkScheme(
    primary = Color(0xFF5E5CE6), primaryContainer = Color(0xFF282766),
    secondary = Color(0xFFBF5AF2), secondaryContainer = Color(0xFF4B1E66),
    tertiary = Color(0xFF0A84FF), tertiaryContainer = Color(0xFF004080)
)
val TwilightOled = createOledDarkScheme(
    primary = Color(0xFF5E5CE6), primaryContainer = Color(0xFF1E1E4D),
    secondary = Color(0xFFBF5AF2), secondaryContainer = Color(0xFF3A174F),
    tertiary = Color(0xFF0A84FF), tertiaryContainer = Color(0xFF003366)
)

// Amethyst (Scholastic Deep Violet)
val AmethystLight = createLightScheme(
    primary = Color(0xFF7B2CBF), primaryContainer = Color(0xFFF0DCFF),
    secondary = Color(0xFF9D4EDD), secondaryContainer = Color(0xFFF5E8FF),
    tertiary = Color(0xFF3A86FF), tertiaryContainer = Color(0xFFD8E7FF)
)
val AmethystDark = createDarkScheme(
    primary = Color(0xFF9D4EDD), primaryContainer = Color(0xFF44126E),
    secondary = Color(0xFFC77DFF), secondaryContainer = Color(0xFF591C8C),
    tertiary = Color(0xFF60A5FA), tertiaryContainer = Color(0xFF1E3A8A)
)
val AmethystOled = createOledDarkScheme(
    primary = Color(0xFF9D4EDD), primaryContainer = Color(0xFF330D54),
    secondary = Color(0xFFC77DFF), secondaryContainer = Color(0xFF421568),
    tertiary = Color(0xFF60A5FA), tertiaryContainer = Color(0xFF172D6B)
)

// Sunset (Radiant Solar Terracotta)
val SunsetLight = createLightScheme(
    primary = Color(0xFFE65100), primaryContainer = Color(0xFFFFE0B2),
    secondary = Color(0xFFFF7043), secondaryContainer = Color(0xFFFFCCBC),
    tertiary = Color(0xFFFFB300), tertiaryContainer = Color(0xFFFFECB3)
)
val SunsetDark = createDarkScheme(
    primary = Color(0xFFFF8A65), primaryContainer = Color(0xFF7E2A10),
    secondary = Color(0xFFFFB74D), secondaryContainer = Color(0xFF7A4A0A),
    tertiary = Color(0xFFFFD54F), tertiaryContainer = Color(0xFF7C630A)
)
val SunsetOled = createOledDarkScheme(
    primary = Color(0xFFFF8A65), primaryContainer = Color(0xFF5F200C),
    secondary = Color(0xFFFFB74D), secondaryContainer = Color(0xFF5C3707),
    tertiary = Color(0xFFFFD54F), tertiaryContainer = Color(0xFF5E4B07)
)

// Cyberpunk (High-Tech Electric Cyan)
val CyberpunkLight = createLightScheme(
    primary = Color(0xFF0097A7), primaryContainer = Color(0xFFB2EBF2),
    secondary = Color(0xFF00BFA5), secondaryContainer = Color(0xFFA7FFEB),
    tertiary = Color(0xFF7C4DFF), tertiaryContainer = Color(0xFFEDE7F6)
)
val CyberpunkDark = createDarkScheme(
    primary = Color(0xFF00E5FF), primaryContainer = Color(0xFF004D5A),
    secondary = Color(0xFF1DE9B6), secondaryContainer = Color(0xFF005244),
    tertiary = Color(0xFFB388FF), tertiaryContainer = Color(0xFF371D75)
)
val CyberpunkOled = createOledDarkScheme(
    primary = Color(0xFF00E5FF), primaryContainer = Color(0xFF003842),
    secondary = Color(0xFF1DE9B6), secondaryContainer = Color(0xFF003D33),
    tertiary = Color(0xFFB388FF), tertiaryContainer = Color(0xFF281554)
)

// Monochrome (Minimalist Obsidian & Slate)
val MonochromeLight = createLightScheme(
    primary = Color(0xFF475569), primaryContainer = Color(0xFFE2E8F0),
    secondary = Color(0xFF64748B), secondaryContainer = Color(0xFFF1F5F9),
    tertiary = Color(0xFF334155), tertiaryContainer = Color(0xFFCBD5E1)
)
val MonochromeDark = createDarkScheme(
    primary = Color(0xFF94A3B8), primaryContainer = Color(0xFF1E293B),
    secondary = Color(0xFFCBD5E1), secondaryContainer = Color(0xFF334155),
    tertiary = Color(0xFFE2E8F0), tertiaryContainer = Color(0xFF475569)
)
val MonochromeOled = createOledDarkScheme(
    primary = Color(0xFF94A3B8), primaryContainer = Color(0xFF161E2E),
    secondary = Color(0xFFCBD5E1), secondaryContainer = Color(0xFF252F3E),
    tertiary = Color(0xFFE2E8F0), tertiaryContainer = Color(0xFF364152)
)

/**
 * Data representation of a curated Lumia theme preset.
 */
@ValueScore(score = 90, importance = Importance.HIGH, description = "Lumia theme preset metadata holder", category = "Theme")
data class LumiaThemePreset(
    val id: String,
    val name: String,
    val description: String,
    val lightScheme: ColorScheme,
    val darkScheme: ColorScheme,
    val oledDarkScheme: ColorScheme,
    val previewColor: Color,
    val secondaryPreview: Color
)

/**
 * Registry of all built-in Lumia theme swatches.
 */
@ValueScore(score = 92, importance = Importance.HIGH, description = "Comprehensive theme preset registry", category = "Theme")
val LumiaThemePresets: List<LumiaThemePreset> = listOf(
    LumiaThemePreset("Ocean", "Ocean", "Academic sapphire & electric cyan", OceanLight, OceanDark, OceanOled, Color(0xFF007AFF), Color(0xFF5856D6)),
    LumiaThemePreset("Emerald", "Emerald", "Study forest & mint growth", EmeraldLight, EmeraldDark, EmeraldOled, Color(0xFF34C759), Color(0xFF30B0C7)),
    LumiaThemePreset("Gold", "Gold", "Scholar amber & harvest sunshine", GoldLight, GoldDark, GoldOled, Color(0xFFFF9500), Color(0xFFFF2D55)),
    LumiaThemePreset("Rose", "Rose", "Velvet crimson & high energy", RoseLight, RoseDark, RoseOled, Color(0xFFFF2D55), Color(0xFFAF52DE)),
    LumiaThemePreset("Sage", "Sage", "Nordic eucalyptus & seafoam slate", SageLight, SageDark, SageOled, Color(0xFF30B0C7), Color(0xFF34C759)),
    LumiaThemePreset("Twilight", "Twilight", "Cosmic deep violet & lavender iris", TwilightLight, TwilightDark, TwilightOled, Color(0xFF5856D6), Color(0xFFAF52DE)),
    LumiaThemePreset("Amethyst", "Amethyst", "Creative intellectual purple", AmethystLight, AmethystDark, AmethystOled, Color(0xFF7B2CBF), Color(0xFF9D4EDD)),
    LumiaThemePreset("Sunset", "Sunset", "Radiant terracotta & amber glow", SunsetLight, SunsetDark, SunsetOled, Color(0xFFFF7043), Color(0xFFFFB300)),
    LumiaThemePreset("Cyberpunk", "Cyberpunk", "Electric cyan & neon high-tech", CyberpunkLight, CyberpunkDark, CyberpunkOled, Color(0xFF00E5FF), Color(0xFF1DE9B6)),
    LumiaThemePreset("Monochrome", "Monochrome", "Obsidian slate & minimalist steel", MonochromeLight, MonochromeDark, MonochromeOled, Color(0xFF64748B), Color(0xFF94A3B8))
)

// ============================================================================
// 5. ADAPTIVE CUSTOM COLOR SCHEME GENERATOR
// ============================================================================

/**
 * Builds an adaptive custom ColorScheme from user-specified hex shades,
 * enforcing contrast safety and OLED pure black accommodation.
 */
@ValueScore(score = 96, importance = Importance.CRITICAL, description = "Intelligent adaptive custom palette constructor", category = "Theme")
fun buildAdaptiveCustomScheme(
    customPrimary: String,
    customPrimaryContainer: String,
    customBackground: String,
    customSurface: String,
    customText: String,
    isDark: Boolean,
    isOled: Boolean
): ColorScheme {
    val pSeed = safeParseColor(customPrimary, Color(0xFF007AFF))
    val pcSeed = safeParseColor(customPrimaryContainer, Color(0xFFD0E6FF))
    val bgSeed = safeParseColor(customBackground, if (isDark) Color(0xFF121214) else Color(0xFFF6F6F9))
    val sfSeed = safeParseColor(customSurface, if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF))
    val txtSeed = safeParseColor(customText, if (isDark) Color(0xFFF4F4F6) else Color(0xFF1C1C1E))

    // Contrast and luminance tuning
    val pLum = pSeed.perceivedLuminance()
    val primary = if (isDark) {
        if (pLum < 0.45f) pSeed.mix(Color.White, 0.65f) else pSeed
    } else {
        if (pLum > 0.55f) pSeed.mix(Color.Black, 0.65f) else pSeed
    }

    val secondary = primary.harmonizeWith(Color(0xFF5856D6), 0.35f)
    val tertiary = primary.harmonizeWith(Color(0xFF34C759), 0.35f)

    val pcLum = pcSeed.perceivedLuminance()
    val primaryContainer = if (isDark) {
        if (pcLum > 0.35f) pcSeed.mix(Color.Black, 0.45f) else pcSeed
    } else {
        if (pcLum < 0.65f) pcSeed.mix(Color.White, 0.45f) else pcSeed
    }

    val secondaryContainer = primaryContainer.harmonizeWith(secondary, 0.5f)
    val tertiaryContainer = primaryContainer.harmonizeWith(tertiary, 0.5f)

    if (isDark && isOled) {
        return createOledDarkScheme(
            primary = primary,
            primaryContainer = primaryContainer,
            secondary = secondary,
            secondaryContainer = secondaryContainer,
            tertiary = tertiary,
            tertiaryContainer = tertiaryContainer
        )
    }

    val bgLum = bgSeed.perceivedLuminance()
    val background = if (isDark) {
        if (bgLum > 0.30f) bgSeed.mix(Color.Black, 0.10f) else bgSeed
    } else {
        if (bgLum < 0.70f) bgSeed.mix(Color.White, 0.95f) else bgSeed
    }

    val sfLum = sfSeed.perceivedLuminance()
    val surface = if (isDark) {
        if (sfLum > 0.35f) sfSeed.mix(Color.Black, 0.15f) else sfSeed
    } else {
        if (sfLum < 0.75f) sfSeed.mix(Color.White, 0.98f) else sfSeed
    }

    val txtLum = txtSeed.perceivedLuminance()
    val text = if (isDark) {
        if (txtLum <= 0.60f) primary.mix(Color.White, 0.90f) else txtSeed
    } else {
        if (txtLum >= 0.40f) primary.mix(Color.Black, 0.15f) else txtSeed
    }

    return if (isDark) {
        createDarkScheme(
            primary = primary,
            primaryContainer = primaryContainer,
            secondary = secondary,
            secondaryContainer = secondaryContainer,
            tertiary = tertiary,
            tertiaryContainer = tertiaryContainer,
            bg = background,
            surface = surface,
            onSurfaceText = text
        )
    } else {
        createLightScheme(
            primary = primary,
            primaryContainer = primaryContainer,
            secondary = secondary,
            secondaryContainer = secondaryContainer,
            tertiary = tertiary,
            tertiaryContainer = tertiaryContainer,
            bg = background,
            surface = surface,
            onSurfaceText = text
        )
    }
}
