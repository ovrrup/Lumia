package lumia.tracker.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import lumia.tracker.ui.theme.animateItemEntry
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import lumia.tracker.ui.theme.glassBar
import lumia.tracker.ui.theme.bouncyScale
import androidx.compose.material.icons.rounded.Add
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.Course
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.BouncyFloatingActionButton
import lumia.tracker.ui.components.GlassCard
import lumia.tracker.viewmodel.ScholarViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun CoursesTab(
    navController: NavController,
    viewModel: ScholarViewModel,
    bottomPadding: PaddingValues,
    onEditCourse: (Course) -> Unit,
    onAddCourseClick: () -> Unit
) {
    var courseToEdit by remember { mutableStateOf<lumia.tracker.model.Course?>(null) }
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        floatingActionButton = {
            val src = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            lumia.tracker.ui.components.BouncyFloatingActionButton(
                onClick = onAddCourseClick,
                interactionSource = src,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = bottomPadding.calculateBottomPadding()).bouncyScale(src)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Course")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, 
                top = bottomPadding.calculateTopPadding() + 16.dp, bottom = bottomPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (courses.isEmpty()) {
                item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "No courses yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        } else {
            itemsIndexed(courses, key = { _, course -> course.id }) { index, course ->
                Box(modifier = Modifier.animateItemEntry(index)) {
                    lumia.tracker.ui.screens.study.CourseItemCard(
                        course = course,
                        onClick = { navController.navigate("courseDetail/${course.id}") },
                        onEdit = { courseToEdit = course },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
    }
    if (courseToEdit != null) {
        lumia.tracker.ui.screens.study.EditCourseDialog(
            course = courseToEdit!!,
            viewModel = viewModel,
            onDismiss = { courseToEdit = null }
        )
    }
}
