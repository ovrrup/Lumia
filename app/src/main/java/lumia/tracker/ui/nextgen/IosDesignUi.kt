package lumia.tracker.ui.nextgen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.viewmodel.ScholarViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Highly polished iOS-style Next-Gen UI Container.
 * Designed to mirror apple HIG with thin lines, rounded container forms, SF-style typography,
 * system grouped layouts, and translucent navigation blurs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IosNextGenLayout(
    navController: NavController,
    viewModel: ScholarViewModel
) {
    var selectedIosTab by remember { mutableStateOf(0) }
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    
    // iOS styling colors
    val iosBg = if (isDark) Color(0xFF000000) else Color(0xFFF2F2F7)
    val iosCardBg = if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
    val iosDivider = if (isDark) Color(0xFF38383A) else Color(0xFFE5E5EA)
    val iosPrimaryText = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)
    val iosSecondaryText = if (isDark) Color(0xFF8E8E93) else Color(0xFF3C3C43).copy(alpha = 0.6f)
    val iosAccentBlue = Color(0xFF007AFF)

    Scaffold(
        containerColor = iosBg,
        bottomBar = {
            IosTabBar(
                selectedTab = selectedIosTab,
                onTabSelected = { selectedIosTab = it },
                isDark = isDark,
                accentColor = iosAccentBlue
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedIosTab) {
                0 -> IosDashboardTab(
                    viewModel = viewModel,
                    navController = navController,
                    cardBg = iosCardBg,
                    dividerColor = iosDivider,
                    primaryText = iosPrimaryText,
                    secondaryText = iosSecondaryText,
                    accentColor = iosAccentBlue
                )
                1 -> IosStudyTab(
                    viewModel = viewModel,
                    navController = navController,
                    cardBg = iosCardBg,
                    dividerColor = iosDivider,
                    primaryText = iosPrimaryText,
                    secondaryText = iosSecondaryText,
                    accentColor = iosAccentBlue
                )
                2 -> IosAnalyticsTab(
                    viewModel = viewModel,
                    cardBg = iosCardBg,
                    primaryText = iosPrimaryText,
                    secondaryText = iosSecondaryText,
                    accentColor = iosAccentBlue
                )
                3 -> IosSettingsTab(
                    viewModel = viewModel,
                    cardBg = iosCardBg,
                    dividerColor = iosDivider,
                    primaryText = iosPrimaryText,
                    secondaryText = iosSecondaryText,
                    accentColor = iosAccentBlue
                )
            }
        }
    }
}

@Composable
fun IosTabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isDark: Boolean,
    accentColor: Color
) {
    val barBg = if (isDark) Color(0xDD161616) else Color(0xDDF9F9F9)
    val inactiveColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF999999)

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(if (isDark) Color(0xFF2C2C2E) else Color(0xFFD1D1D6))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(barBg)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(49.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IosTabItem(
                label = "Dashboard",
                icon = Icons.Rounded.SpaceDashboard,
                isSelected = selectedTab == 0,
                accentColor = accentColor,
                inactiveColor = inactiveColor,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(0) }
            )
            IosTabItem(
                label = "Study",
                icon = Icons.Rounded.School,
                isSelected = selectedTab == 1,
                accentColor = accentColor,
                inactiveColor = inactiveColor,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(1) }
            )
            IosTabItem(
                label = "Analytics",
                icon = Icons.Rounded.BarChart,
                isSelected = selectedTab == 2,
                accentColor = accentColor,
                inactiveColor = inactiveColor,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(2) }
            )
            IosTabItem(
                label = "Settings",
                icon = Icons.Rounded.Settings,
                isSelected = selectedTab == 3,
                accentColor = accentColor,
                inactiveColor = inactiveColor,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(3) }
            )
        }
    }
}

@Composable
fun RowScope.IosTabItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) accentColor else inactiveColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) accentColor else inactiveColor
        )
    }
}

@Composable
fun IosDashboardTab(
    viewModel: ScholarViewModel,
    navController: NavController,
    cardBg: Color,
    dividerColor: Color,
    primaryText: Color,
    secondaryText: Color,
    accentColor: Color
) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        // iOS Large Title Navigation style header
        Text(
            text = "Lumia Tracker",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Dashboard",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = primaryText,
            fontFamily = FontFamily.SansSerif
        )
        
        Spacer(modifier = Modifier.height(20.dp))

        // Profile Quick View Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(cardBg)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(accentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = null, tint = accentColor)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = activeProfile.name.ifBlank { "Lumia Scholar" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryText
                    )
                    Text(
                        text = "Alias: ${activeProfile.alias.ifBlank { "None" }}",
                        fontSize = 13.sp,
                        color = secondaryText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Courses section
        Text(
            text = "COURSES",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = secondaryText,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        if (courses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No Courses Active", color = secondaryText, fontSize = 15.sp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
            ) {
                courses.take(4).forEachIndexed { idx, course ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("courseDetail/${course.id}") }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val cCol = try { Color(android.graphics.Color.parseColor(course.colorHex)) } catch(e:Exception){ accentColor }
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(cCol)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(course.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = primaryText)
                                if (course.instructor.isNotBlank()) {
                                    Text(course.instructor, fontSize = 12.sp, color = secondaryText)
                                }
                            }
                        }
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = secondaryText, modifier = Modifier.size(18.dp))
                    }
                    if (idx < courses.size - 1 && idx < 3) {
                        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(dividerColor).padding(horizontal = 16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Item Tasks Section
        Text(
            text = "UPCOMING TASKS",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = secondaryText,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        val pendingTasks = remember(tasks) { tasks.filter { !it.isCompleted } }
        if (pendingTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("All Tasks Completed", color = secondaryText, fontSize = 15.sp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
            ) {
                pendingTasks.take(3).forEachIndexed { idx, task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(task.title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = primaryText)
                            val dueStr = task.dueDateMillis?.let {
                                try {
                                    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                                    sdf.format(Date(it))
                                } catch (e: Exception) {
                                    "Soon"
                                }
                            } ?: "No due date"
                            Text("Due $dueStr", fontSize = 12.sp, color = secondaryText)
                        }
                    }
                    if (idx < pendingTasks.size - 1 && idx < 2) {
                        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(dividerColor).padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun IosStudyTab(
    viewModel: ScholarViewModel,
    navController: NavController,
    cardBg: Color,
    dividerColor: Color,
    primaryText: Color,
    secondaryText: Color,
    accentColor: Color
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Text(
            text = "ACADEMICS",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Subjects",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = primaryText
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (subjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No Subjects added yet", color = secondaryText, fontSize = 15.sp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
            ) {
                subjects.forEachIndexed { index, subject ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("subjectDetail/${subject.id}") }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(accentColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = subject.name.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(subject.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = primaryText)
                                if (subject.tags.isNotBlank()) {
                                    Text("Tags: ${subject.tags}", fontSize = 12.sp, color = secondaryText)
                                }
                            }
                        }
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = secondaryText, modifier = Modifier.size(18.dp))
                    }
                    if (index < subjects.size - 1) {
                        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(dividerColor).padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun IosAnalyticsTab(
    viewModel: ScholarViewModel,
    cardBg: Color,
    primaryText: Color,
    secondaryText: Color,
    accentColor: Color
) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Text(
            text = "METRICS",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Analytics",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = primaryText
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Progress Overview Block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(cardBg)
                .padding(16.dp)
        ) {
            Column {
                Text("Performance Ratio", fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = primaryText)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Active Courses", fontSize = 12.sp, color = secondaryText)
                        Text("${courses.size}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = primaryText)
                    }
                    Column {
                        Text("Task Completion", fontSize = 12.sp, color = secondaryText)
                        val completed = tasks.count { it.isCompleted }
                        val total = tasks.size
                        val pct = if (total > 0) (completed * 100) / total else 100
                        Text("$pct%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = accentColor)
                    }
                }
            }
        }
    }
}

@Composable
fun IosSettingsTab(
    viewModel: ScholarViewModel,
    cardBg: Color,
    dividerColor: Color,
    primaryText: Color,
    secondaryText: Color,
    accentColor: Color
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val pureBlack by viewModel.pureBlackMode.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Text(
            text = "PREFERENCES",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Settings",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = primaryText
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Apple style system settings section
        Text(
            text = "APPEARANCE",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = secondaryText,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(cardBg)
        ) {
            // Theme Mode setting row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFF9500)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Brightness4, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Theme Mode", fontSize = 16.sp, color = primaryText)
                }
                Text(themeMode, color = secondaryText, fontSize = 15.sp)
            }

            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(dividerColor).padding(horizontal = 16.dp))

            // Pure Black Mode row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.DarkMode, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Pure Black Mode", fontSize = 16.sp, color = primaryText)
                }
                Switch(
                    checked = pureBlack,
                    onCheckedChange = { viewModel.updatePureBlackMode(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor)
                )
            }

            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(dividerColor).padding(horizontal = 16.dp))

            // Next-Gen UI Layout toggle row
            val nextGenUiEnabled by viewModel.nextGenUiEnabled.collectAsStateWithLifecycle()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF34C759)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.PhoneIphone, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Next-Gen iOS Layout", fontSize = 16.sp, color = primaryText)
                }
                Switch(
                    checked = nextGenUiEnabled,
                    onCheckedChange = { viewModel.updateNextGenUiEnabled(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor)
                )
            }
        }
    }
}
