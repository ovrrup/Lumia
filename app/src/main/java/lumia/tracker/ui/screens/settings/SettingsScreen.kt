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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    BouncyIconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
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
                    subtitle = "Themes, AMOLED pure black, lighting & custom color palettes",
                    icon = Icons.Rounded.Palette,
                    onClick = { navController.navigate("settings/appearance") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "Streak Goals & Requirements",
                    subtitle = "Daily targets, completion thresholds & motivational tones",
                    icon = Icons.Rounded.LocalFireDepartment,
                    onClick = { navController.navigate("settings/streaks") }
                )
            }

            // 2. Academic System Configuration
            SettingsGroupCard(title = "Academic & Study System", icon = Icons.Rounded.School) {
                SettingsActionItemInCard(
                    title = "Tag Management",
                    subtitle = "Customize tag colors and global taxonomies",
                    icon = Icons.Rounded.LocalOffer,
                    onClick = { navController.navigate("tags_hub") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "System Configuration",
                    subtitle = "Course-subject linking, synergy scoring & Pomodoro defaults",
                    icon = Icons.Rounded.Settings,
                    onClick = { navController.navigate("settings/system") }
                )
            }

            // 3. Security & Notifications
            SettingsGroupCard(title = "Security & Alerts", icon = Icons.Rounded.Lock) {
                SettingsActionItemInCard(
                    title = "Safety System Guard",
                    subtitle = "App PIN lock, biometric protection & safety alerts",
                    icon = Icons.Rounded.Lock,
                    onClick = { navController.navigate("settings/safety") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "Notifications & Reminders",
                    subtitle = "Notification channels, study alarms & reminders",
                    icon = Icons.Rounded.Notifications,
                    onClick = { navController.navigate("settings/notifications") }
                )
            }

            // 4. Data, Sync & Accounts
            SettingsGroupCard(title = "Data & Connectivity", icon = Icons.Rounded.Storage) {
                SettingsActionItemInCard(
                    title = "Multi-Device P2P Sync",
                    subtitle = "End-to-end encrypted device syncing with 1-time mutual pairing",
                    icon = Icons.Rounded.Autorenew,
                    onClick = { navController.navigate("settings/sync") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "Data & Backups",
                    subtitle = "Export, import, database backups & complete resets",
                    icon = Icons.Rounded.Storage,
                    onClick = { navController.navigate("settings/data") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "Scholar Profiles",
                    subtitle = "Active: ${activeProfile.name.ifBlank { "Scholar" }} • Multi-profile workspaces",
                    icon = Icons.Rounded.Person,
                    onClick = { navController.navigate("profile_menu") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "About Lumia",
                    subtitle = "Version v1.0.7, license & open source repository",
                    icon = Icons.Rounded.Info,
                    onClick = { navController.navigate("settings/about") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
