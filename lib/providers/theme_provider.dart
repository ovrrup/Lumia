import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../core/constants/app_constants.dart';
import '../core/theme/app_theme.dart';

class ThemeState {
  final ThemePreference preference;
  final Color accentColor;
  final int defaultTargetAttendance;

  const ThemeState({
    required this.preference,
    required this.accentColor,
    this.defaultTargetAttendance = 75,
  });

  ThemeState copyWith({
    ThemePreference? preference,
    Color? accentColor,
    int? defaultTargetAttendance,
  }) {
    return ThemeState(
      preference: preference ?? this.preference,
      accentColor: accentColor ?? this.accentColor,
      defaultTargetAttendance: defaultTargetAttendance ?? this.defaultTargetAttendance,
    );
  }
}

class ThemeNotifier extends StateNotifier<ThemeState> {
  ThemeNotifier()
      : super(const ThemeState(
          preference: ThemePreference.system,
          accentColor: Color(0xFF4F46E5),
          defaultTargetAttendance: 75,
        )) {
    _loadFromPrefs();
  }

  static const String _keyTheme = 'adroit_theme_preference';
  static const String _keyAccent = 'adroit_accent_color';
  static const String _keyTarget = 'adroit_default_target_attendance';

  Future<void> _loadFromPrefs() async {
    final prefs = await SharedPreferences.getInstance();
    final themeStr = prefs.getString(_keyTheme) ?? 'system';
    final accentHex = prefs.getString(_keyAccent) ?? '#4F46E5';
    final target = prefs.getInt(_keyTarget) ?? 75;

    ThemePreference pref = ThemePreference.system;
    if (themeStr == 'light') pref = ThemePreference.light;
    if (themeStr == 'dark') pref = ThemePreference.dark;
    if (themeStr == 'oled') pref = ThemePreference.oled;

    final color = AppConstants.parseHexColor(accentHex);

    state = state.copyWith(
      preference: pref,
      accentColor: color,
      defaultTargetAttendance: target,
    );
  }

  Future<void> setThemePreference(ThemePreference pref) async {
    state = state.copyWith(preference: pref);
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_keyTheme, pref.name);
  }

  Future<void> setAccentColor(Color color) async {
    state = state.copyWith(accentColor: color);
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_keyAccent, AppConstants.colorToHex(color));
  }

  Future<void> setDefaultTargetAttendance(int target) async {
    state = state.copyWith(defaultTargetAttendance: target);
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt(_keyTarget, target);
  }
}

final themeProvider = StateNotifierProvider<ThemeNotifier, ThemeState>((ref) {
  return ThemeNotifier();
});
