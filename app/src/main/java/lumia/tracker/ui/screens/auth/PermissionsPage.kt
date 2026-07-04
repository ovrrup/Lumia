package lumia.tracker.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Token
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import lumia.tracker.viewmodel.ScholarViewModel

@Composable
fun PermissionsPage(isActive: Boolean, onComplete: () -> Unit) {
    val scale by animateFloatAsState(if (isActive) 1f else 0.8f, tween(600, easing = androidx.compose.animation.core.FastOutSlowInEasing), label = "perm_scale")
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
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(24.dp)
            .scale(scale)
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "Permissions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Configure permissions to experience Lumia's full layout. Both True AOD overlay options are supported.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }

        // Notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionCard(
                title = "Notifications",
                description = "For Pomodoro timer alerts and important task reminders.",
                icon = Icons.Default.Notifications,
                isGranted = notificationsGranted,
                onRequest = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
            )
        } else {
            PermissionCard(
                title = "Notifications",
                description = "For Pomodoro timer alerts and important task reminders.",
                icon = Icons.Default.Notifications,
                isGranted = true,
                onRequest = { }
            )
        }

        // Overlay
        PermissionCard(
            title = "True AOD Overlay (Technical Version)",
            description = "Available in the Technical Version: Draw screen overlay clock directly over lockscreens. No battery strain.",
            icon = Icons.Rounded.AutoAwesome,
            isGranted = overlayGranted,
            onRequest = {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                try { context.startActivity(intent) } catch (e: Exception) {}
            }
        )

        // Accessibility Service
        PermissionCard(
            title = "AOD Accessibility (Technical Version)",
            description = "Available in the Technical Version: Render screen-saver safely behind system security lock panels.",
            icon = Icons.Rounded.FavoriteBorder,
            isGranted = accessibilityGranted,
            onRequest = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                try { context.startActivity(intent) } catch (e: Exception) {}
            }
        )

        // Exact Alarms
        PermissionCard(
            title = "Exact Alarms",
            description = "Ensure study timer precision even when the device goes into deep sleep.",
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

        // Battery Optimization Exemption
        PermissionCard(
            title = "Exempt Battery Limit",
            description = "Crucial without GMS: Allow Lumia to launch exact local alerts and syncs when deep idling.",
            icon = Icons.Rounded.Analytics, // Using an available rounded icon
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
