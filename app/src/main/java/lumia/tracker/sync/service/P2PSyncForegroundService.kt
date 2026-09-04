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
 * Android Foreground Service maintaining persistent WebRTC DataChannel connectivity,
 * NAT traversal bindings, and 20-second UDP keep-alive heartbeats in the background.
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
                Log.i(TAG, "Stopping P2P Sync Foreground Service...")
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                Log.i(TAG, "Starting P2P Sync Foreground Service with active 20s UDP keep-alives...")
                startForeground(NOTIFICATION_ID, buildNotification("Sync Active • 20s Keep-Alive Ticker Running"))
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
        Log.i(TAG, "P2P Sync Foreground Service destroyed")
    }

    private fun startTelemetryMonitor() {
        telemetryCollectJob?.cancel()
        telemetryCollectJob = serviceScope.launch {
            val engine = P2PSyncEngine.getInstance(applicationContext)
            engine.transport.telemetry.collect { tele ->
                val stateText = when (tele.connectionState) {
                    lumia.tracker.sync.transport.TransportConnectionState.CONNECTED -> "P2P Connected • E2EE Mesh Active"
                    lumia.tracker.sync.transport.TransportConnectionState.KEEP_ALIVE_ACTIVE -> "20s Keep-Alive Active • CGNAT Port Open"
                    lumia.tracker.sync.transport.TransportConnectionState.CGNAT_DISCOVERED -> "CGNAT Traversed • Ready to Pair"
                    lumia.tracker.sync.transport.TransportConnectionState.GATHERING_ICE -> "Gathering STUN/ICE Candidates..."
                    lumia.tracker.sync.transport.TransportConnectionState.CONNECTING -> "Handshake in Progress..."
                    else -> "P2P Engine Standby"
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
            Log.d(TAG, "Acquired partial WakeLock for 20s keep-alives")
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
            Log.d(TAG, "Released WakeLock")
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing WakeLock: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "P2P Live Sync Engine",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Maintains zero-trust P2P WebRTC DataChannel sync and 20s UDP keep-alives"
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
            .setContentTitle("Lumia Zero-Trust P2P Sync")
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
