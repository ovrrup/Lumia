package lumia.tracker.util

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.view.View
import android.widget.RemoteViews
import lumia.tracker.R
import lumia.tracker.data.AppDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

class ScholarTasksWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_tasks)

        // Opening MainActivity when clicked
        val mainIntent = Intent(context, lumia.tracker.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            20,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_title, mainPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_subtitle, mainPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_items_container, mainPendingIntent)

        views.setTextViewText(R.id.widget_subtitle, "Upcoming assignments & tasks")

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(context)
                val allTasks = db.scholarDao().exportAllTasks().filter { !it.isCompleted }
                val allAssignments = db.scholarDao().exportAllAssignments().filter { !it.isCompleted }

                // Combine them
                val items = mutableListOf<String>()
                allAssignments.forEach { ass ->
                    items.add("[Assig] ${ass.title}")
                }
                allTasks.forEach { task ->
                    items.add("[Task] ${task.title}")
                }

                if (items.isEmpty()) {
                    views.setViewVisibility(R.id.widget_no_tasks, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_task_item_1, View.GONE)
                    views.setViewVisibility(R.id.widget_task_item_2, View.GONE)
                    views.setViewVisibility(R.id.widget_task_item_3, View.GONE)
                } else {
                    views.setViewVisibility(R.id.widget_no_tasks, View.GONE)
                    
                    for (i in 0..2) {
                        val viewId = when (i) {
                            0 -> R.id.widget_task_item_1
                            1 -> R.id.widget_task_item_2
                            else -> R.id.widget_task_item_3
                        }
                        
                        if (i < items.size) {
                            views.setViewVisibility(viewId, View.VISIBLE)
                            views.setTextViewText(viewId, items[i])
                        } else {
                            views.setViewVisibility(viewId, View.GONE)
                        }
                    }
                }
                
                WidgetThemeHelper.applyTheme(context, views)
                appWidgetManager.updateAppWidget(widgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
