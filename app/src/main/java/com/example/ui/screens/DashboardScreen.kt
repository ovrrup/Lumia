package com.example.ui.screens

import com.example.ui.theme.liquidGlass
import com.example.ui.theme.glassBar
import com.example.ui.theme.glassPill
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.R
import com.example.viewmodel.ScholarViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassHeroCard
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: ScholarViewModel) {
    val isGlass = com.example.ui.theme.LocalGlassMode.current
    var selectedTab by remember { mutableStateOf(0) }
    val betaFloatingNav by viewModel.betaFloatingNav.collectAsStateWithLifecycle()
    val fuseSubjectsCourses by viewModel.systemFuseSubjectsCourses.collectAsStateWithLifecycle()
    
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            bottomBar = {
                if (!betaFloatingNav) {
                    val navItemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                        indicatorColor = if (isGlass) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    )
                    NavigationBar(
                        modifier = if (isGlass) Modifier.glassBar() else Modifier,
                        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
                    ) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            colors = navItemColors,
                            alwaysShowLabel = true
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = "Courses") },
                            label = { Text("Courses") },
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            colors = navItemColors,
                            alwaysShowLabel = true
                        )
                        if (!fuseSubjectsCourses) {
                            NavigationBarItem(
                                icon = { Icon(Icons.Rounded.FolderOpen, contentDescription = "Subjects") },
                                label = { Text("Subjects") },
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                colors = navItemColors,
                                alwaysShowLabel = true
                            )
                        }
                        NavigationBarItem(
                            icon = { Icon(Icons.Rounded.AutoStories, contentDescription = "Self Study") },
                            label = { Text("Self Study") },
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            colors = navItemColors,
                            alwaysShowLabel = true
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Rounded.Analytics, contentDescription = "Analytics") },
                            label = { Text("Analytics") },
                            selected = selectedTab == 4,
                            onClick = { selectedTab = 4 },
                            colors = navItemColors,
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        ) { padding ->
            val extendedPadding = if (betaFloatingNav) {
                PaddingValues(
                    start = padding.calculateStartPadding(androidx.compose.ui.platform.LocalLayoutDirection.current),
                    top = padding.calculateTopPadding(),
                    end = padding.calculateEndPadding(androidx.compose.ui.platform.LocalLayoutDirection.current),
                    bottom = padding.calculateBottomPadding() + 112.dp
                )
            } else {
                padding
            }
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
            ) {
                androidx.compose.animation.AnimatedContent(
                    targetState = selectedTab,
                    label = "TabTransition",
                    modifier = Modifier.fillMaxSize()
                ) { targetTab ->
                    when (targetTab) {
                        0 -> HomeTab(
                            navController = navController, 
                            viewModel = viewModel, 
                            bottomPadding = extendedPadding,
                            onAddCourseClick = { showAddCourseDialog = true },
                            onAddSubjectClick = { showAddSubjectDialog = true },
                            onNavigateToTasks = { selectedTab = 3 }
                        )
                        1 -> CoursesTab(
                            navController = navController,
                            viewModel = viewModel,
                            bottomPadding = extendedPadding,
                            onEditCourse = { /* Handled in tab if hoisted, else ignore */ },
                            onAddCourseClick = { showAddCourseDialog = true }
                        )
                        2 -> SubjectsTab(
                            navController = navController,
                            viewModel = viewModel,
                            bottomPadding = extendedPadding,
                            onEditSubject = { /* Handled in tab */ },
                            onAddSubjectClick = { showAddSubjectDialog = true }
                        )
                        3 -> SelfStudyTab(
                            navController = navController,
                            viewModel = viewModel,
                            bottomPadding = extendedPadding
                        )
                        4 -> AnalyticsTab(viewModel = viewModel, paddingValues = extendedPadding)
                    }
                }
            }
        }

        if (betaFloatingNav) {
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomCenter)
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .then(if (isGlass) Modifier.glassPill(androidx.compose.foundation.shape.RoundedCornerShape(32.dp)) else Modifier),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
                color = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                contentColor = if (isGlass) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer,
                shadowElevation = if (isGlass) 0.dp else 8.dp,
                tonalElevation = if (isGlass) 0.dp else 4.dp
            ) {
                val navItemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (isGlass) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = if (isGlass) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedIconColor = if (isGlass) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.70f),
                    unselectedTextColor = if (isGlass) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                    indicatorColor = if (isGlass) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                )
                NavigationBar(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0, 0, 0, 0)
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        colors = navItemColors,
                        alwaysShowLabel = true
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = "Courses") },
                        label = { Text("Courses") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        colors = navItemColors,
                        alwaysShowLabel = true
                    )
                    if (!fuseSubjectsCourses) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Rounded.FolderOpen, contentDescription = "Subjects") },
                            label = { Text("Subjects") },
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            colors = navItemColors,
                            alwaysShowLabel = true
                        )
                    }
                    NavigationBarItem(
                        icon = { Icon(Icons.Rounded.AutoStories, contentDescription = "Self Study") },
                        label = { Text("Self Study") },
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        colors = navItemColors,
                        alwaysShowLabel = true
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Rounded.Analytics, contentDescription = "Analytics") },
                        label = { Text("Analytics") },
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        colors = navItemColors,
                        alwaysShowLabel = true
                    )
                }
            }
        }
    }

    if (showAddCourseDialog) {
        var name by remember { mutableStateOf("") }
        var instructor by remember { mutableStateOf("") }
        var schedule by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var tags by remember { mutableStateOf("") }
        var selectedSubjectId by remember { mutableStateOf<Int?>(null) }

        AlertDialog(
            onDismissRequest = { showAddCourseDialog = false },
            title = { Text("Add New Course") },
            text = {
                Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                    androidx.compose.material3.OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Course Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = instructor,
                        onValueChange = { instructor = it },
                        label = { Text("Instructor") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (Optional, comma separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    var showTimePicker by remember { mutableStateOf(false) }
                    
                    if (showTimePicker) {
                        val initialHour = remember {
                            try {
                                var parsedHour = schedule.substringBefore(":").toInt()
                                if (schedule.contains("PM", ignoreCase = true) && parsedHour < 12) parsedHour += 12
                                if (schedule.contains("AM", ignoreCase = true) && parsedHour == 12) parsedHour = 0
                                parsedHour
                            } catch (e: Exception) { 12 }
                        }
                        val initialMinute = remember {
                            try {
                                schedule.substringAfter(":").substringBefore(" ").toInt()
                            } catch (e: Exception) { 0 }
                        }
                        val timePickerState = androidx.compose.material3.rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute)
                        AlertDialog(
                            onDismissRequest = { showTimePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    val hour = timePickerState.hour
                                    val minute = timePickerState.minute
                                    val amPm = if (hour >= 12) "PM" else "AM"
                                    val formatHour = if (hour % 12 == 0) 12 else hour % 12
                                    schedule = String.format(java.util.Locale.getDefault(), "%02d:%02d %s", formatHour, minute, amPm)
                                    showTimePicker = false
                                }) { Text("OK") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                            },
                            text = {
                                androidx.compose.material3.TimePicker(state = timePickerState)
                            }
                        )
                    }
                    
                    androidx.compose.material3.OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (schedule.isEmpty()) "Select Schedule Time" else "Schedule: $schedule")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
                    if (subjects.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Link to Study Subject (Optional)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                androidx.compose.material3.InputChip(
                                    selected = selectedSubjectId == null,
                                    onClick = { selectedSubjectId = null },
                                    label = { Text("None") }
                                )
                            }
                            items(subjects, key = { "add_chips_${it.id}" }) { subj ->
                                androidx.compose.material3.InputChip(
                                    selected = selectedSubjectId == subj.id,
                                    onClick = { selectedSubjectId = subj.id },
                                    label = { Text(subj.name) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addCourse(name, instructor, schedule, description, selectedSubjectId, tags)
                        showAddCourseDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddCourseDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddSubjectDialog) {
        var name by remember { mutableStateOf("") }
        var tags by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = { Text("Add New Subject") },
            text = {
                Column {
                    androidx.compose.material3.OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Subject Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (comma separated, optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addSubject(name, tags)
                        showAddSubjectDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) { Text("Cancel") }
            }
        )
    }
}

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
    val isGlass = com.example.ui.theme.LocalGlassMode.current
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val streak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val betaNotes by viewModel.betaNotes.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var courseToEdit by remember { mutableStateOf<com.example.model.Course?>(null) }
    var subjectToEdit by remember { mutableStateOf<com.example.model.Subject?>(null) }

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
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (betaEnhancedHeader || isGlass) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                    )
                    // Sleek divider line for clean separation and anchoring
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(id = R.string.app_name), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 16.dp)) },
                    actions = {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(44.dp)
                                .shadow(elevation = 8.dp, shape = CircleShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                .clip(CircleShape)
                                .clickable(
                                    onClick = { navController.navigate("settings") },
                                    role = androidx.compose.ui.semantics.Role.Button
                                ),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha=0.5f),
                        scrolledContainerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp).copy(alpha=0.5f)
                    )
                )
            }
        },
        floatingActionButton = {}
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 16.dp,
                bottom = bottomPadding.calculateBottomPadding() + 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                com.example.ui.components.NotificationPermissionPanel()
                com.example.ui.components.ExactAlarmPermissionPanel()
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Streak Card
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
                                imageVector = Icons.Rounded.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f), CircleShape)
                                    .padding(8.dp)
                            )
                            Column {
                                Text(
                                    "$streak",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Day Streak",
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

            if (betaNotes) {
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
                            IconButton(
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
                            IconButton(
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
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.TaskAlt, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
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


