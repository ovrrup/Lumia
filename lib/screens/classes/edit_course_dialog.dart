import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:drift/drift.dart' as drift;
import '../../core/constants/app_constants.dart';
import '../../core/utils/date_utils.dart';
import '../../data/database/database.dart';
import '../../providers/database_provider.dart';

class EditCourseDialog extends ConsumerStatefulWidget {
  final Course? initialCourse;

  const EditCourseDialog({super.key, this.initialCourse});

  @override
  ConsumerState<EditCourseDialog> createState() => _EditCourseDialogState();
}

class _EditCourseDialogState extends ConsumerState<EditCourseDialog> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _nameController;
  late TextEditingController _codeController;
  late TextEditingController _instructorController;
  late TextEditingController _roomController;
  late TextEditingController _descriptionController;

  late String _selectedColorHex;
  late Set<String> _selectedDays;
  late TimeOfDay _startTime;
  late TimeOfDay _endTime;
  late int _targetAttendance;

  @override
  void initState() {
    super.initState();
    final c = widget.initialCourse;
    _nameController = TextEditingController(text: c?.name ?? '');
    _codeController = TextEditingController(text: c?.code ?? '');
    _instructorController = TextEditingController(text: c?.instructor ?? '');
    _roomController = TextEditingController(text: c?.room ?? '');
    _descriptionController = TextEditingController(text: c?.description ?? '');

    _selectedColorHex = c?.colorHex ?? '#4F46E5';
    _selectedDays = (c?.scheduleDays.isNotEmpty ?? false)
        ? c!.scheduleDays.split(',').map((d) => d.trim()).toSet()
        : {'Mon', 'Wed', 'Fri'};

    _startTime = c != null
        ? AdroitDateUtils.parseTime(c.startTime)
        : const TimeOfDay(hour: 9, minute: 0);
    _endTime = c != null
        ? AdroitDateUtils.parseTime(c.endTime)
        : const TimeOfDay(hour: 10, minute: 0);

    _targetAttendance = c?.targetAttendance ?? 75;
  }

  @override
  void dispose() {
    _nameController.dispose();
    _codeController.dispose();
    _instructorController.dispose();
    _roomController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  Future<void> _saveCourse() async {
    if (!_formKey.currentState!.validate()) return;

    final db = ref.read(databaseProvider);
    final daysStr = _selectedDays.toList().join(',');
    final startStr = AdroitDateUtils.formatTime24(_startTime);
    final endStr = AdroitDateUtils.formatTime24(_endTime);

    if (widget.initialCourse == null) {
      await db.insertCourse(CoursesCompanion.insert(
        name: _nameController.text.trim(),
        code: drift.Value(_codeController.text.trim()),
        instructor: drift.Value(_instructorController.text.trim()),
        room: drift.Value(_roomController.text.trim()),
        colorHex: drift.Value(_selectedColorHex),
        scheduleDays: drift.Value(daysStr),
        startTime: drift.Value(startStr),
        endTime: drift.Value(endStr),
        targetAttendance: drift.Value(_targetAttendance),
        description: drift.Value(_descriptionController.text.trim()),
      ));
    } else {
      await db.updateCourseData(widget.initialCourse!.copyWith(
        name: _nameController.text.trim(),
        code: _codeController.text.trim(),
        instructor: _instructorController.text.trim(),
        room: _roomController.text.trim(),
        colorHex: _selectedColorHex,
        scheduleDays: daysStr,
        startTime: startStr,
        endTime: endStr,
        targetAttendance: _targetAttendance,
        description: _descriptionController.text.trim(),
      ));
    }

    if (mounted) Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isEditing = widget.initialCourse != null;

    return Dialog(
      insetPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 24),
      backgroundColor: theme.colorScheme.surface,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
      child: Container(
        constraints: const BoxConstraints(maxWidth: 480),
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
                          isEditing ? 'Edit Course' : 'Create Course',
                          style: const TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.w800,
                            letterSpacing: -0.5,
                          ),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          'Configure lecture details & schedule',
                          style: TextStyle(
                            fontSize: 12,
                            color: theme.colorScheme.onSurface.withOpacity(0.5),
                          ),
                        ),
                      ],
                    ),
                    IconButton(
                      icon: const Icon(Icons.close_rounded, size: 20),
                      onPressed: () => Navigator.of(context).pop(),
                      style: IconButton.styleFrom(
                        backgroundColor: theme.colorScheme.surfaceVariant,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 20),

                // Name input
                TextFormField(
                  controller: _nameController,
                  decoration: const InputDecoration(
                    labelText: 'Course Title *',
                    hintText: 'e.g. Operating Systems',
                  ),
                  validator: (v) =>
                      v == null || v.trim().isEmpty ? 'Course title is required' : null,
                ),
                const SizedBox(height: 12),

                // Code and Room
                Row(
                  children: [
                    Expanded(
                      flex: 3,
                      child: TextFormField(
                        controller: _codeController,
                        decoration: const InputDecoration(
                          labelText: 'Code',
                          hintText: 'CS301',
                        ),
                      ),
                    ),
                    const SizedBox(width: 10),
                    Expanded(
                      flex: 4,
                      child: TextFormField(
                        controller: _roomController,
                        decoration: const InputDecoration(
                          labelText: 'Room / Hall',
                          hintText: 'Hall B-12',
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),

                TextFormField(
                  controller: _instructorController,
                  decoration: const InputDecoration(
                    labelText: 'Instructor / Professor',
                    hintText: 'Prof. Alan Turing',
                  ),
                ),
                const SizedBox(height: 18),

                // Schedule Days
                const Text(
                  'Lecture Days',
                  style: TextStyle(fontSize: 12, fontWeight: FontWeight.w700, letterSpacing: 0.2),
                ),
                const SizedBox(height: 8),
                Wrap(
                  spacing: 6,
                  runSpacing: 6,
                  children: AppConstants.weekDays.map((day) {
                    final isSelected = _selectedDays.contains(day);
                    return InkWell(
                      onTap: () {
                        setState(() {
                          if (isSelected) {
                            if (_selectedDays.length > 1) _selectedDays.remove(day);
                          } else {
                            _selectedDays.add(day);
                          }
                        });
                      },
                      borderRadius: BorderRadius.circular(10),
                      child: AnimatedContainer(
                        duration: const Duration(milliseconds: 160),
                        padding: const EdgeInsets.symmetric(horizontal: 11, vertical: 7),
                        decoration: BoxDecoration(
                          color: isSelected
                              ? theme.colorScheme.primary
                              : theme.colorScheme.surfaceVariant,
                          borderRadius: BorderRadius.circular(10),
                          border: Border.all(
                            color: isSelected ? theme.colorScheme.primary : theme.colorScheme.outline,
                            width: 1,
                          ),
                        ),
                        child: Text(
                          day,
                          style: TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.w700,
                            color: isSelected ? Colors.white : theme.colorScheme.onSurface,
                          ),
                        ),
                      ),
                    );
                  }).toList(),
                ),
                const SizedBox(height: 18),

                // Timings
                const Text(
                  'Timings',
                  style: TextStyle(fontSize: 12, fontWeight: FontWeight.w700, letterSpacing: 0.2),
                ),
                const SizedBox(height: 8),
                Row(
                  children: [
                    Expanded(
                      child: InkWell(
                        onTap: () async {
                          final picked = await showTimePicker(
                            context: context,
                            initialTime: _startTime,
                          );
                          if (picked != null) setState(() => _startTime = picked);
                        },
                        borderRadius: BorderRadius.circular(12),
                        child: Container(
                          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                          decoration: BoxDecoration(
                            color: theme.colorScheme.surfaceVariant,
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(color: theme.colorScheme.outline),
                          ),
                          child: Row(
                            children: [
                              const Icon(Icons.access_time_rounded, size: 16, color: Colors.grey),
                              const SizedBox(width: 8),
                              Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  const Text('START', style: TextStyle(fontSize: 9, fontWeight: FontWeight.w700, color: Colors.grey)),
                                  Text(AdroitDateUtils.formatTime12(context, _startTime), style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w700)),
                                ],
                              ),
                            ],
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(width: 10),
                    Expanded(
                      child: InkWell(
                        onTap: () async {
                          final picked = await showTimePicker(
                            context: context,
                            initialTime: _endTime,
                          );
                          if (picked != null) setState(() => _endTime = picked);
                        },
                        borderRadius: BorderRadius.circular(12),
                        child: Container(
                          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                          decoration: BoxDecoration(
                            color: theme.colorScheme.surfaceVariant,
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(color: theme.colorScheme.outline),
                          ),
                          child: Row(
                            children: [
                              const Icon(Icons.access_time_rounded, size: 16, color: Colors.grey),
                              const SizedBox(width: 8),
                              Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  const Text('END', style: TextStyle(fontSize: 9, fontWeight: FontWeight.w700, color: Colors.grey)),
                                  Text(AdroitDateUtils.formatTime12(context, _endTime), style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w700)),
                                ],
                              ),
                            ],
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 18),

                // Target Attendance
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      'Attendance Target',
                      style: TextStyle(fontSize: 12, fontWeight: FontWeight.w700, letterSpacing: 0.2),
                    ),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                      decoration: BoxDecoration(
                        color: theme.colorScheme.primary.withOpacity(0.12),
                        borderRadius: BorderRadius.circular(6),
                      ),
                      child: Text(
                        '$_targetAttendance%',
                        style: TextStyle(
                          fontSize: 13,
                          fontWeight: FontWeight.w800,
                          color: theme.colorScheme.primary,
                        ),
                      ),
                    ),
                  ],
                ),
                Slider(
                  value: _targetAttendance.toDouble(),
                  min: 50,
                  max: 100,
                  divisions: 10,
                  onChanged: (val) => setState(() => _targetAttendance = val.round()),
                ),
                const SizedBox(height: 14),

                // Color Palette
                const Text(
                  'Accent Tag',
                  style: TextStyle(fontSize: 12, fontWeight: FontWeight.w700, letterSpacing: 0.2),
                ),
                const SizedBox(height: 8),
                Wrap(
                  spacing: 10,
                  runSpacing: 10,
                  children: AppConstants.presetColors.map((preset) {
                    final hex = preset['hex'] as String;
                    final color = preset['color'] as Color;
                    final isSelected = _selectedColorHex.toLowerCase() == hex.toLowerCase();

                    return InkWell(
                      onTap: () => setState(() => _selectedColorHex = hex),
                      borderRadius: BorderRadius.circular(16),
                      child: Container(
                        width: 30,
                        height: 30,
                        decoration: BoxDecoration(
                          color: color,
                          shape: BoxShape.circle,
                          border: isSelected
                              ? Border.all(color: Colors.white, width: 2.2)
                              : null,
                          boxShadow: isSelected
                              ? [BoxShadow(color: color.withOpacity(0.55), blurRadius: 6)]
                              : null,
                        ),
                        child: isSelected
                            ? const Icon(Icons.check, size: 16, color: Colors.white)
                            : null,
                      ),
                    );
                  }).toList(),
                ),
                const SizedBox(height: 24),

                // Action Button
                FilledButton(
                  onPressed: _saveCourse,
                  style: FilledButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 16),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                  ),
                  child: Text(
                    isEditing ? 'Save Changes' : 'Enroll Course',
                    style: const TextStyle(fontSize: 15, fontWeight: FontWeight.w800),
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
