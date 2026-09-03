package lumia.tracker.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notifFormalTone by viewModel.notifFormalTone.collectAsStateWithLifecycle()
    val notifEnableDeadlines by viewModel.notifEnableDeadlines.collectAsStateWithLifecycle()
    val notifEnableClasses by viewModel.notifEnableClasses.collectAsStateWithLifecycle()
    val notifEnableDailyDigest by viewModel.notifEnableDailyDigest.collectAsStateWithLifecycle()

    val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            Box {
                if (betaEnhancedHeader || isGlass) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                    )
                    HorizontalDivider(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text("Notifications", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    ),
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            lumia.tracker.ui.components.NotificationPermissionPanel()
            lumia.tracker.ui.components.ExactAlarmPermissionPanel()
            lumia.tracker.ui.components.BatteryOptimizationPermissionPanel()
            
            SettingsGroupCard(title = "Notification Preferences", icon = Icons.Rounded.Notifications) {
                SettingsToggleItem(
                    title = "Formal Notification Tone",
                    subtitle = if (notifFormalTone) "Polite and professional wording for reminders" else "Casual and direct wording for reminders",
                    checked = notifFormalTone,
                    icon = Icons.Rounded.RecordVoiceOver,
                    onCheckedChange = { 
                        viewModel.updateNotifFormalTone(it) 
                    }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsToggleItem(
                    title = "Deadline Alerts",
                    subtitle = "Receive timely reminders before assignment and task deadlines",
                    checked = notifEnableDeadlines,
                    onCheckedChange = { viewModel.updateNotifEnableDeadlines(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsToggleItem(
                    title = "Daily Digest",
                    subtitle = "Morning overview of your classes, assignments, and tasks for today",
                    checked = notifEnableDailyDigest,
                    onCheckedChange = { viewModel.updateNotifEnableDailyDigest(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsToggleItem(
                    title = "Class & Attendance Alerts",
                    subtitle = "Reminders when classes begin and when they conclude to log attendance",
                    checked = notifEnableClasses,
                    onCheckedChange = { viewModel.updateNotifEnableClasses(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsGroupCard(title = "Notification Channels", icon = Icons.Rounded.List) {
                Text(
                    text = "Lumia routes notifications through Android system channels. You can customize individual channel sounds, vibration, and lock screen behavior in system settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                SettingsActionItemInCard(
                    title = "System Notification Settings",
                    subtitle = "Manage channel alerts, sound, vibration, and badges in Android settings",
                    icon = Icons.Rounded.Settings,
                    onClick = {
                        try {
                            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                            } else {
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Unable to open system notification settings", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
