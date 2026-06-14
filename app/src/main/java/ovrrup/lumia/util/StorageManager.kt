package ovrrup.lumia.util

import android.content.Context
import android.util.Log
import ovrrup.lumia.data.AppDatabase
import java.io.File

object StorageManager {
    fun getDatabaseSize(context: Context): Long {
        val dbPath = context.getDatabasePath("scholar_sync_database")
        if (!dbPath.exists()) return 0L
        
        var totalSize = dbPath.length()
        
        // Also check sidecars (-wal and -shm files)
        val walFile = File(dbPath.absolutePath + "-wal")
        if (walFile.exists()) totalSize += walFile.length()
        
        val shmFile = File(dbPath.absolutePath + "-shm")
        if (shmFile.exists()) totalSize += shmFile.length()
        
        return totalSize
    }

    fun getCacheSize(context: Context): Long {
        var totalSize = 0L
        try {
            totalSize += getFolderSize(context.cacheDir)
            context.externalCacheDir?.let {
                totalSize += getFolderSize(it)
            }
            // Code cache
            totalSize += getFolderSize(context.codeCacheDir)
        } catch (e: Exception) {
            Log.e("StorageManager", "Error calculating cache size", e)
        }
        return totalSize
    }
    
    private fun getFolderSize(file: File?): Long {
        if (file == null || !file.exists()) return 0L
        if (file.isFile) return file.length()
        
        var size = 0L
        val children = file.listFiles() ?: return 0L
        for (child in children) {
            size += getFolderSize(child)
        }
        return size
    }

    fun formatSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0.00 KB"
        val units = arrayOf("Bytes", "KB", "MB", "GB")
        val digitGroups = (Math.log10(sizeInBytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = Math.min(digitGroups, units.size - 1)
        return String.format("%.2f %s", sizeInBytes / Math.pow(1024.0, index.toDouble()), units[index])
    }

    suspend fun optimizeStorage(context: Context): Map<String, String> {
        val initialDbSize = getDatabaseSize(context)
        val initialCacheSize = getCacheSize(context)
        
        // 1. Clear internal cache files
        try {
            deleteDirContent(context.cacheDir)
            context.externalCacheDir?.let { deleteDirContent(it) }
            deleteDirContent(context.codeCacheDir)
        } catch (e: Exception) {
            Log.e("StorageManager", "Error clearing caches", e)
        }
        
        // 2. Perform DB checkpoint and VACUUM
        try {
            val db = AppDatabase.getDatabase(context)
            val dbHelper = db.openHelper.writableDatabase
            dbHelper.execSQL("PRAGMA wal_checkpoint(TRUNCATE)")
            dbHelper.execSQL("VACUUM")
        } catch (e: Exception) {
            Log.e("StorageManager", "Database optimization failed", e)
        }
        
        val finalDbSize = getDatabaseSize(context)
        val finalCacheSize = getCacheSize(context)
        
        val spaceSaved = (initialDbSize + initialCacheSize) - (finalDbSize + finalCacheSize)
        val savedFormatted = if (spaceSaved > 0) formatSize(spaceSaved) else "0.00 KB"
        
        return mapOf(
            "space_saved" to savedFormatted,
            "db_size" to formatSize(finalDbSize),
            "cache_size" to formatSize(finalCacheSize)
        )
    }

    private fun deleteDirContent(file: File?): Boolean {
        if (file == null || !file.exists()) return false
        if (file.isDirectory) {
            val children = file.listFiles() ?: return true
            for (child in children) {
                deleteDirContent(child)
                child.delete()
            }
        }
        return true
    }
}
