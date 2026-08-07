package lumia.tracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import lumia.tracker.MainActivity
import lumia.tracker.util.NotificationHelper

object PomodoroNotificationHelper {
    private var mediaPlayer: MediaPlayer? = null

    fun playAlarmSound(context: Context, isWorkEnd: Boolean) {
        stopAlarmSound()
        try {
            val soundUri = if (isWorkEnd) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, soundUri)
                setAudioStreamType(if (isWorkEnd) AudioManager.STREAM_ALARM else AudioManager.STREAM_NOTIFICATION)
                isLooping = isWorkEnd
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("PomodoroNotifHelper", "Error playing alarm sound", e)
            try {
                val toneG = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 2000)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun stopAlarmSound() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("PomodoroNotifHelper", "Error stopping alarm sound", e)
        }
    }

    fun buildNotification(
        context: Context,
        currentMode: PomodoroMode,
        time: Int,
        originalTime: Int,
        isPaused: Boolean,
        isAlarmActive: Boolean,
        sessionsCompleted: Int,
        periodSessions: Int
    ): Notification {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("pomodoro_service", "Pomodoro Foreground", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Ongoing Pomodoro Timer"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val minutes = time / 60
        val seconds = time % 60
        val timeStr = String.format("%02d:%02d", minutes, seconds)
        val title = when (currentMode) {
            PomodoroMode.WORK -> "Focusing (Session ${sessionsCompleted + 1}/$periodSessions)"
            PomodoroMode.SHORT_BREAK -> "Short Rest"
            PomodoroMode.LONG_BREAK -> "Long Rest (Period Complete!)"
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply { 
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP 
            putExtra("OPEN_POMODORO", true)
        }
        val mainPending = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        if (isAlarmActive) {
            val stopAlarmIntent = Intent(context, PomodoroActionReceiver::class.java).apply { action = "STOP_ALARM" }
            val stopAlarmPending = PendingIntent.getBroadcast(context, 3, stopAlarmIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            
            return NotificationCompat.Builder(context, "pomodoro_service")
                .setSmallIcon(NotificationHelper.getSmallIcon())
                .setContentTitle(if (currentMode == PomodoroMode.WORK) "Rest Break Finished!" else "Focus Session Finished!")
                .setContentText("Alarm active! Tap to stop sound.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(mainPending)
                .setOngoing(true)
                .setColor(NotificationHelper.getColor(context))
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Alarm", stopAlarmPending)
                .build()
        }

        val stopIntent = Intent(context, PomodoroActionReceiver::class.java).apply { action = "STOP" }
        val stopPending = PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val pauseIntent = Intent(context, PomodoroActionReceiver::class.java).apply { action = "PAUSE_RESUME" }
        val pausePending = PendingIntent.getBroadcast(context, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val skipIntent = Intent(context, PomodoroActionReceiver::class.java).apply { action = "SKIP" }
        val skipPending = PendingIntent.getBroadcast(context, 2, skipIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val progressMax = originalTime
        val progressNow = originalTime - time
        
        val builder = NotificationCompat.Builder(context, "pomodoro_service")
            .setSmallIcon(NotificationHelper.getSmallIcon())
            .setContentTitle(title)
            .setContentText("Time remaining: $timeStr" + if (isPaused) " (PAUSED)" else "")
            .setProgress(progressMax, progressNow, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(mainPending)
            .setOngoing(true)
            .setColor(NotificationHelper.getColor(context))
            .setUsesChronometer(!isPaused)
            .setWhen(System.currentTimeMillis() + time * 1000L)
            
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setChronometerCountDown(true)
        }
            
        return builder.addAction(if (isPaused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause, if (isPaused) "Resume" else "Pause", pausePending)
            .addAction(android.R.drawable.ic_media_next, "Skip", skipPending)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Exit", stopPending)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2))
            .setOnlyAlertOnce(true)
            .build()
    }
}
