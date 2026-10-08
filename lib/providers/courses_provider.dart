import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../core/utils/date_utils.dart';
import '../data/database/database.dart';
import '../data/models/models.dart';
import 'database_provider.dart';

final allCoursesProvider = StreamProvider<List<Course>>((ref) {
  final db = ref.watch(databaseProvider);
  return db.watchAllCourses();
});

final courseByIdProvider = StreamProvider.family<Course?, int>((ref, courseId) {
  final db = ref.watch(databaseProvider);
  return db.watchCourseById(courseId);
});

final selectedDayFilterProvider = StateProvider<String>((ref) {
  return AdroitDateUtils.getCurrentShortWeekday();
});

/// List of scheduled classes for a specific day of the week
final classesForDayProvider = Provider.family<List<ScheduledClassItem>, String>((ref, day) {
  final coursesAsync = ref.watch(allCoursesProvider);
  final allAttendanceAsync = ref.watch(allAttendanceProvider);

  final courses = coursesAsync.value ?? [];
  final attendance = allAttendanceAsync.value ?? [];

  final todayMidnight = AdroitDateUtils.toMidnightMillis(DateTime.now());
  final isToday = day == AdroitDateUtils.getCurrentShortWeekday();

  final items = <ScheduledClassItem>[];

  for (final course in courses) {
    final days = course.scheduleDays.split(',').map((d) => d.trim()).toList();
    if (days.contains(day)) {
      AttendanceRecord? todayRecord;
      if (isToday) {
        try {
          todayRecord = attendance.firstWhere(
            (a) => a.courseId == course.id && a.dateMillis == todayMidnight,
          );
        } catch (_) {}
      }

      items.add(ScheduledClassItem(
        course: course,
        todayRecord: todayRecord,
        dayOfWeek: day,
        startTime: AdroitDateUtils.parseTime(course.startTime),
        endTime: AdroitDateUtils.parseTime(course.endTime),
      ));
    }
  }

  // Sort chronologically by start time
  items.sort((a, b) {
    final aMins = a.startTime.hour * 60 + a.startTime.minute;
    final bMins = b.startTime.hour * 60 + b.startTime.minute;
    return aMins.compareTo(bMins);
  });

  return items;
});

/// Convenience provider for today's classes
final todayClassesProvider = Provider<List<ScheduledClassItem>>((ref) {
  final today = AdroitDateUtils.getCurrentShortWeekday();
  return ref.watch(classesForDayProvider(today));
});

// All attendance stream
final allAttendanceProvider = StreamProvider<List<AttendanceRecord>>((ref) {
  final db = ref.watch(databaseProvider);
  return db.watchAllAttendance();
});
