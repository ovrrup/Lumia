package lumia.tracker.ui.screens.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Convenience wrapper for [SettingsActionItem] when rendered inside a [SettingsGroupCard].
 */
@Composable
fun SettingsActionItemInCard(
    title: String,
    subtitle: String = "",
    icon: ImageVector,
    isDestructive: Boolean = false,
    iconBgColor: Color? = null,
    iconTint: Color? = null,
    onClick: () -> Unit
) {
    SettingsActionItem(
        title = title,
        subtitle = subtitle,
        icon = icon,
        isDestructive = isDestructive,
        inCard = true,
        iconBgColor = iconBgColor,
        iconTint = iconTint,
        onClick = onClick
    )
}
