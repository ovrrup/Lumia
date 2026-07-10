package lumia.tracker.model

import java.io.Serializable

data class TagMetadata(
    val name: String,
    val courses: List<Course> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val chapters: List<Chapter> = emptyList(),
    val topics: List<Topic> = emptyList(),
    val assignments: List<PracticeAssignment> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val testRecords: List<TestRecord> = emptyList()
) : Serializable {
    val totalCount: Int
        get() = courses.size + subjects.size + chapters.size + topics.size + assignments.size + tasks.size + testRecords.size
}
