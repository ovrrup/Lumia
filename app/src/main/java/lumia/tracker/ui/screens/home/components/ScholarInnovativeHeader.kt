package lumia.tracker.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import lumia.tracker.ui.components.StreakWidget
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * ScholarInnovativeHeader - Dissolved serialized action header.
 * Eliminates broad heavy app banners in favor of a minimal, floating horizontal
 * cluster of essential action buttons and live unclickable indicators.
 */
@Composable
fun ScholarInnovativeHeader(
    selectedTab: Int,
    navController: NavController,
    viewModel: ScholarViewModel,
    modifier: Modifier = Modifier
) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Serialized Button: Quick Search Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f),
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
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Search...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            // 2. Serialized Button: 1-Tap Pomodoro Focus Space Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                modifier = Modifier
                    .height(40.dp)
                    .bouncyClick(onClick = { navController.navigate("pomodoro") })
                    .testTag("open_pomodoro_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = "Focus Space",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Focus",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // 3. Serialized Indicator: Unclickable Streak Flame Counter
            StreakWidget(
                viewModel = viewModel,
                navController = navController
            )

            Spacer(Modifier.width(8.dp))

            // 4. Serialized Button: Active Profile Avatar Capsule
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
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
