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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.Task
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyFloatingActionButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.ScholarCard
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Task Breakdown Metrics Hero Row
            item(key = "task_metrics") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Upcoming Assignments
                    ScholarCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = upcomingAssignments.size.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Upcoming",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Pending Tasks
                    ScholarCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = pendingTasks.size.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Pending",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Scheduled / Completed Tasks
                    ScholarCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = futureTasks.size.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Scheduled",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Filter Tabs Segmented Bar
            item(key = "filter_tabs_bar") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
                            val bg = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                            val textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(bg, RoundedCornerShape(10.dp))
                                    .clickable { selectedFilterTab = tab }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
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
                        FilterChip(
                            selected = selectedPriorityFilter == null,
                            onClick = { selectedPriorityFilter = null },
                            label = { Text("All", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = selectedPriorityFilter == 2,
                            onClick = { selectedPriorityFilter = if (selectedPriorityFilter == 2) null else 2 },
                            label = { Text("High", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.error, CircleShape))
                            }
                        )
                        FilterChip(
                            selected = selectedPriorityFilter == 1,
                            onClick = { selectedPriorityFilter = if (selectedPriorityFilter == 1) null else 1 },
                            label = { Text("Medium", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.secondary, CircleShape))
                            }
                        )
                    }

                    // Grouping dropdown (when advanced tasks enabled)
                    if (advancedTasks && tasks.isNotEmpty()) {
                        var expandSort by remember { mutableStateOf(false) }
                        Box {
                            BouncyTextButton(onClick = { expandSort = true }) {
                                Text(
                                    "Group: $groupBy",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = expandSort,
                                onDismissRequest = { expandSort = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None") },
                                    onClick = { groupBy = "None"; expandSort = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Tags") },
                                    onClick = { groupBy = "Tags"; expandSort = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Priority") },
                                    onClick = { groupBy = "Priority"; expandSort = false }
                                )
                            }
                        }
                    }
                }
            }

            // Task Items / Empty State
            if (filteredTasks.isEmpty()) {
                item(key = "empty_filtered_tasks") {
                    ScholarCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (selectedFilterTab == TaskFilterTab.COMPLETED) Icons.Rounded.CheckCircleOutline else Icons.Rounded.TaskAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = when (selectedFilterTab) {
                                    TaskFilterTab.IN_PROGRESS -> "All caught up! 🎉"
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
                                    TaskFilterTab.IN_PROGRESS -> "You have no active study tasks remaining."
                                    TaskFilterTab.COMPLETED -> "Complete your active tasks to see them logged here."
                                    TaskFilterTab.ALL -> "Tap the + button below to create your first study task."
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
                                    )
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Create Task", fontWeight = FontWeight.Bold)
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
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                            )
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
                                Text(
                                    text = pLabel,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pLabel.startsWith("High")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                                )
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
