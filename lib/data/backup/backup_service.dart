import 'dart:convert';
import 'package:drift/drift.dart';
import '../database/database.dart';

class BackupService {
  final AppDatabase db;

  BackupService(this.db);

  /// Exports current database into a clean JSON string
  Future<String> exportBackupJson() async {
    final allCourses = await db.getAllCourses();
    final allAttendance = await db.attendanceRecords.select().get();
    final allSubjects = await db.getAllSubjects();
    final allChapters = await db.chapters.select().get();
    final allTopics = await db.getAllTopics();
    final allTasks = await db.getAllTasks();

    final data = {
      'app': 'Adroit',
      'version': 1,
      'exportedAt': DateTime.now().toIso8601String(),
      'courses': allCourses.map((c) => c.toJson()).toList(),
      'attendance': allAttendance.map((a) => a.toJson()).toList(),
      'subjects': allSubjects.map((s) => s.toJson()).toList(),
      'chapters': allChapters.map((ch) => ch.toJson()).toList(),
      'topics': allTopics.map((t) => t.toJson()).toList(),
      'tasks': allTasks.map((tk) => tk.toJson()).toList(),
    };

    return const JsonEncoder.withIndent('  ').convert(data);
  }

  /// Restores from JSON string (supports Adroit and Lumia backup formats)
  Future<bool> importBackupJson(String jsonString) async {
    try {
      final Map<String, dynamic> root = jsonDecode(jsonString);

      // Support Lumia legacy format: check if it has 'fullAppBackupJson' or direct lists
      Map<String, dynamic> data = root;
      if (root.containsKey('fullAppBackupJson') && root['fullAppBackupJson'] != null) {
        data = jsonDecode(root['fullAppBackupJson']);
      }

      await db.clearAllData();

      // 1. Restore Courses
      final coursesList = (data['courses'] as List<dynamic>?) ?? [];
      for (final raw in coursesList) {
        final c = raw as Map<String, dynamic>;
        await db.insertCourse(CoursesCompanion.insert(
          name: c['name']?.toString() ?? 'Unnamed Course',
          code: Value(c['code']?.toString() ?? ''),
          instructor: Value(c['instructor']?.toString() ?? ''),
          room: Value(c['room']?.toString() ?? ''),
          colorHex: Value(c['colorHex']?.toString() ?? '#4F46E5'),
          scheduleDays: Value(c['scheduleDays']?.toString() ?? ''),
          startTime: Value(c['startTime']?.toString() ?? c['scheduleStartTime']?.toString() ?? '09:00'),
          endTime: Value(c['endTime']?.toString() ?? c['scheduleEndTime']?.toString() ?? '10:00'),
          targetAttendance: Value(c['targetAttendance'] as int? ?? 75),
          description: Value(c['description']?.toString() ?? ''),
        ));
      }

      // Re-fetch created courses to map legacy IDs if necessary
      final savedCourses = await db.getAllCourses();
      final courseMapByName = {for (final c in savedCourses) c.name.toLowerCase(): c.id};

      // 2. Restore Subjects
      final subjectsList = (data['subjects'] as List<dynamic>?) ?? [];
      final oldSubjectIdToNew = <int, int>{};

      for (final raw in subjectsList) {
        final s = raw as Map<String, dynamic>;
        final oldId = s['id'] as int?;
        final name = s['name']?.toString() ?? 'Unnamed Subject';

        // Check course link
        int? linkedCourseId;
        final cId = s['courseId'] as int?;
        if (cId != null && courseMapByName.containsKey(name.toLowerCase())) {
          linkedCourseId = courseMapByName[name.toLowerCase()];
        }

        final newSubId = await db.insertSubject(SubjectsCompanion.insert(
          name: name,
          code: Value(s['code']?.toString() ?? ''),
          colorHex: Value(s['colorHex']?.toString() ?? '#06B6D4'),
          courseId: Value(linkedCourseId),
        ));

        if (oldId != null) {
          oldSubjectIdToNew[oldId] = newSubId;
        }
      }

      // 3. Restore Chapters
      final chaptersList = (data['chapters'] as List<dynamic>?) ?? [];
      final oldChapterIdToNew = <int, int>{};

      for (final raw in chaptersList) {
        final ch = raw as Map<String, dynamic>;
        final oldId = ch['id'] as int?;
        final oldSubId = ch['subjectId'] as int?;
        final newSubId = oldSubjectIdToNew[oldSubId] ?? (savedCourses.isNotEmpty ? savedCourses.first.id : 1);

        final newChId = await db.insertChapter(ChaptersCompanion.insert(
          subjectId: newSubId,
          title: ch['title']?.toString() ?? ch['name']?.toString() ?? 'Unit',
          orderIndex: Value(ch['orderIndex'] as int? ?? 0),
        ));

        if (oldId != null) {
          oldChapterIdToNew[oldId] = newChId;
        }
      }

      // 4. Restore Topics
      final topicsList = (data['topics'] as List<dynamic>?) ?? [];
      for (final raw in topicsList) {
        final t = raw as Map<String, dynamic>;
        final oldChId = t['chapterId'] as int?;
        var newChId = oldChapterIdToNew[oldChId];

        // If no chapter found, create an Unassigned unit in the subject if possible
        if (newChId == null) {
          final oldSubId = t['subjectId'] as int?;
          final newSubId = oldSubjectIdToNew[oldSubId];
          if (newSubId != null) {
            newChId = await db.insertChapter(ChaptersCompanion.insert(
              subjectId: newSubId,
              title: 'General Topics',
            ));
            if (oldChId != null) {
              oldChapterIdToNew[oldChId] = newChId;
            }
          }
        }

        if (newChId != null) {
          await db.insertTopic(TopicsCompanion.insert(
            chapterId: newChId,
            title: t['title']?.toString() ?? 'Topic',
            isCompleted: Value(t['isCompleted'] == true || t['isCompleted'] == 1),
            orderIndex: Value(t['orderIndex'] as int? ?? 0),
          ));
        }
      }

      // 5. Restore Attendance
      final attendanceList = (data['attendance'] as List<dynamic>?) ?? [];
      for (final raw in attendanceList) {
        final a = raw as Map<String, dynamic>;
        final cId = a['courseId'] as int?;
        final date = a['dateMillis'] as int? ?? (a['date'] as int? ?? 0);
        final status = a['status']?.toString() ?? 'Present';

        if (cId != null && date > 0) {
          await db.recordAttendance(
            courseId: cId,
            dateMillis: date,
            status: status,
          );
        }
      }

      // 6. Restore Tasks / Assignments
      final tasksList = (data['tasks'] as List<dynamic>?) ??
          (data['assignments'] as List<dynamic>?) ??
          [];
      for (final raw in tasksList) {
        final tk = raw as Map<String, dynamic>;
        await db.insertTask(CourseTasksCompanion.insert(
          title: tk['title']?.toString() ?? 'Task',
          description: Value(tk['description']?.toString() ?? ''),
          courseId: Value(tk['courseId'] as int?),
          dueDateMillis: Value(tk['dueDateMillis'] as int? ?? (tk['dueDate'] as int?)),
          priority: Value(tk['priority'] as int? ?? 0),
          isCompleted: Value(tk['isCompleted'] == true || tk['isCompleted'] == 1),
          createdAtMillis: tk['createdAtMillis'] as int? ?? DateTime.now().millisecondsSinceEpoch,
        ));
      }

      return true;
    } catch (e) {
      return false;
    }
  }
}
