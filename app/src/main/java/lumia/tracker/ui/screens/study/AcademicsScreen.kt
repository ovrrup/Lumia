package lumia.tracker.ui.screens.study

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.study.dialogs.AddCourseDialog
import lumia.tracker.ui.screens.study.dialogs.AddSubjectDialog
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
 * with a clean top segmented switcher and fluid microtransitions.
 */
@ValueScore(
    score = 96,
    importance = Importance.CRITICAL,
    description = "Unified Academics workspace unifying course enrollment and subject trees with clean segmented control",
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

    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()

    val layoutDirection = LocalLayoutDirection.current
    val academicsContentPadding = remember(bottomPadding, layoutDirection) {
        PaddingValues(
            start = bottomPadding.calculateStartPadding(layoutDirection),
            top = 0.dp,
            end = bottomPadding.calculateEndPadding(layoutDirection),
            bottom = bottomPadding.calculateBottomPadding()
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = bottomPadding.calculateTopPadding())
    ) {
        // Clean, Top-Centered Segmented Pill Switcher
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AcademicViewMode.entries.forEach { mode ->
                    val isSelected = activeView == mode
                    val count = if (mode == AcademicViewMode.COURSES) courses.size else subjects.size
                    val bg = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent
                    val textColor = if (isSelected) {
                        if (mode == AcademicViewMode.COURSES) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bg)
                            .clickable { activeView = mode }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = mode.icon,
                                contentDescription = mode.title,
                                modifier = Modifier.size(16.dp),
                                tint = textColor
                            )
                            Text(
                                text = "${mode.title} ($count)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }

        // Content Area with Smooth Horizontal Slide + Crossfade
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
                            bottomPadding = academicsContentPadding,
                            onEditCourse = { /* Handled within CoursesTab */ },
                            onAddCourseClick = { onAddCourseClick() }
                        )
                    }
                    AcademicViewMode.SUBJECTS -> {
                        SubjectsTab(
                            navController = navController,
                            viewModel = viewModel,
                            bottomPadding = academicsContentPadding,
                            onEditSubject = { /* Handled within SubjectsTab */ },
                            onAddSubjectClick = { onAddSubjectClick() }
                        )
                    }
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
