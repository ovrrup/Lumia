package lumia.tracker.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.screens.settings.components.SettingsActionItemInCard
import lumia.tracker.ui.screens.settings.components.SettingsGroupCard
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * SettingsScreen - Clean, structured academic preferences and configuration hub.
 * Logically organized into 4 distinct groups with zero redundancy or duplicate entries.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    BouncyIconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Personalization Group
            SettingsGroupCard(title = "Personalization", icon = Icons.Rounded.Palette) {
                SettingsActionItemInCard(
                    title = "Appearance & Theme",
                    subtitle = "Themes, AMOLED pure black, lighting & custom palettes",
                    icon = Icons.Rounded.Palette,
                    iconBgColor = androidx.compose.ui.graphics.Color(0xFF007AFF),
                    onClick = { navController.navigate("settings/appearance") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(start = 48.dp)
                )

                SettingsActionItemInCard(
                    title = "Streak Goals & Requirements",
                    subtitle = "Daily targets, completion thresholds & motivational tones",
                    icon = Icons.Rounded.LocalFireDepartment,
                    iconBgColor = androidx.compose.ui.graphics.Color(0xFFFF9500),
                    onClick = { navController.navigate("settings/streaks") }
                )
            }

            // 2. Academic System Configuration
            SettingsGroupCard(title = "Academic & Study System", icon = Icons.Rounded.School) {
                SettingsActionItemInCard(
                    title = "Tag Management",
                    subtitle = "Customize tag colors and global taxonomies",
                    icon = Icons.Rounded.LocalOffer,
                    iconBgColor = androidx.compose.ui.graphics.Color(0xFF30B0C7),
                    onClick = { navController.navigate("tags_hub") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(start = 48.dp)
                )

                SettingsActionItemInCard(
                    title = "System Configuration",
                    subtitle = "Course-subject linking, synergy scoring & Pomodoro defaults",
                    icon = Icons.Rounded.Settings,
                    iconBgColor = androidx.compose.ui.graphics.Color(0xFF5856D6),
                    onClick = { navController.navigate("settings/system") }
                )
            }

            // 3. Security & Notifications
            SettingsGroupCard(title = "Security & Alerts", icon = Icons.Rounded.Lock) {
                SettingsActionItemInCard(
                    title = "Safety System Guard",
                    subtitle = "App PIN lock, biometric protection & safety alerts",
                    icon = Icons.Rounded.Lock,
                    iconBgColor = androidx.compose.ui.graphics.Color(0xFFFF3B30),
                    onClick = { navController.navigate("settings/safety") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(start = 48.dp)
                )

                SettingsActionItemInCard(
                    title = "Notifications & Reminders",
                    subtitle = "Notification channels, study alarms & reminders",
                    icon = Icons.Rounded.Notifications,
                    iconBgColor = androidx.compose.ui.graphics.Color(0xFF34C759),
                    onClick = { navController.navigate("settings/notifications") }
                )
            }

            // 4. Data, Sync & Accounts
            SettingsGroupCard(title = "Data & Connectivity", icon = Icons.Rounded.Storage) {
                SettingsActionItemInCard(
                    title = "Multi-Device P2P Sync",
                    subtitle = "End-to-end encrypted device syncing with 1-time mutual pairing",
                    icon = Icons.Rounded.Autorenew,
                    iconBgColor = androidx.compose.ui.graphics.Color(0xFFAF52DE),
                    onClick = { navController.navigate("settings/sync") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(start = 48.dp)
                )

                SettingsActionItemInCard(
                    title = "Data & Backups",
                    subtitle = "Export, import, database backups & complete resets",
                    icon = Icons.Rounded.Storage,
                    iconBgColor = androidx.compose.ui.graphics.Color(0xFF8E8E93),
                    onClick = { navController.navigate("settings/data") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(start = 48.dp)
                )

                SettingsActionItemInCard(
                    title = "Scholar Profiles",
                    subtitle = "Active: ${activeProfile.name.ifBlank { "Scholar" }} • Multi-profile workspaces",
                    icon = Icons.Rounded.Person,
                    iconBgColor = androidx.compose.ui.graphics.Color(0xFFFF2D55),
                    onClick = { navController.navigate("profile_menu") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(start = 48.dp)
                )

                SettingsActionItemInCard(
                    title = "About Lumia",
                    subtitle = "Version v1.0.7, license & open source repository",
                    icon = Icons.Rounded.Info,
                    iconBgColor = androidx.compose.ui.graphics.Color(0xFF636366),
                    onClick = { navController.navigate("settings/about") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
