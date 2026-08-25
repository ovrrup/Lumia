package lumia.tracker.ui.screens.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore

/**
 * SettingsGroupMenu - Standalone menu action row delegating to [SettingsActionItem]
 * to maintain consistent touch targets, badges, and typography across settings screens.
 */
@ValueScore(
    score = 50,
    importance = Importance.MEDIUM,
    description = "Standalone settings navigation menu row component delegating to standard action item",
    category = "Settings"
)
@Composable
fun SettingsGroupMenu(
    title: String,
    subtitle: String = "",
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconBgColor: Color? = null,
    iconTint: Color? = null,
    onClick: () -> Unit
) {
    SettingsActionItem(
        title = title,
        subtitle = subtitle,
        icon = icon,
        modifier = modifier,
        inCard = false,
        iconBgColor = iconBgColor,
        iconTint = iconTint,
        onClick = onClick
    )
}
