package lumia.tracker.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object VersionUtils {
    /**
     * Compares two version strings (e.g., "1.0.2" and "1.0.3").
     * Returns true if the remote version is strictly greater than the current version.
     */
    fun isUpdateAvailable(current: String, remote: String): Boolean {
        val currentBase = current.split("-").firstOrNull() ?: current
        val remoteBase = remote.split("-").firstOrNull() ?: remote
        val currentClean = currentBase.lowercase().replace("v", "").replace("foss", "").trim()
        val remoteClean = remoteBase.lowercase().replace("v", "").replace("foss", "").trim()
        
        if (currentClean == remoteClean) return false
        
        val currentParts = currentClean.split(".")
        val remoteParts = remoteClean.split(".")
        
        val length = maxOf(currentParts.size, remoteParts.size)
        for (i in 0 until length) {
            val currentPart = currentParts.getOrNull(i)?.toIntOrNull() ?: 0
            val remotePart = remoteParts.getOrNull(i)?.toIntOrNull() ?: 0
            
            if (remotePart > currentPart) return true
            if (remotePart < currentPart) return false
        }
        
        return false
    }

    /**
     * Parses an ISO 8601 timestamp string (e.g., "2026-08-24T17:17:49Z") into epoch milliseconds.
     */
    fun parseIsoTimestamp(isoString: String): Long {
        if (isoString.isBlank()) return 0L
        return try {
            val clean = isoString.replace("Z", "+0000")
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            format.parse(clean)?.time ?: 0L
        } catch (e: Exception) {
            try {
                val formatFallback = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                formatFallback.parse(isoString.substringBefore("Z").substringBefore("+"))?.time ?: 0L
            } catch (e2: Exception) {
                0L
            }
        }
    }

    /**
     * Returns true if the remote published ISO timestamp is strictly newer than the installed app time.
     */
    fun isNightlyNewer(installedTimeMillis: Long, publishedAtIso: String): Boolean {
        val remoteTime = parseIsoTimestamp(publishedAtIso)
        if (remoteTime <= 0L || installedTimeMillis <= 0L) return false
        return remoteTime > (installedTimeMillis + 60_000L)
    }
}
