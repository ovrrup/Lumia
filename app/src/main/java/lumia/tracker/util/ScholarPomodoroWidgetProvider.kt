package lumia.tracker.util

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.widget.RemoteViews
import lumia.tracker.R
import lumia.tracker.service.PomodoroService

class ScholarPomodoroWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_pomodoro)
        val state = PomodoroService.state.value

        // Direct Open App action
        val openIntent = Intent(context, lumia.tracker.MainActivity::class.java).apply {
            action = "ACTION_OPEN_POMODORO"
            putExtra("OPEN_POMODORO", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            101,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_app, openPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_title, openPendingIntent)

        // Control Toggle action
        val actionIntent = Intent(context, lumia.tracker.service.PomodoroActionReceiver::class.java).apply {
            if (state.isRunning) {
                action = "PAUSE_RESUME"
            } else {
                action = "START"
                // Pass standard 25 / 5 / 15 durations
                putExtra("workDuration", 25 * 60)
                putExtra("shortBreakDuration", 5 * 60)
                putExtra("longBreakDuration", 15 * 60)
                putExtra("periodSessions", 4)
            }
        }
        val actionPendingIntent = PendingIntent.getBroadcast(
            context,
            102,
            actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_mode, actionPendingIntent)

        // Show info
        if (state.isRunning) {
            val minutes = state.timeLeft / 60
            val seconds = state.timeLeft % 60
            views.setTextViewText(R.id.widget_pomo_time, String.format("%02d:%02d", minutes, seconds))

            val modeName = when (state.modeString) {
                "WORK" -> "Focus Block (${state.sessionsCompleted + 1}/4)"
                "SHORT_BREAK" -> "Short Rest Break"
                "LONG_BREAK" -> "Long Rest Break"
                else -> "Focus Mode"
            }
            views.setTextViewText(R.id.widget_pomo_state, modeName + if (state.isPaused) " (PAUSED)" else "")
            views.setTextViewText(R.id.widget_btn_text_1, if (state.isPaused) "▶️ Resume" else "⏸️ Pause")
        } else {
            views.setTextViewText(R.id.widget_pomo_time, "25:00")
            views.setTextViewText(R.id.widget_pomo_state, "Ready to focus")
            views.setTextViewText(R.id.widget_btn_text_1, "▶️ Start")
        }

        appWidgetManager.updateAppWidget(widgetId, views)
    }
}
