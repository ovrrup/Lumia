package lumia.tracker.ui.screens.study

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.*
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.components.ScholarHeroCard
import lumia.tracker.ui.screens.study.charts.*
import lumia.tracker.viewmodel.ScholarViewModel
import java.util.*

@Composable
fun MetricHeroCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    ScholarCard(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(containerColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsTab(
    navController: NavController,
    viewModel: ScholarViewModel,
    paddingValues: PaddingValues
) {
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val actionLogs by viewModel.actionLogs.collectAsStateWithLifecycle()
    val pomodoroSessions by viewModel.pomodoroSessions.collectAsStateWithLifecycle()
    val allTestRecords by viewModel.allTestRecords.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val topics by viewModel.allTopics.collectAsStateWithLifecycle()
    val allAttendance by viewModel.allAttendanceRecords.collectAsStateWithLifecycle()

    val totalAssignments = assignments.size
    val completedAssignments = assignments.count { it.isCompleted }
    val assignmentRate = if (totalAssignments > 0) ((completedAssignments.toFloat() / totalAssignments) * 100).toInt() else 0

    val totalFocusMins = remember(pomodoroSessions) {
        pomodoroSessions.sumOf { it.durationMinutes }
    }
    val focusHoursFormatted = remember(totalFocusMins) {
        val h = totalFocusMins / 60
        val m = totalFocusMins % 60
        if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    val totalCompletedTasksCount = remember(assignments) {
        completedAssignments
    }

    val avgAttendanceRate = remember(allAttendance) {
        if (allAttendance.isNotEmpty()) {
            val attended = allAttendance.count { it.status.equals("PRESENT", ignoreCase = true) || it.status.equals("LATE", ignoreCase = true) }
            "${((attended.toFloat() / allAttendance.size) * 100).toInt()}%"
        } else "N/A"
    }

    val showActionHistory by viewModel.showActionHistory.collectAsStateWithLifecycle()
    var selectedCourseId by remember { mutableStateOf(-1) }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = paddingValues.calculateTopPadding() + 12.dp,
                bottom = paddingValues.calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Title
            item {
                Text(
                    text = "Academic Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 2x2 Summary Metric Hero Cards
            item(key = "summary_metrics_grid") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Row 1: Total Focus & Completed Tasks
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricHeroCard(
                            title = "Total Focus",
                            value = focusHoursFormatted,
                            icon = Icons.Rounded.Timer,
                            color = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        )
                        MetricHeroCard(
                            title = "Tasks Completed",
                            value = totalCompletedTasksCount.toString(),
                            icon = Icons.Rounded.CheckCircle,
                            color = MaterialTheme.colorScheme.tertiary,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2: Assignments Rate & Avg Attendance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricHeroCard(
                            title = "Assignment Rate",
                            value = "$assignmentRate%",
                            icon = Icons.Rounded.AssignmentTurnedIn,
                            color = MaterialTheme.colorScheme.secondary,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        )
                        MetricHeroCard(
                            title = "Avg Attendance",
                            value = avgAttendanceRate,
                            icon = Icons.Rounded.School,
                            color = Color(0xFFFF9500),
                            containerColor = Color(0xFFFF9500).copy(alpha = 0.15f),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Streaks & Daily Focus Card
            item(key = "streaks_analytics_hero") {
                val streakTotalNormal by viewModel.streakTotalNormal.collectAsStateWithLifecycle()
                val streakTotalComplete by viewModel.streakTotalComplete.collectAsStateWithLifecycle()
                val streakLongest by viewModel.streakLongest.collectAsStateWithLifecycle()
                val streakCurrent by viewModel.streakCurrent.collectAsStateWithLifecycle()

                ScholarCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Whatshot,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = "Study Streak & Momentum",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "${viewModel.getSessionsTodayCount()} Sessions Today",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Current Streak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$streakCurrent days",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Longest Streak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$streakLongest days",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Full Target", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$streakTotalComplete",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }

            // Academic & Test Analytics
            if (allTestRecords.isNotEmpty()) {
                item(key = "test_analytics_section") {
                    ScholarCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.School,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Text(
                                        text = "Assessment Performance",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Filter Chips
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedCourseId == -1,
                                        onClick = { selectedCourseId = -1 },
                                        label = { Text("All Courses") }
                                    )
                                }
                                items(courses) { course ->
                                    FilterChip(
                                        selected = selectedCourseId == course.id,
                                        onClick = { selectedCourseId = course.id },
                                        label = { Text(course.name) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val filteredTestRecords = if (selectedCourseId == -1) allTestRecords else allTestRecords.filter { it.courseId == selectedCourseId }
                            val totalTests = filteredTestRecords.size
                            val pctScores = filteredTestRecords.map { if (it.totalMarks > 0) (it.marksObtained / it.totalMarks) * 100f else 0f }
                            val overallAverage = if (pctScores.isNotEmpty()) pctScores.average().toFloat() else 0f
                            val perfectScoresCount = filteredTestRecords.count { it.marksObtained == it.totalMarks && it.totalMarks > 0 }
                            val passingTestsCount = pctScores.count { it >= 50f }
                            val passRate = if (totalTests > 0) (passingTestsCount.toFloat() / totalTests * 100).toInt() else 0

                            if (totalTests == 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No tests logged for this selection.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("GPA / Avg", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${overallAverage.toInt()}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Tests Taken", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("$totalTests", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Pass Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "$passRate%",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            color = if (passRate >= 75) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                if (perfectScoresCount > 0) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.EmojiEvents,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "$perfectScoresCount Perfect Scores Achieved!",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
                                }
                            }

                            // Subject Strength & Mastery
                            if (subjects.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "Subject Mastery Breakdown",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    subjects.forEach { subj ->
                                        val subjTests = allTestRecords.filter { test ->
                                            test.subjectId == subj.id ||
                                                (test.topicId != null && topics.find { it.id == test.topicId }?.subjectId == subj.id) ||
                                                (test.courseId != null && courses.find { it.id == test.courseId }?.let { c ->
                                                    c.subjectId == subj.id ||
                                                        c.subjectIds.split(",").mapNotNull { it.trim().toIntOrNull() }.contains(subj.id)
                                                } == true)
                                        }
                                        if (subjTests.isNotEmpty()) {
                                            val subjAvg = subjTests.map { if (it.totalMarks > 0) (it.marksObtained / it.totalMarks) * 100f else 0f }.average().toFloat()
                                            val (statusText, statusColor) = when {
                                                subjAvg >= 85f -> "MASTER" to MaterialTheme.colorScheme.tertiary
                                                subjAvg >= 70f -> "PROFICIENT" to MaterialTheme.colorScheme.primary
                                                subjAvg >= 50f -> "DEVELOPING" to MaterialTheme.colorScheme.secondary
                                                else -> "NEEDS WORK" to MaterialTheme.colorScheme.error
                                            }

                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = subj.name,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = statusColor.copy(alpha = 0.15f)
                                                    ) {
                                                        Text(
                                                            text = statusText,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Black,
                                                            color = statusColor,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "${subjAvg.toInt()}%",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                LinearProgressIndicator(
                                                    progress = { subjAvg / 100f },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(5.dp)
                                                        .clip(RoundedCornerShape(3.dp)),
                                                    color = statusColor,
                                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
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

            // Sleek Chart Containers
            item(key = "chart_weekly_assignments") {
                ScholarCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        WeeklyAssignmentsDueChart(
                            modifier = Modifier.fillMaxWidth(),
                            assignments = assignments,
                            courses = courses,
                            onToggleCompletion = { viewModel.toggleAssignmentCompleted(it) }
                        )
                    }
                }
            }

            item(key = "chart_assignment_donut") {
                ScholarCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AssignmentsStatusDonutChart(
                            modifier = Modifier.fillMaxWidth(),
                            total = totalAssignments,
                            completed = completedAssignments,
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            primaryColor = MaterialTheme.colorScheme.primary,
                            secondaryColor = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            item(key = "chart_focus_course") {
                ScholarCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        FocusTimePerCourseChart(
                            modifier = Modifier.fillMaxWidth(),
                            sessions = pomodoroSessions,
                            courses = courses
                        )
                    }
                }
            }

            if (courses.isNotEmpty() && assignments.isNotEmpty()) {
                item(key = "chart_assignments_bar") {
                    ScholarCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            AssignmentsPerCourseBarChart(
                                modifier = Modifier.fillMaxWidth(),
                                courses = courses,
                                assignments = assignments,
                                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                                barColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item(key = "chart_category_dist") {
                ScholarCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AssignmentCategoryDistributionChart(
                            modifier = Modifier.fillMaxWidth(),
                            assignments = assignments
                        )
                    }
                }
            }

            item(key = "chart_pomodoro_heatmap") {
                ScholarCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        PomodoroHeatmapChart(
                            modifier = Modifier.fillMaxWidth(),
                            sessions = pomodoroSessions,
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            primaryColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Action History Telemetry Log Section
            if (showActionHistory) {
                item(key = "telemetry_header") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = "History",
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Activity Log",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (actionLogs.isEmpty()) {
                    item(key = "telemetry_empty") {
                        ScholarCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No recorded actions yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(actionLogs.take(25), key = { it.id }) { log ->
                        ScholarCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = log.actionText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val date = DateFormat.format("MMM dd, yyyy • HH:mm", Date(log.timestampMillis)).toString()
                                    Text(
                                        text = date,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
