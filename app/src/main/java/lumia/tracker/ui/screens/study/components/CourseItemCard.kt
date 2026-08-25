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
import lumia.tracker.ui.util.getTagColors
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * CourseItemCard - Modern Academic Course Overview Card with Color Accent,
 * Attendance Health Gauge, Schedule Chips, Tag Chips, and Linked Subjects.
 */
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

    // Attendance calculation
    val attendedCount = remember(attendanceList) {
        attendanceList.count { it.status.equals("PRESENT", ignoreCase = true) || it.status.equals("LATE", ignoreCase = true) }
    }
    val totalAttendance = attendanceList.size
    val attendancePercentage = if (totalAttendance > 0) {
        ((attendedCount.toFloat() / totalAttendance) * 100).toInt()
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

    ScholarCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Monogram / Code Badge, Course Name, Instructor, Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Course Code / Initials Monogram Badge
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(courseColor.copy(alpha = 0.14f), RoundedCornerShape(14.dp)),
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

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (course.code.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = courseColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = course.code,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = courseColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (course.instructor.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = course.instructor,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Options Menu
                Box {
                    BouncyIconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.size(36.dp)
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

            // Badges Section: Attendance Gauge & Pending Assignments
            val hasAttendance = attendancePercentage != null
            val hasAssignments = courseAssignments.isNotEmpty()

            if (hasAttendance || hasAssignments) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attendance Status Badge
                    if (attendancePercentage != null) {
                        val isGood = attendancePercentage >= 75
                        val isWarning = attendancePercentage in 65..74
                        val statusColor = if (isGood) MaterialTheme.colorScheme.tertiary else if (isWarning) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        val containerColor = if (isGood) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f) else if (isWarning) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = containerColor
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isGood) Icons.Rounded.CheckCircle else if (isWarning) Icons.Rounded.Warning else Icons.Rounded.Error,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "$attendancePercentage% Attendance ($attendedCount/$totalAttendance)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Pending Assignments Badge
                    if (pendingAssignmentsCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AssignmentLate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "$pendingAssignmentsCount Pending",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (courseAssignments.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.TaskAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "${courseAssignments.size} Assignments",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Schedule Row
            val hasSchedule = course.scheduleDays.isNotBlank() || course.schedule.isNotBlank() || course.scheduleStartTime.isNotBlank()
            if (hasSchedule) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    val daysShort = course.scheduleDays.split(",").map { it.trim().take(3) }.filter { it.isNotBlank() }.joinToString(", ")
                    val timeRange = if (course.scheduleStartTime.isNotBlank()) {
                        "${course.scheduleStartTime} - ${course.scheduleEndTime}".trim()
                    } else ""
                    val scheduleStr = listOf(daysShort, timeRange, course.schedule).filter { it.isNotBlank() }.joinToString(" • ")
                    Text(
                        text = scheduleStr.ifBlank { "Schedule set" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
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
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tagsList.forEach { tag ->
                        val (bgColor, textColor) = getTagColors(tag)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = bgColor
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalOffer,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = textColor
                                )
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Linked Subjects Chips
            if (linkedSubjects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (subj in linkedSubjects) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.clickable { onSubjectClick(subj.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
