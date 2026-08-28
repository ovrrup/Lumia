package lumia.tracker.ui.screens.settings

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import lumia.tracker.service.AodAccessibilityService
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.settings.components.SettingsGroupCard
import lumia.tracker.ui.screens.settings.components.SettingsToggleItem
import lumia.tracker.viewmodel.ScholarViewModel

@ValueScore(
    score = 90,
    importance = Importance.HIGH,
    description = "Safety guard monitoring, conflicts warnings, and OLED AOD burn-in protection configurations",
    category = "Settings"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyFeaturesScreen(navController: NavController, viewModel: ScholarViewModel) {
    val context = LocalContext.current
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var hasAccessibilityPermission by remember { mutableStateOf(AodAccessibilityService.isServiceEnabled(context)) }

    val lifecycleOwnerForPermissions = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwnerForPermissions) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = Settings.canDrawOverlays(context)
                hasAccessibilityPermission = AodAccessibilityService.isServiceEnabled(context)
            }
        }
        lifecycleOwnerForPermissions.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwnerForPermissions.lifecycle.removeObserver(observer)
        }
    }

    val safetyPinEnabled by viewModel.safetyPinEnabled.collectAsStateWithLifecycle()
    val safetyPinConflictWarning by viewModel.safetyPinConflictWarning.collectAsStateWithLifecycle()
    val safetyPinRecommendations by viewModel.safetyPinRecommendations.collectAsStateWithLifecycle()

    val aodTrueBlackOled by viewModel.aodTrueBlackOled.collectAsStateWithLifecycle()
    val aodAutoDeactivateTrueBlack by viewModel.aodAutoDeactivateTrueBlack.collectAsStateWithLifecycle()
    val aodBurnInShiftSpeed by viewModel.aodBurnInShiftSpeed.collectAsStateWithLifecycle()
    val aodLockScreenSupport by viewModel.aodLockScreenSupport.collectAsStateWithLifecycle()

    val aodTrueAodEnabled by viewModel.aodTrueAodEnabled.collectAsStateWithLifecycle()
    val aodTrueAodMode by viewModel.aodTrueAodMode.collectAsStateWithLifecycle()
    val aodSensitivity by viewModel.aodSensitivity.collectAsStateWithLifecycle()
    val aodMotionSensitivity by viewModel.aodMotionSensitivity.collectAsStateWithLifecycle()
    val aodDimnessLevel by viewModel.aodDimnessLevel.collectAsStateWithLifecycle()
    val aodLockTimeout by viewModel.aodLockTimeout.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Safety Guard & AOD",
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    BouncyIconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Safety Guard Monitor
            SettingsGroupCard(title = "Safety Guard Monitor", icon = Icons.Rounded.Shield) {
                SettingsToggleItem(
                    title = "System Safety Watch",
                    subtitle = "Detect settings conflicts and suggest optimizations",
                    checked = safetyPinEnabled,
                    icon = Icons.Rounded.Shield,
                    onCheckedChange = { viewModel.updateSafetyPinEnabled(it) }
                )

                AnimatedVisibility(
                    visible = safetyPinEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        SettingsToggleItem(
                            title = "Conflict Warnings",
                            subtitle = "Alert when active settings oppose each other",
                            checked = safetyPinConflictWarning,
                            onCheckedChange = { viewModel.updateSafetyPinConflictWarning(it) }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        SettingsToggleItem(
                            title = "Visual Recommendations",
                            subtitle = "Suggest synergy settings when detected",
                            checked = safetyPinRecommendations,
                            onCheckedChange = { viewModel.updateSafetyPinRecommendations(it) }
                        )
                    }
                }
            }

            // 2. Always-On Display (AOD)
            SettingsGroupCard(title = "Always-On Display (AOD)", icon = Icons.Rounded.ScreenLockPortrait) {
                SettingsToggleItem(
                    title = "Always-On Display",
                    subtitle = "Show minimalist clock and status during sleep",
                    checked = aodTrueAodEnabled,
                    icon = Icons.Rounded.Visibility,
                    onCheckedChange = { viewModel.updateAodTrueAodEnabled(it) }
                )

                AnimatedVisibility(
                    visible = aodTrueAodEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        // AOD Dimness Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dimness Level",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${(aodDimnessLevel * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Slider(
                            value = aodDimnessLevel,
                            onValueChange = { viewModel.updateAodDimnessLevel(it) },
                            valueRange = 0.1f..1.0f,
                            modifier = Modifier.fillMaxWidth()
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        // Motion Sensitivity
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Motion Wake Sensitivity",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${(aodMotionSensitivity * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Slider(
                            value = aodMotionSensitivity,
                            onValueChange = { viewModel.updateAodMotionSensitivity(it) },
                            valueRange = 0.0f..1.0f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 3. OLED Burn-In Prevention
            SettingsGroupCard(title = "OLED Burn-In Protection", icon = Icons.Rounded.BrightnessLow) {
                SettingsToggleItem(
                    title = "Pure Black OLED Surface",
                    subtitle = "Turn off pixels on OLED screens",
                    checked = aodTrueBlackOled,
                    icon = Icons.Rounded.Contrast,
                    onCheckedChange = { viewModel.updateAodTrueBlackOled(it) }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                SettingsToggleItem(
                    title = "Lock Screen Support",
                    subtitle = "Display AOD status on lock screen",
                    checked = aodLockScreenSupport,
                    icon = Icons.Rounded.Lock,
                    onCheckedChange = { viewModel.updateAodLockScreenSupport(it) }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Burn In Shift Speed Slider
                Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pixel Shift Interval",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${aodBurnInShiftSpeed}s",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Slider(
                        value = aodBurnInShiftSpeed.toFloat(),
                        onValueChange = { viewModel.updateAodBurnInShiftSpeed(it.toInt()) },
                        valueRange = 10f..120f,
                        steps = 10,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

