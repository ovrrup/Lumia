package lumia.tracker

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
import lumia.tracker.ui.screens.CourseDetailScreen
import lumia.tracker.ui.screens.DashboardScreen
import lumia.tracker.ui.screens.SettingsScreen
import lumia.tracker.ui.theme.ScholarTheme
import lumia.tracker.viewmodel.ScholarViewModel
import lumia.tracker.worker.AssignmentMonitorWorker
import java.util.concurrent.TimeUnit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Close

class MainActivity : ComponentActivity() {
    private val viewModel: ScholarViewModel by viewModels()

    private val _crashData = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle crash restarts
        val crashData = intent.getStringExtra("FATAL_CRASH_DATA")
        if (crashData != null) {
            _crashData.value = crashData
            intent.removeExtra("FATAL_CRASH_DATA")
        }

        lumia.tracker.util.LogDog.setup(applicationContext)
        
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
            val moreRoundsMode by viewModel.moreRoundsMode.collectAsStateWithLifecycle()

            val customPrimary by viewModel.customPrimary.collectAsStateWithLifecycle()
            val customPrimaryContainer by viewModel.customPrimaryContainer.collectAsStateWithLifecycle()
            val customBackground by viewModel.customBackground.collectAsStateWithLifecycle()
            val customSurface by viewModel.customSurface.collectAsStateWithLifecycle()
            val customText by viewModel.customText.collectAsStateWithLifecycle()
            val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()

            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val effectiveDark = themeMode == "Dark" || (themeMode == "System" && isSystemDark)

            val navBarGlassOpacityValue by viewModel.navBarGlassOpacityValue.collectAsStateWithLifecycle()
            val navBarGlassLinkedToMain by viewModel.navBarGlassLinkedToMain.collectAsStateWithLifecycle()
            val navBarGlassBackdropStyle by viewModel.navBarGlassBackdropStyle.collectAsStateWithLifecycle()
            val navBarGlassDynamic by viewModel.navBarGlassDynamic.collectAsStateWithLifecycle()
            androidx.compose.runtime.LaunchedEffect(effectiveDark, themeColor) {
                viewModel.refreshNavBarGlassOpacity(themeColor, effectiveDark)
            }

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

            val startupState = remember { androidx.compose.runtime.mutableStateOf("splash") }
            val actForStartup = androidx.activity.compose.LocalActivity.current as? MainActivity
            androidx.compose.runtime.LaunchedEffect(actForStartup?.intent) {
                if (actForStartup?.intent?.getBooleanExtra("OPEN_PROFILE_SELECTOR", false) == true) {
                    startupState.value = "selector"
                    actForStartup.intent.removeExtra("OPEN_PROFILE_SELECTOR")
                }
            }
            val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
            val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()

            ScholarTheme(
                themeMode = themeMode,
                themeColor = themeColor,
                pureBlackMode = pureBlackMode,
                glassMode = betaGlassUi,
                glassDynamic = betaGlassDynamic,
                frostGlass = betaFrostGlass,
                glassBackdropStyle = glassBackdropStyle,
                glassOpacityValue = glassOpacityValue,
                navBarGlassOpacityValue = navBarGlassOpacityValue,
                navBarGlassLinkedToMain = navBarGlassLinkedToMain,
                navBarGlassBackdropStyle = navBarGlassBackdropStyle,
                navBarGlassDynamic = navBarGlassDynamic,
                betterTexts = betaBetterTexts,
                betterTextsPalette = betaBetterTextsPalette,
                appAnimationMode = appAnimationMode,
                moreRounds = moreRounds,
                moreRoundsMode = moreRoundsMode,
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
                    if (startupState.value == "splash") {
                        lumia.tracker.ui.screens.ProfileSplashLoadingScreen(
                            activeProfile = activeProfile,
                            onEnter = {
                                startupState.value = "main"
                            },
                            onSwitchAccount = {
                                startupState.value = "selector"
                            }
                        )
                    } else if (startupState.value == "selector") {
                        lumia.tracker.ui.screens.ProfileSelectionScreen(
                            profiles = allProfiles,
                            onProfileSelected = { profileId ->
                                if (profileId != activeProfile.id) {
                                    viewModel.switchProfileAndRestart(this@MainActivity, profileId)
                                } else {
                                    startupState.value = "main"
                                }
                            },
                            onCreateProfile = { name, emoji, alias, starterTheme -> 
                                viewModel.createProfile(name, emoji, alias, starterTheme)
                            }
                        )
                    } else {
                        val safetyPinDialogData by viewModel.safetyPinDialogData.collectAsStateWithLifecycle()
                        safetyPinDialogData?.let { data ->
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

                    val act = androidx.activity.compose.LocalActivity.current as? MainActivity
                    androidx.compose.runtime.LaunchedEffect(act?.intent) {
                        act?.intent?.let { intent ->
                            if (intent.action == "ACTION_OPEN_POMODORO" || intent.getBooleanExtra("OPEN_POMODORO", false)) {
                                navController.navigate("pomodoro") {
                                    launchSingleTop = true
                                }
                                intent.removeExtra("OPEN_POMODORO")
                                intent.action = null
                            }
                        }
                    }

                    val context = androidx.compose.ui.platform.LocalContext.current
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        val profMgr = lumia.tracker.data.ProfileManager(context)
                        val prefs = profMgr.getProfilePrefs()
                        if (prefs.getBoolean("auto_check_updates", true)) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                try {
                                    val url = java.net.URL("https://api.github.com/repos/ovrrup/Lumia/releases/latest")
                                    val connection = url.openConnection() as java.net.HttpURLConnection
                                    connection.requestMethod = "GET"
                                    connection.setRequestProperty("User-Agent", "Lumia-FOSS-App")
                                    connection.connectTimeout = 5000
                                    connection.readTimeout = 5000
                                    
                                    if (connection.responseCode == 200) {
                                        val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                                        val json = org.json.JSONObject(responseText)
                                        val tagName = json.optString("tag_name", "")
                                        val currentVersion = lumia.tracker.BuildConfig.VERSION_NAME
                                        
                                        if (tagName.isNotEmpty()) {
                                            if (lumia.tracker.util.VersionUtils.isUpdateAvailable(currentVersion, tagName)) {
                                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "Update Available: $tagName. Check About settings.",
                                                        android.widget.Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Ignore quietly in background
                                }
                            }
                        }
                    }

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
                            startDestination = if (isOnboardingCompleted) "dashboard" else "onboarding",
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
                        composable("onboarding") {
                            lumia.tracker.ui.screens.OnboardingScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("dashboard") {
                            DashboardScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("pomodoro") {
                            lumia.tracker.ui.screens.PomodoroScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("notes") {
                            lumia.tracker.ui.screens.QuickNotesScreen(navController = navController)
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
                            lumia.tracker.ui.screens.study.SubjectDetailScreen(navController = navController, viewModel = viewModel, subjectId = id)
                        }
                        composable("settings") {
                            SettingsScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        
                        composable("settings/streaks") {
                            lumia.tracker.ui.screens.settings.StreakSettingsScreen(navController, viewModel)
                        }

                        composable("settings/appearance") {
                            lumia.tracker.ui.screens.AppearanceScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("settings/advanced_theme") {
                            lumia.tracker.ui.screens.AdvancedThemeScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("settings/beta") {
                            lumia.tracker.ui.screens.BetaFeaturesScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("settings/safety") {
                            lumia.tracker.ui.screens.SafetyFeaturesScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("settings/data") {
                            lumia.tracker.ui.screens.DataManagementScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("settings/system") {
                            lumia.tracker.ui.screens.SystemSettingsScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("settings/notifications") {
                            lumia.tracker.ui.screens.NotificationsScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("settings/about") {
                            lumia.tracker.ui.screens.AboutAppScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("pdf_viewer?filePath={filePath}&fileName={fileName}") { backStackEntry ->
                            val filePath = backStackEntry.arguments?.getString("filePath")
                            val fileName = backStackEntry.arguments?.getString("fileName")
                            lumia.tracker.ui.screens.PdfViewerScreen(
                                navController = navController,
                                filePath = filePath,
                                fileName = fileName
                            )
                        }
                        composable("profile_menu") {
                            lumia.tracker.ui.screens.ProfileMenuScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("switch_user") {
                            startupState.value = "selector"
                            // Since startupState controls the entire surface display above NavHost, it'll just show the profile selector.
                            // However, we should pop back because it's an overlay
                            androidx.compose.runtime.LaunchedEffect(Unit) {
                                navController.navigateUp()
                            }
                        }
                    }
                    
                    val crashDataState by _crashData.collectAsStateWithLifecycle()
                    if (crashDataState != null) {
                        val parsed = remember(crashDataState) { lumia.tracker.util.LogDog.analyzeCrash(crashDataState ?: "") }
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
                                val context = androidx.compose.ui.platform.LocalContext.current
                                androidx.compose.material3.TextButton(onClick = {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Crash Log", crashDataState)
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, "Copied log to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                                    _crashData.value = null
                                }) { Text("Copy Log") }
                            }
                        )
                    }
                    
                    } // End of Box
                    } // End of else
                } // End of Surface
            }
        }
    }
}
