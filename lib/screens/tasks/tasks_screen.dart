import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/constants/app_constants.dart';
import '../../core/utils/date_utils.dart';
import '../../data/database/database.dart';
import '../../providers/courses_provider.dart';
import '../../providers/database_provider.dart';
import '../../providers/tasks_provider.dart';
import '../../widgets/adroit_card.dart';
import '../../widgets/empty_state.dart';
import '../../widgets/status_badge.dart';
import 'edit_task_dialog.dart';

class TasksScreen extends ConsumerWidget {
  const TasksScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final activeFilter = ref.watch(taskFilterProvider);
    final filteredTasks = ref.watch(filteredTasksProvider);
    final allCourses = ref.watch(allCoursesProvider).value ?? [];

    final courseMap = {for (final c in allCourses) c.id: c};

    return Scaffold(
      appBar: AppBar(
        title: const Text('Tasks & Deadlines'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add_rounded),
            tooltip: 'Add Task',
            onPressed: () => showDialog(
              context: context,
              builder: (ctx) => const EditTaskDialog(),
            ),
          ),
        ],
      ),
      body: Column(
        children: [
          // Filter Chips
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: Row(
              children: [
                _TaskFilterChip(
                  label: 'Upcoming',
                  filter: TaskFilter.upcoming,
                  current: activeFilter,
                  onSelect: () =>
                      ref.read(taskFilterProvider.notifier).state = TaskFilter.upcoming,
                ),
                const SizedBox(width: 8),
                _TaskFilterChip(
                  label: 'Overdue',
                  filter: TaskFilter.overdue,
                  current: activeFilter,
                  onSelect: () =>
                      ref.read(taskFilterProvider.notifier).state = TaskFilter.overdue,
                ),
                const SizedBox(width: 8),
                _TaskFilterChip(
                  label: 'All Tasks',
                  filter: TaskFilter.all,
                  current: activeFilter,
                  onSelect: () =>
                      ref.read(taskFilterProvider.notifier).state = TaskFilter.all,
                ),
                const SizedBox(width: 8),
                _TaskFilterChip(
                  label: 'Completed',
                  filter: TaskFilter.completed,
                  current: activeFilter,
                  onSelect: () =>
                      ref.read(taskFilterProvider.notifier).state = TaskFilter.completed,
                ),
              ],
            ),
          ),

          // Tasks List
          Expanded(
            child: filteredTasks.isEmpty
                ? EmptyState(
                    icon: Icons.checklist_rounded,
                    title: activeFilter == TaskFilter.completed
                        ? 'No completed tasks'
                        : 'No tasks scheduled',
                    subtitle: activeFilter == TaskFilter.completed
                        ? 'Completed coursework will show up here.'
                        : 'Keep on top of your class assignments, problem sets, and lab reports.',
                    action: activeFilter != TaskFilter.completed
                        ? FilledButton.icon(
                            icon: const Icon(Icons.add),
                            label: const Text('Add Task'),
                            onPressed: () => showDialog(
                              context: context,
                              builder: (ctx) => const EditTaskDialog(),
                            ),
                          )
                        : null,
                  )
                : ListView.separated(
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    itemCount: filteredTasks.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 10),
                    itemBuilder: (context, index) {
                      final task = filteredTasks[index];
                      final course = task.courseId != null ? courseMap[task.courseId] : null;

                      Color priorityColor = Colors.blueGrey;
                      String priorityLabel = 'Low';
                      if (task.priority == 1) {
                        priorityColor = Colors.amber.shade700;
                        priorityLabel = 'Med';
                      } else if (task.priority == 2) {
                        priorityColor = Colors.redAccent;
                        priorityLabel = 'High';
                      }

                      final isOverdue = task.dueDateMillis != null &&
                          task.dueDateMillis! < DateTime.now().millisecondsSinceEpoch &&
                          !task.isCompleted;

                      return AdroitCard(
                        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                        child: Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Checkbox(
                              value: task.isCompleted,
                              onChanged: (val) {
                                ref.read(databaseProvider).setTaskCompletion(task.id, val ?? false);
                              },
                            ),
                            const SizedBox(width: 6),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Row(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Expanded(
                                        child: Text(
                                          task.title,
                                          style: TextStyle(
                                            fontSize: 15,
                                            fontWeight: FontWeight.w700,
                                            decoration: task.isCompleted
                                                ? TextDecoration.lineThrough
                                                : null,
                                            color: task.isCompleted
                                                ? theme.colorScheme.onSurface.withOpacity(0.4)
                                                : null,
                                            letterSpacing: -0.2,
                                          ),
                                        ),
                                      ),
                                      StatusBadge(
                                        label: priorityLabel,
                                        color: priorityColor,
                                      ),
                                    ],
                                  ),
                                  if (task.description.isNotEmpty) ...[
                                    const SizedBox(height: 4),
                                    Text(
                                      task.description,
                                      style: TextStyle(
                                        fontSize: 12,
                                        color: theme.colorScheme.onSurface.withOpacity(0.6),
                                      ),
                                      maxLines: 2,
                                      overflow: TextOverflow.ellipsis,
                                    ),
                                  ],
                                  const SizedBox(height: 8),

                                  // Course tag & Due date row
                                  Row(
                                    children: [
                                      if (course != null) ...[
                                        Container(
                                          padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                          decoration: BoxDecoration(
                                            color: AppConstants.parseHexColor(course.colorHex).withOpacity(0.12),
                                            borderRadius: BorderRadius.circular(4),
                                          ),
                                          child: Text(
                                            course.code.isNotEmpty ? course.code : course.name,
                                            style: TextStyle(
                                              fontSize: 10,
                                              fontWeight: FontWeight.w700,
                                              color: AppConstants.parseHexColor(course.colorHex),
                                            ),
                                          ),
                                        ),
                                        const SizedBox(width: 8),
                                      ],
                                      if (task.dueDateMillis != null) ...[
                                        Icon(
                                          Icons.access_time_rounded,
                                          size: 13,
                                          color: isOverdue ? Colors.redAccent : Colors.grey,
                                        ),
                                        const SizedBox(width: 4),
                                        Text(
                                          AdroitDateUtils.formatDeadline(task.dueDateMillis!),
                                          style: TextStyle(
                                            fontSize: 11,
                                            fontWeight: isOverdue ? FontWeight.w700 : FontWeight.w500,
                                            color: isOverdue ? Colors.redAccent : Colors.grey,
                                          ),
                                        ),
                                      ],
                                    ],
                                  ),
                                ],
                              ),
                            ),
                            PopupMenuButton<String>(
                              icon: const Icon(Icons.more_vert, size: 18, color: Colors.grey),
                              onSelected: (action) {
                                if (action == 'edit') {
                                  showDialog(
                                    context: context,
                                    builder: (ctx) => EditTaskDialog(initialTask: task),
                                  );
                                } else if (action == 'delete') {
                                  ref.read(databaseProvider).deleteTask(task.id);
                                }
                              },
                              itemBuilder: (ctx) => [
                                const PopupMenuItem(
                                  value: 'edit',
                                  child: Text('Edit Task'),
                                ),
                                const PopupMenuItem(
                                  value: 'delete',
                                  child: Text('Delete Task', style: TextStyle(color: Colors.redAccent)),
                                ),
                              ],
                            ),
                          ],
                        ),
                      );
                    },
                  ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => showDialog(
          context: context,
          builder: (ctx) => const EditTaskDialog(),
        ),
        child: const Icon(Icons.add),
      ),
    );
  }
}

class _TaskFilterChip extends StatelessWidget {
  final String label;
  final TaskFilter filter;
  final TaskFilter current;
  final VoidCallback onSelect;

  const _TaskFilterChip({
    required this.label,
    required this.filter,
    required this.current,
    required this.onSelect,
  });

  @override
  Widget build(BuildContext context) {
    return ChoiceChip(
      label: Text(label),
      selected: filter == current,
      onSelected: (_) => onSelect(),
    );
  }
}
