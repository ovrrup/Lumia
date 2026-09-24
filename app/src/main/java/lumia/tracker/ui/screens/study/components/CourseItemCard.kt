package lumia.tracker.ui.screens.study.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.model.Course
import lumia.tracker.model.Subject
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.util.getTagColors
import lumia.tracker.viewmodel.ScholarViewModel
import java.util.Calendar
import kotlin.math.roundToInt


/**
 * CourseItemCard - Modern Academic Course Overview Card with Color Accent,
 * Attendance Health Gauge, Schedule Chips, Tag Chips, and Linked Subjects.
 */
@ValueScore(
    score = 88,
    importance = Importance.HIGH,
    description = "Course summary card with attendance health gauge, schedule chips, and linked subjects",
    category = "Study"
)
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CourseItemCard(
    course: Course,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    viewModel: ScholarViewModel,
    onSubjectClick: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
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

    // Attendance calculation with cancelled/holiday subtraction
    val attendedCount = remember(attendanceList) {
        attendanceList.count { it.status.equals("PRESENT", ignoreCase = true) || it.status.equals("LATE", ignoreCase = true) }
    }
    val cancelledCount = remember(attendanceList) {
        attendanceList.count { it.status.equals("Cancelled", ignoreCase = true) || it.status.equals("Holiday", ignoreCase = true) }
    }
    val effectiveTotal = attendanceList.size - cancelledCount
    val finalAttended = if (effectiveTotal > 0) attendedCount else course.attendedClasses
    val finalTotal = if (effectiveTotal > 0) effectiveTotal else course.totalClasses
    val attendancePercentage = if (finalTotal > 0) {
        ((finalAttended.toFloat() / finalTotal) * 100).roundToInt()
    } else null

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

    // Attendance calculation with exact integer mathematics (target: 75% attendance)
    val safeToMiss = if (finalTotal > 0 && finalAttended * 4 >= finalTotal * 3) {
        (4 * finalAttended - 3 * finalTotal) / 3
    } else 0
    val neededToAttend = if (finalTotal > 0 && finalAttended * 4 < finalTotal * 3) {
        3 * finalTotal - 4 * finalAttended
    } else 0
    val attendanceRecommendation = if (attendancePercentage != null) {
        if (attendancePercentage >= 75) {
            if (safeToMiss > 0) {
                val classWord = if (safeToMiss == 1) "class" else "classes"
                "Safe to miss $safeToMiss $classWord"
            } else {
                "On track"
            }
        } else {
            val classWord = if (neededToAttend == 1) "class" else "classes"
            "Need $neededToAttend more $classWord"
        }
    } else ""


    ScholarCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Monogram Badge, Course Name & Instructor, Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Course Code / Initials Monogram Capsule Badge
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(courseColor.copy(alpha = 0.15f), CircleShape),
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
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Options Menu
                Box {
                    BouncyIconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Course") },
                            onClick = {
                                expanded = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Course") },
                            onClick = {
                                expanded = false
                                viewModel.deleteCourse(course)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            // Badges Section: Smart Attendance Health Gauge & Pending Assignments
            val hasAttendance = attendancePercentage != null
            val hasAssignments = courseAssignments.isNotEmpty()

            if (hasAttendance || hasAssignments) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Smart Attendance Status Badge
                    if (attendancePercentage != null) {
                        val isGood = attendancePercentage >= 75
                        val isWarning = attendancePercentage in 50..74
                        val statusColor = if (isGood) Color(0xFF10B981) else if (isWarning) Color(0xFFF59E0B) else Color(0xFFEF4444)

                        Surface(
                            shape = CircleShape,
                            color = statusColor.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(statusColor, CircleShape)
                                )
                                Text(
                                    text = "$attendancePercentage% • $attendanceRecommendation",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Pending Assignments Badge
                    if (pendingAssignmentsCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                )
                                Text(
                                    text = "$pendingAssignmentsCount to do",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (courseAssignments.isNotEmpty()) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "${courseAssignments.size} tasks done",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Schedule Row
            val hasSchedule = course.scheduleDays.isNotBlank() || course.schedule.isNotBlank() || course.scheduleStartTime.isNotBlank()
            if (hasSchedule) {
                Spacer(modifier = Modifier.height(10.dp))
                val daysShort = course.scheduleDays.split(",").map { it.trim().take(3) }.filter { it.isNotBlank() }.joinToString(", ")
                val timeRange = if (course.scheduleStartTime.isNotBlank()) {
                    "${course.scheduleStartTime} - ${course.scheduleEndTime}".trim()
                } else ""
                val scheduleStr = listOf(daysShort, timeRange, course.schedule).filter { it.isNotBlank() }.joinToString(" • ")

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = scheduleStr.ifBlank { "Schedule set" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Course Tag Chips
            val tagsList = remember(course.tags) {
                course.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
            }
            if (tagsList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tagsList.forEach { tag ->
                        val (bgColor, textColor) = getTagColors(tag)
                        Surface(
                            shape = CircleShape,
                            color = bgColor
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Linked Subjects Chips
            if (linkedSubjects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (subj in linkedSubjects) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onSubjectClick(subj.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                                Text(
                                    text = subj.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
