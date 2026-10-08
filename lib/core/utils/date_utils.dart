import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

class AdroitDateUtils {
  /// Normalizes a DateTime to midnight (00:00:00.000) epoch milliseconds
  static int toMidnightMillis(DateTime date) {
    final clean = DateTime(date.year, date.month, date.day);
    return clean.millisecondsSinceEpoch;
  }

  /// Converts midnight epoch milliseconds back to DateTime
  static DateTime fromMillis(int millis) {
    return DateTime.fromMillisecondsSinceEpoch(millis);
  }

  /// Returns current 3-letter weekday abbreviation, e.g. "Mon", "Tue"
  static String getCurrentShortWeekday([DateTime? date]) {
    final target = date ?? DateTime.now();
    return DateFormat('E').format(target); // 'Mon', 'Tue', etc.
  }

  /// Parses "09:30" string to TimeOfDay
  static TimeOfDay parseTime(String timeStr) {
    try {
      final parts = timeStr.trim().split(':');
      if (parts.length >= 2) {
        return TimeOfDay(
          hour: int.parse(parts[0]),
          minute: int.parse(parts[1]),
        );
      }
    } catch (_) {}
    return const TimeOfDay(hour: 9, minute: 0);
  }

  /// Formats TimeOfDay to 24-hr "09:30" string
  static String formatTime24(TimeOfDay time) {
    final hour = time.hour.toString().padLeft(2, '0');
    final minute = time.minute.toString().padLeft(2, '0');
    return '$hour:$minute';
  }

  /// Formats TimeOfDay to user-friendly "9:30 AM"
  static String formatTime12(BuildContext context, TimeOfDay time) {
    return time.format(context);
  }

  /// Formats date millis to human friendly string: "Today", "Yesterday", or "Oct 8, 2026"
  static String formatRelativeDate(int millis) {
    final date = DateTime.fromMillisecondsSinceEpoch(millis);
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);
    final target = DateTime(date.year, date.month, date.day);

    final difference = target.difference(today).inDays;
    if (difference == 0) return 'Today';
    if (difference == 1) return 'Tomorrow';
    if (difference == -1) return 'Yesterday';
    if (difference > 1 && difference < 7) {
      return DateFormat('EEEE').format(date); // 'Friday'
    }

    return DateFormat('MMM d, yyyy').format(date);
  }

  /// Deadlines formatting: "Overdue by 2d", "Due in 3h", "Due Today"
  static String formatDeadline(int millis) {
    final due = DateTime.fromMillisecondsSinceEpoch(millis);
    final now = DateTime.now();
    final diff = due.difference(now);

    if (diff.isNegative) {
      final days = diff.inDays.abs();
      if (days == 0) return 'Overdue';
      return 'Overdue by ${days}d';
    }

    if (diff.inDays == 0) {
      if (diff.inHours <= 1) return 'Due in ${diff.inMinutes}m';
      return 'Due in ${diff.inHours}h';
    }
    if (diff.inDays == 1) return 'Due tomorrow';
    return 'Due in ${diff.inDays}d';
  }
}
