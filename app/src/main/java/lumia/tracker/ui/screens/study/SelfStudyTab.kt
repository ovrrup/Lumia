package lumia.tracker.ui.screens.study

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import lumia.tracker.ui.components.BouncyFloatingActionButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.screens.study.components.TaskItemCard
import lumia.tracker.ui.theme.bouncyScale
import lumia.tracker.viewmodel.ScholarViewModel
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

/**
 * SelfStudyTab - Dedicated Task & Study Goal Organizer.
 * Eliminates repetitive permission cards and duplicate timer shortcuts
 * in favor of a clean, drag-reorderable task planner.
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
                task.copy(orderIndex = index, priority = if (groupBy == "Priority") task.priority else task.priority)
            }
            viewModel.updateTasksOrder(updatedTasks)
        }
    }

    val upcomingAssignments = assignments.filter { !it.isCompleted && it.dueDateMillis > System.currentTimeMillis() }
    val pendingTasks = tasks.filter { !it.isCompleted && (it.dueDateMillis == null || it.dueDateMillis < System.currentTimeMillis()) }
    val futureTasks = tasks.filter { !it.isCompleted && it.dueDateMillis != null && it.dueDateMillis > System.currentTimeMillis() }

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
                top = bottomPadding.calculateTopPadding() + 16.dp,
                bottom = bottomPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Task Breakdown Metrics
            item(key = "task_metrics") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ScholarCard(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                upcomingAssignments.size.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Upcoming",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    ScholarCard(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                pendingTasks.size.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Pending",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    ScholarCard(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                futureTasks.size.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Scheduled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Tasks Section Title and Grouping Control
            item(key = "task_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Your Tasks (${localTasks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (advancedTasks && tasks.isNotEmpty()) {
                        var expandSort by remember { mutableStateOf(false) }
                        Box {
                            BouncyTextButton(onClick = { expandSort = true }) {
                                Text("Group: $groupBy")
                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = expandSort, onDismissRequest = { expandSort = false }) {
                                DropdownMenuItem(text = { Text("None") }, onClick = { groupBy = "None"; expandSort = false })
                                DropdownMenuItem(text = { Text("Tags") }, onClick = { groupBy = "Tags"; expandSort = false })
                                DropdownMenuItem(text = { Text("Priority") }, onClick = { groupBy = "Priority"; expandSort = false })
                            }
                        }
                    }
                }
            }

            if (localTasks.isEmpty()) {
                item(key = "empty_tasks") {
                    ScholarCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TaskAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("No active tasks", fontWeight = FontWeight.Bold)
                            Text(
                                "Tap the + button below to create your first study task.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                if (!advancedTasks || groupBy == "None") {
                    items(localTasks, key = { it.id }) { task ->
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
                    val grouped = localTasks.groupBy { if (it.tags.isBlank()) "Uncategorized" else it.tags.split(",")[0].trim() }
                    grouped.forEach { (tag, tTasks) ->
                        item(key = "tag_header_$tag") {
                            Text(
                                tag,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(top = 8.dp)
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
                    val grouped = localTasks.groupBy {
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
                                    pLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (pLabel.startsWith("High")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.padding(top = 8.dp)
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

    if (showAddTaskDialog) {
        lumia.tracker.ui.screens.study.dialogs.AddTaskDialog(
            viewModel = viewModel,
            onDismiss = { showAddTaskDialog = false }
        )
    }

    taskToEdit?.let { task ->
        lumia.tracker.ui.screens.study.dialogs.EditTaskDialog(
            task = task,
            viewModel = viewModel,
            onDismiss = { taskToEdit = null }
        )
    }
}
