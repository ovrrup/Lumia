package lumia.tracker.ui.screens.settings

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.GlassCapsule
import lumia.tracker.ui.components.ScholarCard
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

    val currentThemeSelection = remember(themeMode, pureBlackMode) {
        if (pureBlackMode) "Pure Black" else themeMode
    }

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

            // Streamlined Frosted Glassmorphism Theme Preview Component
            val activeColor = remember(themeColor) {
                when (themeColor) {
                    "Ocean" -> Color(0xFF3197D6)
                    "Emerald" -> Color(0xFF4BC27D)
                    "Gold" -> Color(0xFFFFC646)
                    "Rose" -> Color(0xFFE52F28)
                    "Sage" -> Color(0xFFACBDAA)
                    "Twilight" -> Color(0xFF958CE8)
                    "Custom" -> Color(0xFF999999)
                    else -> Color(0xFF3197D6)
                }
            }

            ScholarCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(22.dp),
                glassmorphic = true
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Ambient backlight glowing aura
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .align(Alignment.TopEnd)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        activeColor.copy(alpha = 0.28f),
                                        activeColor.copy(alpha = 0.08f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Mini circular glass highlight orb
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    activeColor.copy(alpha = 0.35f),
                                                    Color.Transparent
                                                )
                                            ),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(activeColor)
                                            .border(
                                                width = 1.dp,
                                                brush = Brush.verticalGradient(
                                                    listOf(
                                                        Color.White.copy(alpha = 0.70f),
                                                        Color.White.copy(alpha = 0.20f)
                                                    )
                                                ),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Glass highlight
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        0.0f to Color.White.copy(alpha = 0.45f),
                                                        0.5f to Color.Transparent
                                                    )
                                                )
                                        )
                                        Icon(
                                            imageVector = when (currentThemeSelection) {
                                                "Light" -> Icons.Rounded.LightMode
                                                "Dark" -> Icons.Rounded.DarkMode
                                                "Pure Black" -> Icons.Rounded.Contrast
                                                else -> Icons.Rounded.SettingsBrightness
                                            },
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "$themeColor Theme",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = currentThemeSelection,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Frosted Glass Capsule badge
                            GlassCapsule {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(activeColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Streamlined preview surface snippet
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
                            border = BorderStroke(
                                1.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.35f),
                                        Color.White.copy(alpha = 0.08f)
                                    )
                                )
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Lumia Tracker",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Frosted Glass Aesthetic",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = activeColor,
                                    border = BorderStroke(
                                        1.dp,
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.45f),
                                                Color.White.copy(alpha = 0.10f)
                                            )
                                        )
                                    )
                                ) {
                                    Text(
                                        text = "Live",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 1. Display Mode Card with modern capsule segmented picker (CircleShape)
            SettingsGroupCard(title = "Display Mode", icon = Icons.Rounded.DarkMode) {
                ThemeModeCapsulePicker(
                    selectedMode = currentThemeSelection,
                    onModeSelected = { mode ->
                        when (mode) {
                            "Light" -> {
                                viewModel.updateThemeMode("Light")
                                viewModel.updatePureBlackMode(false)
                            }
                            "Dark" -> {
                                viewModel.updateThemeMode("Dark")
                                viewModel.updatePureBlackMode(false)
                            }
                            "System" -> {
                                viewModel.updateThemeMode("System")
                                viewModel.updatePureBlackMode(false)
                            }
                            "Pure Black" -> {
                                viewModel.updateThemeMode("Dark")
                                viewModel.updatePureBlackMode(true)
                            }
                        }
                    }
                )
            }

            // 2. Branding & Theme Palette Card
            SettingsGroupCard(title = "Theme Color Palette", icon = Icons.Rounded.Palette) {
                Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
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

            // 3. Animation Profiles Card
            SettingsGroupCard(title = "Animations & Motion", icon = Icons.Rounded.Speed) {
                SettingsSegmentedPicker(
                    title = "",
                    subtitle = "",
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
