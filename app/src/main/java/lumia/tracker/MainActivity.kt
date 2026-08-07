package lumia.tracker

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import lumia.tracker.ui.components.AmbientBackgroundCanvas
import lumia.tracker.ui.components.SafetyPinDialog
import lumia.tracker.ui.navigation.AppNavigationGraph
import lumia.tracker.ui.screens.*
import lumia.tracker.ui.theme.ScholarTheme
import lumia.tracker.util.LogDog
import lumia.tracker.viewmodel.*
import lumia.tracker.worker.AssignmentMonitorWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val viewModel: ScholarViewModel by viewModels()
    private val _crashData = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    private val _intentFlow = kotlinx.coroutines.flow.MutableStateFlow<android.content.Intent?>(null)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        _intentFlow.value = intent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _intentFlow.value = intent

        intent.getStringExtra("FATAL_CRASH_DATA")?.let {
            _crashData.value = it
            intent.removeExtra("FATAL_CRASH_DATA")
        }

        LogDog.setup(applicationContext)

        try {
            val workRequest = PeriodicWorkRequestBuilder<AssignmentMonitorWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(1, TimeUnit.HOURS).build()
            WorkManager.getInstance(this).enqueueUniquePeriodicWork("assignment_monitor", ExistingPeriodicWorkPolicy.KEEP, workRequest)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "WorkManager failed to start", e)
        }

        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
            val pureBlackMode by viewModel.pureBlackMode.collectAsStateWithLifecycle()
            val displayLayoutMode by viewModel.displayLayoutMode.collectAsStateWithLifecycle()
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
            val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()

            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val effectiveDark = themeMode == "Dark" || (themeMode == "System" && isSystemDark)

            val navBarGlassOpacityValue by viewModel.navBarGlassOpacityValue.collectAsStateWithLifecycle()
            val navBarGlassLinkedToMain by viewModel.navBarGlassLinkedToMain.collectAsStateWithLifecycle()
            val navBarGlassBackdropStyle by viewModel.navBarGlassBackdropStyle.collectAsStateWithLifecycle()
            val navBarGlassDynamic by viewModel.navBarGlassDynamic.collectAsStateWithLifecycle()

            LaunchedEffect(effectiveDark, themeColor) { viewModel.refreshNavBarGlassOpacity(themeColor, effectiveDark) }

            val systemBarVisible by viewModel.systemBarVisible.collectAsStateWithLifecycle()
            LaunchedEffect(displayLayoutMode, systemBarVisible) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val window = (this@MainActivity).window
                    window.attributes = window.attributes.apply {
                        layoutInDisplayCutoutMode = if (displayLayoutMode == "Immersive") android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES else android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
                    }
                }
                val window = (this@MainActivity).window
                val controller = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
                if (displayLayoutMode == "Immersive" && !systemBarVisible) {
                    controller.hide(androidx.core.view.WindowInsetsCompat.Type.statusBars())
                    controller.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    controller.show(androidx.core.view.WindowInsetsCompat.Type.statusBars())
                }
            }

            val startupState = remember { mutableStateOf("splash") }
            val actForStartup = androidx.activity.compose.LocalActivity.current as? MainActivity
            LaunchedEffect(actForStartup?.intent) {
                if (actForStartup?.intent?.getBooleanExtra("OPEN_PROFILE_SELECTOR", false) == true) {
                    startupState.value = "selector"
                    actForStartup.intent.removeExtra("OPEN_PROFILE_SELECTOR")
                }
            }
            val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
            val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()

            ScholarTheme(
                themeMode = themeMode, themeColor = themeColor, pureBlackMode = pureBlackMode, glassMode = false,
                glassDynamic = betaGlassDynamic, frostGlass = betaFrostGlass, glassBackdropStyle = glassBackdropStyle,
                glassOpacityValue = glassOpacityValue, navBarGlassOpacityValue = navBarGlassOpacityValue,
                navBarGlassLinkedToMain = navBarGlassLinkedToMain, navBarGlassBackdropStyle = navBarGlassBackdropStyle,
                navBarGlassDynamic = navBarGlassDynamic, betterTexts = betaBetterTexts, betterTextsPalette = betaBetterTextsPalette,
                appAnimationMode = appAnimationMode, moreRounds = moreRounds, customPrimary = customPrimary,
                customPrimaryContainer = customPrimaryContainer, customBackground = customBackground,
                customSurface = customSurface, customText = customText
            ) {
                val dragAccumulator = remember { mutableStateOf(0f) }
                Surface(
                    modifier = Modifier.fillMaxSize().then(
                        if (displayLayoutMode == "Notch Optimization") Modifier.displayCutoutPadding() else Modifier
                    ).then(
                        if (displayLayoutMode == "Immersive") {
                            Modifier.pointerInput(Unit) {
                                detectVerticalDragGestures(
                                    onDragEnd = { dragAccumulator.value = 0f },
                                    onDragCancel = { dragAccumulator.value = 0f },
                                    onVerticalDrag = { _, dragAmount ->
                                        dragAccumulator.value += dragAmount
                                        if (dragAccumulator.value > 50f) { viewModel.setSystemBarVisible(true); dragAccumulator.value = 0f }
                                        else if (dragAccumulator.value < -50f) { viewModel.setSystemBarVisible(false); dragAccumulator.value = 0f }
                                    }
                                )
                            }
                        } else Modifier
                    ), color = MaterialTheme.colorScheme.background
                ) {
                    if (startupState.value == "splash") {
                        ProfileSplashLoadingScreen(activeProfile = activeProfile, onEnter = { startupState.value = "main" }, onSwitchAccount = { startupState.value = "selector" })
                    } else if (startupState.value == "selector") {
                        ProfileSelectionScreen(
                            profiles = allProfiles,
                            onProfileSelected = { profileId ->
                                if (profileId != activeProfile.id) viewModel.switchProfileAndRestart(this@MainActivity, profileId)
                                else startupState.value = "main"
                            },
                            onCreateProfile = { name, emoji, alias, starterTheme -> viewModel.createProfile(name, emoji, alias, starterTheme) }
                        )
                    } else {
                        SafetyPinDialog(viewModel = viewModel)

                        val navController = rememberNavController()
                        val currentIntent by _intentFlow.collectAsStateWithLifecycle()
                        LaunchedEffect(currentIntent) {
                            currentIntent?.let { intent ->
                                intent.getStringExtra("OPEN_SCREEN")?.takeIf { it.isNotEmpty() }?.let {
                                    navController.navigate(it) { launchSingleTop = true }
                                    intent.removeExtra("OPEN_SCREEN")
                                }
                                val openTab = intent.getIntExtra("OPEN_TAB", -1)
                                if (openTab != -1) {
                                    viewModel.setSelectedDashboardTab(openTab)
                                    navController.navigate("dashboard") { popUpTo("dashboard") { inclusive = false }; launchSingleTop = true }
                                    intent.removeExtra("OPEN_TAB")
                                }
                                if (intent.action == "ACTION_OPEN_POMODORO" || intent.getBooleanExtra("OPEN_POMODORO", false)) {
                                    navController.navigate("pomodoro") { launchSingleTop = true }
                                    intent.removeExtra("OPEN_POMODORO")
                                    intent.action = null
                                }
                                _intentFlow.value = null
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            AmbientBackgroundCanvas(enabled = betaDynamicBackground, lightBrightness = dynamicBgLightBrightness, darkBrightness = dynamicBgDarkBrightness)
                            AppNavigationGraph(navController = navController, viewModel = viewModel, isOnboardingCompleted = isOnboardingCompleted, displayLayoutMode = displayLayoutMode, appAnimationMode = appAnimationMode, startupState = startupState)
                        }
                    }
                }
            }
        }
    }
}
