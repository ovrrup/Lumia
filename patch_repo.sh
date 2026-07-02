sed -i 's/suspend fun exportDataToStream(outputStream: OutputStream, settings: Map<String, String>) {/suspend fun exportDataToStream(outputStream: OutputStream, backup: ScholarBackup) {/g' app/src/main/java/lumia/tracker/data/ScholarRepository.kt
sed -i '105,119d' app/src/main/java/lumia/tracker/data/ScholarRepository.kt
