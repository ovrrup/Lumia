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
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Info

class MainActivity : ComponentActivity() {
    private val viewModel: ScholarViewModel by viewModels()

    private val _crashData = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle crash restarts
        val crashData = intent.getStringExtra("FATAL_CRASH_DATA")
        if (crashData != null) {
            _crashData.value = crashData
            intent.removeExtra("FATAL_CRASH_DATA")
        }

        com.example.util.LogDog.setup(applicationContext)
        
        val pomodoroReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
                if (intent?.action == "PomodoroLogSession") {
                    val wasWork = intent.getBooleanExtra("isWork", true)
                    val originalTime = intent.getIntExtra("originalTime", 25 * 60)
                    val sId = if (intent.hasExtra("subjectId")) intent.getIntExtra("subjectId", -1).takeIf { it != -1 } else null
                    val cId = if (intent.hasExtra("courseId")) intent.getIntExtra("courseId", -1).takeIf { it != -1 } else null
                    val aId = if (intent.hasExtra("assignmentId")) intent.getIntExtra("assignmentId", -1).takeIf { it != -1 } else null
                    val tId = if (intent.hasExtra("taskId")) intent.getIntExtra("taskId", -1).takeIf { it != -1 } else null

                    if (wasWork && viewModel.systemPomodoroAutoLog.value) {
                        viewModel.addPomodoroSession(originalTime / 60, sId, cId, aId, tId)
                    }
                }
            }
        }
        val filterOptions = android.content.IntentFilter("PomodoroLogSession")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(pomodoroReceiver, filterOptions, android.content.Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(pomodoroReceiver, filterOptions)
        }
        
        try {
            val workRequest = PeriodicWorkRequestBuilder<AssignmentMonitorWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(1, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "assignment_monitor",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "WorkManager failed to start", e)
        }
        
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
            val pureBlackMode by viewModel.pureBlackMode.collectAsStateWithLifecycle()
            val displayLayoutMode by viewModel.displayLayoutMode.collectAsStateWithLifecycle()
            val betaGlassUi by viewModel.betaGlassUi.collectAsStateWithLifecycle()
            val betaGlassDynamic by viewModel.betaGlassDynamic.collectAsStateWithLifecycle()
            val betaFrostGlass by viewModel.betaFrostGlass.collectAsStateWithLifecycle()
            val betaDynamicBackground by viewModel.betaDynamicBackground.collectAsStateWithLifecycle()
            val dynamicBgLightBrightness by viewModel.dynamicBgLightBrightness.collectAsStateWithLifecycle()
            val dynamicBgDarkBrightness by viewModel.dynamicBgDarkBrightness.collectAsStateWithLifecycle()
            val betaBetterTexts by viewModel.betaBetterTexts.collectAsStateWithLifecycle()
            val betaBetterTextsPalette by viewModel.betaBetterTextsPalette.collectAsStateWithLifecycle()
            val glassBackdropStyle by viewModel.glassBackdropStyle.collectAsStateWithLifecycle()
            val glassOpacityValue by viewModel.glassOpacityValue.collectAsStateWithLifecycle()
            val appAnimationMode by viewModel.appAnimationMode.collectAsStateWithLifecycle()
            val moreRounds by viewModel.moreRounds.collectAsStateWithLifecycle()

            val customPrimary by viewModel.customPrimary.collectAsStateWithLifecycle()
            val customPrimaryContainer by viewModel.customPrimaryContainer.collectAsStateWithLifecycle()
            val customBackground by viewModel.customBackground.collectAsStateWithLifecycle()
            val customSurface by viewModel.customSurface.collectAsStateWithLifecycle()
            val customText by viewModel.customText.collectAsStateWithLifecycle()

            androidx.compose.runtime.LaunchedEffect(displayLayoutMode) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val window = (this@MainActivity).window
                    window.attributes = window.attributes.apply {
                        layoutInDisplayCutoutMode = if (displayLayoutMode == "Immersive") {
                            android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                        } else {
                            android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
                        }
                    }
                }
            }

            ScholarTheme(
                themeMode = themeMode,
                themeColor = themeColor,
                pureBlackMode = pureBlackMode,
                glassMode = betaGlassUi,
                glassDynamic = betaGlassDynamic,
                frostGlass = betaFrostGlass,
                glassBackdropStyle = glassBackdropStyle,
                glassOpacityValue = glassOpacityValue,
                betterTexts = betaBetterTexts,
                betterTextsPalette = betaBetterTextsPalette,
                appAnimationMode = appAnimationMode,
                moreRounds = moreRounds,
                customPrimary = customPrimary,
                customPrimaryContainer = customPrimaryContainer,
                customBackground = customBackground,
                customSurface = customSurface,
                customText = customText
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize().then(
                        if (displayLayoutMode == "Notch Optimization") Modifier.displayCutoutPadding() else Modifier
                    ), 
                    color = MaterialTheme.colorScheme.background
                ) {
                    val safetyPinDialogData by viewModel.safetyPinDialogData.collectAsStateWithLifecycle()
                    if (safetyPinDialogData != null) {
                        val data = safetyPinDialogData!!
                        androidx.compose.material3.AlertDialog(
                            icon = {
                                androidx.compose.material3.Icon(
                                    imageVector = if (data.isConflict) Icons.Rounded.Warning else Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = if (data.isConflict) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            },
                            title = { androidx.compose.material3.Text(data.title) },
                            text = { androidx.compose.material3.Text(data.description) },
                            onDismissRequest = { data.onIgnore() },
                            confirmButton = {
                                androidx.compose.material3.TextButton(onClick = data.onConfirm) {
                                    androidx.compose.material3.Text(if (data.isConflict) "Continue" else "Apply")
                                }
                            },
                            dismissButton = {
                                androidx.compose.material3.TextButton(onClick = data.onIgnore) {
                                    androidx.compose.material3.Text(if (data.isConflict) "Stop" else "Ignore")
                                }
                            }
                        )
                    }

                    val navController = rememberNavController()

                    // Full-screen ambient gradient — sits behind ALL composables
                    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                        if (betaDynamicBackground || betaGlassUi) {
                            // Animated or static soft ambient background
                            val infiniteTransition = rememberInfiniteTransition()
                            val offset by if (betaDynamicBackground) {
                                infiniteTransition.animateFloat(
                                    initialValue = 0f, targetValue = 1f,
                                    animationSpec = infiniteRepeatable(tween(12000), RepeatMode.Reverse),
                                    label = "bg_anim"
                                )
                            } else {
                                remember { androidx.compose.runtime.mutableStateOf(0.5f) }
                            }
                            val colorScheme = MaterialTheme.colorScheme
                            val isDark = androidx.compose.foundation.isSystemInDarkTheme() || colorScheme.background.red < 0.5f
                            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                val colors = listOf(
                                    colorScheme.primaryContainer,
                                    colorScheme.secondaryContainer,
                                    colorScheme.tertiaryContainer
                                )
                                val alphaScale = if (isDark) dynamicBgDarkBrightness else dynamicBgLightBrightness
                                drawRect(
                                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                        colors = listOf(colors[0].copy(alpha = alphaScale.coerceIn(0f, 1f) * 1.0f), colors[0].copy(alpha = alphaScale.coerceIn(0f, 1f) * 0.25f), androidx.compose.ui.graphics.Color.Transparent),
                                        center = androidx.compose.ui.geometry.Offset(size.width * 0.25f, size.height * (0.18f + offset * 0.12f)),
                                        radius = size.width * 1.6f
                                    )
                                )
                                drawRect(
                                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                        colors = listOf(colors[2].copy(alpha = alphaScale.coerceIn(0f, 1f) * 0.85f), colors[2].copy(alpha = alphaScale.coerceIn(0f, 1f) * 0.15f), androidx.compose.ui.graphics.Color.Transparent),
                                        center = androidx.compose.ui.geometry.Offset(size.width * 0.82f, size.height * (0.65f - offset * 0.08f)),
                                        radius = size.width * 1.5f
                                    )
                                )
                                drawRect(
                                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                        colors = listOf(colors[1].copy(alpha = alphaScale.coerceIn(0f, 1f) * 0.65f), androidx.compose.ui.graphics.Color.Transparent),
                                        center = androidx.compose.ui.geometry.Offset(size.width * 0.90f, size.height * 0.30f),
                                        radius = size.width * 1.3f
                                    )
                                )
                            }
                        }

                        NavHost(
                            navController = navController, 
                            startDestination = "dashboard",
                            modifier = Modifier.then(
                                if (displayLayoutMode == "Immersive") Modifier.windowInsetsPadding(
                                    androidx.compose.foundation.layout.WindowInsets.safeDrawing.only(
                                        androidx.compose.foundation.layout.WindowInsetsSides.Horizontal
                                    )
                                ) else Modifier
                            ),
                        enterTransition = {
                            val spec = if (appAnimationMode == "Bouncy") {
                                androidx.compose.animation.core.spring<Float>(dampingRatio = 0.45f, stiffness = 200f)
                            } else if (appAnimationMode == "Dynamic") {
                                androidx.compose.animation.core.spring<Float>(dampingRatio = 0.75f, stiffness = 500f)
                            } else {
                                androidx.compose.animation.core.tween<Float>(300, easing = androidx.compose.animation.core.LinearOutSlowInEasing)
                            }
                            androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) +
                            androidx.compose.animation.scaleIn(initialScale = if (appAnimationMode == "Bouncy") 0.8f else 0.95f, animationSpec = spec)
                        },
                        exitTransition = {
                            val spec = if (appAnimationMode == "Bouncy") {
                                androidx.compose.animation.core.spring<Float>(dampingRatio = 0.45f, stiffness = 200f)
                            } else if (appAnimationMode == "Dynamic") {
                                androidx.compose.animation.core.spring<Float>(dampingRatio = 0.75f, stiffness = 500f)
                            } else {
                                androidx.compose.animation.core.tween<Float>(300, easing = androidx.compose.animation.core.FastOutLinearInEasing)
                            }
                            androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) +
                            androidx.compose.animation.scaleOut(targetScale = if (appAnimationMode == "Bouncy") 1.2f else 1.05f, animationSpec = spec)
                        },
                        popEnterTransition = {
                            val spec = if (appAnimationMode == "Bouncy") {
                                androidx.compose.animation.core.spring<Float>(dampingRatio = 0.45f, stiffness = 200f)
                            } else if (appAnimationMode == "Dynamic") {
                                androidx.compose.animation.core.spring<Float>(dampingRatio = 0.75f, stiffness = 500f)
                            } else {
                                androidx.compose.animation.core.tween<Float>(300, easing = androidx.compose.animation.core.LinearOutSlowInEasing)
                            }
                            androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) +
                            androidx.compose.animation.scaleIn(initialScale = if (appAnimationMode == "Bouncy") 1.2f else 1.05f, animationSpec = spec)
                        },
                        popExitTransition = {
                            val spec = if (appAnimationMode == "Bouncy") {
                                androidx.compose.animation.core.spring<Float>(dampingRatio = 0.45f, stiffness = 200f)
                            } else if (appAnimationMode == "Dynamic") {
                                androidx.compose.animation.core.spring<Float>(dampingRatio = 0.75f, stiffness = 500f)
                            } else {
                                androidx.compose.animation.core.tween<Float>(300, easing = androidx.compose.animation.core.FastOutLinearInEasing)
                            }
                            androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) +
                            androidx.compose.animation.scaleOut(targetScale = if (appAnimationMode == "Bouncy") 0.8f else 0.95f, animationSpec = spec)
                        }
                    ) {
                        composable("dashboard") {
                            DashboardScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("pomodoro") {
                            com.example.ui.screens.PomodoroScreen(navController = navController, viewModel = viewModel)
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
                        composable("settings/advanced_theme") {
                            com.example.ui.screens.AdvancedThemeScreen(
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
                        composable("settings/safety") {
                            com.example.ui.screens.SafetyFeaturesScreen(
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
                        composable("settings/system") {
                            com.example.ui.screens.SystemSettingsScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("settings/notifications") {
                            com.example.ui.screens.NotificationsScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                    }
                    
                    val crashDataState by _crashData.collectAsStateWithLifecycle()
                    if (crashDataState != null) {
                        val parsed = remember(crashDataState) { com.example.util.LogDog.analyzeCrash(crashDataState ?: "") }
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { _crashData.value = null },
                            title = { 
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    androidx.compose.material3.Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Rounded.Warning, 
                                        contentDescription = "LogDog",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("LogDog Crash Recovery", color = MaterialTheme.colorScheme.onSurface)
                                }
                            },
                            text = {
                                Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                                    Text("Ruff rough... I caught a crash before it took down the whole app completely! Here is my analysis:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 12.dp))
                                    Text(parsed.suggestion, style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.height(16.dp))
                                    Text("Location: ${parsed.crashLocation}", style = MaterialTheme.typography.bodySmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                                    Text("Error: ${parsed.exceptionType}", style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            confirmButton = {
                                androidx.compose.material3.Button(onClick = { _crashData.value = null }) { Text("Good Boy") }
                            },
                            dismissButton = {
                                androidx.compose.material3.TextButton(onClick = {
                                    _crashData.value = null
                                    navController.navigate("settings/beta") // or wherever logs are shown
                                }) { Text("View Full Logs") }
                            }
                        )
                    }

                    } // End of Box
                }
            }
        }
    }
}
