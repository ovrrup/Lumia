package lumia.tracker.ui.screens.settings.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore

/**
 * ThemeColorPickerItem - Interactive color swatch pill for theme palette selection.
 * Supports single and dual-tone gradient rings, Dynamic Material You icons, and custom color indicators.
 */
@ValueScore(
    score = 86,
    importance = Importance.MEDIUM,
    description = "Theme palette swatch selector with dynamic gradient and press animations",
    category = "UI"
)
@Composable
fun ThemeColorPickerItem(
    name: String,
    color: Color,
    isSelected: Boolean,
    secondaryColor: Color? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {
        val backgroundModifier = if (name == "Dynamic") {
            Modifier.background(
                Brush.sweepGradient(
                    listOf(
                        Color(0xFF388E3C),
                        Color(0xFF1976D2),
                        Color(0xFF7B1FA2),
                        Color(0xFFE64A19),
                        Color(0xFF388E3C)
                    )
                )
            )
        } else if (secondaryColor != null) {
            Modifier.background(
                Brush.linearGradient(
                    listOf(color, secondaryColor)
                )
            )
        } else {
            Modifier.background(color)
        }

        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .then(backgroundModifier)
                .border(
                    width = if (isSelected) 3.5.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(Color.Black.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else if (name == "Dynamic") {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = "Dynamic Material You",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else if (name == "Custom") {
                Icon(
                    imageVector = Icons.Rounded.Colorize,
                    contentDescription = "Custom Palette",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
