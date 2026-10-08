import 'package:flutter/material.dart';

class AdroitProgressBar extends StatelessWidget {
  final double progress; // 0.0 to 1.0
  final Color? color;
  final double height;
  final Color? backgroundColor;

  const AdroitProgressBar({
    super.key,
    required this.progress,
    this.color,
    this.height = 6,
    this.backgroundColor,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final clamped = progress.clamp(0.0, 1.0);
    final activeColor = color ?? theme.colorScheme.primary;
    final isDark = theme.brightness == Brightness.dark;

    final bg = backgroundColor ??
        (isDark ? const Color(0xFF262626) : const Color(0xFFE5E7EB));

    return LayoutBuilder(
      builder: (context, constraints) {
        return Container(
          width: constraints.maxWidth,
          height: height,
          decoration: BoxDecoration(
            color: bg,
            borderRadius: BorderRadius.circular(height / 2),
          ),
          child: FractionallySizedBox(
            alignment: Alignment.centerLeft,
            widthFactor: clamped,
            child: Container(
              decoration: BoxDecoration(
                color: activeColor,
                borderRadius: BorderRadius.circular(height / 2),
              ),
            ),
          ),
        );
      },
    );
  }
}
