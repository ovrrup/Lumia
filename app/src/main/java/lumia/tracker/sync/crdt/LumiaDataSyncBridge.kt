package lumia.tracker.sync.crdt

/**
 * High-level bridge connecting practical user items (tasks, notes, focus sessions)
 * to the database-free CRDT document engine.
 * Converts structured user items into conflict-free CRDT mutations and back.
 */
class LumiaDataSyncBridge(private val crdtDocument: AutomergeCrdtDocument) {

    data class SyncedTaskItem(
        val id: String,
        val title: String,
        val completed: Boolean,
        val courseName: String,
        val dueDate: Long
    )

    data class SyncedNoteItem(
        val id: String,
        val title: String,
        val content: String,
        val lastModified: Long
    )

    data class SyncedSessionItem(
        val id: String,
        val courseId: String,
        val durationMinutes: Int,
        val timestamp: Long
    )

    // --- Write Helpers ---

    fun putTask(task: SyncedTaskItem) {
        val path = "tasks/${task.id}"
        crdtDocument.set(path, "id", task.id)
        crdtDocument.set(path, "title", task.title)
        crdtDocument.set(path, "completed", task.completed)
        crdtDocument.set(path, "courseName", task.courseName)
        crdtDocument.set(path, "dueDate", task.dueDate)
    }

    fun removeTask(taskId: String) {
        crdtDocument.delete("tasks", taskId)
    }

    fun putNote(note: SyncedNoteItem) {
        val path = "notes/${note.id}"
        crdtDocument.set(path, "id", note.id)
        crdtDocument.set(path, "title", note.title)
        crdtDocument.set(path, "content", note.content)
        crdtDocument.set(path, "lastModified", note.lastModified)
    }

    fun putSession(session: SyncedSessionItem) {
        val path = "sessions/${session.id}"
        crdtDocument.set(path, "id", session.id)
        crdtDocument.set(path, "courseId", session.courseId)
        crdtDocument.set(path, "durationMinutes", session.durationMinutes)
        crdtDocument.set(path, "timestamp", session.timestamp)
    }

    // --- Read Helpers ---

    @Suppress("UNCHECKED_CAST")
    fun getAllSyncedTasks(): List<SyncedTaskItem> {
        val data = crdtDocument.getSnapshot().documentData
        val tasksMap = data["tasks"] as? Map<String, Any?> ?: return emptyList()

        val list = mutableListOf<SyncedTaskItem>()
        for ((_, item) in tasksMap) {
            val map = item as? Map<String, Any?> ?: continue
            val id = map["id"]?.toString() ?: continue
            val title = map["title"]?.toString() ?: "Untitled Task"
            val completed = map["completed"] as? Boolean ?: false
            val course = map["courseName"]?.toString() ?: "General"
            val due = (map["dueDate"] as? Number)?.toLong() ?: 0L

            list.add(SyncedTaskItem(id, title, completed, course, due))
        }
        return list
    }

    @Suppress("UNCHECKED_CAST")
    fun getAllSyncedNotes(): List<SyncedNoteItem> {
        val data = crdtDocument.getSnapshot().documentData
        val notesMap = data["notes"] as? Map<String, Any?> ?: return emptyList()

        val list = mutableListOf<SyncedNoteItem>()
        for ((_, item) in notesMap) {
            val map = item as? Map<String, Any?> ?: continue
            val id = map["id"]?.toString() ?: continue
            val title = map["title"]?.toString() ?: "Untitled Note"
            val content = map["content"]?.toString() ?: ""
            val mod = (map["lastModified"] as? Number)?.toLong() ?: 0L

            list.add(SyncedNoteItem(id, title, content, mod))
        }
        return list
    }

    @Suppress("UNCHECKED_CAST")
    fun getAllSyncedSessions(): List<SyncedSessionItem> {
        val data = crdtDocument.getSnapshot().documentData
        val sessionsMap = data["sessions"] as? Map<String, Any?> ?: return emptyList()

        val list = mutableListOf<SyncedSessionItem>()
        for ((_, item) in sessionsMap) {
            val map = item as? Map<String, Any?> ?: continue
            val id = map["id"]?.toString() ?: continue
            val courseId = map["courseId"]?.toString() ?: ""
            val duration = (map["durationMinutes"] as? Number)?.toInt() ?: 25
            val time = (map["timestamp"] as? Number)?.toLong() ?: 0L

            list.add(SyncedSessionItem(id, courseId, duration, time))
        }
        return list
    }
}
