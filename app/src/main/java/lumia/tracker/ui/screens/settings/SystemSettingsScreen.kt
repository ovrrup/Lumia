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
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.screens.settings.components.SettingsGroupCard
import lumia.tracker.ui.screens.settings.components.SettingsToggleItem
import lumia.tracker.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemSettingsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val autoLinkByName by viewModel.systemAutoLinkByName.collectAsStateWithLifecycle()
    val enableSynergy by viewModel.systemEnableSynergy.collectAsStateWithLifecycle()
    val autoCreateSubject by viewModel.systemAutoCreateSubject.collectAsStateWithLifecycle()
    val fuseSubjectsCourses by viewModel.systemFuseSubjectsCourses.collectAsStateWithLifecycle()
    val advancedTasks by viewModel.systemAdvancedTasks.collectAsStateWithLifecycle()
    val pomodoroAutoLog by viewModel.systemPomodoroAutoLog.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "System Configuration",
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
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Course & Subject Integration
            SettingsGroupCard(title = "Course & Subject Integration", icon = Icons.Rounded.Hub) {
                SettingsToggleItem(
                    title = "Auto-Link by Name",
                    subtitle = "Automatically couple Courses and study Subjects together if they share the same name (case-insensitive)",
                    checked = autoLinkByName,
                    icon = Icons.Rounded.Link,
                    onCheckedChange = { viewModel.updateSystemAutoLinkByName(it) }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                SettingsToggleItem(
                    title = "Course Synergy Score",
                    subtitle = "Measure alignments between lectures and study topics using a Dynamic Synergy Gauge",
                    checked = enableSynergy,
                    icon = Icons.Rounded.Star,
                    onCheckedChange = { viewModel.updateSystemEnableSynergy(it) }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                SettingsToggleItem(
                    title = "Auto-Create Associated Subject",
                    subtitle = "Automatically create a matching Study Subject whenever you enroll in/add a new academic Course",
                    checked = autoCreateSubject,
                    icon = Icons.Rounded.School,
                    onCheckedChange = { viewModel.updateSystemAutoCreateSubject(it) }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                SettingsToggleItem(
                    title = "Fuse Subjects & Courses",
                    subtitle = "Embed subjects within courses to simplify navigation. Turn off to display 'Subjects' as a separate bottom tab",
                    checked = fuseSubjectsCourses,
                    icon = Icons.Rounded.MergeType,
                    onCheckedChange = { viewModel.updateSystemFuseSubjectsCourses(it) }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                SettingsToggleItem(
                    title = "Advanced Tasks & Linkages",
                    subtitle = "Enable complex task tracking, including multi-linking with courses and assignments, plus advanced sorting",
                    checked = advancedTasks,
                    icon = Icons.Rounded.ListAlt,
                    onCheckedChange = { viewModel.updateSystemAdvancedTasks(it) }
                )
            }

            // 2. Pomodoro & Session Logging
            SettingsGroupCard(title = "Timer & Productivity Log", icon = Icons.Rounded.Timer) {
                SettingsToggleItem(
                    title = "Auto-Log Focus Sessions",
                    subtitle = "Automatically register and log Pomodoro 'Work' sessions into the database productivity history log upon completion",
                    checked = pomodoroAutoLog,
                    icon = Icons.Rounded.History,
                    onCheckedChange = { viewModel.updateSystemPomodoroAutoLog(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
