package lumia.tracker.ui.screens.settings

import androidx.compose.foundation.BorderStroke
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
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.settings.components.HexColorInputItem
import lumia.tracker.ui.screens.settings.components.SettingsGroupCard
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

            // 1. Preset Palettes
            SettingsGroupCard(title = "Curated Presets", icon = Icons.Rounded.Palette) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                    presets.forEach { (name, hex, previewColor) ->
                        Surface(
                            modifier = Modifier.clickable {
                                viewModel.generatePaletteFromPrimaryHex(hex)
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = previewColor.copy(alpha = 0.12f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = previewColor.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
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
                }
            }

            // 2. Primary Color & Auto-Generate
            SettingsGroupCard(title = "Primary Hex & Generator", icon = Icons.Rounded.ColorLens) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    HexColorInputItem("Primary", customPrimary) {
                        viewModel.updateCustomColor("primary", it)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    BouncyButton(
                        onClick = { viewModel.generatePaletteFromPrimaryHex(customPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
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

            // 4. Live Palette Preview
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

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = parsedBg,
                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Sample Preview",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = parsedText
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = parsedSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Surface Card",
                                        fontWeight = FontWeight.SemiBold,
                                        color = parsedText,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Text content preview",
                                        color = parsedText.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = parsedPrimary
                                ) {
                                    Text(
                                        text = "Action",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
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

