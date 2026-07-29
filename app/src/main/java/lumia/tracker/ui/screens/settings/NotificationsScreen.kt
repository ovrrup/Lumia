package lumia.tracker.ui.screens

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.RecordVoiceOver
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
fun NotificationsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val notifFormalTone by viewModel.notifFormalTone.collectAsStateWithLifecycle()
    val notifEnableDeadlines by viewModel.notifEnableDeadlines.collectAsStateWithLifecycle()
    val notifEnableClasses by viewModel.notifEnableClasses.collectAsStateWithLifecycle()
    val notifEnableDailyDigest by viewModel.notifEnableDailyDigest.collectAsStateWithLifecycle()

    val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        ) { padding ->
            val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = statusBarHeight + 64.dp, bottom = 24.dp)
            ) {
            Spacer(modifier = Modifier.height(16.dp))
            lumia.tracker.ui.components.NotificationPermissionPanel()
            lumia.tracker.ui.components.ExactAlarmPermissionPanel()
            lumia.tracker.ui.components.BatteryOptimizationPermissionPanel()
            
            SettingsGroupCard(title = "Notification Configuration", icon = Icons.Rounded.Notifications) {
                SettingsToggleItem(
                    title = "Formal Notification Tone",
                    subtitle = if (notifFormalTone) "Notifications will sound polite and professional" else "Notifications will sound taunting and strict to push you harder!",
                    checked = notifFormalTone,
                    icon = Icons.Rounded.RecordVoiceOver,
                    onCheckedChange = { 
                        viewModel.updateNotifFormalTone(it) 
                    }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsToggleItem(
                    title = "Deadline Alerts",
                    subtitle = "Get notified before assignment and task deadlines",
                    checked = notifEnableDeadlines,
                    onCheckedChange = { viewModel.updateNotifEnableDeadlines(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsToggleItem(
                    title = "Daily Digest",
                    subtitle = "A combined summary of tasks & assignments for the day",
                    checked = notifEnableDailyDigest,
                    onCheckedChange = { viewModel.updateNotifEnableDailyDigest(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsToggleItem(
                    title = "Class & Attendance Alerts",
                    subtitle = "Get notified when classes start and end to log attendance",
                    checked = notifEnableClasses,
                    onCheckedChange = { viewModel.updateNotifEnableClasses(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    lumia.tracker.ui.components.UniversalCapsuleHeader(
        title = "Notifications",
        onBackClick = { navController.popBackStack() }
    )
}
}
