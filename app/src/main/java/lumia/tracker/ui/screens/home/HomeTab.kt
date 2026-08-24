package lumia.tracker.ui.screens.home

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.Course
import lumia.tracker.model.Subject
import lumia.tracker.ui.components.*
import lumia.tracker.ui.screens.home.components.DashboardStatusCard
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel
import java.text.SimpleDateFormat
import java.util.*

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
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val betaNotes by viewModel.betaNotes.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val streakCurrent by viewModel.streakCurrent.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedDateOffset by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = bottomPadding.calculateTopPadding() + 16.dp,
                bottom = bottomPadding.calculateBottomPadding() + 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item(key = "permissions") {
                NotificationPermissionPanel()
                ExactAlarmPermissionPanel()
                BatteryOptimizationPermissionPanel()
            }

            item(key = "dashboard_status") {
                DashboardStatusCard(
                    userName = activeProfile.name.ifBlank { "Scholar" },
                    streakDays = streakCurrent,
                    viewModel = viewModel,
                    onStartFocus = { navController.navigate("pomodoro") }
                )
            }

            item(key = "stats_row") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tasks Completed Card
                    ScholarHeroCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp),
                        shape = RoundedCornerShape(28.dp),
                        onClick = onNavigateToTasks
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TaskAlt,
                                contentDescription = "Completed Tasks",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                                        CircleShape
                                    )
                                    .padding(8.dp)
                            )
                            Column {
                                Text(
                                    "${tasks.count { it.isCompleted }}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Tasks Done",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // Courses Stats Card
                    ScholarHeroCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp),
                        shape = RoundedCornerShape(28.dp),
                        onClick = { viewModel.setSelectedDashboardTab(1) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                contentDescription = "Courses",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                                        CircleShape
                                    )
                                    .padding(8.dp)
                            )
                            Column {
                                Text(
                                    "${courses.size}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Enrolled",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            if (betaNotes) {
                item(key = "notes_tool") {
                    ScholarCard(
                        onClick = { navController.navigate("notes") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Quick Notes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Draft thoughts & formulas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Notes,
                                contentDescription = "Quick Notes",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        CircleShape
                                    )
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }

            item(key = "today_classes_calendar") {
                val baseCalendar = Calendar.getInstance()
                baseCalendar.add(Calendar.DAY_OF_MONTH, selectedDateOffset)
                val currentDayOfWeekStr = SimpleDateFormat("EEEE", Locale.getDefault()).format(baseCalendar.time)
                val todayDayNum = baseCalendar.get(Calendar.DAY_OF_MONTH)
                val todayMonthStr = SimpleDateFormat("MMM", Locale.getDefault()).format(baseCalendar.time)

                val scheduledCourses = courses.filter { course ->
                    course.scheduleDays.contains(currentDayOfWeekStr, ignoreCase = true)
                }

                ScholarCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = when (selectedDateOffset) {
                                        0 -> "Today's Schedule"
                                        1 -> "Tomorrow's Schedule"
                                        -1 -> "Yesterday's Schedule"
                                        else -> "$currentDayOfWeekStr's Schedule"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "$currentDayOfWeekStr, $todayMonthStr $todayDayNum",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    .clickable { selectedDateOffset = 0 },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.CalendarMonth,
                                    contentDescription = "Today",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Day chips selector
                        val todayCalendar = Calendar.getInstance()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (i in -2..2) {
                                val calOffset = todayCalendar.clone() as Calendar
                                val targetOffset = selectedDateOffset + i
                                calOffset.add(Calendar.DAY_OF_MONTH, targetOffset)
                                val isSelected = i == 0
                                val dayLetter = SimpleDateFormat("E", Locale.getDefault()).format(calOffset.time).take(1)
                                val dayNumber = calOffset.get(Calendar.DAY_OF_MONTH).toString()

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 3.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .clickable { selectedDateOffset = targetOffset }
                                        .padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = dayLetter,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = dayNumber,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (scheduledCourses.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No classes scheduled for this day.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                scheduledCourses.forEach { course ->
                                    val courseColor = try {
                                        Color(android.graphics.Color.parseColor(course.colorHex.ifBlank { "#3197D6" }))
                                    } catch (e: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(courseColor.copy(alpha = 0.08f))
                                            .border(1.dp, courseColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                            .clickable { navController.navigate("courseDetail/${course.id}") }
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
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
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = course.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (course.schedule.isNotBlank()) {
                                                Text(
                                                    text = course.schedule,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
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
                ScholarCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Upcoming Assignments",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            BouncyIconButton(
                                onClick = onAddCourseClick,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Rounded.Add,
                                    contentDescription = "Add",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))

                        val upcomingAssigns = assignments
                            .filter { !it.isCompleted && it.dueDateMillis > System.currentTimeMillis() }
                            .sortedBy { it.dueDateMillis }
                            .take(3)

                        if (upcomingAssigns.isEmpty()) {
                            Text(
                                "No pending assignments.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            upcomingAssigns.forEach { assignment ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val catColor = try {
                                        Color(android.graphics.Color.parseColor(assignment.categoryColor.ifEmpty { "#3197D6" }))
                                    } catch (e: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(catColor, CircleShape)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = assignment.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(key = "upcoming_tasks") {
                ScholarCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Upcoming Tasks",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            BouncyIconButton(
                                onClick = onNavigateToTasks,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Rounded.Add,
                                    contentDescription = "Add",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))

                        val upcomingTasks = tasks
                            .filter { !it.isCompleted }
                            .sortedBy { it.dueDateMillis ?: Long.MAX_VALUE }
                            .take(3)

                        if (upcomingTasks.isEmpty()) {
                            Text(
                                "No upcoming tasks.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                                        overflow = TextOverflow.Ellipsis
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
