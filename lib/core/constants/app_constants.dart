import 'package:flutter/material.dart';

class AppConstants {
  static const String appName = 'Adroit';
  static const String appTagline = 'Classes & Syllabus Tracker';
  static const String appVersion = '1.0.0';

  static const List<String> weekDays = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

  static const Map<String, String> weekDayFullNames = {
    'Mon': 'Monday',
    'Tue': 'Tuesday',
    'Wed': 'Wednesday',
    'Thu': 'Thursday',
    'Fri': 'Friday',
    'Sat': 'Saturday',
    'Sun': 'Sunday',
  };

  static const List<Map<String, dynamic>> presetColors = [
    {'name': 'Indigo', 'hex': '#4F46E5', 'color': Color(0xFF4F46E5)},
    {'name': 'Emerald', 'hex': '#10B981', 'color': Color(0xFF10B981)},
    {'name': 'Sapphire', 'hex': '#0EA5E9', 'color': Color(0xFF0EA5E9)},
    {'name': 'Rose', 'hex': '#F43F5E', 'color': Color(0xFFF43F5E)},
    {'name': 'Amber', 'hex': '#F59E0B', 'color': Color(0xFFF59E0B)},
    {'name': 'Violet', 'hex': '#8B5CF6', 'color': Color(0xFF8B5CF6)},
    {'name': 'Teal', 'hex': '#14B8A6', 'color': Color(0xFF14B8A6)},
    {'name': 'Coral', 'hex': '#FB923C', 'color': Color(0xFFFB923C)},
  ];

  static Color parseHexColor(String hexString, {Color fallback = const Color(0xFF4F46E5)}) {
    try {
      final buffer = StringBuffer();
      if (hexString.length == 6 || hexString.length == 7) buffer.write('ff');
      buffer.write(hexString.replaceFirst('#', ''));
      return Color(int.parse(buffer.toString(), radix: 16));
    } catch (_) {
      return fallback;
    }
  }

  static String colorToHex(Color color) {
    return '#${(color.value & 0xFFFFFF).toRadixString(16).padLeft(6, '0').toUpperCase()}';
  }
}
