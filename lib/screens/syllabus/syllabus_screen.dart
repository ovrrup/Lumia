import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/constants/app_constants.dart';
import '../../data/database/database.dart';
import '../../providers/courses_provider.dart';
import '../../providers/syllabus_provider.dart';
import '../../widgets/adroit_card.dart';
import '../../widgets/empty_state.dart';
import '../../widgets/progress_bar.dart';
import '../../widgets/radial_gauge.dart';
import '../../widgets/status_badge.dart';
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
                              'CURRICULUM',
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
                            '$completedTopics of $totalTopics Topics',
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
                        'Syllabus Coverage',
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
                    label: const Text('Add Subject', style: TextStyle(fontWeight: FontWeight.w700)),
                    onPressed: () => showDialog(
                      context: context,
                      builder: (ctx) => const EditSubjectDialog(),
                    ),
                    style: FilledButton.styleFrom(
                      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                    ),
                  ),
                ],
              ),
            ),

            Expanded(
              child: allSubjects.isEmpty
                  ? EmptyState(
                      icon: Icons.menu_book_rounded,
                      title: 'No academic subjects registered',
                      subtitle: 'Add subjects to track curriculum chapters and check off topics as you study.',
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
                      padding: const EdgeInsets.fromLTRB(20, 6, 20, 100),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          // Overall Progress Banner
                          AdroitCard(
                            padding: const EdgeInsets.all(20),
                            child: Row(
                              children: [
                                RadialProgressGauge(
                                  progress: overallProgress,
                                  size: 78,
                                  strokeWidth: 7.5,
                                  color: theme.colorScheme.primary,
                                  centerChild: Text(
                                    '$overallPercent%',
                                    style: TextStyle(
                                      fontSize: 18,
                                      fontWeight: FontWeight.w800,
                                      color: theme.colorScheme.primary,
                                    ),
                                  ),
                                ),
                                const SizedBox(width: 20),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      const Text(
                                        'Overall Mastery',
                                        style: TextStyle(
                                          fontSize: 17,
                                          fontWeight: FontWeight.w800,
                                          letterSpacing: -0.3,
                                        ),
                                      ),
                                      const SizedBox(height: 4),
                                      Text(
                                        '$completedTopics completed across ${allSubjects.length} subjects',
                                        style: TextStyle(
                                          fontSize: 12,
                                          fontWeight: FontWeight.w500,
                                          color: theme.colorScheme.onSurface.withOpacity(0.6),
                                        ),
                                      ),
                                      const SizedBox(height: 10),
                                      AdroitProgressBar(
                                        progress: overallProgress,
                                        height: 6,
                                      ),
                                    ],
                                  ),
                                ),
                              ],
                            ),
                          ),
                          const SizedBox(height: 20),

                          Text(
                            'Enrolled Subjects (${allSubjects.length})',
                            style: const TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.w800,
                              letterSpacing: -0.3,
                            ),
                          ),
                          const SizedBox(height: 12),

                          // Subjects list
                          ListView.separated(
                            shrinkWrap: true,
                            physics: const NeverScrollableScrollPhysics(),
                            itemCount: allSubjects.length,
                            separatorBuilder: (_, __) => const SizedBox(height: 12),
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
                                padding: const EdgeInsets.all(18),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Row(
                                      crossAxisAlignment: CrossAxisAlignment.start,
                                      children: [
                                        progressAsync.maybeWhen(
                                          data: (p) => RadialProgressGauge(
                                            progress: p.progress,
                                            size: 52,
                                            strokeWidth: 5,
                                            color: subjectColor,
                                            centerChild: Text(
                                              '${p.percentage}%',
                                              style: TextStyle(
                                                fontSize: 11,
                                                fontWeight: FontWeight.w800,
                                                color: subjectColor,
                                              ),
                                            ),
                                          ),
                                          orElse: () => Container(
                                            width: 52,
                                            height: 52,
                                            decoration: BoxDecoration(
                                              color: subjectColor.withOpacity(0.12),
                                              shape: BoxShape.circle,
                                            ),
                                            child: Icon(Icons.menu_book_rounded, color: subjectColor, size: 22),
                                          ),
                                        ),
                                        const SizedBox(width: 14),
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
                                                        fontWeight: FontWeight.w800,
                                                        letterSpacing: -0.3,
                                                      ),
                                                    ),
                                                  ),
                                                  if (subject.code.isNotEmpty)
                                                    Container(
                                                      padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 2),
                                                      decoration: BoxDecoration(
                                                        color: subjectColor.withOpacity(0.15),
                                                        borderRadius: BorderRadius.circular(6),
                                                      ),
                                                      child: Text(
                                                        subject.code,
                                                        style: TextStyle(
                                                          fontSize: 11,
                                                          fontWeight: FontWeight.w800,
                                                          color: subjectColor,
                                                        ),
                                                      ),
                                                    ),
                                                ],
                                              ),
                                              const SizedBox(height: 4),
                                              if (linkedCourse != null)
                                                Text(
                                                  'Class: ${linkedCourse.name}',
                                                  style: TextStyle(
                                                    fontSize: 12,
                                                    fontWeight: FontWeight.w600,
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

                                    // Units and topics footer
                                    progressAsync.maybeWhen(
                                      data: (p) => Row(
                                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                        children: [
                                          Text(
                                            '${p.chapters.length} UNITS • ${p.completedTopics}/${p.totalTopics} TOPICS',
                                            style: TextStyle(
                                              fontSize: 10,
                                              fontWeight: FontWeight.w800,
                                              letterSpacing: 0.6,
                                              color: theme.colorScheme.onSurface.withOpacity(0.5),
                                            ),
                                          ),
                                          StatusBadge(
                                            label: '${p.percentage}% MASTERED',
                                            color: subjectColor,
                                            showDot: false,
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
                        ],
                      ),
                    ),
            ),
          ],
        ),
      ),
    );
  }
}
