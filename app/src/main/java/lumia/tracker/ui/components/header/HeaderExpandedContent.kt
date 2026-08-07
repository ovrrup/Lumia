package lumia.tracker.ui.components.header

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.theme.LocalGlassMode
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel

@Composable
fun HeaderExpandedQuickHub(
    title: String,
    viewModel: ScholarViewModel? = null,
    navController: NavController? = null,
    onCollapse: () -> Unit
) {
    val isGlass = LocalGlassMode.current
    val activeProfile = viewModel?.activeProfile?.collectAsStateWithLifecycle()?.value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Header Title Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Quick Hub & Quick Actions",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (activeProfile != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = activeProfile.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowUp,
                        contentDescription = "Collapse Header",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live Dashboard Stats Badges
        HeaderDashboardStatsBadges(viewModel)

        Spacer(modifier = Modifier.height(10.dp))

        // Embedded Quick Note Creator
        HeaderQuickNoteInput(viewModel)

        Spacer(modifier = Modifier.height(10.dp))

        // Action Shortcuts Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderActionButton(
                icon = Icons.Rounded.Timer,
                label = "Focus",
                onClick = { navController?.navigate("pomodoro") }
            )
            HeaderActionButton(
                icon = Icons.Rounded.Search,
                label = "Search",
                onClick = { navController?.navigate("search") }
            )
            HeaderActionButton(
                icon = Icons.Rounded.ColorLens,
                label = "Glass UI",
                onClick = {
                    if (viewModel != null) {
                        viewModel.updateBetaGlassUi(!isGlass)
                    }
                }
            )
            HeaderActionButton(
                icon = Icons.Rounded.Person,
                label = "Profiles",
                onClick = { navController?.navigate("profile_menu") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Collapse Push Handle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .bouncyClick { onCollapse() }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Push Up to Collapse",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
