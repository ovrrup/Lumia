import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:drift/drift.dart' as drift;
import '../../core/constants/app_constants.dart';
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
        courseId: drift.Value(_selectedCourseId),
        subjectId: drift.Value(_selectedSubjectId),
        dueDateMillis: drift.Value(dueMillis),
        priority: _priority,
      ));
    }

    if (mounted) Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final courses = ref.watch(allCoursesProvider).value ?? [];
    final subjects = ref.watch(allSubjectsProvider).value ?? [];
    final isEditing = widget.initialTask != null;

    return Dialog(
      insetPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 24),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(24),
        side: BorderSide(color: theme.colorScheme.outline.withOpacity(0.4), width: 1),
      ),
      child: Container(
        constraints: const BoxConstraints(maxWidth: 460),
        padding: const EdgeInsets.all(24),
        child: SingleChildScrollView(
          child: Form(
            key: _formKey,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // Header
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          isEditing ? 'MODIFY TASK' : 'LOG ACADEMIC TASK',
                          style: TextStyle(
                            fontSize: 10.5,
                            fontWeight: FontWeight.w700,
                            letterSpacing: 1.2,
                            color: theme.colorScheme.primary,
                          ),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          isEditing ? 'Edit Assignment' : 'New Assignment / Task',
                          style: const TextStyle(
                            fontSize: 19,
                            fontWeight: FontWeight.w800,
                            letterSpacing: -0.4,
                          ),
                        ),
                      ],
                    ),
                    IconButton(
                      icon: const Icon(Icons.close_rounded, size: 20),
                      style: IconButton.styleFrom(
                        backgroundColor: theme.colorScheme.onSurface.withOpacity(0.05),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                      ),
                      onPressed: () => Navigator.of(context).pop(),
                    ),
                  ],
                ),
                const SizedBox(height: 18),

                // Task title
                TextFormField(
                  controller: _titleController,
                  decoration: InputDecoration(
                    labelText: 'Task Title *',
                    hintText: 'e.g. Lab Report 3, Problem Set 4',
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
                    prefixIcon: const Icon(Icons.assignment_rounded, size: 20),
                  ),
                  validator: (v) =>
                      v == null || v.trim().isEmpty ? 'Title is required' : null,
                ),
                const SizedBox(height: 14),

                // Description
                TextFormField(
                  controller: _descController,
                  decoration: InputDecoration(
                    labelText: 'Notes & Instructions',
                    hintText: 'Optional instructions, rubric, or formulas',
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
                    prefixIcon: const Icon(Icons.notes_rounded, size: 20),
                  ),
                  maxLines: 2,
                ),
                const SizedBox(height: 14),

                // Link Course
                DropdownButtonFormField<int?>(
                  value: _selectedCourseId,
                  decoration: InputDecoration(
                    labelText: 'Linked Course (Optional)',
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
                    prefixIcon: const Icon(Icons.book_rounded, size: 20),
                  ),
                  borderRadius: BorderRadius.circular(16),
                  items: [
                    const DropdownMenuItem<int?>(
                      value: null,
                      child: Text('No Course Linked'),
                    ),
                    ...courses.map((c) => DropdownMenuItem<int?>(
                          value: c.id,
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Container(
                                width: 10,
                                height: 10,
                                margin: const EdgeInsets.only(right: 8),
                                decoration: BoxDecoration(
                                  color: AppConstants.parseHexColor(c.colorHex),
                                  shape: BoxShape.circle,
                                ),
                              ),
                              Text(c.code.isNotEmpty ? '${c.name} (${c.code})' : c.name),
                            ],
                          ),
                        )),
                  ],
                  onChanged: (val) => setState(() => _selectedCourseId = val),
                ),
                const SizedBox(height: 14),

                // Link Subject
                DropdownButtonFormField<int?>(
                  value: _selectedSubjectId,
                  decoration: InputDecoration(
                    labelText: 'Linked Syllabus Subject (Optional)',
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
                    prefixIcon: const Icon(Icons.menu_book_rounded, size: 20),
                  ),
                  borderRadius: BorderRadius.circular(16),
                  items: [
                    const DropdownMenuItem<int?>(
                      value: null,
                      child: Text('No Subject Linked'),
                    ),
                    ...subjects.map((s) => DropdownMenuItem<int?>(
                          value: s.id,
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Container(
                                width: 10,
                                height: 10,
                                margin: const EdgeInsets.only(right: 8),
                                decoration: BoxDecoration(
                                  color: AppConstants.parseHexColor(s.colorHex),
                                  shape: BoxShape.circle,
                                ),
                              ),
                              Text(s.code.isNotEmpty ? '${s.name} (${s.code})' : s.name),
                            ],
                          ),
                        )),
                  ],
                  onChanged: (val) => setState(() => _selectedSubjectId = val),
                ),
                const SizedBox(height: 18),

                // Due Date & Time
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      'Target Deadline',
                      style: TextStyle(fontSize: 12.5, fontWeight: FontWeight.w700),
                    ),
                    if (_selectedDueDate != null)
                      InkWell(
                        onTap: () => setState(() {
                          _selectedDueDate = null;
                          _selectedDueTime = null;
                        }),
                        child: Text(
                          'Clear',
                          style: TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.w600,
                            color: theme.colorScheme.primary,
                          ),
                        ),
                      ),
                  ],
                ),
                const SizedBox(height: 8),
                Row(
                  children: [
                    Expanded(
                      child: InkWell(
                        onTap: () async {
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
                        borderRadius: BorderRadius.circular(12),
                        child: Container(
                          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                          decoration: BoxDecoration(
                            color: theme.colorScheme.onSurface.withOpacity(0.04),
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(
                              color: theme.colorScheme.onSurface.withOpacity(0.12),
                            ),
                          ),
                          child: Row(
                            children: [
                              Icon(
                                Icons.calendar_today_rounded,
                                size: 16,
                                color: theme.colorScheme.primary,
                              ),
                              const SizedBox(width: 8),
                              Expanded(
                                child: Text(
                                  _selectedDueDate == null
                                      ? 'Pick Date'
                                      : AdroitDateUtils.formatRelativeDate(
                                          _selectedDueDate!.millisecondsSinceEpoch,
                                        ),
                                  style: TextStyle(
                                    fontSize: 12,
                                    fontWeight: _selectedDueDate != null
                                        ? FontWeight.w700
                                        : FontWeight.w500,
                                  ),
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: InkWell(
                        onTap: () async {
                          final picked = await showTimePicker(
                            context: context,
                            initialTime: _selectedDueTime ?? const TimeOfDay(hour: 23, minute: 59),
                          );
                          if (picked != null) {
                            setState(() => _selectedDueTime = picked);
                          }
                        },
                        borderRadius: BorderRadius.circular(12),
                        child: Container(
                          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                          decoration: BoxDecoration(
                            color: theme.colorScheme.onSurface.withOpacity(0.04),
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(
                              color: theme.colorScheme.onSurface.withOpacity(0.12),
                            ),
                          ),
                          child: Row(
                            children: [
                              Icon(
                                Icons.access_time_rounded,
                                size: 16,
                                color: theme.colorScheme.primary,
                              ),
                              const SizedBox(width: 8),
                              Expanded(
                                child: Text(
                                  _selectedDueTime == null
                                      ? '11:59 PM'
                                      : AdroitDateUtils.formatTime12(context, _selectedDueTime!),
                                  style: TextStyle(
                                    fontSize: 12,
                                    fontWeight: _selectedDueTime != null
                                        ? FontWeight.w700
                                        : FontWeight.w500,
                                  ),
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 18),

                // Priority Selector
                const Text(
                  'Urgency / Priority',
                  style: TextStyle(fontSize: 12.5, fontWeight: FontWeight.w700),
                ),
                const SizedBox(height: 8),
                Row(
                  children: [
                    _PriorityChip(
                      label: 'Low',
                      color: Colors.grey,
                      isSelected: _priority == 0,
                      onTap: () => setState(() => _priority = 0),
                    ),
                    const SizedBox(width: 8),
                    _PriorityChip(
                      label: 'Medium',
                      color: const Color(0xFFF59E0B),
                      isSelected: _priority == 1,
                      onTap: () => setState(() => _priority = 1),
                    ),
                    const SizedBox(width: 8),
                    _PriorityChip(
                      label: 'Urgent',
                      color: const Color(0xFFEF4444),
                      isSelected: _priority == 2,
                      onTap: () => setState(() => _priority = 2),
                    ),
                  ],
                ),
                const SizedBox(height: 24),

                // Save button
                FilledButton(
                  style: FilledButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 14),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                  ),
                  onPressed: _save,
                  child: Text(
                    isEditing ? 'Update Task' : 'Create Task',
                    style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 14),
                  ),
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
        borderRadius: BorderRadius.circular(12),
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 180),
          padding: const EdgeInsets.symmetric(vertical: 10),
          decoration: BoxDecoration(
            color: isSelected ? color.withOpacity(0.18) : Colors.transparent,
            borderRadius: BorderRadius.circular(12),
            border: Border.all(
              color: isSelected ? color : Colors.grey.withOpacity(0.25),
              width: isSelected ? 1.6 : 1,
            ),
          ),
          alignment: Alignment.center,
          child: Text(
            label,
            style: TextStyle(
              fontSize: 12,
              fontWeight: isSelected ? FontWeight.w800 : FontWeight.w600,
              color: isSelected ? color : null,
            ),
          ),
        ),
      ),
    );
  }
}
