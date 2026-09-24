package lumia.tracker.ui.screens.study

import android.text.format.DateFormat
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.*
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.viewmodel.ScholarViewModel
import java.util.*

@Composable
fun MetricHeroCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    containerColor: Color = Color.Transparent,
    modifier: Modifier = Modifier
) {
    ScholarCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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

    // Last 7 days focus activity data for the native focus strip
    val last7DaysData = remember(pomodoroSessions) {
        val cal = Calendar.getInstance()
        (6 downTo 0).map { daysAgo ->
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val dayStart = cal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val dayEnd = dayStart + 24 * 60 * 60 * 1000L
            val dayLabel = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Mon"
                Calendar.TUESDAY -> "Tue"
                Calendar.WEDNESDAY -> "Wed"
                Calendar.THURSDAY -> "Thu"
                Calendar.FRIDAY -> "Fri"
                Calendar.SATURDAY -> "Sat"
                Calendar.SUNDAY -> "Sun"
                else -> ""
            }
            val mins = pomodoroSessions
                .filter { it.dateMillis in dayStart until dayEnd }
                .sumOf { it.durationMinutes }
            Triple(dayLabel, mins, daysAgo == 0)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = paddingValues.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
            // 2x2 Sleek Metric Hero Cards
            item(key = "summary_metrics_grid") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Row 1: Total Focus & Completed Tasks
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricHeroCard(
                            title = "Total Focus",
                            value = focusHoursFormatted,
                            icon = Icons.Rounded.Timer,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricHeroCard(
                            title = "Tasks Done",
                            value = totalCompletedTasksCount.toString(),
                            icon = Icons.Rounded.CheckCircle,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2: Finished Rate & Avg Attendance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricHeroCard(
                            title = "Finished Rate",
                            value = "$assignmentRate%",
                            icon = Icons.Rounded.AssignmentTurnedIn,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricHeroCard(
                            title = "Attendance",
                            value = avgAttendanceRate,
                            icon = Icons.Rounded.School,
                            color = Color(0xFFFF9500),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Weekly Focus Activity Bar Strip
            item(key = "weekly_focus_strip") {
                ScholarCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.BarChart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "This Week's Focus",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            val weeklyTotalMins = last7DaysData.sumOf { it.second }
                            Text(
                                text = if (weeklyTotalMins >= 60) "${weeklyTotalMins / 60}h ${weeklyTotalMins % 60}m" else "${weeklyTotalMins}m",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        val maxMins = (last7DaysData.maxOfOrNull { it.second } ?: 60).coerceAtLeast(60).toFloat()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            last7DaysData.forEach { (day, mins, isToday) ->
                                val targetHeightFrac = (mins / maxMins).coerceIn(0.08f, 1f)
                                val animatedHeightFrac by animateFloatAsState(
                                    targetValue = targetHeightFrac,
                                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                                    label = "barHeight_$day"
                                )
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (mins > 0) {
                                        Text(
                                            text = "${mins}m",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 11.sp,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .fillMaxHeight(animatedHeightFrac)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isToday) MaterialTheme.colorScheme.primary
                                                else if (mins > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Attendance Health Insight Alert Card
            if (allAttendance.isNotEmpty()) {
                item(key = "attendance_health_card") {
                    val attendedCount = remember(allAttendance) {
                        allAttendance.count { it.status.equals("PRESENT", ignoreCase = true) || it.status.equals("LATE", ignoreCase = true) }
                    }
                    val attendanceRateInt = ((attendedCount.toFloat() / allAttendance.size) * 100).toInt()
                    val isGoodStanding = attendanceRateInt >= 75

                    ScholarCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isGoodStanding) Color(0xFF34C759).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isGoodStanding) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber,
                                    contentDescription = null,
                                    tint = if (isGoodStanding) Color(0xFF34C759) else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isGoodStanding) "Attendance is looking great" else "Attendance needs attention",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isGoodStanding) {
                                        "Your overall attendance is $attendanceRateInt%, above the 75% target."
                                    } else {
                                        "Your overall attendance is $attendanceRateInt%, below the 75% target. Try not to miss upcoming classes."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Streaks & Daily Focus Card
            item(key = "streaks_analytics_hero") {
                val streakLongest by viewModel.streakLongest.collectAsStateWithLifecycle()
                val streakCurrent by viewModel.streakCurrent.collectAsStateWithLifecycle()
                val streakTotalComplete by viewModel.streakTotalComplete.collectAsStateWithLifecycle()

                ScholarCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Whatshot,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Study Streaks",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Current Streak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$streakCurrent days",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Longest Streak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$streakLongest days",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Completed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$streakTotalComplete",
                                    style = MaterialTheme.typography.titleMedium,
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
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Test Scores",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Filter Chips
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedCourseId == -1,
                                        onClick = { selectedCourseId = -1 },
                                        label = { Text("All Courses", style = MaterialTheme.typography.labelMedium) }
                                    )
                                }
                                items(courses, key = { it.id }) { course ->
                                    FilterChip(
                                        selected = selectedCourseId == course.id,
                                        onClick = { selectedCourseId = course.id },
                                        label = { Text(course.name, style = MaterialTheme.typography.labelMedium) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            val filteredTestRecords = if (selectedCourseId == -1) allTestRecords else allTestRecords.filter { it.courseId == selectedCourseId }
                            val totalTests = filteredTestRecords.size
                            val pctScores = filteredTestRecords.map { if (it.totalMarks > 0) (it.marksObtained / it.totalMarks) * 100f else 0f }
                            val overallAverage = if (pctScores.isNotEmpty()) pctScores.average().toFloat() else 0f
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
                                        Text("Average", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${overallAverage.toInt()}%",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Tests", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$totalTests",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Pass Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$passRate%",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = if (passRate >= 75) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            // Subject Scores Breakdown
                            if (subjects.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(18.dp))
                                Text(
                                    text = "Subject Scores",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
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
                                                subjAvg >= 85f -> "Great" to MaterialTheme.colorScheme.tertiary
                                                subjAvg >= 70f -> "Good" to MaterialTheme.colorScheme.primary
                                                subjAvg >= 50f -> "Fair" to MaterialTheme.colorScheme.secondary
                                                else -> "Needs Work" to MaterialTheme.colorScheme.error
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
                                                        fontWeight = FontWeight.Medium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Text(
                                                        text = statusText,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = statusColor
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "${subjAvg.toInt()}%",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                LinearProgressIndicator(
                                                    progress = { subjAvg / 100f },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(4.dp)
                                                        .clip(RoundedCornerShape(2.dp)),
                                                    color = statusColor,
                                                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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

            // 2 Essential Minimalist Progress Indicators
            item(key = "indicator_focus_progress") {
                ScholarCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Timer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Focus Goal",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${pomodoroSessions.size} total sessions",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = focusHoursFormatted,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val weeklyFocusGoalMins = 600
                        val weeklyProgress = (totalFocusMins.toFloat() / weeklyFocusGoalMins).coerceIn(0f, 1f)

                        LinearProgressIndicator(
                            progress = { weeklyProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            item(key = "indicator_task_completion") {
                ScholarCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Tasks Finished",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$completedAssignments of $totalAssignments completed",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "$assignmentRate%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val completionFraction = if (totalAssignments > 0) completedAssignments.toFloat() / totalAssignments else 0f
                        LinearProgressIndicator(
                            progress = { completionFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.tertiary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // Recent Activity Section
            if (showActionHistory) {
                item(key = "activity_log_header") {
                    ScholarCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.History,
                                    contentDescription = "History",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Recent Activity",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (actionLogs.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No recent activity yet.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    actionLogs.take(20).forEach { log ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = log.actionText,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            val date = DateFormat.format("MMM d, HH:mm", Date(log.timestampMillis)).toString()
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
    }


