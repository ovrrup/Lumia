import re

with open("app/src/main/java/lumia/tracker/model/Entities.kt", "r") as f:
    content = f.read()

# remove double @JsonClass
content = content.replace("@JsonClass(generateAdapter = true)\n@JsonClass(generateAdapter = true)", "@JsonClass(generateAdapter = true)")

# add default emptyList() to ScholarBackup courses, subjects, topics, assignments if they don't have it
content = content.replace("val courses: List<Course>,", "val courses: List<Course> = emptyList(),")
content = content.replace("val subjects: List<Subject>,", "val subjects: List<Subject> = emptyList(),")
content = content.replace("val topics: List<Topic>,", "val topics: List<Topic> = emptyList(),")
content = content.replace("val assignments: List<PracticeAssignment>,", "val assignments: List<PracticeAssignment> = emptyList(),")

with open("app/src/main/java/lumia/tracker/model/Entities.kt", "w") as f:
    f.write(content)
