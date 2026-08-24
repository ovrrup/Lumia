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
import lumia.tracker.ui.screens.study.*
import lumia.tracker.ui.theme.LocalAppAnimationMode
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel

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

    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            topBar = {
                val titleText = when (selectedTab) {
                    0 -> stringResource(id = R.string.app_name)
                    1 -> "Your Courses"
                    2 -> "Your Subjects"
                    3 -> "Self Study & Tasks"
                    else -> "Analytics"
                }
                Box {
                    if (betaEnhancedHeader) {
                        HorizontalDivider(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = titleText,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        navigationIcon = {
                            BouncyIconButton(
                                onClick = { navController.navigate("search") },
                                modifier = Modifier.padding(start = 12.dp).testTag("open_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "Open Global Search",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        actions = {
                            StreakWidget(viewModel, navController)
                            val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
                            Box(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(42.dp)
                                    .shadow(elevation = 3.dp, shape = CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    .clip(CircleShape)
                                    .bouncyClick(
                                        onClick = { navController.navigate("profile_menu") }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val isLocalImage = activeProfile.avatarEmoji.startsWith("/") ||
                                        activeProfile.avatarEmoji.startsWith("file://") ||
                                        activeProfile.avatarEmoji.startsWith("content://")
                                if (isLocalImage) {
                                    coil.compose.AsyncImage(
                                        model = activeProfile.avatarEmoji,
                                        contentDescription = "Profile Picture",
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    val fallback = if (activeProfile.avatarEmoji.isNotBlank() &&
                                        activeProfile.avatarEmoji.length <= 2 &&
                                        activeProfile.avatarEmoji != "A" && activeProfile.avatarEmoji != "U"
                                    ) {
                                        activeProfile.avatarEmoji.uppercase()
                                    } else {
                                        activeProfile.name.take(2).uppercase()
                                    }
                                    Text(
                                        text = fallback,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = if (betaEnhancedHeader) MaterialTheme.colorScheme.surface.copy(alpha = 0.95f) else MaterialTheme.colorScheme.surface,
                            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                }
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
                            onSelectTab = { viewModel.setSelectedDashboardTab(it) },
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
                val appAnimationMode = LocalAppAnimationMode.current
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val spec = if (appAnimationMode == "Bouncy") {
                            spring<androidx.compose.ui.unit.IntOffset>(dampingRatio = 0.45f, stiffness = 200f)
                        } else if (appAnimationMode == "Dynamic") {
                            spring<androidx.compose.ui.unit.IntOffset>(dampingRatio = 0.75f, stiffness = 500f)
                        } else {
                            tween<androidx.compose.ui.unit.IntOffset>(300, easing = LinearOutSlowInEasing)
                        }
                        val scaleSpec = if (appAnimationMode == "Bouncy") {
                            spring<Float>(dampingRatio = 0.45f, stiffness = 200f)
                        } else if (appAnimationMode == "Dynamic") {
                            spring<Float>(dampingRatio = 0.75f, stiffness = 500f)
                        } else {
                            tween<Float>(300, easing = LinearOutSlowInEasing)
                        }
                        if (targetState > initialState) {
                            (slideInHorizontally(animationSpec = spec) { width -> width / 3 } +
                                    fadeIn(animationSpec = tween(220)) +
                                    scaleIn(initialScale = 0.95f, animationSpec = scaleSpec)).togetherWith(
                                slideOutHorizontally(animationSpec = spec) { width -> -width / 3 } +
                                        fadeOut(animationSpec = tween(220)) +
                                        scaleOut(targetScale = 0.95f, animationSpec = scaleSpec)
                            )
                        } else {
                            (slideInHorizontally(animationSpec = spec) { width -> -width / 3 } +
                                    fadeIn(animationSpec = tween(220)) +
                                    scaleIn(initialScale = 0.95f, animationSpec = scaleSpec)).togetherWith(
                                slideOutHorizontally(animationSpec = spec) { width -> width / 3 } +
                                        fadeOut(animationSpec = tween(220)) +
                                        scaleOut(targetScale = 0.95f, animationSpec = scaleSpec)
                            )
                        }
                    },
                    label = "TabTransition",
                    modifier = Modifier.fillMaxSize()
                ) { targetTab ->
                    when (targetTab) {
                        0 -> HomeTab(
                            navController = navController,
                            viewModel = viewModel,
                            bottomPadding = extendedPadding,
                            onAddCourseClick = { showAddCourseDialog = true },
                            onAddSubjectClick = { showAddSubjectDialog = true },
                            onNavigateToTasks = { viewModel.setSelectedDashboardTab(3) }
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
                        4 -> AnalyticsTab(navController = navController, viewModel = viewModel, paddingValues = extendedPadding)
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
                        onSelectTab = { viewModel.setSelectedDashboardTab(it) },
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
