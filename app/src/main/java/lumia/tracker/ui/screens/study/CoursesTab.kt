package lumia.tracker.ui.screens.study

import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.Course
import lumia.tracker.model.Subject
import lumia.tracker.ui.components.BouncyFloatingActionButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.components.ScholarCardDefaults
import lumia.tracker.ui.screens.study.components.StudyEmptyStateCard
import lumia.tracker.ui.screens.study.components.StudyHeaderStatCard
import lumia.tracker.ui.screens.study.dialogs.EditCourseDialog
import lumia.tracker.ui.screens.study.dialogs.StudyDeleteConfirmationDialog
import lumia.tracker.ui.theme.animateItemEntry
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.ui.util.getTagColors
import lumia.tracker.viewmodel.ScholarViewModel
import java.util.Calendar
import kotlin.math.roundToInt

/**
 * CoursesTab - Lists all registered university / school courses with progress,
 * attendance metrics, and quick navigation in Lumia's calm ambient aesthetic.
 */
@ValueScore(
    score = 92,
    importance = Importance.HIGH,
    description = "Courses directory dashboard with attendance statistics, enrolled cards, and management in ambient soft style",
    category = "Study"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesTab(
    navController: NavController,
    viewModel: ScholarViewModel,
    bottomPadding: PaddingValues,
    onEditCourse: (Course) -> Unit = {},
    onAddCourseClick: () -> Unit = {}
) {
    var courseToEdit by remember { mutableStateOf<Course?>(null) }
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val allAttendance by viewModel.allAttendanceRecords.collectAsStateWithLifecycle()

    // Header stats
    val totalCourses = courses.size
    val distinctAllAttendance = remember(allAttendance) {
        allAttendance.distinctBy {
            val cal = Calendar.getInstance().apply {
                timeInMillis = it.dateMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            "${it.courseId}_${cal.timeInMillis}"
        }
    }
    val totalAttended = remember(distinctAllAttendance) {
        distinctAllAttendance.count { it.status.equals("PRESENT", ignoreCase = true) || it.status.equals("LATE", ignoreCase = true) }
    }
    val totalCancelled = remember(distinctAllAttendance) {
        distinctAllAttendance.count { it.status.equals("Cancelled", ignoreCase = true) || it.status.equals("Holiday", ignoreCase = true) }
    }
    val effectiveAttendanceCount = distinctAllAttendance.size - totalCancelled
    val avgAttendancePct = remember(distinctAllAttendance, totalAttended, effectiveAttendanceCount) {
        if (effectiveAttendanceCount > 0) {
            ((totalAttended.toFloat() / effectiveAttendanceCount) * 100).toInt()
        } else null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = bottomPadding.calculateBottomPadding() + 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Stats Row
            if (courses.isNotEmpty()) {
                item(key = "courses_header_stats") {
                    val isGood = (avgAttendancePct ?: 0) >= 75
                    val statusColor = if (isGood) Color(0xFF10B981) else MaterialTheme.colorScheme.secondary

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StudyHeaderStatCard(
                            value = "$totalCourses",
                            label = "Enrolled",
                            icon = Icons.Rounded.School,
                            iconColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )

                        StudyHeaderStatCard(
                            value = if (avgAttendancePct != null) "$avgAttendancePct%" else "N/A",
                            label = "Attendance",
                            icon = Icons.Rounded.FactCheck,
                            iconColor = statusColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Course List / Empty State
            if (courses.isEmpty()) {
                item(key = "empty_courses") {
                    StudyEmptyStateCard(
                        icon = Icons.Rounded.School,
                        title = "No courses enrolled",
                        description = "Enroll in your university or school courses to track attendance, schedules, and assignments.",
                        buttonText = "Add Course",
                        onButtonClick = onAddCourseClick,
                        modifier = Modifier.padding(top = 16.dp),
                        accentColor = MaterialTheme.colorScheme.primary,
                        buttonContainerColor = MaterialTheme.colorScheme.primary,
                        buttonContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                itemsIndexed(courses, key = { _, course -> course.id }) { index, course ->
                    Box(modifier = Modifier.animateItemEntry(index)) {
                        CourseListingCard(
                            course = course,
                            onClick = { navController.navigate("courseDetail/${course.id}") { launchSingleTop = true } },
                            onEdit = { courseToEdit = course },
                            viewModel = viewModel,
                            onSubjectClick = { subjId -> navController.navigate("subjectDetail/$subjId") { launchSingleTop = true } }
                        )
                    }
                }
            }
        }

        BouncyFloatingActionButton(
            onClick = onAddCourseClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = bottomPadding.calculateBottomPadding() + 16.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add Course", modifier = Modifier.size(24.dp))
        }
    }

    courseToEdit?.let { targetCourse ->
        EditCourseDialog(
            course = targetCourse,
            viewModel = viewModel,
            onDismiss = { courseToEdit = null }
        )
    }
}

/**
 * Modern Course Listing Card with soft ambient styling, clean typography,
 * attendance progress bar, schedule indicators, and quick 1-tap attendance marking.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CourseListingCard(
    course: Course,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    viewModel: ScholarViewModel,
    onSubjectClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val attendanceList by viewModel.getAttendanceForCourse(course.id).collectAsStateWithLifecycle()

    val linkedSubjects = remember(course, subjects) {
        val list = mutableListOf<Subject>()
        if (course.subjectIds.isNotBlank()) {
            val ids = course.subjectIds.split(",").mapNotNull { it.trim().toIntOrNull() }
            list.addAll(subjects.filter { ids.contains(it.id) })
        }
        if (course.subjectId != null && list.none { it.id == course.subjectId }) {
            subjects.find { it.id == course.subjectId }?.let { list.add(it) }
        }
        list.distinct()
    }

    // Attendance calculations
    val distinctAttendanceList = remember(attendanceList) {
        attendanceList.distinctBy {
            Calendar.getInstance().apply {
                timeInMillis = it.dateMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
    }
    val attendedCount = remember(distinctAttendanceList) {
        distinctAttendanceList.count { it.status.equals("PRESENT", ignoreCase = true) || it.status.equals("LATE", ignoreCase = true) }
    }
    val cancelledCount = remember(distinctAttendanceList) {
        distinctAttendanceList.count { it.status.equals("Cancelled", ignoreCase = true) || it.status.equals("Holiday", ignoreCase = true) }
    }
    val effectiveTotal = distinctAttendanceList.size - cancelledCount
    val finalAttended = if (effectiveTotal > 0) attendedCount else course.attendedClasses
    val finalTotal = if (effectiveTotal > 0) effectiveTotal else course.totalClasses
    val attendancePercentage = if (finalTotal > 0) {
        ((finalAttended.toFloat() / finalTotal) * 100).roundToInt()
    } else null

    // Attendance target calculation (75%)
    val safeToMiss = if (finalTotal > 0 && finalAttended * 4 >= finalTotal * 3) {
        (4 * finalAttended - 3 * finalTotal) / 3
    } else 0
    val neededToAttend = if (finalTotal > 0 && finalAttended * 4 < finalTotal * 3) {
        3 * finalTotal - 4 * finalAttended
    } else 0

    // Today's attendance status
    val todayStartMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val todayAttendance = remember(distinctAttendanceList, todayStartMillis) {
        distinctAttendanceList.find { it.dateMillis == todayStartMillis }
    }
    val isPresentToday = remember(todayAttendance) {
        todayAttendance?.status.equals("Present", ignoreCase = true)
    }
    val isAbsentToday = remember(todayAttendance) {
        todayAttendance?.status.equals("Absent", ignoreCase = true)
    }
    val isLateToday = remember(todayAttendance) {
        todayAttendance?.status.equals("Late", ignoreCase = true)
    }

    // Course assignments
    val courseAssignments = remember(assignments, course.id) {
        assignments.filter { it.courseId == course.id }
    }
    val pendingAssignmentsCount = remember(courseAssignments) {
        courseAssignments.count { !it.isCompleted }
    }

    val courseColor = remember(course.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(course.colorHex.ifBlank { "#3197D6" }))
        } catch (e: Exception) {
            Color(0xFF3197D6)
        }
    }

    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) MaterialTheme.colorScheme.surfaceContainerLow
                 else MaterialTheme.colorScheme.surfaceContainerLowest

    ScholarCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        containerColor = cardBg,
        border = ScholarCardDefaults.border()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Monogram Squircle, Title & Subtitle, Options Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Course Code / Initials Monogram Squircle
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(courseColor.copy(alpha = if (isDark) 0.16f else 0.12f), RoundedCornerShape(13.dp))
                        .border(0.5.dp, courseColor.copy(alpha = 0.25f), RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (course.code.isNotBlank()) course.code.take(3).uppercase() else course.name.take(2).uppercase(),
                        fontWeight = FontWeight.Black,
                        color = courseColor,
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

                    val subtitle = remember(course.code, course.instructor) {
                        listOf(course.code, course.instructor).filter { it.isNotBlank() }.joinToString(" • ")
                    }
                    if (subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Single Clean Options Menu Button
                Box {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        BouncyIconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Course") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Course") },
                            onClick = {
                                showMenu = false
                                showDeleteConfirmation = true
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Attendance Progress & Health Section
            if (finalTotal > 0 && attendancePercentage != null) {
                val isGood = attendancePercentage >= 75
                val isWarning = attendancePercentage in 50..74
                val statusColor = if (isGood) Color(0xFF10B981) else if (isWarning) Color(0xFFF59E0B) else Color(0xFFEF4444)
                val statusLabel = if (isGood) {
                    if (safeToMiss > 0) "$safeToMiss safe to miss" else "On track"
                } else {
                    "Need $neededToAttend classes"
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(statusColor, CircleShape)
                            )
                            Text(
                                text = "$attendancePercentage% Attendance",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "• $finalAttended/$finalTotal",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = statusColor.copy(alpha = 0.12f),
                            border = BorderStroke(0.5.dp, statusColor.copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    val progress = (finalAttended.toFloat() / finalTotal).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = statusColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            // Schedule & Assignments Chips Row
            val hasSchedule = course.scheduleDays.isNotBlank() || course.schedule.isNotBlank() || course.scheduleStartTime.isNotBlank()
            if (hasSchedule || pendingAssignmentsCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasSchedule) {
                        val daysShort = course.scheduleDays.split(",").map { it.trim().take(3) }.filter { it.isNotBlank() }.joinToString(", ")
                        val timeRange = if (course.scheduleStartTime.isNotBlank()) {
                            "${course.scheduleStartTime} - ${course.scheduleEndTime}".trim()
                        } else ""
                        val scheduleStr = listOf(daysShort, timeRange, course.schedule).filter { it.isNotBlank() }.joinToString(" • ")

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = scheduleStr.ifBlank { "Schedule set" },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (pendingAssignmentsCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Assignment,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "$pendingAssignmentsCount due",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Linked Subjects & Tags Chips
            val tagsList = remember(course.tags) {
                course.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
            }
            if (tagsList.isNotEmpty() || linkedSubjects.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    linkedSubjects.forEach { subj ->
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onSubjectClick(subj.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FolderOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = subj.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    tagsList.forEach { tag ->
                        val (bgColor, textColor) = getTagColors(tag)
                        Surface(
                            shape = CircleShape,
                            color = bgColor.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, textColor.copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Quick 1-Tap Attendance Footer Action
            val attBg = when {
                isPresentToday -> Color(0xFF10B981).copy(alpha = 0.12f)
                isAbsentToday -> Color(0xFFEF4444).copy(alpha = 0.12f)
                isLateToday -> Color(0xFFF59E0B).copy(alpha = 0.12f)
                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            }
            val attBorder = when {
                isPresentToday -> BorderStroke(0.5.dp, Color(0xFF10B981).copy(alpha = 0.30f))
                isAbsentToday -> BorderStroke(0.5.dp, Color(0xFFEF4444).copy(alpha = 0.30f))
                isLateToday -> BorderStroke(0.5.dp, Color(0xFFF59E0B).copy(alpha = 0.30f))
                else -> BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
            }
            val attIconColor = when {
                isPresentToday -> Color(0xFF10B981)
                isAbsentToday -> Color(0xFFEF4444)
                isLateToday -> Color(0xFFF59E0B)
                else -> MaterialTheme.colorScheme.primary
            }
            val attText = when {
                isPresentToday -> "Attended Today (Present)"
                isAbsentToday -> "Marked Absent Today"
                isLateToday -> "Marked Late Today"
                else -> "Mark Today's Attendance"
            }
            val attIcon = when {
                isPresentToday -> Icons.Rounded.CheckCircle
                isAbsentToday -> Icons.Rounded.Close
                isLateToday -> Icons.Rounded.Schedule
                else -> Icons.Rounded.Check
            }

            Surface(
                shape = CircleShape,
                color = attBg,
                border = attBorder,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .bouncyClick {
                        viewModel.toggleTodayAttendance(course.id, todayStartMillis)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = attIcon,
                        contentDescription = null,
                        tint = attIconColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = attText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = attIconColor
                    )
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        StudyDeleteConfirmationDialog(
            title = "Delete Course",
            message = "Are you sure you want to delete ${course.name}? All associated attendance records and course details will be permanently removed.",
            onConfirmDelete = {
                showDeleteConfirmation = false
                viewModel.deleteCourse(course)
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }
}
