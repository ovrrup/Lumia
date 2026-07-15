package lumia.tracker.ui.screens

import lumia.tracker.service.AodAccessibilityService
import lumia.tracker.util.TrueAodManager
import android.content.Intent
import android.provider.Settings
import lumia.tracker.ui.theme.liquidGlass
import lumia.tracker.ui.theme.glassBar
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Close
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.MergeType
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ViewQuilt
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.InvertColors
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.viewmodel.ScholarViewModel
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemSettingsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val autoLinkByName by viewModel.systemAutoLinkByName.collectAsStateWithLifecycle()
    val enableSynergy by viewModel.systemEnableSynergy.collectAsStateWithLifecycle()
    val autoCreateSubject by viewModel.systemAutoCreateSubject.collectAsStateWithLifecycle()
    val fuseSubjectsCourses by viewModel.systemFuseSubjectsCourses.collectAsStateWithLifecycle()
    val advancedTasks by viewModel.systemAdvancedTasks.collectAsStateWithLifecycle()

    val featureSubjectEnabled by viewModel.featureSubjectEnabled.collectAsStateWithLifecycle()
    val featureSelfStudyEnabled by viewModel.featureSelfStudyEnabled.collectAsStateWithLifecycle()
    val featureAnalyticsEnabled by viewModel.featureAnalyticsEnabled.collectAsStateWithLifecycle()
    val featureCalendarEnabled by viewModel.featureCalendarEnabled.collectAsStateWithLifecycle()
    val featureQuickNotesEnabled by viewModel.featureQuickNotesEnabled.collectAsStateWithLifecycle()

    val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (betaEnhancedHeader || isGlass) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
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

            SettingsCategoryHeading(title = "Core Features Manager", icon = Icons.Rounded.ViewQuilt)

            SettingsGroupCard(title = "App Feature Modules", icon = Icons.Rounded.ViewQuilt) {
                SettingsToggleItem(
                    title = "Subjects Tab",
                    subtitle = "Allows tracking specific focus subjects. Hides the Subjects tab when disabled.",
                    checked = featureSubjectEnabled,
                    icon = Icons.Rounded.FolderOpen,
                    onCheckedChange = { viewModel.updateFeatureSubjectEnabled(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsToggleItem(
                    title = "Self Study & Tasks Tab",
                    subtitle = "Enable tasks and customizable study logs tab in the navigation bar.",
                    checked = featureSelfStudyEnabled,
                    icon = Icons.Rounded.AutoStories,
                    onCheckedChange = { viewModel.updateFeatureSelfStudyEnabled(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsToggleItem(
                    title = "Calendar & Schedule Tab",
                    subtitle = "Display scheduled lectures and courses on a weekly Calendar view.",
                    checked = featureCalendarEnabled,
                    icon = Icons.Rounded.DateRange,
                    onCheckedChange = { viewModel.updateFeatureCalendarEnabled(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsToggleItem(
                    title = "Quick Notes Utility",
                    subtitle = "Enable access to the Quick Notes workspace from home screen.",
                    checked = featureQuickNotesEnabled,
                    icon = Icons.Rounded.Edit,
                    onCheckedChange = { viewModel.updateFeatureQuickNotesEnabled(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsToggleItem(
                    title = "Analytics Dashboard Tab",
                    subtitle = "Access dynamic graphical focus stats, activity insights, and streak logs.",
                    checked = featureAnalyticsEnabled,
                    icon = Icons.Rounded.Analytics,
                    onCheckedChange = { viewModel.updateFeatureAnalyticsEnabled(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsCategoryHeading(title = "Interconnections", icon = Icons.Rounded.Settings)

            SettingsGroupCard(title = "Course & Subject Integration", icon = Icons.Rounded.Settings) {
                SettingsToggleItem(
                    title = "Auto-Link by Name",
                    subtitle = "Automatically couple Courses and study Subjects together if they share the same name (case-insensitive) when no explicit association is set.",
                    checked = autoLinkByName,
                    icon = Icons.Rounded.Settings,
                    onCheckedChange = { viewModel.updateSystemAutoLinkByName(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsToggleItem(
                    title = "Course Synergy Score",
                    subtitle = "Measure alignments between lectures and study topics using a Dynamic Synergy Gauge in details screens.",
                    checked = enableSynergy,
                    icon = Icons.Rounded.Star,
                    onCheckedChange = { viewModel.updateSystemEnableSynergy(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsToggleItem(
                    title = "Auto-Create Associated Subject",
                    subtitle = "Automatically create a matching Study Subject whenever you enroll in/add a new academic Course.",
                    checked = autoCreateSubject,
                    icon = Icons.Rounded.School,
                    onCheckedChange = { viewModel.updateSystemAutoCreateSubject(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsToggleItem(
                    title = "Fuse Subjects & Courses",
                    subtitle = "Embed subjects within courses to simplify navigation. Turn off to display 'Subjects' as a separate bottom tab.",
                    checked = fuseSubjectsCourses,
                    icon = Icons.Rounded.MergeType,
                    onCheckedChange = { viewModel.updateSystemFuseSubjectsCourses(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsToggleItem(
                    title = "Advanced Tasks & Linkages",
                    subtitle = "Enable complex task tracking, including multi-linking with courses and assignments, plus advanced sorting and cross-referencing.",
                    checked = advancedTasks,
                    icon = Icons.Rounded.List,
                    onCheckedChange = { viewModel.updateSystemAdvancedTasks(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            SettingsCategoryHeading(title = "Pomodoro Integration", icon = Icons.Rounded.Timer)

            SettingsGroupCard(title = "Timer & Environment Behaviors", icon = Icons.Rounded.Timer) {
                SettingsToggleItem(
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
                    androidx.compose.material3.Slider(
                        value = workDur.toFloat(),
                        onValueChange = { viewModel.updatePomodoroWorkDuration(it.toInt()) },
                        valueRange = 5f..120f,
                        steps = 114
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val shortDur by viewModel.pomodoroShortBreakDuration.collectAsStateWithLifecycle()
                    Text("Short Break: $shortDur mins", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    androidx.compose.material3.Slider(
                        value = shortDur.toFloat(),
                        onValueChange = { viewModel.updatePomodoroShortBreakDuration(it.toInt()) },
                        valueRange = 1f..30f,
                        steps = 28
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val longDur by viewModel.pomodoroLongBreakDuration.collectAsStateWithLifecycle()
                    Text("Long Break: $longDur mins", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    androidx.compose.material3.Slider(
                        value = longDur.toFloat(),
                        onValueChange = { viewModel.updatePomodoroLongBreakDuration(it.toInt()) },
                        valueRange = 5f..60f,
                        steps = 54
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsGroupCard(
                title = "Period Structure", 
                icon = Icons.Rounded.List,
                infoText = "Allows restricting Pomodoro loops to a fixed target. Session configurations control the number of study/work intervals before a long break is triggered."
            ) {
                SettingsToggleItem(
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
                    androidx.compose.material3.Slider(
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
