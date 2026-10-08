import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/database/database.dart';
import '../data/models/models.dart';
import 'database_provider.dart';
import 'courses_provider.dart';

final attendanceForCourseProvider =
    StreamProvider.family<List<AttendanceRecord>, int>((ref, courseId) {
  final db = ref.watch(databaseProvider);
  return db.watchAttendanceForCourse(courseId);
});

final courseAttendanceAnalyticsProvider =
    Provider.family<AttendanceAnalytics, int>((ref, courseId) {
  final records = ref.watch(attendanceForCourseProvider(courseId)).value ?? [];
  final course = ref.watch(courseByIdProvider(courseId)).value;
  final target = course?.targetAttendance ?? 75;

  return AttendanceAnalytics.fromRecords(
    records: records,
    targetPercentage: target,
  );
});

final overallAttendanceAnalyticsProvider = Provider<AttendanceAnalytics>((ref) {
  final allRecords = ref.watch(allAttendanceProvider).value ?? [];
  return AttendanceAnalytics.fromRecords(
    records: allRecords,
    targetPercentage: 75,
  );
});
