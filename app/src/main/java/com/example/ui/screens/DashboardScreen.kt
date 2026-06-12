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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
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
                            icon = { Icon(Icons.Rounded.Analytics, contentDescription = "Analytics") },
                            label = { Text("Analytics") },
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
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
            androidx.compose.animation.AnimatedContent(
                targetState = selectedTab,
                label = "TabTransition"
            ) { targetTab ->
                if (targetTab == 0) {
                    HomeTab(
                        navController = navController, 
                        viewModel = viewModel, 
                        bottomPadding = extendedPadding,
                        onAddCourseClick = { showAddCourseDialog = true },
                        onAddSubjectClick = { showAddSubjectDialog = true }
                    )
                } else {
                    AnalyticsTab(viewModel = viewModel, paddingValues = extendedPadding)
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
                        icon = { Icon(Icons.Rounded.Analytics, contentDescription = "Analytics") },
                        label = { Text("Analytics") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
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

        AlertDialog(
            onDismissRequest = { showAddCourseDialog = false },
            title = { Text("Add New Course") },
            text = {
                Column {
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
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addCourse(name, instructor, schedule, description)
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
        var targetHours by remember { mutableStateOf("") }

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
                        value = targetHours,
                        onValueChange = { targetHours = it.filter { char -> char.isDigit() } },
                        label = { Text("Target Hours") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number, imeAction = androidx.compose.ui.text.input.ImeAction.Done)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        val hours = targetHours.toIntOrNull() ?: 0
                        viewModel.addSubject(name, hours)
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
    onAddSubjectClick: () -> Unit
) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val streak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val betaMotivation by viewModel.betaMotivation.collectAsStateWithLifecycle()
    val betaPomodoro by viewModel.betaPomodoro.collectAsStateWithLifecycle()
    val betaCgpa by viewModel.betaCgpa.collectAsStateWithLifecycle()
    val betaNotes by viewModel.betaNotes.collectAsStateWithLifecycle()
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

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
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
                if (betaEnhancedHeader) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .liquidGlass(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                                tintAlpha = if (isDark) 0.15f else 0.4f,
                                blurRadius = 60f,
                                isDark = isDark,
                                tintColor = MaterialTheme.colorScheme.surface
                            )
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(id = R.string.app_name), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    actions = {
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (betaEnhancedHeader) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha=0.5f),
                        scrolledContainerColor = if (betaEnhancedHeader) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp).copy(alpha=0.5f)
                    )
                )
            }
        },
        floatingActionButton = {
            var fabExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.padding(bottom = bottomPadding.calculateBottomPadding())) {
                DropdownMenu(expanded = fabExpanded, onDismissRequest = { fabExpanded = false }) {
                    DropdownMenuItem(text = { Text("Add Course") }, onClick = { fabExpanded=false; onAddCourseClick() })
                    DropdownMenuItem(text = { Text("Add Subject") }, onClick = { fabExpanded=false; onAddSubjectClick() })
                }
                androidx.compose.material3.FloatingActionButton(
                    onClick = { fabExpanded = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    val rotation by androidx.compose.animation.core.animateFloatAsState(targetValue = if (fabExpanded) 45f else 0f)
                    Icon(Icons.Rounded.Add, contentDescription = "Add", modifier = Modifier.graphicsLayer { rotationZ = rotation })
                }
            }
        }
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

            if (betaMotivation || betaPomodoro || betaCgpa || betaNotes) {
                item(key = "student_tools_title") {
                    Text(
                        "Student Tools",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.animateItem().padding(vertical = 8.dp)
                    )
                }

                if (betaMotivation) {
                    item(key = "beta_motivation") {
                        GlassCard(
                            modifier = Modifier.animateItem().fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = "Motivation",
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.1f), CircleShape).padding(6.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                val quotes = listOf(
                                    "Success is the sum of small efforts.",
                                    "The expert in everything was once a beginner.",
                                    "Don’t stop until you’re proud.",
                                    "Focus on your goals, not your obstacles."
                                )
                                val quote = remember { quotes.random() }
                                Text(
                                    text = "\"$quote\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }

                if (betaPomodoro || betaCgpa || betaNotes) {
                    item(key = "toolbox_card") {
                        GlassCard(
                            onClick = { navController.navigate("toolbox") },
                            modifier = Modifier.animateItem().fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    val activeTools = listOfNotNull(
                                        "Pomodoro".takeIf { betaPomodoro },
                                        "CGPA".takeIf { betaCgpa },
                                        "Notes".takeIf { betaNotes }
                                    ).joinToString(" • ")
                                    
                                    Text("Toolbox", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text(activeTools, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ManageSearch,
                                    contentDescription = "Toolbox",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f), CircleShape).padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            item(key = "your_courses_title") {
                Text(
                        "Your Courses",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.animateItem().padding(vertical = 8.dp)
                    )
                }

                if (courses.isEmpty()) {
                    item(key = "courses_empty") {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = true,
                            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically(),
                            modifier = Modifier.animateItem()
                        ) {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth().height(240.dp),
                                shape = RoundedCornerShape(32.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(
                                        "No courses yet",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(courses, key = { it.id }) { course ->
                        var expanded by remember { mutableStateOf(false) }
                        GlassCard(
                            onClick = { navController.navigate("courseDetail/${course.id}") },
                            modifier = Modifier
                                .animateItem()
                                .fillMaxWidth()
                                .animateContentSize(),
                            shape = MaterialTheme.shapes.extraLarge,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.large),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    
                                    Box {
                                        IconButton(onClick = { expanded = true }) {
                                            Icon(Icons.Rounded.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Edit") },
                                                onClick = {
                                                    expanded = false
                                                    courseToEdit = course
                                                },
                                                leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Delete") },
                                                onClick = {
                                                    expanded = false
                                                    viewModel.deleteCourse(course)
                                                },
                                                leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                                            )
                                        }
                                    }
                                }

                                Column {
                                    Text(
                                        text = course.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (course.description.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = course.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item(key = "your_subjects_title") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Your Subjects",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.animateItem().padding(vertical = 8.dp)
                    )
                }

                if (subjects.isEmpty()) {
                    item(key = "subjects_empty") {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = true,
                            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically(),
                            modifier = Modifier.animateItem()
                        ) {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth().height(240.dp),
                                shape = RoundedCornerShape(32.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp),
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(
                                        "No subjects yet",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(subjects, key = { it.id }) { subject ->
                        var expanded by remember { mutableStateOf(false) }
                        GlassCard(
                            onClick = { navController.navigate("subjectDetail/${subject.id}") },
                            modifier = Modifier.animateItem().fillMaxWidth(),
                            shape = RoundedCornerShape(32.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier.size(64.dp).background(MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(20.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = subject.name.take(1).uppercase(),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                    Box {
                                        IconButton(onClick = { expanded = true }) {
                                            Icon(Icons.Rounded.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Edit") },
                                                onClick = {
                                                    expanded = false
                                                    subjectToEdit = subject
                                                },
                                                leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Delete") },
                                                onClick = {
                                                    expanded = false
                                                    viewModel.deleteSubject(subject)
                                                },
                                                leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = subject.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${subject.targetHours} hr target",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

    if (courseToEdit != null) {
        var name by remember { mutableStateOf(courseToEdit!!.name) }
        var instructor by remember { mutableStateOf(courseToEdit!!.instructor) }
        var schedule by remember { mutableStateOf(courseToEdit!!.schedule) }
        var description by remember { mutableStateOf(courseToEdit!!.description) }
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
            val timePickerState = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute)
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
                    TimePicker(state = timePickerState)
                }
            )
        }

        AlertDialog(
            onDismissRequest = { courseToEdit = null },
            title = { Text("Edit Course") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Course Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = instructor,
                        onValueChange = { instructor = it },
                        label = { Text("Instructor") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (schedule.isEmpty()) "Select Schedule Time" else "Schedule: $schedule")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.updateCourse(courseToEdit!!.copy(
                            name = name,
                            instructor = instructor,
                            schedule = schedule,
                            description = description
                        ))
                        courseToEdit = null
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { courseToEdit = null }) { Text("Cancel") }
            }
        )
    }

    if (subjectToEdit != null) {
        var name by remember { mutableStateOf(subjectToEdit!!.name) }
        var targetHours by remember { mutableStateOf(subjectToEdit!!.targetHours.toString()) }

        AlertDialog(
            onDismissRequest = { subjectToEdit = null },
            title = { Text("Edit Subject") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Subject Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = targetHours,
                        onValueChange = { targetHours = it.filter { char -> char.isDigit() } },
                        label = { Text("Target Hours") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        val hours = targetHours.toIntOrNull() ?: subjectToEdit!!.targetHours
                        viewModel.updateSubject(subjectToEdit!!.copy(
                            name = name,
                            targetHours = hours
                        ))
                        subjectToEdit = null
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { subjectToEdit = null }) { Text("Cancel") }
            }
        )
    }
}


