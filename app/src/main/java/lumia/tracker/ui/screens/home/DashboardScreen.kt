package lumia.tracker.ui.screens

import lumia.tracker.ui.screens.study.dialogs.*

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.components.StreakWidget
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.home.HomeTab
import lumia.tracker.ui.screens.home.components.DashboardTopFloatingPills
import lumia.tracker.ui.screens.home.components.ScholarInnovativeHeader
import lumia.tracker.ui.screens.study.*
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * DashboardScreen - Core container hosting the top ScholarInnovativeHeader,
 * smooth AnimatedContent tab transitions, and adaptive navigation bar (Floating Pill or Standard M3 dock).
 */
@ValueScore(
    score = 98,
    importance = Importance.CRITICAL,
    description = "Root dashboard container orchestrating responsive navigation bars and smooth 12% slide AnimatedContent tab transitions",
    category = "Navigation"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: ScholarViewModel) {
    val selectedTab by viewModel.selectedDashboardTab.collectAsStateWithLifecycle()
    val betaFloatingNav by viewModel.betaFloatingNav.collectAsStateWithLifecycle()
    val navBarHeightRaw by viewModel.navBarHeight.collectAsStateWithLifecycle()
    val navBarPaddingHorizontalRaw by viewModel.navBarPaddingHorizontal.collectAsStateWithLifecycle()
    val navBarPaddingBottomRaw by viewModel.navBarPaddingBottom.collectAsStateWithLifecycle()
    val navBarCornerRadiusRaw by viewModel.navBarCornerRadius.collectAsStateWithLifecycle()
    val navBarHeight = navBarHeightRaw.toInt()
    val navBarPaddingHorizontal = navBarPaddingHorizontalRaw.toInt()
    val navBarPaddingBottom = navBarPaddingBottomRaw.toInt()
    val navBarCornerRadius = navBarCornerRadiusRaw.toInt()
    val navBarLabelMode by viewModel.navBarLabelMode.collectAsStateWithLifecycle()
    val navBarIndicatorAlpha by viewModel.navBarIndicatorAlpha.collectAsStateWithLifecycle()

    val featureSelfStudyEnabled by viewModel.featureSelfStudyEnabled.collectAsStateWithLifecycle()
    val featureAnalyticsEnabled by viewModel.featureAnalyticsEnabled.collectAsStateWithLifecycle()

    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }

    val navItemColors = rememberDashboardNavBarColors(navBarIndicatorAlpha)

    val navBarsBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val statusBarsTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topFloatingPillHeight = 58.dp
    val extendedPadding = PaddingValues(
        start = 0.dp,
        top = 0.dp,
        end = 0.dp,
        bottom = navBarsBottom + navBarHeight.dp + navBarPaddingBottom.dp + 20.dp
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {},
            bottomBar = {}
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = statusBarsTop + topFloatingPillHeight)
                    .clipToBounds()
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(180, easing = LinearOutSlowInEasing)) +
                         scaleIn(initialScale = 0.98f, animationSpec = tween(180, easing = FastOutSlowInEasing)))
                            .togetherWith(fadeOut(animationSpec = tween(140, easing = FastOutLinearInEasing)))
                    },
                    label = "DashboardTabContent",
                    modifier = Modifier.fillMaxSize()
                ) { targetTab ->
                    when (targetTab) {
                        0 -> HomeTab(
                            navController = navController,
                            viewModel = viewModel,
                            bottomPadding = extendedPadding,
                            onAddCourseClick = { showAddCourseDialog = true },
                            onAddSubjectClick = { showAddSubjectDialog = true },
                            onNavigateToTasks = {
                                viewModel.setSelectedDashboardTab(2)
                            }
                        )
                        1 -> AcademicsScreen(
                            navController = navController,
                            viewModel = viewModel,
                            bottomPadding = extendedPadding,
                            onAddCourseClick = { showAddCourseDialog = true },
                            onAddSubjectClick = { showAddSubjectDialog = true }
                        )
                        2 -> SelfStudyTab(
                            navController = navController,
                            viewModel = viewModel,
                            bottomPadding = extendedPadding
                        )
                        3 -> AnalyticsTab(
                            navController = navController,
                            viewModel = viewModel,
                            paddingValues = extendedPadding
                        )
                        else -> HomeTab(
                            navController = navController,
                            viewModel = viewModel,
                            bottomPadding = extendedPadding,
                            onAddCourseClick = { showAddCourseDialog = true },
                            onAddSubjectClick = { showAddSubjectDialog = true },
                            onNavigateToTasks = {
                                viewModel.setSelectedDashboardTab(2)
                            }
                        )
                    }
                }
            }
        }

        // Top Floating Action Pills Bar
        DashboardTopFloatingPills(
            selectedTab = selectedTab,
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Bottom Floating Navigation Pill Dock
        FloatingDashboardBottomBar(
            navBarHeight = navBarHeight,
            navBarPaddingHorizontal = navBarPaddingHorizontal,
            navBarPaddingBottom = navBarPaddingBottom,
            navBarCornerRadius = navBarCornerRadius,
            navItemColors = navItemColors,
            selectedTab = selectedTab,
            onSelectTab = { target ->
                viewModel.setSelectedDashboardTab(target)
            },
            navBarLabelMode = navBarLabelMode,
            featureSelfStudyEnabled = featureSelfStudyEnabled,
            featureAnalyticsEnabled = featureAnalyticsEnabled
        )
    }

    // Modal dialog triggers
    if (showAddCourseDialog) {
        AddCourseDialog(
            viewModel = viewModel,
            onDismiss = { showAddCourseDialog = false }
        )
    }

    if (showAddSubjectDialog) {
        AddSubjectDialog(
            viewModel = viewModel,
            onDismiss = { showAddSubjectDialog = false }
        )
    }
}

@ValueScore(
    score = 86,
    importance = Importance.MEDIUM,
    description = "Provides unified navigation item color styling and customizable indicator alpha",
    category = "Navigation"
)
@Composable
private fun rememberDashboardNavBarColors(indicatorAlpha: Float): NavigationBarItemColors {
    val clampedAlpha = indicatorAlpha.coerceIn(0.2f, 1f)
    return NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = clampedAlpha)
    )
}

@ValueScore(
    score = 91,
    importance = Importance.HIGH,
    description = "Floating pill dynamic navigation bar with custom corner radius and elevated shadow",
    category = "Navigation"
)
@Composable
private fun BoxScope.FloatingDashboardBottomBar(
    navBarHeight: Int,
    navBarPaddingHorizontal: Int,
    navBarPaddingBottom: Int,
    navBarCornerRadius: Int,
    navItemColors: NavigationBarItemColors,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    navBarLabelMode: String,
    featureSelfStudyEnabled: Boolean,
    featureAnalyticsEnabled: Boolean
) {
    val hPadding = if (navBarPaddingHorizontal > 0) navBarPaddingHorizontal else 20
    val bPadding = if (navBarPaddingBottom > 0) navBarPaddingBottom else 16
    val dockHeight = if (navBarHeight > 0) navBarHeight else 64
    Surface(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(
                start = hPadding.dp,
                end = hPadding.dp,
                bottom = bPadding.dp
            )
            .windowInsetsPadding(WindowInsets.navigationBars),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        shadowElevation = 8.dp,
        tonalElevation = 0.dp
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeight.dp),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            DashboardNavItems(
                selectedTab = selectedTab,
                onSelectTab = onSelectTab,
                navItemColors = navItemColors,
                alwaysShowLabel = navBarLabelMode == "Always",
                hideLabels = navBarLabelMode == "Hidden",
                featureSelfStudyEnabled = featureSelfStudyEnabled,
                featureAnalyticsEnabled = featureAnalyticsEnabled
            )
        }
    }
}

@ValueScore(
    score = 92,
    importance = Importance.CRITICAL,
    description = "Navigation items row supporting fluid scale physics and adaptive indicators",
    category = "Navigation"
)
@Composable
private fun RowScope.DashboardNavItems(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    navItemColors: NavigationBarItemColors,
    alwaysShowLabel: Boolean,
    hideLabels: Boolean,
    featureSelfStudyEnabled: Boolean,
    featureAnalyticsEnabled: Boolean
) {
    // 0: Home
    val homeSelected = selectedTab == 0
    val homeScale by animateFloatAsState(
        targetValue = if (homeSelected) 1.05f else 1.0f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "home_nav_scale"
    )
    NavigationBarItem(
        icon = {
            Icon(
                Icons.Rounded.Home,
                contentDescription = "Home",
                modifier = Modifier.graphicsLayer {
                    scaleX = homeScale
                    scaleY = homeScale
                }
            )
        },
        label = if (hideLabels) null else { { Text("Home", fontWeight = if (homeSelected) FontWeight.Bold else FontWeight.Medium) } },
        selected = homeSelected,
        onClick = { onSelectTab(0) },
        colors = navItemColors,
        alwaysShowLabel = alwaysShowLabel
    )

    // 1: Classes (Courses & Subjects)
    val academicsSelected = selectedTab == 1
    val academicsScale by animateFloatAsState(
        targetValue = if (academicsSelected) 1.05f else 1.0f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "academics_nav_scale"
    )
    NavigationBarItem(
        icon = {
            Icon(
                Icons.AutoMirrored.Rounded.MenuBook,
                contentDescription = "Classes",
                modifier = Modifier.graphicsLayer {
                    scaleX = academicsScale
                    scaleY = academicsScale
                }
            )
        },
        label = if (hideLabels) null else { { Text("Classes", fontWeight = if (academicsSelected) FontWeight.Bold else FontWeight.Medium) } },
        selected = academicsSelected,
        onClick = { onSelectTab(1) },
        colors = navItemColors,
        alwaysShowLabel = alwaysShowLabel
    )

    // 2: Tasks
    if (featureSelfStudyEnabled) {
        val tasksSelected = selectedTab == 2
        val tasksScale by animateFloatAsState(
            targetValue = if (tasksSelected) 1.05f else 1.0f,
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
            label = "tasks_nav_scale"
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Rounded.AutoStories,
                    contentDescription = "Tasks",
                    modifier = Modifier.graphicsLayer {
                        scaleX = tasksScale
                        scaleY = tasksScale
                    }
                )
            },
            label = if (hideLabels) null else { { Text("Tasks", fontWeight = if (tasksSelected) FontWeight.Bold else FontWeight.Medium) } },
            selected = tasksSelected,
            onClick = { onSelectTab(2) },
            colors = navItemColors,
            alwaysShowLabel = alwaysShowLabel
        )
    }

    // 3: Progress
    if (featureAnalyticsEnabled) {
        val analyticsSelected = selectedTab == 3
        val analyticsScale by animateFloatAsState(
            targetValue = if (analyticsSelected) 1.05f else 1.0f,
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
            label = "analytics_nav_scale"
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Rounded.Analytics,
                    contentDescription = "Progress",
                    modifier = Modifier.graphicsLayer {
                        scaleX = analyticsScale
                        scaleY = analyticsScale
                    }
                )
            },
            label = if (hideLabels) null else { { Text("Progress", fontWeight = if (analyticsSelected) FontWeight.Bold else FontWeight.Medium) } },
            selected = analyticsSelected,
            onClick = { onSelectTab(3) },
            colors = navItemColors,
            alwaysShowLabel = alwaysShowLabel
        )
    }
}
