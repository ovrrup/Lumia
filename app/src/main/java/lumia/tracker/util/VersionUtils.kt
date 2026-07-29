package lumia.tracker.util

object VersionUtils {
    /**
     * Compares two version strings (e.g., "1.0.2" and "1.0.3").
     * Returns true if the remote version is strictly greater than the current version.
     */
    fun isUpdateAvailable(current: String, remote: String): Boolean {
        val currentClean = current.lowercase().replace("v", "").replace("-foss", "").trim()
        val remoteClean = remote.lowercase().replace("v", "").replace("-foss", "").trim()
        
        if (currentClean == remoteClean) return false
        
        val currentParts = currentClean.split(".")
        val remoteParts = remoteClean.split(".")
        
        fun parsePart(part: String?): Int {
            if (part.isNullOrEmpty()) return 0
            val digitsOnly = part.takeWhile { it.isDigit() }
            return digitsOnly.toIntOrNull() ?: 0
        }

        val length = maxOf(currentParts.size, remoteParts.size)
        for (i in 0 until length) {
            val currentPart = parsePart(currentParts.getOrNull(i))
            val remotePart = parsePart(remoteParts.getOrNull(i))
            
            if (remotePart > currentPart) return true
            if (remotePart < currentPart) return false
        }
        
        return false
    }
}
