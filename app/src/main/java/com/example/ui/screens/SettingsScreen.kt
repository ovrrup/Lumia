package ovrrup.lumia.ui.screens

import ovrrup.lumia.service.AodAccessibilityService
import ovrrup.lumia.util.TrueAodManager
import android.content.Intent
import android.provider.Settings
import ovrrup.lumia.ui.theme.liquidGlass
import ovrrup.lumia.ui.theme.glassBar
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.compose.material.icons.rounded.Info
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.MergeType
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ViewQuilt
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ovrrup.lumia.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val betaGlassUi by viewModel.betaGlassUi.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (betaEnhancedHeader || isGlass) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text("Settings Hub", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Premium Academy Engine Info Card
            ovrrup.lumia.ui.components.GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Device info",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Lumia Engine Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Text(
                                    text = "System active & synchronized",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ACTIVE SCHEME",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$themeColor Palette",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "ENGINE RENDER",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (betaGlassUi) "High-Fi Glass" else "Elegant Flat",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Personalization Card
            SettingsGroupCard(title = "Personalization", icon = Icons.Rounded.Palette) {
                SettingsActionItemInCard(
                    title = "Appearance & Theme",
                    subtitle = "Themes, color palettes, and layout modifiers",
                    icon = Icons.Rounded.Palette,
                    onClick = { navController.navigate("settings/appearance") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // System configuration
            SettingsGroupCard(title = "System Details", icon = Icons.Rounded.Settings) {
                SettingsActionItemInCard(
                    title = "System Configuration",
                    subtitle = "Advanced background features and interconnections",
                    icon = Icons.Rounded.Settings,
                    onClick = { navController.navigate("settings/system") }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                
                SettingsActionItemInCard(
                    title = "Experimental Features",
                    subtitle = "Quick tools and layouts",
                    icon = Icons.Rounded.Check,
                    onClick = { navController.navigate("settings/beta") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Safety & Notifications
            SettingsGroupCard(title = "Alerts & Security", icon = Icons.Rounded.Lock) {
                SettingsActionItemInCard(
                    title = "Safety System Guard",
                    subtitle = "Automatic alerts and smart recommendations",
                    icon = Icons.Rounded.Lock,
                    onClick = { navController.navigate("settings/safety") }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "Notifications Management",
                    subtitle = "Tones, schedules, and active alerts",
                    icon = Icons.Rounded.Notifications,
                    onClick = { navController.navigate("settings/notifications") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Data Management
            SettingsGroupCard(title = "Storage & Versioning", icon = Icons.Rounded.Storage) {
                SettingsActionItemInCard(
                    title = "Database & Management",
                    subtitle = "Manage secure active backups, exports & resets",
                    icon = Icons.Rounded.Storage,
                    onClick = { navController.navigate("settings/data") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "About Lumia (FOSS)",
                    subtitle = "Developer info, GNU license text & update status",
                    icon = Icons.Rounded.Info,
                    onClick = { navController.navigate("settings/about") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(navController: NavController, viewModel: ScholarViewModel) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
    val betaGlassUi by viewModel.betaGlassUi.collectAsStateWithLifecycle()
    val betaDynamicBackground by viewModel.betaDynamicBackground.collectAsStateWithLifecycle()
    val dynamicBgLightBrightness by viewModel.dynamicBgLightBrightness.collectAsStateWithLifecycle()
    val dynamicBgDarkBrightness by viewModel.dynamicBgDarkBrightness.collectAsStateWithLifecycle()
    val betaBetterTexts by viewModel.betaBetterTexts.collectAsStateWithLifecycle()
    val betaBetterTextsPalette by viewModel.betaBetterTextsPalette.collectAsStateWithLifecycle()
    val glassBackdropStyle by viewModel.glassBackdropStyle.collectAsStateWithLifecycle()
    val glassOpacityValue by viewModel.glassOpacityValue.collectAsStateWithLifecycle()
    val navBarGlassOpacityValue by viewModel.navBarGlassOpacityValue.collectAsStateWithLifecycle()
    val pureBlackMode by viewModel.pureBlackMode.collectAsStateWithLifecycle()
    val betaMinimalistMode by viewModel.betaMinimalistMode.collectAsStateWithLifecycle()
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val dynamicAppIcon by viewModel.dynamicAppIcon.collectAsStateWithLifecycle()
    val betaFrostGlass by viewModel.betaFrostGlass.collectAsStateWithLifecycle()

    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current

    val isSystemSystemDarkForOpacity = androidx.compose.foundation.isSystemInDarkTheme()
    androidx.compose.runtime.LaunchedEffect(themeColor, themeMode) {
        val effectiveDark = themeMode == "Dark" || (themeMode == "System" && isSystemSystemDarkForOpacity)
        viewModel.refreshNavBarGlassOpacity(themeColor, effectiveDark)
    }

    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (betaEnhancedHeader || isGlass) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text("Appearance & Theme", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Core Mode Card
            SettingsGroupCard(title = "Core Theme Style", icon = Icons.Rounded.DarkMode) {
                // Segmented Theme selector
                SettingsSegmentedPicker(
                    title = "Active Render Mode",
                    subtitle = "Select how the system environment is rendered",
                    options = listOf(
                        Triple("System", "System", Icons.Rounded.Settings),
                        Triple("Light", "Light", Icons.Rounded.Palette),
                        Triple("Dark", "Dark", Icons.Rounded.DarkMode)
                    ),
                    selected = themeMode,
                    onSelected = { viewModel.updateThemeMode(it) }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                
                SettingsPremiumToggleItem(
                    title = "Pure Black Canvas",
                    subtitle = "Apply solid pitch-black background inside dark render style",
                    checked = pureBlackMode,
                    icon = Icons.Rounded.DarkMode,
                    enabled = themeMode != "Light",
                    unavailableReason = "Requires System/Dark mode options.",
                    onCheckedChange = { viewModel.updatePureBlackMode(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Screen Layout
            SettingsGroupCard(title = "Screen Layout", icon = Icons.Rounded.CropFree) {
                val displayLayoutMode by viewModel.displayLayoutMode.collectAsStateWithLifecycle()
                SettingsSegmentedPicker(
                    title = "Display Drawing Mode",
                    subtitle = "Adjust how to handle device notches and screen edges",
                    options = listOf(
                        Triple("Normal", "Normal", null),
                        Triple("Notch Optimization", "Safe Area", null),
                        Triple("Immersive", "Immersive", Icons.Rounded.Star)
                    ),
                    selected = displayLayoutMode,
                    onSelected = { viewModel.updateDisplayLayoutMode(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsGroupCard(title = "Animatics & Shape Configurations", icon = Icons.Rounded.PlayArrow) {
                val appAnimationMode by viewModel.appAnimationMode.collectAsStateWithLifecycle()
                val moreRounds by viewModel.moreRounds.collectAsStateWithLifecycle()

                SettingsSegmentedPicker(
                    title = "Application Animation Quality",
                    subtitle = "Changes the responsiveness and bounce traits across panels and gestures.",
                    options = listOf(
                        Triple("Normal", "Normal", null),
                        Triple("Dynamic", "Dynamic", null),
                        Triple("Bouncy", "Bouncy", Icons.Rounded.Star)
                    ),
                    selected = appAnimationMode,
                    onSelected = { viewModel.updateAppAnimationMode(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                SettingsPremiumToggleItem(
                    title = "More Rounds Mode",
                    subtitle = "Replace all sharp-edged geometries with bouncy, spherical rounded layouts",
                    checked = moreRounds,
                    icon = Icons.Rounded.CheckCircle,
                    onCheckedChange = { viewModel.updateMoreRounds(it) }
                )
            }

            // 2. Glass UI Engine Card (Animated entry)
            AnimatedVisibility(visible = !betaMinimalistMode) {
                SettingsGroupCard(title = "Aesthetic Glass Engine", icon = Icons.Rounded.Palette) {
                    SettingsPremiumToggleItem(
                        title = "Frosted Glass UI",
                        subtitle = "Enable premium translucent glass textures across screen panels",
                        checked = betaGlassUi,
                        icon = Icons.Rounded.Palette,
                        onCheckedChange = { viewModel.updateBetaGlassUi(it) }
                    )
                    
                    AnimatedVisibility(visible = betaGlassUi) {
                        val betaGlassDynamic by viewModel.betaGlassDynamic.collectAsStateWithLifecycle()
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(start = 12.dp, top = 8.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            
                            SettingsPremiumToggleItem(
                                title = "Dynamic Color Tinting",
                                subtitle = "Blend glass texture directly with active theme shades",
                                checked = betaGlassDynamic,
                                onCheckedChange = { viewModel.updateBetaGlassDynamic(it) }
                            )

                            SettingsPremiumToggleItem(
                                title = "Soft Frost Glaze",
                                subtitle = "Apply high-end satin texture blur to the primary panel layers",
                                checked = betaFrostGlass,
                                onCheckedChange = { viewModel.updateBetaFrostGlass(it) }
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                            // Sub-segmented backdrop style
                            SettingsSegmentedPicker(
                                title = "Backdrop Density Style",
                                subtitle = "Choose panel translucency characteristics",
                                options = listOf(
                                    Triple("Transparent", "Clear", null),
                                    Triple("Translucent", "Satin", null),
                                    Triple("Opaque", "Solid", null)
                                ),
                                selected = glassBackdropStyle,
                                onSelected = { viewModel.updateGlassBackdropStyle(it) }
                            )

                            // Slider for Translucent
                            AnimatedVisibility(visible = glassBackdropStyle == "Translucent") {
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Frosted Layer Opacity",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${(glassOpacityValue * 100).toInt()}%",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = "Calibrate the light passage density through frosted satin panes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    Slider(
                                        value = glassOpacityValue,
                                        onValueChange = { viewModel.updateGlassOpacityValue(it) },
                                        valueRange = 0.1f..1.0f,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            
                            // Nav Bar Glass Opacity
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Nav Bar Glass Opacity",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${(navBarGlassOpacityValue * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "Control bottom bar glass opacity for current theme and light/dark mode",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                val isSystemSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
                                val effectiveDark = themeMode == "Dark" || (themeMode == "System" && isSystemSystemDark)
                                Slider(
                                    value = navBarGlassOpacityValue,
                                    onValueChange = { viewModel.updateNavBarGlassOpacityValue(it, themeColor, effectiveDark) },
                                    valueRange = 0.1f..1.0f,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // Advanced Navigation Panel Configuration Card
            SettingsGroupCard(title = "Advanced Bottom Navigation", icon = Icons.Rounded.Settings) {
                val betaFloatingNav by viewModel.betaFloatingNav.collectAsStateWithLifecycle()
                val navBarHeight by viewModel.navBarHeight.collectAsStateWithLifecycle()
                val navBarPaddingHorizontal by viewModel.navBarPaddingHorizontal.collectAsStateWithLifecycle()
                val navBarPaddingBottom by viewModel.navBarPaddingBottom.collectAsStateWithLifecycle()
                val navBarCornerRadius by viewModel.navBarCornerRadius.collectAsStateWithLifecycle()
                val navBarLabelMode by viewModel.navBarLabelMode.collectAsStateWithLifecycle()
                val navBarGlassForceEnabled by viewModel.navBarGlassForceEnabled.collectAsStateWithLifecycle()
                val navBarIndicatorAlpha by viewModel.navBarIndicatorAlpha.collectAsStateWithLifecycle()

                // Layout Style choosing picker
                SettingsSegmentedPicker(
                    title = "Bottom Navigation Format",
                    subtitle = "Switch layout form between standard flat and suspended floating deck",
                    options = listOf(
                        Triple("Flat", "Standard Flat", null),
                        Triple("Floating", "Floating Dock", Icons.Rounded.Star)
                    ),
                    selected = if (betaFloatingNav) "Floating" else "Flat",
                    onSelected = { viewModel.updateBetaFloatingNav(it == "Floating") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                // Label visibility mode
                SettingsSegmentedPicker(
                    title = "Desktop Label Icons",
                    subtitle = "Set when menu item labels should be visible on the bar",
                    options = listOf(
                        Triple("Always", "Always", null),
                        Triple("Selected Only", "Selected", null),
                        Triple("Hidden", "Icons Only", null)
                    ),
                    selected = navBarLabelMode,
                    onSelected = { viewModel.updateNavBarLabelMode(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                // Toggle for forced glass navbar style
                SettingsPremiumToggleItem(
                    title = "Independent Glass Backdrop",
                    subtitle = "Force glass satin backdrop overlay specifically on bottom bar even if global Frosted UI is off",
                    checked = navBarGlassForceEnabled,
                    icon = Icons.Rounded.Palette,
                    onCheckedChange = { viewModel.updateNavBarGlassForceEnabled(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                // Height Slider
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bar Panel Height",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${navBarHeight.toInt()} dp",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Customize the absolute thickness of bottom panel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Slider(
                        value = navBarHeight,
                        onValueChange = { viewModel.updateNavBarHeight(it) },
                        valueRange = 56f..96f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                // Active item indicator pill opacity highlight
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Indicator Tint Alpha",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(navBarIndicatorAlpha * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Calibrate the select-state container overlay opacity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Slider(
                        value = navBarIndicatorAlpha,
                        onValueChange = { viewModel.updateNavBarIndicatorAlpha(it) },
                        valueRange = 0.0f..0.5f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (betaFloatingNav) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                    // Floating Dock Radius Customization
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pill Corner Radius",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${navBarCornerRadius.toInt()} dp",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Control roundness bounding the suspended pill geometry",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Slider(
                            value = navBarCornerRadius,
                            onValueChange = { viewModel.updateNavBarCornerRadius(it) },
                            valueRange = 0f..48f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                    // Horizontal margins customization
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Horizontal Deck Margin",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${navBarPaddingHorizontal.toInt()} dp",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Expand or narrow down the width profile of bottom panel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Slider(
                            value = navBarPaddingHorizontal,
                            onValueChange = { viewModel.updateNavBarPaddingHorizontal(it) },
                            valueRange = 0f..48f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                    // Bottom lift margin
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bottom Lift Padding",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${navBarPaddingBottom.toInt()} dp",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Elevate the bottom action shelf distance off device screen trim",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Slider(
                            value = navBarPaddingBottom,
                            onValueChange = { viewModel.updateNavBarPaddingBottom(it) },
                            valueRange = 0f..48f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Theme & Colors Card
            SettingsGroupCard(title = "Branding & Color Scheme", icon = Icons.Rounded.Palette) {
                Text(
                    text = "Active App Theme",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Select your personalized active Lumia color scheme",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val palettes = mutableListOf(
                        "Ocean" to androidx.compose.ui.graphics.Color(0xFF3197D6),
                        "Emerald" to androidx.compose.ui.graphics.Color(0xFF4BC27D),
                        "Gold" to androidx.compose.ui.graphics.Color(0xFFFFC646),
                        "Rose" to androidx.compose.ui.graphics.Color(0xFFE52F28),
                        "Sage" to androidx.compose.ui.graphics.Color(0xFFACBDAA),
                        "Twilight" to androidx.compose.ui.graphics.Color(0xFF958CE8),
                        "Custom" to androidx.compose.ui.graphics.Color(0xFF999999)
                    )
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        palettes.add(0, "Dynamic" to androidx.compose.ui.graphics.Color(0xFF909090))
                    }
                    items(palettes) { (name, color) ->
                        ThemeColorPickerItem(
                            name = name,
                            color = color,
                            isSelected = themeColor == name,
                            onClick = { viewModel.updateThemeColor(name) }
                        )
                    }
                }

                if (themeColor == "Custom") {
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    SettingsActionItemInCard(
                        title = "Fine-Tune Advanced Colors",
                        subtitle = "Deep customize specific hex shades for the custom palette",
                        icon = Icons.Rounded.Edit,
                        onClick = { navController.navigate("settings/advanced_theme") }
                    )
                }
            }

            // 4. Interface Tweaks Card
            SettingsGroupCard(title = "Interface Modifiers", icon = Icons.Rounded.Settings) {
                SettingsPremiumToggleItem(
                    title = "Minimalist Focus Mode",
                    subtitle = "Force-off and lock complex visuals for intense studying focus",
                    checked = betaMinimalistMode,
                    icon = Icons.Rounded.Star,
                    onCheckedChange = { viewModel.updateBetaMinimalistMode(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                SettingsPremiumToggleItem(
                    title = "UI-based Launcher Icon",
                    subtitle = "Match home screen app icon style with the active Lumia color scheme",
                    checked = dynamicAppIcon,
                    icon = Icons.Rounded.Palette,
                    onCheckedChange = { viewModel.updateDynamicAppIcon(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                SettingsPremiumToggleItem(
                    title = "Enhanced Blur Navigation",
                    subtitle = "Apply a polished satin translucent backdrop to primary navigation header",
                    checked = betaEnhancedHeader,
                    enabled = !betaMinimalistMode,
                    icon = Icons.Rounded.Settings,
                    unavailableReason = "Locked by Minimalist Focus Mode.",
                    onCheckedChange = { viewModel.updateBetaEnhancedHeader(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                SettingsPremiumToggleItem(
                    title = "Dynamic Lighting Background",
                    subtitle = "Soft, vibrant animated background gradient shifts",
                    checked = betaDynamicBackground,
                    enabled = !betaMinimalistMode,
                    icon = Icons.Rounded.Check,
                    unavailableReason = "Locked by Minimalist Focus Mode.",
                    onCheckedChange = { viewModel.updateBetaDynamicBackground(it) }
                )

                AnimatedVisibility(visible = betaDynamicBackground && !betaMinimalistMode) {
                    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.5f
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp)) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isDarkTheme) "Dark Mode Lighting Brightness" else "Light Mode Lighting Brightness",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            val currentBrightness = if (isDarkTheme) dynamicBgDarkBrightness else dynamicBgLightBrightness
                            Text(
                                text = "${(currentBrightness * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = if (isDarkTheme) {
                                "Calibrate background glow intensity in dark modes for optimal readability"
                            } else {
                                "Calibrate vibrant background energy in light modes for clean visual focus"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Slider(
                            value = if (isDarkTheme) dynamicBgDarkBrightness else dynamicBgLightBrightness,
                            onValueChange = {
                                if (isDarkTheme) {
                                    viewModel.updateDynamicBgDarkBrightness(it)
                                } else {
                                    viewModel.updateDynamicBgLightBrightness(it)
                                }
                            },
                            valueRange = 0.05f..1.0f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 5. Legibility & Typography Card
            SettingsGroupCard(title = "Legibility & Typography", icon = Icons.Rounded.Edit) {
                SettingsPremiumToggleItem(
                    title = "Better Texts Rendering",
                    subtitle = "Enhance text readability, high contrasts and aesthetic typography",
                    checked = betaBetterTexts,
                    icon = Icons.Rounded.Edit,
                    enabled = !betaMinimalistMode,
                    unavailableReason = "Locked by Minimalist Focus Mode.",
                    onCheckedChange = { viewModel.updateBetaBetterTexts(it) }
                )

                AnimatedVisibility(visible = betaBetterTexts && !betaMinimalistMode) {
                    Column(modifier = Modifier.padding(start = 12.dp, top = 8.dp)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                        SettingsPremiumToggleItem(
                            title = "Complex Palette Text Shades",
                            subtitle = "Render text with warm color palette tones instead of absolute white/black",
                            checked = betaBetterTextsPalette,
                            enabled = betaBetterTexts && !betaMinimalistMode,
                            unavailableReason = if (betaMinimalistMode) "Locked by Minimalist Focus Mode." else "Requires 'Better Texts Rendering' to be enabled.",
                            onCheckedChange = { viewModel.updateBetaBetterTextsPalette(it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
data class BetaFeatureDialogData(
    val title: String,
    val description: String,
    val onConfirm: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BetaFeaturesScreen(navController: NavController, viewModel: ScholarViewModel) {
    var pendingFeature by remember { mutableStateOf<BetaFeatureDialogData?>(null) }
    
    val handleToggle = { isChecked: Boolean, title: String, subtitle: String, updateAction: (Boolean) -> Unit ->
        if (isChecked) {
            pendingFeature = BetaFeatureDialogData(title, subtitle) {
                updateAction(true)
            }
        } else {
            updateAction(false)
        }
    }

    if (pendingFeature != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingFeature = null },
            title = { Text("Beta Feature: ${pendingFeature?.title ?: ""}", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Disclaimer: You are about to enable an experimental feature. Extreme caution is recommended. These capabilities are in active development and might present functional quirks or display modifications.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "About this feature:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        pendingFeature?.description ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { 
                    pendingFeature?.onConfirm?.invoke()
                    pendingFeature = null 
                }) { Text("Enable Feature", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { pendingFeature = null }) { Text("Cancel") }
            }
        )
    }

    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (isGlass) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text("Experimental Features", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Experimental Workflow
            SettingsGroupCard(title = "Experimental Workflow", icon = Icons.Rounded.Edit) {
                val betaNotes by viewModel.betaNotes.collectAsStateWithLifecycle()
                SettingsPremiumToggleItem(
                    title = "Quick Notes Overlay",
                    subtitle = "Draft scratchpad canvas for immediate raw notes overlay panel.",
                    checked = betaNotes,
                    icon = Icons.Rounded.Edit,
                    onCheckedChange = { handleToggle(it, "Quick Notes Overlay", "Enable immediate raw scratchpad notes overlay panel.") { isChecked -> viewModel.updateBetaNotes(isChecked) } }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Display Hacks & System Settings
            SettingsGroupCard(title = "Display Settings & Hooks", icon = Icons.Rounded.Settings) {
                val betaMinimalistMode by viewModel.betaMinimalistMode.collectAsStateWithLifecycle()
                val betaFloatingNav by viewModel.betaFloatingNav.collectAsStateWithLifecycle()
                SettingsPremiumToggleItem(
                    title = "Floating Action Bar",
                    subtitle = "Floating layout styling for the primary bottom navigator bar",
                    checked = betaFloatingNav,
                    icon = Icons.Rounded.Settings,
                    enabled = !betaMinimalistMode,
                    unavailableReason = "Locked by Minimalist Focus Mode.",
                    onCheckedChange = { handleToggle(it, "Floating Action Bar", "Float the bottom navigation overlay panel.") { isChecked -> viewModel.updateBetaFloatingNav(isChecked) } }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                val showActionHistory by viewModel.showActionHistory.collectAsStateWithLifecycle()
                SettingsPremiumToggleItem(
                    title = "Display Action History",
                    subtitle = "Integrate detailed reactive logs list inside Analytics interface",
                    checked = showActionHistory,
                    icon = Icons.Rounded.List,
                    onCheckedChange = { handleToggle(it, "Display Action History", "Synthesize analytics telemetry block containing audit records.") { isChecked -> viewModel.updateShowActionHistory(isChecked) } }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(navController: NavController, viewModel: ScholarViewModel) {
    val status by viewModel.importExportStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.exportData(uri)
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importData(uri)
        }
    }

    LaunchedEffect(status) {
        status?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearStatus()
        }
    }

    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (isGlass) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text("Data Management", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            val coursesCount by viewModel.courses.collectAsStateWithLifecycle()
            val assignmentsCount by viewModel.assignments.collectAsStateWithLifecycle()
            val subjectsCount by viewModel.subjects.collectAsStateWithLifecycle()
            val pomodoroSessionsCount by viewModel.pomodoroSessions.collectAsStateWithLifecycle()

                // LogDog Card
                var showLogDogDialog by remember { mutableStateOf(false) }
                ovrrup.lumia.ui.components.GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("LogDog Diagnostic Handler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        SettingsActionItemInCard(
                            title = "Trigger Analysis (Woof!)",
                            subtitle = "Run code analysis and view last captured crash error data with a funny twist.",
                            icon = Icons.Rounded.RecordVoiceOver,
                            onClick = { 
                                showLogDogDialog = true
                            }
                        )
                    }
                }

                if (showLogDogDialog) {
                    var isScanning by remember { mutableStateOf(true) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(1200)
                        isScanning = false
                    }
                    var crashesList by remember { mutableStateOf(ovrrup.lumia.util.LogDog.getCrashes(context)) }
                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                    
                    androidx.compose.ui.window.Dialog(
                        onDismissRequest = { showLogDogDialog = false },
                        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 32.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = androidx.compose.ui.graphics.Color(0xFF0F172A),
                            contentColor = androidx.compose.ui.graphics.Color(0xFF38BDF8)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().background(androidx.compose.ui.graphics.Color(0xFF1E293B)).padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text("LogDog Core Analyzer v2.0", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = androidx.compose.ui.graphics.Color.White)
                                    }
                                    IconButton(onClick = { showLogDogDialog = false }) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = "Close", tint = androidx.compose.ui.graphics.Color.White)
                                    }
                                }
                                
                                Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
                                    if (isScanning) {
                                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                CircularProgressIndicator(color = androidx.compose.ui.graphics.Color(0xFF38BDF8))
                                                Spacer(Modifier.height(16.dp))
                                                Text("Sniffing stack traces... parsing runtime metrics...", style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color(0xFF94A3B8))
                                            }
                                        }
                                    } else if (crashesList.isEmpty()) {
                                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF22C55E), modifier = Modifier.size(48.dp))
                                                Spacer(Modifier.height(16.dp))
                                                Text("System Stable. Zero fatal faults in kennel.", style = MaterialTheme.typography.bodyLarge, color = androidx.compose.ui.graphics.Color.White)
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("FATAL EXCEPTIONS: ${crashesList.size}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color(0xFFF43F5E))
                                            TextButton(
                                                onClick = {
                                                    ovrrup.lumia.util.LogDog.clearCrashes(context)
                                                    crashesList = emptyList()
                                                    android.widget.Toast.makeText(context, "LogDog purged all telemetry.", android.widget.Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.textButtonColors(contentColor = androidx.compose.ui.graphics.Color(0xFFEF4444))
                                            ) {
                                                Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text("PURGE LOGS")
                                            }
                                        }
                                        
                                        crashesList.forEachIndexed { index, crash ->
                                            val parsed = remember(crash) { ovrrup.lumia.util.LogDog.analyzeCrash(crash) }
                                            
                                            Card(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF1E293B)),
                                                shape = RoundedCornerShape(16.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                        val sevColor = when (parsed.severityLevel) {
                                                            "Critical" -> androidx.compose.ui.graphics.Color(0xFFEF4444)
                                                            "High" -> androidx.compose.ui.graphics.Color(0xFFF97316)
                                                            else -> androidx.compose.ui.graphics.Color(0xFFEAB308)
                                                        }
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Box(modifier = Modifier.size(12.dp).background(sevColor, CircleShape))
                                                            Spacer(Modifier.width(8.dp))
                                                            Text("DUMP [${index + 1}] | ${parsed.severityLevel.uppercase()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
                                                        }
                                                        IconButton(
                                                            onClick = {
                                                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(crash))
                                                                android.widget.Toast.makeText(context, "Copied trace dump to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                                                            },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy raw log", tint = androidx.compose.ui.graphics.Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                                                        }
                                                    }
                                                    
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                    Text("FAULT MODULE: ${parsed.likelyComponent.uppercase()}", style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color(0xFF94A3B8))
                                                    Text(parsed.exceptionType, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color(0xFFF87171))
                                                    
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text("RAW MESSAGE:", style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color(0xFF94A3B8))
                                                    Text(parsed.errorMessage, style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color.LightGray)
                                                    
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text("POINTER:", style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color(0xFF94A3B8))
                                                    Text(parsed.crashLocation, style = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFF34D399)))
                                                    
                                                    if (parsed.isFrameworkBug) {
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text("⚠️ Framework-level crash detected. Trace pointer is outside your primary package.", style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color(0xFFEAB308))
                                                    }
                                                    
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                    Surface(
                                                        color = androidx.compose.ui.graphics.Color(0xFF0F172A),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                                                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                                            Spacer(Modifier.width(8.dp))
                                                            Text(parsed.suggestion, style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color.White)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                ovrrup.lumia.ui.components.GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Storage,
                                contentDescription = "Active Database Info",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Database Integrity Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Secure active binary protection enabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = coursesCount.size.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text(text = "Courses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = subjectsCount.size.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text(text = "Subjects", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = assignmentsCount.size.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text(text = "Assignments", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = pomodoroSessionsCount.size.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text(text = "Pomodoros", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            SettingsGroupCard(title = "Backup & Erasure Hub", icon = Icons.Rounded.Storage) {
                SettingsActionItemInCard(
                    title = "Export Secure Data Profile",
                    subtitle = "Back up all settings, customisations, courses and assignments into a portable file",
                    icon = Icons.Rounded.Upload,
                    onClick = { showExportDialog = true }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                
                SettingsActionItemInCard(
                    title = "Import Secure Data Profile",
                    subtitle = "Restore binary backup. Warning: Completely overwrites current active assets",
                    icon = Icons.Rounded.Download,
                    isDestructive = true,
                    onClick = { openDocumentLauncher.launch(arrayOf("application/octet-stream", "*/*")) }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                
                SettingsActionItemInCard(
                    title = "Full Environment Factory Erase",
                    subtitle = "Permanently delete all customisations, courses, subjects, assignments, and local history reports",
                    icon = Icons.Rounded.DeleteForever,
                    isDestructive = true,
                    onClick = { showResetDialog = true }
                )
            }
        }
    }

    if (showExportDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Data & Settings") },
            text = { Text("Are you sure you want to export a binary backup of all your data and settings?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExportDialog = false
                        createDocumentLauncher.launch("scholar_backup.bin")
                    }
                ) { Text("Export") }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showResetDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Erase All Data & Settings?") },
            text = { Text("This action cannot be undone. All your progress, custom themes, subjects, and settings will be permanently removed.", color = MaterialTheme.colorScheme.error) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Erase Data", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsCategoryHeading(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SettingsGroupMenu(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ThemeOptionButton(title: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(title) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun ThemeColorPickerItem(name: String, color: androidx.compose.ui.graphics.Color, isSelected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else if (isSelected) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsToggleItem(
    title: String, 
    subtitle: String, 
    checked: Boolean, 
    enabled: Boolean = true,
    unavailableReason: String? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp).alpha(if (enabled) 1f else 0.5f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!enabled && unavailableReason != null) {
                Text(
                    text = unavailableReason,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsActionItem(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isDestructive: Boolean = false, onClick: () -> Unit) {
    val contentColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isDestructive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = contentColor)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyFeaturesScreen(navController: NavController, viewModel: ScholarViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var hasOverlayPermission by remember { mutableStateOf(android.provider.Settings.canDrawOverlays(context)) }
    var hasAccessibilityPermission by remember { mutableStateOf(ovrrup.lumia.service.AodAccessibilityService.isServiceEnabled(context)) }

    val lifecycleOwnerForPermissions = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwnerForPermissions) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = android.provider.Settings.canDrawOverlays(context)
                hasAccessibilityPermission = ovrrup.lumia.service.AodAccessibilityService.isServiceEnabled(context)
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

    val aodTrueAodEnabled by viewModel.aodTrueAodEnabled.collectAsStateWithLifecycle()
    val aodTrueAodMode by viewModel.aodTrueAodMode.collectAsStateWithLifecycle()
    val aodSensitivity by viewModel.aodSensitivity.collectAsStateWithLifecycle()
    val aodDimnessLevel by viewModel.aodDimnessLevel.collectAsStateWithLifecycle()
    val aodLockTimeout by viewModel.aodLockTimeout.collectAsStateWithLifecycle()

    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (betaEnhancedHeader || isGlass) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text("Safety Guard", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SettingsGroupCard(title = "Safety Guard Monitor", icon = Icons.Rounded.Lock) {
                SettingsPremiumToggleItem(
                    title = "System Safety Watch",
                    subtitle = "Monitor settings state conflicts and offer smart ecosystem guidelines",
                    checked = safetyPinEnabled,
                    icon = Icons.Rounded.Lock,
                    onCheckedChange = { viewModel.updateSafetyPinEnabled(it) }
                )

                androidx.compose.animation.AnimatedVisibility(visible = safetyPinEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                        SettingsPremiumToggleItem(
                            title = "Active Conflict Warnings",
                            subtitle = "Immediate warning banner if toggles physically oppose each other structurally",
                            checked = safetyPinConflictWarning,
                            onCheckedChange = { viewModel.updateSafetyPinConflictWarning(it) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                        SettingsPremiumToggleItem(
                            title = "Aesthetic Recommendations",
                            subtitle = "Suggest complementary layout features whenever core styles change",
                            checked = safetyPinRecommendations,
                            onCheckedChange = { viewModel.updateSafetyPinRecommendations(it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsGroupCard(title = "Focus & AOD Safety Rules", icon = Icons.Rounded.Timer) {
                SettingsPremiumToggleItem(
                    title = "True Black OLED Focus",
                    subtitle = "AOD focus screen will use solid #000000 pixels to conserve battery power on OLED hardware",
                    checked = aodTrueBlackOled,
                    icon = Icons.Rounded.Timer,
                    onCheckedChange = { viewModel.updateAodTrueBlackOled(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                SettingsPremiumToggleItem(
                    title = "Auto-Deactivate with Bright Themes",
                    subtitle = "Automatically replace True Black with a beautifully dimmed themed focus screen when using Light theme, dynamic layouts, or Glass UI",
                    checked = aodAutoDeactivateTrueBlack,
                    onCheckedChange = { viewModel.updateAodAutoDeactivateTrueBlack(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Pixel Burn-In Shift Interval",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Period taken before slightly shifting always-on focus layout items slightly to protect modern screen pixels from permanent burn-in",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(5, 10, 20, 30).forEach { seconds ->
                            item {
                                FilterChip(
                                    selected = aodBurnInShiftSpeed == seconds,
                                    onClick = { viewModel.updateAodBurnInShiftSpeed(seconds) },
                                    label = { Text("$seconds sec") }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                
                val aodLockScreenSupport by viewModel.aodLockScreenSupport.collectAsStateWithLifecycle()
                SettingsPremiumToggleItem(
                    title = "Lock Screen Protection",
                    subtitle = "Allows AOD to safely bypass system lock screen without permanently turning screen on, ideal for extended focus",
                    checked = aodLockScreenSupport,
                    icon = Icons.Rounded.Lock,
                    onCheckedChange = { viewModel.updateAodLockScreenSupport(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                SettingsPremiumToggleItem(
                    title = "True Always-On Display",
                    subtitle = "Draw a full-screen SYSTEM OVERLAY clock directly over lockscreens, launchers and other apps for ultimate aesthetic hardware preservation.",
                    checked = aodTrueAodEnabled,
                    icon = Icons.Rounded.CropFree,
                    onCheckedChange = { viewModel.updateAodTrueAodEnabled(it) }
                )

                androidx.compose.animation.AnimatedVisibility(visible = aodTrueAodEnabled) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val isAccessibilityRecommended = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "True AOD Integration Mode",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            // Mode 1: System Overlay
                            val isOverlaySelected = aodTrueAodMode == "overlay"
                            Card(
                                onClick = { viewModel.updateAodTrueAodMode("overlay") },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isOverlaySelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
                                                     else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isOverlaySelected) 2.dp else 1.dp,
                                    color = if (isOverlaySelected) MaterialTheme.colorScheme.primary 
                                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isOverlaySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ViewQuilt,
                                            contentDescription = null,
                                            tint = if (isOverlaySelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("System Overlay", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                            if (!isAccessibilityRecommended) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                SuggestionChip(
                                                    onClick = {},
                                                    label = { Text("Recommended", fontSize = 10.sp) },
                                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                        labelColor = MaterialTheme.colorScheme.primary
                                                    ),
                                                    border = null,
                                                    modifier = Modifier.height(20.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            "Ideal for physical battery optimization and fast system responses.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    RadioButton(selected = isOverlaySelected, onClick = { viewModel.updateAodTrueAodMode("overlay") })
                                }
                            }

                            // Mode 2: Accessibility Overlay
                            val isAccessSelected = aodTrueAodMode == "accessibility"
                            Card(
                                onClick = { viewModel.updateAodTrueAodMode("accessibility") },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isAccessSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
                                                     else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isAccessSelected) 2.dp else 1.dp,
                                    color = if (isAccessSelected) MaterialTheme.colorScheme.primary 
                                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isAccessSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Accessibility,
                                            contentDescription = null,
                                            tint = if (isAccessSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Accessibility Overlay", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                            if (isAccessibilityRecommended) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                SuggestionChip(
                                                    onClick = {},
                                                    label = { Text("Recommended", fontSize = 10.sp) },
                                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                        labelColor = MaterialTheme.colorScheme.primary
                                                    ),
                                                    border = null,
                                                    modifier = Modifier.height(20.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            "Safely render screen-saver behind system notification and secure lock controls.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    RadioButton(selected = isAccessSelected, onClick = { viewModel.updateAodTrueAodMode("accessibility") })
                                }
                            }
                        }

                        // Check permissions
                        val currentPermissionGranted = if (aodTrueAodMode == "overlay") hasOverlayPermission else hasAccessibilityPermission
                        if (!currentPermissionGranted) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Lock,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (aodTrueAodMode == "overlay") "Overlay Authorization Required" else "Accessibility Authorization Required",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (aodTrueAodMode == "overlay") {
                                            "Enable 'Draw over other apps' to allow Lumia to overlay a pure black fullscreen layout matching real OLED clocks."
                                        } else {
                                            "Enable Lumia's Accessibility Service to render the AOD safely behind system lock panels."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            if (aodTrueAodMode == "overlay") {
                                                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                                                    data = Uri.parse("package:${context.packageName}")
                                                }
                                                try { context.startActivity(intent) } catch (e: Exception) {}
                                            } else {
                                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                                try { context.startActivity(intent) } catch (e: Exception) {}
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Grant Authorization", color = androidx.compose.ui.graphics.Color.White)
                                    }
                                }
                            }
                        }

                        // If accessibility is selected & active, show secure lock option
                        if (aodTrueAodMode == "accessibility" && hasAccessibilityPermission) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                            
                            val isSecureLockEnabled = aodLockTimeout > 0
                            SettingsPremiumToggleItem(
                                title = "Secure Lock Fallback",
                                subtitle = "Increase security and protect your physical screen: programmatically locks the system after AOD commences.",
                                checked = isSecureLockEnabled,
                                onCheckedChange = { isChecked ->
                                    viewModel.updateAodLockTimeout(if (isChecked) 30 else 0)
                                }
                            )

                            if (isSecureLockEnabled) {
                                Text(
                                    text = "Screen Lock Timeout Timer",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(5, 15, 30, 60, 120).forEach { seconds ->
                                        val isSelected = aodLockTimeout == seconds
                                        Card(
                                            onClick = { viewModel.updateAodLockTimeout(seconds) },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary 
                                                                 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${seconds}s",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "⚠️ FORMAL COMPLIANCE WARNINGS",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "• PRIVACY NOTATION: Accessibility services are strictly utilized locally to issue standard lock actions. Lumia guarantees 100% data offline processing and never logs user touch inputs or credentials.\n" +
                                                   "• BIOMETRIC COOLDOWN: Android OS rules state programmatic accessibility locking might sometimes prompt lock PIN fallback during your device's next unlock instead of fingerprint recognition.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))

                        // AOD Wakeup / Deactivation Sensitivity
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "AOD Wake-Up Sensitivity",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Control physical contact and motion thresholds required to wake up from Always-On Display.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            val sensitivityOptions = listOf(
                                Pair("motion", "Max (Motion & Tap)"),
                                Pair("highest", "High (Single Tap)"),
                                Pair("medium", "Mid (Double Tap)"),
                                Pair("secure", "Hold (Secure)")
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                sensitivityOptions.forEach { (sens, label) ->
                                    val isSelected = aodSensitivity == sens
                                    Card(
                                        onClick = { viewModel.updateAodSensitivity(sens) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary 
                                                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label.split(" ").firstOrNull() ?: label,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Speed,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = when (aodSensitivity) {
                                            "motion" -> "Maximum: Accelerometer linear movement, tilt, shake, or single screen tap instantly wakes up Lumia."
                                            "highest" -> "High sensitivity: Any single tap anywhere on the dark screen will instantly wake it up."
                                            "medium" -> "Balanced sensitivity: Restricts wake-up to intentional double-taps to fully avoid pocket triggers."
                                            "secure" -> "Fortified safety: Requires holding touch down anywhere for 1 second to unlock back."
                                            else -> "Standard touch gesture control."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))

                        // Controllable dimness override level
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Screen Dimness Override Level",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Controls absolute pixel dimness. High darkness values scale back hardware output to support OLED and eye comfort.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            val dimnessLevels = listOf(
                                Pair(0.50f, "50%"),
                                Pair(0.75f, "75%"),
                                Pair(0.90f, "90%"),
                                Pair(0.95f, "95%"),
                                Pair(0.99f, "99%")
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                dimnessLevels.forEach { (level, percentString) ->
                                    val isSelected = aodDimnessLevel == level
                                    Card(
                                        onClick = { viewModel.updateAodDimnessLevel(level) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary 
                                                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = percentString,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedThemeScreen(navController: NavController, viewModel: ScholarViewModel) {
    val customPrimary by viewModel.customPrimary.collectAsStateWithLifecycle()
    val customPrimaryContainer by viewModel.customPrimaryContainer.collectAsStateWithLifecycle()
    val customBackground by viewModel.customBackground.collectAsStateWithLifecycle()
    val customSurface by viewModel.customSurface.collectAsStateWithLifecycle()
    val customText by viewModel.customText.collectAsStateWithLifecycle()

    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (betaEnhancedHeader || isGlass) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text("Advanced Theme", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsCategoryHeading(title = "Auto-Generate Custom Palette", icon = Icons.Rounded.Palette)
            
            Text(
                text = "Tap a preset to load a beautifully calculated theme, or enter a Primary Hex below and tap 'Generate Palette' to scientifically compute matching container, surface, background, and text shades.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                val presets = listOf(
                    Triple("Cyber Glow", "#00A896", androidx.compose.ui.graphics.Color(0xFF00A896)),
                    Triple("Amethyst", "#7B2CBF", androidx.compose.ui.graphics.Color(0xFF7B2CBF)),
                    Triple("Emerald Forest", "#2D6A4F", androidx.compose.ui.graphics.Color(0xFF2D6A4F)),
                    Triple("Sunset Orange", "#FF7043", androidx.compose.ui.graphics.Color(0xFFFF7043)),
                    Triple("Midnight Ocean", "#1A237E", androidx.compose.ui.graphics.Color(0xFF1A237E)),
                    Triple("Rose Petal", "#D81B60", androidx.compose.ui.graphics.Color(0xFFD81B60)),
                    Triple("Cappuccino", "#8D6E63", androidx.compose.ui.graphics.Color(0xFF8D6E63)),
                    Triple("Lavender Bliss", "#9575CD", androidx.compose.ui.graphics.Color(0xFF9575CD))
                )
                presets.forEach { preset ->
                    val name = preset.first
                    val hex = preset.second
                    val previewColor = preset.third
                    Surface(
                        modifier = Modifier.clickable {
                            viewModel.generatePaletteFromPrimaryHex(hex)
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = previewColor.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, previewColor.copy(alpha = 0.6f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(previewColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            SettingsCategoryHeading(title = "Fine-Tune Individual Colors", icon = Icons.Rounded.Edit)

            HexColorInputItem("Primary Shade", customPrimary) { viewModel.updateCustomColor("primary", it) }
            
            Button(
                onClick = { viewModel.generatePaletteFromPrimaryHex(customPrimary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Palette,
                    contentDescription = "Magic Wand",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Cohesive Palette from Primary", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            HexColorInputItem("Secondary/Header Shade", customPrimaryContainer) { viewModel.updateCustomColor("primary_container", it) }
            HexColorInputItem("Background Shade", customBackground) { viewModel.updateCustomColor("background", it) }
            HexColorInputItem("Surface/Panel Shade", customSurface) { viewModel.updateCustomColor("surface", it) }
            HexColorInputItem("Text Shade", customText) { viewModel.updateCustomColor("text", it) }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun HexColorInputItem(label: String, value: String, onValueChange: (String) -> Unit) {
    var textValue by remember { mutableStateOf(value) }
    
    // Sync external changes properly
    LaunchedEffect(value) {
        if (textValue != value && textValue != value.replace("#","")) {
            textValue = value
        }
    }
    
    val colorPreview = remember(textValue) {
        try {
            val formatted = if (textValue.isNotEmpty() && !textValue.startsWith("#")) "#$textValue" else textValue
            androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(formatted))
        } catch(e: Exception) {
            androidx.compose.ui.graphics.Color.Transparent
        }
    }
    
    val isError = remember(textValue) {
        try {
            val formatted = if (textValue.isNotEmpty() && !textValue.startsWith("#")) "#$textValue" else textValue
            if (formatted.length == 7 || formatted.length == 9) {
                false
            } else {
                true
            }
        } catch(e: Exception) {
            true
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = textValue,
                onValueChange = { 
                    textValue = it
                    val formatted = if (it.isNotEmpty() && !it.startsWith("#")) "#$it" else it
                    try {
                        if (formatted.length == 7 || formatted.length == 9) {
                            android.graphics.Color.parseColor(formatted)
                            onValueChange(formatted)
                        }
                    } catch(e: Exception) {
                        // Safe ignore during half-typed strings
                    }
                },
                singleLine = true,
                isError = isError,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, end = 16.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(colorPreview, shape = RoundedCornerShape(8.dp))
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
        )
    }
}

@Composable
fun SettingsGroupCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (title.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.5.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }
        }
        
        if (isGlass) {
            ovrrup.lumia.ui.components.GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    content()
                }
            }
        } else {
            androidx.compose.material3.OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun SettingsPremiumToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    enabled: Boolean = true,
    unavailableReason: String? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    val alpha = if (enabled) 1f else 0.5f
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (checked && enabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .alpha(alpha),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (checked && enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
            }
            
            Column(modifier = Modifier.alpha(alpha).padding(end = 8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!enabled && unavailableReason != null) {
                    Text(
                        text = unavailableReason,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.8f)
        )
    }
}

@Composable
fun <T> SettingsSegmentedPicker(
    title: String,
    subtitle: String,
    options: List<Triple<T, String, androidx.compose.ui.graphics.vector.ImageVector?>>,
    selected: T,
    onSelected: (T) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        if (title.isNotEmpty()) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { (value, label, icon) ->
                val isSelected = value == selected
                val bgSelected = MaterialTheme.colorScheme.primaryContainer
                val borderSelected = MaterialTheme.colorScheme.primary
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) bgSelected else androidx.compose.ui.graphics.Color.Transparent)
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) borderSelected else androidx.compose.ui.graphics.Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelected(value) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp).padding(end = 4.dp)
                            )
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BetaToolGridCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    
    val cardBg = if (checked) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isGlass) 0.35f else 0.85f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isGlass) 0.12f else 0.33f)
    }
    
    val cardBorder = if (checked) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f)
    }
    
    val contentColor = if (checked) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1.1f)
            .clip(RoundedCornerShape(22.dp))
            .background(cardBg)
            .border(width = 1.dp, color = cardBorder, shape = RoundedCornerShape(22.dp))
            .clickable { onToggle(!checked) }
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon wrapper with custom decorative colored backing
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (checked) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Fine-tuned visual indicator label or dynamic mini checkbox
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (checked) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (checked) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (checked) contentColor.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    lineHeight = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
            }
        }
    }
}

@Composable
fun SettingsActionItemInCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val contentColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isDestructive) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = contentColor)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemSettingsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val autoLinkByName by viewModel.systemAutoLinkByName.collectAsStateWithLifecycle()
    val enableSynergy by viewModel.systemEnableSynergy.collectAsStateWithLifecycle()
    val autoCreateSubject by viewModel.systemAutoCreateSubject.collectAsStateWithLifecycle()
    val fuseSubjectsCourses by viewModel.systemFuseSubjectsCourses.collectAsStateWithLifecycle()
    val advancedTasks by viewModel.systemAdvancedTasks.collectAsStateWithLifecycle()

    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (betaEnhancedHeader || isGlass) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text("System Configuration", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SettingsCategoryHeading(title = "Interconnections", icon = Icons.Rounded.Settings)

            SettingsGroupCard(title = "Course & Subject Integration", icon = Icons.Rounded.Settings) {
                SettingsPremiumToggleItem(
                    title = "Auto-Link by Name",
                    subtitle = "Automatically couple Courses and study Subjects together if they share the same name (case-insensitive) when no explicit association is set.",
                    checked = autoLinkByName,
                    icon = Icons.Rounded.Settings,
                    onCheckedChange = { viewModel.updateSystemAutoLinkByName(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsPremiumToggleItem(
                    title = "Course Synergy Score",
                    subtitle = "Measure alignments between lectures and study topics using a Dynamic Synergy Gauge in details screens.",
                    checked = enableSynergy,
                    icon = Icons.Rounded.Star,
                    onCheckedChange = { viewModel.updateSystemEnableSynergy(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsPremiumToggleItem(
                    title = "Auto-Create Associated Subject",
                    subtitle = "Automatically create a matching Study Subject whenever you enroll in/add a new academic Course.",
                    checked = autoCreateSubject,
                    icon = Icons.Rounded.School,
                    onCheckedChange = { viewModel.updateSystemAutoCreateSubject(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsPremiumToggleItem(
                    title = "Fuse Subjects & Courses",
                    subtitle = "Embed subjects within courses to simplify navigation. Turn off to display 'Subjects' as a separate bottom tab.",
                    checked = fuseSubjectsCourses,
                    icon = Icons.Rounded.MergeType,
                    onCheckedChange = { viewModel.updateSystemFuseSubjectsCourses(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsPremiumToggleItem(
                    title = "Advanced Tasks & Linkages",
                    subtitle = "Enable complex task tracking, including multi-linking with courses and assignments, plus advanced sorting and cross-referencing.",
                    checked = advancedTasks,
                    icon = Icons.Rounded.List,
                    onCheckedChange = { viewModel.updateSystemAdvancedTasks(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            SettingsCategoryHeading(title = "Pomodoro Integration", icon = Icons.Rounded.Timer)

            SettingsGroupCard(title = "Timer & Environment Behaviors", icon = Icons.Rounded.Timer) {
                SettingsPremiumToggleItem(
                    title = "Auto-Log Focus Sessions",
                    subtitle = "Automatically register and log Pomodoro 'Work' sessions into the database productivity history log upon completion.",
                    checked = viewModel.systemPomodoroAutoLog.collectAsStateWithLifecycle().value,
                    icon = Icons.Rounded.History,
                    onCheckedChange = { viewModel.updateSystemPomodoroAutoLog(it) }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                
                Column(modifier = Modifier.padding(16.dp)) {
                    val workDur by viewModel.pomodoroWorkDuration.collectAsStateWithLifecycle()
                    Text("Work Duration: $workDur mins", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    androidx.compose.material3.Slider(
                        value = workDur.toFloat(),
                        onValueChange = { viewModel.updatePomodoroWorkDuration(it.toInt()) },
                        valueRange = 5f..120f,
                        steps = 114
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val shortDur by viewModel.pomodoroShortBreakDuration.collectAsStateWithLifecycle()
                    Text("Short Break: $shortDur mins", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    androidx.compose.material3.Slider(
                        value = shortDur.toFloat(),
                        onValueChange = { viewModel.updatePomodoroShortBreakDuration(it.toInt()) },
                        valueRange = 1f..30f,
                        steps = 28
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val longDur by viewModel.pomodoroLongBreakDuration.collectAsStateWithLifecycle()
                    Text("Long Break: $longDur mins", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    androidx.compose.material3.Slider(
                        value = longDur.toFloat(),
                        onValueChange = { viewModel.updatePomodoroLongBreakDuration(it.toInt()) },
                        valueRange = 5f..60f,
                        steps = 54
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsGroupCard(title = "Period Structure", icon = Icons.Rounded.List) {
                SettingsPremiumToggleItem(
                    title = "Enable Target Periods",
                    subtitle = "Allows restricting pomodoro loops to a fixed target instead of infinite repetetion.",
                    checked = viewModel.pomodoroEnablePeriodTarget.collectAsStateWithLifecycle().value,
                    icon = Icons.Rounded.Star,
                    onCheckedChange = { viewModel.updatePomodoroEnablePeriodTarget(it) }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                
                Column(modifier = Modifier.padding(16.dp)) {
                    val periodSessions by viewModel.pomodoroPeriodSessions.collectAsStateWithLifecycle()
                    Text("Sessions per Period: $periodSessions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("Number of study/work sessions before a long break.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    androidx.compose.material3.Slider(
                        value = periodSessions.toFloat(),
                        onValueChange = { viewModel.updatePomodoroPeriodSessions(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val notifFormalTone by viewModel.notifFormalTone.collectAsStateWithLifecycle()
    val notifEnableDeadlines by viewModel.notifEnableDeadlines.collectAsStateWithLifecycle()
    val notifEnableStreaks by viewModel.notifEnableStreaks.collectAsStateWithLifecycle()
    val notifEnableClasses by viewModel.notifEnableClasses.collectAsStateWithLifecycle()
    val notifEnableDailyDigest by viewModel.notifEnableDailyDigest.collectAsStateWithLifecycle()

    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (betaEnhancedHeader || isGlass) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text("Notifications", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            ovrrup.lumia.ui.components.NotificationPermissionPanel()
            ovrrup.lumia.ui.components.ExactAlarmPermissionPanel()
            ovrrup.lumia.ui.components.BatteryOptimizationPermissionPanel()
            
            SettingsGroupCard(title = "Notification Configuration", icon = Icons.Rounded.Notifications) {
                SettingsPremiumToggleItem(
                    title = "Formal Notification Tone",
                    subtitle = if (notifFormalTone) "Notifications will sound polite and professional" else "Notifications will sound taunting and strict to push you harder!",
                    checked = notifFormalTone,
                    icon = Icons.Rounded.RecordVoiceOver,
                    onCheckedChange = { viewModel.updateNotifFormalTone(it) }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsToggleItem(
                    title = "Deadline Alerts",
                    subtitle = "Get notified before assignment and task deadlines",
                    checked = notifEnableDeadlines,
                    onCheckedChange = { viewModel.updateNotifEnableDeadlines(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsToggleItem(
                    title = "Streak Maintenance",
                    subtitle = "Warnings when streak is at risk, and alerts on updates",
                    checked = notifEnableStreaks,
                    onCheckedChange = { viewModel.updateNotifEnableStreaks(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsToggleItem(
                    title = "Daily Digest",
                    subtitle = "A combined summary of tasks & assignments for the day",
                    checked = notifEnableDailyDigest,
                    onCheckedChange = { viewModel.updateNotifEnableDailyDigest(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(navController: NavController, viewModel: ScholarViewModel) {
    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentVersion = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val vName = pInfo.versionName
            if (vName != null && vName != "1.0.0" && vName != "1.0") vName else "1.4.0"
        } catch (e: Exception) {
            "1.4.0"
        }
    }

    // Preferences for Auto Update check
    val prefs = remember { context.getSharedPreferences("lumia_prefs", android.content.Context.MODE_PRIVATE) }
    var autoCheckEnabled by remember { mutableStateOf(prefs.getBoolean("auto_check_updates", true)) }

    // Update Checker Status States: "idle", "checking", "available", "latest", "error"
    var updateState by remember { mutableStateOf("idle") }
    var updateTagName by remember { mutableStateOf("") }
    var updateError by remember { mutableStateOf("") }

    // Expandable details states
    var showTerms by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showLicense by remember { mutableStateOf(false) }

    fun runUpdateCheck() {
        updateState = "checking"
        updateError = ""
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val url = java.net.URL("https://api.github.com/repos/ovrrup/Lumia/releases/latest")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "Lumia-FOSS-App")
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                
                if (connection.responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val regex = "\"tag_name\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                    val matchResult = regex.find(responseText)
                    val tagName = matchResult?.groups?.get(1)?.value
                    
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        if (tagName != null) {
                            updateTagName = tagName
                            val currentVersionClean = currentVersion.lowercase().replace("v", "").replace("-foss", "").trim()
                            val cleanTag = tagName.lowercase().replace("v", "").replace("-foss", "").trim()
                            if (cleanTag != currentVersionClean && cleanTag.isNotEmpty()) {
                                updateState = "available"
                            } else {
                                updateState = "latest"
                            }
                        } else {
                            updateState = "error"
                            updateError = "Failed to parse release tag info."
                        }
                    }
                } else {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        updateState = "error"
                        updateError = "GitHub server status: ${connection.responseCode}"
                    }
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    updateState = "error"
                    updateError = e.localizedMessage ?: "No connection"
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (autoCheckEnabled) {
            runUpdateCheck()
        }
    }

    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (betaEnhancedHeader || isGlass) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text("About Lumia", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Logo & Brand Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.School,
                            contentDescription = "Lumia Logo",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Lumia", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    Text("v$currentVersion-foss", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "A beautiful, 100% offline-first academic tracker and focus cockpit built with Material 3 design, local Room databases, LogDog diagnostic, and the True Always-On Display saving option.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            // GitHub & Community Listings
            SettingsGroupCard(title = "Community & Sources", icon = Icons.Rounded.Star) {
                SettingsActionItemInCard(
                    title = "Creator GitHub Profile",
                    subtitle = "github.com/ovrrup",
                    icon = Icons.Rounded.Edit,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ovrrup"))
                        try { context.startActivity(intent) } catch (e: Exception) {}
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                SettingsActionItemInCard(
                    title = "GitHub Source Repository",
                    subtitle = "Join our development base or clone the code",
                    icon = Icons.Rounded.School,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ovrrup/Lumia"))
                        try { context.startActivity(intent) } catch (e: Exception) {}
                    }
                )
            }

            // Updates Manager
            SettingsGroupCard(title = "Open Source Version Control", icon = Icons.Rounded.Download) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Check Updates on Startup", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Verify latest releases from public channels", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoCheckEnabled,
                        onCheckedChange = {
                            autoCheckEnabled = it
                            prefs.edit().putBoolean("auto_check_updates", it).apply()
                        }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                // Manual Check panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { runUpdateCheck() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = updateState != "checking"
                    ) {
                        Text(if (updateState == "checking") "Checking Repository..." else "Check for Updates Now")
                    }

                    when (updateState) {
                        "checking" -> {
                            Text("Querying GitHub releases API...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        "available" -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Update Available! ($updateTagName)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "A new formal release is available. You can download the latest Lumia.apk directly from the GitHub Releases assets.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                    Button(
                                        onClick = {
                                            val intent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://github.com/ovrrup/Lumia/releases/latest")
                                            )
                                            try { context.startActivity(intent) } catch (e: Exception) {}
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Download,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Go to GitHub Releases")
                                    }
                                }
                            }
                        }
                        "latest" -> {
                            Text("✨ Lumia FOSS is up-to-date (v$currentVersion) with latest release branch.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                        }
                        "error" -> {
                            Text("⚠️ Code status: Offline or DNS limitation. Standard local database remains highly optimized.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Terms & Privacy policies
            SettingsGroupCard(title = "Legal terms", icon = Icons.Rounded.Lock) {
                // Terms
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTerms = !showTerms }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Terms & Conditions Agreement", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Read our local user accountability parameters", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Expand",
                            modifier = Modifier.scale(1f, if (showTerms) -1f else 1f)
                        )
                    }
                    if (showTerms) {
                        Card(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "1. Open Source Framework & License\n" +
                                       "Lumia is free software distributed under the GNU GPLv3 terms. You can modify and share code as long as your changes remain public under the same copyleft license.\n\n" +
                                       "2. No Warranty\n" +
                                       "Lumia is provided entirely 'as-is' without warranties of any kind. You are solely responsible for local storage backups.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                // Privacy
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPrivacy = !showPrivacy }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Local Privacy Policy", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("100% serverless, zero telemetry operations", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Expand",
                            modifier = Modifier.scale(1f, if (showPrivacy) -1f else 1f)
                        )
                    }
                    if (showPrivacy) {
                        Card(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "1. Zero Telemetry\n" +
                                       "Lumia operates completely offline. No tracking SDKs, no external ads, no user harvesting.\n\n" +
                                       "2. Safe Permission Utilization\n" +
                                       "All accessibility and overlay requests are completely handled locally on your own CPU to manage focus screen timers.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                // License
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLicense = !showLicense }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("GNU GPLv3 Open Source License", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Verification parameters of other distributions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Expand",
                            modifier = Modifier.scale(1f, if (showLicense) -1f else 1f)
                        )
                    }
                    if (showLicense) {
                        Card(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "Lumia is powered by the GNU General Public License v3.0.\n\n" +
                                       "This license grants you the freedom to run, study, share, and modify Lumia. Any public distribution of derivative versions must be open-source and preserve original contributor attributions.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

