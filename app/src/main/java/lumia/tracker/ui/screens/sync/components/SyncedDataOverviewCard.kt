package lumia.tracker.ui.screens.sync.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Pending
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore

/**
 * Data model representing a synced user data category in everyday human terms.
 */
data class SyncedDataCategory(
    val title: String,
    val count: Int,
    val subtitle: String? = null,
    val icon: ImageVector
)

/**
 * Data model representing a sync activity event formatted for simple human readability.
 */
data class SyncHistoryEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val description: String,
    val timestampText: String,
    val icon: ImageVector = Icons.Rounded.CheckCircle
)

/**
 * Default sample history events illustrating human-centric sync activity.
 */
val DefaultSampleSyncHistory = listOf(
    SyncHistoryEvent(
        id = "event-1",
        description = "Phone paired",
        timestampText = "2m ago",
        icon = Icons.Rounded.Devices
    ),
    SyncHistoryEvent(
        id = "event-2",
        description = "Tasks synchronized",
        timestampText = "10m ago",
        icon = Icons.Rounded.TaskAlt
    ),
    SyncHistoryEvent(
        id = "event-3",
        description = "Notes & Reminders updated",
        timestampText = "25m ago",
        icon = Icons.Rounded.Description
    ),
    SyncHistoryEvent(
        id = "event-4",
        description = "Focus Sessions synced",
        timestampText = "1h ago",
        icon = Icons.Rounded.Timer
    ),
    SyncHistoryEvent(
        id = "event-5",
        description = "Settings & Tags synchronized",
        timestampText = "3h ago",
        icon = Icons.Rounded.Tune
    )
)

/**
 * SyncedDataOverviewCard - Modern Material 3 card presenting user sync status in simple, everyday terms.
 * Features:
 * - Synced Data Summary: Study Tasks, Notes & Reminders, Focus Sessions, Settings & Tags
 * - Sync Health: Clear indication of whether everything is up to date or items are pending sync
 * - Quick Action: Tactile BouncyButton for immediate synchronization
 * - Sync History List: Clear, recent sync activity log with relative timestamps
 */
@ValueScore(
    score = 92,
    importance = Importance.CRITICAL,
    description = "Human-centric synced data overview card showing data categories, sync health status, quick action, and recent sync history",
    category = "Sync"
)
@Composable
fun SyncedDataOverviewCard(
    modifier: Modifier = Modifier,
    studyTasksCount: Int = 12,
    notesRemindersCount: Int = 5,
    focusSessionsCount: Int = 8,
    settingsTagsCount: Int = 14,
    pendingItemsCount: Int = 0,
    isSyncing: Boolean = false,
    lastSyncTimeText: String = "Just now",
    historyEvents: List<SyncHistoryEvent> = DefaultSampleSyncHistory,
    onSyncNow: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sync_spin"
    )

    val healthStatusColor = when {
        isSyncing -> MaterialTheme.colorScheme.primary
        pendingItemsCount == 0 -> Color(0xFF34C759)
        else -> Color(0xFFFF9500)
    }

    val healthIcon = when {
        isSyncing -> Icons.Rounded.Sync
        pendingItemsCount == 0 -> Icons.Rounded.CheckCircle
        else -> Icons.Rounded.Pending
    }

    val healthTitle = when {
        isSyncing -> "Syncing in progress"
        pendingItemsCount == 0 -> "Everything up to date"
        pendingItemsCount == 1 -> "1 item pending sync"
        else -> "$pendingItemsCount items pending sync"
    }

    val healthSubtitle = when {
        isSyncing -> "Exchanging latest changes with paired devices"
        pendingItemsCount == 0 -> "Last synchronized $lastSyncTimeText"
        pendingItemsCount == 1 -> "Ready to synchronize with paired devices"
        else -> "Changes will sync when a paired device connects"
    }

    val categories = remember(studyTasksCount, notesRemindersCount, focusSessionsCount, settingsTagsCount) {
        listOf(
            SyncedDataCategory(
                title = "Study Tasks",
                count = studyTasksCount,
                subtitle = if (studyTasksCount == 1) "1 item" else "$studyTasksCount items",
                icon = Icons.Rounded.TaskAlt
            ),
            SyncedDataCategory(
                title = "Notes & Reminders",
                count = notesRemindersCount,
                subtitle = if (notesRemindersCount == 1) "1 item" else "$notesRemindersCount items",
                icon = Icons.Rounded.Description
            ),
            SyncedDataCategory(
                title = "Focus Sessions",
                count = focusSessionsCount,
                subtitle = if (focusSessionsCount == 1) "1 item" else "$focusSessionsCount items",
                icon = Icons.Rounded.Timer
            ),
            SyncedDataCategory(
                title = "Settings & Tags",
                count = settingsTagsCount,
                subtitle = if (settingsTagsCount == 1) "1 item" else "$settingsTagsCount items",
                icon = Icons.Rounded.Tune
            )
        )
    }

    ScholarCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Synced Data Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Everyday content and activity across your devices",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = healthStatusColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, healthStatusColor.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = if (isSyncing) "SYNCING" else if (pendingItemsCount == 0) "UP TO DATE" else "PENDING",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = healthStatusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sync Health & Quick Action Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = healthStatusColor.copy(alpha = 0.08f),
                border = BorderStroke(
                    width = 1.dp,
                    color = healthStatusColor.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(healthStatusColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = healthIcon,
                                contentDescription = null,
                                tint = healthStatusColor,
                                modifier = Modifier
                                    .size(22.dp)
                                    .then(if (isSyncing) Modifier.rotate(rotationAngle) else Modifier)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = healthTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = healthSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    BouncyButton(
                        onClick = onSyncNow,
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Sync,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .then(if (isSyncing) Modifier.rotate(rotationAngle) else Modifier)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSyncing) "Syncing" else "Sync Now",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Synced Data Summary: 2x2 Grid
            Text(
                text = "Synced Content",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Row 1: Study Tasks & Notes & Reminders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SyncedCategoryTile(
                    category = categories[0],
                    modifier = Modifier.weight(1f)
                )
                SyncedCategoryTile(
                    category = categories[1],
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Focus Sessions & Settings & Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SyncedCategoryTile(
                    category = categories[2],
                    modifier = Modifier.weight(1f)
                )
                SyncedCategoryTile(
                    category = categories[3],
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(16.dp))

            // Sync History List
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recent Sync History",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${historyEvents.size} events",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
                border = BorderStroke(
                    width = 0.8.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    if (historyEvents.isEmpty()) {
                        Text(
                            text = "No recent sync events recorded yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        historyEvents.forEachIndexed { index, event ->
                            SyncHistoryItemRow(event = event)
                            if (index < historyEvents.size - 1) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncedCategoryTile(
    category: SyncedDataCategory,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            width = 0.8.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Text(
                    text = "${category.count}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = category.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!category.subtitle.isNullOrEmpty()) {
                Text(
                    text = category.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SyncHistoryItemRow(
    event: SyncHistoryEvent,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = event.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(13.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "${event.description} • ${event.timestampText}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
