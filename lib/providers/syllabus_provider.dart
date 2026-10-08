import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/database/database.dart';
import '../data/models/models.dart';
import 'database_provider.dart';
import 'courses_provider.dart';

enum SyllabusFilter { all, pending, completed }

final allSubjectsProvider = StreamProvider<List<Subject>>((ref) {
  final db = ref.watch(databaseProvider);
  return db.watchAllSubjects();
});

final subjectByIdProvider = StreamProvider.family<Subject?, int>((ref, subjectId) {
  final db = ref.watch(databaseProvider);
  return db.watchSubjectById(subjectId);
});

final chaptersForSubjectProvider =
    StreamProvider.family<List<Chapter>, int>((ref, subjectId) {
  final db = ref.watch(databaseProvider);
  return db.watchChaptersForSubject(subjectId);
});

final topicsForChapterProvider =
    StreamProvider.family<List<Topic>, int>((ref, chapterId) {
  final db = ref.watch(databaseProvider);
  return db.watchTopicsForChapter(chapterId);
});

final allTopicsProvider = StreamProvider<List<Topic>>((ref) {
  final db = ref.watch(databaseProvider);
  return db.watchAllTopics();
});

final syllabusSearchQueryProvider = StateProvider<String>((ref) => '');
final syllabusFilterProvider = StateProvider<SyllabusFilter>((ref) => SyllabusFilter.all);

/// Detailed progress object for a specific subject
final subjectProgressProvider =
    Provider.family<AsyncValue<SubjectProgress>, int>((ref, subjectId) {
  final subjectAsync = ref.watch(subjectByIdProvider(subjectId));
  final chaptersAsync = ref.watch(chaptersForSubjectProvider(subjectId));
  final allTopicsAsync = ref.watch(allTopicsProvider);
  final allCoursesAsync = ref.watch(allCoursesProvider);

  if (subjectAsync.isLoading || chaptersAsync.isLoading || allTopicsAsync.isLoading) {
    return const AsyncValue.loading();
  }

  final subject = subjectAsync.value;
  if (subject == null) {
    return AsyncValue.error('Subject not found', StackTrace.current);
  }

  final chapters = chaptersAsync.value ?? [];
  final allTopics = allTopicsAsync.value ?? [];
  final courses = allCoursesAsync.value ?? [];

  Course? linkedCourse;
  if (subject.courseId != null) {
    try {
      linkedCourse = courses.firstWhere((c) => c.id == subject.courseId);
    } catch (_) {}
  }

  final chaptersWithTopics = chapters.map((ch) {
    final chTopics = allTopics.where((t) => t.chapterId == ch.id).toList();
    chTopics.sort((a, b) => a.orderIndex.compareTo(b.orderIndex));
    return ChapterWithTopics(chapter: ch, topics: chTopics);
  }).toList();

  return AsyncValue.data(SubjectProgress(
    subject: subject,
    chapters: chaptersWithTopics,
    linkedCourse: linkedCourse,
  ));
});
