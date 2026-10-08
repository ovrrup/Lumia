import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:drift/drift.dart' as drift;
import '../../data/database/database.dart';
import '../../providers/database_provider.dart';

class EditChapterDialog extends ConsumerStatefulWidget {
  final int subjectId;
  final Chapter? initialChapter;

  const EditChapterDialog({
    super.key,
    required this.subjectId,
    this.initialChapter,
  });

  @override
  ConsumerState<EditChapterDialog> createState() => _EditChapterDialogState();
}

class _EditChapterDialogState extends ConsumerState<EditChapterDialog> {
  late TextEditingController _titleController;

  @override
  void initState() {
    super.initState();
    _titleController = TextEditingController(text: widget.initialChapter?.title ?? '');
  }

  @override
  void dispose() {
    _titleController.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    final title = _titleController.text.trim();
    if (title.isEmpty) return;

    final db = ref.read(databaseProvider);
    if (widget.initialChapter == null) {
      await db.insertChapter(ChaptersCompanion.insert(
        subjectId: widget.subjectId,
        title: title,
      ));
    } else {
      await db.updateChapterData(widget.initialChapter!.copyWith(title: title));
    }

    if (mounted) Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final isEditing = widget.initialChapter != null;

    return AlertDialog(
      title: Text(isEditing ? 'Edit Unit / Chapter' : 'Add Unit / Chapter'),
      content: TextField(
        controller: _titleController,
        autofocus: true,
        decoration: const InputDecoration(
          labelText: 'Unit Title',
          hintText: 'e.g. Unit 1: Memory Management',
        ),
        onSubmitted: (_) => _save(),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Cancel'),
        ),
        FilledButton(
          onPressed: _save,
          child: Text(isEditing ? 'Save' : 'Add'),
        ),
      ],
    );
  }
}

class EditTopicDialog extends ConsumerStatefulWidget {
  final int chapterId;
  final Topic? initialTopic;

  const EditTopicDialog({
    super.key,
    required this.chapterId,
    this.initialTopic,
  });

  @override
  ConsumerState<EditTopicDialog> createState() => _EditTopicDialogState();
}

class _EditTopicDialogState extends ConsumerState<EditTopicDialog> {
  late TextEditingController _titleController;

  @override
  void initState() {
    super.initState();
    _titleController = TextEditingController(text: widget.initialTopic?.title ?? '');
  }

  @override
  void dispose() {
    _titleController.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    final title = _titleController.text.trim();
    if (title.isEmpty) return;

    final db = ref.read(databaseProvider);
    if (widget.initialTopic == null) {
      await db.insertTopic(TopicsCompanion.insert(
        chapterId: widget.chapterId,
        title: title,
      ));
    } else {
      await db.updateTopicData(widget.initialTopic!.copyWith(title: title));
    }

    if (mounted) Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final isEditing = widget.initialTopic != null;

    return AlertDialog(
      title: Text(isEditing ? 'Edit Topic' : 'Add Topic'),
      content: TextField(
        controller: _titleController,
        autofocus: true,
        decoration: const InputDecoration(
          labelText: 'Topic Title',
          hintText: 'e.g. Virtual Memory & Page Tables',
        ),
        onSubmitted: (_) => _save(),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Cancel'),
        ),
        FilledButton(
          onPressed: _save,
          child: Text(isEditing ? 'Save' : 'Add'),
        ),
      ],
    );
  }
}
