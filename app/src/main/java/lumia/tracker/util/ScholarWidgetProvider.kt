package lumia.tracker.util

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import lumia.tracker.R
import lumia.tracker.data.AppDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.util.Calendar

class ScholarWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.scholar_widget)

        // 1. PendingIntent to open the main app
        val mainIntent = android.content.Intent(context, lumia.tracker.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainPendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_title, mainPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_subtitle, mainPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_items_container, mainPendingIntent)

        // 2. PendingIntent to open Pomodoro Screen directly
        val pomodoroIntent = android.content.Intent(context, lumia.tracker.MainActivity::class.java).apply {
            action = "ACTION_OPEN_POMODORO"
            putExtra("OPEN_POMODORO", true)
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pomodoroPendingIntent = android.app.PendingIntent.getActivity(
            context,
            1,
            pomodoroIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
         views.setOnClickPendingIntent(R.id.widget_pomodoro_button, pomodoroPendingIntent)

        // Get today's day of week
        val calendar = Calendar.getInstance()
        val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            Calendar.SUNDAY -> "Sunday"
            else -> "Monday"
        }

        views.setTextViewText(R.id.widget_subtitle, "$dayOfWeek schedule")

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(context)
                val allCourses = db.scholarDao().exportAllCourses()
                val todayCourses = allCourses.filter { course ->
                    course.scheduleDays.split(",").any { it.trim().equals(dayOfWeek, ignoreCase = true) }
                }.sortedBy { it.scheduleStartTime }

                if (todayCourses.isEmpty()) {
                    views.setViewVisibility(R.id.widget_no_classes, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_item_1, View.GONE)
                    views.setViewVisibility(R.id.widget_item_2, View.GONE)
                    views.setViewVisibility(R.id.widget_item_3, View.GONE)
                } else {
                    views.setViewVisibility(R.id.widget_no_classes, View.GONE)
                    
                    // Render up to 3 courses
                    for (i in 0..2) {
                        val viewId = when (i) {
                            0 -> R.id.widget_item_1
                            1 -> R.id.widget_item_2
                            else -> R.id.widget_item_3
                        }
                        
                        if (i < todayCourses.size) {
                            val course = todayCourses[i]
                            val label = if (course.code.isNotBlank()) "[${course.code}] ${course.name}" else course.name
                            val time = if (course.scheduleStartTime.isNotBlank() && course.scheduleEndTime.isNotBlank()) {
                                " (${course.scheduleStartTime} - ${course.scheduleEndTime})"
                            } else {
                                if (course.schedule.isNotBlank()) " (${course.schedule})" else ""
                            }
                            views.setViewVisibility(viewId, View.VISIBLE)
                            views.setTextViewText(viewId, "• $label$time")
                        } else {
                            views.setViewVisibility(viewId, View.GONE)
                        }
                    }
                }
                
                appWidgetManager.updateAppWidget(widgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
