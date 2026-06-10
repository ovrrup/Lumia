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
        
        // Request Notification Permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
        
        // Schedule periodic worker
        val workRequest = PeriodicWorkRequestBuilder<AssignmentMonitorWorker>(4, TimeUnit.HOURS).build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "AssignmentMonitor",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()

            ScholarTheme(themeMode = themeMode, themeColor = themeColor) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController, 
                        startDestination = "dashboard",
                        enterTransition = {
                            androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) +
                                androidx.compose.animation.slideInHorizontally(
                                    initialOffsetX = { fullWidth -> fullWidth / 8 },
                                    animationSpec = androidx.compose.animation.core.tween(300)
                                )
                        },
                        exitTransition = {
                            androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) +
                                androidx.compose.animation.slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> -fullWidth / 8 },
                                    animationSpec = androidx.compose.animation.core.tween(300)
                                )
                        },
                        popEnterTransition = {
                            androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) +
                                androidx.compose.animation.slideInHorizontally(
                                    initialOffsetX = { fullWidth -> -fullWidth / 8 },
                                    animationSpec = androidx.compose.animation.core.tween(300)
                                )
                        },
                        popExitTransition = {
                            androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) +
                                androidx.compose.animation.slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> fullWidth / 8 },
                                    animationSpec = androidx.compose.animation.core.tween(300)
                                )
                        }
                    ) {
                        composable("dashboard") {
                            DashboardScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
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
                    }
                }
            }
        }
    }
}
