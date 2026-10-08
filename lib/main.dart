import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'core/theme/app_theme.dart';
import 'providers/theme_provider.dart';
import 'screens/main_navigation_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const ProviderScope(child: AdroitApp()));
}

class AdroitApp extends ConsumerWidget {
  const AdroitApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final themeState = ref.watch(themeProvider);

    final lightTheme = AppTheme.createTheme(
      brightness: Brightness.light,
      accentColor: themeState.accentColor,
    );

    final darkTheme = AppTheme.createTheme(
      brightness: Brightness.dark,
      accentColor: themeState.accentColor,
      isOled: themeState.preference == ThemePreference.oled,
    );

    ThemeMode mode;
    switch (themeState.preference) {
      case ThemePreference.light:
        mode = ThemeMode.light;
        break;
      case ThemePreference.dark:
      case ThemePreference.oled:
        mode = ThemeMode.dark;
        break;
      case ThemePreference.system:
        mode = ThemeMode.system;
        break;
    }

    return MaterialApp(
      title: 'Adroit',
      debugShowCheckedModeBanner: false,
      theme: lightTheme,
      darkTheme: darkTheme,
      themeMode: mode,
      home: const MainNavigationScreen(),
    );
  }
}
