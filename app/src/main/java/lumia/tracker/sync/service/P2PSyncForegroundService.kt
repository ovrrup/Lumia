package lumia.tracker.sync.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import lumia.tracker.MainActivity
import lumia.tracker.R
import lumia.tracker.sync.P2PSyncEngine

/**
 * Android Foreground Service maintaining direct device sync
 * and network keep-alive checks in the background.
 */
class P2PSyncForegroundService : Service() {

    companion object {
        private const val TAG = "P2PSyncService"
        const val CHANNEL_ID = "lumia_p2p_sync_channel"
        const val NOTIFICATION_ID = 51820
        const val ACTION_START = "lumia.tracker.sync.ACTION_START"
        const val ACTION_STOP = "lumia.tracker.sync.ACTION_STOP"

        fun start(context: Context) {
            val intent = Intent(context, P2PSyncForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, P2PSyncForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var telemetryCollectJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                Log.i(TAG, "Stopping Lumia Device Sync service...")
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                Log.i(TAG, "Starting Lumia Device Sync service...")
                startForeground(NOTIFICATION_ID, buildNotification("Active • Connecting with nearby devices"))
                startTelemetryMonitor()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        telemetryCollectJob?.cancel()
        serviceScope.cancel()
        releaseWakeLock()
        Log.i(TAG, "Lumia Device Sync service stopped")
    }

    private fun startTelemetryMonitor() {
        telemetryCollectJob?.cancel()
        telemetryCollectJob = serviceScope.launch {
            val engine = P2PSyncEngine.getInstance(applicationContext)
            engine.transport.telemetry.collect { tele ->
                val stateText = when (tele.connectionState) {
                    lumia.tracker.sync.transport.TransportConnectionState.CONNECTED -> "Connected • Syncing notes and tasks"
                    lumia.tracker.sync.transport.TransportConnectionState.KEEP_ALIVE_ACTIVE -> "Connected • Ready to sync"
                    lumia.tracker.sync.transport.TransportConnectionState.CGNAT_DISCOVERED -> "Active • Ready to connect"
                    lumia.tracker.sync.transport.TransportConnectionState.GATHERING_ICE -> "Active • Discovering network..."
                    lumia.tracker.sync.transport.TransportConnectionState.CONNECTING -> "Active • Connecting with nearby devices"
                    else -> "Active • Ready to sync"
                }
                updateNotification(stateText)
            }
        }
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "Lumia::P2PSyncKeepAliveWakeLock"
            ).apply {
                setReferenceCounted(false)
                acquire(12 * 60 * 60 * 1000L) // 12 hour max safety limit
            }
            Log.d(TAG, "Acquired background lock for device sync")
        } catch (e: Exception) {
            Log.w(TAG, "Could not acquire WakeLock: ${e.message}")
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
            wakeLock = null
            Log.d(TAG, "Released background lock")
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing background lock: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Lumia Device Sync",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Syncs notes and tasks directly between your devices in the background"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("NAVIGATE_TO", "settings/sync")
        }
        val pendingTapIntent = PendingIntent.getActivity(
            this,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val stopIntent = Intent(this, P2PSyncForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStopIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Lumia Device Sync")
            .setContentText(statusText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingTapIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Sync", pendingStopIntent)
            .build()
    }

    private fun updateNotification(statusText: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(statusText))
    }
}
