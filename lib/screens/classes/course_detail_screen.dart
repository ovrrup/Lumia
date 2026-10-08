import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/constants/app_constants.dart';
import '../../core/utils/date_utils.dart';
import '../../data/database/database.dart';
import '../../data/models/models.dart';
import '../../providers/attendance_provider.dart';
import '../../providers/courses_provider.dart';
import '../../providers/database_provider.dart';
import '../../providers/syllabus_provider.dart';
import '../../providers/tasks_provider.dart';
import '../../widgets/adroit_card.dart';
import '../../widgets/empty_state.dart';
import '../../widgets/progress_bar.dart';
import '../../widgets/status_badge.dart';
import 'edit_course_dialog.dart';
import 'record_attendance_dialog.dart';
import '../tasks/edit_task_dialog.dart';

class CourseDetailScreen extends ConsumerWidget {
  final int courseId;

  const CourseDetailScreen({super.key, required this.courseId});

  Future<void> _confirmDeleteCourse(BuildContext context, WidgetRef ref, Course course) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Delete Course?'),
        content: Text('Are you sure you want to delete "${course.name}"? All associated attendance records and syllabus links will be removed.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(ctx).pop(true),
            style: FilledButton.styleFrom(backgroundColor: Colors.red),
            child: const Text('Delete'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      final db = ref.read(databaseProvider);
      await db.deleteCourse(course.id);
      if (context.mounted) Navigator.of(context).pop();
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final courseAsync = ref.watch(courseByIdProvider(courseId));
    final attendanceRecords = ref.watch(attendanceForCourseProvider(courseId)).value ?? [];
    final analytics = ref.watch(courseAttendanceAnalyticsProvider(courseId));
    final allTasks = ref.watch(allTasksProvider).value ?? [];
    final courseTasks = allTasks.where((t) => t.courseId == courseId).toList();

    return courseAsync.when(
      loading: () => const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      ),
      error: (e, _) => Scaffold(
        appBar: AppBar(),
        body: Center(child: Text('Error: $e')),
      ),
      data: (course) {
        if (course == null) {
          return Scaffold(
            appBar: AppBar(),
            body: const Center(child: Text('Course not found')),
          );
        }

        final courseColor = AppConstants.parseHexColor(course.colorHex);
        final scheduleDaysList = course.scheduleDays.split(',').where((s) => s.isNotEmpty).toList();

        return Scaffold(
          appBar: AppBar(
            title: Text(course.name),
            actions: [
              IconButton(
                icon: const Icon(Icons.edit_outlined),
                tooltip: 'Edit Course',
                onPressed: () => showDialog(
                  context: context,
                  builder: (ctx) => EditCourseDialog(initialCourse: course),
                ),
              ),
              IconButton(
                icon: const Icon(Icons.delete_outline_rounded, color: Colors.redAccent),
                tooltip: 'Delete Course',
                onPressed: () => _confirmDeleteCourse(context, ref, course),
              ),
            ],
          ),
          body: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // Top Meta Card
                AdroitCard(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Container(
                            width: 12,
                            height: 48,
                            decoration: BoxDecoration(
                              color: courseColor,
                              borderRadius: BorderRadius.circular(6),
                            ),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  course.name,
                                  style: const TextStyle(
                                    fontSize: 18,
                                    fontWeight: FontWeight.w700,
                                    letterSpacing: -0.4,
                                  ),
                                ),
                                if (course.code.isNotEmpty) ...[
                                  const SizedBox(height: 2),
                                  Text(
                                    course.code,
                                    style: TextStyle(
                                      fontSize: 13,
                                      fontWeight: FontWeight.w600,
                                      color: courseColor,
                                    ),
                                  ),
                                ],
                              ],
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 14),

                      // Instructor & Room row
                      if (course.instructor.isNotEmpty || course.room.isNotEmpty) ...[
                        Row(
                          children: [
                            if (course.instructor.isNotEmpty) ...[
                              const Icon(Icons.person_outline_rounded, size: 16, color: Colors.grey),
                              const SizedBox(width: 4),
                              Expanded(
                                child: Text(
                                  course.instructor,
                                  style: const TextStyle(fontSize: 13),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                            ],
                            if (course.room.isNotEmpty) ...[
                              const SizedBox(width: 12),
                              const Icon(Icons.room_outlined, size: 16, color: Colors.grey),
                              const SizedBox(width: 4),
                              Text(
                                course.room,
                                style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w500),
                              ),
                            ],
                          ],
                        ),
                        const SizedBox(height: 10),
                      ],

                      // Timings & Days
                      Row(
                        children: [
                          const Icon(Icons.access_time_rounded, size: 16, color: Colors.grey),
                          const SizedBox(width: 4),
                          Text(
                            '${course.startTime} - ${course.endTime}',
                            style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Wrap(
                              spacing: 4,
                              children: scheduleDaysList.map((d) {
                                return Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                  decoration: BoxDecoration(
                                    color: theme.colorScheme.surfaceVariant,
                                    borderRadius: BorderRadius.circular(4),
                                  ),
                                  child: Text(
                                    d,
                                    style: const TextStyle(fontSize: 10, fontWeight: FontWeight.w600),
                                  ),
                                );
                              }).toList(),
                            ),
                          ),
                        ],
                      ),
                      if (course.description.isNotEmpty) ...[
                        const SizedBox(height: 12),
                        const Divider(height: 1),
                        const SizedBox(height: 10),
                        Text(
                          course.description,
                          style: TextStyle(
                            fontSize: 13,
                            color: theme.colorScheme.onSurface.withOpacity(0.7),
                          ),
                        ),
                      ],
                    ],
                  ),
                ),
                const SizedBox(height: 16),

                // Attendance Section
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      'Attendance Tracker',
                      style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700),
                    ),
                    FilledButton.tonalIcon(
                      icon: const Icon(Icons.add, size: 16),
                      label: const Text('Log Attendance'),
                      onPressed: () => showDialog(
                        context: context,
                        builder: (ctx) => RecordAttendanceDialog(
                          courseId: course.id,
                          courseName: course.name,
                        ),
                      ),
                      style: FilledButton.styleFrom(
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),

                // Attendance Metrics Card
                AdroitCard(
                  child: Column(
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                '${analytics.percentage.toStringAsFixed(1)}%',
                                style: TextStyle(
                                  fontSize: 32,
                                  fontWeight: FontWeight.w800,
                                  letterSpacing: -1,
                                  color: analytics.health.color,
                                ),
                              ),
                              Text(
                                'Target: ${course.targetAttendance}%',
                                style: TextStyle(
                                  fontSize: 12,
                                  color: theme.colorScheme.onSurface.withOpacity(0.6),
                                ),
                              ),
                            ],
                          ),
                          StatusBadge(
                            label: analytics.health.label,
                            color: analytics.health.color,
                            icon: Icons.shield_outlined,
                            isFilled: true,
                          ),
                        ],
                      ),
                      const SizedBox(height: 14),
                      AdroitProgressBar(
                        progress: analytics.percentage / 100.0,
                        color: analytics.health.color,
                        height: 8,
                      ),
                      const SizedBox(height: 16),

                      // Breakdown grid
                      Row(
                        children: [
                          _MetricStat(label: 'Attended', value: '${analytics.attendedClasses}', color: const Color(0xFF10B981)),
                          _MetricStat(label: 'Missed', value: '${analytics.missedClasses}', color: const Color(0xFFEF4444)),
                          _MetricStat(label: 'Cancelled', value: '${analytics.cancelledClasses}', color: Colors.grey),
                          _MetricStat(label: 'Total', value: '${analytics.totalClasses}', color: theme.colorScheme.onSurface),
                        ],
                      ),

                      // Smart Attendance Advice
                      const SizedBox(height: 14),
                      Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: analytics.health.color.withOpacity(0.08),
                          borderRadius: BorderRadius.circular(10),
                          border: Border.all(color: analytics.health.color.withOpacity(0.2)),
                        ),
                        child: Row(
                          children: [
                            Icon(Icons.info_outline_rounded, size: 18, color: analytics.health.color),
                            const SizedBox(width: 10),
                            Expanded(
                              child: Text(
                                analytics.health == AttendanceHealth.critical
                                    ? 'You must attend next ${analytics.classesToRecover} consecutive classes to reach ${course.targetAttendance}%.'
                                    : 'You can safely miss ${analytics.safeBunksAllowed} more classes and stay at or above ${course.targetAttendance}%.',
                                style: TextStyle(
                                  fontSize: 12,
                                  fontWeight: FontWeight.w600,
                                  color: analytics.health.color,
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 20),

                // Course Tasks / Deadlines
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      'Tasks & Deadlines (${courseTasks.length})',
                      style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w700),
                    ),
                    IconButton(
                      icon: const Icon(Icons.add_circle_outline, size: 20),
                      tooltip: 'Add Task',
                      onPressed: () => showDialog(
                        context: context,
                        builder: (ctx) => EditTaskDialog(courseId: course.id),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                if (courseTasks.isEmpty)
                  Padding(
                    padding: const EdgeInsets.symmetric(vertical: 8),
                    child: Text(
                      'No tasks scheduled for this course.',
                      style: TextStyle(
                        fontSize: 13,
                        color: theme.colorScheme.onSurface.withOpacity(0.5),
                      ),
                    ),
                  )
                else
                  ...courseTasks.map((t) {
                    return Padding(
                      padding: const EdgeInsets.only(bottom: 8),
                      child: AdroitCard(
                        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                        child: Row(
                          children: [
                            Checkbox(
                              value: t.isCompleted,
                              onChanged: (val) {
                                ref.read(databaseProvider).setTaskCompletion(t.id, val ?? false);
                              },
                            ),
                            const SizedBox(width: 8),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    t.title,
                                    style: TextStyle(
                                      fontSize: 14,
                                      fontWeight: FontWeight.w600,
                                      decoration: t.isCompleted ? TextDecoration.lineThrough : null,
                                      color: t.isCompleted
                                          ? theme.colorScheme.onSurface.withOpacity(0.4)
                                          : null,
                                    ),
                                  ),
                                  if (t.dueDateMillis != null) ...[
                                    const SizedBox(height: 2),
                                    Text(
                                      AdroitDateUtils.formatDeadline(t.dueDateMillis!),
                                      style: TextStyle(
                                        fontSize: 11,
                                        fontWeight: FontWeight.w500,
                                        color: t.dueDateMillis! < DateTime.now().millisecondsSinceEpoch && !t.isCompleted
                                            ? Colors.redAccent
                                            : Colors.grey,
                                      ),
                                    ),
                                  ],
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                    );
                  }),
                const SizedBox(height: 20),

                // Attendance Log History
                const Text(
                  'Attendance History',
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700),
                ),
                const SizedBox(height: 10),
                if (attendanceRecords.isEmpty)
                  const EmptyState(
                    icon: Icons.history_rounded,
                    title: 'No attendance records',
                    subtitle: 'Use the button above to record your attendance for this class.',
                  )
                else
                  ...attendanceRecords.map((r) {
                    final status = AttendanceStatus.values.firstWhere(
                      (s) => s.name == r.status.toLowerCase(),
                      orElse: () => AttendanceStatus.present,
                    );

                    return Padding(
                      padding: const EdgeInsets.only(bottom: 6),
                      child: AdroitCard(
                        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Row(
                              children: [
                                Icon(status.icon, size: 20, color: status.color),
                                const SizedBox(width: 10),
                                Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      AdroitDateUtils.formatRelativeDate(r.dateMillis),
                                      style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
                                    ),
                                    Text(
                                      AdroitDateUtils.fromMillis(r.dateMillis).toString().split(' ')[0],
                                      style: TextStyle(fontSize: 11, color: theme.colorScheme.onSurface.withOpacity(0.5)),
                                    ),
                                  ],
                                ),
                              ],
                            ),
                            Row(
                              children: [
                                StatusBadge(label: status.label, color: status.color),
                                IconButton(
                                  icon: const Icon(Icons.delete_outline, size: 18, color: Colors.grey),
                                  onPressed: () {
                                    ref.read(databaseProvider).deleteAttendanceRecord(r.id);
                                  },
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                    );
                  }),
                const SizedBox(height: 32),
              ],
            ),
          ),
        );
      },
    );
  }
}

class _MetricStat extends StatelessWidget {
  final String label;
  final String value;
  final Color color;

  const _MetricStat({
    required this.label,
    required this.value,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: Column(
        children: [
          Text(
            value,
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w800,
              color: color,
            ),
          ),
          const SizedBox(height: 2),
          Text(
            label,
            style: const TextStyle(fontSize: 11, color: Colors.grey),
          ),
        ],
      ),
    );
  }
}
