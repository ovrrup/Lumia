package lumia.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lumia.tracker.ui.theme.LocalGlassMode
import lumia.tracker.ui.theme.LocalMoreRounds
import lumia.tracker.ui.theme.LocalMoreRoundsMode
import lumia.tracker.ui.theme.LocalGlassTint
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.ui.theme.LocalGlassDynamic
import lumia.tracker.ui.theme.liquidGlass
import lumia.tracker.ui.theme.mix
import lumia.tracker.ui.theme.LocalDarkTheme
import lumia.tracker.ui.theme.LocalPureBlackMode

@Composable
fun Modifier.glassHeaderCapsule(
    useGlass: Boolean,
    shape: Shape = RoundedCornerShape(32.dp)
): Modifier = composed {
    val isDark = LocalDarkTheme.current
    val isPureBlack = LocalPureBlackMode.current
    val actualUseGlass = if (isPureBlack) false else useGlass
    val tint = LocalGlassTint.current
    val dynamic = LocalGlassDynamic.current

    if (actualUseGlass) {
        val tintColor = if (dynamic) {
            if (isDark) tint.mix(Color.Black, 0.15f) else tint.mix(Color.White, 0.25f)
        } else {
            if (isDark) Color.Black else Color.White
        }

        this
            .shadow(
                elevation = 8.dp,
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .liquidGlass(
                shape = shape,
                tintColor = tintColor,
                tintAlpha = if (isDark) 0.35f else 0.45f,
                opacityOverride = 1.0f,
                backdropStyleOverride = "Satin" // Force Satin so it is never completely transparent
            )
            .border(
                width = 1.5.dp, // Thicker to reflect hard light
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.65f), // High-reflection hard light top edge
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.10f) // Softer bottom edge
                    )
                ),
                shape = shape
            )
    } else {
        this
            .shadow(
                elevation = 10.dp,
                shape = shape,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            )
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                shape = shape
            )
    }
}

@Composable
fun UniversalCapsuleHeader(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    val isGlass = LocalGlassMode.current
    val moreRounds = LocalMoreRounds.current
    val moreRoundsMode = LocalMoreRoundsMode.current
    val isMrGlass = moreRounds && moreRoundsMode == "Glass"
    val useGlassHeader = isGlass || isMrGlass

    val isDark = LocalDarkTheme.current
    val isPureBlack = LocalPureBlackMode.current
    val actualUseGlassHeader = if (isPureBlack) false else useGlassHeader

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassHeaderCapsule(useGlass = actualUseGlassHeader)
                .height(48.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Left Side: iOS style Back button
            if (onBackClick != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .bouncyClick { onBackClick() }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Back",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Center: iOS Title (Clean, semi-bold, size 17-18sp)
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (actualUseGlassHeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 72.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Right Side: Action buttons
            if (actions != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    actions()
                }
            }
        }
    }
}
