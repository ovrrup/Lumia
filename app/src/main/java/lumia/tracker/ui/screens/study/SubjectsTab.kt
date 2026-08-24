package lumia.tracker.ui.screens.study

import androidx.compose.foundation.background
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
import lumia.tracker.model.Subject
import lumia.tracker.ui.components.BouncyFloatingActionButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.screens.study.components.SubjectItemCard
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
                top = bottomPadding.calculateTopPadding() + 8.dp,
                bottom = bottomPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (subjects.isEmpty()) {
                item {
                    ScholarCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No subjects registered yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Add your academic subjects and topics to organize study chapters.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
