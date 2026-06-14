package ovrrup.lumia.util

import android.content.Context

object NotificationHelper {
    fun getSmallIcon(): Int {
        return android.R.drawable.ic_lock_idle_alarm
    }
    
    fun getColor(context: Context): Int {
        return 0xFF3197D6.toInt()
    }
}
