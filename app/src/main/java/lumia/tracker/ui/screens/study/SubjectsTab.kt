package lumia.tracker.ui.screens.study

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
import lumia.tracker.ui.screens.study.dialogs.EditSubjectDialog
import lumia.tracker.ui.theme.animateItemEntry
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * SubjectsTab - Professional Academic Subject Directory.
 * Displays subject curriculum coverage, syllabus progress, and linked course associations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsTab(
    navController: NavController,
    viewModel: ScholarViewModel,
    bottomPadding: PaddingValues,
    onEditSubject: (Subject) -> Unit = {},
    onAddSubjectClick: () -> Unit
) {
    var subjectToEdit by remember { mutableStateOf<Subject?>(null) }
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val allTopics by viewModel.allTopics.collectAsStateWithLifecycle(emptyList())

    // Header stats calculation
    val totalSubjects = subjects.size
    val totalTopics = allTopics.size
    val completedTopics = remember(allTopics) { allTopics.count { it.isCompleted } }
    val overallCoveragePct = if (totalTopics > 0) ((completedTopics.toFloat() / totalTopics) * 100).toInt() else 0

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Stats Row
            if (subjects.isNotEmpty()) {
                item(key = "subjects_header_stats") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total Subjects Card
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
                                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Category,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "$totalSubjects",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Subjects",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Syllabus Coverage Card
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
                                        imageVector = Icons.Rounded.PieChart,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (totalTopics > 0) "$overallCoveragePct%" else "$totalTopics Topics",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Coverage",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Subject List / Empty State
            if (subjects.isEmpty()) {
                item(key = "empty_subjects") {
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
                                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(38.dp),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "No subjects registered yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Organize your academic curriculum by subject and break chapters down into syllabus topics.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            BouncyButton(
                                onClick = onAddSubjectClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create First Subject", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
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
