package lumia.tracker.ui.screens.study.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.model.Course
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * CourseItemCard - Professional Course Overview Card with Attendance Health Gauge,
 * Pending Assignment Indicators, Schedule Chips, and Linked Subjects.
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
        val list = mutableListOf<lumia.tracker.model.Subject>()
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
        attendanceList.count { it.status == "PRESENT" || it.status == "LATE" }
    }
    val totalAttendance = attendanceList.size
    val attendancePercentage = if (totalAttendance > 0) {
        ((attendedCount.toFloat() / totalAttendance) * 100).toInt()
    } else null

    // Pending assignments for this course
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
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Course Icon, Title & Code, Instructor, Options Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(courseColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (course.code.isNotBlank()) course.code.take(3).uppercase() else course.name.take(2).uppercase(),
                        fontWeight = FontWeight.Black,
                        color = courseColor,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (course.code.isNotBlank()) {
                            Text(
                                text = course.code,
                                style = MaterialTheme.typography.labelSmall,
                                color = courseColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (course.instructor.isNotBlank()) {
                            if (course.code.isNotBlank()) Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

                Box {
                    BouncyIconButton(onClick = { expanded = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Course") },
                            onClick = {
                                expanded = false
                                viewModel.deleteCourse(course)
                            },
                            leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Badges Row: Attendance Health & Pending Assignments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attendance Gauge Pill
                if (attendancePercentage != null) {
                    val isGood = attendancePercentage >= 75
                    val isWarning = attendancePercentage in 65..74
                    val statusColor = if (isGood) MaterialTheme.colorScheme.tertiary else if (isWarning) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                    val containerColor = if (isGood) MaterialTheme.colorScheme.tertiaryContainer else if (isWarning) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)

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
                                imageVector = if (isGood) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
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

                // Pending Assignments Pill
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
                        Text(
                            text = "${courseAssignments.size} Assignments",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Schedule Section
            val hasSchedule = course.scheduleDays.isNotBlank() || course.schedule.isNotBlank()
            if (hasSchedule) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Rounded.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    val daysShort = course.scheduleDays.split(",").map { it.trim().take(3) }.filter { it.isNotBlank() }.joinToString(", ")
                    val scheduleStr = listOf(daysShort, course.schedule).filter { it.isNotBlank() }.joinToString(" • ")
                    Text(
                        text = scheduleStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
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
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
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
