package lumia.tracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.R
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.StreakWidget
import lumia.tracker.ui.screens.home.HomeTab
import lumia.tracker.ui.screens.home.components.ScholarInnovativeHeader
import lumia.tracker.ui.screens.study.*
import lumia.tracker.ui.theme.LocalAppAnimationMode
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch

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

    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) {
            pagerState.animateScrollToPage(selectedTab)
        }
    }

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
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = navBarIndicatorAlpha.coerceAtLeast(0.5f))
                    )
                    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    NavigationBar(
                        modifier = Modifier.height(navBarHeight.dp + bottomInset),
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        DashboardNavItems(
                            selectedTab = selectedTab,
                            onSelectTab = { 
                                viewModel.setSelectedDashboardTab(it)
                                coroutineScope.launch { pagerState.animateScrollToPage(it) }
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
        ) { padding ->
            val extendedPadding = if (betaFloatingNav) {
                PaddingValues(
                    start = padding.calculateStartPadding(androidx.compose.ui.platform.LocalLayoutDirection.current),
                    top = 0.dp,
                    end = padding.calculateEndPadding(androidx.compose.ui.platform.LocalLayoutDirection.current),
                    bottom = padding.calculateBottomPadding() + navBarHeight.dp + navBarPaddingBottom.dp + 16.dp
                )
            } else {
                PaddingValues(
                    start = padding.calculateStartPadding(androidx.compose.ui.platform.LocalLayoutDirection.current),
                    top = 0.dp,
                    end = padding.calculateEndPadding(androidx.compose.ui.platform.LocalLayoutDirection.current),
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
                                coroutineScope.launch { pagerState.animateScrollToPage(3) }
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

        if (betaFloatingNav) {
            val navItemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
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
                shadowElevation = 8.dp,
                tonalElevation = 4.dp
            ) {
                NavigationBar(
                    modifier = Modifier.fillMaxWidth().height(navBarHeight.dp),
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0, 0, 0, 0)
                ) {
                    DashboardNavItems(
                        selectedTab = selectedTab,
                        onSelectTab = { 
                            viewModel.setSelectedDashboardTab(it)
                            coroutineScope.launch { pagerState.animateScrollToPage(it) }
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
    NavigationBarItem(
        icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
        label = if (hideLabels) null else { { Text("Home") } },
        selected = selectedTab == 0,
        onClick = { onSelectTab(0) },
        colors = navItemColors,
        alwaysShowLabel = alwaysShowLabel
    )
    NavigationBarItem(
        icon = { Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = "Courses") },
        label = if (hideLabels) null else { { Text("Courses") } },
        selected = selectedTab == 1,
        onClick = { onSelectTab(1) },
        colors = navItemColors,
        alwaysShowLabel = alwaysShowLabel
    )
    if (featureSubjectEnabled && !fuseSubjectsCourses) {
        NavigationBarItem(
            icon = { Icon(Icons.Rounded.FolderOpen, contentDescription = "Subjects") },
            label = if (hideLabels) null else { { Text("Subjects") } },
            selected = selectedTab == 2,
            onClick = { onSelectTab(2) },
            colors = navItemColors,
            alwaysShowLabel = alwaysShowLabel
        )
    }
    if (featureSelfStudyEnabled) {
        NavigationBarItem(
            icon = { Icon(Icons.Rounded.AutoStories, contentDescription = "Tasks") },
            label = if (hideLabels) null else { { Text("Tasks") } },
            selected = selectedTab == 3,
            onClick = { onSelectTab(3) },
            colors = navItemColors,
            alwaysShowLabel = alwaysShowLabel
        )
    }
    if (featureAnalyticsEnabled) {
        NavigationBarItem(
            icon = { Icon(Icons.Rounded.Analytics, contentDescription = "Analytics") },
            label = if (hideLabels) null else { { Text("Analytics") } },
            selected = selectedTab == 4,
            onClick = { onSelectTab(4) },
            colors = navItemColors,
            alwaysShowLabel = alwaysShowLabel
        )
    }
}
