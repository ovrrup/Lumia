package lumia.tracker.ui.screens.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore

/**
 * Convenience wrapper for [SettingsActionItem] when rendered inside a [SettingsGroupCard].
 */
@ValueScore(
    score = 85,
    importance = Importance.HIGH,
    description = "Primary interactive action row for navigation and triggers within settings group cards",
    category = "Settings"
)
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
