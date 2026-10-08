import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/utils/date_utils.dart';
import '../../data/models/models.dart';
import '../../providers/database_provider.dart';

class RecordAttendanceDialog extends ConsumerStatefulWidget {
  final int courseId;
  final String courseName;
  final int? initialDateMillis;
  final String? initialStatus;

  const RecordAttendanceDialog({
    super.key,
    required this.courseId,
    required this.courseName,
    this.initialDateMillis,
    this.initialStatus,
  });

  @override
  ConsumerState<RecordAttendanceDialog> createState() => _RecordAttendanceDialogState();
}

class _RecordAttendanceDialogState extends ConsumerState<RecordAttendanceDialog> {
  late DateTime _selectedDate;
  late AttendanceStatus _selectedStatus;

  @override
  void initState() {
    super.initState();
    _selectedDate = widget.initialDateMillis != null
        ? AdroitDateUtils.fromMillis(widget.initialDateMillis!)
        : DateTime.now();

    final statusStr = widget.initialStatus?.toLowerCase() ?? 'present';
    _selectedStatus = AttendanceStatus.values.firstWhere(
      (s) => s.name == statusStr,
      orElse: () => AttendanceStatus.present,
    );
  }

  Future<void> _save() async {
    final db = ref.read(databaseProvider);
    final midnight = AdroitDateUtils.toMidnightMillis(_selectedDate);

    await db.recordAttendance(
      courseId: widget.courseId,
      dateMillis: midnight,
      status: _selectedStatus.name,
    );

    if (mounted) Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Dialog(
      insetPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 24),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
      backgroundColor: theme.colorScheme.surface,
      child: Container(
        constraints: const BoxConstraints(maxWidth: 420),
        padding: const EdgeInsets.all(24),
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
                    const Text(
                      'Log Class Attendance',
                      style: TextStyle(fontSize: 19, fontWeight: FontWeight.w800, letterSpacing: -0.4),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      widget.courseName,
                      style: TextStyle(
                        fontSize: 13,
                        color: theme.colorScheme.onSurface.withOpacity(0.6),
                        fontWeight: FontWeight.w500,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
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
            const SizedBox(height: 22),

            // Date picker box
            InkWell(
              onTap: () async {
                final picked = await showDatePicker(
                  context: context,
                  initialDate: _selectedDate,
                  firstDate: DateTime(2020),
                  lastDate: DateTime.now().add(const Duration(days: 30)),
                );
                if (picked != null) setState(() => _selectedDate = picked);
              },
              borderRadius: BorderRadius.circular(14),
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
                decoration: BoxDecoration(
                  color: theme.colorScheme.surfaceVariant,
                  borderRadius: BorderRadius.circular(14),
                  border: Border.all(color: theme.colorScheme.outline),
                ),
                child: Row(
                  children: [
                    Icon(Icons.calendar_today_rounded, size: 18, color: theme.colorScheme.primary),
                    const SizedBox(width: 12),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'SESSION DATE',
                          style: TextStyle(fontSize: 9, fontWeight: FontWeight.w700, color: Colors.grey, letterSpacing: 0.5),
                        ),
                        Text(
                          AdroitDateUtils.formatRelativeDate(
                            AdroitDateUtils.toMidnightMillis(_selectedDate),
                          ),
                          style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w700),
                        ),
                      ],
                    ),
                    const Spacer(),
                    const Icon(Icons.keyboard_arrow_down_rounded, color: Colors.grey),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 20),

            // Status selection grid
            const Text(
              'Select Status',
              style: TextStyle(fontSize: 12, fontWeight: FontWeight.w700, letterSpacing: 0.2),
            ),
            const SizedBox(height: 10),
            Row(
              children: AttendanceStatus.values.map((status) {
                final isSelected = _selectedStatus == status;
                return Expanded(
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 3),
                    child: InkWell(
                      onTap: () => setState(() => _selectedStatus = status),
                      borderRadius: BorderRadius.circular(14),
                      child: AnimatedContainer(
                        duration: const Duration(milliseconds: 180),
                        padding: const EdgeInsets.symmetric(vertical: 14),
                        decoration: BoxDecoration(
                          color: isSelected
                              ? status.color.withOpacity(0.18)
                              : theme.colorScheme.surfaceVariant,
                          borderRadius: BorderRadius.circular(14),
                          border: Border.all(
                            color: isSelected ? status.color : theme.colorScheme.outline,
                            width: isSelected ? 1.6 : 1,
                          ),
                          boxShadow: isSelected
                              ? [BoxShadow(color: status.color.withOpacity(0.25), blurRadius: 6)]
                              : null,
                        ),
                        child: Column(
                          children: [
                            Icon(status.icon, size: 22, color: status.color),
                            const SizedBox(height: 6),
                            Text(
                              status.label,
                              style: TextStyle(
                                fontSize: 11,
                                fontWeight: isSelected ? FontWeight.w800 : FontWeight.w600,
                                color: isSelected ? status.color : theme.colorScheme.onSurface,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                );
              }).toList(),
            ),
            const SizedBox(height: 26),

            FilledButton(
              onPressed: _save,
              style: FilledButton.styleFrom(
                padding: const EdgeInsets.symmetric(vertical: 16),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
              ),
              child: const Text('Save Attendance Record', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 14)),
            ),
          ],
        ),
      ),
    );
  }
}
