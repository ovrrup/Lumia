package lumia.tracker.ui.screens.study

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.Task
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyFloatingActionButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.components.ScholarCardDefaults
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.study.components.TaskItemCard
import lumia.tracker.ui.screens.study.dialogs.AddTaskDialog
import lumia.tracker.ui.util.getTagColors
import lumia.tracker.viewmodel.ScholarViewModel
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

enum class TaskFilterTab {
    ALL, IN_PROGRESS, COMPLETED
}

/**
 * SelfStudyTab - Reimagined Task & Study Workspace with Soft Ambient Styling,
 * Progress Momentum Hero, Capsule Segmented Controls, and Fluid Reordering.
 */
@ValueScore(
    score = 96,
    importance = Importance.CRITICAL,
    description = "Reimagined ambient task workspace with velocity progress ring, capsule filter tabs, and tactile item cards",
    category = "Study"
)
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SelfStudyTab(
    navController: NavController,
    viewModel: ScholarViewModel,
    bottomPadding: PaddingValues
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val advancedTasks by viewModel.systemAdvancedTasks.collectAsStateWithLifecycle()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var groupBy by remember { mutableStateOf("None") }
    var selectedFilterTab by remember { mutableStateOf(TaskFilterTab.ALL) }
    var selectedPriorityFilter by remember { mutableStateOf<Int?>(null) }
    var selectedTagFilter by remember { mutableStateOf<String?>(null) }

    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current

    var localTasks by remember(tasks) { mutableStateOf(tasks) }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(
        listState = listState,
        onMove = { from, to ->
            val fromPos = localTasks.indexOfFirst { it.id == from.key }
            val toPos = localTasks.indexOfFirst { it.id == to.key }
            if (fromPos >= 0 && toPos >= 0) {
                localTasks = localTasks.toMutableList().apply {
                    add(toPos, removeAt(fromPos))
                }
            }
        },
        canDragOver = { draggedOver, _ -> localTasks.any { it.id == draggedOver.key } }
    )

    // Sync order changes when drag completes
    LaunchedEffect(reorderableState.draggingItemKey) {
        if (reorderableState.draggingItemKey == null && localTasks != tasks) {
            val updatedTasks = localTasks.mapIndexed { index, task ->
                task.copy(orderIndex = index)
            }
            viewModel.updateTasksOrder(updatedTasks)
        }
    }

    val upcomingAssignments = assignments.filter { !it.isCompleted && it.dueDateMillis > System.currentTimeMillis() }
    val pendingTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }
    val totalCount = tasks.size
    val completedCount = completedTasks.size
    val completionRatio = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    // Collect distinct tags with counts for inline tag filtering
    val allTagsWithCounts = remember(localTasks) {
        localTasks.flatMap { it.tags.split(",") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
    }

    // Filtered tasks based on active filter tab, priority chip, and selected tag
    val filteredTasks = remember(localTasks, selectedFilterTab, selectedPriorityFilter, selectedTagFilter) {
        localTasks.filter { task ->
            val matchesTab = when (selectedFilterTab) {
                TaskFilterTab.ALL -> true
                TaskFilterTab.IN_PROGRESS -> !task.isCompleted
                TaskFilterTab.COMPLETED -> task.isCompleted
            }
            val matchesPriority = selectedPriorityFilter == null || task.priority == selectedPriorityFilter
            val matchesTag = selectedTagFilter == null || task.tags.split(",").map { it.trim() }.contains(selectedTagFilter)
            matchesTab && matchesPriority && matchesTag
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .reorderable(reorderableState),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = bottomPadding.calculateBottomPadding() + 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Ambient Task Momentum Hero Card
            item(key = "task_momentum_hero") {
                val animatedRatio by animateFloatAsState(
                    targetValue = completionRatio,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                    label = "completionRatioAnim"
                )

                ScholarCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerLow
                                    else MaterialTheme.colorScheme.surfaceContainerLowest,
                    border = ScholarCardDefaults.border()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.TaskAlt,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "Task Momentum",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                val subtitleText = when {
                                    totalCount == 0 -> "No tasks active. Add one below!"
                                    completedCount == totalCount -> "All caught up! Great work."
                                    else -> "$completedCount of $totalCount tasks completed"
                                }
                                Text(
                                    text = subtitleText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Circular Progress Ring Indicator
                            Box(
                                modifier = Modifier.size(62.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { 1f },
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    strokeWidth = 6.dp,
                                    trackColor = Color.Transparent
                                )
                                CircularProgressIndicator(
                                    progress = { animatedRatio },
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 6.dp,
                                    trackColor = Color.Transparent
                                )
                                Text(
                                    text = "${(animatedRatio * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Summary Pills Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // To-Do Pill
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${pendingTasks.size}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "To-Do",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Done Pill
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${completedTasks.size}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Done",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Due Soon Pill
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${upcomingAssignments.size}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Due Soon",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Capsule Segmented Filter Tab Bar (Matches AcademicsScreen)
            item(key = "filter_tabs_bar") {
                val tabBg = if (isDark) MaterialTheme.colorScheme.surfaceContainerLow
                            else MaterialTheme.colorScheme.surfaceContainerLowest

                Surface(
                    shape = CircleShape,
                    color = tabBg,
                    border = ScholarCardDefaults.border(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            TaskFilterTab.ALL to "All (${tasks.size})",
                            TaskFilterTab.IN_PROGRESS to "To-Do (${pendingTasks.size})",
                            TaskFilterTab.COMPLETED to "Done (${completedTasks.size})"
                        ).forEach { (tab, label) ->
                            val isSelected = selectedFilterTab == tab
                            val activeBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.60f)
                                           else Color.Transparent
                            val activeColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                              else MaterialTheme.colorScheme.onSurfaceVariant

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(activeBg)
                                    .clickable {
                                        if (selectedFilterTab != tab) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedFilterTab = tab
                                        }
                                    }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = activeColor
                                )
                            }
                        }
                    }
                }
            }

            // 3. Ambient Quick-Add Task Capsule Bar
            item(key = "quick_add_task_bar") {
                var quickTaskTitle by remember { mutableStateOf("") }
                val focusManager = LocalFocusManager.current
                val inputBg = if (isDark) MaterialTheme.colorScheme.surfaceContainerLow
                              else MaterialTheme.colorScheme.surfaceContainerLowest

                Surface(
                    shape = CircleShape,
                    color = inputBg,
                    border = ScholarCardDefaults.border(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        BasicTextField(
                            value = quickTaskTitle,
                            onValueChange = { quickTaskTitle = it },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (quickTaskTitle.isNotBlank()) {
                                    viewModel.addTask(Task(title = quickTaskTitle.trim()))
                                    quickTaskTitle = ""
                                    focusManager.clearFocus()
                                }
                            }),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (quickTaskTitle.isEmpty()) {
                                    Text(
                                        text = "Quick add a task...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                    )
                                }
                                innerTextField()
                            }
                        )

                        if (quickTaskTitle.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    viewModel.addTask(Task(title = quickTaskTitle.trim()))
                                    quickTaskTitle = ""
                                    focusManager.clearFocus()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Send,
                                    contentDescription = "Add",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Priority & Tag Filter Chips Row
            item(key = "filter_chips_row") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority filter capsule chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            null to "All Priority",
                            2 to "High",
                            1 to "Medium"
                        ).forEach { (priority, label) ->
                            val isSelected = selectedPriorityFilter == priority
                            val activeColor = when (priority) {
                                2 -> MaterialTheme.colorScheme.error
                                1 -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.primary
                            }
                            val chipBg = if (isSelected) activeColor.copy(alpha = 0.15f)
                                         else if (isDark) MaterialTheme.colorScheme.surfaceContainerLow
                                         else MaterialTheme.colorScheme.surfaceContainerLowest

                            Surface(
                                shape = CircleShape,
                                color = chipBg,
                                border = if (isSelected) BorderStroke(1.dp, activeColor.copy(alpha = 0.40f))
                                         else ScholarCardDefaults.border(),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        selectedPriorityFilter = if (isSelected && priority != null) null else priority
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    if (priority != null) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(activeColor, CircleShape)
                                        )
                                    }
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Grouping dropdown (when advanced tasks enabled)
                    if (advancedTasks && tasks.isNotEmpty()) {
                        var expandSort by remember { mutableStateOf(false) }
                        Box {
                            Surface(
                                shape = CircleShape,
                                color = if (isDark) MaterialTheme.colorScheme.surfaceContainerLow
                                        else MaterialTheme.colorScheme.surfaceContainerLowest,
                                border = ScholarCardDefaults.border(),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { expandSort = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Group: $groupBy",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Icon(
                                        Icons.Rounded.ArrowDropDown,
                                        contentDescription = "Group",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = expandSort,
                                onDismissRequest = { expandSort = false }
                            ) {
                                listOf("None", "Tags", "Priority").forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                fontWeight = if (groupBy == option) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            groupBy = option
                                            expandSort = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Inline Tag Filter Pills (CircleShape capsules)
            if (allTagsWithCounts.isNotEmpty()) {
                item(key = "inline_tag_filter_row") {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item(key = "tag_filter_all") {
                            val isAllSelected = selectedTagFilter == null
                            Surface(
                                shape = CircleShape,
                                color = if (isAllSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f)
                                        else if (isDark) MaterialTheme.colorScheme.surfaceContainerLow
                                        else MaterialTheme.colorScheme.surfaceContainerLowest,
                                border = if (isAllSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.40f))
                                         else ScholarCardDefaults.border(),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { selectedTagFilter = null }
                            ) {
                                Text(
                                    text = "All Tags",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isAllSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                )
                            }
                        }

                        items(allTagsWithCounts, key = { it.first }) { (tag, count) ->
                            val isSelected = selectedTagFilter == tag
                            val (tagBg, tagText) = getTagColors(tag)
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) tagBg.copy(alpha = 0.35f)
                                        else if (isDark) MaterialTheme.colorScheme.surfaceContainerLow
                                        else MaterialTheme.colorScheme.surfaceContainerLowest,
                                border = if (isSelected) BorderStroke(1.dp, tagText.copy(alpha = 0.45f))
                                         else ScholarCardDefaults.border(),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        selectedTagFilter = if (isSelected) null else tag
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "#$tag",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) tagText else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "($count)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = (if (isSelected) tagText else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.65f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Task Items or Ambient Empty State
            if (filteredTasks.isEmpty()) {
                item(key = "empty_filtered_tasks") {
                    ScholarCard(
                        shape = RoundedCornerShape(24.dp),
                        containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerLow
                                        else MaterialTheme.colorScheme.surfaceContainerLowest,
                        border = ScholarCardDefaults.border(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (selectedFilterTab == TaskFilterTab.COMPLETED) Icons.Rounded.CheckCircleOutline else Icons.Rounded.TaskAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = when (selectedFilterTab) {
                                    TaskFilterTab.IN_PROGRESS -> "All caught up!"
                                    TaskFilterTab.COMPLETED -> "No completed tasks yet"
                                    TaskFilterTab.ALL -> "No tasks created yet"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = when (selectedFilterTab) {
                                    TaskFilterTab.IN_PROGRESS -> "You're all done with your current to-do items."
                                    TaskFilterTab.COMPLETED -> "Tasks checked off will appear here."
                                    TaskFilterTab.ALL -> "Add a task to start tracking what you need to study."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            if (selectedFilterTab == TaskFilterTab.ALL) {
                                Spacer(Modifier.height(16.dp))
                                BouncyButton(
                                    onClick = { showAddTaskDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    shape = CircleShape
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Add First Task", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            } else {
                if (!advancedTasks || groupBy == "None") {
                    items(filteredTasks, key = { it.id }) { task ->
                        ReorderableItem(reorderableState, key = task.id) { isDragging ->
                            TaskItemCard(
                                task = task,
                                viewModel = viewModel,
                                onEdit = { taskToEdit = task },
                                modifier = Modifier.detectReorderAfterLongPress(reorderableState),
                                navController = navController
                            )
                        }
                    }
                } else if (groupBy == "Tags") {
                    val grouped = filteredTasks.groupBy {
                        if (it.tags.isBlank()) "Uncategorized" else it.tags.split(",")[0].trim()
                    }
                    grouped.forEach { (tag, tTasks) ->
                        item(key = "tag_header_$tag") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalOffer,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "(${tTasks.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        items(tTasks, key = { it.id }) { task ->
                            TaskItemCard(
                                task = task,
                                viewModel = viewModel,
                                onEdit = { taskToEdit = task },
                                navController = navController
                            )
                        }
                    }
                } else if (groupBy == "Priority") {
                    val grouped = filteredTasks.groupBy {
                        when (it.priority) {
                            2 -> "High Priority"
                            1 -> "Medium Priority"
                            else -> "Low Priority"
                        }
                    }
                    listOf("High Priority", "Medium Priority", "Low Priority").forEach { pLabel ->
                        val tTasks = grouped[pLabel]
                        if (!tTasks.isNullOrEmpty()) {
                            item(key = "priority_header_$pLabel") {
                                val pColor = when {
                                    pLabel.startsWith("High") -> MaterialTheme.colorScheme.error
                                    pLabel.startsWith("Medium") -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(pColor, CircleShape)
                                    )
                                    Text(
                                        text = pLabel,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "(${tTasks.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            items(tTasks, key = { it.id }) { task ->
                                TaskItemCard(
                                    task = task,
                                    viewModel = viewModel,
                                    onEdit = { taskToEdit = task },
                                    navController = navController
                                )
                            }
                        }
                    }
                }
            }
        }

        BouncyFloatingActionButton(
            onClick = { showAddTaskDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = bottomPadding.calculateBottomPadding() + 16.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add Task", modifier = Modifier.size(24.dp))
        }
    }

    if (showAddTaskDialog || taskToEdit != null) {
        AddTaskDialog(
            viewModel = viewModel,
            taskToEdit = taskToEdit,
            onDismiss = {
                showAddTaskDialog = false
                taskToEdit = null
            }
        )
    }
}
