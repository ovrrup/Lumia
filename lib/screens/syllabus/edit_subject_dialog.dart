import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:drift/drift.dart' as drift;
import '../../core/constants/app_constants.dart';
import '../../data/database/database.dart';
import '../../providers/courses_provider.dart';
import '../../providers/database_provider.dart';

class EditSubjectDialog extends ConsumerStatefulWidget {
  final Subject? initialSubject;
  final int? preselectedCourseId;

  const EditSubjectDialog({
    super.key,
    this.initialSubject,
    this.preselectedCourseId,
  });

  @override
  ConsumerState<EditSubjectDialog> createState() => _EditSubjectDialogState();
}

class _EditSubjectDialogState extends ConsumerState<EditSubjectDialog> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _nameController;
  late TextEditingController _codeController;
  late String _selectedColorHex;
  int? _selectedCourseId;

  @override
  void initState() {
    super.initState();
    final s = widget.initialSubject;
    _nameController = TextEditingController(text: s?.name ?? '');
    _codeController = TextEditingController(text: s?.code ?? '');
    _selectedColorHex = s?.colorHex ?? '#06B6D4';
    _selectedCourseId = s?.courseId ?? widget.preselectedCourseId;
  }

  @override
  void dispose() {
    _nameController.dispose();
    _codeController.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    final db = ref.read(databaseProvider);

    if (widget.initialSubject == null) {
      await db.insertSubject(SubjectsCompanion.insert(
        name: _nameController.text.trim(),
        code: drift.Value(_codeController.text.trim()),
        colorHex: drift.Value(_selectedColorHex),
        courseId: drift.Value(_selectedCourseId),
      ));
    } else {
      await db.updateSubjectData(widget.initialSubject!.copyWith(
        name: _nameController.text.trim(),
        code: _codeController.text.trim(),
        colorHex: _selectedColorHex,
        courseId: drift.Value(_selectedCourseId),
      ));
    }

    if (mounted) Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final courses = ref.watch(allCoursesProvider).value ?? [];
    final isEditing = widget.initialSubject != null;

    return Dialog(
      insetPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 24),
      child: Container(
        constraints: const BoxConstraints(maxWidth: 420),
        padding: const EdgeInsets.all(24),
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
                    isEditing ? 'Edit Subject' : 'New Subject',
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
                controller: _nameController,
                decoration: const InputDecoration(
                  labelText: 'Subject / Module Name *',
                  hintText: 'e.g. Operating Systems',
                ),
                validator: (v) =>
                    v == null || v.trim().isEmpty ? 'Subject name is required' : null,
              ),
              const SizedBox(height: 12),

              TextFormField(
                controller: _codeController,
                decoration: const InputDecoration(
                  labelText: 'Subject Code',
                  hintText: 'CS301',
                ),
              ),
              const SizedBox(height: 12),

              // Link to Course dropdown
              DropdownButtonFormField<int?>(
                value: _selectedCourseId,
                decoration: const InputDecoration(
                  labelText: 'Linked Course (Optional)',
                ),
                items: [
                  const DropdownMenuItem<int?>(
                    value: null,
                    child: Text('None (Independent Subject)'),
                  ),
                  ...courses.map((c) => DropdownMenuItem<int?>(
                        value: c.id,
                        child: Text('${c.name} (${c.code})'),
                      )),
                ],
                onChanged: (val) => setState(() => _selectedCourseId = val),
              ),
              const SizedBox(height: 16),

              // Color picker
              const Text(
                'Accent Color',
                style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600),
              ),
              const SizedBox(height: 8),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: AppConstants.presetColors.map((preset) {
                  final hex = preset['hex'] as String;
                  final color = preset['color'] as Color;
                  final isSelected = _selectedColorHex.toLowerCase() == hex.toLowerCase();

                  return InkWell(
                    onTap: () => setState(() => _selectedColorHex = hex),
                    borderRadius: BorderRadius.circular(16),
                    child: Container(
                      width: 28,
                      height: 28,
                      decoration: BoxDecoration(
                        color: color,
                        shape: BoxShape.circle,
                        border: isSelected
                            ? Border.all(color: Colors.white, width: 2)
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

              FilledButton(
                onPressed: _save,
                child: Text(isEditing ? 'Save Changes' : 'Create Subject'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
