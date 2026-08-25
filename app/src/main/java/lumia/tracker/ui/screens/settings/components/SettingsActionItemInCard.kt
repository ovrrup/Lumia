package lumia.tracker.ui.screens.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
    iconBgColor: Color? = null,
    iconTint: Color? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    SettingsActionItem(
        title = title,
        subtitle = subtitle,
        icon = icon,
        modifier = modifier,
        isDestructive = isDestructive,
        inCard = true,
        iconBgColor = iconBgColor,
        iconTint = iconTint,
        trailingContent = trailingContent,
        onClick = onClick
    )
}
