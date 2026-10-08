import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/constants/app_constants.dart';
import '../../data/database/database.dart';
import '../../data/models/models.dart';
import '../../providers/database_provider.dart';
import '../../providers/syllabus_provider.dart';
import '../../widgets/adroit_card.dart';
import '../../widgets/empty_state.dart';
import '../../widgets/progress_bar.dart';
import '../../widgets/radial_gauge.dart';
import '../../widgets/status_badge.dart';
import 'edit_chapter_dialog.dart';
import 'edit_subject_dialog.dart';

class SubjectDetailScreen extends ConsumerStatefulWidget {
  final int subjectId;

  const SubjectDetailScreen({super.key, required this.subjectId});

  @override
  ConsumerState<SubjectDetailScreen> createState() => _SubjectDetailScreenState();
}

class _SubjectDetailScreenState extends ConsumerState<SubjectDetailScreen> {
  String _searchQuery = '';
  SyllabusFilter _filter = SyllabusFilter.all;

  Future<void> _confirmDeleteSubject(BuildContext context, Subject subject) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Delete Subject?'),
        content: Text('Delete "${subject.name}" and all associated units and topics?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(ctx).pop(true),
            style: FilledButton.styleFrom(backgroundColor: Colors.red),
            child: const Text('Delete'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      final db = ref.read(databaseProvider);
      await db.deleteSubject(subject.id);
      if (mounted) Navigator.of(context).pop();
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final progressAsync = ref.watch(subjectProgressProvider(widget.subjectId));

    return progressAsync.when(
      loading: () => const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      ),
      error: (e, _) => Scaffold(
        appBar: AppBar(),
        body: Center(child: Text('Error: $e')),
      ),
      data: (subjectProgress) {
        final subject = subjectProgress.subject;
        final chapters = subjectProgress.chapters;
        final subjectColor = AppConstants.parseHexColor(subject.colorHex);

        return Scaffold(
          appBar: AppBar(
            title: Text(subject.name),
            actions: [
              IconButton(
                icon: const Icon(Icons.edit_outlined),
                tooltip: 'Edit Subject',
                onPressed: () => showDialog(
                  context: context,
                  builder: (ctx) => EditSubjectDialog(initialSubject: subject),
                ),
              ),
              IconButton(
                icon: const Icon(Icons.delete_outline_rounded, color: Colors.redAccent),
                tooltip: 'Delete Subject',
                onPressed: () => _confirmDeleteSubject(context, subject),
              ),
            ],
          ),
          body: SingleChildScrollView(
            padding: const EdgeInsets.fromLTRB(20, 8, 20, 60),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // Top Progress Card
                AdroitCard(
                  padding: const EdgeInsets.all(20),
                  child: Row(
                    children: [
                      RadialProgressGauge(
                        progress: subjectProgress.progress,
                        size: 80,
                        strokeWidth: 8,
                        color: subjectColor,
                        centerChild: Text(
                          '${subjectProgress.percentage}%',
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.w800,
                            color: subjectColor,
                          ),
                        ),
                      ),
                      const SizedBox(width: 20),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              subject.name,
                              style: const TextStyle(
                                fontSize: 18,
                                fontWeight: FontWeight.w800,
                                letterSpacing: -0.4,
                              ),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              '${subjectProgress.completedTopics} of ${subjectProgress.totalTopics} topics mastered',
                              style: TextStyle(
                                fontSize: 12,
                                fontWeight: FontWeight.w600,
                                color: theme.colorScheme.onSurface.withOpacity(0.55),
                              ),
                            ),
                            const SizedBox(height: 10),
                            AdroitProgressBar(
                              progress: subjectProgress.progress,
                              color: subjectColor,
                              height: 6,
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 18),

                // Search Bar
                TextField(
                  decoration: InputDecoration(
                    hintText: 'Filter curriculum topics...',
                    prefixIcon: const Icon(Icons.search_rounded, size: 20),
                    isDense: true,
                    contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                  ),
                  onChanged: (val) => setState(() => _searchQuery = val.trim().toLowerCase()),
                ),
                const SizedBox(height: 12),

                // Filter Chips Row
                Row(
                  children: [
                    _FilterTab(
                      label: 'All Topics',
                      isSelected: _filter == SyllabusFilter.all,
                      onTap: () => setState(() => _filter = SyllabusFilter.all),
                    ),
                    const SizedBox(width: 8),
                    _FilterTab(
                      label: 'Pending',
                      isSelected: _filter == SyllabusFilter.pending,
                      onTap: () => setState(() => _filter = SyllabusFilter.pending),
                    ),
                    const SizedBox(width: 8),
                    _FilterTab(
                      label: 'Mastered',
                      isSelected: _filter == SyllabusFilter.completed,
                      onTap: () => setState(() => _filter = SyllabusFilter.completed),
                    ),
                  ],
                ),
                const SizedBox(height: 20),

                // Units Header
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      'Curriculum Units (${chapters.length})',
                      style: const TextStyle(fontSize: 17, fontWeight: FontWeight.w800, letterSpacing: -0.3),
                    ),
                    FilledButton.icon(
                      icon: const Icon(Icons.add, size: 16),
                      label: const Text('Add Unit'),
                      onPressed: () => showDialog(
                        context: context,
                        builder: (ctx) => EditChapterDialog(subjectId: subject.id),
                      ),
                      style: FilledButton.styleFrom(
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),

                // Chapters list
                if (chapters.isEmpty)
                  EmptyState(
                    icon: Icons.menu_book_rounded,
                    title: 'No units added',
                    subtitle: 'Divide this subject into units or chapters to create a syllabus study checklist.',
                    action: FilledButton(
                      onPressed: () => showDialog(
                        context: context,
                        builder: (ctx) => EditChapterDialog(subjectId: subject.id),
                      ),
                      child: const Text('Add First Unit'),
                    ),
                  )
                else
                  ...chapters.map((chWithTopics) {
                    final chapter = chWithTopics.chapter;
                    var topics = chWithTopics.topics;

                    if (_searchQuery.isNotEmpty) {
                      topics = topics
                          .where((t) => t.title.toLowerCase().contains(_searchQuery))
                          .toList();
                    }

                    if (_filter == SyllabusFilter.pending) {
                      topics = topics.where((t) => !t.isCompleted).toList();
                    } else if (_filter == SyllabusFilter.completed) {
                      topics = topics.where((t) => t.isCompleted).toList();
                    }

                    return Padding(
                      padding: const EdgeInsets.only(bottom: 14),
                      child: AdroitCard(
                        padding: const EdgeInsets.all(18),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.stretch,
                          children: [
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        chapter.title,
                                        style: const TextStyle(
                                          fontSize: 16,
                                          fontWeight: FontWeight.w800,
                                          letterSpacing: -0.3,
                                        ),
                                      ),
                                      const SizedBox(height: 2),
                                      Text(
                                        '${chWithTopics.completedCount} of ${chWithTopics.totalCount} completed • ${(chWithTopics.progress * 100).round()}%',
                                        style: TextStyle(
                                          fontSize: 11,
                                          fontWeight: FontWeight.w600,
                                          color: theme.colorScheme.onSurface.withOpacity(0.5),
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                                Row(
                                  children: [
                                    IconButton(
                                      icon: const Icon(Icons.add_circle_outline, size: 19),
                                      tooltip: 'Add Topic',
                                      onPressed: () => showDialog(
                                        context: context,
                                        builder: (ctx) => EditTopicDialog(chapterId: chapter.id),
                                      ),
                                    ),
                                    IconButton(
                                      icon: const Icon(Icons.delete_outline, size: 18, color: Colors.grey),
                                      tooltip: 'Delete Unit',
                                      onPressed: () async {
                                        final del = await showDialog<bool>(
                                          context: context,
                                          builder: (ctx) => AlertDialog(
                                            title: const Text('Delete Unit?'),
                                            content: Text('Delete "${chapter.title}" and all topics inside?'),
                                            actions: [
                                              TextButton(
                                                onPressed: () => Navigator.of(ctx).pop(false),
                                                child: const Text('Cancel'),
                                              ),
                                              FilledButton(
                                                onPressed: () => Navigator.of(ctx).pop(true),
                                                style: FilledButton.styleFrom(backgroundColor: Colors.red),
                                                child: const Text('Delete'),
                                              ),
                                            ],
                                          ),
                                        );
                                        if (del == true) {
                                          ref.read(databaseProvider).deleteChapter(chapter.id);
                                        }
                                      },
                                    ),
                                  ],
                                ),
                              ],
                            ),
                            const SizedBox(height: 10),
                            AdroitProgressBar(
                              progress: chWithTopics.progress,
                              color: subjectColor,
                              height: 5,
                            ),
                            const SizedBox(height: 14),

                            // Topics list
                            if (topics.isEmpty)
                              Padding(
                                padding: const EdgeInsets.symmetric(vertical: 6),
                                child: Text(
                                  _searchQuery.isNotEmpty || _filter != SyllabusFilter.all
                                      ? 'No topics match the filter.'
                                      : 'No topics yet. Tap + to add topics.',
                                  style: TextStyle(
                                    fontSize: 12,
                                    color: theme.colorScheme.onSurface.withOpacity(0.4),
                                  ),
                                ),
                              )
                            else
                              ...topics.map((t) {
                                return InkWell(
                                  onTap: () {
                                    ref.read(databaseProvider).setTopicCompletion(t.id, !t.isCompleted);
                                  },
                                  borderRadius: BorderRadius.circular(10),
                                  child: Padding(
                                    padding: const EdgeInsets.symmetric(vertical: 5),
                                    child: Row(
                                      children: [
                                        _AnimatedSquircleCheckbox(
                                          isChecked: t.isCompleted,
                                          color: subjectColor,
                                          onTap: () {
                                            ref.read(databaseProvider).setTopicCompletion(t.id, !t.isCompleted);
                                          },
                                        ),
                                        const SizedBox(width: 12),
                                        Expanded(
                                          child: Text(
                                            t.title,
                                            style: TextStyle(
                                              fontSize: 14,
                                              fontWeight: t.isCompleted ? FontWeight.w500 : FontWeight.w700,
                                              decoration: t.isCompleted ? TextDecoration.lineThrough : null,
                                              color: t.isCompleted
                                                  ? theme.colorScheme.onSurface.withOpacity(0.35)
                                                  : null,
                                            ),
                                          ),
                                        ),
                                        IconButton(
                                          icon: const Icon(Icons.close_rounded, size: 16, color: Colors.grey),
                                          onPressed: () {
                                            ref.read(databaseProvider).deleteTopic(t.id);
                                          },
                                        ),
                                      ],
                                    ),
                                  ),
                                );
                              }),
                          ],
                        ),
                      ),
                    );
                  }),
              ],
            ),
          ),
        );
      },
    );
  }
}

class _FilterTab extends StatelessWidget {
  final String label;
  final bool isSelected;
  final VoidCallback onTap;

  const _FilterTab({
    required this.label,
    required this.isSelected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(20),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 160),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 7),
        decoration: BoxDecoration(
          color: isSelected
              ? theme.colorScheme.primary
              : theme.colorScheme.surfaceVariant,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(
            color: isSelected ? theme.colorScheme.primary : theme.colorScheme.outline,
          ),
        ),
        child: Text(
          label,
          style: TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w700,
            color: isSelected ? Colors.white : theme.colorScheme.onSurface,
          ),
        ),
      ),
    );
  }
}

class _AnimatedSquircleCheckbox extends StatelessWidget {
  final bool isChecked;
  final Color color;
  final VoidCallback onTap;

  const _AnimatedSquircleCheckbox({
    required this.isChecked,
    required this.color,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(8),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 180),
        width: 22,
        height: 22,
        decoration: BoxDecoration(
          color: isChecked ? color : Colors.transparent,
          borderRadius: BorderRadius.circular(7),
          border: Border.all(
            color: isChecked ? color : Colors.grey.withOpacity(0.4),
            width: 1.5,
          ),
          boxShadow: isChecked
              ? [BoxShadow(color: color.withOpacity(0.35), blurRadius: 4, offset: const Offset(0, 1))]
              : null,
        ),
        child: isChecked
            ? const Icon(Icons.check_rounded, size: 16, color: Colors.white)
            : null,
      ),
    );
  }
}
