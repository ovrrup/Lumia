package lumia.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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

@Composable
fun Modifier.glassHeaderCapsule(
    useGlass: Boolean,
    shape: Shape = RoundedCornerShape(32.dp)
): Modifier = composed {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.5f
    val tint = LocalGlassTint.current
    val dynamic = LocalGlassDynamic.current

    if (useGlass) {
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = 12.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Side: Back button capsule (increased width!)
        if (onBackClick != null) {
            Row(
                modifier = Modifier
                    .height(44.dp)
                    .widthIn(min = 54.dp) // Increased width!
                    .glassHeaderCapsule(useGlass = useGlassHeader, shape = RoundedCornerShape(22.dp))
                    .bouncyClick { onBackClick() }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = if (useGlassHeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.width(54.dp))
        }

        // Center Side: Title capsule (floating in the middle)
        Row(
            modifier = Modifier
                .height(44.dp)
                .weight(1f, fill = false)
                .padding(horizontal = 8.dp)
                .glassHeaderCapsule(useGlass = useGlassHeader, shape = RoundedCornerShape(22.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = if (useGlassHeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        // Right Side: Action buttons capsule (increased width!)
        if (actions != null) {
            Row(
                modifier = Modifier
                    .height(44.dp)
                    .widthIn(min = 54.dp) // Increased width!
                    .glassHeaderCapsule(useGlass = useGlassHeader, shape = RoundedCornerShape(22.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                actions()
            }
        } else {
            Spacer(modifier = Modifier.width(54.dp))
        }
    }
}
