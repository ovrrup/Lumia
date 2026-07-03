package lumia.tracker.ui.screens

import lumia.tracker.ui.theme.liquidGlass
import lumia.tracker.ui.theme.glassBar
import lumia.tracker.ui.theme.navGlassBar
import lumia.tracker.ui.theme.glassPill
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.togetherWith
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
                            import lumia.tracker.ui.theme.bouncyClick
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// ... the rest of the imports ...
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.R
import lumia.tracker.viewmodel.ScholarViewModel
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.BouncyOutlinedButton
import lumia.tracker.ui.components.BouncyFloatingActionButton
import lumia.tracker.ui.components.GlassCard
import lumia.tracker.ui.components.GlassHeroCard
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.border

import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: ScholarViewModel) {
    val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current
    var selectedTab by remember { mutableStateOf(0) }
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
    
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }

    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                val titleText = when (selectedTab) {
                    0 -> stringResource(id = R.string.app_name)
                    1 -> "Your Courses"
                    2 -> "Your Subjects"
                    3 -> "Self Study & Tasks"
                    else -> "Analytics"
                }
                androidx.compose.foundation.layout.Box {
                    if (betaEnhancedHeader || isGlass) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .matchParentSize()
                                .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                        )
                        // Sleek divider line for clean separation and anchoring
                        androidx.compose.material3.HorizontalDivider(
                            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    }
                    CenterAlignedTopAppBar(
                        title = { Text(titleText, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                        actions = {
                            val moreRounds = lumia.tracker.ui.theme.LocalMoreRounds.current
                            val moreRoundsMode = lumia.tracker.ui.theme.LocalMoreRoundsMode.current
                            lumia.tracker.ui.components.StreakWidget(viewModel, navController)
                            val isMrGlass = moreRounds && moreRoundsMode == "Glass"
                            val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(44.dp)
                                    .then(if (isMrGlass) Modifier else Modifier.shadow(elevation = 8.dp, shape = CircleShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)))
                                    .then(if (isMrGlass) Modifier.liquidGlass(CircleShape, tintAlpha = 0.25f) else Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape))
                                    .clip(CircleShape)
                                    .bouncyClick(
                                        onClick = { navController.navigate("profile_menu") }
                                    ),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                val isLocalImage = activeProfile.avatarEmoji.startsWith("/") || activeProfile.avatarEmoji.startsWith("file://") || activeProfile.avatarEmoji.startsWith("content://")
                                if (isLocalImage) {
                                    coil.compose.AsyncImage(
                                        model = activeProfile.avatarEmoji,
                                        contentDescription = "Profile Picture",
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    val fallback = if (activeProfile.avatarEmoji.isNotBlank() && activeProfile.avatarEmoji.length <= 2 && activeProfile.avatarEmoji != "A" && activeProfile.avatarEmoji != "U") {
                                        activeProfile.avatarEmoji.uppercase()
                                    } else {
                                        activeProfile.name.take(2).uppercase()
                                    }
                                    Text(
                                        text = fallback,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isMrGlass) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            scrolledContainerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp).copy(alpha = 0.5f)
                        )
                    )
                }
            },
            bottomBar = {
                if (!betaFloatingNav) {
                    val useGlass = isGlass || navBarGlassForceEnabled
                    val navItemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = navBarIndicatorAlpha)
                    )
                    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    NavigationBar(
                        modifier = Modifier
                            .height(navBarHeight.dp + bottomInset)
                            .then(if (useGlass) Modifier.navGlassBar(androidx.compose.foundation.shape.RoundedCornerShape(topStart = navBarCornerRadius.dp, topEnd = navBarCornerRadius.dp)) else Modifier),
                        containerColor = if (useGlass) MaterialTheme.colorScheme.surface.copy(alpha = 0.92f) else MaterialTheme.colorScheme.surface,
                    ) {
                        val labelModeAlways = navBarLabelMode == "Always"
                        val hideLabels = navBarLabelMode == "Hidden"
                        
                        NavigationBarItem(
                            icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                            label = if (hideLabels) null else { { Text("Home") } },
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            colors = navItemColors,
                            alwaysShowLabel = labelModeAlways
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = "Courses") },
                            label = if (hideLabels) null else { { Text("Courses") } },
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            colors = navItemColors,
                            alwaysShowLabel = labelModeAlways
                        )
                        if (featureSelfStudyEnabled) {
                            NavigationBarItem(
                                icon = { Icon(Icons.Rounded.AutoStories, contentDescription = "Tasks") },
                                label = if (hideLabels) null else { { Text("Tasks") } },
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                colors = navItemColors,
                                alwaysShowLabel = labelModeAlways
                            )
                        }
                        if (featureAnalyticsEnabled) {
                            NavigationBarItem(
                                icon = { Icon(Icons.Rounded.Analytics, contentDescription = "Analytics") },
                                label = if (hideLabels) null else { { Text("Analytics") } },
                                selected = selectedTab == 4,
                                onClick = { selectedTab = 4 },
                                colors = navItemColors,
                                alwaysShowLabel = labelModeAlways
                            )
                        }
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
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
                    .clipToBounds()
            ) {
                val appAnimationMode = lumia.tracker.ui.theme.LocalAppAnimationMode.current
                androidx.compose.animation.AnimatedContent(
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
                            onNavigateToTasks = { selectedTab = 3 }
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

        if (betaFloatingNav) {
            val useGlass = isGlass || navBarGlassForceEnabled
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomCenter)
                    .padding(
                        start = navBarPaddingHorizontal.dp,
                        end = navBarPaddingHorizontal.dp,
                        bottom = navBarPaddingBottom.dp
                    )
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .then(if (useGlass) Modifier.glassPill(androidx.compose.foundation.shape.RoundedCornerShape(navBarCornerRadius.dp)) else Modifier),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(navBarCornerRadius.dp),
                color = if (useGlass) MaterialTheme.colorScheme.surface.copy(alpha = 0.92f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                contentColor = if (useGlass) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer,
                shadowElevation = if (useGlass) 0.dp else 8.dp,
                tonalElevation = if (useGlass) 0.dp else 4.dp
            ) {
                val navItemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (useGlass) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = if (useGlass) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedIconColor = if (useGlass) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.70f),
                    unselectedTextColor = if (useGlass) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                    indicatorColor = if (useGlass) MaterialTheme.colorScheme.primary.copy(alpha = navBarIndicatorAlpha) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = navBarIndicatorAlpha)
                )
                NavigationBar(
                    modifier = Modifier.fillMaxWidth().height(navBarHeight.dp),
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0, 0, 0, 0)
                ) {
                    val labelModeAlways = navBarLabelMode == "Always"
                    val hideLabels = navBarLabelMode == "Hidden"

                    NavigationBarItem(
                        icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                        label = if (hideLabels) null else { { Text("Home") } },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        colors = navItemColors,
                        alwaysShowLabel = labelModeAlways
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = "Courses") },
                        label = if (hideLabels) null else { { Text("Courses") } },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        colors = navItemColors,
                        alwaysShowLabel = labelModeAlways
                    )
                    if (featureSubjectEnabled && !fuseSubjectsCourses) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Rounded.FolderOpen, contentDescription = "Subjects") },
                            label = if (hideLabels) null else { { Text("Subjects") } },
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            colors = navItemColors,
                            alwaysShowLabel = labelModeAlways
                        )
                    }
                    if (featureSelfStudyEnabled) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Rounded.AutoStories, contentDescription = "Self Study") },
                            label = if (hideLabels) null else { { Text("Self Study") } },
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            colors = navItemColors,
                            alwaysShowLabel = labelModeAlways
                        )
                    }
                    if (featureAnalyticsEnabled) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Rounded.Analytics, contentDescription = "Analytics") },
                            label = if (hideLabels) null else { { Text("Analytics") } },
                            selected = selectedTab == 4,
                            onClick = { selectedTab = 4 },
                            colors = navItemColors,
                            alwaysShowLabel = labelModeAlways
                        )
                    }
                }
            }
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
