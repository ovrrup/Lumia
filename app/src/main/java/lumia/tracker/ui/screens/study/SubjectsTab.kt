package lumia.tracker.ui.screens.study

import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore

import lumia.tracker.ui.screens.study.dialogs.*

import androidx.compose.foundation.background
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
import lumia.tracker.model.Subject
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyFloatingActionButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.screens.study.components.SubjectItemCard
import lumia.tracker.ui.screens.study.components.StudyHeaderStatCard
import lumia.tracker.ui.screens.study.components.StudyEmptyStateCard
import lumia.tracker.ui.screens.study.dialogs.EditSubjectDialog
import lumia.tracker.ui.theme.animateItemEntry
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * SubjectsTab - Professional Academic Subject Directory.
 * Displays subject curriculum coverage, syllabus progress, and linked course associations.
 */
@ValueScore(
    score = 86,
    importance = Importance.HIGH,
    description = "Subjects directory dashboard with syllabus coverage progress, chapter breakdowns, and course linkages",
    category = "Study"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsTab(
    navController: NavController,
    viewModel: ScholarViewModel,
    bottomPadding: PaddingValues,
    onEditSubject: (Subject) -> Unit = {},
    onAddSubjectClick: () -> Unit = {}
) {
    var subjectToEdit by remember { mutableStateOf<Subject?>(null) }
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val allTopics by viewModel.allTopics.collectAsStateWithLifecycle(emptyList())

    // Header stats calculation
    val totalSubjects = subjects.size
    val totalTopics = allTopics.size
    val completedTopics = remember(allTopics) { allTopics.count { it.isCompleted } }
    val overallCoveragePct = remember(totalTopics, completedTopics) {
        if (totalTopics > 0) ((completedTopics.toFloat() / totalTopics) * 100).toInt() else null
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            BouncyFloatingActionButton(
                onClick = onAddSubjectClick,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(bottom = bottomPadding.calculateBottomPadding())
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Subject")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Stats Row
            if (subjects.isNotEmpty()) {
                item(key = "subjects_header_stats") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total Subjects Card
                        StudyHeaderStatCard(
                            value = "$totalSubjects",
                            label = "Subjects",
                            icon = Icons.Rounded.Category,
                            iconColor = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )

                        // Syllabus Coverage Card
                        StudyHeaderStatCard(
                            value = if (overallCoveragePct != null) "$overallCoveragePct%" else "0%",
                            label = "Coverage",
                            icon = Icons.Rounded.PieChart,
                            iconColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Subject List / Empty State
            if (subjects.isEmpty()) {
                item(key = "empty_subjects") {
                    StudyEmptyStateCard(
                        icon = Icons.AutoMirrored.Rounded.MenuBook,
                        title = "No subjects registered yet",
                        description = "Organize your academic curriculum and track syllabus topics by subject.",
                        buttonText = "Create First Subject",
                        onButtonClick = onAddSubjectClick,
                        modifier = Modifier.padding(top = 16.dp),
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        buttonContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        buttonContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            } else {
                itemsIndexed(subjects, key = { _, subject -> subject.id }) { index, subject ->
                    Box(modifier = Modifier.animateItemEntry(index)) {
                        SubjectItemCard(
                            subject = subject,
                            onClick = {
                                navController.navigate("subjectDetail/${subject.id}") {
                                    launchSingleTop = true
                                }
                            },
                            onEdit = { subjectToEdit = subject },
                            viewModel = viewModel,
                            onCourseClick = { courseId ->
                                navController.navigate("courseDetail/$courseId") {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (subjectToEdit != null) {
        EditSubjectDialog(
            subject = subjectToEdit!!,
            viewModel = viewModel,
            onDismiss = { subjectToEdit = null }
        )
    }
}
