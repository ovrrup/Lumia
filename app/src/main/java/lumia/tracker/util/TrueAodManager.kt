package lumia.tracker.util

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.delay
import lumia.tracker.service.AodAccessibilityService
import lumia.tracker.service.PomodoroService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@SuppressLint("StaticFieldLeak")
object TrueAodManager {
    @Volatile
    private var windowManager: WindowManager? = null
    @Volatile
    private var composeView: ComposeView? = null
    @Volatile
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

    @Synchronized
    @SuppressLint("ClickableViewAccessibility")
    fun showAodOverlay(
        context: Context,
        useAccessibility: Boolean,
        dimnessLevel: Float,
        sensitivity: String,
        motionSensitivity: Float = 1.2f,
        lockTimeoutSeconds: Int = 0,
        burnInShiftIntervalSeconds: Int = 10,
        onExit: () -> Unit
    ) {
        if (composeView != null) return

        val overlayContext = if (useAccessibility) {
            AodAccessibilityService.instance ?: context
        } else {
            context
        }

        val wm = overlayContext.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return

        val localLifecycle = OverlayLifecycleOwner()
        localLifecycle.onCreate()
        localLifecycle.onStart()
        localLifecycle.onResume()

        val layoutParams = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            type = if (useAccessibility && overlayContext is android.accessibilityservice.AccessibilityService) {
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
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
            screenBrightness = 0.005f
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
            windowManager = wm
            lifecycleOwner = localLifecycle
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                localLifecycle.onDestroy()
            } catch (ignored: Exception) {}
            composeView = null
            windowManager = null
            lifecycleOwner = null
        }
    }

    @Synchronized
    fun dismissAodOverlay() {
        val view = composeView ?: return
        val wm = windowManager
        val lifecycle = lifecycleOwner

        composeView = null
        windowManager = null
        lifecycleOwner = null

        if (wm != null) {
            try {
                if (view.isAttachedToWindow) {
                    wm.removeViewImmediate(view)
                } else {
                    wm.removeView(view)
                }
            } catch (e: IllegalArgumentException) {
                // View was not attached
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        try {
            lifecycle?.onDestroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                             status == BatteryManager.BATTERY_STATUS_FULL
                batteryLevel = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialIntent = context.registerReceiver(receiver, filter)
        if (initialIntent != null) {
            val level = initialIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = initialIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status = initialIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                         status == BatteryManager.BATTERY_STATUS_FULL
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
    var holdProgress by remember { mutableFloatStateOf(0f) }

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
            else -> {
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

    // Sensor listeners: Proximity and Motion
    if (sensitivity == "motion") {
        val sensorManager = remember {
            context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        }

        DisposableEffect(sensorManager, motionSensitivity) {
            val sm = sensorManager ?: return@DisposableEffect onDispose {}

            val linearSensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
            val accelSensor = if (linearSensor == null) sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) else null
            val proximitySensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY)

            var lastX = 0f
            var lastY = 0f
            var lastZ = 0f
            var isFirstAccelValue = true
            var initialProximityRecorded = false
            var lastProximityNear = false

            val thresholdSq = motionSensitivity * motionSensitivity
            val accelThreshold = motionSensitivity * 0.375f
            val accelThresholdSq = accelThreshold * accelThreshold

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    when (event.sensor.type) {
                        Sensor.TYPE_LINEAR_ACCELERATION -> {
                            if (motionSensitivity > 0f) {
                                val x = event.values[0]
                                val y = event.values[1]
                                val z = event.values[2]
                                val magSq = x * x + y * y + z * z
                                if (magSq > thresholdSq) {
                                    onExitRequest()
                                }
                            }
                        }
                        Sensor.TYPE_ACCELEROMETER -> {
                            if (motionSensitivity > 0f) {
                                val x = event.values[0]
                                val y = event.values[1]
                                val z = event.values[2]
                                if (!isFirstAccelValue) {
                                    val dx = x - lastX
                                    val dy = y - lastY
                                    val dz = z - lastZ
                                    val deltaSq = dx * dx + dy * dy + dz * dz
                                    if (deltaSq > accelThresholdSq) {
                                        onExitRequest()
                                    }
                                }
                                lastX = x
                                lastY = y
                                lastZ = z
                                isFirstAccelValue = false
                            }
                        }
                        Sensor.TYPE_PROXIMITY -> {
                            val distance = event.values.getOrNull(0) ?: return
                            val maxRange = event.sensor.maximumRange
                            val isNear = distance < minOf(maxRange, 5f)
                            if (!initialProximityRecorded) {
                                initialProximityRecorded = true
                                lastProximityNear = isNear
                            } else if (isNear != lastProximityNear) {
                                lastProximityNear = isNear
                                onExitRequest()
                            }
                        }
                    }
                }

                override fun onAccuracyChanged(s: Sensor?, accuracy: Int) {}
            }

            try {
                if (linearSensor != null && motionSensitivity > 0f) {
                    sm.registerListener(listener, linearSensor, SensorManager.SENSOR_DELAY_UI)
                } else if (accelSensor != null && motionSensitivity > 0f) {
                    sm.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
                }
                if (proximitySensor != null) {
                    sm.registerListener(listener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            onDispose {
                try {
                    sm.unregisterListener(listener)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Dynamic Pixel Burn-in Orbital Shifter
    val shiftSpeed = if (burnInShiftIntervalSeconds > 0) burnInShiftIntervalSeconds else 10
    val burnInOffset = remember(serviceState.timeLeft, shiftSpeed) {
        val orbitRadiusDp = 6.0
        val step = (serviceState.timeLeft / shiftSpeed)
        val angleRad = (step % 12) * (2.0 * Math.PI / 12.0)
        val x = (orbitRadiusDp * cos(angleRad)).toFloat().dp
        val y = (orbitRadiusDp * sin(angleRad)).toFloat().dp
        Pair(x, y)
    }

    val textAlpha = (1.0f - dimnessLevel).coerceIn(0.02f, 0.40f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .then(sensitivityModifier),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .offset(x = burnInOffset.first, y = burnInOffset.second)
        ) {
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

            Text(
                text = currentTimeStr,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 68.sp),
                fontWeight = FontWeight.ExtraLight,
                color = Color.White.copy(alpha = textAlpha * 1.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            val m = serviceState.timeLeft / 60
            val s = serviceState.timeLeft % 60
            Text(
                text = String.format(Locale.getDefault(), "%02d:%02d", m, s),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp),
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = textAlpha * 2.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

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
