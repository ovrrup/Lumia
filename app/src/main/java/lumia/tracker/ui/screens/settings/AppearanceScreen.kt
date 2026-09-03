package lumia.tracker.ui.screens

import lumia.tracker.service.AodAccessibilityService
import lumia.tracker.util.TrueAodManager
import android.content.Intent
import android.provider.Settings
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
            SettingsGroupCard(title = "Theme", icon = Icons.Rounded.DarkMode) {
                // Segmented Theme selector
                SettingsSegmentedPicker(
                    title = "Theme Mode",
                    subtitle = "Choose light, dark, or follow system setting",
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
                    title = "Pure Black (AMOLED)",
                    subtitle = "Use pure black backgrounds in dark mode to save battery on OLED screens",
                    checked = pureBlackMode,
                    icon = Icons.Rounded.DarkMode,
                    enabled = themeMode != "Light",
                    onCheckedChange = { viewModel.updatePureBlackMode(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Screen Layout
            SettingsGroupCard(title = "Screen Layout", icon = Icons.Rounded.CropFree) {
                val displayLayoutMode by viewModel.displayLayoutMode.collectAsStateWithLifecycle()
                SettingsSegmentedPicker(
                    title = "Display Cutout & Insets",
                    subtitle = "Adjust content placement around camera cutouts and edges",
                    options = listOf(
                        Triple("Normal", "Standard", null),
                        Triple("Notch Optimization", "Safe Area", null),
                        Triple("Immersive", "Immersive", Icons.Rounded.Star)
                    ),
                    selected = displayLayoutMode,
                    onSelected = { viewModel.updateDisplayLayoutMode(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsGroupCard(title = "Animations & Shapes", icon = Icons.Rounded.PlayArrow) {
                val appAnimationMode by viewModel.appAnimationMode.collectAsStateWithLifecycle()
                val moreRounds by viewModel.moreRounds.collectAsStateWithLifecycle()

                SettingsSegmentedPicker(
                    title = "Animation Style",
                    subtitle = "Adjust responsiveness and spring bounce for touches and transitions",
                    options = listOf(
                        Triple("Normal", "Standard", null),
                        Triple("Dynamic", "Smooth", null),
                        Triple("Bouncy", "Bouncy", Icons.Rounded.Star)
                    ),
                    selected = appAnimationMode,
                    onSelected = { viewModel.updateAppAnimationMode(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                SettingsToggleItem(
                    title = "Extra Rounded Corners",
                    subtitle = "Increase corner roundness across cards, dialogs, and buttons",
                    checked = moreRounds,
                    icon = Icons.Rounded.CheckCircle,
                    onCheckedChange = { viewModel.updateMoreRounds(it) }
                )

                AnimatedVisibility(visible = moreRounds) {
                    val moreRoundsMode by viewModel.moreRoundsMode.collectAsStateWithLifecycle()
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(bottom = 12.dp))
                        SettingsSegmentedPicker(
                            title = "Rounded Button Style",
                            subtitle = "Choose button appearance when extra roundness is enabled",
                            options = listOf(
                                Triple("Pastel", "Pastel Accent", Icons.Rounded.Palette),
                                Triple("Glass", "Translucent Glass", Icons.Rounded.BlurOn)
                            ),
                            selected = moreRoundsMode,
                            onSelected = { viewModel.updateMoreRoundsMode(it) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (moreRoundsMode == "Pastel") 
                                "Buttons use tinted pastel accents with spring animations."
                                else "Buttons use translucent glass styling that adapts to the background.",
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
                SettingsGroupCard(title = "Glass UI Effects", icon = Icons.Rounded.Palette) {
                    SettingsToggleItem(
                        title = "Translucent Glass UI",
                        subtitle = "Apply translucent glass styling to cards and navigation surfaces",
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
                            
                            SettingsToggleItem(
                                title = "Theme Accent Tinting",
                                subtitle = "Tint glass panels with your active theme color",
                                checked = betaGlassDynamic,
                                onCheckedChange = { viewModel.updateBetaGlassDynamic(it) }
                            )

                            SettingsToggleItem(
                                title = "Frosted Blur Effect",
                                subtitle = "Add background blur behind translucent panels",
                                checked = betaFrostGlass,
                                onCheckedChange = { viewModel.updateBetaFrostGlass(it) }
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                            // Sub-segmented backdrop style
                            SettingsSegmentedPicker(
                                title = "Panel Transparency",
                                subtitle = "Select how transparent panel backgrounds should be",
                                options = listOf(
                                    Triple("Transparent", "Clear", null),
                                    Triple("Translucent", "Frosted", null),
                                    Triple("Opaque", "Opaque", null)
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
                                            text = "Panel Opacity",
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
                                        text = "Adjust transparency level for frosted panels",
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
                                        text = "Navigation Bar Opacity",
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
                                    text = "Adjust bottom navigation bar glass transparency",
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
            SettingsGroupCard(title = "Navigation Bar", icon = Icons.Rounded.Settings) {
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
                    title = "Navigation Bar Style",
                    subtitle = "Choose between a standard bottom bar or a floating pill dock",
                    options = listOf(
                        Triple("Flat", "Standard", null),
                        Triple("Floating", "Floating", Icons.Rounded.Star)
                    ),
                    selected = if (betaFloatingNav) "Floating" else "Flat",
                    onSelected = { viewModel.updateBetaFloatingNav(it == "Floating") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                // Label visibility mode
                SettingsSegmentedPicker(
                    title = "Item Labels",
                    subtitle = "Control when text labels are displayed under icons",
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
                SettingsToggleItem(
                    title = "Separate Glass Style",
                    subtitle = "Enable glass styling on the bottom bar independently of global Glass UI",
                    checked = navBarGlassForceEnabled,
                    icon = Icons.Rounded.Palette,
                    onCheckedChange = { viewModel.updateNavBarGlassForceEnabled(it) }
                )

                val isGlassTheme = lumia.tracker.ui.theme.LocalGlassMode.current
                val isNavBarGlassActive = isGlassTheme || navBarGlassForceEnabled

                if (isNavBarGlassActive) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                    // Sync with Dynamic Glass UI Toggle
                    SettingsToggleItem(
                        title = "Match Global Glass Style",
                        subtitle = "Inherit opacity and tint settings from global Glass UI",
                        checked = navBarGlassLinkedToMain,
                        icon = Icons.Rounded.Link,
                        onCheckedChange = { viewModel.updateNavBarGlassLinkedToMain(it) }
                    )

                    if (!navBarGlassLinkedToMain) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                        // Navbar Glass Type segmented chooser
                        SettingsSegmentedPicker(
                            title = "Bar Background Style",
                            subtitle = "Select solid, frosted, or clear transparency for the bar",
                            options = listOf(
                                Triple("Solid", "Solid", null),
                                Triple("Translucent", "Frosted", null),
                                Triple("Clear", "Clear", null)
                            ),
                            selected = navBarGlassBackdropStyle,
                            onSelected = { viewModel.updateNavBarGlassBackdropStyle(it) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                        // Navbar Dynamic Color Tinting Toggle
                        SettingsToggleItem(
                            title = "Theme Color Tint",
                            subtitle = "Tint the navigation bar glass with your active theme color",
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
                                text = "Bar Height",
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
                            text = "Adjust the height of the bottom navigation bar",
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
                                text = "Active Tab Indicator Opacity",
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
                            text = "Adjust background highlight opacity for the selected tab",
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
                                    text = "Corner Radius",
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
                                text = "Adjust corner roundness of the floating navigation bar",
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
                                    text = "Side Margins",
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
                                text = "Adjust horizontal spacing on either side of the floating bar",
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
                                    text = "Bottom Margin",
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
                                text = "Adjust distance between the floating bar and screen bottom",
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
            SettingsGroupCard(title = "Color Palette", icon = Icons.Rounded.Palette) {
                Text(
                    text = "Theme Palette",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Choose an accent color palette for the app",
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
                        title = "Customize Colors",
                        subtitle = "Configure custom hex colors for primary, surface, and text",
                        icon = Icons.Rounded.Edit,
                        onClick = { navController.navigate("settings/advanced_theme") }
                    )
                }
            }

            // 4. Interface Tweaks Card
            SettingsGroupCard(title = "Visual Elements", icon = Icons.Rounded.Settings) {
                SettingsToggleItem(
                    title = "Minimalist Mode",
                    subtitle = "Disable blur, gradients, and glass effects for a clean, distraction-free interface",
                    checked = betaMinimalistMode,
                    icon = Icons.Rounded.Star,
                    onCheckedChange = { viewModel.updateBetaMinimalistMode(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                SettingsToggleItem(
                    title = "Themed App Icon",
                    subtitle = "Tint the launcher icon to match your active theme color",
                    checked = dynamicAppIcon,
                    icon = Icons.Rounded.Palette,
                    onCheckedChange = { viewModel.updateDynamicAppIcon(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                SettingsToggleItem(
                    title = "Frosted Top App Bar",
                    subtitle = "Apply a translucent frosted glass backdrop to the top app bar",
                    checked = betaEnhancedHeader,
                    enabled = !betaMinimalistMode,
                    icon = Icons.Rounded.Settings,
                    onCheckedChange = { viewModel.updateBetaEnhancedHeader(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                SettingsToggleItem(
                    title = "Dynamic Background",
                    subtitle = "Display subtle animated ambient color gradients in the background",
                    checked = betaDynamicBackground,
                    enabled = !betaMinimalistMode,
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
                                text = if (isDarkTheme) "Dark Mode Background Glow" else "Light Mode Background Glow",
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
                                "Adjust background gradient brightness in dark mode"
                            } else {
                                "Adjust background gradient brightness in light mode"
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
            SettingsGroupCard(title = "Typography & Contrast", icon = Icons.Rounded.Edit) {
                SettingsToggleItem(
                    title = "Enhanced Text Contrast",
                    subtitle = "Apply optimized contrast ratios to text across the app",
                    checked = betaBetterTexts,
                    icon = Icons.Rounded.Edit,
                    enabled = !betaMinimalistMode,
                    onCheckedChange = { viewModel.updateBetaBetterTexts(it) }
                )

                AnimatedVisibility(visible = betaBetterTexts && !betaMinimalistMode) {
                    Column(modifier = Modifier.padding(start = 12.dp, top = 8.dp)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                        SettingsToggleItem(
                            title = "Theme-Tinted Text",
                            subtitle = "Subtly tint text with theme colors; turn off for pure black and white",
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
}

@OptIn(ExperimentalMaterial3Api::class)
data class BetaFeatureDialogData(
    val title: String,
    val description: String,
    val onConfirm: () -> Unit
)
