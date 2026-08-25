package lumia.tracker.ui.screens.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import lumia.tracker.service.PomodoroService
import lumia.tracker.ui.components.StreakWidget
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel
import java.util.Locale

/**
 * ScholarInnovativeHeader - iOS Dynamic Island & Action Capsule.
 * Displays live Focus state, search pill, streak status, and profile capsule in a cohesive Apple-grade bar.
 */
@Composable
fun ScholarInnovativeHeader(
    selectedTab: Int,
    navController: NavController,
    viewModel: ScholarViewModel,
    modifier: Modifier = Modifier
) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val pomodoroState by PomodoroService.state.collectAsStateWithLifecycle()
    val isFocusRunning = pomodoroState.isRunning

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. iOS Search Bar Pill
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                shadowElevation = 0.5.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .bouncyClick(onClick = { navController.navigate("search") })
                    .testTag("open_search_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = "Search workspace...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }

            // 2. iOS Dynamic Focus Live Activity Pill
            val focusPillBg by animateColorAsState(
                targetValue = if (isFocusRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                label = "focus_pill_color"
            )
            val focusPillText by animateColorAsState(
                targetValue = if (isFocusRunning) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                label = "focus_pill_text_color"
            )

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = focusPillBg,
                border = BorderStroke(
                    0.6.dp,
                    if (isFocusRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ),
                shadowElevation = if (isFocusRunning) 2.dp else 0.5.dp,
                modifier = Modifier
                    .height(40.dp)
                    .bouncyClick(onClick = { navController.navigate("pomodoro") })
                    .testTag("open_pomodoro_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isFocusRunning) Icons.Rounded.GraphicEq else Icons.Rounded.Timer,
                        contentDescription = "Focus Space",
                        tint = focusPillText,
                        modifier = Modifier.size(17.dp)
                    )
                    val mins = pomodoroState.timeLeft / 60
                    val secs = pomodoroState.timeLeft % 60
                    val timerStr = String.format(Locale.US, "%02d:%02d", mins, secs)

                    Text(
                        text = if (isFocusRunning) timerStr else "Focus",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = focusPillText
                    )
                }
            }

            // 3. Streak Flame Badge
            StreakWidget(
                viewModel = viewModel,
                navController = navController
            )

            // 4. iOS Profile Avatar Capsule
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .shadow(elevation = 1.dp, shape = CircleShape)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clip(CircleShape)
                    .bouncyClick(onClick = { navController.navigate("settings") }),
                contentAlignment = Alignment.Center
            ) {
                val isLocalImage = activeProfile.avatarEmoji.startsWith("/") ||
                        activeProfile.avatarEmoji.startsWith("file://") ||
                        activeProfile.avatarEmoji.startsWith("content://")
                if (isLocalImage) {
                    AsyncImage(
                        model = activeProfile.avatarEmoji,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val fallback = if (activeProfile.avatarEmoji.isNotBlank() &&
                        activeProfile.avatarEmoji.length <= 3 &&
                        !activeProfile.avatarEmoji.startsWith("/")
                    ) {
                        activeProfile.avatarEmoji.uppercase()
                    } else {
                        activeProfile.name.take(2).uppercase().ifBlank { "SC" }
                    }
                    Text(
                        text = fallback,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
