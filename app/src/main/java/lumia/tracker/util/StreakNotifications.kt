package lumia.tracker.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object StreakNotifications {
    val motivational = listOf(
        "You're doing great! Keep up the good work.",
        "Another step forward. Amazing job!",
        "Consistency is key, and you're unlocking it.",
        "Awesome! You've crushed today's goals.",
        "Your dedication is inspiring. Keep going!",
        "Small steps every day lead to big results.",
        "You are unstoppable! Great work today.",
        "Look at you go! The streak continues.",
        "You're building a beautiful habit.",
        "One more day closer to your dreams.",
        "Excellent effort today. Rest well!",
        "You've earned this victory. Be proud!",
        "Setting the bar higher every single day.",
        "Progress, not perfection. And you made progress!",
        "You are on fire! Keep the momentum alive.",
        "Fantastic work! You're really on a roll.",
        "It's paying off. Keep your eyes on the prize.",
        "Consistency looks incredibly good on you.",
        "Believe in yourself. Today proves you can do it.",
        "Another day, another win. Outstanding!",
        "Building momentum, day by day. Great job!",
        "Your hard work is stacking up to greatness.",
        "The journey is long, but your stride is strong.",
        "Be proud of showing up today!",
        "Every effort counts. You're doing amazing!",
        "You're creating an awesome streak right now.",
        "Good habits are forming. Excellent!",
        "Keep that flame burning bright!",
        "Focus and dedication win the race. You got this.",
        "The hardest part is starting. You finished!",
        "Your future self is thanking you today.",
        "Today's effort is tomorrow's success.",
        "Keep climbing, you are doing wonderfully.",
        "Stellar work today! Enjoy the feeling of accomplishment.",
        "It’s all about consistency, and you nailed it today!",
        "Way to go! You’re getting better every day.",
        "Don't stop now, you're doing phenomenally well!",
        "Awesome dedication. Keep it up!",
        "You turned your goals into reality today.",
        "Brilliant work! You should be very proud."
    )

    val aggressive = listOf(
        "Do not break the streak. I am watching you.",
        "Good job. Now do it again tomorrow. No excuses.",
        "You did what was expected. Nothing more. Keep going.",
        "You survived today. Tomorrow will be harder.",
        "Keep pushing, or someone else will take your spot.",
        "The streak lives. Do not let it die on your watch.",
        "You met the minimum requirement. Barely.",
        "Don't get comfortable. The real work is yet to come.",
        "Complacency is the enemy. Stay sharp.",
        "You did it today. Let's see if you can do it tomorrow.",
        "Excuses don't build streaks. Actions do. Good action.",
        "If you stop now, all of this was for nothing.",
        "Keep the streak alive, or face the consequences of failure.",
        "Victory today. War tomorrow. Prepare yourself.",
        "Do you want to be average? Then keep doing this. If you want to be great, do more.",
        "Streak extended. But you can push harder.",
        "I'll tolerate today's effort. But raise the bar tomorrow.",
        "Don't be weak. Keep the flame alive.",
        "Your competitors are resting. You shouldn't be.",
        "Pain is temporary. The streak is forever.",
        "You showed up. Now show out tomorrow.",
        "Only the disciplined survive. Stay disciplined.",
        "Do not dare break this chain.",
        "You are building a weapon. Keep forging.",
        "No days off in the pursuit of greatness.",
        "Mediocrity is knocking. Keep the door closed.",
        "You call that hard work? Do better tomorrow.",
        "The fire is burning, don't let it go out.",
        "Today was just a warm-up for tomorrow.",
        "Don't look back. Just keep moving forward, aggressively.",
        "The streak demands blood, sweat, and focus.",
        "Sleep when the streak is safe. Which is never.",
        "You want success? This is the price. Pay up again tomorrow.",
        "Keep your foot on the gas. No braking allowed.",
        "The streak is your master now. Obey it.",
        "Good. Now go harder.",
        "Prove you're not a fluke. Do it again.",
        "Stop celebrating. The job isn't done.",
        "If you quit, you're just like everyone else.",
        "Maintain the streak. At all costs."
    )

    val preservationMotivational = listOf(
        "Don't let your streak slip away! Complete your daily goals tonight.",
        "Keep your momentum alive! Spend a few minutes studying tonight to save your streak.",
        "Your streak is on the line! Finish today's targets before midnight.",
        "A few minutes of focus tonight will protect your hard-earned streak.",
        "Stay dedicated! Take a quick study session to keep your streak glowing.",
        "Almost there! Check off today's tasks and secure your streak.",
        "Consistency is power. Keep the fire burning before the day ends!"
    )

    val preservationAggressive = listOf(
        "Your streak is about to die. Get to work before midnight.",
        "Clock is ticking. Don't lose your streak to laziness.",
        "Midnight is approaching. Save your streak or start over from zero tomorrow.",
        "You haven't finished your streak requirements today. Fix it now.",
        "Excuses won't save your streak tonight. Put the work in.",
        "Are you really going to let your flame burn out today? Open the app.",
        "Discipline is doing it even when you don't feel like it. Keep the streak alive."
    )

    fun getCompletionMessage(tone: String): String {
        return if (tone == "Motivational") motivational.random() else aggressive.random()
    }

    fun getPreservationMessage(tone: String): String {
        return if (tone == "Motivational") preservationMotivational.random() else preservationAggressive.random()
    }

    fun scheduleEveningPreservationReminder(context: Context) {
        try {
            val profMgr = lumia.tracker.data.ProfileManager(context)
            val prefs = profMgr.getProfilePrefs()

            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 20)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            var triggerTime = calendar.timeInMillis
            if (triggerTime <= now) {
                triggerTime += 86400000L
            }

            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val alreadySentToday = prefs.getBoolean("streak_preservation_notif_sent_$todayStr", false)
            val isCompleteToday = prefs.getString("streak_status_$todayStr", "none") != "none"
            if (alreadySentToday || isCompleteToday) return

            ReminderScheduler.scheduleReminderExact(
                context = context,
                assignmentId = 77777,
                title = "Protect Your Streak! 🔥",
                desc = "Don't lose your streak! Complete your goals tonight.",
                interconnections = "",
                triggerTime = triggerTime,
                type = "streak_preservation"
            )
        } catch (e: Exception) {
            android.util.Log.e("StreakNotifications", "Error scheduling evening preservation reminder", e)
        }
    }

    fun sendPreservationNotificationIfDue(context: Context) {
        try {
            val profMgr = lumia.tracker.data.ProfileManager(context)
            val prefs = profMgr.getProfilePrefs()
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val statusToday = prefs.getString("streak_status_$todayStr", "none")
            val alreadySent = prefs.getBoolean("streak_preservation_notif_sent_$todayStr", false)

            if (statusToday != "none" || alreadySent) return

            val tone = prefs.getString("streak_notif_tone", "Motivational") ?: "Motivational"
            val message = getPreservationMessage(tone)
            val colorHex = prefs.getString("streak_progress_color", "#FF5722") ?: "#FF5722"
            val iconRes = NotificationHelper.getSmallIcon()
            val notifColor = try {
                if (colorHex == "Theme") NotificationHelper.getColor(context)
                else android.graphics.Color.parseColor(colorHex)
            } catch (e: Exception) {
                NotificationHelper.getColor(context)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "scholar_streak_channel",
                    "Streak & Milestone Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Daily streak continuity and milestone notifications"
                    enableLights(true)
                    lightColor = notifColor
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, lumia.tracker.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("OPEN_SCREEN", "settings/streaks")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                77777,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, "scholar_streak_channel")
                .setSmallIcon(iconRes)
                .setContentTitle("Protect Your Streak! 🔥")
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setColor(notifColor)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(77777, notification)
            prefs.edit().putBoolean("streak_preservation_notif_sent_$todayStr", true).apply()
        } catch (e: Exception) {
            android.util.Log.e("StreakNotifications", "Error sending preservation notification", e)
        }
    }
}
