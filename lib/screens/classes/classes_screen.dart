import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/constants/app_constants.dart';
import '../../core/utils/date_utils.dart';
import '../../data/models/models.dart';
import '../../providers/attendance_provider.dart';
import '../../providers/courses_provider.dart';
import '../../providers/database_provider.dart';
import '../../widgets/adroit_card.dart';
import '../../widgets/empty_state.dart';
import '../../widgets/status_badge.dart';
import 'course_detail_screen.dart';
import 'edit_course_dialog.dart';

class ClassesScreen extends ConsumerStatefulWidget {
  const ClassesScreen({super.key});

  @override
  ConsumerState<ClassesScreen> createState() => _ClassesScreenState();
}

class _ClassesScreenState extends ConsumerState<ClassesScreen> {
  int _activeViewIndex = 0; // 0: Schedule, 1: All Courses

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final allCourses = ref.watch(allCoursesProvider).value ?? [];
    final selectedDay = ref.watch(selectedDayFilterProvider);
    final scheduledClasses = ref.watch(classesForDayProvider(selectedDay));
    final currentDayOfWeek = AdroitDateUtils.getCurrentShortWeekday();
    final isToday = selectedDay == currentDayOfWeek;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Classes & Timetable'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add_rounded),
            tooltip: 'Add Course',
            onPressed: () => showDialog(
              context: context,
              builder: (ctx) => const EditCourseDialog(),
            ),
          ),
        ],
      ),
      body: Column(
        children: [
          // View Switcher (Schedule vs All Courses)
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: Container(
              height: 40,
              decoration: BoxDecoration(
                color: theme.colorScheme.surfaceVariant,
                borderRadius: BorderRadius.circular(10),
              ),
              child: Row(
                children: [
                  Expanded(
                    child: _ViewTab(
                      label: 'Timetable',
                      icon: Icons.calendar_view_day_rounded,
                      isSelected: _activeViewIndex == 0,
                      onTap: () => setState(() => _activeViewIndex = 0),
                    ),
                  ),
                  Expanded(
                    child: _ViewTab(
                      label: 'All Courses (${allCourses.length})',
                      icon: Icons.school_outlined,
                      isSelected: _activeViewIndex == 1,
                      onTap: () => setState(() => _activeViewIndex = 1),
                    ),
                  ),
                ],
              ),
            ),
          ),

          Expanded(
            child: _activeViewIndex == 0
                ? _buildScheduleView(
                    context,
                    theme,
                    selectedDay,
                    currentDayOfWeek,
                    isToday,
                    scheduledClasses,
                  )
                : _buildCoursesListView(context, theme, allCourses),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => showDialog(
          context: context,
          builder: (ctx) => const EditCourseDialog(),
        ),
        child: const Icon(Icons.add),
      ),
    );
  }

  Widget _buildScheduleView(
    BuildContext context,
    ThemeData theme,
    String selectedDay,
    String currentDayOfWeek,
    bool isToday,
    List<ScheduledClassItem> scheduledClasses,
  ) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        // Days of week selector
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
          child: SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children: AppConstants.weekDays.map((day) {
                final isSelected = selectedDay == day;
                final isCurrentDay = currentDayOfWeek == day;

                return Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 4),
                  child: ChoiceChip(
                    label: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(day),
                        if (isCurrentDay) ...[
                          const SizedBox(width: 4),
                          Container(
                            width: 6,
                            height: 6,
                            decoration: BoxDecoration(
                              color: isSelected
                                  ? Colors.white
                                  : theme.colorScheme.primary,
                              shape: BoxShape.circle,
                            ),
                          ),
                        ],
                      ],
                    ),
                    selected: isSelected,
                    showCheckmark: false,
                    onSelected: (val) {
                      if (val) {
                        ref.read(selectedDayFilterProvider.notifier).state = day;
                      }
                    },
                  ),
                );
              }).toList(),
            ),
          ),
        ),

        // Subheader showing full day name
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                '${AppConstants.weekDayFullNames[selectedDay] ?? selectedDay}${isToday ? ' (Today)' : ''}',
                style: const TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.w700,
                  letterSpacing: -0.3,
                ),
              ),
              Text(
                '${scheduledClasses.length} ${scheduledClasses.length == 1 ? 'class' : 'classes'}',
                style: TextStyle(
                  fontSize: 12,
                  color: theme.colorScheme.onSurface.withOpacity(0.5),
                ),
              ),
            ],
          ),
        ),

        // Class items list
        Expanded(
          child: scheduledClasses.isEmpty
              ? EmptyState(
                  icon: Icons.event_available_rounded,
                  title: 'No classes on $selectedDay',
                  subtitle: 'You have no scheduled lectures or labs for this day.',
                  action: OutlinedButton.icon(
                    icon: const Icon(Icons.add, size: 16),
                    label: const Text('Add Course'),
                    onPressed: () => showDialog(
                      context: context,
                      builder: (ctx) => const EditCourseDialog(),
                    ),
                  ),
                )
              : ListView.separated(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  itemCount: scheduledClasses.length,
                  separatorBuilder: (_, __) => const SizedBox(height: 10),
                  itemBuilder: (context, index) {
                    final item = scheduledClasses[index];
                    final course = item.course;
                    final courseColor = AppConstants.parseHexColor(course.colorHex);
                    final todayRecord = item.todayRecord;

                    return AdroitCard(
                      onTap: () {
                        Navigator.of(context).push(MaterialPageRoute(
                          builder: (_) => CourseDetailScreen(courseId: course.id),
                        ));
                      },
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Container(
                                width: 4,
                                height: 40,
                                decoration: BoxDecoration(
                                  color: courseColor,
                                  borderRadius: BorderRadius.circular(2),
                                ),
                              ),
                              const SizedBox(width: 12),
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Row(
                                      children: [
                                        Expanded(
                                          child: Text(
                                            course.name,
                                            style: const TextStyle(
                                              fontSize: 16,
                                              fontWeight: FontWeight.w700,
                                              letterSpacing: -0.3,
                                            ),
                                          ),
                                        ),
                                        if (course.code.isNotEmpty)
                                          Text(
                                            course.code,
                                            style: TextStyle(
                                              fontSize: 12,
                                              fontWeight: FontWeight.w600,
                                              color: courseColor,
                                            ),
                                          ),
                                      ],
                                    ),
                                    const SizedBox(height: 4),
                                    Row(
                                      children: [
                                        const Icon(Icons.access_time_rounded,
                                            size: 14, color: Colors.grey),
                                        const SizedBox(width: 4),
                                        Text(
                                          '${course.startTime} - ${course.endTime}',
                                          style: const TextStyle(
                                            fontSize: 12,
                                            fontWeight: FontWeight.w600,
                                          ),
                                        ),
                                        if (course.room.isNotEmpty) ...[
                                          const SizedBox(width: 10),
                                          const Icon(Icons.room_outlined,
                                              size: 14, color: Colors.grey),
                                          const SizedBox(width: 2),
                                          Text(
                                            course.room,
                                            style: const TextStyle(fontSize: 12),
                                          ),
                                        ],
                                      ],
                                    ),
                                  ],
                                ),
                              ),
                            ],
                          ),

                          // Quick attendance row (enabled when viewing today)
                          if (isToday) ...[
                            const SizedBox(height: 12),
                            const Divider(height: 1),
                            const SizedBox(height: 10),
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Text(
                                  todayRecord != null
                                      ? 'Logged: ${todayRecord.status.toUpperCase()}'
                                      : 'Quick Log Attendance:',
                                  style: TextStyle(
                                    fontSize: 11,
                                    fontWeight: FontWeight.w600,
                                    color: theme.colorScheme.onSurface.withOpacity(0.6),
                                  ),
                                ),
                                Row(
                                  children: [
                                    _QuickAttendanceButton(
                                      status: AttendanceStatus.present,
                                      isSelected: todayRecord?.status.toLowerCase() == 'present',
                                      onTap: () {
                                        ref.read(databaseProvider).recordAttendance(
                                              courseId: course.id,
                                              dateMillis: AdroitDateUtils.toMidnightMillis(DateTime.now()),
                                              status: 'present',
                                            );
                                      },
                                    ),
                                    const SizedBox(width: 6),
                                    _QuickAttendanceButton(
                                      status: AttendanceStatus.absent,
                                      isSelected: todayRecord?.status.toLowerCase() == 'absent',
                                      onTap: () {
                                        ref.read(databaseProvider).recordAttendance(
                                              courseId: course.id,
                                              dateMillis: AdroitDateUtils.toMidnightMillis(DateTime.now()),
                                              status: 'absent',
                                            );
                                      },
                                    ),
                                    const SizedBox(width: 6),
                                    _QuickAttendanceButton(
                                      status: AttendanceStatus.cancelled,
                                      isSelected: todayRecord?.status.toLowerCase() == 'cancelled',
                                      onTap: () {
                                        ref.read(databaseProvider).recordAttendance(
                                              courseId: course.id,
                                              dateMillis: AdroitDateUtils.toMidnightMillis(DateTime.now()),
                                              status: 'cancelled',
                                            );
                                      },
                                    ),
                                  ],
                                ),
                              ],
                            ),
                          ],
                        ],
                      ),
                    );
                  },
                ),
        ),
      ],
    );
  }

  Widget _buildCoursesListView(
    BuildContext context,
    ThemeData theme,
    List<Course> courses,
  ) {
    if (courses.isEmpty) {
      return EmptyState(
        icon: Icons.school_outlined,
        title: 'No courses registered',
        subtitle: 'Add your classes and lectures to start tracking timetables and attendance.',
        action: FilledButton.icon(
          icon: const Icon(Icons.add),
          label: const Text('Add First Course'),
          onPressed: () => showDialog(
            context: context,
            builder: (ctx) => const EditCourseDialog(),
          ),
        ),
      );
    }

    return ListView.separated(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      itemCount: courses.length,
      separatorBuilder: (_, __) => const SizedBox(height: 10),
      itemBuilder: (context, index) {
        final course = courses[index];
        final courseColor = AppConstants.parseHexColor(course.colorHex);
        final analytics = ref.watch(courseAttendanceAnalyticsProvider(course.id));

        return AdroitCard(
          onTap: () {
            Navigator.of(context).push(MaterialPageRoute(
              builder: (_) => CourseDetailScreen(courseId: course.id),
            ));
          },
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Container(
                    width: 4,
                    height: 48,
                    decoration: BoxDecoration(
                      color: courseColor,
                      borderRadius: BorderRadius.circular(2),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Expanded(
                              child: Text(
                                course.name,
                                style: const TextStyle(
                                  fontSize: 16,
                                  fontWeight: FontWeight.w700,
                                  letterSpacing: -0.3,
                                ),
                              ),
                            ),
                            if (course.code.isNotEmpty)
                              Text(
                                course.code,
                                style: TextStyle(
                                  fontSize: 12,
                                  fontWeight: FontWeight.w600,
                                  color: courseColor,
                                ),
                              ),
                          ],
                        ),
                        const SizedBox(height: 4),
                        Text(
                          '${course.scheduleDays.isEmpty ? "No days set" : course.scheduleDays} • ${course.startTime} - ${course.endTime}',
                          style: TextStyle(
                            fontSize: 12,
                            color: theme.colorScheme.onSurface.withOpacity(0.6),
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              const Divider(height: 1),
              const SizedBox(height: 10),

              // Attendance stats footer
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Row(
                    children: [
                      Text(
                        '${analytics.percentage.toStringAsFixed(1)}%',
                        style: TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.w800,
                          color: analytics.health.color,
                        ),
                      ),
                      const SizedBox(width: 6),
                      Text(
                        '(${analytics.attendedClasses}/${analytics.totalClasses} classes)',
                        style: TextStyle(
                          fontSize: 11,
                          color: theme.colorScheme.onSurface.withOpacity(0.5),
                        ),
                      ),
                    ],
                  ),
                  StatusBadge(
                    label: analytics.health.label,
                    color: analytics.health.color,
                  ),
                ],
              ),
            ],
          ),
        );
      },
    );
  }
}

class _ViewTab extends StatelessWidget {
  final String label;
  final IconData icon;
  final bool isSelected;
  final VoidCallback onTap;

  const _ViewTab({
    required this.label,
    required this.icon,
    required this.isSelected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(10),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 180),
        decoration: BoxDecoration(
          color: isSelected ? theme.colorScheme.surface : Colors.transparent,
          borderRadius: BorderRadius.circular(10),
          boxShadow: isSelected
              ? [BoxShadow(color: Colors.black.withOpacity(0.04), blurRadius: 4)]
              : null,
        ),
        alignment: Alignment.center,
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              icon,
              size: 15,
              color: isSelected
                  ? theme.colorScheme.primary
                  : theme.colorScheme.onSurface.withOpacity(0.6),
            ),
            const SizedBox(width: 6),
            Text(
              label,
              style: TextStyle(
                fontSize: 12,
                fontWeight: isSelected ? FontWeight.w700 : FontWeight.w500,
                color: isSelected
                    ? theme.colorScheme.primary
                    : theme.colorScheme.onSurface.withOpacity(0.6),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _QuickAttendanceButton extends StatelessWidget {
  final AttendanceStatus status;
  final bool isSelected;
  final VoidCallback onTap;

  const _QuickAttendanceButton({
    required this.status,
    required this.isSelected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(6),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
        decoration: BoxDecoration(
          color: isSelected ? status.color : status.color.withOpacity(0.08),
          borderRadius: BorderRadius.circular(6),
          border: Border.all(
            color: isSelected ? status.color : status.color.withOpacity(0.3),
            width: 1,
          ),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              status.icon,
              size: 12,
              color: isSelected ? Colors.white : status.color,
            ),
            const SizedBox(width: 4),
            Text(
              status.label,
              style: TextStyle(
                fontSize: 10,
                fontWeight: FontWeight.w700,
                color: isSelected ? Colors.white : status.color,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
