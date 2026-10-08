import 'package:flutter/material.dart';

class AdroitCard extends StatelessWidget {
  final Widget child;
  final EdgeInsetsGeometry? padding;
  final VoidCallback? onTap;
  final Color? color;
  final BorderSide? border;

  const AdroitCard({
    super.key,
    required this.child,
    this.padding,
    this.onTap,
    this.color,
    this.border,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    final cardBorder = border ??
        BorderSide(
          color: isDark ? const Color(0xFF262626) : const Color(0xFFE5E7EB),
          width: 1,
        );

    Widget content = Container(
      padding: padding ?? const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: color ?? theme.cardTheme.color,
        borderRadius: BorderRadius.circular(16),
        border: Border.fromBorderSide(cardBorder),
      ),
      child: child,
    );

    if (onTap != null) {
      return Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(16),
          child: content,
        ),
      );
    }

    return content;
  }
}
