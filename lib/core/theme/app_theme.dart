import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

enum ThemePreference { system, light, dark, oled }

class AppTheme {
  // Ultra-refined neutral palette (Zinc / Slate inspired)
  static const Color darkBg = Color(0xFF09090B);
  static const Color darkSurface = Color(0xFF131316);
  static const Color darkSurfaceElevated = Color(0xFF1C1C21);
  static const Color darkBorder = Color(0xFF26262E);

  static const Color oledBg = Color(0xFF000000);
  static const Color oledSurface = Color(0xFF0A0A0C);
  static const Color oledSurfaceElevated = Color(0xFF141418);
  static const Color oledBorder = Color(0xFF1E1E24);

  static const Color lightBg = Color(0xFFF8F9FA);
  static const Color lightSurface = Color(0xFFFFFFFF);
  static const Color lightSurfaceElevated = Color(0xFFF1F3F5);
  static const Color lightBorder = Color(0xFFE4E4E7);

  static ThemeData createTheme({
    required Brightness brightness,
    required Color accentColor,
    bool isOled = false,
  }) {
    final isDark = brightness == Brightness.dark;

    final bg = isOled ? oledBg : (isDark ? darkBg : lightBg);
    final surface = isOled ? oledSurface : (isDark ? darkSurface : lightSurface);
    final surfaceElevated = isOled
        ? oledSurfaceElevated
        : (isDark ? darkSurfaceElevated : lightSurfaceElevated);
    final border = isOled ? oledBorder : (isDark ? darkBorder : lightBorder);

    final colorScheme = ColorScheme.fromSeed(
      seedColor: accentColor,
      brightness: brightness,
    ).copyWith(
      surface: surface,
      surfaceContainer: surfaceElevated,
      surfaceContainerHigh: surfaceElevated,
      outline: border,
    );

    return ThemeData(
      useMaterial3: true,
      brightness: brightness,
      colorScheme: colorScheme,
      scaffoldBackgroundColor: bg,
      appBarTheme: AppBarTheme(
        backgroundColor: bg,
        elevation: 0,
        scrolledUnderElevation: 0,
        centerTitle: false,
        systemOverlayStyle: isDark ? SystemUiOverlayStyle.light : SystemUiOverlayStyle.dark,
        titleTextStyle: TextStyle(
          color: isDark ? Colors.white : const Color(0xFF09090B),
          fontSize: 22,
          fontWeight: FontWeight.w800,
          letterSpacing: -0.6,
        ),
        iconTheme: IconThemeData(
          color: isDark ? Colors.white : const Color(0xFF09090B),
          size: 20,
        ),
      ),
      cardTheme: CardThemeData(
        color: surface,
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20),
          side: BorderSide(color: border, width: 1),
        ),
        margin: EdgeInsets.zero,
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: surfaceElevated,
        contentPadding: const EdgeInsets.symmetric(horizontal: 18, vertical: 16),
        labelStyle: TextStyle(
          color: isDark ? const Color(0xFFA1A1AA) : const Color(0xFF71717A),
          fontSize: 13,
          fontWeight: FontWeight.w500,
        ),
        hintStyle: TextStyle(
          color: isDark ? const Color(0xFF52525B) : const Color(0xFFA1A1AA),
          fontSize: 13,
        ),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: BorderSide(color: border, width: 1),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: BorderSide(color: border, width: 1),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: BorderSide(color: accentColor, width: 1.5),
        ),
      ),
      dialogTheme: DialogThemeData(
        backgroundColor: surface,
        surfaceTintColor: Colors.transparent,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(24),
          side: BorderSide(color: border, width: 1),
        ),
      ),
      floatingActionButtonTheme: FloatingActionButtonThemeData(
        backgroundColor: accentColor,
        foregroundColor: Colors.white,
        elevation: 4,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
      ),
      dividerTheme: DividerThemeData(
        color: border,
        thickness: 1,
        space: 1,
      ),
      chipTheme: ChipThemeData(
        backgroundColor: surfaceElevated,
        side: BorderSide(color: border, width: 1),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
        labelStyle: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600),
      ),
    );
  }
}
