package lumia.tracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.components.GlassCard
import lumia.tracker.viewmodel.ScholarViewModel

@Composable
fun AnalyticsTab(
    navController: NavController,
    viewModel: ScholarViewModel,
    bottomPadding: PaddingValues = PaddingValues(0.dp)
) {
    val courses by viewModel.courses.collectAsStateWithLifecycle(initialValue = emptyList())
    val subjects by viewModel.subjects.collectAsStateWithLifecycle(initialValue = emptyList())
    val assignments by viewModel.assignments.collectAsStateWithLifecycle(initialValue = emptyList())
    val sessions by viewModel.pomodoroSessions.collectAsStateWithLifecycle(initialValue = emptyList())

    val completedAssignments = remember(assignments) { assignments.count { it.isCompleted } }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 700.dp)
                .padding(bottom = bottomPadding.calculateBottomPadding()),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Analytics & Insights",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // High Level Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Courses",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${courses.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Subjects",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${subjects.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Assignments",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${assignments.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Assignments Status Donut Chart
            item {
                AssignmentsStatusDonutChart(
                    modifier = Modifier.fillMaxWidth(),
                    total = assignments.size,
                    completed = completedAssignments,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    primaryColor = MaterialTheme.colorScheme.primary,
                    secondaryColor = MaterialTheme.colorScheme.secondary
                )
            }

            // Focus Time Per Course Chart
            item {
                FocusTimePerCourseChart(
                    modifier = Modifier.fillMaxWidth(),
                    sessions = sessions,
                    courses = courses
                )
            }

            // Assignments Per Course Bar Chart
            item {
                AssignmentsPerCourseBarChart(
                    modifier = Modifier.fillMaxWidth(),
                    courses = courses,
                    assignments = assignments,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    barColor = MaterialTheme.colorScheme.primary
                )
            }

            // Assignment Category Distribution
            item {
                AssignmentCategoryDistributionChart(
                    modifier = Modifier.fillMaxWidth(),
                    assignments = assignments
                )
            }

            // Pomodoro Study Heatmap
            item {
                PomodoroHeatmapChart(
                    modifier = Modifier.fillMaxWidth(),
                    sessions = sessions,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    primaryColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
