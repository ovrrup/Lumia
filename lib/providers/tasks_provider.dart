import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/database/database.dart';
import 'database_provider.dart';

enum TaskFilter { upcoming, all, overdue, completed }

final allTasksProvider = StreamProvider<List<CourseTask>>((ref) {
  final db = ref.watch(databaseProvider);
  return db.watchAllTasks();
});

final taskFilterProvider = StateProvider<TaskFilter>((ref) => TaskFilter.upcoming);

final filteredTasksProvider = Provider<List<CourseTask>>((ref) {
  final tasks = ref.watch(allTasksProvider).value ?? [];
  final filter = ref.watch(taskFilterProvider);
  final nowMillis = DateTime.now().millisecondsSinceEpoch;

  switch (filter) {
    case TaskFilter.upcoming:
      return tasks.where((t) => !t.isCompleted).toList();
    case TaskFilter.overdue:
      return tasks.where((t) =>
          !t.isCompleted &&
          t.dueDateMillis != null &&
          t.dueDateMillis! < nowMillis).toList();
    case TaskFilter.completed:
      return tasks.where((t) => t.isCompleted).toList();
    case TaskFilter.all:
      return tasks;
  }
});
