package lumia.tracker.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import lumia.tracker.service.AodAccessibilityService
import lumia.tracker.service.PomodoroService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrueAodOverlayUi(
    dimnessLevel: Float,
    sensitivity: String,
    motionSensitivity: Float = 1.2f,
    lockTimeoutSeconds: Int,
    onExitRequest: () -> Unit
) {
    val serviceState by PomodoroService.state.collectAsState()
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    var currentTimeStr by remember { mutableStateOf(timeFormat.format(Date())) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeStr = timeFormat.format(Date())
            delay(1000)
        }
    }

    val context = LocalContext.current
    LaunchedEffect(lockTimeoutSeconds) {
        if (lockTimeoutSeconds > 0) {
            delay(lockTimeoutSeconds * 1000L)
            if (AodAccessibilityService.isServiceEnabled(context)) {
                AodAccessibilityService.lockScreen()
            }
        }
    }

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
            "motion", "highest" -> Modifier.pointerInput(Unit) { detectTapGestures(onTap = { onExitRequest() }) }
            "medium" -> Modifier.pointerInput(Unit) { detectTapGestures(onDoubleTap = { onExitRequest() }) }
            else -> Modifier.pointerInput(Unit) {
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

    if (sensitivity == "motion" && motionSensitivity > 0f) {
        AodMotionDetector(context, motionSensitivity, onExitRequest)
    }

    val shiftSpeed = 10
    val burnInOffset = remember(serviceState.timeLeft) {
        val tickFraction = serviceState.timeLeft / shiftSpeed
        val tick = (tickFraction % 10)
        val x = if (tick % 2 == 0) (tick - 5).dp else 0.dp
        val y = if (tick % 2 != 0) (tick - 5).dp else 0.dp
        Pair(x, y)
    }

    val textAlpha = (1.0f - dimnessLevel).coerceIn(0.02f, 0.40f)

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black).then(sensitivityModifier),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp).offset(x = burnInOffset.first, y = burnInOffset.second)
        ) {
            Text(text = currentTimeStr, style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp), fontWeight = FontWeight.ExtraLight, color = Color.White.copy(alpha = textAlpha * 1.5f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            val m = serviceState.timeLeft / 60
            val s = serviceState.timeLeft % 60
            Text(text = String.format("%02d:%02d", m, s), style = MaterialTheme.typography.displayLarge.copy(fontSize = 104.sp), fontWeight = FontWeight.Light, color = Color.White.copy(alpha = textAlpha * 2.5f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Text(text = "${serviceState.modeString} • Session #${serviceState.sessionsCompleted + 1}", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White.copy(alpha = textAlpha * 2.0f), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
            }
            Spacer(modifier = Modifier.height(48.dp))
            val guideMessage = when (sensitivity) {
                "motion" -> "True Always-On Active • Touch or Move Device to Dismiss"
                "highest" -> "True Always-On Active • Tap Screen to Dismiss"
                "medium" -> "True Always-On Active • Double-Tap Screen to Dismiss"
                else -> if (isHolding) "Releasing System lock..." else "True Always-On Active • Touch and Hold to Dismiss"
            }
            Text(text = guideMessage, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = textAlpha * 1.2f), textAlign = TextAlign.Center)
            if (sensitivity == "secure" && isHolding) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.width(180.dp).height(6.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(3.dp))) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(holdProgress).background(Color.White.copy(alpha = textAlpha * 3.0f), RoundedCornerShape(3.dp)))
                }
            }
        }
    }
}

@Composable
private fun AodMotionDetector(context: Context, motionSensitivity: Float, onExitRequest: () -> Unit) {
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val linearSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) }
    val accelSensor = remember { if (linearSensor == null) sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) else null }

    DisposableEffect(sensorManager, linearSensor, accelSensor, motionSensitivity) {
        var lastX = 0f; var lastY = 0f; var lastZ = 0f
        var isFirstValue = true
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
                    val x = event.values[0]; val y = event.values[1]; val z = event.values[2]
                    val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                    if (acceleration > motionSensitivity) onExitRequest()
                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]; val y = event.values[1]; val z = event.values[2]
                    if (!isFirstValue) {
                        val dx = x - lastX; val dy = y - lastY; val dz = z - lastZ
                        val delta = Math.sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
                        if (delta > motionSensitivity * 0.375f) onExitRequest()
                    }
                    lastX = x; lastY = y; lastZ = z; isFirstValue = false
                }
            }
            override fun onAccuracyChanged(s: Sensor?, accuracy: Int) {}
        }
        if (linearSensor != null) sensorManager.registerListener(listener, linearSensor, SensorManager.SENSOR_DELAY_UI)
        else if (accelSensor != null) sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)

        onDispose { sensorManager.unregisterListener(listener) }
    }
}
