package ovrrup.lumia.util

object VersionUtils {
    fun isUpdateAvailable(currentVersion: String, remoteVersion: String): Boolean {
        val current = currentVersion.trim().removePrefix("v").split(".")
        val remote = remoteVersion.trim().removePrefix("v").split(".")
        val maxLen = maxOf(current.size, remote.size)
        for (i in 0 until maxLen) {
            val cVal = current.getOrNull(i)?.toIntOrNull() ?: 0
            val rVal = remote.getOrNull(i)?.toIntOrNull() ?: 0
            if (rVal > cVal) return true
            if (cVal > rVal) return false
        }
        return false
    }
}
