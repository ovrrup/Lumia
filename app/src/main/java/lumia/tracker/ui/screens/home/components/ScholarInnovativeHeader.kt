package lumia.tracker.ui.screens.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import lumia.tracker.ui.components.StreakWidget
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * ScholarInnovativeHeader - Re-innovated modern header bar for Lumia Dashboard.
 * Features time-aware greetings, current academic date indicator, search capsule,
 * 1-tap focus launcher, live streak indicator, and scholar profile avatar.
 */
@Composable
fun ScholarInnovativeHeader(
    selectedTab: Int,
    navController: NavController,
    viewModel: ScholarViewModel,
    modifier: Modifier = Modifier
) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()

    // Determine time-aware greeting
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }
    }

    // Formatted current date indicator (e.g. "Monday, Aug 24")
    val currentDateStr = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Calendar.getInstance().time)
    }

    // Dynamic header title and subtitle based on current tab
    val (headerTitle, headerSubtitle) = when (selectedTab) {
        0 -> {
            val name = activeProfile.name.ifBlank { "Scholar" }
            "$greeting, $name" to currentDateStr
        }
        1 -> "Courses Hub" to "Academic Curriculum"
        2 -> "Subjects Index" to "Study Topics & Chapters"
        3 -> "Tasks & Deadlines" to "Self Study Planner"
        4 -> "Academic Analytics" to "Performance & Progress"
        else -> "Lumia Study OS" to "Companion Workspace"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = if (betaEnhancedHeader) MaterialTheme.colorScheme.surface.copy(alpha = 0.95f) else MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Header Title + Context/Date Subtitle
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                AnimatedContent(
                    targetState = headerTitle,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "header_title"
                ) { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = headerSubtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }

            // Right: Action cluster (Search, Focus shortcut, Streak, Profile Avatar)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Search Pill Button
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(38.dp)
                        .bouncyClick(onClick = { navController.navigate("search") })
                        .testTag("open_search_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // 2. 1-Tap Pomodoro Focus Shortcut
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                    modifier = Modifier
                        .size(38.dp)
                        .bouncyClick(onClick = { navController.navigate("pomodoro") })
                        .testTag("open_pomodoro_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = "Focus Space",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // 3. Streak Counter Flame Widget
                StreakWidget(viewModel = viewModel, navController = navController)

                // 4. Active Profile Avatar Capsule
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(elevation = 2.dp, shape = CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                        .clip(CircleShape)
                        .bouncyClick(onClick = { navController.navigate("profile_menu") }),
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
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
