package lumia.tracker.ui.screens

import lumia.tracker.ui.theme.liquidGlass
import lumia.tracker.ui.theme.glassBar
import lumia.tracker.ui.theme.navGlassBar
import lumia.tracker.ui.theme.glassPill
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.togetherWith
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
                            import lumia.tracker.ui.theme.bouncyClick
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// ... the rest of the imports ...
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.R
import lumia.tracker.viewmodel.ScholarViewModel
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.BouncyOutlinedButton
import lumia.tracker.ui.components.BouncyFloatingActionButton
import lumia.tracker.ui.components.GlassCard
import lumia.tracker.ui.components.GlassHeroCard
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.border

import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTab(
    navController: NavController, 
    viewModel: ScholarViewModel, 
    bottomPadding: PaddingValues, 
    onAddCourseClick: () -> Unit, 
    onAddSubjectClick: () -> Unit,
    onNavigateToTasks: () -> Unit
) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val betaNotes by viewModel.betaNotes.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val featureQuickNotesEnabled by viewModel.featureQuickNotesEnabled.collectAsStateWithLifecycle()

    var courseToEdit by remember { mutableStateOf<lumia.tracker.model.Course?>(null) }
    var subjectToEdit by remember { mutableStateOf<lumia.tracker.model.Subject?>(null) }

    // Notifications Permission
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(context, "Reminders may not work properly without permission.", Toast.LENGTH_SHORT).show()
        }
    }

    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    var selectedDateOffset by remember { androidx.compose.runtime.mutableIntStateOf(0) }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        floatingActionButton = {}
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = bottomPadding.calculateTopPadding() + 16.dp,
                bottom = bottomPadding.calculateBottomPadding() + 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            item {
                lumia.tracker.ui.components.NotificationPermissionPanel()
                lumia.tracker.ui.components.ExactAlarmPermissionPanel()
                lumia.tracker.ui.components.BatteryOptimizationPermissionPanel()
            }
            item {
                // Stunning Visual Banner
                lumia.tracker.ui.components.WelcomeMeshBanner()
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Tasks Completed Card
                    GlassHeroCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(160.dp),
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TaskAlt,
                                contentDescription = "Completed Tasks",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f), CircleShape)
                                    .padding(8.dp)
                            )
                            Column {
                                Text(
                                    "${tasks.count { it.isCompleted }}",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Tasks Done",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // Courses Stats
                    GlassHeroCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(160.dp),
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                contentDescription = "Courses",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f), CircleShape)
                                    .padding(8.dp)
                            )
                            Column {
                                Text(
                                    "${courses.size}",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    "Enrolled",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }


            if (featureQuickNotesEnabled && betaNotes) {
                item(key = "student_tools_title") {
                    Text(
                        "Student Tools",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.animateItem().padding(vertical = 8.dp)
                    )
                }

                item(key = "notes_tool") {
                    GlassCard(
                        onClick = { navController.navigate("notes") },
                        modifier = Modifier.animateItem().fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Quick Notes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text("Draft scratchpad canvas", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Notes,
                                contentDescription = "Quick Notes",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f), CircleShape).padding(8.dp)
                            )
                        }
                    }
                }
            }
            
            item(key = "today_classes_calendar") {
                Spacer(modifier = Modifier.height(16.dp))
                val baseCalendar = java.util.Calendar.getInstance()
                baseCalendar.add(java.util.Calendar.DAY_OF_MONTH, selectedDateOffset)
                val currentDayOfWeekStr = java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault()).format(baseCalendar.time)
                val todayDayNum = baseCalendar.get(java.util.Calendar.DAY_OF_MONTH)
                val todayMonthStr = java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault()).format(baseCalendar.time)
                
                val scheduledCourses = courses.filter { course ->
                    course.scheduleDays.contains(currentDayOfWeekStr, ignoreCase = true)
                }

                GlassCard(
                    modifier = Modifier.animateItem().fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = if (selectedDateOffset == 0) "Today's Classes" else if (selectedDateOffset == 1) "Tomorrow's Classes" else if (selectedDateOffset == -1) "Yesterday's Classes" else "$currentDayOfWeekStr's Classes",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "$currentDayOfWeekStr, $todayMonthStr $todayDayNum",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    .clickable { selectedDateOffset = 0 }, // reset to today
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.CalendarMonth,
                                    contentDescription = "Today",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // We will build a sleek horizontal list of dates for the surrounding 5 days
                        val daysBefore = 2
                        val todayCalendar = java.util.Calendar.getInstance()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (i in -daysBefore..2) {
                                val calOffset = todayCalendar.clone() as java.util.Calendar
                                val targetOffset = i
                                calOffset.add(java.util.Calendar.DAY_OF_MONTH, targetOffset)
                                val isSelected = selectedDateOffset == targetOffset
                                val dayLetter = java.text.SimpleDateFormat("E", java.util.Locale.getDefault()).format(calOffset.time).take(1)
                                val dayNumber = calOffset.get(java.util.Calendar.DAY_OF_MONTH).toString()
                                
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .clickable { selectedDateOffset = targetOffset }
                                        .padding(vertical = 12.dp)
                                ) {
                                    Text(
                                        text = dayLetter,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = dayNumber,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        if (scheduledCourses.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No classes scheduled for ${if (selectedDateOffset == 0) "today" else "this day"}.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                scheduledCourses.forEach { course ->
                                    val courseColor = try {
                                        Color(android.graphics.Color.parseColor(course.colorHex.ifBlank { "#3197D6" }))
                                    } catch(e: Exception) {
                                        Color(0xFF3197D6)
                                    }
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(courseColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                            .border(1.dp, courseColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(courseColor.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                course.name.take(1).uppercase(),
                                                color = courseColor,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = course.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                            if (course.schedule.isNotBlank()) {
                                                Text(
                                                    text = course.schedule,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item(key = "upcoming_classes_assignments") {
                Spacer(modifier = Modifier.height(16.dp))
                GlassCard(
                    modifier = Modifier.animateItem().fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Upcoming Classes & Assignments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            BouncyIconButton(
                                onClick = onAddCourseClick,
                                modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        
                        val upcomingAssigns = assignments.filter { !it.isCompleted && it.dueDateMillis > System.currentTimeMillis() }.sortedBy { it.dueDateMillis }.take(3)
                        if (upcomingAssigns.isEmpty()) {
                            Text("No upcoming classes or assignments.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            upcomingAssigns.forEach { assignment ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(8.dp).background(
                                            androidx.compose.ui.graphics.Color(
                                                android.graphics.Color.parseColor(
                                                    assignment.categoryColor.ifEmpty { "#3197D6" }
                                                )
                                            ), CircleShape
                                        )
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = assignment.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(key = "upcoming_tasks") {
                Spacer(modifier = Modifier.height(16.dp))
                GlassCard(
                    modifier = Modifier.animateItem().fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Upcoming Tasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            BouncyIconButton(
                                onClick = onNavigateToTasks,
                                modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        
                        val upcomingTasks = tasks.filter { !it.isCompleted }.sortedBy { it.dueDateMillis ?: Long.MAX_VALUE }.take(3)
                        if (upcomingTasks.isEmpty()) {
                            Text("No upcoming tasks.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            upcomingTasks.forEach { task ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .bouncyClick { viewModel.toggleTaskCompleted(task) },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = task.isCompleted,
                                        onCheckedChange = { _ -> viewModel.toggleTaskCompleted(task) },
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


