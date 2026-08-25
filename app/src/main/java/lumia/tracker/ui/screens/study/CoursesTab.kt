package lumia.tracker.ui.screens.study

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.Course
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyFloatingActionButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.screens.study.components.CourseItemCard
import lumia.tracker.ui.screens.study.dialogs.EditCourseDialog
import lumia.tracker.ui.theme.animateItemEntry
import lumia.tracker.ui.theme.bouncyScale
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * CoursesTab - Lists all registered university / school courses with progress,
 * attendance metrics, and quick navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesTab(
    navController: NavController,
    viewModel: ScholarViewModel,
    bottomPadding: PaddingValues,
    onEditCourse: (Course) -> Unit = {},
    onAddCourseClick: () -> Unit
) {
    var courseToEdit by remember { mutableStateOf<Course?>(null) }
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val allAttendance by viewModel.allAttendanceRecords.collectAsStateWithLifecycle()

    // Header stats
    val totalCourses = courses.size
    val totalAttended = remember(allAttendance) {
        allAttendance.count { it.status.equals("PRESENT", ignoreCase = true) || it.status.equals("LATE", ignoreCase = true) }
    }
    val avgAttendancePct = remember(allAttendance) {
        if (allAttendance.isNotEmpty()) {
            ((totalAttended.toFloat() / allAttendance.size) * 100).toInt()
        } else null
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            val src = remember { MutableInteractionSource() }
            BouncyFloatingActionButton(
                onClick = onAddCourseClick,
                interactionSource = src,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .padding(bottom = bottomPadding.calculateBottomPadding())
                    .bouncyScale(src)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Course")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = bottomPadding.calculateTopPadding() + 12.dp,
                bottom = bottomPadding.calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Stats Row
            if (courses.isNotEmpty()) {
                item(key = "courses_header_stats") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total Courses
                        ScholarCard(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.School,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "$totalCourses",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Enrolled",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Attendance Rate
                        ScholarCard(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val isGood = (avgAttendancePct ?: 0) >= 75
                                val statusColor = if (isGood) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(statusColor.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.FactCheck,
                                        contentDescription = null,
                                        tint = statusColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (avgAttendancePct != null) "$avgAttendancePct%" else "N/A",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Attendance",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Course List / Empty State
            if (courses.isEmpty()) {
                item(key = "empty_courses") {
                    ScholarCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(38.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "No courses enrolled yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Add your university or school courses to track lectures, attendance, assignments, and curriculum.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            BouncyButton(
                                onClick = onAddCourseClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Your First Course", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(courses, key = { _, course -> course.id }) { index, course ->
                    Box(modifier = Modifier.animateItemEntry(index)) {
                        CourseItemCard(
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
    }

    if (courseToEdit != null) {
        EditCourseDialog(
            course = courseToEdit!!,
            viewModel = viewModel,
            onDismiss = { courseToEdit = null }
        )
    }
}
