package lumia.tracker.ui.screens.study

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.Task
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyFloatingActionButton
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.study.components.MetricSummaryTile
import lumia.tracker.ui.screens.study.components.TaskItemCard
import lumia.tracker.ui.screens.study.dialogs.AddTaskDialog
import lumia.tracker.ui.theme.bouncyScale
import lumia.tracker.viewmodel.ScholarViewModel
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

enum class TaskFilterTab {
    ALL, IN_PROGRESS, COMPLETED
}

/**
 * SelfStudyTab - Dedicated Task & Study Goal Organizer with Clean Filter Tabs,
 * Priority Filtering, Drag-Reorderable Task List, and Metric Cards.
 */
@ValueScore(
    score = 90,
    importance = Importance.CRITICAL,
    description = "Self study task dashboard with filtering, drag-reorder, and hero metrics",
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

    var localTasks by remember(tasks) { mutableStateOf(tasks) }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(
        listState = listState,
        onMove = { from, to ->
            localTasks = localTasks.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        },
        canDragOver = { draggedOver, _ -> localTasks.any { it.id == draggedOver.key } }
    )

    // Save when drag finishes
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
    val futureTasks = tasks.filter { !it.isCompleted && it.dueDateMillis != null && it.dueDateMillis > System.currentTimeMillis() }

    // Filtered tasks based on active filter tab and priority chip
    val filteredTasks = remember(localTasks, selectedFilterTab, selectedPriorityFilter) {
        localTasks.filter { task ->
            val matchesTab = when (selectedFilterTab) {
                TaskFilterTab.ALL -> true
                TaskFilterTab.IN_PROGRESS -> !task.isCompleted
                TaskFilterTab.COMPLETED -> task.isCompleted
            }
            val matchesPriority = selectedPriorityFilter == null || task.priority == selectedPriorityFilter
            matchesTab && matchesPriority
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            val src = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            BouncyFloatingActionButton(
                onClick = { showAddTaskDialog = true },
                interactionSource = src,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .padding(bottom = bottomPadding.calculateBottomPadding())
                    .bouncyScale(src)
            ) {
                Icon(Icons.Rounded.AddTask, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .reorderable(reorderableState),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = bottomPadding.calculateTopPadding() + 12.dp,
                bottom = bottomPadding.calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Task Breakdown Metrics Hero Row
            item(key = "task_metrics") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Upcoming Assignments
                    MetricSummaryTile(
                        value = upcomingAssignments.size.toString(),
                        label = "Upcoming",
                        valueColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )

                    // Pending Tasks
                    MetricSummaryTile(
                        value = pendingTasks.size.toString(),
                        label = "Pending",
                        valueColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )

                    // Scheduled Tasks
                    MetricSummaryTile(
                        value = futureTasks.size.toString(),
                        label = "Scheduled",
                        valueColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Filter Tabs Segmented Bar
            item(key = "filter_tabs_bar") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
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
                            TaskFilterTab.IN_PROGRESS to "Active (${pendingTasks.size})",
                            TaskFilterTab.COMPLETED to "Done (${completedTasks.size})"
                        ).forEach { (tab, label) ->
                            val isSelected = selectedFilterTab == tab
                            val bg = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent
                            val textColor = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bg)
                                    .clickable { selectedFilterTab = tab }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }

            // Priority Filter Chips and Grouping Control
            item(key = "priority_chips_row") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority filter chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            null to "All",
                            2 to "High",
                            1 to "Medium"
                        ).forEach { (priority, label) ->
                            val isSelected = selectedPriorityFilter == priority
                            val activeColor = when (priority) {
                                2 -> MaterialTheme.colorScheme.error
                                1 -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.primary
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) activeColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier.clickable {
                                    selectedPriorityFilter = if (isSelected && priority != null) null else priority
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
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
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier.clickable { expandSort = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
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

            // Task Items / Empty State
            if (filteredTasks.isEmpty()) {
                item(key = "empty_filtered_tasks") {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (selectedFilterTab == TaskFilterTab.COMPLETED) Icons.Rounded.CheckCircleOutline else Icons.Rounded.TaskAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = when (selectedFilterTab) {
                                    TaskFilterTab.IN_PROGRESS -> "All caught up"
                                    TaskFilterTab.COMPLETED -> "No completed tasks"
                                    TaskFilterTab.ALL -> "No tasks yet"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = when (selectedFilterTab) {
                                    TaskFilterTab.IN_PROGRESS -> "No active study tasks remaining."
                                    TaskFilterTab.COMPLETED -> "Completed tasks will appear here."
                                    TaskFilterTab.ALL -> "Add a task to start tracking your study goals."
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
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Create Task", fontWeight = FontWeight.SemiBold)
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

