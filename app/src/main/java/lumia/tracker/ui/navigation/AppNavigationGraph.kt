package lumia.tracker.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import lumia.tracker.ui.screens.*
import lumia.tracker.ui.screens.search.SearchScreen
import lumia.tracker.ui.screens.settings.*
import lumia.tracker.ui.screens.study.*
import lumia.tracker.ui.screens.sync.MultiDeviceSyncScreen
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * AppNavigationGraph - The central navigation controller for Lumia.
 *
 * Configures all navigation destinations, route arguments, and adaptive screen transition
 * animations based on the user's selected animation preference (Bouncy, Dynamic, or Default).
 */
@Composable
fun AppNavigationGraph(
    navController: NavHostController,
    viewModel: ScholarViewModel,
    isOnboardingCompleted: Boolean,
    displayLayoutMode: String,
    appAnimationMode: String,
    startupState: MutableState<String>
) {
    NavHost(
        navController = navController,
        startDestination = if (isOnboardingCompleted) "dashboard" else "onboarding",
        // Forward screen entry transition
        enterTransition = {
            val spec: FiniteAnimationSpec<Float> = when (appAnimationMode) {
                "Bouncy" -> spring(dampingRatio = 0.8f, stiffness = 380f)
                "Dynamic" -> spring(dampingRatio = 0.9f, stiffness = 450f)
                else -> tween(250, easing = FastOutSlowInEasing)
            }
            fadeIn(animationSpec = tween(250)) +
                    scaleIn(initialScale = if (appAnimationMode == "Bouncy") 0.94f else 0.97f, animationSpec = spec)
        },
        // Forward screen exit transition
        exitTransition = {
            val spec: FiniteAnimationSpec<Float> = when (appAnimationMode) {
                "Bouncy" -> spring(dampingRatio = 0.8f, stiffness = 380f)
                "Dynamic" -> spring(dampingRatio = 0.9f, stiffness = 450f)
                else -> tween(250, easing = FastOutSlowInEasing)
            }
            fadeOut(animationSpec = tween(250)) +
                    scaleOut(targetScale = if (appAnimationMode == "Bouncy") 1.04f else 1.02f, animationSpec = spec)
        },
        // Pop/backstack return entry transition
        popEnterTransition = {
            val spec: FiniteAnimationSpec<Float> = when (appAnimationMode) {
                "Bouncy" -> spring(dampingRatio = 0.8f, stiffness = 380f)
                "Dynamic" -> spring(dampingRatio = 0.9f, stiffness = 450f)
                else -> tween(250, easing = FastOutSlowInEasing)
            }
            fadeIn(animationSpec = tween(250)) +
                    scaleIn(initialScale = if (appAnimationMode == "Bouncy") 0.94f else 0.97f, animationSpec = spec)
        },
        // Pop/backstack return exit transition
        popExitTransition = {
            val spec: FiniteAnimationSpec<Float> = when (appAnimationMode) {
                "Bouncy" -> spring(dampingRatio = 0.8f, stiffness = 380f)
                "Dynamic" -> spring(dampingRatio = 0.9f, stiffness = 450f)
                else -> tween(250, easing = FastOutSlowInEasing)
            }
            fadeOut(animationSpec = tween(250)) +
                    scaleOut(targetScale = if (appAnimationMode == "Bouncy") 1.04f else 1.02f, animationSpec = spec)
        }
    ) {
        // Welcome and First-Time Onboarding Flow
        composable("onboarding") {
            OnboardingScreen(navController = navController, viewModel = viewModel)
        }

        // Primary App Dashboard with Tab Navigation (Home, Courses, Subjects, Tasks, Analytics)
        composable("dashboard") {
            DashboardScreen(navController = navController, viewModel = viewModel)
        }

        // Global Instant Search
        composable("search") {
            SearchScreen(navController = navController, viewModel = viewModel)
        }

        // Tags Organization Hub
        composable(
            "tags_hub?selectedTag={selectedTag}",
            arguments = listOf(navArgument("selectedTag") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            val selectedTag = backStackEntry.arguments?.getString("selectedTag") ?: ""
            TagsHubScreen(
                navController = navController,
                viewModel = viewModel,
                initialTag = selectedTag
            )
        }

        // Pomodoro Focus Timer Session
        composable(
            "pomodoro?subjectId={subjectId}&courseId={courseId}&assignmentId={assignmentId}&taskId={taskId}&topicId={topicId}",
            arguments = listOf(
                navArgument("subjectId") { type = NavType.StringType; defaultValue = "" },
                navArgument("courseId") { type = NavType.StringType; defaultValue = "" },
                navArgument("assignmentId") { type = NavType.StringType; defaultValue = "" },
                navArgument("taskId") { type = NavType.StringType; defaultValue = "" },
                navArgument("topicId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val sId = backStackEntry.arguments?.getString("subjectId")?.toIntOrNull()
            val cId = backStackEntry.arguments?.getString("courseId")?.toIntOrNull()
            val aId = backStackEntry.arguments?.getString("assignmentId")?.toIntOrNull()
            val tId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull()
            val topId = backStackEntry.arguments?.getString("topicId")?.toIntOrNull()
            PomodoroScreen(
                navController = navController,
                viewModel = viewModel,
                initialSubjectId = sId,
                initialCourseId = cId,
                initialAssignmentId = aId,
                initialTaskId = tId,
                initialTopicId = topId
            )
        }

        // Quick Notes Scratchpad
        composable("notes") {
            QuickNotesScreen(navController = navController)
        }

        // Course Detail Screen with Chapters, Topics, and Assignments
        composable(
            "courseDetail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            CourseDetailScreen(navController = navController, viewModel = viewModel, courseId = id)
        }

        // Subject Detail Screen with Linked Courses and Analytics
        composable(
            "subjectDetail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            SubjectDetailScreen(navController = navController, viewModel = viewModel, subjectId = id)
        }

        // Settings Hub & Categorized Configuration Screens
        composable("settings") {
            SettingsScreen(navController = navController, viewModel = viewModel)
        }

        composable("settings/streaks") {
            StreakSettingsScreen(navController = navController, viewModel = viewModel)
        }

        composable("settings/appearance") {
            AppearanceScreen(navController = navController, viewModel = viewModel)
        }

        composable("settings/advanced_theme") {
            AdvancedThemeScreen(navController = navController, viewModel = viewModel)
        }

        composable("settings/beta") {
            BetaFeaturesScreen(navController = navController, viewModel = viewModel)
        }

        composable("settings/safety") {
            SafetyFeaturesScreen(navController = navController, viewModel = viewModel)
        }

        composable("settings/data") {
            DataManagementScreen(navController = navController, viewModel = viewModel)
        }

        composable("settings/system") {
            SystemSettingsScreen(navController = navController, viewModel = viewModel)
        }

        composable("settings/notifications") {
            NotificationsScreen(navController = navController, viewModel = viewModel)
        }

        composable("settings/about") {
            AboutAppScreen(navController = navController, viewModel = viewModel)
        }

        // Multi-Device P2P / WebRTC Synchronization Hub
        composable("settings/sync") {
            MultiDeviceSyncScreen(navController = navController, viewModel = viewModel)
        }

        composable("p2p_sync") {
            MultiDeviceSyncScreen(navController = navController, viewModel = viewModel)
        }

        // In-App Document & Syllabus PDF Viewer
        composable("pdf_viewer?filePath={filePath}&fileName={fileName}") { backStackEntry ->
            val filePath = backStackEntry.arguments?.getString("filePath")
            val fileName = backStackEntry.arguments?.getString("fileName")
            PdfViewerScreen(
                navController = navController,
                filePath = filePath,
                fileName = fileName
            )
        }

        // User Profile & Account Settings
        composable("profile_menu") {
            SettingsScreen(navController = navController, viewModel = viewModel)
        }

        // Fast Profile Switch Trigger
        composable("switch_user") {
            startupState.value = "selector"
            LaunchedEffect(Unit) {
                navController.navigateUp()
            }
        }
    }
}
