import 'package:flutter/material.dart';

class StatusBadge extends StatelessWidget {
  final String label;
  final Color color;
  final IconData? icon;
  final bool isFilled;

  const StatusBadge({
    super.key,
    required this.label,
    required this.color,
    this.icon,
    this.isFilled = false,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: isFilled ? color : color.withOpacity(0.12),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(
          color: isFilled ? color : color.withOpacity(0.3),
          width: 0.8,
        ),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (icon != null) ...[
            Icon(
              icon,
              size: 13,
              color: isFilled ? Colors.white : color,
            ),
            const SizedBox(width: 4),
          ],
          Text(
            label,
            style: TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.w600,
              color: isFilled ? Colors.white : color,
              letterSpacing: -0.2,
            ),
          ),
        ],
      ),
    );
  }
}
