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
import androidx.compose.material.icons.rounded.Add
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
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.theme.animateItemEntry
import lumia.tracker.ui.theme.bouncyScale
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * CoursesTab - Lists all registered university / school courses with progress,
 * syllabus coverage, and quick navigation.
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
                top = bottomPadding.calculateTopPadding() + 16.dp, 
                bottom = bottomPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (courses.isEmpty()) {
                item {
                    ScholarCard(
                        modifier = Modifier.fillMaxWidth().height(240.dp),
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                "No courses yet",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(courses, key = { _, course -> course.id }) { index, course ->
                    Box(modifier = Modifier.animateItemEntry(index)) {
                        CourseItemCard(
                            course = course,
                            onClick = { navController.navigate("courseDetail/${course.id}") },
                            onEdit = { courseToEdit = course },
                            viewModel = viewModel,
                            onSubjectClick = { subjId -> navController.navigate("subjectDetail/$subjId") }
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
