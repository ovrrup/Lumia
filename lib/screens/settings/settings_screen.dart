import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/constants/app_constants.dart';
import '../../core/theme/app_theme.dart';
import '../../providers/database_provider.dart';
import '../../providers/theme_provider.dart';
import '../../widgets/adroit_card.dart';

class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  Future<void> _exportBackup(BuildContext context, WidgetRef ref) async {
    final backupService = ref.read(backupServiceProvider);
    final json = await backupService.exportBackupJson();

    if (!context.mounted) return;

    await showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Backup JSON Exported'),
        content: SizedBox(
          width: double.maxFinite,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const Text(
                'Your complete Adroit classes, syllabus, attendance, and tasks database has been exported to JSON:',
                style: TextStyle(fontSize: 13),
              ),
              const SizedBox(height: 12),
              Container(
                height: 160,
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: Colors.black.withOpacity(0.05),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: SingleChildScrollView(
                  child: SelectableText(
                    json,
                    style: const TextStyle(fontFamily: 'monospace', fontSize: 11),
                  ),
                ),
              ),
            ],
          ),
        ),
        actions: [
          TextButton.icon(
            icon: const Icon(Icons.copy_rounded, size: 16),
            label: const Text('Copy to Clipboard'),
            onPressed: () {
              Clipboard.setData(ClipboardData(text: json));
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Backup JSON copied to clipboard!')),
              );
              Navigator.of(ctx).pop();
            },
          ),
          FilledButton(
            onPressed: () => Navigator.of(ctx).pop(),
            child: const Text('Done'),
          ),
        ],
      ),
    );
  }

  Future<void> _importBackup(BuildContext context, WidgetRef ref) async {
    final textController = TextEditingController();

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Import Backup'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Text(
              'Paste your Adroit or Lumia backup JSON below. Existing data will be replaced.',
              style: TextStyle(fontSize: 13),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: textController,
              maxLines: 6,
              style: const TextStyle(fontFamily: 'monospace', fontSize: 11),
              decoration: const InputDecoration(
                hintText: '{\n  "app": "Adroit",\n  "courses": [...]\n}',
                border: OutlineInputBorder(),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(ctx).pop(true),
            child: const Text('Import & Restore'),
          ),
        ],
      ),
    );

    if (confirmed == true && textController.text.trim().isNotEmpty) {
      final backupService = ref.read(backupServiceProvider);
      final success = await backupService.importBackupJson(textController.text.trim());

      if (!context.mounted) return;
      if (success) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Database restored successfully!')),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Failed to parse or restore backup JSON.')),
        );
      }
    }
  }

  Future<void> _resetData(BuildContext context, WidgetRef ref) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Clear All Data?'),
        content: const Text(
          'This will permanently delete all courses, syllabus topics, attendance records, and tasks.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(ctx).pop(true),
            style: FilledButton.styleFrom(backgroundColor: Colors.red),
            child: const Text('Erase All'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      await ref.read(databaseProvider).clearAllData();
      if (!context.mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('All data cleared.')),
      );
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final themeState = ref.watch(themeProvider);
    final themeNotifier = ref.read(themeProvider.notifier);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Settings'),
      ),
      body: ListView(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        children: [
          // Theme & Appearance Section
          const _SectionHeader(title: 'Appearance & Theme'),
          const SizedBox(height: 8),
          AdroitCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Theme Mode',
                  style: TextStyle(fontSize: 14, fontWeight: FontWeight.w700),
                ),
                const SizedBox(height: 10),
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  child: Row(
                    children: [
                      _ThemeModeChip(
                        label: 'System',
                        icon: Icons.brightness_auto_rounded,
                        isSelected: themeState.preference == ThemePreference.system,
                        onTap: () => themeNotifier.setThemePreference(ThemePreference.system),
                      ),
                      const SizedBox(width: 6),
                      _ThemeModeChip(
                        label: 'Light',
                        icon: Icons.light_mode_rounded,
                        isSelected: themeState.preference == ThemePreference.light,
                        onTap: () => themeNotifier.setThemePreference(ThemePreference.light),
                      ),
                      const SizedBox(width: 6),
                      _ThemeModeChip(
                        label: 'Dark',
                        icon: Icons.dark_mode_rounded,
                        isSelected: themeState.preference == ThemePreference.dark,
                        onTap: () => themeNotifier.setThemePreference(ThemePreference.dark),
                      ),
                      const SizedBox(width: 6),
                      _ThemeModeChip(
                        label: 'OLED Black',
                        icon: Icons.nightlight_round,
                        isSelected: themeState.preference == ThemePreference.oled,
                        onTap: () => themeNotifier.setThemePreference(ThemePreference.oled),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),
                const Divider(height: 1),
                const SizedBox(height: 14),

                const Text(
                  'Accent Palette',
                  style: TextStyle(fontSize: 14, fontWeight: FontWeight.w700),
                ),
                const SizedBox(height: 10),
                Wrap(
                  spacing: 10,
                  runSpacing: 10,
                  children: AppConstants.presetColors.map((preset) {
                    final color = preset['color'] as Color;
                    final isSelected = themeState.accentColor.value == color.value;

                    return InkWell(
                      onTap: () => themeNotifier.setAccentColor(color),
                      borderRadius: BorderRadius.circular(20),
                      child: Container(
                        width: 34,
                        height: 34,
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
              ],
            ),
          ),
          const SizedBox(height: 20),

          // Academic Defaults
          const _SectionHeader(title: 'Academic Settings'),
          const SizedBox(height: 8),
          AdroitCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      'Default Target Attendance',
                      style: TextStyle(fontSize: 14, fontWeight: FontWeight.w700),
                    ),
                    Text(
                      '${themeState.defaultTargetAttendance}%',
                      style: TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.w800,
                        color: theme.colorScheme.primary,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 4),
                Text(
                  'Used as the target threshold when adding new classes.',
                  style: TextStyle(
                    fontSize: 12,
                    color: theme.colorScheme.onSurface.withOpacity(0.5),
                  ),
                ),
                Slider(
                  value: themeState.defaultTargetAttendance.toDouble(),
                  min: 50,
                  max: 100,
                  divisions: 10,
                  onChanged: (val) =>
                      themeNotifier.setDefaultTargetAttendance(val.round()),
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),

          // Data Management
          const _SectionHeader(title: 'Data & Backups'),
          const SizedBox(height: 8),
          AdroitCard(
            padding: EdgeInsets.zero,
            child: Column(
              children: [
                ListTile(
                  leading: const Icon(Icons.upload_file_rounded),
                  title: const Text('Export Backup (JSON)'),
                  subtitle: const Text('Create a portable offline JSON snapshot', style: TextStyle(fontSize: 12)),
                  onTap: () => _exportBackup(context, ref),
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.download_rounded),
                  title: const Text('Import Backup'),
                  subtitle: const Text('Restore from Adroit or legacy Lumia JSON', style: TextStyle(fontSize: 12)),
                  onTap: () => _importBackup(context, ref),
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.delete_forever_rounded, color: Colors.redAccent),
                  title: const Text('Clear All Data', style: TextStyle(color: Colors.redAccent)),
                  subtitle: const Text('Permanently erase all courses and topics', style: TextStyle(fontSize: 12)),
                  onTap: () => _resetData(context, ref),
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),

          // About Adroit
          const _SectionHeader(title: 'About'),
          const SizedBox(height: 8),
          AdroitCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        color: theme.colorScheme.primary.withOpacity(0.12),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Icon(
                        Icons.school_rounded,
                        color: theme.colorScheme.primary,
                        size: 24,
                      ),
                    ),
                    const SizedBox(width: 14),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          AppConstants.appName,
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.w800,
                            letterSpacing: -0.4,
                          ),
                        ),
                        Text(
                          'Version ${AppConstants.appVersion}',
                          style: TextStyle(
                            fontSize: 12,
                            color: theme.colorScheme.onSurface.withOpacity(0.5),
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Text(
                  'Adroit is a minimalist, local-first academic battle-station focused exclusively on classes, timetables, and syllabus curriculum tracking. Built with Flutter, Drift, and Riverpod.',
                  style: TextStyle(
                    fontSize: 12,
                    color: theme.colorScheme.onSurface.withOpacity(0.7),
                    height: 1.4,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 32),
        ],
      ),
    );
  }
}

class _SectionHeader extends StatelessWidget {
  final String title;

  const _SectionHeader({required this.title});

  @override
  Widget build(BuildContext context) {
    return Text(
      title,
      style: const TextStyle(
        fontSize: 13,
        fontWeight: FontWeight.w700,
        color: Colors.grey,
        letterSpacing: 0.2,
      ),
    );
  }
}

class _ThemeModeChip extends StatelessWidget {
  final String label;
  final IconData icon;
  final bool isSelected;
  final VoidCallback onTap;

  const _ThemeModeChip({
    required this.label,
    required this.icon,
    required this.isSelected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(10),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        decoration: BoxDecoration(
          color: isSelected
              ? theme.colorScheme.primary.withOpacity(0.15)
              : theme.colorScheme.surfaceVariant,
          borderRadius: BorderRadius.circular(10),
          border: Border.all(
            color: isSelected ? theme.colorScheme.primary : Colors.transparent,
            width: 1.2,
          ),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              icon,
              size: 15,
              color: isSelected ? theme.colorScheme.primary : null,
            ),
            const SizedBox(width: 6),
            Text(
              label,
              style: TextStyle(
                fontSize: 12,
                fontWeight: isSelected ? FontWeight.w700 : FontWeight.w500,
                color: isSelected ? theme.colorScheme.primary : null,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
