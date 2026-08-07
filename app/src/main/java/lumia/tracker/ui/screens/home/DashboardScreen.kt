package lumia.tracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.components.header.InteractivePushPullHeader
import lumia.tracker.ui.components.navigation.FloatingCapsuleNavBar
import lumia.tracker.ui.components.navigation.NavTabItem
import lumia.tracker.viewmodel.ScholarViewModel

@Composable
fun DashboardScreen(navController: NavController, viewModel: ScholarViewModel) {
    val tabs = remember {
        listOf(
            NavTabItem(0, "Home", Icons.Rounded.Home),
            NavTabItem(1, "Courses", Icons.Rounded.Book),
            NavTabItem(2, "Subjects", Icons.Rounded.Category),
            NavTabItem(3, "Analytics", Icons.Rounded.Analytics)
        )
    }
    val selectedTabId by viewModel.selectedDashboardTab.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            InteractivePushPullHeader(
                title = tabs.find { it.id == selectedTabId }?.label ?: "Lumia",
                navController = navController,
                viewModel = viewModel
            )
        },
        bottomBar = {
            FloatingCapsuleNavBar(
                tabs = tabs,
                selectedTabId = selectedTabId,
                onTabSelected = { viewModel.setSelectedDashboardTab(it) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTabId) {
                0 -> HomeTab(navController = navController, viewModel = viewModel)
                1 -> CoursesTab(navController = navController, viewModel = viewModel)
                2 -> SubjectsTab(navController = navController, viewModel = viewModel)
                3 -> AnalyticsTab(navController = navController, viewModel = viewModel)
            }
        }
    }
}
