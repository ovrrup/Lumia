package lumia.tracker.ui.screens.home.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.text.style.TextOverflow
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
 * ScholarInnovativeHeader - Dynamic Island & Action Capsule Bar.
 * Unifies context-aware tab title/search capsule, responsive Focus pill with live countdown,
 * streak widget, and iOS avatar capsule in uniform 42dp height.
 */
@ValueScore(
    score = 96,
    importance = Importance.CRITICAL,
    description = "Dynamic Island inspired action capsule header unifying tab-aware search capsule, focus pill, streak widget, and profile avatar with 42dp uniform capsule height",
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

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Search / Tab Title Action Capsule (Reflects active tab context)
                ScholarSearchCapsule(
                    selectedTab = selectedTab,
                    onClick = { navController.navigate("search") },
                    modifier = Modifier.weight(1f)
                )

                // 2. Responsive Live Focus Countdown Pill
                ScholarFocusPill(
                    isRunning = pomodoroState.isRunning,
                    timeLeft = pomodoroState.timeLeft,
                    onClick = { navController.navigate("pomodoro") }
                )

                // 3. Streak Flame Badge Widget (Interactive bottom sheet)
                StreakWidget(
                    viewModel = viewModel,
                    navController = navController,
                    modifier = Modifier.height(42.dp)
                )

                // 4. iOS Profile Avatar Capsule (Settings navigation)
                ScholarProfileAvatar(
                    avatarEmoji = activeProfile.avatarEmoji,
                    displayName = activeProfile.name,
                    onClick = { navController.navigate("settings") }
                )
            }
        }
    }
}

/**
 * ScholarSearchCapsule - Search bar capsule button triggering search navigation,
 * dynamically updating title and icon based on active tab with uniform 42dp height.
 */
@ValueScore(
    score = 88,
    importance = Importance.HIGH,
    description = "Tab-responsive search and title capsule button triggering search workspace with animated tab title transitions and uniform 42dp height",
    category = "Navigation"
)
@Composable
fun ScholarSearchCapsule(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedTab: Int = 0
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            0.75.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        shadowElevation = 0.5.dp,
        tonalElevation = 1.dp,
        modifier = modifier
            .height(42.dp)
            .clip(CircleShape)
            .bouncyClick(onClick = onClick)
            .testTag("open_search_button")
    ) {
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, delayMillis = 40)) +
                        slideInVertically(animationSpec = tween(220)) { height -> height / 2 })
                    .togetherWith(
                        fadeOut(animationSpec = tween(150)) +
                                slideOutVertically(animationSpec = tween(150)) { height -> -height / 2 }
                    )
            },
            label = "header_title_transition",
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) { targetTab ->
            val (icon, title) = when (targetTab) {
                0 -> Icons.Rounded.Search to "Search workspace..."
                1 -> Icons.AutoMirrored.Rounded.MenuBook to "Academics"
                2 -> Icons.Rounded.AutoStories to "Tasks"
                3 -> Icons.Rounded.Analytics to "Analytics"
                else -> Icons.Rounded.Search to "Search workspace..."
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (targetTab == 0) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = if (targetTab == 0) {
                        MaterialTheme.typography.bodyMedium
                    } else {
                        MaterialTheme.typography.titleSmall
                    },
                    fontWeight = if (targetTab == 0) {
                        FontWeight.Normal
                    } else {
                        FontWeight.SemiBold
                    },
                    color = if (targetTab == 0) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * ScholarFocusPill - Dynamic Island live countdown pill with pulse equalizer wave and smooth transitions.
 */
@ValueScore(
    score = 93,
    importance = Importance.CRITICAL,
    description = "Live Focus/Pomodoro countdown pill with active equalizer wave animation, smooth color transitions, and uniform 42dp height",
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

    // Equalizer wave pulse animation when active
    val infiniteTransition = rememberInfiniteTransition(label = "focus_pulse")
    val eqScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq_scale"
    )

    Surface(
        shape = CircleShape,
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
            .clip(CircleShape)
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
                        .background(MaterialTheme.colorScheme.onPrimary, CircleShape)
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

            val mins = (timeLeft / 60).coerceAtLeast(0)
            val secs = (timeLeft % 60).coerceAtLeast(0)
            val timerStr = String.format(Locale.US, "%02d:%02d", mins, secs)

            AnimatedContent(
                targetState = isRunning to (if (isRunning) timerStr else "Focus"),
                transitionSpec = {
                    (fadeIn(animationSpec = tween(200)) + slideInVertically { it / 3 })
                        .togetherWith(fadeOut(animationSpec = tween(150)) + slideOutVertically { -it / 3 })
                },
                label = "focus_pill_text_transition"
            ) { (_, labelText) ->
                Text(
                    text = labelText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = focusPillText,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * ScholarProfileAvatar - iOS profile avatar capsule with monogram or image and direct settings navigation.
 */
@ValueScore(
    score = 82,
    importance = Importance.HIGH,
    description = "Avatar capsule displaying user profile emoji, photo, or monogram with direct settings navigation and uniform 42dp diameter",
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
            .bouncyClick(onClick = onClick)
            .testTag("profile_avatar_button"),
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
