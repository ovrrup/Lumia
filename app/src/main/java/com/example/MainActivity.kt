package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ui.screens.CourseDetailScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubjectDetailScreen
import com.example.ui.theme.ScholarTheme
import com.example.viewmodel.ScholarViewModel
import com.example.worker.AssignmentMonitorWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val viewModel: ScholarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // WorkManager has been temporarily removed to prevent startup issues
        
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
            val pureBlackMode by viewModel.pureBlackMode.collectAsStateWithLifecycle()
            val betaNotchOptimization by viewModel.betaNotchOptimization.collectAsStateWithLifecycle()
            val betaImmersiveMode by viewModel.betaImmersiveMode.collectAsStateWithLifecycle()

            androidx.compose.runtime.LaunchedEffect(betaImmersiveMode) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val window = (this@MainActivity).window
                    window.attributes = window.attributes.apply {
                        layoutInDisplayCutoutMode = if (betaImmersiveMode) {
                            android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                        } else {
                            android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
                        }
                    }
                }
            }

            ScholarTheme(themeMode = themeMode, themeColor = themeColor, pureBlackMode = pureBlackMode) {
                Surface(
                    modifier = Modifier.fillMaxSize().then(
                        if (betaNotchOptimization && !betaImmersiveMode) Modifier.displayCutoutPadding() else Modifier
                    ), 
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController, 
                        startDestination = "dashboard",
                        modifier = Modifier.then(
                            if (betaImmersiveMode) Modifier.windowInsetsPadding(
                                androidx.compose.foundation.layout.WindowInsets.safeDrawing.only(
                                    androidx.compose.foundation.layout.WindowInsetsSides.Horizontal
                                )
                            ) else Modifier
                        ),
                        enterTransition = {
                            androidx.compose.animation.fadeIn(
                                animationSpec = androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.LinearOutSlowInEasing)
                            ) + androidx.compose.animation.scaleIn(
                                initialScale = 0.95f,
                                animationSpec = androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.LinearOutSlowInEasing)
                            )
                        },
                        exitTransition = {
                            androidx.compose.animation.fadeOut(
                                animationSpec = androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.FastOutLinearInEasing)
                            ) + androidx.compose.animation.scaleOut(
                                targetScale = 1.05f,
                                animationSpec = androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.FastOutLinearInEasing)
                            )
                        },
                        popEnterTransition = {
                            androidx.compose.animation.fadeIn(
                                animationSpec = androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.LinearOutSlowInEasing)
                            ) + androidx.compose.animation.scaleIn(
                                initialScale = 1.05f,
                                animationSpec = androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.LinearOutSlowInEasing)
                            )
                        },
                        popExitTransition = {
                            androidx.compose.animation.fadeOut(
                                animationSpec = androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.FastOutLinearInEasing)
                            ) + androidx.compose.animation.scaleOut(
                                targetScale = 0.95f,
                                animationSpec = androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.FastOutLinearInEasing)
                            )
                        }
                    ) {
                        composable("dashboard") {
                            DashboardScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("toolbox") {
                            com.example.ui.screens.ToolboxScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("pomodoro") {
                            com.example.ui.screens.PomodoroScreen(navController = navController)
                        }
                        composable("cgpa") {
                            com.example.ui.screens.CgpaCalculatorScreen(navController = navController)
                        }
                        composable("notes") {
                            com.example.ui.screens.QuickNotesScreen(navController = navController)
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
                        composable("settings") {
                            SettingsScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("settings/appearance") {
                            com.example.ui.screens.AppearanceScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("settings/beta") {
                            com.example.ui.screens.BetaFeaturesScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("settings/data") {
                            com.example.ui.screens.DataManagementScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
