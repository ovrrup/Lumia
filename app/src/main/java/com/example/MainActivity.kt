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
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.safeDrawing
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
            val betaNotchOptimization by viewModel.betaNotchOptimization.collectAsStateWithLifecycle()
            val betaImmersiveMode by viewModel.betaImmersiveMode.collectAsStateWithLifecycle()
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

            val customPrimary by viewModel.customPrimary.collectAsStateWithLifecycle()
            val customPrimaryContainer by viewModel.customPrimaryContainer.collectAsStateWithLifecycle()
            val customBackground by viewModel.customBackground.collectAsStateWithLifecycle()
            val customSurface by viewModel.customSurface.collectAsStateWithLifecycle()
            val customText by viewModel.customText.collectAsStateWithLifecycle()

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
                customPrimary = customPrimary,
                customPrimaryContainer = customPrimaryContainer,
                customBackground = customBackground,
                customSurface = customSurface,
                customText = customText
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize().then(
                        if (betaNotchOptimization && !betaImmersiveMode) Modifier.displayCutoutPadding() else Modifier
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
                            com.example.ui.screens.PomodoroScreen(navController = navController, viewModel = viewModel)
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
                    }
                    } // End of Box
                }
            }
        }
    }
}
