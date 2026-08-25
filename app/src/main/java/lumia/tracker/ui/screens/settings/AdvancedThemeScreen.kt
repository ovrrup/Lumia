package lumia.tracker.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.screens.settings.components.HexColorInputItem
import lumia.tracker.ui.screens.settings.components.SettingsGroupCard
import lumia.tracker.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedThemeScreen(navController: NavController, viewModel: ScholarViewModel) {
    val customPrimary by viewModel.customPrimary.collectAsStateWithLifecycle()
    val customPrimaryContainer by viewModel.customPrimaryContainer.collectAsStateWithLifecycle()
    val customBackground by viewModel.customBackground.collectAsStateWithLifecycle()
    val customSurface by viewModel.customSurface.collectAsStateWithLifecycle()
    val customText by viewModel.customText.collectAsStateWithLifecycle()

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

            // Hero Guidance Card
            ScholarCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Scientific Palette Generator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Pick a preset or enter a custom hex color to compute complementary container, background, surface, and text shades.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 1. Preset Palettes
            SettingsGroupCard(title = "Curated Color Presets", icon = Icons.Rounded.Palette) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "Instant Harmonic Palettes",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap any preset below to automatically populate all color slots",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val presets = listOf(
                            Triple("Cyber Glow", "#00A896", Color(0xFF00A896)),
                            Triple("Amethyst", "#7B2CBF", Color(0xFF7B2CBF)),
                            Triple("Emerald Forest", "#2D6A4F", Color(0xFF2D6A4F)),
                            Triple("Sunset Orange", "#FF7043", Color(0xFFFF7043)),
                            Triple("Midnight Ocean", "#1A237E", Color(0xFF1A237E)),
                            Triple("Rose Petal", "#D81B60", Color(0xFFD81B60)),
                            Triple("Cappuccino", "#8D6E63", Color(0xFF8D6E63)),
                            Triple("Lavender Bliss", "#9575CD", Color(0xFF9575CD))
                        )
                        presets.forEach { (name, hex, previewColor) ->
                            Surface(
                                modifier = Modifier.clickable {
                                    viewModel.generatePaletteFromPrimaryHex(hex)
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = previewColor.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.2.dp,
                                    color = previewColor.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
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
                    }
                }
            }

            // 2. Primary Color & Auto-Generate
            SettingsGroupCard(title = "Primary Hex & Generator", icon = Icons.Rounded.ColorLens) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    HexColorInputItem("Primary Shade", customPrimary) {
                        viewModel.updateCustomColor("primary", it)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = { viewModel.generatePaletteFromPrimaryHex(customPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "Magic Wand",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Generate Cohesive Palette from Primary",
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
                    HexColorInputItem("Secondary / Header Shade", customPrimaryContainer) {
                        viewModel.updateCustomColor("primary_container", it)
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    HexColorInputItem("Background Shade", customBackground) {
                        viewModel.updateCustomColor("background", it)
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    HexColorInputItem("Surface / Panel Shade", customSurface) {
                        viewModel.updateCustomColor("surface", it)
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    HexColorInputItem("Text Shade", customText) {
                        viewModel.updateCustomColor("text", it)
                    }
                }
            }

            // 4. Live Palette Preview
            SettingsGroupCard(title = "Live Palette Preview", icon = Icons.Rounded.Visibility) {
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

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = parsedBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Theme Canvas Sample",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = parsedText
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = parsedSurface,
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Sample Surface Card",
                                        fontWeight = FontWeight.SemiBold,
                                        color = parsedText,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Subtle card content text preview",
                                        color = parsedText.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = parsedPrimary
                                ) {
                                    Text(
                                        text = "Action",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
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
