package lumia.tracker.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import lumia.tracker.ui.screens.*
import lumia.tracker.viewmodel.ScholarViewModel

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
        modifier = Modifier.then(
            if (displayLayoutMode == "Immersive") Modifier.windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
            ) else Modifier
        ),
        enterTransition = {
            if (appAnimationMode == "iOS" || appAnimationMode == "Dynamic") {
                slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }, animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(animationSpec = tween(300))
            } else {
                val spec = if (appAnimationMode == "Bouncy") spring<Float>(dampingRatio = 0.45f, stiffness = 200f) else tween<Float>(300, easing = LinearOutSlowInEasing)
                fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = if (appAnimationMode == "Bouncy") 0.8f else 0.95f, animationSpec = spec)
            }
        },
        exitTransition = {
            if (appAnimationMode == "iOS" || appAnimationMode == "Dynamic") {
                slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth / 3 }, animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeOut(animationSpec = tween(300))
            } else {
                val spec = if (appAnimationMode == "Bouncy") spring<Float>(dampingRatio = 0.45f, stiffness = 200f) else tween<Float>(300, easing = FastOutLinearInEasing)
                fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = if (appAnimationMode == "Bouncy") 1.2f else 1.05f, animationSpec = spec)
            }
        },
        popEnterTransition = {
            if (appAnimationMode == "iOS" || appAnimationMode == "Dynamic") {
                slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth / 3 }, animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(animationSpec = tween(300))
            } else {
                val spec = if (appAnimationMode == "Bouncy") spring<Float>(dampingRatio = 0.45f, stiffness = 200f) else tween<Float>(300, easing = LinearOutSlowInEasing)
                fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = if (appAnimationMode == "Bouncy") 1.2f else 1.05f, animationSpec = spec)
            }
        },
        popExitTransition = {
            if (appAnimationMode == "iOS" || appAnimationMode == "Dynamic") {
                slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }, animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeOut(animationSpec = tween(300))
            } else {
                val spec = if (appAnimationMode == "Bouncy") spring<Float>(dampingRatio = 0.45f, stiffness = 200f) else tween<Float>(300, easing = FastOutLinearInEasing)
                fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = if (appAnimationMode == "Bouncy") 0.8f else 0.95f, animationSpec = spec)
            }
        }
    ) {
        composable("onboarding") { OnboardingScreen(navController = navController, viewModel = viewModel) }
        composable("dashboard") { DashboardScreen(navController = navController, viewModel = viewModel) }
        composable("search") { SearchScreen(navController = navController, viewModel = viewModel) }
        composable(
            "tags_hub?selectedTag={selectedTag}",
            arguments = listOf(navArgument("selectedTag") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            val selectedTag = backStackEntry.arguments?.getString("selectedTag") ?: ""
            TagsHubScreen(navController = navController, viewModel = viewModel, initialTag = selectedTag)
        }
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
            PomodoroScreen(navController = navController, viewModel = viewModel, initialSubjectId = sId, initialCourseId = cId, initialAssignmentId = aId, initialTaskId = tId, initialTopicId = topId)
        }
        composable(
            "courseDetail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            CourseDetailScreen(navController = navController, viewModel = viewModel, courseId = id)
        }
        composable(
            "subjectDetail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            SubjectDetailScreen(navController = navController, viewModel = viewModel, subjectId = id)
        }
        composable("settings") { SettingsScreen(navController = navController, viewModel = viewModel) }
        composable("settings/streaks") { StreakSettingsScreen(navController = navController, viewModel = viewModel) }
        composable("settings/appearance") { AppearanceScreen(navController = navController, viewModel = viewModel) }
        composable("settings/advanced_theme") { AdvancedThemeScreen(navController = navController, viewModel = viewModel) }
        composable("settings/data") { DataManagementScreen(navController = navController, viewModel = viewModel) }
        composable("settings/system") { SystemSettingsScreen(navController = navController, viewModel = viewModel) }
        composable("settings/notifications") { NotificationsScreen(navController = navController, viewModel = viewModel) }
        composable("settings/about") { AboutAppScreen(navController = navController, viewModel = viewModel) }
        composable("pdf_viewer?filePath={filePath}&fileName={fileName}") { backStackEntry ->
            val filePath = backStackEntry.arguments?.getString("filePath")
            val fileName = backStackEntry.arguments?.getString("fileName")
            PdfViewerScreen(navController = navController, filePath = filePath, fileName = fileName)
        }
        composable("profile_menu") { ProfileMenuScreen(navController = navController, viewModel = viewModel) }
        composable("switch_user") {
            startupState.value = "selector"
            androidx.compose.runtime.LaunchedEffect(Unit) { navController.navigateUp() }
        }
    }
}
