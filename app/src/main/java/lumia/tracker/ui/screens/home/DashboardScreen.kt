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
    val navBarHeight by viewModel.navBarHeight.collectAsStateWithLifecycle()
    val navBarPaddingHorizontal by viewModel.navBarPaddingHorizontal.collectAsStateWithLifecycle()
    val navBarPaddingBottom by viewModel.navBarPaddingBottom.collectAsStateWithLifecycle()
    val navBarCornerRadius by viewModel.navBarCornerRadius.collectAsStateWithLifecycle()
    val navBarLabelMode by viewModel.navBarLabelMode.collectAsStateWithLifecycle()
    val navBarIndicatorAlpha by viewModel.navBarIndicatorAlpha.collectAsStateWithLifecycle()

    val featureSelfStudyEnabled by viewModel.featureSelfStudyEnabled.collectAsStateWithLifecycle()
    val featureAnalyticsEnabled by viewModel.featureAnalyticsEnabled.collectAsStateWithLifecycle()

    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                ScholarInnovativeHeader(
                    selectedTab = selectedTab,
                    navController = navController,
                    viewModel = viewModel
                )
            },
            bottomBar = {
                if (!betaFloatingNav) {
                    val navItemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(
                            alpha = navBarIndicatorAlpha.coerceAtLeast(0.45f)
                        )
                    )
                    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 3.dp,
                        shadowElevation = 3.dp,
                        border = BorderStroke(
                            0.5.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        NavigationBar(
                            modifier = Modifier.height(navBarHeight.dp + bottomInset),
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp
                        ) {
                            DashboardNavItems(
                                selectedTab = selectedTab,
                                onSelectTab = { target ->
                                    viewModel.setSelectedDashboardTab(target)
                                },
                                navItemColors = navItemColors,
                                alwaysShowLabel = navBarLabelMode == "Always",
                                hideLabels = navBarLabelMode == "Hidden",
                                featureSelfStudyEnabled = featureSelfStudyEnabled,
                                featureAnalyticsEnabled = featureAnalyticsEnabled
                            )
                        }
                    }
                }
            }
        ) { padding ->
            val layoutDirection = LocalLayoutDirection.current
            val extendedPadding = if (betaFloatingNav) {
                PaddingValues(
                    start = padding.calculateStartPadding(layoutDirection),
                    top = 0.dp,
                    end = padding.calculateEndPadding(layoutDirection),
                    bottom = padding.calculateBottomPadding() + navBarHeight.dp + navBarPaddingBottom.dp + 16.dp
                )
            } else {
                PaddingValues(
                    start = padding.calculateStartPadding(layoutDirection),
                    top = 0.dp,
                    end = padding.calculateEndPadding(layoutDirection),
                    bottom = padding.calculateBottomPadding()
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
                    .clipToBounds()
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val isForward = targetState > initialState
                        val direction = if (isForward) 1 else -1
                        val slideDistanceFraction = 0.12f
                        val duration = 200

                        (slideInHorizontally(
                            animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> (fullWidth * slideDistanceFraction * direction).toInt() }
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = duration, easing = LinearOutSlowInEasing)
                        )).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                                targetOffsetX = { fullWidth -> (-fullWidth * slideDistanceFraction * direction).toInt() }
                            ) + fadeOut(
                                animationSpec = tween(durationMillis = duration, easing = FastOutLinearInEasing)
                            )
                        )
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

        // Floating Navigation Bar Pill
        if (betaFloatingNav) {
            val navItemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = navBarIndicatorAlpha.coerceAtLeast(0.5f)
                )
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = navBarPaddingHorizontal.dp,
                        end = navBarPaddingHorizontal.dp,
                        bottom = navBarPaddingBottom.dp
                    )
                    .windowInsetsPadding(WindowInsets.navigationBars),
                shape = RoundedCornerShape(navBarCornerRadius.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(
                    0.8.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                ),
                shadowElevation = 10.dp,
                tonalElevation = 6.dp
            ) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(navBarHeight.dp),
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0, 0, 0, 0)
                ) {
                    DashboardNavItems(
                        selectedTab = selectedTab,
                        onSelectTab = { target ->
                            viewModel.setSelectedDashboardTab(target)
                        },
                        navItemColors = navItemColors,
                        alwaysShowLabel = navBarLabelMode == "Always",
                        hideLabels = navBarLabelMode == "Hidden",
                        featureSelfStudyEnabled = featureSelfStudyEnabled,
                        featureAnalyticsEnabled = featureAnalyticsEnabled
                    )
                }
            }
        }
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
        targetValue = if (homeSelected) 1.12f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
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

    // 1: Academics (Courses & Subjects Unified)
    val academicsSelected = selectedTab == 1
    val academicsScale by animateFloatAsState(
        targetValue = if (academicsSelected) 1.12f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "academics_nav_scale"
    )
    NavigationBarItem(
        icon = {
            Icon(
                Icons.AutoMirrored.Rounded.MenuBook,
                contentDescription = "Academics",
                modifier = Modifier.graphicsLayer {
                    scaleX = academicsScale
                    scaleY = academicsScale
                }
            )
        },
        label = if (hideLabels) null else { { Text("Academics", fontWeight = if (academicsSelected) FontWeight.Bold else FontWeight.Medium) } },
        selected = academicsSelected,
        onClick = { onSelectTab(1) },
        colors = navItemColors,
        alwaysShowLabel = alwaysShowLabel
    )

    // 2: Tasks (SelfStudy)
    if (featureSelfStudyEnabled) {
        val tasksSelected = selectedTab == 2
        val tasksScale by animateFloatAsState(
            targetValue = if (tasksSelected) 1.12f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
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

    // 3: Analytics
    if (featureAnalyticsEnabled) {
        val analyticsSelected = selectedTab == 3
        val analyticsScale by animateFloatAsState(
            targetValue = if (analyticsSelected) 1.12f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
            label = "analytics_nav_scale"
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Rounded.Analytics,
                    contentDescription = "Analytics",
                    modifier = Modifier.graphicsLayer {
                        scaleX = analyticsScale
                        scaleY = analyticsScale
                    }
                )
            },
            label = if (hideLabels) null else { { Text("Analytics", fontWeight = if (analyticsSelected) FontWeight.Bold else FontWeight.Medium) } },
            selected = analyticsSelected,
            onClick = { onSelectTab(3) },
            colors = navItemColors,
            alwaysShowLabel = alwaysShowLabel
        )
    }
}
