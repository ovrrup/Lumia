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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.components.BatteryOptimizationPermissionPanel
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.ExactAlarmPermissionPanel
import lumia.tracker.ui.components.NotificationPermissionPanel
import lumia.tracker.ui.screens.settings.components.SettingsGroupCard
import lumia.tracker.ui.screens.settings.components.SettingsToggleItem
import lumia.tracker.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val notifFormalTone by viewModel.notifFormalTone.collectAsStateWithLifecycle()
    val notifEnableDeadlines by viewModel.notifEnableDeadlines.collectAsStateWithLifecycle()
    val notifEnableClasses by viewModel.notifEnableClasses.collectAsStateWithLifecycle()
    val notifEnableDailyDigest by viewModel.notifEnableDailyDigest.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Notifications & Reminders",
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    BouncyIconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Permission Request Panels
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationPermissionPanel()
                ExactAlarmPermissionPanel()
                BatteryOptimizationPermissionPanel()
            }

            // 1. Notification Persona & Voice Tone
            SettingsGroupCard(title = "Notification Tone & Persona", icon = Icons.Rounded.RecordVoiceOver) {
                SettingsToggleItem(
                    title = "Formal Notification Tone",
                    subtitle = if (notifFormalTone) {
                        "Polite, respectful, and professional tone for reminders"
                    } else {
                        "Strict, challenging, and taunting tone to push you harder!"
                    },
                    checked = notifFormalTone,
                    icon = Icons.Rounded.RecordVoiceOver,
                    onCheckedChange = { viewModel.updateNotifFormalTone(it) }
                )
            }

            // 2. Alert Subscriptions
            SettingsGroupCard(title = "Alert Subscriptions", icon = Icons.Rounded.Notifications) {
                SettingsToggleItem(
                    title = "Deadline Alerts",
                    subtitle = "Get notified before upcoming assignment and task deadlines",
                    checked = notifEnableDeadlines,
                    icon = Icons.Rounded.Alarm,
                    onCheckedChange = { viewModel.updateNotifEnableDeadlines(it) }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                SettingsToggleItem(
                    title = "Daily Digest Summary",
                    subtitle = "A single unified morning summary of your schedule and tasks",
                    checked = notifEnableDailyDigest,
                    icon = Icons.Rounded.Summarize,
                    onCheckedChange = { viewModel.updateNotifEnableDailyDigest(it) }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                SettingsToggleItem(
                    title = "Class & Attendance Reminders",
                    subtitle = "Get notified when academic lectures start and end to log attendance",
                    checked = notifEnableClasses,
                    icon = Icons.Rounded.School,
                    onCheckedChange = { viewModel.updateNotifEnableClasses(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
