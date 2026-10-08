import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/constants/app_constants.dart';
import '../../data/database/database.dart';
import '../../providers/courses_provider.dart';
import '../../providers/syllabus_provider.dart';
import '../../widgets/adroit_card.dart';
import '../../widgets/empty_state.dart';
import '../../widgets/progress_bar.dart';
import 'edit_subject_dialog.dart';
import 'subject_detail_screen.dart';

class SyllabusScreen extends ConsumerWidget {
  const SyllabusScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final allSubjects = ref.watch(allSubjectsProvider).value ?? [];
    final allCourses = ref.watch(allCoursesProvider).value ?? [];
    final allTopics = ref.watch(allTopicsProvider).value ?? [];

    final totalTopics = allTopics.length;
    final completedTopics = allTopics.where((t) => t.isCompleted).length;
    final overallProgress = totalTopics == 0 ? 0.0 : completedTopics / totalTopics;
    final overallPercent = (overallProgress * 100).round();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Syllabus & Curriculum'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add_rounded),
            tooltip: 'Add Subject',
            onPressed: () => showDialog(
              context: context,
              builder: (ctx) => const EditSubjectDialog(),
            ),
          ),
        ],
      ),
      body: allSubjects.isEmpty
          ? EmptyState(
              icon: Icons.menu_book_rounded,
              title: 'No subjects created',
              subtitle: 'Add academic subjects and modules to track your syllabus coverage step-by-step.',
              action: FilledButton.icon(
                icon: const Icon(Icons.add),
                label: const Text('Add Subject'),
                onPressed: () => showDialog(
                  context: context,
                  builder: (ctx) => const EditSubjectDialog(),
                ),
              ),
            )
          : SingleChildScrollView(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  // Overall Syllabus Overview Card
                  AdroitCard(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  '$overallPercent%',
                                  style: TextStyle(
                                    fontSize: 30,
                                    fontWeight: FontWeight.w800,
                                    letterSpacing: -1,
                                    color: theme.colorScheme.primary,
                                  ),
                                ),
                                Text(
                                  'Overall Syllabus Coverage',
                                  style: TextStyle(
                                    fontSize: 12,
                                    color: theme.colorScheme.onSurface.withOpacity(0.6),
                                  ),
                                ),
                              ],
                            ),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                              decoration: BoxDecoration(
                                color: theme.colorScheme.primary.withOpacity(0.12),
                                borderRadius: BorderRadius.circular(8),
                              ),
                              child: Text(
                                '$completedTopics / $totalTopics Topics',
                                style: TextStyle(
                                  fontSize: 12,
                                  fontWeight: FontWeight.w700,
                                  color: theme.colorScheme.primary,
                                ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 12),
                        AdroitProgressBar(
                          progress: overallProgress,
                          height: 7,
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 16),

                  Text(
                    'Subjects (${allSubjects.length})',
                    style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w700),
                  ),
                  const SizedBox(height: 10),

                  // Subjects list
                  ListView.separated(
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    itemCount: allSubjects.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 10),
                    itemBuilder: (context, index) {
                      final subject = allSubjects[index];
                      final subjectColor = AppConstants.parseHexColor(subject.colorHex);
                      final progressAsync = ref.watch(subjectProgressProvider(subject.id));

                      Course? linkedCourse;
                      if (subject.courseId != null) {
                        try {
                          linkedCourse = allCourses.firstWhere((c) => c.id == subject.courseId);
                        } catch (_) {}
                      }

                      return AdroitCard(
                        onTap: () {
                          Navigator.of(context).push(MaterialPageRoute(
                            builder: (_) => SubjectDetailScreen(subjectId: subject.id),
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
                                  height: 44,
                                  decoration: BoxDecoration(
                                    color: subjectColor,
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
                                              subject.name,
                                              style: const TextStyle(
                                                fontSize: 16,
                                                fontWeight: FontWeight.w700,
                                                letterSpacing: -0.3,
                                              ),
                                            ),
                                          ),
                                          if (subject.code.isNotEmpty)
                                            Text(
                                              subject.code,
                                              style: TextStyle(
                                                fontSize: 12,
                                                fontWeight: FontWeight.w600,
                                                color: subjectColor,
                                              ),
                                            ),
                                        ],
                                      ),
                                      const SizedBox(height: 4),
                                      if (linkedCourse != null)
                                        Text(
                                          'Class: ${linkedCourse.name}',
                                          style: TextStyle(
                                            fontSize: 11,
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

                            // Progress details
                            progressAsync.maybeWhen(
                              data: (p) => Column(
                                crossAxisAlignment: CrossAxisAlignment.stretch,
                                children: [
                                  Row(
                                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                    children: [
                                      Text(
                                        '${p.chapters.length} Units • ${p.completedTopics}/${p.totalTopics} Topics',
                                        style: TextStyle(
                                          fontSize: 11,
                                          color: theme.colorScheme.onSurface.withOpacity(0.5),
                                        ),
                                      ),
                                      Text(
                                        '${p.percentage}%',
                                        style: TextStyle(
                                          fontSize: 13,
                                          fontWeight: FontWeight.w800,
                                          color: subjectColor,
                                        ),
                                      ),
                                    ],
                                  ),
                                  const SizedBox(height: 6),
                                  AdroitProgressBar(
                                    progress: p.progress,
                                    color: subjectColor,
                                    height: 5,
                                  ),
                                ],
                              ),
                              orElse: () => const SizedBox.shrink(),
                            ),
                          ],
                        ),
                      );
                    },
                  ),
                  const SizedBox(height: 24),
                ],
              ),
            ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => showDialog(
          context: context,
          builder: (ctx) => const EditSubjectDialog(),
        ),
        child: const Icon(Icons.add),
      ),
    );
  }
}
