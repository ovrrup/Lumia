package lumia.tracker.ui.screens.settings

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.screens.settings.components.*
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * AppearanceScreen - Comprehensive visual, thematic, and tactile personalization hub.
 * Houses theme palettes, AMOLED pure black, dynamic background lighting, typography enhancements,
 * bottom navigation dock dimensions, and animation profiles with zero glassmorphism clutter.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(navController: NavController, viewModel: ScholarViewModel) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()

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
    val appAnimationMode by viewModel.appAnimationMode.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Appearance & Themes", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    BouncyIconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
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
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Theme Mode (Dark / Light / System) Card
            SettingsGroupCard(title = "Display Mode & AMOLED Contrast", icon = Icons.Rounded.DarkMode) {
                SettingsSegmentedPicker(
                    title = "Color Scheme Mode",
                    subtitle = "Switch between dark, light, or follow system theme",
                    options = listOf(
                        Triple("System", "System Follow", Icons.Rounded.Settings),
                        Triple("Light", "Day Light", Icons.Rounded.Check),
                        Triple("Dark", "Night Dark", Icons.Rounded.DarkMode)
                    ),
                    selected = themeMode,
                    onSelected = { viewModel.updateThemeMode(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

                SettingsToggleItem(
                    title = "Pure AMOLED Black",
                    subtitle = "Maximize battery savings and OLED contrast with true #000000 black",
                    checked = pureBlackMode,
                    icon = Icons.Rounded.Contrast,
                    onCheckedChange = { viewModel.updatePureBlackMode(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Branding & Theme Palette Card
            SettingsGroupCard(title = "Theme Color Palette", icon = Icons.Rounded.Palette) {
                Text(
                    text = "Active Theme Accent",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Personalize the primary hue used across all academic components",
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
                        "Ocean" to Color(0xFF3197D6),
                        "Emerald" to Color(0xFF4BC27D),
                        "Gold" to Color(0xFFFFC646),
                        "Rose" to Color(0xFFE52F28),
                        "Sage" to Color(0xFFACBDAA),
                        "Twilight" to Color(0xFF958CE8),
                        "Custom" to Color(0xFF999999)
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        palettes.add(0, "Dynamic" to Color(0xFF909090))
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
                        title = "Fine-Tune Custom Hex Palette",
                        subtitle = "Deep customize specific hex color codes",
                        icon = Icons.Rounded.Edit,
                        onClick = { navController.navigate("settings/advanced_theme") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Dynamic Background Lighting Card
            SettingsGroupCard(title = "Dynamic Background Lighting", icon = Icons.Rounded.Flare) {
                SettingsToggleItem(
                    title = "Dynamic Ambient Lighting",
                    subtitle = "Soft, responsive animated background gradient accents tailored to the theme",
                    checked = betaDynamicBackground,
                    enabled = !betaMinimalistMode,
                    icon = Icons.Rounded.Flare,
                    onCheckedChange = { viewModel.updateBetaDynamicBackground(it) }
                )

                AnimatedVisibility(visible = betaDynamicBackground && !betaMinimalistMode) {
                    val isDarkTheme = isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.5f
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
                                text = if (isDarkTheme) "Dark Mode Glow Intensity" else "Light Mode Glow Intensity",
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
                                "Calibrate ambient glow intensity in dark modes for optimal readability"
                            } else {
                                "Calibrate background energy in light modes for clean visual focus"
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

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Typography & Legibility Enhancements Card
            SettingsGroupCard(title = "Typography & Text Enhancements", icon = Icons.Rounded.Edit) {
                SettingsToggleItem(
                    title = "Enhanced Text Rendering",
                    subtitle = "Boost font weight contrast, line spacing, and optimal readability",
                    checked = betaBetterTexts,
                    icon = Icons.Rounded.Edit,
                    enabled = !betaMinimalistMode,
                    onCheckedChange = { viewModel.updateBetaBetterTexts(it) }
                )

                AnimatedVisibility(visible = betaBetterTexts && !betaMinimalistMode) {
                    Column(modifier = Modifier.padding(start = 12.dp, top = 8.dp)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                        SettingsToggleItem(
                            title = "Theme-Tinted Typography",
                            subtitle = "Subtly infuse primary theme tones into header and title text",
                            checked = betaBetterTextsPalette,
                            enabled = betaBetterTexts && !betaMinimalistMode,
                            onCheckedChange = { viewModel.updateBetaBetterTextsPalette(it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Advanced Bottom Navigation Dock Card
            SettingsGroupCard(title = "Bottom Navigation Dock", icon = Icons.Rounded.ViewStream) {
                val betaFloatingNav by viewModel.betaFloatingNav.collectAsStateWithLifecycle()
                val navBarHeight by viewModel.navBarHeight.collectAsStateWithLifecycle()
                val navBarPaddingHorizontal by viewModel.navBarPaddingHorizontal.collectAsStateWithLifecycle()
                val navBarPaddingBottom by viewModel.navBarPaddingBottom.collectAsStateWithLifecycle()
                val navBarCornerRadius by viewModel.navBarCornerRadius.collectAsStateWithLifecycle()
                val navBarLabelMode by viewModel.navBarLabelMode.collectAsStateWithLifecycle()
                val navBarIndicatorAlpha by viewModel.navBarIndicatorAlpha.collectAsStateWithLifecycle()
                val betaNavBarSizeControls by viewModel.betaNavBarSizeControls.collectAsStateWithLifecycle()

                SettingsSegmentedPicker(
                    title = "Navigation Layout",
                    subtitle = "Switch layout format between standard flat and suspended floating dock",
                    options = listOf(
                        Triple("Flat", "Standard Flat", null),
                        Triple("Floating", "Floating Dock", Icons.Rounded.Star)
                    ),
                    selected = if (betaFloatingNav) "Floating" else "Flat",
                    onSelected = { viewModel.updateBetaFloatingNav(it == "Floating") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                SettingsSegmentedPicker(
                    title = "Item Labels Mode",
                    subtitle = "Control visibility of tab titles on the navigation bar",
                    options = listOf(
                        Triple("Always", "Always", null),
                        Triple("Selected Only", "Selected", null),
                        Triple("Hidden", "Icons Only", null)
                    ),
                    selected = navBarLabelMode,
                    onSelected = { viewModel.updateNavBarLabelMode(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                SettingsToggleItem(
                    title = "Custom Dimension Controls",
                    subtitle = "Fine-tune dock height, corner radius, and lift margins",
                    checked = betaNavBarSizeControls,
                    icon = Icons.Rounded.Straighten,
                    onCheckedChange = { viewModel.updateBetaNavBarSizeControls(it) }
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
                        Slider(
                            value = navBarIndicatorAlpha,
                            onValueChange = { viewModel.updateNavBarIndicatorAlpha(it) },
                            valueRange = 0.0f..0.5f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (betaFloatingNav) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                        // Corner radius slider
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
                            Slider(
                                value = navBarCornerRadius,
                                onValueChange = { viewModel.updateNavBarCornerRadius(it) },
                                valueRange = 0f..48f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                        // Horizontal padding slider
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Horizontal Dock Margin",
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
                            Slider(
                                value = navBarPaddingHorizontal,
                                onValueChange = { viewModel.updateNavBarPaddingHorizontal(it) },
                                valueRange = 0f..48f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                        // Bottom lift padding slider
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Bottom Lift Distance",
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

            // 6. Tactile & Animation Profiles Card
            SettingsGroupCard(title = "Tactile Animations", icon = Icons.Rounded.Speed) {
                SettingsSegmentedPicker(
                    title = "Interaction Spring Physics",
                    subtitle = "Choose touch response bounciness across cards and buttons",
                    options = listOf(
                        Triple("Bouncy", "Tactile Bouncy", Icons.Rounded.Check),
                        Triple("Dynamic", "Smooth Dynamic", null),
                        Triple("Minimal", "Minimal Subtle", null),
                        Triple("Off", "Instant Off", null)
                    ),
                    selected = appAnimationMode,
                    onSelected = { viewModel.updateAppAnimationMode(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
