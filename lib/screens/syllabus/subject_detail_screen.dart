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
        content: Text('Are you sure you want to delete "${subject.name}"? All units and checklist topics will be permanently removed.'),
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
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // Top Progress Card
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
                                '${subjectProgress.percentage}%',
                                style: TextStyle(
                                  fontSize: 32,
                                  fontWeight: FontWeight.w800,
                                  letterSpacing: -1,
                                  color: subjectColor,
                                ),
                              ),
                              Text(
                                'Syllabus Completion',
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
                              color: subjectColor.withOpacity(0.12),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Text(
                              '${subjectProgress.completedTopics} of ${subjectProgress.totalTopics} Topics',
                              style: TextStyle(
                                fontSize: 12,
                                fontWeight: FontWeight.w700,
                                color: subjectColor,
                              ),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 14),
                      AdroitProgressBar(
                        progress: subjectProgress.progress,
                        color: subjectColor,
                        height: 8,
                      ),
                      if (subjectProgress.linkedCourse != null) ...[
                        const SizedBox(height: 12),
                        Row(
                          children: [
                            const Icon(Icons.link_rounded, size: 14, color: Colors.grey),
                            const SizedBox(width: 4),
                            Text(
                              'Linked Course: ${subjectProgress.linkedCourse!.name}',
                              style: TextStyle(
                                fontSize: 12,
                                color: theme.colorScheme.onSurface.withOpacity(0.7),
                              ),
                            ),
                          ],
                        ),
                      ],
                    ],
                  ),
                ),
                const SizedBox(height: 16),

                // Search & Filter controls
                Row(
                  children: [
                    Expanded(
                      child: TextField(
                        decoration: InputDecoration(
                          hintText: 'Search topics...',
                          prefixIcon: const Icon(Icons.search, size: 18),
                          isDense: true,
                          contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                          border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
                        ),
                        onChanged: (val) => setState(() => _searchQuery = val.trim().toLowerCase()),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),

                // Filter chips
                Row(
                  children: [
                    FilterChip(
                      label: const Text('All Topics'),
                      selected: _filter == SyllabusFilter.all,
                      onSelected: (_) => setState(() => _filter = SyllabusFilter.all),
                    ),
                    const SizedBox(width: 8),
                    FilterChip(
                      label: const Text('Pending Only'),
                      selected: _filter == SyllabusFilter.pending,
                      onSelected: (_) => setState(() => _filter = SyllabusFilter.pending),
                    ),
                    const SizedBox(width: 8),
                    FilterChip(
                      label: const Text('Completed'),
                      selected: _filter == SyllabusFilter.completed,
                      onSelected: (_) => setState(() => _filter = SyllabusFilter.completed),
                    ),
                  ],
                ),
                const SizedBox(height: 16),

                // Units & Chapters Header
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      'Curriculum Units (${chapters.length})',
                      style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w700),
                    ),
                    FilledButton.tonalIcon(
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
                const SizedBox(height: 10),

                // Chapters list
                if (chapters.isEmpty)
                  EmptyState(
                    icon: Icons.menu_book_rounded,
                    title: 'No units added yet',
                    subtitle: 'Create units or chapters to break down your syllabus into actionable topics.',
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

                    // Apply search query
                    if (_searchQuery.isNotEmpty) {
                      topics = topics
                          .where((t) => t.title.toLowerCase().contains(_searchQuery))
                          .toList();
                    }

                    // Apply filter
                    if (_filter == SyllabusFilter.pending) {
                      topics = topics.where((t) => !t.isCompleted).toList();
                    } else if (_filter == SyllabusFilter.completed) {
                      topics = topics.where((t) => t.isCompleted).toList();
                    }

                    return Padding(
                      padding: const EdgeInsets.only(bottom: 12),
                      child: AdroitCard(
                        padding: const EdgeInsets.all(14),
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
                                          fontSize: 15,
                                          fontWeight: FontWeight.w700,
                                        ),
                                      ),
                                      const SizedBox(height: 2),
                                      Text(
                                        '${chWithTopics.completedCount}/${chWithTopics.totalCount} completed (${(chWithTopics.progress * 100).round()}%)',
                                        style: TextStyle(
                                          fontSize: 11,
                                          color: theme.colorScheme.onSurface.withOpacity(0.5),
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                                Row(
                                  children: [
                                    IconButton(
                                      icon: const Icon(Icons.add_circle_outline, size: 18),
                                      tooltip: 'Add Topic to Unit',
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
                                            content: Text('Delete "${chapter.title}" and its topics?'),
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
                            const SizedBox(height: 8),
                            AdroitProgressBar(
                              progress: chWithTopics.progress,
                              color: subjectColor,
                              height: 5,
                            ),
                            const SizedBox(height: 10),

                            // Topics Checklist
                            if (topics.isEmpty)
                              Padding(
                                padding: const EdgeInsets.symmetric(vertical: 8),
                                child: Text(
                                  _searchQuery.isNotEmpty || _filter != SyllabusFilter.all
                                      ? 'No topics match the filter.'
                                      : 'No topics in this unit yet. Tap + to add topics.',
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
                                  borderRadius: BorderRadius.circular(8),
                                  child: Padding(
                                    padding: const EdgeInsets.symmetric(vertical: 4),
                                    child: Row(
                                      children: [
                                        Checkbox(
                                          value: t.isCompleted,
                                          activeColor: subjectColor,
                                          onChanged: (val) {
                                            ref.read(databaseProvider).setTopicCompletion(t.id, val ?? false);
                                          },
                                        ),
                                        Expanded(
                                          child: Text(
                                            t.title,
                                            style: TextStyle(
                                              fontSize: 13,
                                              fontWeight: t.isCompleted ? FontWeight.w400 : FontWeight.w600,
                                              decoration: t.isCompleted ? TextDecoration.lineThrough : null,
                                              color: t.isCompleted
                                                  ? theme.colorScheme.onSurface.withOpacity(0.4)
                                                  : null,
                                            ),
                                          ),
                                        ),
                                        IconButton(
                                          icon: const Icon(Icons.close, size: 16, color: Colors.grey),
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
                const SizedBox(height: 32),
              ],
            ),
          ),
        );
      },
    );
  }
}
