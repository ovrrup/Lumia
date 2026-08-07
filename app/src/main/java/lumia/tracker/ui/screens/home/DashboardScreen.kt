package lumia.tracker.ui.screens

import lumia.tracker.ui.components.header.glassHeaderCapsule
import lumia.tracker.ui.theme.navGlassBar
import lumia.tracker.ui.theme.glassPill
import lumia.tracker.ui.theme.LocalDarkTheme
import lumia.tracker.ui.theme.LocalPureBlackMode
import lumia.tracker.ui.theme.LocalMoreRounds
import lumia.tracker.ui.theme.LocalMoreRoundsMode
import androidx.compose.animation.togetherWith
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
                            import lumia.tracker.ui.theme.bouncyClick
import androidx.compose.foundation.layout.*

// ... the rest of the imports ...
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.platform.testTag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.R
import lumia.tracker.viewmodel.ScholarViewModel
import androidx.compose.foundation.border
import lumia.tracker.ui.components.navigation.FloatingCapsuleNavBar
import lumia.tracker.ui.components.navigation.NavTabItem


@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: ScholarViewModel) {
    val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current
    val selectedTab by viewModel.selectedDashboardTab.collectAsStateWithLifecycle()
    val betaFloatingNav by viewModel.betaFloatingNav.collectAsStateWithLifecycle()
    val navBarHeight by viewModel.navBarHeight.collectAsStateWithLifecycle()
    val navBarPaddingHorizontal by viewModel.navBarPaddingHorizontal.collectAsStateWithLifecycle()
    val navBarPaddingBottom by viewModel.navBarPaddingBottom.collectAsStateWithLifecycle()
    val navBarCornerRadius by viewModel.navBarCornerRadius.collectAsStateWithLifecycle()
    val navBarLabelMode by viewModel.navBarLabelMode.collectAsStateWithLifecycle()
    val navBarGlassForceEnabled by viewModel.navBarGlassForceEnabled.collectAsStateWithLifecycle()
    val navBarIndicatorAlpha by viewModel.navBarIndicatorAlpha.collectAsStateWithLifecycle()
    val fuseSubjectsCourses by viewModel.systemFuseSubjectsCourses.collectAsStateWithLifecycle()
    
    val featureSubjectEnabled by viewModel.featureSubjectEnabled.collectAsStateWithLifecycle()
    val featureSelfStudyEnabled by viewModel.featureSelfStudyEnabled.collectAsStateWithLifecycle()
    val featureAnalyticsEnabled by viewModel.featureAnalyticsEnabled.collectAsStateWithLifecycle()
    val featureCalendarEnabled by viewModel.featureCalendarEnabled.collectAsStateWithLifecycle()
    val featureQuickNotesEnabled by viewModel.featureQuickNotesEnabled.collectAsStateWithLifecycle()

    val tabHomeLabel by viewModel.tabHomeLabel.collectAsStateWithLifecycle()
    val tabHomeIconName by viewModel.tabHomeIcon.collectAsStateWithLifecycle()
    val tabCoursesLabel by viewModel.tabCoursesLabel.collectAsStateWithLifecycle()
    val tabCoursesIconName by viewModel.tabCoursesIcon.collectAsStateWithLifecycle()
    val tabSubjectsLabel by viewModel.tabSubjectsLabel.collectAsStateWithLifecycle()
    val tabSubjectsIconName by viewModel.tabSubjectsIcon.collectAsStateWithLifecycle()
    val tabSelfStudyLabel by viewModel.tabSelfStudyLabel.collectAsStateWithLifecycle()
    val tabSelfStudyIconName by viewModel.tabSelfStudyIcon.collectAsStateWithLifecycle()
    val tabAnalyticsLabel by viewModel.tabAnalyticsLabel.collectAsStateWithLifecycle()
    val tabAnalyticsIconName by viewModel.tabAnalyticsIcon.collectAsStateWithLifecycle()
    val tabCalendarLabel by viewModel.tabCalendarLabel.collectAsStateWithLifecycle()
    val tabCalendarIconName by viewModel.tabCalendarIcon.collectAsStateWithLifecycle()

    val tabHomeIcon = getTabIcon(tabHomeIconName)
    val tabCoursesIcon = getTabIcon(tabCoursesIconName)
    val tabSubjectsIcon = getTabIcon(tabSubjectsIconName)
    val tabSelfStudyIcon = getTabIcon(tabSelfStudyIconName)
    val tabAnalyticsIcon = getTabIcon(tabAnalyticsIconName)
    val tabCalendarIcon = getTabIcon(tabCalendarIconName)
    
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }

    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()

    val navTabs = remember(
        featureSubjectEnabled, fuseSubjectsCourses, featureSelfStudyEnabled, featureAnalyticsEnabled,
        tabHomeLabel, tabCoursesLabel, tabSubjectsLabel, tabSelfStudyLabel, tabAnalyticsLabel,
        tabHomeIcon, tabCoursesIcon, tabSubjectsIcon, tabSelfStudyIcon, tabAnalyticsIcon
    ) {
        val list = mutableListOf(
            NavTabItem(0, tabHomeLabel, tabHomeIcon),
            NavTabItem(1, tabCoursesLabel, tabCoursesIcon)
        )
        if (featureSubjectEnabled && !fuseSubjectsCourses) {
            list.add(NavTabItem(2, tabSubjectsLabel, tabSubjectsIcon))
        }
        if (featureSelfStudyEnabled) {
            list.add(NavTabItem(3, tabSelfStudyLabel, tabSelfStudyIcon))
        }
        if (featureAnalyticsEnabled) {
            list.add(NavTabItem(4, tabAnalyticsLabel, tabAnalyticsIcon))
        }
        list
    }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ) { padding ->
            val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val extendedPadding = PaddingValues(
                start = padding.calculateStartPadding(androidx.compose.ui.platform.LocalLayoutDirection.current),
                top = statusBarHeight + 64.dp,
                end = padding.calculateEndPadding(androidx.compose.ui.platform.LocalLayoutDirection.current),
                bottom = padding.calculateBottomPadding() + 68.dp
            )
            var dragAmount by remember { mutableStateOf(0f) }
            val enabledTabs = remember(navTabs) { navTabs.map { it.id } }

            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
                    .clipToBounds()
                    .pointerInput(enabledTabs, selectedTab) {
                        detectHorizontalDragGestures(
                            onDragStart = { dragAmount = 0f },
                            onDragEnd = {
                                val threshold = 180f
                                val currentIndex = enabledTabs.indexOf(selectedTab)
                                if (currentIndex != -1) {
                                    if (dragAmount < -threshold && currentIndex < enabledTabs.size - 1) {
                                        viewModel.setSelectedDashboardTab(enabledTabs[currentIndex + 1])
                                    } else if (dragAmount > threshold && currentIndex > 0) {
                                        viewModel.setSelectedDashboardTab(enabledTabs[currentIndex - 1])
                                    }
                                }
                            },
                            onHorizontalDrag = { change: PointerInputChange, dragAmountPx: Float ->
                                change.consume()
                                dragAmount += dragAmountPx
                            }
                        )
                    }
            ) {
                val appAnimationMode = lumia.tracker.ui.theme.LocalAppAnimationMode.current
                androidx.compose.animation.AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val spec = if (appAnimationMode == "Bouncy") {
                            spring<androidx.compose.ui.unit.IntOffset>(dampingRatio = 0.45f, stiffness = 200f)
                        } else if (appAnimationMode == "Dynamic" || appAnimationMode == "iOS") {
                            spring<androidx.compose.ui.unit.IntOffset>(dampingRatio = 0.75f, stiffness = 500f)
                        } else {
                            tween<androidx.compose.ui.unit.IntOffset>(300, easing = LinearOutSlowInEasing)
                        }
                        val scaleSpec = if (appAnimationMode == "Bouncy") {
                            spring<Float>(dampingRatio = 0.45f, stiffness = 200f)
                        } else if (appAnimationMode == "Dynamic" || appAnimationMode == "iOS") {
                            spring<Float>(dampingRatio = 0.75f, stiffness = 500f)
                        } else {
                            tween<Float>(300, easing = LinearOutSlowInEasing)
                        }
                        if (targetState > initialState) {
                            (androidx.compose.animation.slideInHorizontally(animationSpec = spec) { width -> width / 3 } + 
                             fadeIn(animationSpec = tween(220)) + 
                             scaleIn(initialScale = 0.95f, animationSpec = scaleSpec)).togetherWith(
                                androidx.compose.animation.slideOutHorizontally(animationSpec = spec) { width -> -width / 3 } + 
                                fadeOut(animationSpec = tween(220)) + 
                                scaleOut(targetScale = 0.95f, animationSpec = scaleSpec)
                            )
                        } else {
                            (androidx.compose.animation.slideInHorizontally(animationSpec = spec) { width -> -width / 3 } + 
                             fadeIn(animationSpec = tween(220)) + 
                             scaleIn(initialScale = 0.95f, animationSpec = scaleSpec)).togetherWith(
                                androidx.compose.animation.slideOutHorizontally(animationSpec = spec) { width -> width / 3 } + 
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
                            onEditCourse = { /* Handled in tab if hoisted, else ignore */ },
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

        FloatingCapsuleNavBar(
            tabs = navTabs,
            selectedTabId = selectedTab,
            onTabSelected = { viewModel.setSelectedDashboardTab(it) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
        )
        
        // Interactive Push and Pull Header for the main screens
        val titleText = when (selectedTab) {
            0 -> tabHomeLabel
            1 -> tabCoursesLabel
            2 -> tabSubjectsLabel
            3 -> tabSelfStudyLabel
            4 -> tabAnalyticsLabel
            5 -> tabCalendarLabel
            else -> stringResource(id = R.string.app_name)
        }

        Box(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            lumia.tracker.ui.components.InteractivePushPullHeader(
                title = titleText,
                viewModel = viewModel,
                navController = navController
            )
        }
    }
    if (showAddCourseDialog) {
        lumia.tracker.ui.screens.study.AddCourseDialog(
            viewModel = viewModel,
            onDismiss = { showAddCourseDialog = false }
        )
    }

    if (showAddSubjectDialog) {
        lumia.tracker.ui.screens.study.AddSubjectDialog(
            viewModel = viewModel,
            onDismiss = { showAddSubjectDialog = false }
        )
    }
}

fun getTabIcon(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName) {
        "Home" -> Icons.Rounded.Home
        "School" -> Icons.Rounded.School
        "Star" -> Icons.Rounded.Star
        "Person" -> Icons.Rounded.Person
        "List" -> Icons.Rounded.List
        
        "MenuBook" -> Icons.AutoMirrored.Rounded.MenuBook
        "Class" -> Icons.Rounded.Class
        "AutoStories" -> Icons.Rounded.AutoStories
        "Folder" -> Icons.Rounded.Folder
        
        "FolderOpen" -> Icons.Rounded.FolderOpen
        "Category" -> Icons.Rounded.Category
        
        "Timer" -> Icons.Rounded.Timer
        "History" -> Icons.Rounded.History
        "PlayArrow" -> Icons.Rounded.PlayArrow
        
        "CalendarMonth" -> Icons.Rounded.CalendarMonth
        "DateRange" -> Icons.Rounded.DateRange
        "Schedule" -> Icons.Rounded.Schedule
        
        "Analytics" -> Icons.Rounded.Analytics
        "CheckCircle" -> Icons.Rounded.CheckCircle
        
        else -> Icons.Rounded.Home
    }
}
