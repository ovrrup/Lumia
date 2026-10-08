import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'classes/classes_screen.dart';
import 'syllabus/syllabus_screen.dart';
import 'tasks/tasks_screen.dart';
import 'settings/settings_screen.dart';

final selectedNavigationIndexProvider = StateProvider<int>((ref) => 0);

class MainNavigationScreen extends ConsumerWidget {
  const MainNavigationScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final selectedIndex = ref.watch(selectedNavigationIndexProvider);

    final screens = const [
      ClassesScreen(),
      SyllabusScreen(),
      TasksScreen(),
      SettingsScreen(),
    ];

    final isDark = theme.brightness == Brightness.dark;

    return Scaffold(
      extendBody: true,
      body: IndexedStack(
        index: selectedIndex,
        children: screens,
      ),
      bottomNavigationBar: SafeArea(
        child: Container(
          margin: const EdgeInsets.only(left: 20, right: 20, bottom: 12),
          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
          decoration: BoxDecoration(
            color: theme.colorScheme.surface,
            borderRadius: BorderRadius.circular(32),
            border: Border.all(
              color: theme.colorScheme.outline,
              width: 1,
            ),
            boxShadow: isDark
                ? [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.4),
                      blurRadius: 16,
                      offset: const Offset(0, 6),
                    ),
                  ]
                : [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.06),
                      blurRadius: 14,
                      offset: const Offset(0, 4),
                    ),
                  ],
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _DockNavItem(
                icon: Icons.calendar_today_rounded,
                label: 'Classes',
                isSelected: selectedIndex == 0,
                onTap: () => ref.read(selectedNavigationIndexProvider.notifier).state = 0,
              ),
              _DockNavItem(
                icon: Icons.menu_book_rounded,
                label: 'Syllabus',
                isSelected: selectedIndex == 1,
                onTap: () => ref.read(selectedNavigationIndexProvider.notifier).state = 1,
              ),
              _DockNavItem(
                icon: Icons.checklist_rounded,
                label: 'Tasks',
                isSelected: selectedIndex == 2,
                onTap: () => ref.read(selectedNavigationIndexProvider.notifier).state = 2,
              ),
              _DockNavItem(
                icon: Icons.tune_rounded,
                label: 'Settings',
                isSelected: selectedIndex == 3,
                onTap: () => ref.read(selectedNavigationIndexProvider.notifier).state = 3,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _DockNavItem extends StatelessWidget {
  final IconData icon;
  final String label;
  final bool isSelected;
  final VoidCallback onTap;

  const _DockNavItem({
    required this.icon,
    required this.label,
    required this.isSelected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final accentColor = theme.colorScheme.primary;

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(24),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 220),
        curve: Curves.easeOutCubic,
        padding: EdgeInsets.symmetric(
          horizontal: isSelected ? 16 : 12,
          vertical: 8,
        ),
        decoration: BoxDecoration(
          color: isSelected ? accentColor.withOpacity(0.14) : Colors.transparent,
          borderRadius: BorderRadius.circular(24),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              icon,
              size: 20,
              color: isSelected ? accentColor : theme.colorScheme.onSurface.withOpacity(0.45),
            ),
            if (isSelected) ...[
              const SizedBox(width: 8),
              Text(
                label,
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w700,
                  letterSpacing: -0.2,
                  color: accentColor,
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
