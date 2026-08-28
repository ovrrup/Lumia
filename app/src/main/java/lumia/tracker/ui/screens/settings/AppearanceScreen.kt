package lumia.tracker.ui.screens.settings

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.settings.components.*
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * AppearanceScreen - Visual, thematic, and tactile personalization hub.
 * Houses theme palettes, AMOLED pure black, dynamic background lighting, typography enhancements,
 * bottom navigation dock dimensions, and animation profiles with clean Material 3 design.
 */
@ValueScore(
    score = 92,
    importance = Importance.HIGH,
    description = "Visual, thematic, and tactile personalization hub for theme, AMOLED, lighting, and dock",
    category = "Settings"
)
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
                title = {
                    Text(
                        "Appearance & Themes",
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

            // 1. Display Mode (Dark / Light / System) Card
            SettingsGroupCard(title = "Display Mode & Contrast", icon = Icons.Rounded.DarkMode) {
                SettingsSegmentedPicker(
                    title = "Color Scheme",
                    subtitle = "",
                    options = listOf(
                        Triple("System", "System", Icons.Rounded.SettingsBrightness),
                        Triple("Light", "Light", Icons.Rounded.LightMode),
                        Triple("Dark", "Dark", Icons.Rounded.DarkMode)
                    ),
                    selected = themeMode,
                    onSelected = { viewModel.updateThemeMode(it) }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                SettingsToggleItem(
                    title = "Pure AMOLED Black",
                    subtitle = "True black surfaces for OLED screens",
                    checked = pureBlackMode,
                    icon = Icons.Rounded.Contrast,
                    onCheckedChange = { viewModel.updatePureBlackMode(it) }
                )
            }

            // 2. Branding & Theme Palette Card
            SettingsGroupCard(title = "Theme Color Palette", icon = Icons.Rounded.Palette) {
                Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
                    Text(
                        text = "Accent Color",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
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
                }

                if (themeColor == "Custom") {
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    SettingsActionItemInCard(
                        title = "Fine-Tune Custom Palette",
                        subtitle = "Custom hex color codes",
                        icon = Icons.Rounded.Edit,
                        onClick = { navController.navigate("settings/advanced_theme") }
                    )
                }
            }

            // 3. Dynamic Background Lighting Card
            SettingsGroupCard(title = "Dynamic Background Lighting", icon = Icons.Rounded.Flare) {
                SettingsToggleItem(
                    title = "Dynamic Ambient Lighting",
                    subtitle = "Animated gradient accents",
                    checked = betaDynamicBackground,
                    enabled = !betaMinimalistMode,
                    icon = Icons.Rounded.Flare,
                    onCheckedChange = { viewModel.updateBetaDynamicBackground(it) }
                )

                AnimatedVisibility(
                    visible = betaDynamicBackground && !betaMinimalistMode,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    val isDarkTheme = isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.5f
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
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
                                text = if (isDarkTheme) "Dark Glow Intensity" else "Light Glow Intensity",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            val currentBrightness = if (isDarkTheme) dynamicBgDarkBrightness else dynamicBgLightBrightness
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${(currentBrightness * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
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

            // 4. Typography & Legibility Enhancements Card
            SettingsGroupCard(title = "Typography & Text Enhancements", icon = Icons.Rounded.TextFields) {
                SettingsToggleItem(
                    title = "Enhanced Text Rendering",
                    subtitle = "Enhanced contrast & legibility",
                    checked = betaBetterTexts,
                    icon = Icons.Rounded.TextFields,
                    enabled = !betaMinimalistMode,
                    onCheckedChange = { viewModel.updateBetaBetterTexts(it) }
                )

                AnimatedVisibility(
                    visible = betaBetterTexts && !betaMinimalistMode,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(start = 12.dp, top = 4.dp)) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        SettingsToggleItem(
                            title = "Theme-Tinted Typography",
                            subtitle = "Tint headings with accent color",
                            checked = betaBetterTextsPalette,
                            enabled = betaBetterTexts && !betaMinimalistMode,
                            onCheckedChange = { viewModel.updateBetaBetterTextsPalette(it) }
                        )
                    }
                }
            }

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
                    subtitle = "",
                    options = listOf(
                        Triple("Flat", "Standard Flat", Icons.Rounded.HorizontalSplit),
                        Triple("Floating", "Floating Dock", Icons.Rounded.VerticalAlignBottom)
                    ),
                    selected = if (betaFloatingNav) "Floating" else "Flat",
                    onSelected = { viewModel.updateBetaFloatingNav(it == "Floating") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                SettingsSegmentedPicker(
                    title = "Item Labels Mode",
                    subtitle = "",
                    options = listOf(
                        Triple("Always", "Always", null),
                        Triple("Selected Only", "Selected", null),
                        Triple("Hidden", "Icons Only", null)
                    ),
                    selected = navBarLabelMode,
                    onSelected = { viewModel.updateNavBarLabelMode(it) }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                SettingsToggleItem(
                    title = "Custom Dimensions",
                    subtitle = "Adjust height, margins & radius",
                    checked = betaNavBarSizeControls,
                    icon = Icons.Rounded.Straighten,
                    onCheckedChange = { viewModel.updateBetaNavBarSizeControls(it) }
                )

                AnimatedVisibility(
                    visible = betaNavBarSizeControls,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Height Slider
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
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${navBarHeight.toInt()} dp",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Slider(
                            value = navBarHeight,
                            onValueChange = { viewModel.updateNavBarHeight(it) },
                            valueRange = 56f..96f,
                            modifier = Modifier.fillMaxWidth()
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Active item indicator pill opacity highlight
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Indicator Alpha",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${(navBarIndicatorAlpha * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Slider(
                            value = navBarIndicatorAlpha,
                            onValueChange = { viewModel.updateNavBarIndicatorAlpha(it) },
                            valueRange = 0.0f..0.5f,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (betaFloatingNav) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            // Corner radius slider
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
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${navBarCornerRadius.toInt()} dp",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Slider(
                                value = navBarCornerRadius,
                                onValueChange = { viewModel.updateNavBarCornerRadius(it) },
                                valueRange = 0f..48f,
                                modifier = Modifier.fillMaxWidth()
                            )

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            // Horizontal padding slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Horizontal Margin",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${navBarPaddingHorizontal.toInt()} dp",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Slider(
                                value = navBarPaddingHorizontal,
                                onValueChange = { viewModel.updateNavBarPaddingHorizontal(it) },
                                valueRange = 0f..48f,
                                modifier = Modifier.fillMaxWidth()
                            )

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            // Bottom lift padding slider
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
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${navBarPaddingBottom.toInt()} dp",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
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

            // 6. Tactile & Animation Profiles Card
            SettingsGroupCard(title = "Tactile Animations", icon = Icons.Rounded.Speed) {
                SettingsSegmentedPicker(
                    title = "Spring Physics",
                    subtitle = "",
                    options = listOf(
                        Triple("Bouncy", "Bouncy", Icons.Rounded.TouchApp),
                        Triple("Dynamic", "Dynamic", null),
                        Triple("Minimal", "Subtle", null),
                        Triple("Off", "Off", null)
                    ),
                    selected = appAnimationMode,
                    onSelected = { viewModel.updateAppAnimationMode(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
