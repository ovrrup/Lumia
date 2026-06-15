package ovrrup.lumia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.MergeType
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ovrrup.lumia.ui.theme.glassBar
import ovrrup.lumia.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemSettingsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val autoLinkByName by viewModel.systemAutoLinkByName.collectAsStateWithLifecycle()
    val enableSynergy by viewModel.systemEnableSynergy.collectAsStateWithLifecycle()
    val autoCreateSubject by viewModel.systemAutoCreateSubject.collectAsStateWithLifecycle()
    val fuseSubjectsCourses by viewModel.systemFuseSubjectsCourses.collectAsStateWithLifecycle()
    val advancedTasks by viewModel.systemAdvancedTasks.collectAsStateWithLifecycle()

    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            Box {
                if (betaEnhancedHeader || isGlass) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = RoundedCornerShape(0.dp))
                    )
                    HorizontalDivider(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text("System Configuration", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SettingsCategoryHeading(title = "Interconnections", icon = Icons.Rounded.Settings)

            SettingsGroupCard(title = "Course & Subject Integration", icon = Icons.Rounded.Settings) {
                SettingsPremiumToggleItem(
                    title = "Auto-Link by Name",
                    subtitle = "Automatically couple Courses and study Subjects together if they share the same name (case-insensitive) when no explicit association is set.",
                    checked = autoLinkByName,
                    icon = Icons.Rounded.Settings,
                    onCheckedChange = { viewModel.updateSystemAutoLinkByName(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsPremiumToggleItem(
                    title = "Course Synergy Score",
                    subtitle = "Measure alignments between lectures and study topics using a Dynamic Synergy Gauge in details screens.",
                    checked = enableSynergy,
                    icon = Icons.Rounded.Star,
                    onCheckedChange = { viewModel.updateSystemEnableSynergy(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsPremiumToggleItem(
                    title = "Auto-Create Associated Subject",
                    subtitle = "Automatically create a matching Study Subject whenever you enroll in/add a new academic Course.",
                    checked = autoCreateSubject,
                    icon = Icons.Rounded.School,
                    onCheckedChange = { viewModel.updateSystemAutoCreateSubject(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsPremiumToggleItem(
                    title = "Fuse Subjects & Courses",
                    subtitle = "Embed subjects within courses to simplify navigation. Turn off to display 'Subjects' as a separate bottom tab.",
                    checked = fuseSubjectsCourses,
                    icon = Icons.AutoMirrored.Rounded.MergeType,
                    onCheckedChange = { viewModel.updateSystemFuseSubjectsCourses(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsPremiumToggleItem(
                    title = "Advanced Tasks & Linkages",
                    subtitle = "Enable complex task tracking, including multi-linking with courses and assignments, plus advanced sorting and cross-referencing.",
                    checked = advancedTasks,
                    icon = Icons.AutoMirrored.Rounded.List,
                    onCheckedChange = { viewModel.updateSystemAdvancedTasks(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            SettingsCategoryHeading(title = "Pomodoro Integration", icon = Icons.Rounded.Timer)

            SettingsGroupCard(title = "Timer & Environment Behaviors", icon = Icons.Rounded.Timer) {
                SettingsPremiumToggleItem(
                    title = "Auto-Log Focus Sessions",
                    subtitle = "Automatically register and log Pomodoro 'Work' sessions into the database productivity history log upon completion.",
                    checked = viewModel.systemPomodoroAutoLog.collectAsStateWithLifecycle().value,
                    icon = Icons.Rounded.History,
                    onCheckedChange = { viewModel.updateSystemPomodoroAutoLog(it) }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                
                Column(modifier = Modifier.padding(16.dp)) {
                    val workDur by viewModel.pomodoroWorkDuration.collectAsStateWithLifecycle()
                    Text("Work Duration: $workDur mins", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = workDur.toFloat(),
                        onValueChange = { viewModel.updatePomodoroWorkDuration(it.toInt()) },
                        valueRange = 5f..120f,
                        steps = 114
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val shortDur by viewModel.pomodoroShortBreakDuration.collectAsStateWithLifecycle()
                    Text("Short Break: $shortDur mins", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = shortDur.toFloat(),
                        onValueChange = { viewModel.updatePomodoroShortBreakDuration(it.toInt()) },
                        valueRange = 1f..30f,
                        steps = 28
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val longDur by viewModel.pomodoroLongBreakDuration.collectAsStateWithLifecycle()
                    Text("Long Break: $longDur mins", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = longDur.toFloat(),
                        onValueChange = { viewModel.updatePomodoroLongBreakDuration(it.toInt()) },
                        valueRange = 5f..60f,
                        steps = 54
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsGroupCard(title = "Period Structure", icon = Icons.AutoMirrored.Rounded.List) {
                SettingsPremiumToggleItem(
                    title = "Enable Target Periods",
                    subtitle = "Allows restricting pomodoro loops to a fixed target instead of infinite repetetion.",
                    checked = viewModel.pomodoroEnablePeriodTarget.collectAsStateWithLifecycle().value,
                    icon = Icons.Rounded.Star,
                    onCheckedChange = { viewModel.updatePomodoroEnablePeriodTarget(it) }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                
                Column(modifier = Modifier.padding(16.dp)) {
                    val periodSessions by viewModel.pomodoroPeriodSessions.collectAsStateWithLifecycle()
                    Text("Sessions per Period: $periodSessions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("Number of study/work sessions before a long break.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(
                        value = periodSessions.toFloat(),
                        onValueChange = { viewModel.updatePomodoroPeriodSessions(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
