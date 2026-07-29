package lumia.tracker.ui.screens

import android.content.Intent
import android.provider.Settings
import lumia.tracker.ui.theme.LocalDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.rounded.Info
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current
    val isDark = LocalDarkTheme.current
    val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val betaGlassUi by viewModel.betaGlassUi.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background
        ) { padding ->
            val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = statusBarHeight + 64.dp, bottom = 24.dp),
            ) {

            // Personalization Card
            SettingsGroupCard(title = "Personalization", icon = Icons.Rounded.Palette) {
                SettingsActionItemInCard(
                    title = "Appearance & Theme",
                    subtitle = "Themes, color palettes, and layout modifiers",
                    icon = Icons.Rounded.Palette,
                    onClick = { navController.navigate("settings/appearance") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
            SettingsGroupCard(title = "Profile Selection", icon = Icons.Rounded.Person) {
                val context = androidx.compose.ui.platform.LocalContext.current
                SettingsActionItemInCard(
                    title = "Switch Profile",
                    subtitle = "Currently using: ${activeProfile.name}",
                    icon = Icons.Rounded.SwapHoriz,
                    onClick = { 
                        // Tell main activity to show profile selector
                        val intent = android.content.Intent(context, lumia.tracker.MainActivity::class.java)
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.putExtra("OPEN_PROFILE_SELECTOR", true)
                        if (context is android.app.Activity) {
                            context.finish()
                        }
                        context.startActivity(intent)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // System configuration
            SettingsGroupCard(title = "System Details", icon = Icons.Rounded.Settings) {
                SettingsActionItemInCard(
                    title = "System Configuration",
                    subtitle = "Advanced background features and interconnections",
                    icon = Icons.Rounded.Settings,
                    onClick = { navController.navigate("settings/system") }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "Tag Management",
                    subtitle = "Customize tag aesthetics and view their academic connections",
                    icon = Icons.Rounded.LocalOffer,
                    onClick = { navController.navigate("tags_hub") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Notifications Card
            SettingsGroupCard(title = "Alerts & Notifications", icon = Icons.Rounded.Notifications) {
                SettingsActionItemInCard(
                    title = "Notifications Management",
                    subtitle = "Tones, schedules, and active alerts",
                    icon = Icons.Rounded.Notifications,
                    onClick = { navController.navigate("settings/notifications") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Data Management
            SettingsGroupCard(title = "Storage & Versioning", icon = Icons.Rounded.Storage) {
                SettingsActionItemInCard(
                    title = "Database & Management",
                    subtitle = "Manage secure active backups, exports & resets",
                    icon = Icons.Rounded.Storage,
                    onClick = { navController.navigate("settings/data") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "About App",
                    subtitle = "Developer info, update status & open source details",
                    icon = Icons.Rounded.Info,
                    onClick = { navController.navigate("settings/about") }
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    lumia.tracker.ui.components.UniversalCapsuleHeader(
        title = "Settings Hub",
        onBackClick = { navController.popBackStack() }
    )
}
}
