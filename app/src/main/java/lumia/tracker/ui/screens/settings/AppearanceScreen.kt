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
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
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
    val context = androidx.compose.ui.platform.LocalContext.current

    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
    val betaDynamicBackground by viewModel.betaDynamicBackground.collectAsStateWithLifecycle()
    val dynamicBgLightBrightness by viewModel.dynamicBgLightBrightness.collectAsStateWithLifecycle()
    val dynamicBgDarkBrightness by viewModel.dynamicBgDarkBrightness.collectAsStateWithLifecycle()
    val betaBetterTexts by viewModel.betaBetterTexts.collectAsStateWithLifecycle()
    val betaBetterTextsPalette by viewModel.betaBetterTextsPalette.collectAsStateWithLifecycle()
    val pureBlackMode by viewModel.pureBlackMode.collectAsStateWithLifecycle()
    val betaMinimalistMode by viewModel.betaMinimalistMode.collectAsStateWithLifecycle()
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val dynamicAppIcon by viewModel.dynamicAppIcon.collectAsStateWithLifecycle()
    val betaGlassUi by viewModel.betaGlassUi.collectAsStateWithLifecycle()

    val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current

    val isSystemSystemDarkForOpacity = androidx.compose.foundation.isSystemInDarkTheme()
    androidx.compose.runtime.LaunchedEffect(themeColor, themeMode) {
        val effectiveDark = themeMode == "Dark" || (themeMode == "System" && isSystemSystemDarkForOpacity)
        viewModel.refreshNavBarGlassOpacity(themeColor, effectiveDark)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 80.dp, bottom = 24.dp),
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
                
                SettingsToggleItem(
                    title = "Pure Black Canvas",
                    subtitle = "Apply solid pitch-black background inside dark render style",
                    checked = pureBlackMode,
                    icon = Icons.Rounded.DarkMode,
                    enabled = themeMode != "Light",
                    onCheckedChange = {
                        if (true) {
                            viewModel.updatePureBlackMode(it)
                        } else {
                            val msg = "Pure Black Canvas is a Appearance setting."
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
                        if (true) {
                            viewModel.updateDisplayLayoutMode(it) 
                        } else {
                            val msg = "Requires Advanced Screen Layouts."
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
                        if (true) {
                            viewModel.updateAppAnimationMode(it)
                        } else {
                            val msg = "Requires Advanced Animations."
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                SettingsToggleItem(
                    title = "More Rounds Mode",
                    subtitle = "Replace all sharp-edged geometries with bouncy, spherical rounded layouts",
                    checked = moreRounds,
                    icon = Icons.Rounded.CheckCircle,
                    onCheckedChange = {
                        if (true) {
                            viewModel.updateMoreRounds(it)
                        } else {
                            val msg = "More Rounds is a Appearance setting."
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                )

                AnimatedVisibility(visible = moreRounds) {
                    val moreRoundsMode by viewModel.moreRoundsMode.collectAsStateWithLifecycle()
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(bottom = 12.dp))
                        val roundOptions = if (betaGlassUi) {
                            listOf(
                                Triple("Pastel", "Soft Pastel", Icons.Rounded.Palette),
                                Triple("Glass", "Liquid Glass", Icons.Rounded.BlurOn)
                            )
                        } else {
                            listOf(
                                Triple("Pastel", "Soft Pastel", Icons.Rounded.Palette)
                            )
                        }
                        SettingsSegmentedPicker(
                            title = "Enhanced Rounds Style",
                            subtitle = "Select the visual approach for rounded components and buttons",
                            options = roundOptions,
                            selected = if (betaGlassUi) moreRoundsMode else "Pastel",
                            onSelected = { viewModel.updateMoreRoundsMode(it) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (!betaGlassUi || moreRoundsMode == "Pastel") 
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



            // Advanced Navigation Panel Configuration Card
            SettingsGroupCard(title = "Advanced Bottom Navigation", icon = Icons.Rounded.Settings) {
                val betaFloatingNav by viewModel.betaFloatingNav.collectAsStateWithLifecycle()
                val navBarHeight by viewModel.navBarHeight.collectAsStateWithLifecycle()
                val navBarPaddingHorizontal by viewModel.navBarPaddingHorizontal.collectAsStateWithLifecycle()
                val navBarPaddingBottom by viewModel.navBarPaddingBottom.collectAsStateWithLifecycle()
                val navBarCornerRadius by viewModel.navBarCornerRadius.collectAsStateWithLifecycle()
                val navBarLabelMode by viewModel.navBarLabelMode.collectAsStateWithLifecycle()
                val navBarIndicatorAlpha by viewModel.navBarIndicatorAlpha.collectAsStateWithLifecycle()
                val betaNavBarSizeControls by viewModel.betaNavBarSizeControls.collectAsStateWithLifecycle()

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
                                viewModel.updateThemeColor(name)
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
                SettingsToggleItem(
                    title = "Minimalist Focus Mode",
                    subtitle = "Force-off and lock complex visuals for intense studying focus",
                    checked = betaMinimalistMode,
                    icon = Icons.Rounded.Star,
                    
                    onCheckedChange = { viewModel.updateBetaMinimalistMode(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                SettingsToggleItem(
                    title = "UI-based Launcher Icon",
                    subtitle = "Match home screen app icon style with the active Lumia color scheme",
                    checked = dynamicAppIcon,
                    icon = Icons.Rounded.Palette,
                    
                    onCheckedChange = { viewModel.updateDynamicAppIcon(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                SettingsToggleItem(
                    title = "Enhanced Blur Navigation",
                    subtitle = "Apply a polished satin translucent backdrop to primary navigation header",
                    checked = betaEnhancedHeader,
                    enabled = !betaMinimalistMode ,
                    icon = Icons.Rounded.Settings,
                    onCheckedChange = { viewModel.updateBetaEnhancedHeader(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                SettingsToggleItem(
                    title = "Dynamic Lighting Background",
                    subtitle = "Soft, vibrant animated background gradient shifts",
                    checked = betaDynamicBackground,
                    enabled = !betaMinimalistMode ,
                    icon = Icons.Rounded.Check,
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
                SettingsToggleItem(
                    title = "Better Texts Rendering",
                    subtitle = "Enhance text readability, high contrasts and aesthetic typography",
                    checked = betaBetterTexts,
                    icon = Icons.Rounded.Edit,
                    enabled = !betaMinimalistMode,
                    onCheckedChange = { viewModel.updateBetaBetterTexts(it) }
                )

                AnimatedVisibility(visible = betaBetterTexts && !betaMinimalistMode) {
                    Column(modifier = Modifier.padding(start = 12.dp, top = 8.dp)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                        SettingsToggleItem(
                            title = "Complex Palette Text Shades",
                            subtitle = "Render text with warm color palette tones instead of absolute white/black",
                            checked = betaBetterTextsPalette,
                            enabled = betaBetterTexts && !betaMinimalistMode,
                            onCheckedChange = { viewModel.updateBetaBetterTextsPalette(it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    lumia.tracker.ui.components.UniversalCapsuleHeader(
        title = "Appearance & Theme",
        onBackClick = { navController.popBackStack() }
    )
}
}

@OptIn(ExperimentalMaterial3Api::class)
data class BetaFeatureDialogData(
    val title: String,
    val description: String,
    val onConfirm: () -> Unit
)


