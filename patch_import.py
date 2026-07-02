import re

with open("app/src/main/java/lumia/tracker/data/ScholarRepository.kt", "r") as f:
    content = f.read()

start_idx = content.find("suspend fun importDataFromStream")
end_idx = content.find("return backup.settings", start_idx) + len("return backup.settings")

import_code = """    suspend fun importDataFromStream(inputStream: InputStream): ScholarBackup {
        val bis = java.io.BufferedInputStream(inputStream)
        bis.mark(2)
        val header = ByteArray(2)
        val readBytes = bis.read(header)
        bis.reset()

        val json = if (readBytes == 2 && header[0] == 0x1f.toByte() && header[1] == 0x8b.toByte()) {
            java.util.zip.GZIPInputStream(bis).reader().use { it.readText() }
        } else {
            bis.reader().use { it.readText() }
        }
        val backup = backupAdapter.fromJson(json) ?: throw IllegalArgumentException("Invalid backup file")
        return backup
    }"""

content = content[:start_idx] + import_code + content[end_idx:]

with open("app/src/main/java/lumia/tracker/data/ScholarRepository.kt", "w") as f:
    f.write(content)
