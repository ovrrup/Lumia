package lumia.tracker.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.delay
import lumia.tracker.service.PomodoroService
import lumia.tracker.service.AodAccessibilityService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("StaticFieldLeak")
object TrueAodManager {
    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    class OverlayLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val mViewModelStore = ViewModelStore()
        private val savedStateRegistryController = SavedStateRegistryController.create(this)

        init {
            lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
        }

        fun onCreate() {
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }

        fun onStart() {
            lifecycleRegistry.currentState = Lifecycle.State.STARTED
        }

        fun onResume() {
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        }

        fun onDestroy() {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
            mViewModelStore.clear()
        }

        override val lifecycle: Lifecycle = lifecycleRegistry
        override val viewModelStore: ViewModelStore = mViewModelStore
        override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry
    }

    fun isOverlayShowing(): Boolean {
        return composeView != null
    }

    @SuppressLint("ClickableViewAccessibility")
    fun showAodOverlay(
        context: Context,
        useAccessibility: Boolean,
        dimnessLevel: Float, // e.g., 0.95f
        sensitivity: String, // "highest", "medium", "secure", "motion"
        motionSensitivity: Float = 1.2f,
        lockTimeoutSeconds: Int = 0, // 30 seconds
        burnInShiftIntervalSeconds: Int = 10,
        onExit: () -> Unit
    ) {
        if (composeView != null) return

        val overlayContext = if (useAccessibility) {
            lumia.tracker.service.AodAccessibilityService.instance ?: context
        } else {
            context
        }

        val wm = overlayContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager = wm

        val localLifecycle = OverlayLifecycleOwner()
        localLifecycle.onCreate()
        localLifecycle.onStart()
        localLifecycle.onResume()
        lifecycleOwner = localLifecycle

        val layoutParams = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            type = if (useAccessibility && overlayContext is android.accessibilityservice.AccessibilityService) {
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                }
            }
            flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            screenBrightness = 0.005f // force minimum hardware backlight
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.FILL
        }

        val view = ComposeView(overlayContext).apply {
            setViewTreeLifecycleOwner(localLifecycle)
            setViewTreeViewModelStoreOwner(localLifecycle)
            setViewTreeSavedStateRegistryOwner(localLifecycle)
            
            setContent {
                TrueAodOverlayUi(
                    dimnessLevel = dimnessLevel,
                    sensitivity = sensitivity,
                    motionSensitivity = motionSensitivity,
                    lockTimeoutSeconds = lockTimeoutSeconds,
                    burnInShiftIntervalSeconds = burnInShiftIntervalSeconds,
                    onExitRequest = {
                        dismissAodOverlay()
                        onExit()
                    }
                )
            }
        }

        try {
            wm.addView(view, layoutParams)
            composeView = view
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dismissAodOverlay() {
        val wm = windowManager ?: return
        val view = composeView ?: return
        try {
            wm.removeView(view)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        composeView = null
        windowManager = null
        lifecycleOwner?.onDestroy()
        lifecycleOwner = null
    }
}

@Composable
fun TrueAodOverlayUi(
    dimnessLevel: Float,
    sensitivity: String,
    motionSensitivity: Float = 1.2f,
    lockTimeoutSeconds: Int,
    burnInShiftIntervalSeconds: Int = 10,
    onExitRequest: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val serviceState by PomodoroService.state.collectAsState()
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()) }
    var currentTimeStr by remember { mutableStateOf(timeFormat.format(Date())) }
    var currentDateStr by remember { mutableStateOf(dateFormat.format(Date())) }

    // Tick real time & date
    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTimeStr = timeFormat.format(now)
            currentDateStr = dateFormat.format(now)
            delay(1000)
        }
    }

    // Battery status monitor
    var batteryLevel by remember { mutableIntStateOf(100) }
    var isCharging by remember { mutableStateOf(false) }
    DisposableEffect(context) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: android.content.Intent?) {
                val level = intent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
                val status = intent?.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1) ?: -1
                isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                             status == android.os.BatteryManager.BATTERY_STATUS_FULL
                batteryLevel = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
            }
        }
        val filter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
        val initialIntent = context.registerReceiver(receiver, filter)
        if (initialIntent != null) {
            val level = initialIntent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
            val scale = initialIntent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
            val status = initialIntent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)
            isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                         status == android.os.BatteryManager.BATTERY_STATUS_FULL
            batteryLevel = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
        }
        onDispose {
            try { context.unregisterReceiver(receiver) } catch (e: Exception) {}
        }
    }

    // Accessible Safety Fallback Screen-Lock Timer
    LaunchedEffect(lockTimeoutSeconds) {
        if (lockTimeoutSeconds > 0) {
            delay(lockTimeoutSeconds * 1000L)
            if (AodAccessibilityService.isServiceEnabled(context)) {
                AodAccessibilityService.lockScreen()
            }
        }
    }

    // Touch holding stats for Secure Hold Sensitivity
    var isHolding by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            val start = System.currentTimeMillis()
            while (isHolding && holdProgress < 1.0f) {
                val elapsed = System.currentTimeMillis() - start
                holdProgress = (elapsed / 1000f).coerceIn(0f, 1f)
                if (holdProgress >= 1.0f) {
                    onExitRequest()
                    break
                }
                delay(16)
            }
        } else {
            holdProgress = 0f
        }
    }

    // Modifiers matching sensitivity mode
    val sensitivityModifier = remember(sensitivity) {
        when (sensitivity) {
            "motion", "highest" -> {
                Modifier.pointerInput(Unit) {
                    detectTapGestures(onTap = { onExitRequest() })
                }
            }
            "medium" -> {
                Modifier.pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = { onExitRequest() })
                }
            }
            else -> { // secure (hold for 1s)
                Modifier.pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitFirstDown()
                            isHolding = true
                            waitForUpOrCancellation()
                            isHolding = false
                        }
                    }
                }
            }
        }
    }

    if (sensitivity == "motion" && motionSensitivity > 0f) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
        val linearSensor = remember { sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_LINEAR_ACCELERATION) }
        val accelSensor = remember { if (linearSensor == null) sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER) else null }
        
        DisposableEffect(sensorManager, linearSensor, accelSensor, motionSensitivity) {
            var lastX = 0f
            var lastY = 0f
            var lastZ = 0f
            var isFirstValue = true
            
            val listener = object : android.hardware.SensorEventListener {
                override fun onSensorChanged(event: android.hardware.SensorEvent) {
                    if (event.sensor.type == android.hardware.Sensor.TYPE_LINEAR_ACCELERATION) {
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]
                        val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                        if (acceleration > motionSensitivity) {
                            onExitRequest()
                        }
                    } else if (event.sensor.type == android.hardware.Sensor.TYPE_ACCELEROMETER) {
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]
                        if (!isFirstValue) {
                            val dx = x - lastX
                            val dy = y - lastY
                            val dz = z - lastZ
                            val delta = Math.sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
                            val accelThreshold = motionSensitivity * 0.375f
                            if (delta > accelThreshold) {
                                onExitRequest()
                            }
                        }
                        lastX = x
                        lastY = y
                        lastZ = z
                        isFirstValue = false
                    }
                }
                override fun onAccuracyChanged(s: android.hardware.Sensor?, accuracy: Int) {}
            }
            
            if (linearSensor != null) {
                sensorManager.registerListener(listener, linearSensor, android.hardware.SensorManager.SENSOR_DELAY_UI)
            } else if (accelSensor != null) {
                sensorManager.registerListener(listener, accelSensor, android.hardware.SensorManager.SENSOR_DELAY_UI)
            }
            
            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    // Dynamic Pixel Burn-in Orbital Shifter
    val shiftSpeed = if (burnInShiftIntervalSeconds > 0) burnInShiftIntervalSeconds else 10
    val burnInOffset = remember(serviceState.timeLeft, shiftSpeed) {
        val tick = ((serviceState.timeLeft / shiftSpeed) % 8)
        val x = when (tick) {
            0 -> 0.dp; 1 -> 4.dp; 2 -> 0.dp; 3 -> (-4).dp
            4 -> 3.dp; 5 -> (-3).dp; 6 -> 5.dp; else -> (-5).dp
        }
        val y = when (tick) {
            0 -> 0.dp; 1 -> (-4).dp; 2 -> 4.dp; 3 -> 0.dp
            4 -> (-3).dp; 5 -> 3.dp; 6 -> (-5).dp; else -> 5.dp
        }
        Pair(x, y)
    }

    // Dimness scaling
    val textAlpha = (1.0f - dimnessLevel).coerceIn(0.02f, 0.40f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .then(sensitivityModifier),
        contentAlignment = Alignment.Center
    ) {
        // Main Focus/Time Center Layout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .offset(x = burnInOffset.first, y = burnInOffset.second)
        ) {
            // Live Battery & Real-time Date Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = currentDateStr,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = textAlpha * 1.5f)
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = textAlpha * 1.0f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = if (isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = textAlpha * 1.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "$batteryLevel%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = textAlpha * 1.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Real-time Clock
            Text(
                text = currentTimeStr,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 68.sp),
                fontWeight = FontWeight.ExtraLight,
                color = Color.White.copy(alpha = textAlpha * 1.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Pomodoro Countdown Clock
            val m = serviceState.timeLeft / 60
            val s = serviceState.timeLeft % 60
            Text(
                text = String.format("%02d:%02d", m, s),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp),
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = textAlpha * 2.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Session Mode Pill
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${if (serviceState.modeString == "WORK") "FOCUS" else serviceState.modeString.replace("_", " ")} • SESSION #${serviceState.sessionsCompleted + 1}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White.copy(alpha = textAlpha * 2.2f)
                    )

                    if (serviceState.isPaused) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "PAUSED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = textAlpha * 2.5f),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            val guideMessage = when (sensitivity) {
                "motion" -> "True AOD Active • Move Device or Tap Screen to Wake"
                "highest" -> "True AOD Active • Single Tap to Wake"
                "medium" -> "True AOD Active • Double Tap to Wake"
                else -> if (isHolding) "Unlocking screen..." else "True AOD Active • Touch and Hold to Wake"
            }

            Text(
                text = guideMessage,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal),
                color = Color.White.copy(alpha = textAlpha * 1.3f),
                textAlign = TextAlign.Center
            )

            // Dynamic progress bar when secure long-pressing to exit
            if (sensitivity == "secure" && isHolding) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(6.dp)
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(holdProgress)
                            .background(Color.White.copy(alpha = textAlpha * 3.0f), RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}
