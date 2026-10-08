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
    final theme = Theme.of(context);
    final courses = ref.watch(allCoursesProvider).value ?? [];
    final isEditing = widget.initialSubject != null;

    return Dialog(
      insetPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 24),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(24),
        side: BorderSide(color: theme.colorScheme.outline.withOpacity(0.4), width: 1),
      ),
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
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          isEditing ? 'MODIFY CURRICULUM' : 'NEW CURRICULUM',
                          style: TextStyle(
                            fontSize: 10.5,
                            fontWeight: FontWeight.w700,
                            letterSpacing: 1.2,
                            color: theme.colorScheme.primary,
                          ),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          isEditing ? 'Edit Subject' : 'Add Subject / Module',
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

                TextFormField(
                  controller: _nameController,
                  decoration: InputDecoration(
                    labelText: 'Subject Name *',
                    hintText: 'e.g. Operating Systems, Thermodynamics',
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
                    prefixIcon: const Icon(Icons.menu_book_rounded, size: 20),
                  ),
                  validator: (v) =>
                      v == null || v.trim().isEmpty ? 'Subject name is required' : null,
                ),
                const SizedBox(height: 14),

                TextFormField(
                  controller: _codeController,
                  decoration: InputDecoration(
                    labelText: 'Subject Code (Optional)',
                    hintText: 'CS301, ME204',
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
                    prefixIcon: const Icon(Icons.tag_rounded, size: 20),
                  ),
                ),
                const SizedBox(height: 14),

                // Link to Course dropdown
                DropdownButtonFormField<int?>(
                  value: _selectedCourseId,
                  decoration: InputDecoration(
                    labelText: 'Linked Timetable Course (Optional)',
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
                    prefixIcon: const Icon(Icons.link_rounded, size: 20),
                  ),
                  borderRadius: BorderRadius.circular(16),
                  items: [
                    const DropdownMenuItem<int?>(
                      value: null,
                      child: Text('Independent Subject'),
                    ),
                    ...courses.map((c) => DropdownMenuItem<int?>(
                          value: c.id,
                          child: Text('${c.name} (${c.code})'),
                        )),
                  ],
                  onChanged: (val) => setState(() => _selectedCourseId = val),
                ),
                const SizedBox(height: 18),

                // Color picker
                const Text(
                  'Subject Accent Palette',
                  style: TextStyle(fontSize: 12.5, fontWeight: FontWeight.w700),
                ),
                const SizedBox(height: 10),
                Wrap(
                  spacing: 10,
                  runSpacing: 10,
                  children: AppConstants.presetColors.map((preset) {
                    final hex = preset['hex'] as String;
                    final color = preset['color'] as Color;
                    final isSelected = _selectedColorHex.toLowerCase() == hex.toLowerCase();

                    return InkWell(
                      onTap: () => setState(() => _selectedColorHex = hex),
                      borderRadius: BorderRadius.circular(18),
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
                            ? const Icon(Icons.check_rounded, size: 18, color: Colors.white)
                            : null,
                      ),
                    );
                  }).toList(),
                ),
                const SizedBox(height: 24),

                FilledButton(
                  style: FilledButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 14),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                  ),
                  onPressed: _save,
                  child: Text(
                    isEditing ? 'Save Changes' : 'Create Subject',
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
