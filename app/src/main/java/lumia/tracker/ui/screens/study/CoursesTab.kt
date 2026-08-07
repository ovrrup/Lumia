package lumia.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import lumia.tracker.ui.theme.animateItemEntry
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.Course
import lumia.tracker.ui.components.BouncyFloatingActionButton
import lumia.tracker.ui.components.GlassCard
import lumia.tracker.ui.screens.study.dialogs.*
import lumia.tracker.ui.theme.bouncyScale
import lumia.tracker.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesTab(
    navController: NavController,
    viewModel: ScholarViewModel,
    bottomPadding: PaddingValues = PaddingValues(0.dp),
    onEditCourse: (Course) -> Unit = {}
) {
    var courseToEdit by remember { mutableStateOf<Course?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    val courses by viewModel.courses.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        floatingActionButton = {
            val src = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            BouncyFloatingActionButton(
                onClick = { showAddDialog = true },
                interactionSource = src,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .padding(bottom = bottomPadding.calculateBottomPadding() + 80.dp)
                    .bouncyScale(src)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Course")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 700.dp)
                    .nestedScroll(remember { lumia.tracker.util.SystemBarScrollConnection(viewModel) }),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = 12.dp, bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Courses",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (courses.isEmpty()) {
                    item {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            androidx.compose.foundation.shape.CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No courses yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Tap + to create your first course",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(courses, key = { _, course -> course.id }) { index, course ->
                        Box(modifier = Modifier.animateItemEntry(index)) {
                            lumia.tracker.ui.screens.study.components.CourseItemCard(
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
    }

    if (showAddDialog) {
        AddCourseDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }

    courseToEdit?.let { course ->
        EditCourseDialog(
            course = course,
            viewModel = viewModel,
            onDismiss = { courseToEdit = null }
        )
    }
}
