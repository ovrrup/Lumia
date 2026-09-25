package lumia.tracker.ui.screens.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.settings.components.*
import lumia.tracker.viewmodel.ScholarViewModel

@ValueScore(
    score = 88,
    importance = Importance.HIGH,
    description = "Scientific hex palette generator and live preview canvas for custom themes",
    category = "Settings"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedThemeScreen(navController: NavController, viewModel: ScholarViewModel) {
    val customPrimary by viewModel.customPrimary.collectAsStateWithLifecycle()
    val customPrimaryContainer by viewModel.customPrimaryContainer.collectAsStateWithLifecycle()
    val customBackground by viewModel.customBackground.collectAsStateWithLifecycle()
    val customSurface by viewModel.customSurface.collectAsStateWithLifecycle()
    val customText by viewModel.customText.collectAsStateWithLifecycle()

    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val pureBlackMode by viewModel.pureBlackMode.collectAsStateWithLifecycle()

    val currentThemeSelection = remember(themeMode, pureBlackMode) {
        if (pureBlackMode) "Pure Black" else themeMode
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Custom Color Palette",
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
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Preset Palettes with circular glass highlights and glowing auras
            SettingsGroupCard(title = "Curated Presets", icon = Icons.Rounded.Palette) {
                val presets = listOf(
                    Triple("Cyber Glow", "#00A896", Color(0xFF00A896)),
                    Triple("Amethyst", "#7B2CBF", Color(0xFF7B2CBF)),
                    Triple("Emerald", "#2D6A4F", Color(0xFF2D6A4F)),
                    Triple("Sunset", "#FF7043", Color(0xFFFF7043)),
                    Triple("Midnight", "#1A237E", Color(0xFF1A237E)),
                    Triple("Rose", "#D81B60", Color(0xFFD81B60)),
                    Triple("Cappuccino", "#8D6E63", Color(0xFF8D6E63)),
                    Triple("Lavender", "#9575CD", Color(0xFF9575CD))
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(presets) { (name, hex, previewColor) ->
                        val isSelected = customPrimary.equals(hex, ignoreCase = true)
                        ModernPresetColorSwatch(
                            name = name,
                            hex = hex,
                            color = previewColor,
                            isSelected = isSelected,
                            onClick = {
                                viewModel.generatePaletteFromPrimaryHex(hex)
                            }
                        )
                    }
                }
            }

            // 2. Primary Color & Auto-Generate
            SettingsGroupCard(title = "Primary Hex & Generator", icon = Icons.Rounded.ColorLens) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    HexColorInputItem("Primary", customPrimary) {
                        viewModel.updateCustomColor("primary", it)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    BouncyButton(
                        onClick = { viewModel.generatePaletteFromPrimaryHex(customPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Generate Harmonic Palette",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 3. Fine-Tune Individual Colors
            SettingsGroupCard(title = "Fine-Tune Individual Tones", icon = Icons.Rounded.Tune) {
                Column(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HexColorInputItem("Container", customPrimaryContainer) {
                        viewModel.updateCustomColor("primary_container", it)
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    HexColorInputItem("Background", customBackground) {
                        viewModel.updateCustomColor("background", it)
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    HexColorInputItem("Surface", customSurface) {
                        viewModel.updateCustomColor("surface", it)
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    HexColorInputItem("Text", customText) {
                        viewModel.updateCustomColor("text", it)
                    }
                }
            }

            // 4. Live Palette Preview with Frosted Glassmorphism
            SettingsGroupCard(title = "Live Preview", icon = Icons.Rounded.Visibility) {
                val parsedPrimary = remember(customPrimary) {
                    try { Color(android.graphics.Color.parseColor(customPrimary)) } catch (e: Exception) { Color(0xFF3197D6) }
                }
                val parsedContainer = remember(customPrimaryContainer) {
                    try { Color(android.graphics.Color.parseColor(customPrimaryContainer)) } catch (e: Exception) { Color(0xFFD6E4FF) }
                }
                val parsedBg = remember(customBackground) {
                    try { Color(android.graphics.Color.parseColor(customBackground)) } catch (e: Exception) { Color(0xFFF8F9FA) }
                }
                val parsedSurface = remember(customSurface) {
                    try { Color(android.graphics.Color.parseColor(customSurface)) } catch (e: Exception) { Color(0xFFFFFFFF) }
                }
                val parsedText = remember(customText) {
                    try { Color(android.graphics.Color.parseColor(customText)) } catch (e: Exception) { Color(0xFF1C1B1F) }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Capsule Theme Mode Picker
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

                    // Streamlined Frosted Glassmorphism Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(parsedBg)
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.35f),
                                        Color.White.copy(alpha = 0.08f)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(14.dp)
                    ) {
                        // Ambient radial glow behind preview
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .align(Alignment.TopEnd)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            parsedPrimary.copy(alpha = 0.24f),
                                            parsedPrimary.copy(alpha = 0.06f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Top glass capsule badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = parsedContainer.copy(alpha = 0.70f),
                                    border = BorderStroke(
                                        0.75.dp,
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.40f),
                                                Color.White.copy(alpha = 0.10f)
                                            )
                                        )
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .background(parsedPrimary, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Active Palette",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = parsedText
                                        )
                                    }
                                }

                                Text(
                                    text = customPrimary.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = parsedText.copy(alpha = 0.6f)
                                )
                            }

                            // Frosted glass interactive card
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = parsedSurface.copy(alpha = 0.75f),
                                border = BorderStroke(
                                    1.dp,
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.45f),
                                            Color.White.copy(alpha = 0.12f)
                                        )
                                    )
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Study Focus",
                                            fontWeight = FontWeight.Bold,
                                            color = parsedText,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "2h 45m streak",
                                            color = parsedText.copy(alpha = 0.65f),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = parsedPrimary,
                                        border = BorderStroke(
                                            1.dp,
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color.White.copy(alpha = 0.40f),
                                                    Color.White.copy(alpha = 0.10f)
                                                )
                                            )
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.PlayArrow,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Start",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * ModernPresetColorSwatch - Circular glass highlight swatch with subtle glowing aura
 * for curated preset palettes in AdvancedThemeScreen.
 */
@Composable
private fun ModernPresetColorSwatch(
    name: String,
    hex: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "preset_scale_$hex"
    )

    val auraAlpha = if (isSelected) 0.40f else 0.12f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        // Outer subtle glowing aura
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = auraAlpha),
                            color.copy(alpha = auraAlpha * 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Main circular swatch with frosted glass border
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        brush = Brush.verticalGradient(
                            colors = if (isSelected) {
                                listOf(
                                    Color.White.copy(alpha = 0.90f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.70f)
                                )
                            } else {
                                listOf(
                                    Color.White.copy(alpha = 0.45f),
                                    Color.White.copy(alpha = 0.12f)
                                )
                            }
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Specular circular glass highlight overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color.White.copy(alpha = 0.40f),
                                0.45f to Color.White.copy(alpha = 0.12f),
                                0.85f to Color.Transparent
                            )
                        )
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.35f),
                                        Color.Black.copy(alpha = 0.45f)
                                    )
                                ),
                                CircleShape
                            )
                            .border(0.75.dp, Color.White.copy(alpha = 0.65f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


