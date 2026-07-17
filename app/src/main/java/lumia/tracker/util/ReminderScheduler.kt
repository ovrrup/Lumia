package lumia.tracker.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import lumia.tracker.MainActivity
import lumia.tracker.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val assignmentId = intent.getIntExtra("assignment_id", -1)
        
        val profMgr = lumia.tracker.data.ProfileManager(context)
        val prefs = profMgr.getProfilePrefs()
        val formalTone = prefs.getBoolean("notif_formal_tone", true)
        val enableDeadlines = prefs.getBoolean("notif_enable_deadlines", true)
        
        if (action == "ACTION_MARK_DONE" && assignmentId != -1) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(assignmentId)
            
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                if (assignmentId >= 20000) {
                    val taskId = assignmentId - 20000
                    val tasks = db.scholarDao().exportAllTasks()
                    val task = tasks.find { it.id == taskId }
                    if (task != null) {
                        db.scholarDao().updateTask(task.copy(isCompleted = true))
                    }
                } else {
                    val assignments = db.scholarDao().exportAllAssignments()
                    val assignment = assignments.find { it.id == assignmentId }
                    if (assignment != null) {
                        db.scholarDao().updateAssignment(assignment.copy(isCompleted = true))
                    }
                }
            }
            return
        }

        if (action == "ACTION_SNOOZE" && assignmentId != -1) {
            val title = intent.getStringExtra("title") ?: "Assignment Due"
            val desc = intent.getStringExtra("desc") ?: "You have an assignment to complete."
            val interconnections = intent.getStringExtra("interconnections") ?: ""
            val type = intent.getStringExtra("type") ?: "assignment"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(assignmentId)
            
            // Snooze for 15 minutes
            ReminderScheduler.scheduleReminderExact(context, assignmentId, title, desc, interconnections, System.currentTimeMillis() + 15 * 60 * 1000, type = type)
            return
        }

        val typeExtra = intent.getStringExtra("type") ?: "assignment"
        val titleExtra = intent.getStringExtra("title") ?: "Assignment Due"
        val descExtra = intent.getStringExtra("desc") ?: "You have an assignment to complete."
        val interconnections = intent.getStringExtra("interconnections") ?: ""

        val isClass = typeExtra == "class_start" || typeExtra == "class_end"
        if (isClass) {
            val enableClasses = prefs.getBoolean("notif_enable_classes", true)
            if (!enableClasses) return
        } else {
            if (!enableDeadlines) return
        }

        val (finalTitle, finalDesc) = NotificationContent.getPersonalizedContent(
            type = typeExtra,
            title = titleExtra,
            desc = descExtra,
            tone = if (formalTone) "Formal" else "Aggressive",
            interconnections = interconnections
        )
        
        Log.d("ReminderReceiver", "Showing notification for: $finalTitle")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "scholar_sync_channel", 
                "ScholarSync Reminders", 
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for assignments, tasks, and classes"
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (typeExtra == "task") {
                putExtra("OPEN_TAB", 3)
            } else if (intent.hasExtra("courseId")) {
                val cId = intent.getIntExtra("courseId", -1)
                if (cId != -1) {
                    putExtra("OPEN_SCREEN", "courseDetail/$cId")
                }
            } else if (intent.hasExtra("subjectId")) {
                val sId = intent.getIntExtra("subjectId", -1)
                if (sId != -1) {
                    putExtra("OPEN_SCREEN", "subjectDetail/$sId")
                }
            } else if (typeExtra == "class_start" || typeExtra == "class_end") {
                putExtra("OPEN_TAB", 1) // Courses tab
            } else {
                putExtra("OPEN_TAB", 3) // Self Study & Tasks tab
            }
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context, assignmentId, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val doneIntent = Intent(context, ReminderReceiver::class.java).apply {
            this.action = "ACTION_MARK_DONE"
            putExtra("assignment_id", assignmentId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context, assignmentId, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
            this.action = "ACTION_SNOOZE"
            putExtra("assignment_id", assignmentId)
            putExtra("title", titleExtra)
            putExtra("desc", descExtra)
            putExtra("interconnections", interconnections)
            putExtra("type", typeExtra)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, assignmentId + 10000, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(finalDesc)
            .setBigContentTitle(finalTitle)
            .setSummaryText(if (typeExtra.startsWith("class")) "Class Alert" else "Deadline Alert")

        val builder = NotificationCompat.Builder(context, "scholar_sync_channel")
            .setSmallIcon(lumia.tracker.util.NotificationHelper.getSmallIcon())
            .setContentTitle(finalTitle)
            .setContentText(finalDesc)
            .setStyle(bigTextStyle)
            .setColor(lumia.tracker.util.NotificationHelper.getColor(context))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(mainPendingIntent)
            .setAutoCancel(true)
            .setGroup("assignments_group")
            
        if (typeExtra == "assignment" || typeExtra == "task") {
            builder.addAction(android.R.drawable.ic_menu_edit, if (formalTone) "Mark Done" else "I Did It!", donePendingIntent)
            builder.addAction(android.R.drawable.ic_popup_sync, "Snooze 15m", snoozePendingIntent)
        }

        val notification = builder.build()
            
        notificationManager.notify(if (assignmentId != -1) assignmentId else System.currentTimeMillis().toInt(), notification)
    }
}

object ReminderScheduler {
    fun cancelReminder(context: Context, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Error cancelling reminder $id", e)
        }
    }

    fun scheduleReminder(context: Context, assignmentId: Int, title: String, desc: String, interconnections: String, timestamp: Long, type: String = "assignment", courseId: Int? = null, subjectId: Int? = null) {
        // Remind 1 hour before due date
        val triggerTime = timestamp - (1000 * 60 * 60)
        
        // Ensure we don't schedule in the past
        if (triggerTime > System.currentTimeMillis()) {
            scheduleReminderExact(context, assignmentId, title, desc, interconnections, triggerTime, type, courseId, subjectId)
        }
    }

    fun scheduleClassReminder(context: Context, classId: Int, title: String, desc: String, timestamp: Long, type: String = "class_start", courseId: Int? = null) {
        val profMgr = lumia.tracker.data.ProfileManager(context)
        val prefs = profMgr.getProfilePrefs()
        if (!prefs.getBoolean("notif_enable_classes", true)) return
        if (timestamp > System.currentTimeMillis()) {
            scheduleReminderExact(context, classId + (if(type == "class_start") 50000 else 60000), title, desc, "", timestamp, type, courseId = courseId)
        }
    }

    fun scheduleReminderExact(context: Context, assignmentId: Int, title: String, desc: String, interconnections: String, triggerTime: Long, type: String = "assignment", courseId: Int? = null, subjectId: Int? = null) {
        if (triggerTime <= System.currentTimeMillis()) {
            Log.d("ReminderScheduler", "Skipping alarm scheduling in the past: $title at $triggerTime")
            return
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("assignment_id", assignmentId)
            putExtra("title", title)
            putExtra("desc", desc)
            putExtra("interconnections", interconnections)
            putExtra("type", type)
            if (courseId != null) putExtra("courseId", courseId)
            if (subjectId != null) putExtra("subjectId", subjectId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            assignmentId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        try {
            if (canScheduleExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                Log.d("ReminderScheduler", "Scheduled exact reminder for $title at $triggerTime")
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                Log.d("ReminderScheduler", "Scheduled inexact reminder fallback for $title (exact alarm not allowed)")
            }
        } catch (e: SecurityException) {
            Log.e("ReminderScheduler", "Exact alarm SecurityException, falling back to inexact.", e)
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } catch (ex: java.lang.Exception) {
                Log.e("ReminderScheduler", "Failed to schedule fallback inexact alarm", ex)
            }
        }
    }

    fun rescheduleAllAlarms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val profMgr = lumia.tracker.data.ProfileManager(context)
                val prefs = profMgr.getProfilePrefs()
                
                val enableDeadlines = prefs.getBoolean("notif_enable_deadlines", true)
                val enableClasses = prefs.getBoolean("notif_enable_classes", true)
                
                val now = System.currentTimeMillis()
                
                // 1. Assignments and Tasks
                if (enableDeadlines) {
                    val assignments = db.scholarDao().exportAllAssignments()
                    for (assignment in assignments) {
                        if (!assignment.isCompleted) {
                            val alarmTime = assignment.dueDateMillis - (1000 * 60 * 60)
                            if (alarmTime > now) {
                                var interconnections = "Course: " + (db.scholarDao().exportAllCourses().find { it.id == assignment.courseId }?.name ?: "Unknown")
                                if (assignment.tags.isNotBlank()) interconnections += ", Tags: ${assignment.tags}"
                                scheduleReminderExact(
                                    context = context,
                                    assignmentId = assignment.id,
                                    title = assignment.title,
                                    desc = assignment.description,
                                    interconnections = interconnections,
                                    triggerTime = alarmTime,
                                    type = "assignment",
                                    courseId = assignment.courseId,
                                    subjectId = assignment.subjectId
                                )
                            } else {
                                cancelReminder(context, assignment.id)
                                cancelReminder(context, assignment.id + 10000)
                            }
                        } else {
                            cancelReminder(context, assignment.id)
                            cancelReminder(context, assignment.id + 10000)
                        }
                    }
                    
                    val tasks = db.scholarDao().exportAllTasks()
                    for (task in tasks) {
                        if (!task.isCompleted && task.dueDateMillis != null) {
                            val alarmTime = task.dueDateMillis - (1000 * 60 * 60)
                            if (alarmTime > now) {
                                val links = mutableListOf<String>()
                                if (task.subjectId != null) links.add("Subject")
                                if (task.courseId != null) links.add("Course")
                                if (task.assignmentId != null) links.add("Assignment")
                                if (task.tags.isNotBlank()) links.add("Tags: ${task.tags}")
                                scheduleReminderExact(
                                    context = context,
                                    assignmentId = task.id + 20000,
                                    title = "Task: ${task.title}",
                                    desc = task.description,
                                    interconnections = links.joinToString(", "),
                                    triggerTime = alarmTime,
                                    type = "task",
                                    courseId = task.courseId,
                                    subjectId = task.subjectId
                                )
                            } else {
                                cancelReminder(context, task.id + 20000)
                                cancelReminder(context, task.id + 30000)
                            }
                        } else {
                            cancelReminder(context, task.id + 20000)
                            cancelReminder(context, task.id + 30000)
                        }
                    }
                } else {
                    val assignments = db.scholarDao().exportAllAssignments()
                    for (assignment in assignments) {
                        cancelReminder(context, assignment.id)
                        cancelReminder(context, assignment.id + 10000)
                    }
                    val tasks = db.scholarDao().exportAllTasks()
                    for (task in tasks) {
                        cancelReminder(context, task.id + 20000)
                        cancelReminder(context, task.id + 30000)
                    }
                }
                
                // 2. Classes
                if (enableClasses) {
                    val calendar = java.util.Calendar.getInstance()
                    val currentDayOfWeekStr = java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault()).format(calendar.time)
                    val courses = db.scholarDao().exportAllCourses()
                    val todaysCourses = courses.filter {
                        it.scheduleDays.contains(currentDayOfWeekStr, ignoreCase = true)
                    }
                    
                    for (course in courses) {
                        if (!todaysCourses.contains(course)) {
                            cancelReminder(context, course.id + 50000)
                            cancelReminder(context, course.id + 60000)
                        }
                    }
                    
                    todaysCourses.forEach { course ->
                        try {
                            if (course.scheduleStartTime.isNotBlank()) {
                                val sdfTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                                val startCal = java.util.Calendar.getInstance()
                                val parsedStart = sdfTime.parse(course.scheduleStartTime.uppercase())
                                if (parsedStart != null) {
                                    val timeCal = java.util.Calendar.getInstance()
                                    timeCal.time = parsedStart
                                    startCal.set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY))
                                    startCal.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE))
                                    startCal.set(java.util.Calendar.SECOND, 0)
                                    val triggerTime = startCal.timeInMillis - (10 * 60 * 1000)
                                    if (triggerTime > now) {
                                        scheduleClassReminder(
                                            context, course.id, course.name, "Starts at ${course.scheduleStartTime}",
                                            triggerTime, "class_start", courseId = course.id
                                        )
                                    } else {
                                        cancelReminder(context, course.id + 50000)
                                    }
                                }
                            } else {
                                cancelReminder(context, course.id + 50000)
                            }
                            if (course.scheduleEndTime.isNotBlank()) {
                                val sdfTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                                val endCal = java.util.Calendar.getInstance()
                                val parsedEnd = sdfTime.parse(course.scheduleEndTime.uppercase())
                                if (parsedEnd != null) {
                                    val timeCal = java.util.Calendar.getInstance()
                                    timeCal.time = parsedEnd
                                    endCal.set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY))
                                    endCal.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE))
                                    endCal.set(java.util.Calendar.SECOND, 0)
                                    val triggerTime = endCal.timeInMillis
                                    if (triggerTime > now) {
                                        scheduleClassReminder(
                                            context, course.id, course.name, "Class finished. Don't forget to mark your attendance!",
                                            triggerTime, "class_end", courseId = course.id
                                        )
                                    } else {
                                        cancelReminder(context, course.id + 60000)
                                    }
                                }
                            } else {
                                cancelReminder(context, course.id + 60000)
                            }
                        } catch (e: Exception) {
                            Log.e("ReminderScheduler", "Error parsing time for course ${course.name}", e)
                        }
                    }
                } else {
                    val courses = db.scholarDao().exportAllCourses()
                    for (course in courses) {
                        cancelReminder(context, course.id + 50000)
                        cancelReminder(context, course.id + 60000)
                    }
                }
            } catch (e: Exception) {
                Log.e("ReminderScheduler", "Error in rescheduleAllAlarms", e)
            }
        }
    }
}
