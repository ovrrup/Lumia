import 'package:drift/drift.dart';
import 'package:drift_flutter/drift_flutter.dart';

part 'database.g.dart';

/// Table representing Courses / Classes enrolled by the student
class Courses extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get name => text()();
  TextColumn get code => text().withDefault(const Constant(''))();
  TextColumn get instructor => text().withDefault(const Constant(''))();
  TextColumn get room => text().withDefault(const Constant(''))();
  TextColumn get colorHex => text().withDefault(const Constant('#4F46E5'))();
  TextColumn get scheduleDays => text().withDefault(const Constant(''))(); // e.g. "Mon,Tue,Wed,Thu,Fri"
  TextColumn get startTime => text().withDefault(const Constant('09:00'))();
  TextColumn get endTime => text().withDefault(const Constant('10:00'))();
  IntColumn get targetAttendance => integer().withDefault(const Constant(75))();
  TextColumn get description => text().withDefault(const Constant(''))();
  DateTimeColumn get createdAt => dateTime().withDefault(currentDateAndTime)();
}

/// Table representing Class Attendance records
class AttendanceRecords extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get courseId => integer().references(Courses, #id, onDelete: KeyAction.cascade)();
  IntColumn get dateMillis => integer()(); // Epoch milliseconds at midnight
  TextColumn get status => text()(); // 'present', 'absent', 'cancelled', 'late'
}

/// Table representing Academic Subjects / Modules in the syllabus
class Subjects extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get name => text()();
  TextColumn get code => text().withDefault(const Constant(''))();
  TextColumn get colorHex => text().withDefault(const Constant('#06B6D4'))();
  IntColumn get courseId => integer().nullable().references(Courses, #id, onDelete: KeyAction.setNull)();
}

/// Table representing Chapters / Units within a Subject
class Chapters extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get subjectId => integer().references(Subjects, #id, onDelete: KeyAction.cascade)();
  TextColumn get title => text()();
  IntColumn get orderIndex => integer().withDefault(const Constant(0))();
}

/// Table representing Atomic Topics within a Chapter checklist
class Topics extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get chapterId => integer().references(Chapters, #id, onDelete: KeyAction.cascade)();
  TextColumn get title => text()();
  BoolColumn get isCompleted => boolean().withDefault(const Constant(false))();
  IntColumn get orderIndex => integer().withDefault(const Constant(0))();
  IntColumn get completedAtMillis => integer().nullable()();
}

/// Table representing Coursework Tasks & Assignments
class CourseTasks extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get title => text()();
  TextColumn get description => text().withDefault(const Constant(''))();
  IntColumn get courseId => integer().nullable().references(Courses, #id, onDelete: KeyAction.setNull)();
  IntColumn get subjectId => integer().nullable().references(Subjects, #id, onDelete: KeyAction.setNull)();
  IntColumn get topicId => integer().nullable().references(Topics, #id, onDelete: KeyAction.setNull)();
  IntColumn get dueDateMillis => integer().nullable()();
  IntColumn get priority => integer().withDefault(const Constant(0))(); // 0 = Low, 1 = Med, 2 = High
  BoolColumn get isCompleted => boolean().withDefault(const Constant(false))();
  IntColumn get createdAtMillis => integer()();
}

@DriftDatabase(tables: [
  Courses,
  AttendanceRecords,
  Subjects,
  Chapters,
  Topics,
  CourseTasks,
])
class AppDatabase extends _$AppDatabase {
  AppDatabase() : super(driftDatabase(name: 'adroit_database'));
  AppDatabase.forTesting(DatabaseConnection connection) : super(connection);

  @override
  int get schemaVersion => 1;

  // =========================================================================
  // Courses Operations
  // =========================================================================

  Stream<List<Course>> watchAllCourses() =>
      (select(courses)..orderBy([(t) => OrderingTerm(expression: t.name)])).watch();

  Future<List<Course>> getAllCourses() =>
      (select(courses)..orderBy([(t) => OrderingTerm(expression: t.name)])).get();

  Stream<Course?> watchCourseById(int id) =>
      (select(courses)..where((t) => t.id.equals(id))).watchSingleOrNull();

  Future<Course?> getCourseById(int id) =>
      (select(courses)..where((t) => t.id.equals(id))).getSingleOrNull();

  Future<int> insertCourse(CoursesCompanion companion) => into(courses).insert(companion);

  Future<bool> updateCourseData(Course entry) => update(courses).replace(entry);

  Future<int> deleteCourse(int id) => (delete(courses)..where((t) => t.id.equals(id))).go();

  // =========================================================================
  // Attendance Operations
  // =========================================================================

  Stream<List<AttendanceRecord>> watchAttendanceForCourse(int courseId) =>
      (select(attendanceRecords)
            ..where((t) => t.courseId.equals(courseId))
            ..orderBy([(t) => OrderingTerm.desc(t.dateMillis)]))
          .watch();

  Future<List<AttendanceRecord>> getAttendanceForCourse(int courseId) =>
      (select(attendanceRecords)..where((t) => t.courseId.equals(courseId))).get();

  Stream<List<AttendanceRecord>> watchAllAttendance() => select(attendanceRecords).watch();

  Future<AttendanceRecord?> getAttendanceForDate(int courseId, int dateMillis) =>
      (select(attendanceRecords)
            ..where((t) => t.courseId.equals(courseId) & t.dateMillis.equals(dateMillis)))
          .getSingleOrNull();

  Future<int> recordAttendance({
    required int courseId,
    required int dateMillis,
    required String status,
  }) async {
    // Delete existing record for this date to avoid duplicates
    await (delete(attendanceRecords)
          ..where((t) => t.courseId.equals(courseId) & t.dateMillis.equals(dateMillis)))
        .go();

    return into(attendanceRecords).insert(AttendanceRecordsCompanion.insert(
      courseId: courseId,
      dateMillis: dateMillis,
      status: status,
    ));
  }

  Future<int> deleteAttendanceRecord(int id) =>
      (delete(attendanceRecords)..where((t) => t.id.equals(id))).go();

  Future<int> deleteAttendanceForDate(int courseId, int dateMillis) =>
      (delete(attendanceRecords)
            ..where((t) => t.courseId.equals(courseId) & t.dateMillis.equals(dateMillis)))
          .go();

  // =========================================================================
  // Subjects Operations
  // =========================================================================

  Stream<List<Subject>> watchAllSubjects() =>
      (select(subjects)..orderBy([(t) => OrderingTerm(expression: t.name)])).watch();

  Future<List<Subject>> getAllSubjects() =>
      (select(subjects)..orderBy([(t) => OrderingTerm(expression: t.name)])).get();

  Stream<Subject?> watchSubjectById(int id) =>
      (select(subjects)..where((t) => t.id.equals(id))).watchSingleOrNull();

  Stream<List<Subject>> watchSubjectsForCourse(int courseId) =>
      (select(subjects)..where((t) => t.courseId.equals(courseId))).watch();

  Future<int> insertSubject(SubjectsCompanion companion) => into(subjects).insert(companion);

  Future<bool> updateSubjectData(Subject entry) => update(subjects).replace(entry);

  Future<int> deleteSubject(int id) => (delete(subjects)..where((t) => t.id.equals(id))).go();

  // =========================================================================
  // Chapters Operations
  // =========================================================================

  Stream<List<Chapter>> watchChaptersForSubject(int subjectId) =>
      (select(chapters)
            ..where((t) => t.subjectId.equals(subjectId))
            ..orderBy([(t) => OrderingTerm(expression: t.orderIndex)]))
          .watch();

  Future<List<Chapter>> getChaptersForSubject(int subjectId) =>
      (select(chapters)
            ..where((t) => t.subjectId.equals(subjectId))
            ..orderBy([(t) => OrderingTerm(expression: t.orderIndex)]))
          .get();

  Future<int> insertChapter(ChaptersCompanion companion) => into(chapters).insert(companion);

  Future<bool> updateChapterData(Chapter entry) => update(chapters).replace(entry);

  Future<int> deleteChapter(int id) => (delete(chapters)..where((t) => t.id.equals(id))).go();

  // =========================================================================
  // Topics Operations
  // =========================================================================

  Stream<List<Topic>> watchTopicsForChapter(int chapterId) =>
      (select(topics)
            ..where((t) => t.chapterId.equals(chapterId))
            ..orderBy([(t) => OrderingTerm(expression: t.orderIndex)]))
          .watch();

  Future<List<Topic>> getTopicsForChapter(int chapterId) =>
      (select(topics)
            ..where((t) => t.chapterId.equals(chapterId))
            ..orderBy([(t) => OrderingTerm(expression: t.orderIndex)]))
          .get();

  Stream<List<Topic>> watchAllTopics() => select(topics).watch();

  Future<List<Topic>> getAllTopics() => select(topics).get();

  Future<int> insertTopic(TopicsCompanion companion) => into(topics).insert(companion);

  Future<bool> updateTopicData(Topic entry) => update(topics).replace(entry);

  Future<int> deleteTopic(int id) => (delete(topics)..where((t) => t.id.equals(id))).go();

  Future<void> setTopicCompletion(int topicId, bool isCompleted) async {
    await (update(topics)..where((t) => t.id.equals(topicId))).write(
      TopicsCompanion(
        isCompleted: Value(isCompleted),
        completedAtMillis: Value(isCompleted ? DateTime.now().millisecondsSinceEpoch : null),
      ),
    );
  }

  // =========================================================================
  // Tasks Operations
  // =========================================================================

  Stream<List<CourseTask>> watchAllTasks() =>
      (select(courseTasks)..orderBy([(t) => OrderingTerm(expression: t.dueDateMillis)])).watch();

  Future<List<CourseTask>> getAllTasks() =>
      (select(courseTasks)..orderBy([(t) => OrderingTerm(expression: t.dueDateMillis)])).get();

  Stream<List<CourseTask>> watchTasksForCourse(int courseId) =>
      (select(courseTasks)..where((t) => t.courseId.equals(courseId))).watch();

  Future<int> insertTask(CourseTasksCompanion companion) => into(courseTasks).insert(companion);

  Future<bool> updateTaskData(CourseTask entry) => update(courseTasks).replace(entry);

  Future<int> deleteTask(int id) => (delete(courseTasks)..where((t) => t.id.equals(id))).go();

  Future<void> setTaskCompletion(int taskId, bool isCompleted) async {
    await (update(courseTasks)..where((t) => t.id.equals(taskId))).write(
      CourseTasksCompanion(isCompleted: Value(isCompleted)),
    );
  }

  // Clear all data
  Future<void> clearAllData() async {
    await delete(courseTasks).go();
    await delete(topics).go();
    await delete(chapters).go();
    await delete(subjects).go();
    await delete(attendanceRecords).go();
    await delete(courses).go();
  }
}
