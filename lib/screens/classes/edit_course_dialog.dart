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
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      isEditing ? 'Edit Course' : 'New Course',
                      style: const TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.w700,
                        letterSpacing: -0.5,
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.close_rounded, size: 20),
                      onPressed: () => Navigator.of(context).pop(),
                    ),
                  ],
                ),
                const SizedBox(height: 16),

                // Name & Code
                TextFormField(
                  controller: _nameController,
                  decoration: const InputDecoration(
                    labelText: 'Course Name *',
                    hintText: 'e.g. Data Structures & Algorithms',
                  ),
                  validator: (v) =>
                      v == null || v.trim().isEmpty ? 'Course name is required' : null,
                ),
                const SizedBox(height: 12),

                Row(
                  children: [
                    Expanded(
                      flex: 2,
                      child: TextFormField(
                        controller: _codeController,
                        decoration: const InputDecoration(
                          labelText: 'Course Code',
                          hintText: 'CS201',
                        ),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      flex: 2,
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
                const SizedBox(height: 16),

                // Schedule Days
                const Text(
                  'Class Schedule Days',
                  style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
                ),
                const SizedBox(height: 8),
                Wrap(
                  spacing: 6,
                  children: AppConstants.weekDays.map((day) {
                    final isSelected = _selectedDays.contains(day);
                    return FilterChip(
                      label: Text(day),
                      selected: isSelected,
                      showCheckmark: false,
                      onSelected: (val) {
                        setState(() {
                          if (val) {
                            _selectedDays.add(day);
                          } else {
                            if (_selectedDays.length > 1) {
                              _selectedDays.remove(day);
                            }
                          }
                        });
                      },
                    );
                  }).toList(),
                ),
                const SizedBox(height: 16),

                // Timings
                Row(
                  children: [
                    Expanded(
                      child: OutlinedButton.icon(
                        icon: const Icon(Icons.access_time_rounded, size: 16),
                        label: Text(
                          'Start: ${AdroitDateUtils.formatTime12(context, _startTime)}',
                          style: const TextStyle(fontSize: 12),
                        ),
                        onPressed: () async {
                          final picked = await showTimePicker(
                            context: context,
                            initialTime: _startTime,
                          );
                          if (picked != null) setState(() => _startTime = picked);
                        },
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: OutlinedButton.icon(
                        icon: const Icon(Icons.access_time_rounded, size: 16),
                        label: Text(
                          'End: ${AdroitDateUtils.formatTime12(context, _endTime)}',
                          style: const TextStyle(fontSize: 12),
                        ),
                        onPressed: () async {
                          final picked = await showTimePicker(
                            context: context,
                            initialTime: _endTime,
                          );
                          if (picked != null) setState(() => _endTime = picked);
                        },
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),

                // Target Attendance
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      'Target Attendance',
                      style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
                    ),
                    Text(
                      '$_targetAttendance%',
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w700,
                        color: theme.colorScheme.primary,
                      ),
                    ),
                  ],
                ),
                Slider(
                  value: _targetAttendance.toDouble(),
                  min: 50,
                  max: 100,
                  divisions: 10,
                  onChanged: (val) =>
                      setState(() => _targetAttendance = val.round()),
                ),
                const SizedBox(height: 12),

                // Color accent picker
                const Text(
                  'Accent Color',
                  style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
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
                      borderRadius: BorderRadius.circular(20),
                      child: Container(
                        width: 32,
                        height: 32,
                        decoration: BoxDecoration(
                          color: color,
                          shape: BoxShape.circle,
                          border: isSelected
                              ? Border.all(color: Colors.white, width: 2.5)
                              : null,
                          boxShadow: isSelected
                              ? [BoxShadow(color: color.withOpacity(0.5), blurRadius: 6)]
                              : null,
                        ),
                        child: isSelected
                            ? const Icon(Icons.check, size: 18, color: Colors.white)
                            : null,
                      ),
                    );
                  }).toList(),
                ),
                const SizedBox(height: 24),

                // Submit button
                FilledButton(
                  onPressed: _saveCourse,
                  style: FilledButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 14),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                  ),
                  child: Text(
                    isEditing ? 'Save Changes' : 'Create Course',
                    style: const TextStyle(fontWeight: FontWeight.w700),
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
