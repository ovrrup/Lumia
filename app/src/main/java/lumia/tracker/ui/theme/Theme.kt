package lumia.tracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape

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
    surfaceVariant = Color(0xFFE5E5EA), onSurfaceVariant = Color(0xFF8E8E93),
    surfaceContainer = Color(0xFFFFFFFF), surfaceContainerHigh = Color(0xFFF2F2F7),
    outline = Color(0xFFD1D1D6), outlineVariant = Color(0xFFE5E5EA),
    error = Color(0xFFFF3B30), onError = Color.White,
    errorContainer = Color(0xFFFFE5E5), onErrorContainer = Color(0xFFD70015),
    surfaceTint = primary
)

fun createDarkScheme(
    primary: Color, primaryContainer: Color,
    secondary: Color, secondaryContainer: Color,
    tertiary: Color, tertiaryContainer: Color,
    bg: Color = Color(0xFF000000),
    surface: Color = Color(0xFF1C1C1E),
    onSurfaceText: Color = Color(0xFFFFFFFF)
) = darkColorScheme(
    primary = primary, onPrimary = Color.White,
    primaryContainer = primaryContainer, onPrimaryContainer = Color.White.mix(primaryContainer, 0.85f),
    secondary = secondary, onSecondary = Color.White,
    secondaryContainer = secondaryContainer, onSecondaryContainer = Color.White.mix(secondaryContainer, 0.85f),
    tertiary = tertiary, onTertiary = Color.White,
    tertiaryContainer = tertiaryContainer, onTertiaryContainer = Color.White.mix(tertiaryContainer, 0.85f),
    background = bg, onBackground = onSurfaceText,
    surface = surface, onSurface = onSurfaceText,
    surfaceVariant = Color(0xFF2C2C2E), onSurfaceVariant = Color(0xFF8E8E93),
    surfaceContainer = Color(0xFF1C1C1E), surfaceContainerHigh = Color(0xFF2C2C2E),
    outline = Color(0xFF38383A), outlineVariant = Color(0xFF2C2C2E),
    error = Color(0xFFFF453A), onError = Color.White,
    errorContainer = Color(0xFF5E110E), onErrorContainer = Color(0xFFFFB4AB),
    surfaceTint = primary
)

val OceanLight = createLightScheme(
    primary = Color(0xFF007AFF), primaryContainer = Color(0xFFD0E6FF), 
    secondary = Color(0xFF5856D6), secondaryContainer = Color(0xFFE5E4FF), 
    tertiary = Color(0xFF34C759), tertiaryContainer = Color(0xFFD5F5DD),
    bg = Color(0xFFF2F2F7), surface = Color(0xFFFFFFFF)
)
val OceanDark = createDarkScheme(
    primary = Color(0xFF0A84FF), primaryContainer = Color(0xFF004080), 
    secondary = Color(0xFF5E5CE6), secondaryContainer = Color(0xFF282766), 
    tertiary = Color(0xFF30D158), tertiaryContainer = Color(0xFF0E5420),
    bg = Color(0xFF000000), surface = Color(0xFF1C1C1E)
)

val EmeraldLight = createLightScheme(
    primary = Color(0xFF34C759), primaryContainer = Color(0xFFD5F5DD), 
    secondary = Color(0xFF30B0C7), secondaryContainer = Color(0xFFD5F3F7), 
    tertiary = Color(0xFF007AFF), tertiaryContainer = Color(0xFFD0E6FF),
    bg = Color(0xFFF2F2F7), surface = Color(0xFFFFFFFF)
)
val EmeraldDark = createDarkScheme(
    primary = Color(0xFF30D158), primaryContainer = Color(0xFF0E5420), 
    secondary = Color(0xFF40CBE0), secondaryContainer = Color(0xFF104B54), 
    tertiary = Color(0xFF0A84FF), tertiaryContainer = Color(0xFF004080),
    bg = Color(0xFF000000), surface = Color(0xFF1C1C1E)
)

val GoldLight = createLightScheme(
    primary = Color(0xFFFF9500), primaryContainer = Color(0xFFFFECC4), 
    secondary = Color(0xFFFF2D55), secondaryContainer = Color(0xFFFFD5DD), 
    tertiary = Color(0xFF34C759), tertiaryContainer = Color(0xFFD5F5DD),
    bg = Color(0xFFF2F2F7), surface = Color(0xFFFFFFFF)
)
val GoldDark = createDarkScheme(
    primary = Color(0xFFFF9F0A), primaryContainer = Color(0xFF6B3E00), 
    secondary = Color(0xFFFF375F), secondaryContainer = Color(0xFF660E1F), 
    tertiary = Color(0xFF30D158), tertiaryContainer = Color(0xFF0E5420),
    bg = Color(0xFF000000), surface = Color(0xFF1C1C1E)
)

val RoseLight = createLightScheme(
    primary = Color(0xFFFF2D55), primaryContainer = Color(0xFFFFD5DD), 
    secondary = Color(0xFFAF52DE), secondaryContainer = Color(0xFFF2DCFA), 
    tertiary = Color(0xFFFF9500), tertiaryContainer = Color(0xFFFFECC4),
    bg = Color(0xFFF2F2F7), surface = Color(0xFFFFFFFF)
)
val RoseDark = createDarkScheme(
    primary = Color(0xFFFF375F), primaryContainer = Color(0xFF660E1F), 
    secondary = Color(0xFFBF5AF2), secondaryContainer = Color(0xFF4B1E66), 
    tertiary = Color(0xFFFF9F0A), tertiaryContainer = Color(0xFF6B3E00),
    bg = Color(0xFF000000), surface = Color(0xFF1C1C1E)
)

val SageLight = createLightScheme(
    primary = Color(0xFF30B0C7), primaryContainer = Color(0xFFD5F3F7), 
    secondary = Color(0xFF34C759), secondaryContainer = Color(0xFFD5F5DD), 
    tertiary = Color(0xFF5856D6), tertiaryContainer = Color(0xFFE5E4FF), 
    bg = Color(0xFFF2F2F7), surface = Color(0xFFFFFFFF)
)
val SageDark = createDarkScheme(
    primary = Color(0xFF40CBE0), primaryContainer = Color(0xFF104B54), 
    secondary = Color(0xFF30D158), secondaryContainer = Color(0xFF0E5420), 
    tertiary = Color(0xFF5E5CE6), tertiaryContainer = Color(0xFF282766), 
    bg = Color(0xFF000000), surface = Color(0xFF1C1C1E)
)

val TwilightLight = createLightScheme(
    primary = Color(0xFF5856D6), primaryContainer = Color(0xFFE5E4FF), 
    secondary = Color(0xFFAF52DE), secondaryContainer = Color(0xFFF2DCFA), 
    tertiary = Color(0xFF007AFF), tertiaryContainer = Color(0xFFD0E6FF), 
    bg = Color(0xFFF2F2F7), surface = Color(0xFFFFFFFF)
)
val TwilightDark = createDarkScheme(
    primary = Color(0xFF5E5CE6), primaryContainer = Color(0xFF282766), 
    secondary = Color(0xFFBF5AF2), secondaryContainer = Color(0xFF4B1E66), 
    tertiary = Color(0xFF0A84FF), tertiaryContainer = Color(0xFF004080), 
    bg = Color(0xFF000000), surface = Color(0xFF1C1C1E)
)

fun Color.mix(other: Color, weight: Float): Color {
    return Color(
        red = this.red * weight + other.red * (1f - weight),
        green = this.green * weight + other.green * (1f - weight),
        blue = this.blue * weight + other.blue * (1f - weight),
        alpha = this.alpha * weight + other.alpha * (1f - weight)
    )
}

val LocalAppAnimationMode = androidx.compose.runtime.compositionLocalOf { "Normal" }
val LocalMoreRounds = androidx.compose.runtime.compositionLocalOf { false }
val LocalMoreRoundsMode = androidx.compose.runtime.compositionLocalOf { "Pastel" }

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
                colorScheme = colorScheme.copy(
                    onSurface = Color.White,
                    onBackground = Color.White,
                    onSurfaceVariant = Color.White.copy(alpha = 0.85f),
                    onPrimaryContainer = Color.White,
                    onSecondaryContainer = Color.White,
                    onTertiaryContainer = Color.White
                )
            } else {
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

    if (moreRounds && moreRoundsMode == "Pastel") {
        colorScheme = colorScheme.copy(
            primary = colorScheme.primary.mix(if (isDark) Color.White else Color.Black, 0.4f).copy(alpha = 0.9f),
            secondary = colorScheme.secondary.mix(if (isDark) Color.White else Color.Black, 0.4f).copy(alpha = 0.9f),
            tertiary = colorScheme.tertiary.mix(if (isDark) Color.White else Color.Black, 0.4f).copy(alpha = 0.9f)
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

    androidx.compose.runtime.CompositionLocalProvider(
        LocalAppAnimationMode provides appAnimationMode,
        LocalMoreRounds provides moreRounds,
        LocalMoreRoundsMode provides moreRoundsMode
    ) {
        val currentShapes = if (moreRounds) Shapes(
            extraSmall = RoundedCornerShape(4.dp),
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(28.dp),
            extraLarge = RoundedCornerShape(36.dp)
        ) else Shapes
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = currentShapes,
            content = content
        )
    }
}

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
            "Bouncy" -> spring(dampingRatio = if (moreRounds) 0.3f else 0.35f, stiffness = if (moreRounds) 250f else 300f)
            "Dynamic" -> spring(dampingRatio = if (moreRounds) 0.5f else 0.6f, stiffness = 600f)
            else -> spring(dampingRatio = 0.85f, stiffness = 1000f)
        },
        label = "bouncyScale"
    )
    
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun Modifier.bouncyClick(enabled: Boolean = true, onClick: () -> Unit = {}): Modifier {
    val animationMode = LocalAppAnimationMode.current
    if (!enabled) return this
    
    val interactionSource = androidx.compose.runtime.remember { MutableInteractionSource() }
    
    if (animationMode == "Off" || animationMode == "None") {
        return this.clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.material3.ripple(),
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
            "Dynamic" -> spring(dampingRatio = 0.6f, stiffness = 600f)
            else -> spring(dampingRatio = 0.85f, stiffness = 1000f)
        },
        label = "bouncyClickScale"
    )
    
    return this.clickable(
        interactionSource = interactionSource,
        indication = androidx.compose.material3.ripple(),
        enabled = enabled,
        onClick = onClick
    ).graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun Modifier.animateItemEntry(index: Int): Modifier {
    val animationMode = LocalAppAnimationMode.current
    if (animationMode == "None") return this
    
    val visibleState = remember { 
        MutableTransitionState(false).apply { targetState = true } 
    }
    val transition = rememberTransition(visibleState, label = "itemEntry")
    
    val delay = (index * 45).coerceAtMost(300) // staggered delay with a reasonable cap
    
    val offset by transition.animateDp(
        transitionSpec = {
            if (animationMode == "Bouncy") {
                spring(
                    dampingRatio = 0.55f, 
                    stiffness = Spring.StiffnessMediumLow
                )
            } else {
                tween(
                    durationMillis = 350, 
                    delayMillis = delay, 
                    easing = FastOutSlowInEasing
                )
            }
        },
        label = "offset"
    ) { state ->
        if (state) 0.dp else 16.dp
    }
    
    val alpha by transition.animateFloat(
        transitionSpec = {
            tween(
                durationMillis = 300, 
                delayMillis = delay, 
                easing = LinearOutSlowInEasing
            )
        },
        label = "alpha"
    ) { state ->
        if (state) 1f else 0f
    }
    
    return this
        .graphicsLayer {
            translationY = offset.toPx()
            this.alpha = alpha
        }
}

