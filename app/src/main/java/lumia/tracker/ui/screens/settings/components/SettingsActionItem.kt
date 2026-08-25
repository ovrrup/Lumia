package lumia.tracker.ui.screens.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore

/**
 * SettingsActionItem - Refined action item row with 34dp rounded squircle icon badge,
 * crisp title, subtle subtitle, sleek trailing chevron, and >=48dp touch target.
 */
@ValueScore(
    score = 80,
    importance = Importance.HIGH,
    description = "Configurable settings action row with icon badge, title/subtitle, and customizable trailing content",
    category = "Settings"
)
@Composable
fun SettingsActionItem(
    title: String,
    subtitle: String = "",
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
    inCard: Boolean = false,
    iconBgColor: Color? = null,
    iconTint: Color? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val titleColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val badgeShape = RoundedCornerShape(10.dp)

    val badgeBg = iconBgColor ?: if (isDestructive) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    val badgeTint = iconTint ?: if (iconBgColor != null) {
        Color.White
    } else if (isDestructive) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 52.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(
                horizontal = if (inCard) 8.dp else 14.dp,
                vertical = 10.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(badgeShape)
                .background(badgeBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = badgeTint,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        } else {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
