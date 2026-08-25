package lumia.tracker.ui.screens.study

import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore

import lumia.tracker.ui.screens.study.dialogs.*

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
import lumia.tracker.ui.components.BouncyFloatingActionButton
import lumia.tracker.ui.screens.study.components.CourseItemCard
import lumia.tracker.ui.screens.study.components.StudyEmptyStateCard
import lumia.tracker.ui.screens.study.components.StudyHeaderStatCard
import lumia.tracker.ui.screens.study.dialogs.EditCourseDialog
import lumia.tracker.ui.theme.animateItemEntry
import lumia.tracker.ui.theme.bouncyScale
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * CoursesTab - Lists all registered university / school courses with progress,
 * attendance metrics, and quick navigation.
 */
@ValueScore(
    score = 88,
    importance = Importance.HIGH,
    description = "Courses directory dashboard with attendance statistics, enrolled cards, and management",
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
            clipToPadding = false,
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
                        StudyHeaderStatCard(
                            value = "$totalCourses",
                            label = "Enrolled",
                            icon = Icons.Rounded.School,
                            iconColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )

                        // Attendance Rate
                        val isGood = (avgAttendancePct ?: 0) >= 75
                        val statusColor = if (isGood) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
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
                        icon = Icons.AutoMirrored.Rounded.MenuBook,
                        title = "No courses enrolled yet",
                        description = "Add your university or school courses to track lectures, attendance, assignments, and curriculum.",
                        buttonText = "Add Your First Course",
                        onButtonClick = onAddCourseClick,
                        modifier = Modifier.padding(top = 16.dp),
                        accentColor = MaterialTheme.colorScheme.primary,
                        buttonContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        buttonContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
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
