import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:drift/drift.dart' as drift;
import '../../core/utils/date_utils.dart';
import '../../data/database/database.dart';
import '../../providers/courses_provider.dart';
import '../../providers/database_provider.dart';
import '../../providers/syllabus_provider.dart';

class EditTaskDialog extends ConsumerStatefulWidget {
  final CourseTask? initialTask;
  final int? courseId;
  final int? subjectId;

  const EditTaskDialog({
    super.key,
    this.initialTask,
    this.courseId,
    this.subjectId,
  });

  @override
  ConsumerState<EditTaskDialog> createState() => _EditTaskDialogState();
}

class _EditTaskDialogState extends ConsumerState<EditTaskDialog> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _titleController;
  late TextEditingController _descController;

  int? _selectedCourseId;
  int? _selectedSubjectId;
  DateTime? _selectedDueDate;
  TimeOfDay? _selectedDueTime;
  int _priority = 1; // 0=Low, 1=Medium, 2=High

  @override
  void initState() {
    super.initState();
    final t = widget.initialTask;
    _titleController = TextEditingController(text: t?.title ?? '');
    _descController = TextEditingController(text: t?.description ?? '');

    _selectedCourseId = t?.courseId ?? widget.courseId;
    _selectedSubjectId = t?.subjectId ?? widget.subjectId;
    _priority = t?.priority ?? 1;

    if (t?.dueDateMillis != null) {
      final dt = DateTime.fromMillisecondsSinceEpoch(t!.dueDateMillis!);
      _selectedDueDate = dt;
      _selectedDueTime = TimeOfDay.fromDateTime(dt);
    }
  }

  @override
  void dispose() {
    _titleController.dispose();
    _descController.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    final db = ref.read(databaseProvider);

    int? dueMillis;
    if (_selectedDueDate != null) {
      final time = _selectedDueTime ?? const TimeOfDay(hour: 23, minute: 59);
      final combined = DateTime(
        _selectedDueDate!.year,
        _selectedDueDate!.month,
        _selectedDueDate!.day,
        time.hour,
        time.minute,
      );
      dueMillis = combined.millisecondsSinceEpoch;
    }

    if (widget.initialTask == null) {
      await db.insertTask(CourseTasksCompanion.insert(
        title: _titleController.text.trim(),
        description: drift.Value(_descController.text.trim()),
        courseId: drift.Value(_selectedCourseId),
        subjectId: drift.Value(_selectedSubjectId),
        dueDateMillis: drift.Value(dueMillis),
        priority: drift.Value(_priority),
        createdAtMillis: DateTime.now().millisecondsSinceEpoch,
      ));
    } else {
      await db.updateTaskData(widget.initialTask!.copyWith(
        title: _titleController.text.trim(),
        description: _descController.text.trim(),
        courseId: _selectedCourseId,
        subjectId: _selectedSubjectId,
        dueDateMillis: dueMillis,
        priority: _priority,
      ));
    }

    if (mounted) Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final courses = ref.watch(allCoursesProvider).value ?? [];
    final subjects = ref.watch(allSubjectsProvider).value ?? [];
    final isEditing = widget.initialTask != null;

    return Dialog(
      insetPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 24),
      child: Container(
        constraints: const BoxConstraints(maxWidth: 440),
        padding: const EdgeInsets.all(24),
        child: SingleChildScrollView(
          child: Form(
            key: _formKey,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      isEditing ? 'Edit Task' : 'New Task / Assignment',
                      style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w700),
                    ),
                    IconButton(
                      icon: const Icon(Icons.close, size: 20),
                      onPressed: () => Navigator.of(context).pop(),
                    ),
                  ],
                ),
                const SizedBox(height: 16),

                TextFormField(
                  controller: _titleController,
                  decoration: const InputDecoration(
                    labelText: 'Task Title *',
                    hintText: 'e.g. Complete Lab Report 3',
                  ),
                  validator: (v) =>
                      v == null || v.trim().isEmpty ? 'Title is required' : null,
                ),
                const SizedBox(height: 12),

                TextFormField(
                  controller: _descController,
                  decoration: const InputDecoration(
                    labelText: 'Notes / Description',
                    hintText: 'Optional notes, formulas, or instructions',
                  ),
                  maxLines: 2,
                ),
                const SizedBox(height: 14),

                // Link Course
                DropdownButtonFormField<int?>(
                  value: _selectedCourseId,
                  decoration: const InputDecoration(labelText: 'Course (Optional)'),
                  items: [
                    const DropdownMenuItem<int?>(
                      value: null,
                      child: Text('No Course'),
                    ),
                    ...courses.map((c) => DropdownMenuItem<int?>(
                          value: c.id,
                          child: Text('${c.name} (${c.code})'),
                        )),
                  ],
                  onChanged: (val) => setState(() => _selectedCourseId = val),
                ),
                const SizedBox(height: 12),

                // Link Subject
                DropdownButtonFormField<int?>(
                  value: _selectedSubjectId,
                  decoration: const InputDecoration(labelText: 'Subject (Optional)'),
                  items: [
                    const DropdownMenuItem<int?>(
                      value: null,
                      child: Text('No Subject'),
                    ),
                    ...subjects.map((s) => DropdownMenuItem<int?>(
                          value: s.id,
                          child: Text(s.name),
                        )),
                  ],
                  onChanged: (val) => setState(() => _selectedSubjectId = val),
                ),
                const SizedBox(height: 16),

                // Due Date & Time
                const Text(
                  'Due Date & Time',
                  style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600),
                ),
                const SizedBox(height: 8),
                Row(
                  children: [
                    Expanded(
                      child: OutlinedButton.icon(
                        icon: const Icon(Icons.calendar_today_rounded, size: 16),
                        label: Text(
                          _selectedDueDate == null
                              ? 'Pick Date'
                              : AdroitDateUtils.formatRelativeDate(
                                  _selectedDueDate!.millisecondsSinceEpoch,
                                ),
                          style: const TextStyle(fontSize: 12),
                        ),
                        onPressed: () async {
                          final picked = await showDatePicker(
                            context: context,
                            initialDate: _selectedDueDate ?? DateTime.now(),
                            firstDate: DateTime.now().subtract(const Duration(days: 30)),
                            lastDate: DateTime.now().add(const Duration(days: 365)),
                          );
                          if (picked != null) {
                            setState(() => _selectedDueDate = picked);
                          }
                        },
                      ),
                    ),
                    if (_selectedDueDate != null) ...[
                      const SizedBox(width: 8),
                      Expanded(
                        child: OutlinedButton.icon(
                          icon: const Icon(Icons.access_time_rounded, size: 16),
                          label: Text(
                            _selectedDueTime == null
                                ? '11:59 PM'
                                : AdroitDateUtils.formatTime12(context, _selectedDueTime!),
                            style: const TextStyle(fontSize: 12),
                          ),
                          onPressed: () async {
                            final picked = await showTimePicker(
                              context: context,
                              initialTime: _selectedDueTime ?? const TimeOfDay(hour: 23, minute: 59),
                            );
                            if (picked != null) {
                              setState(() => _selectedDueTime = picked);
                            }
                          },
                        ),
                      ),
                      IconButton(
                        icon: const Icon(Icons.clear, size: 18),
                        onPressed: () => setState(() {
                          _selectedDueDate = null;
                          _selectedDueTime = null;
                        }),
                      ),
                    ],
                  ],
                ),
                const SizedBox(height: 16),

                // Priority Selector
                const Text(
                  'Priority',
                  style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600),
                ),
                const SizedBox(height: 8),
                Row(
                  children: [
                    _PriorityChip(
                      label: 'Low',
                      color: Colors.blueGrey,
                      isSelected: _priority == 0,
                      onTap: () => setState(() => _priority = 0),
                    ),
                    const SizedBox(width: 8),
                    _PriorityChip(
                      label: 'Medium',
                      color: Colors.amber.shade700,
                      isSelected: _priority == 1,
                      onTap: () => setState(() => _priority = 1),
                    ),
                    const SizedBox(width: 8),
                    _PriorityChip(
                      label: 'High',
                      color: Colors.redAccent,
                      isSelected: _priority == 2,
                      onTap: () => setState(() => _priority = 2),
                    ),
                  ],
                ),
                const SizedBox(height: 24),

                FilledButton(
                  onPressed: _save,
                  child: Text(isEditing ? 'Save Changes' : 'Create Task'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _PriorityChip extends StatelessWidget {
  final String label;
  final Color color;
  final bool isSelected;
  final VoidCallback onTap;

  const _PriorityChip({
    required this.label,
    required this.color,
    required this.isSelected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(8),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 8),
          decoration: BoxDecoration(
            color: isSelected ? color.withOpacity(0.18) : Colors.transparent,
            borderRadius: BorderRadius.circular(8),
            border: Border.all(
              color: isSelected ? color : Colors.grey.withOpacity(0.3),
              width: isSelected ? 1.5 : 1,
            ),
          ),
          alignment: Alignment.center,
          child: Text(
            label,
            style: TextStyle(
              fontSize: 12,
              fontWeight: isSelected ? FontWeight.w700 : FontWeight.w500,
              color: isSelected ? color : null,
            ),
          ),
        ),
      ),
    );
  }
}
