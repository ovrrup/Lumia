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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Timer
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController, viewModel: ScholarViewModel) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLegalAgreed by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> LegalConsentPage(
                        isActive = pagerState.currentPage == page,
                        isAgreed = isLegalAgreed,
                        onAgreedChange = { isLegalAgreed = it }
                    )
                    1 -> OnboardingPage(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "Welcome to Lumia",
                        description = "Your beautifully crafted companion for seamless study and productivity. Let's make learning an elegant experience.",
                        isActive = pagerState.currentPage == page
                    )
                    2 -> OnboardingPage(
                        icon = Icons.Rounded.Timer,
                        title = "Focus & Analytics",
                        description = "Utilize the True AOD Pomodoro timer to maintain deep focus without draining battery, and track everything with insightful analytics.",
                        isActive = pagerState.currentPage == page
                    )
                    3 -> OnboardingPage(
                        icon = Icons.Rounded.EditNote,
                        title = "Effortless Organization",
                        description = "Manage tasks, take quick notes, and organize courses effortlessly in a sleek, distraction-free environment.",
                        isActive = pagerState.currentPage == page
                    )
                    4 -> PermissionsPage(
                        isActive = pagerState.currentPage == page,
                        onComplete = {
                            viewModel.completeOnboarding()
                            navController.navigate("dashboard") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    )
                }
            }

            // Bottom Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicators
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(5) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(if (isSelected) 24.dp else 8.dp, label = "indicator_width")
                        val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                // Next / Get Started Button
                Button(
                    onClick = {
                        if (pagerState.currentPage < 4) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            viewModel.completeOnboarding()
                            navController.navigate("dashboard") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                    enabled = pagerState.currentPage != 0 || isLegalAgreed
                ) {
                    Text(if (pagerState.currentPage == 4) "Finish" else "Next", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LegalConsentPage(
    isActive: Boolean,
    isAgreed: Boolean,
    onAgreedChange: (Boolean) -> Unit
) {
    val scale by animateFloatAsState(if (isActive) 1f else 0.8f, tween(600, easing = androidx.compose.animation.core.FastOutSlowInEasing), label = "consent_scale")
    val alpha by animateFloatAsState(if (isActive) 1f else 0f, tween(600), label = "consent_alpha")

    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .scale(scale)
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Icon / Branding
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = "Terms & Privacy Policy",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Before using Lumia, please review and accept our legal terms. Lumia operates 100% offline and preserves complete user data privacy.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Summary of Key Points:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "• 100% Offline-First: Your courses, tasks, notes, and records are stored exclusively in your local database.\n" +
                           "• No Account Required: No email, login, or personal profile is ever asked.\n" +
                           "• True AOD & Permissions: System overlay and Accessibility permissions are only used locally to run the black energy-saving screen blocker.\n" +
                           "• Open Source: Licensed under GPLv3 copyleft terms.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }

        // Action Links Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { showTermsDialog = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("View Terms")
            }
            OutlinedButton(
                onClick = { showPrivacyDialog = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("View Privacy")
            }
        }

        // Checkbox Agreement row
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAgreed) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = if (isAgreed) 2.dp else 1.dp,
                color = if (isAgreed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            ),
            onClick = { onAgreedChange(!isAgreed) }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Checkbox(
                    checked = isAgreed,
                    onCheckedChange = { onAgreedChange(it) }
                )
                Text(
                    text = "I have read and agree to the Terms and Conditions and Privacy Policy of Lumia.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }
        }
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms and Conditions") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "1. Open Source Framework & License\n" +
                               "Lumia is free software distributed under the GNU GPLv3 copyleft terms. Derivative works must also remain open source.\n\n" +
                               "2. Local Administration & No Warranty\n" +
                               "Since Lumia is 100% offline, clearing storage will erase data forever. The app is provided as-is without any hardware damage warranties.\n\n" +
                               "3. Advanced Overlays\n" +
                               "Lumia uses System Overlay and Accessibility options to draw screen-savers nicely. Use responsibly on compatible displays.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "1. Zero-Data Harvesting\n" +
                               "Lumia has no central server, analytical tracking SDKs, or remote storage. Your records stay yours.\n\n" +
                               "2. Permission Local Isolation\n" +
                               "• Overlays: Used solely to draw black AMOLED timers. No on-screen visual recording.\n" +
                               "• Accessibility: Intercepts lock status locally. NEVER logs touch coordinates or keys.\n" +
                               "• Alarms: Coordinates exact wake times locally.\n\n" +
                               "3. Offline Security\n" +
                               "Everything is managed in secure local SQLite sandboxes on your direct Android container.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}


@Composable
fun OnboardingPage(icon: ImageVector, title: String, description: String, isActive: Boolean) {
    val scale by animateFloatAsState(if (isActive) 1f else 0.8f, tween(600, easing = androidx.compose.animation.core.FastOutSlowInEasing), label = "page_scale")
    val alpha by animateFloatAsState(if (isActive) 1f else 0f, tween(600), label = "page_alpha")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .scale(scale)
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

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
            title = "True AOD Overlay",
            description = "Draw screen overlay clock directly over lockscreens. No battery strain.",
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
            title = "AOD Accessibility",
            description = "Recommended: Render screen-saver safely behind system security lock panels.",
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

@Composable
fun PermissionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = { if (!isGranted) onRequest() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGranted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(16.dp))
            if (isGranted) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Granted",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                TextButton(onClick = onRequest) {
                    Text("ALLOW")
                }
            }
        }
    }
}
