import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/database/database.dart';
import '../data/backup/backup_service.dart';

final databaseProvider = Provider<AppDatabase>((ref) {
  final db = AppDatabase();
  ref.onDispose(() => db.close());
  return db;
});

final backupServiceProvider = Provider<BackupService>((ref) {
  final db = ref.watch(databaseProvider);
  return BackupService(db);
});
