package lumia.tracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import lumia.tracker.R
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.StreakWidget
import lumia.tracker.ui.screens.home.HomeTab
import lumia.tracker.ui.screens.home.components.ScholarInnovativeHeader
import lumia.tracker.ui.screens.study.*
import lumia.tracker.ui.theme.LocalAppAnimationMode
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * DashboardScreen - Core container hosting the top ScholarInnovativeHeader,
 * smooth HorizontalPager tabs, and adaptive navigation bar (Floating Pill or Standard M3 dock).
 */
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
    val fuseSubjectsCourses by viewModel.systemFuseSubjectsCourses.collectAsStateWithLifecycle()

    val featureSubjectEnabled by viewModel.featureSubjectEnabled.collectAsStateWithLifecycle()
    val featureSelfStudyEnabled by viewModel.featureSelfStudyEnabled.collectAsStateWithLifecycle()
    val featureAnalyticsEnabled by viewModel.featureAnalyticsEnabled.collectAsStateWithLifecycle()

    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = selectedTab, pageCount = { 5 })

    // Sync ViewModel tab changes into pager
    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) {
            pagerState.animateScrollToPage(
                page = selectedTab,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    // Sync user swiping inside pager back to ViewModel
    LaunchedEffect(pagerState.currentPage) {
        if (selectedTab != pagerState.currentPage) {
            viewModel.setSelectedDashboardTab(pagerState.currentPage)
        }
    }

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
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(
                                            page = target,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                    }
                                },
                                navItemColors = navItemColors,
                                alwaysShowLabel = navBarLabelMode == "Always",
                                hideLabels = navBarLabelMode == "Hidden",
                                featureSubjectEnabled = featureSubjectEnabled,
                                fuseSubjectsCourses = fuseSubjectsCourses,
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
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    key = { it }
                ) { targetTab ->
                    when (targetTab) {
                        0 -> HomeTab(
                            navController = navController,
                            viewModel = viewModel,
                            bottomPadding = extendedPadding,
                            onAddCourseClick = { showAddCourseDialog = true },
                            onAddSubjectClick = { showAddSubjectDialog = true },
                            onNavigateToTasks = {
                                viewModel.setSelectedDashboardTab(3)
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(
                                        page = 3,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                            }
                        )
                        1 -> CoursesTab(
                            navController = navController,
                            viewModel = viewModel,
                            bottomPadding = extendedPadding,
                            onEditCourse = { /* Handled in tab */ },
                            onAddCourseClick = { showAddCourseDialog = true }
                        )
                        2 -> SubjectsTab(
                            navController = navController,
                            viewModel = viewModel,
                            bottomPadding = extendedPadding,
                            onEditSubject = { /* Handled in tab */ },
                            onAddSubjectClick = { showAddSubjectDialog = true }
                        )
                        3 -> SelfStudyTab(
                            navController = navController,
                            viewModel = viewModel,
                            bottomPadding = extendedPadding
                        )
                        4 -> AnalyticsTab(
                            navController = navController,
                            viewModel = viewModel,
                            paddingValues = extendedPadding
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
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(
                                    page = target,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }
                        },
                        navItemColors = navItemColors,
                        alwaysShowLabel = navBarLabelMode == "Always",
                        hideLabels = navBarLabelMode == "Hidden",
                        featureSubjectEnabled = featureSubjectEnabled,
                        fuseSubjectsCourses = fuseSubjectsCourses,
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

@Composable
private fun RowScope.DashboardNavItems(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    navItemColors: NavigationBarItemColors,
    alwaysShowLabel: Boolean,
    hideLabels: Boolean,
    featureSubjectEnabled: Boolean,
    fuseSubjectsCourses: Boolean,
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

    // 1: Courses
    val coursesSelected = selectedTab == 1
    val coursesScale by animateFloatAsState(
        targetValue = if (coursesSelected) 1.12f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "courses_nav_scale"
    )
    NavigationBarItem(
        icon = {
            Icon(
                Icons.AutoMirrored.Rounded.MenuBook,
                contentDescription = "Courses",
                modifier = Modifier.graphicsLayer {
                    scaleX = coursesScale
                    scaleY = coursesScale
                }
            )
        },
        label = if (hideLabels) null else { { Text("Courses", fontWeight = if (coursesSelected) FontWeight.Bold else FontWeight.Medium) } },
        selected = coursesSelected,
        onClick = { onSelectTab(1) },
        colors = navItemColors,
        alwaysShowLabel = alwaysShowLabel
    )

    // 2: Subjects
    if (featureSubjectEnabled && !fuseSubjectsCourses) {
        val subjectsSelected = selectedTab == 2
        val subjectsScale by animateFloatAsState(
            targetValue = if (subjectsSelected) 1.12f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
            label = "subjects_nav_scale"
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Rounded.FolderOpen,
                    contentDescription = "Subjects",
                    modifier = Modifier.graphicsLayer {
                        scaleX = subjectsScale
                        scaleY = subjectsScale
                    }
                )
            },
            label = if (hideLabels) null else { { Text("Subjects", fontWeight = if (subjectsSelected) FontWeight.Bold else FontWeight.Medium) } },
            selected = subjectsSelected,
            onClick = { onSelectTab(2) },
            colors = navItemColors,
            alwaysShowLabel = alwaysShowLabel
        )
    }

    // 3: Tasks (SelfStudy)
    if (featureSelfStudyEnabled) {
        val tasksSelected = selectedTab == 3
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
            onClick = { onSelectTab(3) },
            colors = navItemColors,
            alwaysShowLabel = alwaysShowLabel
        )
    }

    // 4: Analytics
    if (featureAnalyticsEnabled) {
        val analyticsSelected = selectedTab == 4
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
            onClick = { onSelectTab(4) },
            colors = navItemColors,
            alwaysShowLabel = alwaysShowLabel
        )
    }
}
