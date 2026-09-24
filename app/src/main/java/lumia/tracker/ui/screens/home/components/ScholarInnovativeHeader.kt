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
 * ScholarInnovativeHeader - Modern Glassmorphic Action Capsule Header.
 * Unifies minimalist tab title, responsive Focus live pill, streak widget,
 * search circular capsule button, and profile avatar in sleek capsule design.
 */
@ValueScore(
    score = 96,
    importance = Importance.CRITICAL,
    description = "Modern glassmorphic capsule header unifying minimalist tab title, live focus pill, streak widget, and profile avatar",
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

    val tabTitle = when (selectedTab) {
        0 -> "Home"
        1 -> "Classes"
        2 -> "Tasks"
        3 -> "Progress"
        else -> "Home"
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.82f),
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        ),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState = tabTitle,
                transitionSpec = {
                    (fadeIn(tween(180)) + slideInVertically(tween(180)) { it / 3 })
                        .togetherWith(fadeOut(tween(140)) + slideOutVertically(tween(140)) { -it / 3 })
                },
                label = "header_title",
                modifier = Modifier.weight(1f)
            ) { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pomodoroState.isRunning) {
                    ScholarFocusPill(
                        isRunning = true,
                        timeLeft = pomodoroState.timeLeft,
                        onClick = { navController.navigate("pomodoro") }
                    )
                }

                StreakWidget(
                    viewModel = viewModel,
                    navController = navController
                )

                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .bouncyClick(onClick = { navController.navigate("search") }),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f),
                    border = BorderStroke(
                        0.5.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                ScholarProfileAvatar(
                    avatarEmoji = activeProfile.avatarEmoji,
                    displayName = activeProfile.name,
                    onClick = { navController.navigate("settings") },
                    modifier = Modifier.size(40.dp)
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
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.70f),
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
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
                0 -> Icons.Rounded.Search to "Search..."
                1 -> Icons.AutoMirrored.Rounded.MenuBook to "Academics"
                2 -> Icons.Rounded.AutoStories to "Tasks"
                3 -> Icons.Rounded.Analytics to "Analytics"
                else -> Icons.Rounded.Search to "Search..."
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
    description = "Live Focus/Pomodoro countdown pill with active equalizer wave animation, smooth color transitions, and uniform 40dp height",
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
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.70f)
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

    // Pulse animation when active
    val infiniteTransition = rememberInfiniteTransition(label = "focus_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        shape = CircleShape,
        color = focusPillBg,
        border = BorderStroke(
            0.5.dp,
            if (isRunning) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            }
        ),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = modifier
            .height(40.dp)
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
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                        .background(MaterialTheme.colorScheme.onPrimary, CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = "Focus Space",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            val mins = (timeLeft / 60).coerceAtLeast(0)
            val secs = (timeLeft % 60).coerceAtLeast(0)
            val timerStr = String.format(Locale.US, "%02d:%02d", mins, secs)

            AnimatedContent(
                targetState = if (isRunning) timerStr else "Focus",
                transitionSpec = {
                    (fadeIn(animationSpec = tween(200)) + slideInVertically { it / 3 })
                        .togetherWith(fadeOut(animationSpec = tween(150)) + slideOutVertically { -it / 3 })
                },
                label = "focus_pill_text_transition"
            ) { labelText ->
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
    description = "Avatar capsule displaying user profile emoji, photo, or monogram with direct settings navigation and uniform 40dp diameter",
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
            .size(40.dp)
            .shadow(elevation = 0.dp, shape = CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.70f), CircleShape)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f), CircleShape)
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
