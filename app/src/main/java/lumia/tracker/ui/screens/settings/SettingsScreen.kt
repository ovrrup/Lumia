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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings Hub", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
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
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Personalization Group
            SettingsGroupCard(title = "Personalization", icon = Icons.Rounded.Palette) {
                SettingsActionItemInCard(
                    title = "Appearance & Theme",
                    subtitle = "Themes, color palettes, dynamic colors & layout modifiers",
                    icon = Icons.Rounded.Palette,
                    onClick = { navController.navigate("settings/appearance") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "Streak Goals & Visuals",
                    subtitle = "Configure percentage limits, fire color, and liquid animations",
                    icon = Icons.Rounded.LocalFireDepartment,
                    onClick = { navController.navigate("settings/streaks") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Academic Tools Group
            SettingsGroupCard(title = "Academic Experience", icon = Icons.Rounded.School) {
                SettingsActionItemInCard(
                    title = "Tag Management",
                    subtitle = "Customize tag aesthetics and academic connections",
                    icon = Icons.Rounded.LocalOffer,
                    onClick = { navController.navigate("tags_hub") }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "System Configuration",
                    subtitle = "Advanced background features, course-subject fusion, and interconnections",
                    icon = Icons.Rounded.Settings,
                    onClick = { navController.navigate("settings/system") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                
                SettingsActionItemInCard(
                    title = "Experimental Features",
                    subtitle = "Quick tools, floating bars, and beta layouts",
                    icon = Icons.Rounded.Science,
                    onClick = { navController.navigate("settings/beta") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Safety & Notifications Group
            SettingsGroupCard(title = "Alerts & Security", icon = Icons.Rounded.Lock) {
                SettingsActionItemInCard(
                    title = "Safety System Guard",
                    subtitle = "Automatic alerts, security PIN and smart recommendations",
                    icon = Icons.Rounded.Lock,
                    onClick = { navController.navigate("settings/safety") }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "Notifications Management",
                    subtitle = "Tones, schedules, and active task alerts",
                    icon = Icons.Rounded.Notifications,
                    onClick = { navController.navigate("settings/notifications") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Storage & Synchronization Group
            SettingsGroupCard(title = "Storage & Synchronization", icon = Icons.Rounded.Storage) {
                SettingsActionItemInCard(
                    title = "Multi-Device P2P Sync",
                    subtitle = "Zero-trust WebRTC & local network synchronization with QR/PIN pairing",
                    icon = Icons.Rounded.Autorenew,
                    onClick = { navController.navigate("settings/sync") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "Database & Management",
                    subtitle = "Manage secure active backups, exports & schema resets",
                    icon = Icons.Rounded.Storage,
                    onClick = { navController.navigate("settings/data") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "User Account & Profiles",
                    subtitle = "Active: ${activeProfile.name} • Manage multi-profile workspaces",
                    icon = Icons.Rounded.Person,
                    onClick = { navController.navigate("profile_menu") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "About App",
                    subtitle = "Developer info, update status & open source details",
                    icon = Icons.Rounded.Info,
                    onClick = { navController.navigate("settings/about") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
