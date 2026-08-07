package lumia.tracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.screens.home.components.DashboardStatusCard
import lumia.tracker.ui.screens.home.components.HomeAssignmentsList
import lumia.tracker.ui.screens.home.components.QuickActionHub
import lumia.tracker.viewmodel.ScholarViewModel

@Composable
fun HomeTab(navController: NavController, viewModel: ScholarViewModel) {
    val assignments by viewModel.assignments.collectAsStateWithLifecycle(initialValue = emptyList())
    val courses by viewModel.courses.collectAsStateWithLifecycle(initialValue = emptyList())
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val streakDays by viewModel.streakCurrent.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 700.dp)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // New High-Fidelity Status Card (Date + Week Grid Streak dots)
            item {
                DashboardStatusCard(
                    userName = activeProfile?.name ?: "Student",
                    streakDays = streakDays,
                    viewModel = viewModel,
                    onStartFocus = { navController.navigate("pomodoro") }
                )
            }

            // Beautiful Course Status & Automated Goal Progress (Ref 2 Flat Progress Style)
            item {
                QuickActionHub(
                    courses = courses,
                    assignments = assignments,
                    onCourseClick = { courseId ->
                        navController.navigate("courseDetail/$courseId")
                    }
                )
            }

            // Clean, One-Tap Assignment Quick Check-off (minimizes upkeep actions)
            item {
                HomeAssignmentsList(
                    assignments = assignments,
                    onToggleComplete = { assignment ->
                        viewModel.updateAssignment(assignment.copy(isCompleted = !assignment.isCompleted))
                    },
                    onNavigateToCourse = { courseId ->
                        navController.navigate("courseDetail/$courseId")
                    },
                    onViewAll = { viewModel.setSelectedDashboardTab(1) }
                )
            }
        }
    }
}
