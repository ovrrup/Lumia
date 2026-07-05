package lumia.tracker.util

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object WidgetUpdateHelper {

    fun updateAllWidgets(context: Context) {
        updateCalendarWidget(context)
        updateTasksWidget(context)
        updatePomodoroWidget(context)
    }

    fun updateCalendarWidget(context: Context) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ScholarCalendarWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, ScholarCalendarWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateTasksWidget(context: Context) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ScholarTasksWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, ScholarTasksWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updatePomodoroWidget(context: Context) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ScholarPomodoroWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, ScholarPomodoroWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
