package lumia.tracker.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

@Composable
fun PermissionsPage(isActive: Boolean, onComplete: () -> Unit) {
    val scale by animateFloatAsState(if (isActive) 1f else 0.85f, tween(600), label = "perm_scale")
    val alpha by animateFloatAsState(if (isActive) 1f else 0f, tween(600), label = "perm_alpha")

    val context = LocalContext.current
    var notificationsGranted by remember { 
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsGranted = granted
    }

    // Alarms
    val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
    var alarmsGranted by remember { 
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms() else true
        )
    }

    var batteryIgnoring by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as? android.os.PowerManager
                powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
            } else {
                true
            }
        )
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    // Overlay & Accessibility Permissions
    var overlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var accessibilityGranted by remember { mutableStateOf(lumia.tracker.service.AodAccessibilityService.isServiceEnabled(context)) }

    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                alarmsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms() else true
                overlayGranted = Settings.canDrawOverlays(context)
                accessibilityGranted = lumia.tracker.service.AodAccessibilityService.isServiceEnabled(context)
                batteryIgnoring = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as? android.os.PowerManager
                    powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
                } else {
                    true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .scale(scale)
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Redesigned Header Area
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.VerifiedUser,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "System Integration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Configure system access permissions to let Lumia track, schedule, and render displays accurately.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        // Section 1: Core Features Group Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Core Helpers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Notifications
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PermissionCard(
                        title = "Notifications",
                        description = "Triggers alerts for Pomodoro timer stages and due task schedules.",
                        icon = Icons.Default.Notifications,
                        isGranted = notificationsGranted,
                        onRequest = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                    )
                } else {
                    PermissionCard(
                        title = "Notifications",
                        description = "Triggers alerts for Pomodoro timer stages and due task schedules.",
                        icon = Icons.Default.Notifications,
                        isGranted = true,
                        onRequest = { }
                    )
                }

                // Exact Alarms
                PermissionCard(
                    title = "Exact Alarms",
                    description = "Fires high-precision timers and daily agenda reminders reliably.",
                    icon = Icons.Default.Timer,
                    isGranted = alarmsGranted,
                    onRequest = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            try { context.startActivity(intent) } catch (e: Exception) {}
                        }
                    }
                )

                // Battery Exempt
                PermissionCard(
                    title = "Exempt Battery Limits",
                    description = "Let Lumia run exact local timers and widget refreshes in idle mode.",
                    icon = Icons.Rounded.FlashOn,
                    isGranted = batteryIgnoring,
                    onRequest = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                    context.startActivity(intent)
                                } catch (ex: Exception) {}
                            }
                        }
                    }
                )
            }
        }

        // Section 2: Always On Display Services
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Always-On Displays",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Overlay
                PermissionCard(
                    title = "True AOD Overlay",
                    description = "Available in Technical Version: Draws high-contrast screensavers over secure locks.",
                    icon = Icons.Rounded.Layers,
                    isGranted = overlayGranted,
                    onRequest = {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        try { context.startActivity(intent) } catch (e: Exception) {}
                    }
                )

                // Accessibility
                PermissionCard(
                    title = "AOD Accessibility",
                    description = "Available in Technical Version: Suspends system overlays safely behind secure locked views.",
                    icon = Icons.Rounded.AccessibilityNew,
                    isGranted = accessibilityGranted,
                    onRequest = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        try { context.startActivity(intent) } catch (e: Exception) {}
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
    }
}
