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
    val pureBlackMode by viewModel.pureBlackMode.collectAsStateWithLifecycle()
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


            // 6. Animation Profiles Card
            SettingsGroupCard(title = "Animations & Motion", icon = Icons.Rounded.Speed) {
                SettingsSegmentedPicker(
                    title = "Button & Screen Effects",
                    subtitle = "Choose how animations feel",
                    options = listOf(
                        Triple("Bouncy", "Bouncy", Icons.Rounded.TouchApp),
                        Triple("Dynamic", "Smooth", null),
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
