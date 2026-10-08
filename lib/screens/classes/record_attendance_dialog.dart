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
    return Dialog(
      insetPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 24),
      child: Container(
        constraints: const BoxConstraints(maxWidth: 400),
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Log Attendance',
                        style: TextStyle(fontSize: 18, fontWeight: FontWeight.w700),
                      ),
                      Text(
                        widget.courseName,
                        style: TextStyle(
                          fontSize: 13,
                          color: Theme.of(context).colorScheme.onSurface.withOpacity(0.6),
                        ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ],
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.close_rounded, size: 20),
                  onPressed: () => Navigator.of(context).pop(),
                ),
              ],
            ),
            const SizedBox(height: 20),

            // Date picker button
            OutlinedButton.icon(
              icon: const Icon(Icons.calendar_today_rounded, size: 16),
              label: Text(
                AdroitDateUtils.formatRelativeDate(
                  AdroitDateUtils.toMidnightMillis(_selectedDate),
                ),
              ),
              onPressed: () async {
                final picked = await showDatePicker(
                  context: context,
                  initialDate: _selectedDate,
                  firstDate: DateTime(2020),
                  lastDate: DateTime.now().add(const Duration(days: 30)),
                );
                if (picked != null) setState(() => _selectedDate = picked);
              },
            ),
            const SizedBox(height: 16),

            // Status selection
            const Text(
              'Status',
              style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
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
                      borderRadius: BorderRadius.circular(10),
                      child: Container(
                        padding: const EdgeInsets.symmetric(vertical: 10),
                        decoration: BoxDecoration(
                          color: isSelected
                              ? status.color.withOpacity(0.2)
                              : Colors.transparent,
                          borderRadius: BorderRadius.circular(10),
                          border: Border.all(
                            color: isSelected ? status.color : Colors.grey.withOpacity(0.2),
                            width: isSelected ? 1.5 : 1,
                          ),
                        ),
                        child: Column(
                          children: [
                            Icon(status.icon, size: 20, color: status.color),
                            const SizedBox(height: 4),
                            Text(
                              status.label,
                              style: TextStyle(
                                fontSize: 11,
                                fontWeight: isSelected ? FontWeight.w700 : FontWeight.w500,
                                color: isSelected ? status.color : null,
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
            const SizedBox(height: 24),

            FilledButton(
              onPressed: _save,
              child: const Text('Save Record', style: TextStyle(fontWeight: FontWeight.w700)),
            ),
          ],
        ),
      ),
    );
  }
}
