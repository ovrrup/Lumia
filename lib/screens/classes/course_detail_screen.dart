import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/constants/app_constants.dart';
import '../../core/utils/date_utils.dart';
import '../../data/database/database.dart';
import '../../data/models/models.dart';
import '../../providers/attendance_provider.dart';
import '../../providers/courses_provider.dart';
import '../../providers/database_provider.dart';
import '../../providers/tasks_provider.dart';
import '../../widgets/adroit_card.dart';
import '../../widgets/empty_state.dart';
import '../../widgets/progress_bar.dart';
import '../../widgets/radial_gauge.dart';
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
        content: Text('Delete "${course.name}" and all associated attendance records?'),
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
            padding: const EdgeInsets.fromLTRB(20, 8, 20, 60),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // Hero Overview Card
                AdroitCard(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Container(
                            width: 14,
                            height: 48,
                            decoration: BoxDecoration(
                              color: courseColor,
                              borderRadius: BorderRadius.circular(8),
                            ),
                          ),
                          const SizedBox(width: 14),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  course.name,
                                  style: const TextStyle(
                                    fontSize: 20,
                                    fontWeight: FontWeight.w800,
                                    letterSpacing: -0.5,
                                  ),
                                ),
                                if (course.code.isNotEmpty) ...[
                                  const SizedBox(height: 3),
                                  Text(
                                    course.code,
                                    style: TextStyle(
                                      fontSize: 13,
                                      fontWeight: FontWeight.w800,
                                      color: courseColor,
                                    ),
                                  ),
                                ],
                              ],
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),

                      // Location & Instructor
                      if (course.instructor.isNotEmpty || course.room.isNotEmpty) ...[
                        Row(
                          children: [
                            if (course.instructor.isNotEmpty) ...[
                              Icon(Icons.person_rounded, size: 16, color: theme.colorScheme.onSurface.withOpacity(0.4)),
                              const SizedBox(width: 6),
                              Expanded(
                                child: Text(
                                  course.instructor,
                                  style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                            ],
                            if (course.room.isNotEmpty) ...[
                              const SizedBox(width: 12),
                              Icon(Icons.room_rounded, size: 16, color: theme.colorScheme.onSurface.withOpacity(0.4)),
                              const SizedBox(width: 4),
                              Text(
                                course.room,
                                style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
                              ),
                            ],
                          ],
                        ),
                        const SizedBox(height: 12),
                      ],

                      // Timings & Lecture Days
                      Row(
                        children: [
                          Icon(Icons.access_time_rounded, size: 16, color: theme.colorScheme.onSurface.withOpacity(0.4)),
                          const SizedBox(width: 6),
                          Text(
                            '${course.startTime} - ${course.endTime}',
                            style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w700),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Wrap(
                              spacing: 5,
                              children: scheduleDaysList.map((d) {
                                return Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 3),
                                  decoration: BoxDecoration(
                                    color: theme.colorScheme.surfaceVariant,
                                    borderRadius: BorderRadius.circular(6),
                                  ),
                                  child: Text(
                                    d,
                                    style: const TextStyle(fontSize: 10, fontWeight: FontWeight.w800),
                                  ),
                                );
                              }).toList(),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 18),

                // Attendance Command Center Header
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      'Attendance Intelligence',
                      style: TextStyle(fontSize: 17, fontWeight: FontWeight.w800, letterSpacing: -0.3),
                    ),
                    FilledButton.icon(
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

                // Executive Attendance Card
                AdroitCard(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    children: [
                      Row(
                        children: [
                          RadialProgressGauge(
                            progress: analytics.percentage / 100.0,
                            size: 82,
                            strokeWidth: 8,
                            color: analytics.health.color,
                            centerChild: Text(
                              '${analytics.percentage.round()}%',
                              style: TextStyle(
                                fontSize: 18,
                                fontWeight: FontWeight.w800,
                                color: analytics.health.color,
                              ),
                            ),
                          ),
                          const SizedBox(width: 20),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                StatusBadge(
                                  label: analytics.health.label.toUpperCase(),
                                  color: analytics.health.color,
                                  isFilled: true,
                                ),
                                const SizedBox(height: 8),
                                Text(
                                  'Threshold Target: ${course.targetAttendance}%',
                                  style: TextStyle(
                                    fontSize: 12,
                                    fontWeight: FontWeight.w600,
                                    color: theme.colorScheme.onSurface.withOpacity(0.55),
                                  ),
                                ),
                                const SizedBox(height: 4),
                                Text(
                                  '${analytics.attendedClasses} attended of ${analytics.totalClasses} total lectures',
                                  style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w700),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 18),
                      const Divider(height: 1),
                      const SizedBox(height: 16),

                      // 4-column metric grid
                      Row(
                        children: [
                          _MetricStat(label: 'ATTENDED', value: '${analytics.attendedClasses}', color: const Color(0xFF10B981)),
                          _MetricStat(label: 'MISSED', value: '${analytics.missedClasses}', color: const Color(0xFFEF4444)),
                          _MetricStat(label: 'CANCELLED', value: '${analytics.cancelledClasses}', color: Colors.grey),
                          _MetricStat(label: 'TOTAL', value: '${analytics.totalClasses}', color: theme.colorScheme.onSurface),
                        ],
                      ),
                      const SizedBox(height: 16),

                      // Safe Bunks / Recovery Advice Card
                      Container(
                        padding: const EdgeInsets.all(14),
                        decoration: BoxDecoration(
                          color: analytics.health.color.withOpacity(0.08),
                          borderRadius: BorderRadius.circular(14),
                          border: Border.all(color: analytics.health.color.withOpacity(0.25)),
                        ),
                        child: Row(
                          children: [
                            Icon(
                              analytics.health == AttendanceHealth.critical
                                  ? Icons.warning_amber_rounded
                                  : Icons.shield_rounded,
                              size: 20,
                              color: analytics.health.color,
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Text(
                                analytics.health == AttendanceHealth.critical
                                    ? 'You must attend the next ${analytics.classesToRecover} consecutive classes to recover to ${course.targetAttendance}%.'
                                    : 'You can safely miss ${analytics.safeBunksAllowed} more classes without falling below ${course.targetAttendance}%.',
                                style: TextStyle(
                                  fontSize: 12,
                                  fontWeight: FontWeight.w700,
                                  color: analytics.health.color,
                                  height: 1.35,
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 22),

                // Tasks & Deadlines Section
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      'Coursework Tasks (${courseTasks.length})',
                      style: const TextStyle(fontSize: 17, fontWeight: FontWeight.w800, letterSpacing: -0.3),
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
                        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
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
                                      fontWeight: FontWeight.w700,
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
                                        fontWeight: FontWeight.w600,
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
                const SizedBox(height: 22),

                // Attendance Log History
                const Text(
                  'Attendance History',
                  style: TextStyle(fontSize: 17, fontWeight: FontWeight.w800, letterSpacing: -0.3),
                ),
                const SizedBox(height: 10),
                if (attendanceRecords.isEmpty)
                  const EmptyState(
                    icon: Icons.history_rounded,
                    title: 'No attendance records yet',
                    subtitle: 'Use the button above to log your class sessions.',
                  )
                else
                  ...attendanceRecords.map((r) {
                    final status = AttendanceStatus.values.firstWhere(
                      (s) => s.name == r.status.toLowerCase(),
                      orElse: () => AttendanceStatus.present,
                    );

                    return Padding(
                      padding: const EdgeInsets.only(bottom: 8),
                      child: AdroitCard(
                        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Row(
                              children: [
                                Icon(status.icon, size: 22, color: status.color),
                                const SizedBox(width: 12),
                                Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      AdroitDateUtils.formatRelativeDate(r.dateMillis),
                                      style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w700),
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
                                StatusBadge(label: status.label.toUpperCase(), color: status.color),
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
              fontSize: 20,
              fontWeight: FontWeight.w800,
              letterSpacing: -0.5,
              color: color,
            ),
          ),
          const SizedBox(height: 2),
          Text(
            label,
            style: const TextStyle(fontSize: 9, fontWeight: FontWeight.w800, color: Colors.grey, letterSpacing: 0.5),
          ),
        ],
      ),
    );
  }
}
