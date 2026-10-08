import 'package:flutter/material.dart';
import '../database/database.dart';

enum AttendanceStatus {
  present,
  absent,
  cancelled,
  late;

  String get label {
    switch (this) {
      case AttendanceStatus.present:
        return 'Present';
      case AttendanceStatus.absent:
        return 'Absent';
      case AttendanceStatus.cancelled:
        return 'Cancelled';
      case AttendanceStatus.late:
        return 'Late';
    }
  }

  IconData get icon {
    switch (this) {
      case AttendanceStatus.present:
        return Icons.check_circle_rounded;
      case AttendanceStatus.absent:
        return Icons.cancel_rounded;
      case AttendanceStatus.cancelled:
        return Icons.pause_circle_rounded;
      case AttendanceStatus.late:
        return Icons.schedule_rounded;
    }
  }

  Color get color {
    switch (this) {
      case AttendanceStatus.present:
        return const Color(0xFF10B981);
      case AttendanceStatus.absent:
        return const Color(0xFFEF4444);
      case AttendanceStatus.cancelled:
        return const Color(0xFF6B7280);
      case AttendanceStatus.late:
        return const Color(0xFFF59E0B);
    }
  }
}

enum AttendanceHealth {
  safe,
  warning,
  critical;

  String get label {
    switch (this) {
      case AttendanceHealth.safe:
        return 'On Track';
      case AttendanceHealth.warning:
        return 'Near Target';
      case AttendanceHealth.critical:
        return 'Attendance Shortage';
    }
  }

  Color get color {
    switch (this) {
      case AttendanceHealth.safe:
        return const Color(0xFF10B981);
      case AttendanceHealth.warning:
        return const Color(0xFFF59E0B);
      case AttendanceHealth.critical:
        return const Color(0xFFEF4444);
    }
  }
}

class AttendanceAnalytics {
  final int totalClasses;
  final int attendedClasses;
  final int missedClasses;
  final int cancelledClasses;
  final int lateClasses;
  final int targetPercentage;

  AttendanceAnalytics({
    required this.totalClasses,
    required this.attendedClasses,
    required this.missedClasses,
    required this.cancelledClasses,
    required this.lateClasses,
    this.targetPercentage = 75,
  });

  factory AttendanceAnalytics.fromRecords({
    required List<AttendanceRecord> records,
    int targetPercentage = 75,
  }) {
    int attended = 0;
    int missed = 0;
    int cancelled = 0;
    int lateCount = 0;

    for (final r in records) {
      switch (r.status.toLowerCase()) {
        case 'present':
          attended++;
          break;
        case 'absent':
          missed++;
          break;
        case 'cancelled':
          cancelled++;
          break;
        case 'late':
          attended++; // Counted as attended in most universities
          lateCount++;
          break;
      }
    }

    final total = attended + missed;

    return AttendanceAnalytics(
      totalClasses: total,
      attendedClasses: attended,
      missedClasses: missed,
      cancelledClasses: cancelled,
      lateClasses: lateCount,
      targetPercentage: targetPercentage,
    );
  }

  double get percentage {
    if (totalClasses == 0) return 100.0;
    return (attendedClasses / totalClasses) * 100.0;
  }

  AttendanceHealth get health {
    if (totalClasses == 0) return AttendanceHealth.safe;
    if (percentage >= targetPercentage) {
      if (percentage - targetPercentage <= 5) return AttendanceHealth.warning;
      return AttendanceHealth.safe;
    }
    return AttendanceHealth.critical;
  }

  /// Calculates how many future classes you can safely miss without falling below target
  int get safeBunksAllowed {
    if (totalClasses == 0) return 0;
    final target = targetPercentage / 100.0;
    if (percentage < targetPercentage) return 0;

    // attended / (total + x) >= target  ==>  attended / target - total >= x
    final maxTotal = attendedClasses / target;
    final canMiss = (maxTotal - totalClasses).floor();
    return canMiss >= 0 ? canMiss : 0;
  }

  /// Calculates how many consecutive classes you need to attend to recover to target
  int get classesToRecover {
    if (percentage >= targetPercentage) return 0;
    final target = targetPercentage / 100.0;

    // (attended + x) / (total + x) >= target
    // attended + x >= target * total + target * x
    // x * (1 - target) >= target * total - attended
    final needed = ((target * totalClasses - attendedClasses) / (1 - target)).ceil();
    return needed > 0 ? needed : 0;
  }
}

class ChapterWithTopics {
  final Chapter chapter;
  final List<Topic> topics;

  ChapterWithTopics({
    required this.chapter,
    required this.topics,
  });

  int get totalCount => topics.length;
  int get completedCount => topics.where((t) => t.isCompleted).length;
  double get progress => totalCount == 0 ? 0.0 : completedCount / totalCount;
}

class SubjectProgress {
  final Subject subject;
  final List<ChapterWithTopics> chapters;
  final Course? linkedCourse;

  SubjectProgress({
    required this.subject,
    required this.chapters,
    this.linkedCourse,
  });

  int get totalTopics =>
      chapters.fold(0, (sum, ch) => sum + ch.totalCount);
  int get completedTopics =>
      chapters.fold(0, (sum, ch) => sum + ch.completedCount);
  double get progress =>
      totalTopics == 0 ? 0.0 : completedTopics / totalTopics;
  int get percentage => (progress * 100).round();
}

class ScheduledClassItem {
  final Course course;
  final AttendanceRecord? todayRecord;
  final String dayOfWeek;
  final TimeOfDay startTime;
  final TimeOfDay endTime;

  ScheduledClassItem({
    required this.course,
    this.todayRecord,
    required this.dayOfWeek,
    required this.startTime,
    required this.endTime,
  });
}
