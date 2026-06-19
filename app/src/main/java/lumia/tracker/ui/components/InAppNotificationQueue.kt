package lumia.tracker.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import lumia.tracker.viewmodel.ScholarViewModel.InAppNotification

@Composable
fun InAppNotificationQueue(
    notifications: List<InAppNotification>,
    onDismissNotification: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 40.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Show up to 3 notifications sequentially
            notifications.take(3).forEach { notification ->
                key(notification.id) {
                    InAppNotificationItem(
                        notification = notification,
                        onDismiss = { onDismissNotification(notification.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun InAppNotificationItem(
    notification: InAppNotification,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        // Stay active for 3.5 seconds
        delay(3500)
        visible = false
        // Delay complete closure animation
        delay(300)
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable { visible = false },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Customized game badge badge icons based on notification type
                val cardColor = when (notification.type) {
                    "ACHIEVEMENT" -> MaterialTheme.colorScheme.tertiary
                    "CREDITS" -> MaterialTheme.colorScheme.secondary
                    "POINTS" -> MaterialTheme.colorScheme.primary
                    "XP" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.outline
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(cardColor.copy(alpha = 0.15f))
                        .border(1.dp, cardColor.copy(alpha = 0.4f), CircleShape)
                ) {
                    if (notification.iconEmoji.isNotBlank()) {
                        Text(
                            text = notification.iconEmoji,
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else {
                        val iconVector = when (notification.type) {
                            "ACHIEVEMENT" -> Icons.Rounded.Star
                            "CREDITS" -> Icons.Rounded.Savings
                            "POINTS" -> Icons.Rounded.MilitaryTech
                            "XP" -> Icons.Rounded.Bolt
                            else -> Icons.Rounded.Info
                        }
                        Icon(
                            imageVector = iconVector,
                            contentDescription = "",
                            tint = cardColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = { visible = false },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Dismiss Notification",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
