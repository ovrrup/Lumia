package lumia.tracker.ui.screens.home.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import lumia.tracker.service.PomodoroService
import lumia.tracker.ui.components.StreakWidget
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel
import java.util.Locale

/**
 * ScholarInnovativeHeader - Modern Dynamic Island & Action Capsule Bar.
 * Unifies search capsule (42dp, 22dp radius), responsive Focus pill with live countdown,
 * aligned streak badge, and avatar capsule with instant settings navigation.
 */
@ValueScore(
    score = 96,
    importance = Importance.CRITICAL,
    description = "Dynamic Island inspired action capsule header unifying search, focus pill, streak widget, and profile avatar",
    category = "Navigation"
)
@Composable
fun ScholarInnovativeHeader(
    selectedTab: Int,
    navController: NavController,
    viewModel: ScholarViewModel,
    modifier: Modifier = Modifier
) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val pomodoroState by PomodoroService.state.collectAsStateWithLifecycle()

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
            // 1. Search Bar Action Capsule
            ScholarSearchCapsule(
                onClick = { navController.navigate("search") },
                modifier = Modifier.weight(1f)
            )

            // 2. Responsive Live Focus Pill
            ScholarFocusPill(
                isRunning = pomodoroState.isRunning,
                timeLeft = pomodoroState.timeLeft,
                onClick = { navController.navigate("pomodoro") }
            )

            // 3. Streak Flame Badge Widget (interactive with bottom sheet)
            StreakWidget(
                viewModel = viewModel,
                navController = navController,
                modifier = Modifier.height(42.dp)
            )

            // 4. iOS Profile Avatar Capsule
            ScholarProfileAvatar(
                avatarEmoji = activeProfile.avatarEmoji,
                displayName = activeProfile.name,
                onClick = { navController.navigate("settings") }
            )
        }
    }
}

/**
 * ScholarSearchCapsule - Search bar capsule button triggering search navigation.
 */
@ValueScore(
    score = 87,
    importance = Importance.HIGH,
    description = "Search bar action capsule button with quick trigger to search workspace",
    category = "Navigation"
)
@Composable
fun ScholarSearchCapsule(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            0.75.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        shadowElevation = 0.5.dp,
        tonalElevation = 1.dp,
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(22.dp))
            .bouncyClick(onClick = onClick)
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
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Search workspace...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

/**
 * ScholarFocusPill - Dynamic Island live countdown pill with pulse and equalizer animation.
 */
@ValueScore(
    score = 93,
    importance = Importance.CRITICAL,
    description = "Live Focus/Pomodoro countdown pill with active equalizer animation and dynamic color transition",
    category = "Focus"
)
@Composable
fun ScholarFocusPill(
    isRunning: Boolean,
    timeLeft: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusPillBg by animateColorAsState(
        targetValue = if (isRunning) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "focus_pill_color"
    )
    val focusPillText by animateColorAsState(
        targetValue = if (isRunning) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "focus_pill_text_color"
    )

    // Equalizer wave animation when active
    val infiniteTransition = rememberInfiniteTransition(label = "focus_pulse")
    val eqScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq_scale"
    )

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = focusPillBg,
        border = BorderStroke(
            0.75.dp,
            if (isRunning) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            }
        ),
        shadowElevation = if (isRunning) 2.5.dp else 0.5.dp,
        tonalElevation = if (isRunning) 4.dp else 1.dp,
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(22.dp))
            .bouncyClick(onClick = onClick)
            .testTag("open_pomodoro_button")
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isRunning) {
                // Pulsing dot indicator
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .graphicsLayer {
                            scaleX = eqScale
                            scaleY = eqScale
                        }
                        .background(Color.White, CircleShape)
                )
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = "Focus Active",
                    tint = focusPillText,
                    modifier = Modifier.size(17.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = "Focus Space",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            val mins = timeLeft / 60
            val secs = timeLeft % 60
            val timerStr = String.format(Locale.US, "%02d:%02d", mins, secs)

            Text(
                text = if (isRunning) timerStr else "Focus",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = focusPillText
            )
        }
    }
}

/**
 * ScholarProfileAvatar - iOS profile avatar capsule with monogram or image and direct settings navigation.
 */
@ValueScore(
    score = 82,
    importance = Importance.HIGH,
    description = "Avatar capsule displaying user profile emoji or monogram with direct settings navigation",
    category = "Navigation"
)
@Composable
fun ScholarProfileAvatar(
    avatarEmoji: String,
    displayName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .shadow(elevation = 1.5.dp, shape = CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.75f), CircleShape)
            .clip(CircleShape)
            .bouncyClick(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val isLocalImage = avatarEmoji.startsWith("/") ||
                avatarEmoji.startsWith("file://") ||
                avatarEmoji.startsWith("content://")
        if (isLocalImage) {
            AsyncImage(
                model = avatarEmoji,
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val fallback = if (avatarEmoji.isNotBlank() &&
                avatarEmoji.length <= 3 &&
                !avatarEmoji.startsWith("/")
            ) {
                avatarEmoji.uppercase()
            } else {
                displayName.take(2).uppercase().ifBlank { "SC" }
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
