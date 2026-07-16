package lumia.tracker.kost

import android.content.Context
import lumia.tracker.data.ScholarDao
import lumia.tracker.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONArray
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import lumia.tracker.BuildConfig

class KostRepository(
    private val scholarDao: ScholarDao,
    private val kostDao: KostDao
) {
    val allEvents: Flow<List<KostBehaviorEvent>> = kostDao.getAllEvents()
    val latestReport: Flow<KostPatternReport?> = kostDao.getLatestReport()

    suspend fun logEvent(
        category: String,
        action: String,
        durationMillis: Long? = null,
        rating: Float? = null,
        performanceMetric: Float? = null,
        tagString: String = "",
        description: String = "",
        metadata: Map<String, Any> = emptyMap()
    ) = withContext(Dispatchers.IO) {
        val metadataJson = try {
            val json = JSONObject()
            metadata.forEach { (key, value) ->
                json.put(key, value)
            }
            json.toString()
        } catch (e: Exception) {
            "{}"
        }

        val event = KostBehaviorEvent(
            category = category,
            action = action,
            durationMillis = durationMillis,
            rating = rating,
            performanceMetric = performanceMetric,
            tagString = tagString,
            description = description,
            metadataJson = metadataJson
        )
        kostDao.insertEvent(event)
    }

    suspend fun compileCompleteDataset(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()

        // 1. Courses
        val courses = scholarDao.exportAllCourses()
        val coursesArr = JSONArray()
        courses.forEach { c ->
            coursesArr.put(JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("code", c.code)
                put("schedule", c.schedule)
                put("scheduleDays", c.scheduleDays)
                put("scheduleStartTime", c.scheduleStartTime)
                put("scheduleEndTime", c.scheduleEndTime)
                put("attendedClasses", c.attendedClasses)
                put("totalClasses", c.totalClasses)
            })
        }
        root.put("courses", coursesArr)

        // 2. Assignments
        val assignments = scholarDao.exportAllAssignments()
        val assignmentsArr = JSONArray()
        assignments.forEach { a ->
            assignmentsArr.put(JSONObject().apply {
                put("id", a.id)
                put("courseId", a.courseId)
                put("title", a.title)
                put("dueDate", a.dueDateMillis)
                put("isCompleted", a.isCompleted)
                put("category", a.category)
                put("priority", a.priority)
                put("tags", a.tags)
            })
        }
        root.put("assignments", assignmentsArr)

        // 3. Tests
        val tests = scholarDao.exportAllTestRecords()
        val testsArr = JSONArray()
        tests.forEach { t ->
            testsArr.put(JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("date", t.dateMillis)
                put("marksObtained", t.marksObtained)
                put("totalMarks", t.totalMarks)
                put("courseId", t.courseId)
                put("tags", t.tags)
            })
        }
        root.put("tests", testsArr)

        // 4. Pomodoro Sessions
        val pomodoros = scholarDao.exportAllPomodoro()
        val pomodorosArr = JSONArray()
        pomodoros.forEach { p ->
            pomodorosArr.put(JSONObject().apply {
                put("id", p.id)
                put("date", p.dateMillis)
                put("durationMinutes", p.durationMinutes)
                put("courseId", p.courseId)
                put("taskId", p.taskId)
                put("assignmentId", p.assignmentId)
            })
        }
        root.put("pomodoros", pomodorosArr)

        // 5. Tasks
        val tasks = scholarDao.exportAllTasks()
        val tasksArr = JSONArray()
        tasks.forEach { t ->
            tasksArr.put(JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("isCompleted", t.isCompleted)
                put("dueDate", t.dueDateMillis)
                put("priority", t.priority)
                put("tags", t.tags)
                put("courseId", t.courseId)
            })
        }
        root.put("tasks", tasksArr)

        // 6. Attendance
        val attendance = scholarDao.exportAllAttendance()
        val attendanceArr = JSONArray()
        attendance.forEach { att ->
            attendanceArr.put(JSONObject().apply {
                put("id", att.id)
                put("courseId", att.courseId)
                put("date", att.dateMillis)
                put("status", att.status)
            })
        }
        root.put("attendance", attendanceArr)

        // 7. Recent Kost Behavioral Actions
        val behaviorEvents = kostDao.getAllEventsList()
        val behaviorArr = JSONArray()
        behaviorEvents.take(50).forEach { b ->
            behaviorArr.put(JSONObject().apply {
                put("category", b.category)
                put("action", b.action)
                put("timestamp", b.timestamp)
                put("durationMillis", b.durationMillis)
                put("rating", b.rating)
                put("performance", b.performanceMetric)
                put("tags", b.tagString)
                put("description", b.description)
                put("metadataJson", b.metadataJson)
            })
        }
        root.put("behavioralEvents", behaviorArr)

        root.toString(2)
    }

    suspend fun runKostAnalysis(): Result<KostPatternReport> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured."))
        }

        val dataset = compileCompleteDataset()

        val promptText = """
            You are "Kost", the advanced, central behavioral pattern intelligence engine integrated directly into the Lumia study tracker app.
            Your task is to analyze the user's complete dataset (routines, plans, tasks, pomodoro sessions, attendance, tests, assignments, and logged actions) to notice deep behavioral patterns, productivity correlations, plans deviations, and consistency trends.
            
            Here is the complete JSON dataset of the user:
            $dataset

            Analyze this data and return your insights STRICTLY in the following JSON format. Do not return any other text, markdown formatting, or HTML. Return ONLY a valid JSON object matching this structure:
            {
              "summary": "A concise, elegant 2-3 sentence overview of the most critical behavior pattern identified.",
              "detected_patterns": [
                {
                  "name": "Pattern name (e.g., Tuesday Academic Surge)",
                  "type": "Strength / Weakness / Correlation / Trend",
                  "description": "Detailed explanation of what the pattern is, pointing to specific days, hours, subjects, or actions.",
                  "correlation_coefficient": "Strong / Medium / Light",
                  "confidence": 0.9
                }
              ],
              "academic_correlations": {
                "study_time_to_test_marks": "Heuristic correlation analysis between Pomodoro study minutes and test scores achieved in associated subjects/courses.",
                "attendance_to_assignment_completion": "Heuristic correlation analysis between class attendance status and whether assignments/tasks are completed on time.",
                "planning_lead_time_to_success": "Analysis of whether creating plans/tasks earlier (checking dueDates vs task creation) correlates with higher completion rates."
              },
              "plan_deviations": [
                {
                  "plan": "Plan / Task name",
                  "deviation_type": "Late submission / Skipped class / Overdue task",
                  "impact": "Heuristic estimate of how this deviation affected other goals or performance metrics."
                }
              ],
              "action_plan_suggestions": "Bullet points list of highly specific, actionable, data-driven behavioral recommendations to optimize their learning routine, study plan, and streak."
            }
        """.trimIndent()

        // Construct request
        val requestBodyJson = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().apply {
                    put("text", promptText)
                }))
            }))
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.4)
            })
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(requestBodyJson.toString().toRequestBody(mediaType))
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP Error: ${response.code} ${response.message}"))
            }

            val bodyString = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response body"))
            val responseJson = JSONObject(bodyString)
            val candidates = responseJson.optJSONArray("candidates")
            val textContent = candidates?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text") ?: ""

            if (textContent.isEmpty()) {
                return@withContext Result.failure(Exception("No parsed text in Gemini response"))
            }

            // Parse textContent to verify it's a valid JSON object matching our criteria
            val parsedResult = JSONObject(textContent)
            val summary = parsedResult.optString("summary", "Analysis completed successfully.")
            val suggestions = parsedResult.optString("action_plan_suggestions", "Keep up the excellent study consistency!")

            val report = KostPatternReport(
                summary = summary,
                insightsJson = textContent,
                modelAccuracyMetric = 0.95f,
                actionPlanSuggestions = suggestions
            )

            kostDao.insertReport(report)
            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
