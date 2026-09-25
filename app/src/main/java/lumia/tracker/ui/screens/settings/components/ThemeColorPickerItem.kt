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

    val auraColor = if (name == "Dynamic") MaterialTheme.colorScheme.primary else color
    val auraAlpha = if (isSelected) 0.40f else 0.12f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp, horizontal = 2.dp)
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

        // Outer subtle glowing aura
        Box(
            modifier = Modifier
                .size(58.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            auraColor.copy(alpha = auraAlpha),
                            auraColor.copy(alpha = auraAlpha * 0.45f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Main circular swatch with frosted glass border
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .then(backgroundModifier)
                    .border(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        brush = Brush.verticalGradient(
                            colors = if (isSelected) {
                                listOf(
                                    Color.White.copy(alpha = 0.90f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.70f)
                                )
                            } else {
                                listOf(
                                    Color.White.copy(alpha = 0.45f),
                                    Color.White.copy(alpha = 0.12f)
                                )
                            }
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Circular glass specular highlight
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color.White.copy(alpha = 0.40f),
                                0.45f to Color.White.copy(alpha = 0.12f),
                                0.85f to Color.Transparent
                            )
                        )
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.35f),
                                        Color.Black.copy(alpha = 0.45f)
                                    )
                                ),
                                CircleShape
                            )
                            .border(0.75.dp, Color.White.copy(alpha = 0.65f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                } else if (name == "Dynamic") {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "Dynamic Material You",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                } else if (name == "Custom") {
                    Icon(
                        imageVector = Icons.Rounded.Colorize,
                        contentDescription = "Custom Palette",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
