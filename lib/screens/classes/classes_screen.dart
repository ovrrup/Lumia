import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/constants/app_constants.dart';
import '../../core/utils/date_utils.dart';
import '../../data/database/database.dart';
import '../../data/models/models.dart';
import '../../providers/attendance_provider.dart';
import '../../providers/courses_provider.dart';
import '../../providers/database_provider.dart';
import '../../widgets/adroit_card.dart';
import '../../widgets/empty_state.dart';
import '../../widgets/radial_gauge.dart';
import '../../widgets/status_badge.dart';
import 'course_detail_screen.dart';
import 'edit_course_dialog.dart';

class ClassesScreen extends ConsumerStatefulWidget {
  const ClassesScreen({super.key});

  @override
  ConsumerState<ClassesScreen> createState() => _ClassesScreenState();
}

class _ClassesScreenState extends ConsumerState<ClassesScreen> {
  int _activeViewIndex = 0; // 0: Agenda, 1: Enrolled Courses

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final allCourses = ref.watch(allCoursesProvider).value ?? [];
    final selectedDay = ref.watch(selectedDayFilterProvider);
    final scheduledClasses = ref.watch(classesForDayProvider(selectedDay));
    final currentDayOfWeek = AdroitDateUtils.getCurrentShortWeekday();
    final isToday = selectedDay == currentDayOfWeek;

    return Scaffold(
      body: SafeArea(
        bottom: false,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Top Executive Header
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 16, 20, 12),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 2),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.primary.withOpacity(0.12),
                              borderRadius: BorderRadius.circular(6),
                            ),
                            child: Text(
                              'ACADEMIC',
                              style: TextStyle(
                                fontSize: 10,
                                fontWeight: FontWeight.w800,
                                letterSpacing: 0.8,
                                color: theme.colorScheme.primary,
                              ),
                            ),
                          ),
                          const SizedBox(width: 8),
                          Text(
                            AdroitDateUtils.formatRelativeDate(
                              AdroitDateUtils.toMidnightMillis(DateTime.now()),
                            ),
                            style: TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.w600,
                              color: theme.colorScheme.onSurface.withOpacity(0.5),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 4),
                      const Text(
                        'Classes & Agenda',
                        style: TextStyle(
                          fontSize: 24,
                          fontWeight: FontWeight.w800,
                          letterSpacing: -0.7,
                        ),
                      ),
                    ],
                  ),
                  FilledButton.icon(
                    icon: const Icon(Icons.add, size: 18),
                    label: const Text('Add Class', style: TextStyle(fontWeight: FontWeight.w700)),
                    onPressed: () => showDialog(
                      context: context,
                      builder: (ctx) => const EditCourseDialog(),
                    ),
                    style: FilledButton.styleFrom(
                      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                    ),
                  ),
                ],
              ),
            ),

            // Segmented View Switcher
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 6),
              child: Container(
                height: 44,
                padding: const EdgeInsets.all(4),
                decoration: BoxDecoration(
                  color: theme.colorScheme.surfaceVariant,
                  borderRadius: BorderRadius.circular(14),
                  border: Border.all(color: theme.colorScheme.outline),
                ),
                child: Row(
                  children: [
                    Expanded(
                      child: _SegmentTab(
                        title: 'Timetable Agenda',
                        icon: Icons.calendar_view_day_rounded,
                        isSelected: _activeViewIndex == 0,
                        onTap: () => setState(() => _activeViewIndex = 0),
                      ),
                    ),
                    Expanded(
                      child: _SegmentTab(
                        title: 'All Courses (${allCourses.length})',
                        icon: Icons.school_rounded,
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
        // Days of week calendar strip
        Container(
          height: 68,
          padding: const EdgeInsets.symmetric(vertical: 8),
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 20),
            itemCount: AppConstants.weekDays.length,
            separatorBuilder: (_, __) => const SizedBox(width: 8),
            itemBuilder: (context, index) {
              final day = AppConstants.weekDays[index];
              final isSelected = selectedDay == day;
              final isCurrentDay = currentDayOfWeek == day;

              return InkWell(
                onTap: () {
                  ref.read(selectedDayFilterProvider.notifier).state = day;
                },
                borderRadius: BorderRadius.circular(14),
                child: AnimatedContainer(
                  duration: const Duration(milliseconds: 180),
                  width: 52,
                  decoration: BoxDecoration(
                    color: isSelected
                        ? theme.colorScheme.primary
                        : (isCurrentDay
                            ? theme.colorScheme.primary.withOpacity(0.12)
                            : theme.colorScheme.surface),
                    borderRadius: BorderRadius.circular(14),
                    border: Border.all(
                      color: isSelected
                          ? theme.colorScheme.primary
                          : (isCurrentDay
                              ? theme.colorScheme.primary.withOpacity(0.4)
                              : theme.colorScheme.outline),
                      width: 1,
                    ),
                    boxShadow: isSelected
                        ? [
                            BoxShadow(
                              color: theme.colorScheme.primary.withOpacity(0.3),
                              blurRadius: 8,
                              offset: const Offset(0, 3),
                            ),
                          ]
                        : null,
                  ),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(
                        day.toUpperCase(),
                        style: TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.w800,
                          letterSpacing: 0.5,
                          color: isSelected
                              ? Colors.white
                              : (isCurrentDay
                                  ? theme.colorScheme.primary
                                  : theme.colorScheme.onSurface.withOpacity(0.6)),
                        ),
                      ),
                      const SizedBox(height: 4),
                      Container(
                        width: 5,
                        height: 5,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          color: isSelected
                              ? Colors.white
                              : (isCurrentDay ? theme.colorScheme.primary : Colors.transparent),
                        ),
                      ),
                    ],
                  ),
                ),
              );
            },
          ),
        ),

        // Subheader showing day info & class count
        Padding(
          padding: const EdgeInsets.fromLTRB(20, 8, 20, 10),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                '${AppConstants.weekDayFullNames[selectedDay] ?? selectedDay}${isToday ? ' • Today' : ''}',
                style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w800,
                  letterSpacing: -0.3,
                ),
              ),
              StatusBadge(
                label: '${scheduledClasses.length} ${scheduledClasses.length == 1 ? 'LECTURE' : 'LECTURES'}',
                color: theme.colorScheme.primary,
                showDot: false,
              ),
            ],
          ),
        ),

        // Class items list
        Expanded(
          child: scheduledClasses.isEmpty
              ? EmptyState(
                  icon: Icons.event_available_rounded,
                  title: 'No lectures on $selectedDay',
                  subtitle: 'You have no scheduled classes for this day. Enjoy your study time!',
                  action: OutlinedButton.icon(
                    icon: const Icon(Icons.add, size: 16),
                    label: const Text('Add Lecture to Schedule'),
                    onPressed: () => showDialog(
                      context: context,
                      builder: (ctx) => const EditCourseDialog(),
                    ),
                  ),
                )
              : ListView.separated(
                  padding: const EdgeInsets.fromLTRB(20, 4, 20, 100),
                  itemCount: scheduledClasses.length,
                  separatorBuilder: (_, __) => const SizedBox(height: 12),
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
                      padding: const EdgeInsets.all(18),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              // Left time indicator column
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
                                decoration: BoxDecoration(
                                  color: courseColor.withOpacity(0.12),
                                  borderRadius: BorderRadius.circular(12),
                                  border: Border.all(color: courseColor.withOpacity(0.25)),
                                ),
                                child: Column(
                                  children: [
                                    Text(
                                      course.startTime,
                                      style: TextStyle(
                                        fontSize: 13,
                                        fontWeight: FontWeight.w800,
                                        color: courseColor,
                                      ),
                                    ),
                                    Container(
                                      height: 10,
                                      width: 1.5,
                                      color: courseColor.withOpacity(0.4),
                                      margin: const EdgeInsets.symmetric(vertical: 2),
                                    ),
                                    Text(
                                      course.endTime,
                                      style: TextStyle(
                                        fontSize: 11,
                                        fontWeight: FontWeight.w700,
                                        color: courseColor.withOpacity(0.8),
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                              const SizedBox(width: 14),

                              // Course Details
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
                                              fontWeight: FontWeight.w800,
                                              letterSpacing: -0.4,
                                            ),
                                          ),
                                        ),
                                        if (course.code.isNotEmpty) ...[
                                          const SizedBox(width: 6),
                                          Container(
                                            padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 2),
                                            decoration: BoxDecoration(
                                              color: courseColor.withOpacity(0.15),
                                              borderRadius: BorderRadius.circular(6),
                                            ),
                                            child: Text(
                                              course.code,
                                              style: TextStyle(
                                                fontSize: 11,
                                                fontWeight: FontWeight.w800,
                                                color: courseColor,
                                              ),
                                            ),
                                          ),
                                        ],
                                      ],
                                    ),
                                    const SizedBox(height: 6),

                                    // Room and Instructor
                                    Wrap(
                                      spacing: 12,
                                      children: [
                                        if (course.room.isNotEmpty)
                                          Row(
                                            mainAxisSize: MainAxisSize.min,
                                            children: [
                                              Icon(Icons.room_rounded, size: 14, color: theme.colorScheme.onSurface.withOpacity(0.4)),
                                              const SizedBox(width: 4),
                                              Text(
                                                course.room,
                                                style: TextStyle(
                                                  fontSize: 12,
                                                  fontWeight: FontWeight.w600,
                                                  color: theme.colorScheme.onSurface.withOpacity(0.7),
                                                ),
                                              ),
                                            ],
                                          ),
                                        if (course.instructor.isNotEmpty)
                                          Row(
                                            mainAxisSize: MainAxisSize.min,
                                            children: [
                                              Icon(Icons.person_rounded, size: 14, color: theme.colorScheme.onSurface.withOpacity(0.4)),
                                              const SizedBox(width: 4),
                                              Text(
                                                course.instructor,
                                                style: TextStyle(
                                                  fontSize: 12,
                                                  fontWeight: FontWeight.w500,
                                                  color: theme.colorScheme.onSurface.withOpacity(0.6),
                                                ),
                                              ),
                                            ],
                                          ),
                                      ],
                                    ),
                                  ],
                                ),
                              ),
                            ],
                          ),

                          // Quick attendance row for today
                          if (isToday) ...[
                            const SizedBox(height: 16),
                            const Divider(height: 1),
                            const SizedBox(height: 12),
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Text(
                                  todayRecord != null
                                      ? 'STATUS: ${todayRecord.status.toUpperCase()}'
                                      : 'LOG ATTENDANCE',
                                  style: TextStyle(
                                    fontSize: 10,
                                    fontWeight: FontWeight.w800,
                                    letterSpacing: 0.6,
                                    color: theme.colorScheme.onSurface.withOpacity(0.45),
                                  ),
                                ),
                                // Segmented action pills
                                Row(
                                  children: [
                                    _AttendancePill(
                                      label: 'Present',
                                      icon: Icons.check_circle_rounded,
                                      color: const Color(0xFF10B981),
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
                                    _AttendancePill(
                                      label: 'Absent',
                                      icon: Icons.cancel_rounded,
                                      color: const Color(0xFFEF4444),
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
                                    _AttendancePill(
                                      label: 'Skip',
                                      icon: Icons.pause_circle_rounded,
                                      color: const Color(0xFF6B7280),
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
        icon: Icons.school_rounded,
        title: 'No courses registered',
        subtitle: 'Enroll your academic classes and lectures to start monitoring attendance and schedules.',
        action: FilledButton.icon(
          icon: const Icon(Icons.add),
          label: const Text('Add Course'),
          onPressed: () => showDialog(
            context: context,
            builder: (ctx) => const EditCourseDialog(),
          ),
        ),
      );
    }

    return ListView.separated(
      padding: const EdgeInsets.fromLTRB(20, 8, 20, 100),
      itemCount: courses.length,
      separatorBuilder: (_, __) => const SizedBox(height: 12),
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
          padding: const EdgeInsets.all(18),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  RadialProgressGauge(
                    progress: analytics.percentage / 100.0,
                    size: 56,
                    strokeWidth: 5.5,
                    color: analytics.health.color,
                    centerChild: Text(
                      '${analytics.percentage.round()}%',
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.w800,
                        color: analytics.health.color,
                      ),
                    ),
                  ),
                  const SizedBox(width: 16),
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
                                  fontSize: 17,
                                  fontWeight: FontWeight.w800,
                                  letterSpacing: -0.4,
                                ),
                              ),
                            ),
                            if (course.code.isNotEmpty)
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 2),
                                decoration: BoxDecoration(
                                  color: courseColor.withOpacity(0.12),
                                  borderRadius: BorderRadius.circular(6),
                                ),
                                child: Text(
                                  course.code,
                                  style: TextStyle(
                                    fontSize: 11,
                                    fontWeight: FontWeight.w800,
                                    color: courseColor,
                                  ),
                                ),
                              ),
                          ],
                        ),
                        const SizedBox(height: 4),
                        Text(
                          '${course.scheduleDays.isEmpty ? "No days" : course.scheduleDays} • ${course.startTime} - ${course.endTime}',
                          style: TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.w500,
                            color: theme.colorScheme.onSurface.withOpacity(0.55),
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 14),
              const Divider(height: 1),
              const SizedBox(height: 12),

              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Row(
                    children: [
                      Text(
                        '${analytics.attendedClasses}/${analytics.totalClasses} ATTENDED',
                        style: TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.w800,
                          letterSpacing: 0.5,
                          color: theme.colorScheme.onSurface.withOpacity(0.5),
                        ),
                      ),
                    ],
                  ),
                  StatusBadge(
                    label: analytics.health == AttendanceHealth.critical
                        ? 'NEED ${analytics.classesToRecover} CLASSES'
                        : '${analytics.safeBunksAllowed} SAFE BUNKS',
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

class _SegmentTab extends StatelessWidget {
  final String title;
  final IconData icon;
  final bool isSelected;
  final VoidCallback onTap;

  const _SegmentTab({
    required this.title,
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
              ? [BoxShadow(color: Colors.black.withOpacity(0.08), blurRadius: 4, offset: const Offset(0, 1))]
              : null,
        ),
        alignment: Alignment.center,
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              icon,
              size: 15,
              color: isSelected ? theme.colorScheme.primary : theme.colorScheme.onSurface.withOpacity(0.5),
            ),
            const SizedBox(width: 6),
            Text(
              title,
              style: TextStyle(
                fontSize: 12,
                fontWeight: isSelected ? FontWeight.w800 : FontWeight.w600,
                color: isSelected ? theme.colorScheme.primary : theme.colorScheme.onSurface.withOpacity(0.6),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _AttendancePill extends StatelessWidget {
  final String label;
  final IconData icon;
  final Color color;
  final bool isSelected;
  final VoidCallback onTap;

  const _AttendancePill({
    required this.label,
    required this.icon,
    required this.color,
    required this.isSelected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(20),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 160),
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
        decoration: BoxDecoration(
          color: isSelected ? color : color.withOpacity(0.08),
          borderRadius: BorderRadius.circular(20),
          border: Border.all(
            color: isSelected ? color : color.withOpacity(0.25),
            width: 1,
          ),
          boxShadow: isSelected
              ? [BoxShadow(color: color.withOpacity(0.35), blurRadius: 6, offset: const Offset(0, 2))]
              : null,
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icon, size: 13, color: isSelected ? Colors.white : color),
            const SizedBox(width: 4),
            Text(
              label,
              style: TextStyle(
                fontSize: 11,
                fontWeight: FontWeight.w800,
                color: isSelected ? Colors.white : color,
                letterSpacing: 0.2,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
