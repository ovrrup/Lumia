package lumia.tracker.ui.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore

/**
 * Theme - Core Theme Engine & Visual Customization Subsystem for Lumia Academic Tracker.
 * Manages reactive Material 3 color schemes, dynamic wallpaper harmonizing, OLED pure black tokens,
 * and high-performance fluid theme transitions without flicker or restarts.
 */

// ============================================================================
// COMPOSITION LOCALS
// ============================================================================

@ValueScore(score = 88, importance = Importance.HIGH, description = "Composition local for touch spring animation mode", category = "Theme")
val LocalAppAnimationMode = compositionLocalOf { "Normal" }

@ValueScore(score = 86, importance = Importance.MEDIUM, description = "Composition local for hyper-rounded UI corner shapes", category = "Theme")
val LocalMoreRounds = compositionLocalOf { false }

@ValueScore(score = 84, importance = Importance.LOW, description = "Composition local for hyper-rounded corner color styling", category = "Theme")
val LocalMoreRoundsMode = compositionLocalOf { "Pastel" }

@ValueScore(score = 88, importance = Importance.HIGH, description = "Composition local for active pure AMOLED black mode", category = "Theme")
val LocalPureBlackMode = compositionLocalOf { false }

@ValueScore(score = 88, importance = Importance.HIGH, description = "Composition local for current theme palette name", category = "Theme")
val LocalThemeColor = compositionLocalOf { "Ocean" }

// ============================================================================
// SMOOTH COLOR SCHEME INTERPOLATION (Zero Flicker / Instant Switch)
// ============================================================================

/**
 * Interpolates color scheme changes smoothly across all open screens without screen flashing or restarts.
 */
@ValueScore(score = 95, importance = Importance.CRITICAL, description = "Smooth reactive color scheme transition engine", category = "Theme")
@Composable
fun animateColorSchemeAsState(
    targetColorScheme: ColorScheme,
    animationMode: String
): ColorScheme {
    if (animationMode == "Off" || animationMode == "None") {
        return targetColorScheme
    }

    val spec: AnimationSpec<Color> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val primary by animateColorAsState(targetColorScheme.primary, spec, label = "primary")
    val onPrimary by animateColorAsState(targetColorScheme.onPrimary, spec, label = "onPrimary")
    val primaryContainer by animateColorAsState(targetColorScheme.primaryContainer, spec, label = "primaryContainer")
    val onPrimaryContainer by animateColorAsState(targetColorScheme.onPrimaryContainer, spec, label = "onPrimaryContainer")
    val secondary by animateColorAsState(targetColorScheme.secondary, spec, label = "secondary")
    val onSecondary by animateColorAsState(targetColorScheme.onSecondary, spec, label = "onSecondary")
    val secondaryContainer by animateColorAsState(targetColorScheme.secondaryContainer, spec, label = "secondaryContainer")
    val onSecondaryContainer by animateColorAsState(targetColorScheme.onSecondaryContainer, spec, label = "onSecondaryContainer")
    val tertiary by animateColorAsState(targetColorScheme.tertiary, spec, label = "tertiary")
    val onTertiary by animateColorAsState(targetColorScheme.onTertiary, spec, label = "onTertiary")
    val tertiaryContainer by animateColorAsState(targetColorScheme.tertiaryContainer, spec, label = "tertiaryContainer")
    val onTertiaryContainer by animateColorAsState(targetColorScheme.onTertiaryContainer, spec, label = "onTertiaryContainer")
    val background by animateColorAsState(targetColorScheme.background, spec, label = "background")
    val onBackground by animateColorAsState(targetColorScheme.onBackground, spec, label = "onBackground")
    val surface by animateColorAsState(targetColorScheme.surface, spec, label = "surface")
    val onSurface by animateColorAsState(targetColorScheme.onSurface, spec, label = "onSurface")
    val surfaceVariant by animateColorAsState(targetColorScheme.surfaceVariant, spec, label = "surfaceVariant")
    val onSurfaceVariant by animateColorAsState(targetColorScheme.onSurfaceVariant, spec, label = "onSurfaceVariant")
    val surfaceTint by animateColorAsState(targetColorScheme.surfaceTint, spec, label = "surfaceTint")
    val outline by animateColorAsState(targetColorScheme.outline, spec, label = "outline")
    val outlineVariant by animateColorAsState(targetColorScheme.outlineVariant, spec, label = "outlineVariant")
    val error by animateColorAsState(targetColorScheme.error, spec, label = "error")
    val onError by animateColorAsState(targetColorScheme.onError, spec, label = "onError")
    val errorContainer by animateColorAsState(targetColorScheme.errorContainer, spec, label = "errorContainer")
    val onErrorContainer by animateColorAsState(targetColorScheme.onErrorContainer, spec, label = "onErrorContainer")
    val surfaceContainerLowest by animateColorAsState(targetColorScheme.surfaceContainerLowest, spec, label = "surfaceContainerLowest")
    val surfaceContainerLow by animateColorAsState(targetColorScheme.surfaceContainerLow, spec, label = "surfaceContainerLow")
    val surfaceContainer by animateColorAsState(targetColorScheme.surfaceContainer, spec, label = "surfaceContainer")
    val surfaceContainerHigh by animateColorAsState(targetColorScheme.surfaceContainerHigh, spec, label = "surfaceContainerHigh")
    val surfaceContainerHighest by animateColorAsState(targetColorScheme.surfaceContainerHighest, spec, label = "surfaceContainerHighest")

    return remember(
        primary, onPrimary, primaryContainer, onPrimaryContainer,
        secondary, onSecondary, secondaryContainer, onSecondaryContainer,
        tertiary, onTertiary, tertiaryContainer, onTertiaryContainer,
        background, onBackground, surface, onSurface,
        surfaceVariant, onSurfaceVariant, surfaceTint, outline, outlineVariant,
        error, onError, errorContainer, onErrorContainer,
        surfaceContainerLowest, surfaceContainerLow, surfaceContainer,
        surfaceContainerHigh, surfaceContainerHighest
    ) {
        targetColorScheme.copy(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceTint = surfaceTint,
            outline = outline,
            outlineVariant = outlineVariant,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest
        )
    }
}

// ============================================================================
// ROOT SCHOLAR THEME COMPOSABLE
// ============================================================================

/**
 * ScholarTheme - Root Material 3 Theme for Lumia Academic Tracker.
 * Dynamically resolves palettes, applies pure black OLED surface tokens,
 * harmonizes typography, and configures translucent system status & navigation bars.
 */
@ValueScore(
    score = 98,
    importance = Importance.CRITICAL,
    description = "Root Material 3 dynamic theme container with instant switching & OLED tokens",
    category = "Theme"
)
@Composable
fun ScholarTheme(
    themeMode: String = "System",
    themeColor: String = "Ocean",
    customPrimary: String = "",
    customPrimaryContainer: String = "",
    customBackground: String = "",
    customSurface: String = "",
    customText: String = "",
    pureBlackMode: Boolean = false,
    betterTexts: Boolean = false,
    betterTextsPalette: Boolean = true,
    appAnimationMode: String = "Normal",
    moreRounds: Boolean = false,
    moreRoundsMode: String = "Pastel",
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

    // Resolve base ColorScheme
    val targetScheme: ColorScheme = remember(
        themeColor, isDark, pureBlackMode, customPrimary,
        customPrimaryContainer, customBackground, customSurface, customText
    ) {
        when {
            themeColor == "Dynamic" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val dynScheme = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                if (isDark && pureBlackMode) {
                    dynScheme.copy(
                        background = OledBackground,
                        surface = OledSurface,
                        surfaceVariant = OledSurfaceVariant,
                        surfaceContainerLowest = OledContainerLowest,
                        surfaceContainerLow = OledContainerLow,
                        surfaceContainer = OledContainer,
                        surfaceContainerHigh = OledContainerHigh,
                        surfaceContainerHighest = OledContainerHighest,
                        outline = OledOutline,
                        outlineVariant = OledOutlineVariant
                    )
                } else {
                    dynScheme
                }
            }
            themeColor == "Custom" -> {
                buildAdaptiveCustomScheme(
                    customPrimary = customPrimary,
                    customPrimaryContainer = customPrimaryContainer,
                    customBackground = customBackground,
                    customSurface = customSurface,
                    customText = customText,
                    isDark = isDark,
                    isOled = pureBlackMode
                )
            }
            else -> {
                val preset = LumiaThemePresets.find { it.id.equals(themeColor, ignoreCase = true) }
                    ?: LumiaThemePresets[0] // fallback to Ocean
                when {
                    isDark && pureBlackMode -> preset.oledDarkScheme
                    isDark -> preset.darkScheme
                    else -> preset.lightScheme
                }
            }
        }
    }

    // Apply Material 3 harmonious micro-tuning & tone adjustments
    var refinedScheme = remember(targetScheme, isDark, pureBlackMode) {
        if (isDark && pureBlackMode) {
            targetScheme.copy(
                background = OledBackground,
                surface = OledSurface,
                surfaceContainerLowest = OledContainerLowest,
                surfaceContainerLow = OledContainerLow,
                surfaceContainer = OledContainer,
                surfaceContainerHigh = OledContainerHigh,
                surfaceContainerHighest = OledContainerHighest,
                outline = OledOutline,
                outlineVariant = OledOutlineVariant
            )
        } else {
            targetScheme.copy(
                background = if (isDark) targetScheme.background else targetScheme.background.mix(targetScheme.primary, 0.985f),
                surface = if (isDark) targetScheme.surface else targetScheme.surface.mix(targetScheme.primary, 0.98f),
                surfaceVariant = targetScheme.surfaceVariant.mix(targetScheme.secondary, 0.96f),
                primaryContainer = targetScheme.primaryContainer.mix(targetScheme.primary, 0.94f),
                secondaryContainer = targetScheme.secondaryContainer.mix(targetScheme.secondary, 0.94f),
                tertiaryContainer = targetScheme.tertiaryContainer.mix(targetScheme.tertiary, 0.94f)
            )
        }
    }

    // Enhanced Typography contrast calibration
    if (betterTexts) {
        val baseTextSourceColor = if (themeColor == "Custom" && customText.isNotBlank()) {
            safeParseColor(customText, refinedScheme.primary)
        } else {
            refinedScheme.primary
        }

        refinedScheme = if (betterTextsPalette) {
            if (isDark) {
                val highContrastOnSurface = baseTextSourceColor.mix(Color.White, 0.12f)
                refinedScheme.copy(
                    onSurface = highContrastOnSurface,
                    onBackground = highContrastOnSurface,
                    onSurfaceVariant = baseTextSourceColor.mix(Color.White, 0.22f).copy(alpha = 0.92f),
                    onPrimaryContainer = highContrastOnSurface,
                    onSecondaryContainer = refinedScheme.secondary.mix(Color.White, 0.12f),
                    onTertiaryContainer = refinedScheme.tertiary.mix(Color.White, 0.12f)
                )
            } else {
                val highContrastOnSurface = baseTextSourceColor.mix(Color.Black, 0.12f)
                refinedScheme.copy(
                    onSurface = highContrastOnSurface,
                    onBackground = highContrastOnSurface,
                    onSurfaceVariant = baseTextSourceColor.mix(Color.Black, 0.22f).copy(alpha = 0.92f),
                    onPrimaryContainer = highContrastOnSurface,
                    onSecondaryContainer = refinedScheme.secondary.mix(Color.Black, 0.12f),
                    onTertiaryContainer = refinedScheme.tertiary.mix(Color.Black, 0.12f)
                )
            }
        } else {
            if (isDark) {
                refinedScheme.copy(
                    onSurface = Color.White,
                    onBackground = Color.White,
                    onSurfaceVariant = Color.White.copy(alpha = 0.88f),
                    onPrimaryContainer = Color.White,
                    onSecondaryContainer = Color.White,
                    onTertiaryContainer = Color.White
                )
            } else {
                refinedScheme.copy(
                    onSurface = Color.Black,
                    onBackground = Color.Black,
                    onSurfaceVariant = Color.Black.copy(alpha = 0.88f),
                    onPrimaryContainer = Color.Black,
                    onSecondaryContainer = Color.Black,
                    onTertiaryContainer = Color.Black
                )
            }
        }
    }

    // Pastel tones adaptation when moreRounds is enabled
    if (moreRounds && moreRoundsMode == "Pastel") {
        refinedScheme = refinedScheme.copy(
            primary = refinedScheme.primary.mix(if (isDark) Color.White else Color.Black, 0.40f).copy(alpha = 0.95f),
            secondary = refinedScheme.secondary.mix(if (isDark) Color.White else Color.Black, 0.40f).copy(alpha = 0.95f),
            tertiary = refinedScheme.tertiary.mix(if (isDark) Color.White else Color.Black, 0.40f).copy(alpha = 0.95f)
        )
    }

    // Animate color scheme transitions smoothly to eliminate flicker & restarts
    val animatedScheme = animateColorSchemeAsState(
        targetColorScheme = refinedScheme,
        animationMode = appAnimationMode
    )

    // Dynamic System Bar Inset Synchronizer
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            var contextActivity: Context = view.context
            while (contextActivity is android.content.ContextWrapper && contextActivity !is Activity) {
                contextActivity = contextActivity.baseContext
            }
            if (contextActivity is Activity) {
                val window = contextActivity.window
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalAppAnimationMode provides appAnimationMode,
        LocalMoreRounds provides moreRounds,
        LocalMoreRoundsMode provides moreRoundsMode,
        LocalPureBlackMode provides pureBlackMode,
        LocalThemeColor provides themeColor
    ) {
        val currentShapes = if (moreRounds) Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(14.dp),
            medium = RoundedCornerShape(22.dp),
            large = RoundedCornerShape(30.dp),
            extraLarge = RoundedCornerShape(38.dp)
        ) else Shapes

        MaterialTheme(
            colorScheme = animatedScheme,
            typography = Typography,
            shapes = currentShapes,
            content = content
        )
    }
}

// ============================================================================
// INTERACTION & TACTILE ANIMATION MODIFIERS
// ============================================================================

/**
 * Adds an interactive spring compression scale effect upon user press.
 */
@ValueScore(score = 89, importance = Importance.HIGH, description = "Touch press bouncy compression modifier", category = "Animation")
@Composable
fun Modifier.bouncyScale(interactionSource: androidx.compose.foundation.interaction.InteractionSource): Modifier {
    val animationMode = LocalAppAnimationMode.current
    val moreRounds = LocalMoreRounds.current
    val isPressed by interactionSource.collectIsPressedAsState()

    val targetScale = if (isPressed) {
        when (animationMode) {
            "Bouncy" -> 0.92f
            "Dynamic" -> 0.95f
            else -> 0.97f
        }
    } else 1.0f

    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = when (animationMode) {
            "Bouncy" -> spring(dampingRatio = if (moreRounds) 0.30f else 0.35f, stiffness = if (moreRounds) 250f else 300f)
            "Dynamic" -> spring(dampingRatio = if (moreRounds) 0.50f else 0.60f, stiffness = 600f)
            else -> spring(dampingRatio = 0.85f, stiffness = 1000f)
        },
        label = "bouncyScale"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Combines clickable interaction with high-precision spring feedback and Material ripple.
 */
@ValueScore(score = 91, importance = Importance.HIGH, description = "Interactive bouncy click modifier with ripple", category = "Animation")
@Composable
fun Modifier.bouncyClick(enabled: Boolean = true, onClick: () -> Unit = {}): Modifier {
    val animationMode = LocalAppAnimationMode.current
    if (!enabled) return this

    val interactionSource = remember { MutableInteractionSource() }

    if (animationMode == "Off" || animationMode == "None") {
        return this.clickable(
            interactionSource = interactionSource,
            indication = ripple(),
            enabled = enabled,
            onClick = onClick
        )
    }

    val isPressed by interactionSource.collectIsPressedAsState()

    val targetScale = if (isPressed) {
        when (animationMode) {
            "Bouncy" -> 0.92f
            "Dynamic" -> 0.95f
            else -> 0.97f
        }
    } else 1.0f

    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = when (animationMode) {
            "Bouncy" -> spring(dampingRatio = 0.35f, stiffness = 300f)
            "Dynamic" -> spring(dampingRatio = 0.60f, stiffness = 600f)
            else -> spring(dampingRatio = 0.85f, stiffness = 1000f)
        },
        label = "bouncyClickScale"
    )

    return this
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(),
            enabled = enabled,
            onClick = onClick
        )
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
}

/**
 * Staggered cascade entrance animation modifier for lists and cards.
 */
@ValueScore(score = 87, importance = Importance.MEDIUM, description = "Staggered item entrance transition modifier", category = "Animation")
@Composable
fun Modifier.animateItemEntry(index: Int): Modifier {
    val animationMode = LocalAppAnimationMode.current
    if (animationMode == "None" || animationMode == "Off") return this

    val visibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    val transition = rememberTransition(visibleState, label = "itemEntry")

    val delay = (index * 45).coerceAtMost(300)

    val offset by transition.animateDp(
        transitionSpec = {
            if (animationMode == "Bouncy") {
                spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)
            } else {
                tween(durationMillis = 350, delayMillis = delay, easing = FastOutSlowInEasing)
            }
        },
        label = "offset"
    ) { state ->
        if (state) 0.dp else 16.dp
    }

    val alpha by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 300, delayMillis = delay, easing = LinearOutSlowInEasing)
        },
        label = "alpha"
    ) { state ->
        if (state) 1f else 0f
    }

    return this.graphicsLayer {
        translationY = offset.toPx()
        this.alpha = alpha
    }
}
