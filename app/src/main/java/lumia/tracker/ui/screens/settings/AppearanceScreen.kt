package lumia.tracker.ui.screens

import lumia.tracker.service.AodAccessibilityService
import lumia.tracker.util.TrueAodManager
import android.content.Intent
import android.provider.Settings
import lumia.tracker.ui.theme.liquidGlass
import lumia.tracker.ui.theme.glassBar
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SwapHoriz
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
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.InvertColors
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
import lumia.tracker.viewmodel.ScholarViewModel
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(navController: NavController, viewModel: ScholarViewModel) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val isPremiumUnlocked = activeProfile.isFeatureUnlocked("feat_theme_pack") || activeProfile.isFeatureUnlocked("feat_custom_theme")
    val context = androidx.compose.ui.platform.LocalContext.current

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

    val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current

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
                        BouncyIconButton(onClick = { navController.popBackStack() }) {
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
                    onCheckedChange = {
                        if (activeProfile.isFeatureUnlocked("feat_theme_pack")) {
                            viewModel.updatePureBlackMode(it)
                        } else {
                            val msg = if (activeProfile.gamificationEnabled) "Pure Black Canvas is a premium Appearance setting. Unlock Theme Pack Expansion in the Plus Shop!" else "Enable Gamification to unlock Appearance premium settings."
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
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
                    onSelected = { 
                        if (activeProfile.isFeatureUnlocked("feat_screen_layout")) {
                            viewModel.updateDisplayLayoutMode(it) 
                        } else {
                            val msg = if (activeProfile.gamificationEnabled) "Requires unlocking Advanced Screen Layouts in Plus Shop" else "Enable Gamification to unlock Advanced Screen Layouts."
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
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
                    onSelected = { 
                        if (activeProfile.isFeatureUnlocked("feat_animations")) {
                            viewModel.updateAppAnimationMode(it)
                        } else {
                            val msg = if (activeProfile.gamificationEnabled) "Requires unlocking Advanced Animations in Plus Shop" else "Enable Gamification to unlock Advanced Animations."
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                SettingsPremiumToggleItem(
                    title = "More Rounds Mode",
                    subtitle = "Replace all sharp-edged geometries with bouncy, spherical rounded layouts",
                    checked = moreRounds,
                    icon = Icons.Rounded.CheckCircle,
                    onCheckedChange = {
                        if (activeProfile.isFeatureUnlocked("feat_theme_pack")) {
                            viewModel.updateMoreRounds(it)
                        } else {
                            val msg = if (activeProfile.gamificationEnabled) "More Rounds is a premium Appearance setting. Unlock Theme Pack Expansion in the Plus Shop!" else "Enable Gamification to unlock Appearance premium settings."
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                )

                AnimatedVisibility(visible = moreRounds) {
                    val moreRoundsMode by viewModel.moreRoundsMode.collectAsStateWithLifecycle()
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(bottom = 12.dp))
                        SettingsSegmentedPicker(
                            title = "Enhanced Rounds Style",
                            subtitle = "Select the visual approach for rounded components and buttons",
                            options = listOf(
                                Triple("Pastel", "Soft Pastel", Icons.Rounded.Palette),
                                Triple("Glass", "Liquid Glass", Icons.Rounded.BlurOn)
                            ),
                            selected = moreRoundsMode,
                            onSelected = { viewModel.updateMoreRoundsMode(it) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (moreRoundsMode == "Pastel") 
                                "Buttons will use high-contrast pastel colors with hidden outlines and deep elastic animations."
                                else "Buttons will gain glass-like translucency and adapt dynamically to the active background.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 2. Glass UI Engine Card (Animated entry)
            AnimatedVisibility(visible = !betaMinimalistMode) {
                SettingsGroupCard(title = "Aesthetic Glass Engine", icon = Icons.Rounded.Palette) {
                    SettingsPremiumToggleItem(
                        title = "Frosted Glass UI",
                        subtitle = "Enable premium translucent glass textures across screen panels",
                        checked = betaGlassUi,
                        icon = Icons.Rounded.Palette,
                        onCheckedChange = {
                            if (activeProfile.isFeatureUnlocked("feat_theme_pack")) {
                                viewModel.updateBetaGlassUi(it)
                            } else {
                                val msg = if (activeProfile.gamificationEnabled) "Frosted Glass is a premium Appearance setting. Unlock Theme Pack Expansion in the Plus Shop!" else "Enable Gamification to unlock Appearance premium settings."
                                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
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
                val betaNavBarSizeControls by viewModel.betaNavBarSizeControls.collectAsStateWithLifecycle()
                val navBarGlassLinkedToMain by viewModel.navBarGlassLinkedToMain.collectAsStateWithLifecycle()
                val navBarGlassBackdropStyle by viewModel.navBarGlassBackdropStyle.collectAsStateWithLifecycle()
                val navBarGlassDynamic by viewModel.navBarGlassDynamic.collectAsStateWithLifecycle()

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
                    onCheckedChange = {
                        if (activeProfile.unlockedFeatures.contains("feat_theme_pack")) {
                            viewModel.updateNavBarGlassForceEnabled(it)
                        } else {
                            val msg = if (activeProfile.gamificationEnabled) "Independent Glass is a premium Appearance setting. Unlock Theme Pack Expansion in the Plus Shop!" else "Enable Gamification to unlock Appearance premium settings."
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                )

                val isGlassTheme = lumia.tracker.ui.theme.LocalGlassMode.current
                val isNavBarGlassActive = isGlassTheme || navBarGlassForceEnabled

                if (isNavBarGlassActive) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                    // Sync with Dynamic Glass UI Toggle
                    SettingsPremiumToggleItem(
                        title = "Sync with Global Glass Style",
                        subtitle = "Link the bottom navigation bar color, style, and glass type directly to the system-wide Glass UI theme setting.",
                        checked = navBarGlassLinkedToMain,
                        icon = Icons.Rounded.Link,
                        onCheckedChange = { viewModel.updateNavBarGlassLinkedToMain(it) }
                    )

                    if (!navBarGlassLinkedToMain) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                        // Navbar Glass Type segmented chooser
                        SettingsSegmentedPicker(
                            title = "Navbar Backdrop Style",
                            subtitle = "Adjust the glass texture from solid translucent background to completely clear dynamic panel",
                            options = listOf(
                                Triple("Solid", "Solid Color", null),
                                Triple("Translucent", "Frosted Glass", null),
                                Triple("Clear", "Totally Clear", null)
                            ),
                            selected = navBarGlassBackdropStyle,
                            onSelected = { viewModel.updateNavBarGlassBackdropStyle(it) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                        // Navbar Dynamic Color Tinting Toggle
                        SettingsPremiumToggleItem(
                            title = "Ambient Accent Tinting",
                            subtitle = "Infuse primary theme color highlight directly into the navigation glass backplane rendering.",
                            checked = navBarGlassDynamic,
                            icon = Icons.Rounded.InvertColors,
                            onCheckedChange = { viewModel.updateNavBarGlassDynamic(it) }
                        )
                    }
                }

                if (betaNavBarSizeControls) {
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
                            onClick = {
                                val starterTheme = activeProfile.starterTheme.ifBlank { "Ocean" }
                                val isUniversalFree = name == starterTheme || name == "Dynamic"
                                if (name == "Custom") {
                                    if (activeProfile.unlockedFeatures.contains("feat_custom_theme")) {
                                        viewModel.updateThemeColor(name)
                                    } else {
                                        val msg = if (activeProfile.gamificationEnabled) "Requires unlocking Custom Themes in Plus Shop" else "Enable Gamification to unlock Custom Themes."
                                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } else if (!isUniversalFree && !activeProfile.unlockedFeatures.contains("feat_theme_pack")) {
                                    val msg = if (activeProfile.gamificationEnabled) "Requires Theme Pack Expansion in Plus Shop! Only your selected starter theme ($starterTheme) is unlocked from the start." else "Enable Gamification to unlock Theme Pack Expansion."
                                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                                } else {
                                    viewModel.updateThemeColor(name)
                                }
                            }
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
                val isMinimalistUnlocked = activeProfile.isFeatureUnlocked("feat_minimal_ui")
                SettingsPremiumToggleItem(
                    title = "Minimalist Focus Mode",
                    subtitle = "Force-off and lock complex visuals for intense studying focus",
                    checked = betaMinimalistMode,
                    icon = Icons.Rounded.Star,
                    enabled = isMinimalistUnlocked,
                    unavailableReason = if (!isMinimalistUnlocked) { if (activeProfile.gamificationEnabled) "Locked. Purchase in Plus Shop or Reach Level 15." else "Locked. Enable Gamification to unlock." } else null,
                    onCheckedChange = { viewModel.updateBetaMinimalistMode(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                val isDynamicIconUnlocked = activeProfile.isFeatureUnlocked("feat_ui_icon")
                SettingsPremiumToggleItem(
                    title = "UI-based Launcher Icon",
                    subtitle = "Match home screen app icon style with the active Lumia color scheme",
                    checked = dynamicAppIcon,
                    icon = Icons.Rounded.Palette,
                    enabled = isDynamicIconUnlocked,
                    unavailableReason = if (!isDynamicIconUnlocked) { if(activeProfile.gamificationEnabled) "Locked. Purchase in Plus Shop or Reach Level 6." else "Locked. Enable Gamification to unlock." } else null,
                    onCheckedChange = { viewModel.updateDynamicAppIcon(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                val isEnhancedBlurUnlocked = activeProfile.isFeatureUnlocked("feat_enhanced_blur")
                SettingsPremiumToggleItem(
                    title = "Enhanced Blur Navigation",
                    subtitle = "Apply a polished satin translucent backdrop to primary navigation header",
                    checked = betaEnhancedHeader,
                    enabled = !betaMinimalistMode && isEnhancedBlurUnlocked,
                    icon = Icons.Rounded.Settings,
                    unavailableReason = if (!isEnhancedBlurUnlocked) { if(activeProfile.gamificationEnabled) "Locked. Purchase in Plus Shop or Reach Level 10." else "Locked. Enable Gamification to unlock." } else "Locked by Minimalist Focus Mode.",
                    onCheckedChange = { viewModel.updateBetaEnhancedHeader(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                val isDynamicLightingUnlocked = activeProfile.isFeatureUnlocked("feat_dynamic_lighting")
                SettingsPremiumToggleItem(
                    title = "Dynamic Lighting Background",
                    subtitle = "Soft, vibrant animated background gradient shifts",
                    checked = betaDynamicBackground,
                    enabled = !betaMinimalistMode && isDynamicLightingUnlocked,
                    icon = Icons.Rounded.Check,
                    unavailableReason = if (!isDynamicLightingUnlocked) { if(activeProfile.gamificationEnabled) "Locked. Purchase in Plus Shop or Reach Level 13." else "Locked. Enable Gamification to unlock." } else "Locked by Minimalist Focus Mode.",
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
