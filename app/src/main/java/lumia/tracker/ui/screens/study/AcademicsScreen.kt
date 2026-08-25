package lumia.tracker.ui.screens.study

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.study.dialogs.AddCourseDialog
import lumia.tracker.ui.screens.study.dialogs.AddSubjectDialog
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * AcademicViewMode - Defines the active view subsection in the Academics hub.
 */
enum class AcademicViewMode(val title: String, val icon: ImageVector) {
    COURSES("Courses", Icons.AutoMirrored.Rounded.MenuBook),
    SUBJECTS("Subjects", Icons.Rounded.FolderOpen)
}

/**
 * AcademicsScreen - Unified academic management hub hosting Courses and Subjects
 * with a floating top-right capsule view switcher and fluid AnimatedContent transitions.
 */
@ValueScore(
    score = 96,
    importance = Importance.CRITICAL,
    description = "Unified Academics workspace unifying course enrollment and subject syllabus trees with a floating capsule view switcher",
    category = "Study"
)
@Composable
fun AcademicsScreen(
    navController: NavController,
    viewModel: ScholarViewModel,
    bottomPadding: PaddingValues,
    onAddCourseClick: () -> Unit = {},
    onAddSubjectClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var activeView by rememberSaveable { mutableStateOf(AcademicViewMode.COURSES) }
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }

    val layoutDirection = LocalLayoutDirection.current
    val academicsAdjustedPadding = remember(bottomPadding, layoutDirection) {
        PaddingValues(
            start = bottomPadding.calculateStartPadding(layoutDirection),
            top = bottomPadding.calculateTopPadding() + 48.dp,
            end = bottomPadding.calculateEndPadding(layoutDirection),
            bottom = bottomPadding.calculateBottomPadding()
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Content Area with Smooth Horizontal Slide + Crossfade
        AnimatedContent(
            targetState = activeView,
            transitionSpec = {
                val isForward = targetState.ordinal > initialState.ordinal
                val direction = if (isForward) 1 else -1
                (slideInHorizontally(
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    initialOffsetX = { fullWidth -> (fullWidth * 0.12f * direction).toInt() }
                ) + fadeIn(animationSpec = tween(200, easing = LinearOutSlowInEasing))).togetherWith(
                    slideOutHorizontally(
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                        targetOffsetX = { fullWidth -> (-fullWidth * 0.12f * direction).toInt() }
                    ) + fadeOut(animationSpec = tween(200, easing = FastOutLinearInEasing))
                )
            },
            label = "AcademicsContentTransition",
            modifier = Modifier.fillMaxSize()
        ) { currentView ->
            when (currentView) {
                AcademicViewMode.COURSES -> {
                    CoursesTab(
                        navController = navController,
                        viewModel = viewModel,
                        bottomPadding = academicsAdjustedPadding,
                        onEditCourse = { /* Handled within CoursesTab */ },
                        onAddCourseClick = {
                            onAddCourseClick()
                        }
                    )
                }
                AcademicViewMode.SUBJECTS -> {
                    SubjectsTab(
                        navController = navController,
                        viewModel = viewModel,
                        bottomPadding = academicsAdjustedPadding,
                        onEditSubject = { /* Handled within SubjectsTab */ },
                        onAddSubjectClick = {
                            onAddSubjectClick()
                        }
                    )
                }
            }
        }

        // 2. Floating Top-Right Capsule View Switcher (zero-clipping, elevated overlay)
        AcademicViewCapsuleSwitcher(
            activeView = activeView,
            onSelectView = { activeView = it },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = bottomPadding.calculateTopPadding() + 8.dp, end = 16.dp)
                .zIndex(20f)
        )
    }

    // Modal dialog triggers when triggered internally
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

/**
 * AcademicViewCapsuleSwitcher - Dynamic floating capsule enabling rapid, one-tap toggling
 * between university courses and subject directories with zero clipping and tactile micro-interactions.
 */
@ValueScore(
    score = 92,
    importance = Importance.HIGH,
    description = "Floating glassmorphic pill switcher with spring physics and bouncy scale micro-interactions",
    category = "Navigation"
)
@Composable
fun AcademicViewCapsuleSwitcher(
    activeView: AcademicViewMode,
    onSelectView: (AcademicViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 6.dp,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            AcademicViewMode.entries.forEach { mode ->
                val isSelected = activeView == mode
                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        if (mode == AcademicViewMode.COURSES) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.tertiaryContainer
                        }
                    } else {
                        Color.Transparent
                    },
                    animationSpec = tween(200),
                    label = "capsule_bg_${mode.name}"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        if (mode == AcademicViewMode.COURSES) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        }
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    },
                    animationSpec = tween(200),
                    label = "capsule_fg_${mode.name}"
                )
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.0f else 0.96f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                    label = "capsule_scale_${mode.name}"
                )

                Surface(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(CircleShape)
                        .bouncyClick { onSelectView(mode) },
                    shape = CircleShape,
                    color = containerColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = mode.icon,
                            contentDescription = mode.title,
                            tint = contentColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = mode.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
