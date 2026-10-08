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
    final allTasks = ref.watch(allTasksProvider).value ?? [];
    final allCourses = ref.watch(allCoursesProvider).value ?? [];

    final courseMap = {for (final c in allCourses) c.id: c};

    final pendingCount = allTasks.where((t) => !t.isCompleted).length;
    final overdueCount = allTasks.where((t) {
      if (t.isCompleted || t.dueDateMillis == null) return false;
      return t.dueDateMillis! < DateTime.now().millisecondsSinceEpoch;
    }).length;

    return Scaffold(
      body: SafeArea(
        child: CustomScrollView(
          slivers: [
            // Executive Header
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(20, 20, 20, 12),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'ACADEMIC WORKLOAD',
                            style: TextStyle(
                              fontSize: 11,
                              fontWeight: FontWeight.w700,
                              letterSpacing: 1.5,
                              color: theme.colorScheme.primary,
                            ),
                          ),
                          const SizedBox(height: 4),
                          const Text(
                            'Tasks & Due Dates',
                            style: TextStyle(
                              fontSize: 26,
                              fontWeight: FontWeight.w800,
                              letterSpacing: -0.8,
                            ),
                          ),
                        ],
                      ),
                    ),
                    // Action button
                    FilledButton.icon(
                      style: FilledButton.styleFrom(
                        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                      ),
                      icon: const Icon(Icons.add_rounded, size: 18),
                      label: const Text('New Task', style: TextStyle(fontWeight: FontWeight.w700, fontSize: 13)),
                      onPressed: () => showDialog(
                        context: context,
                        builder: (ctx) => const EditTaskDialog(),
                      ),
                    ),
                  ],
                ),
              ),
            ),

            // Quick Stats Metric Strip
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 6),
                child: Row(
                  children: [
                    _WorkloadPill(
                      label: 'Pending',
                      count: pendingCount,
                      color: theme.colorScheme.primary,
                      icon: Icons.pending_actions_rounded,
                    ),
                    const SizedBox(width: 10),
                    _WorkloadPill(
                      label: 'Overdue',
                      count: overdueCount,
                      color: overdueCount > 0 ? const Color(0xFFEF4444) : Colors.grey,
                      icon: Icons.warning_amber_rounded,
                    ),
                    const SizedBox(width: 10),
                    _WorkloadPill(
                      label: 'Completed',
                      count: allTasks.length - pendingCount,
                      color: const Color(0xFF10B981),
                      icon: Icons.task_alt_rounded,
                    ),
                  ],
                ),
              ),
            ),

            // Filter Pills
            SliverToBoxAdapter(
              child: SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                child: Row(
                  children: [
                    _FilterPill(
                      label: 'Upcoming',
                      filter: TaskFilter.upcoming,
                      current: activeFilter,
                      onTap: () =>
                          ref.read(taskFilterProvider.notifier).state = TaskFilter.upcoming,
                    ),
                    const SizedBox(width: 8),
                    _FilterPill(
                      label: 'Overdue',
                      filter: TaskFilter.overdue,
                      current: activeFilter,
                      badgeCount: overdueCount > 0 ? overdueCount : null,
                      onTap: () =>
                          ref.read(taskFilterProvider.notifier).state = TaskFilter.overdue,
                    ),
                    const SizedBox(width: 8),
                    _FilterPill(
                      label: 'All Active',
                      filter: TaskFilter.all,
                      current: activeFilter,
                      onTap: () =>
                          ref.read(taskFilterProvider.notifier).state = TaskFilter.all,
                    ),
                    const SizedBox(width: 8),
                    _FilterPill(
                      label: 'Completed',
                      filter: TaskFilter.completed,
                      current: activeFilter,
                      onTap: () =>
                          ref.read(taskFilterProvider.notifier).state = TaskFilter.completed,
                    ),
                  ],
                ),
              ),
            ),

            // Tasks List
            if (filteredTasks.isEmpty)
              SliverFillRemaining(
                hasScrollBody: false,
                child: EmptyState(
                  icon: Icons.assignment_turned_in_rounded,
                  title: activeFilter == TaskFilter.completed
                      ? 'No completed tasks yet'
                      : 'Zero pending tasks',
                  subtitle: activeFilter == TaskFilter.completed
                      ? 'Mark assignments and lab works as done to archive them here.'
                      : 'All caught up! Tap "New Task" to log deadlines, lab reports, or problem sets.',
                  action: activeFilter != TaskFilter.completed
                      ? FilledButton.icon(
                          icon: const Icon(Icons.add_rounded),
                          label: const Text('Add Task'),
                          onPressed: () => showDialog(
                            context: context,
                            builder: (ctx) => const EditTaskDialog(),
                          ),
                        )
                      : null,
                ),
              )
            else
              SliverPadding(
                padding: const EdgeInsets.fromLTRB(20, 4, 20, 110),
                sliver: SliverList(
                  delegate: SliverChildBuilderDelegate(
                    (context, index) {
                      final task = filteredTasks[index];
                      final course = task.courseId != null ? courseMap[task.courseId] : null;

                      Color priorityColor = Colors.grey;
                      String priorityLabel = 'Low';
                      if (task.priority == 1) {
                        priorityColor = const Color(0xFFF59E0B);
                        priorityLabel = 'Medium';
                      } else if (task.priority == 2) {
                        priorityColor = const Color(0xFFEF4444);
                        priorityLabel = 'Urgent';
                      }

                      final isOverdue = task.dueDateMillis != null &&
                          task.dueDateMillis! < DateTime.now().millisecondsSinceEpoch &&
                          !task.isCompleted;

                      return Padding(
                        padding: const EdgeInsets.only(bottom: 12),
                        child: AdroitCard(
                          padding: const EdgeInsets.all(14),
                          onTap: () => showDialog(
                            context: context,
                            builder: (ctx) => EditTaskDialog(initialTask: task),
                          ),
                          child: Row(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              // Squircle Checkbox
                              Padding(
                                padding: const EdgeInsets.only(top: 2, right: 12),
                                child: InkWell(
                                  borderRadius: BorderRadius.circular(10),
                                  onTap: () {
                                    ref.read(databaseProvider).setTaskCompletion(
                                          task.id,
                                          !task.isCompleted,
                                        );
                                  },
                                  child: AnimatedContainer(
                                    duration: const Duration(milliseconds: 200),
                                    width: 26,
                                    height: 26,
                                    decoration: BoxDecoration(
                                      color: task.isCompleted
                                          ? theme.colorScheme.primary
                                          : theme.colorScheme.onSurface.withOpacity(0.04),
                                      borderRadius: BorderRadius.circular(8),
                                      border: Border.all(
                                        color: task.isCompleted
                                            ? theme.colorScheme.primary
                                            : theme.colorScheme.onSurface.withOpacity(0.25),
                                        width: 1.6,
                                      ),
                                    ),
                                    child: task.isCompleted
                                        ? const Icon(
                                            Icons.check_rounded,
                                            size: 17,
                                            color: Colors.white,
                                          )
                                        : null,
                                  ),
                                ),
                              ),

                              // Task details
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
                                              letterSpacing: -0.2,
                                              decoration: task.isCompleted
                                                  ? TextDecoration.lineThrough
                                                  : null,
                                              color: task.isCompleted
                                                  ? theme.colorScheme.onSurface.withOpacity(0.35)
                                                  : theme.colorScheme.onSurface,
                                            ),
                                          ),
                                        ),
                                        const SizedBox(width: 8),
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
                                          fontSize: 12.5,
                                          color: theme.colorScheme.onSurface.withOpacity(0.55),
                                          height: 1.35,
                                        ),
                                        maxLines: 2,
                                        overflow: TextOverflow.ellipsis,
                                      ),
                                    ],
                                    const SizedBox(height: 10),

                                    // Course tag & Deadline chip row
                                    Wrap(
                                      spacing: 8,
                                      runSpacing: 6,
                                      crossAxisAlignment: WrapCrossAlignment.center,
                                      children: [
                                        if (course != null)
                                          Container(
                                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                                            decoration: BoxDecoration(
                                              color: AppConstants.parseHexColor(course.colorHex).withOpacity(0.14),
                                              borderRadius: BorderRadius.circular(6),
                                              border: Border.all(
                                                color: AppConstants.parseHexColor(course.colorHex).withOpacity(0.3),
                                                width: 0.8,
                                              ),
                                            ),
                                            child: Text(
                                              course.code.isNotEmpty ? course.code : course.name,
                                              style: TextStyle(
                                                fontSize: 11,
                                                fontWeight: FontWeight.w700,
                                                color: AppConstants.parseHexColor(course.colorHex),
                                                letterSpacing: 0.1,
                                              ),
                                            ),
                                          ),
                                        if (task.dueDateMillis != null)
                                          Container(
                                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                                            decoration: BoxDecoration(
                                              color: isOverdue
                                                  ? const Color(0xFFEF4444).withOpacity(0.12)
                                                  : theme.colorScheme.onSurface.withOpacity(0.06),
                                              borderRadius: BorderRadius.circular(6),
                                              border: Border.all(
                                                color: isOverdue
                                                    ? const Color(0xFFEF4444).withOpacity(0.3)
                                                    : theme.colorScheme.onSurface.withOpacity(0.1),
                                                width: 0.8,
                                              ),
                                            ),
                                            child: Row(
                                              mainAxisSize: MainAxisSize.min,
                                              children: [
                                                Icon(
                                                  Icons.schedule_rounded,
                                                  size: 12,
                                                  color: isOverdue
                                                      ? const Color(0xFFEF4444)
                                                      : theme.colorScheme.onSurface.withOpacity(0.6),
                                                ),
                                                const SizedBox(width: 4),
                                                Text(
                                                  AdroitDateUtils.formatDeadline(task.dueDateMillis!),
                                                  style: TextStyle(
                                                    fontSize: 11,
                                                    fontWeight: isOverdue ? FontWeight.w800 : FontWeight.w600,
                                                    color: isOverdue
                                                        ? const Color(0xFFEF4444)
                                                        : theme.colorScheme.onSurface.withOpacity(0.7),
                                                  ),
                                                ),
                                              ],
                                            ),
                                          ),
                                      ],
                                    ),
                                  ],
                                ),
                              ),

                              // Quick menu
                              PopupMenuButton<String>(
                                icon: Icon(
                                  Icons.more_horiz_rounded,
                                  size: 20,
                                  color: theme.colorScheme.onSurface.withOpacity(0.4),
                                ),
                                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
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
                                    child: Row(
                                      children: [
                                        Icon(Icons.edit_rounded, size: 16),
                                        SizedBox(width: 8),
                                        Text('Edit Task'),
                                      ],
                                    ),
                                  ),
                                  const PopupMenuItem(
                                    value: 'delete',
                                    child: Row(
                                      children: [
                                        Icon(Icons.delete_outline_rounded, size: 16, color: Colors.redAccent),
                                        SizedBox(width: 8),
                                        Text('Delete', style: TextStyle(color: Colors.redAccent)),
                                      ],
                                    ),
                                  ),
                                ],
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                    childCount: filteredTasks.length,
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class _WorkloadPill extends StatelessWidget {
  final String label;
  final int count;
  final Color color;
  final IconData icon;

  const _WorkloadPill({
    required this.label,
    required this.count,
    required this.color,
    required this.icon,
  });

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 10),
        decoration: BoxDecoration(
          color: color.withOpacity(0.08),
          borderRadius: BorderRadius.circular(14),
          border: Border.all(
            color: color.withOpacity(0.2),
            width: 1,
          ),
        ),
        child: Row(
          children: [
            Icon(icon, size: 16, color: color),
            const SizedBox(width: 6),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    '$count',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w800,
                      color: color,
                      height: 1.1,
                    ),
                  ),
                  Text(
                    label,
                    style: TextStyle(
                      fontSize: 10,
                      fontWeight: FontWeight.w600,
                      color: color.withOpacity(0.8),
                    ),
                    overflow: TextOverflow.ellipsis,
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _FilterPill extends StatelessWidget {
  final String label;
  final TaskFilter filter;
  final TaskFilter current;
  final int? badgeCount;
  final VoidCallback onTap;

  const _FilterPill({
    required this.label,
    required this.filter,
    required this.current,
    this.badgeCount,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isSelected = filter == current;

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 180),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 7),
        decoration: BoxDecoration(
          color: isSelected
              ? theme.colorScheme.primary
              : theme.colorScheme.onSurface.withOpacity(0.05),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: isSelected
                ? theme.colorScheme.primary
                : theme.colorScheme.onSurface.withOpacity(0.12),
            width: 1,
          ),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              label,
              style: TextStyle(
                fontSize: 12,
                fontWeight: isSelected ? FontWeight.w700 : FontWeight.w500,
                color: isSelected
                    ? Colors.white
                    : theme.colorScheme.onSurface.withOpacity(0.7),
              ),
            ),
            if (badgeCount != null) ...[
              const SizedBox(width: 6),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 1),
                decoration: BoxDecoration(
                  color: isSelected ? Colors.white.withOpacity(0.25) : const Color(0xFFEF4444),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(
                  '$badgeCount',
                  style: TextStyle(
                    fontSize: 10,
                    fontWeight: FontWeight.w800,
                    color: isSelected ? Colors.white : Colors.white,
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
