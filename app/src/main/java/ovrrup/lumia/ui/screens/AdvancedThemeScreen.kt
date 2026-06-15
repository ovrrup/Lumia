package ovrrup.lumia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ovrrup.lumia.ui.theme.glassBar
import ovrrup.lumia.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedThemeScreen(navController: NavController, viewModel: ScholarViewModel) {
    val customPrimary by viewModel.customPrimary.collectAsStateWithLifecycle()
    val customPrimaryContainer by viewModel.customPrimaryContainer.collectAsStateWithLifecycle()
    val customBackground by viewModel.customBackground.collectAsStateWithLifecycle()
    val customSurface by viewModel.customSurface.collectAsStateWithLifecycle()
    val customText by viewModel.customText.collectAsStateWithLifecycle()

    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val isSpectraActive by viewModel.isSpectraActive.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            Box {
                if (betaEnhancedHeader || isGlass) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = RoundedCornerShape(0.dp))
                    )
                    HorizontalDivider(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text("Advanced Theme", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    ) { padding ->
        if (!isSpectraActive) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                PluginRequiredCard(
                    pluginName = "Spectra Canvas",
                    pluginDescription = "advanced color sliders, dynamic custom themes, and custom typography engines from GitHub packages",
                    onNavigateToHub = { navController.navigate("settings/plugins") }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                SettingsCategoryHeading(title = "Auto-Generate Custom Palette", icon = Icons.Rounded.Palette)
                
                Text(
                    text = "Tap a preset to load a beautifully calculated theme, or enter a Primary Hex below and tap 'Generate Palette' to scientifically compute matching container, surface, background, and text shades.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                    val presets = listOf(
                        Triple("Cyber Glow", "#00A896", androidx.compose.ui.graphics.Color(0xFF00A896)),
                        Triple("Amethyst", "#7B2CBF", androidx.compose.ui.graphics.Color(0xFF7B2CBF)),
                        Triple("Emerald Forest", "#2D6A4F", androidx.compose.ui.graphics.Color(0xFF2D6A4F)),
                        Triple("Sunset Orange", "#FF7043", androidx.compose.ui.graphics.Color(0xFFFF7043)),
                        Triple("Midnight Ocean", "#1A237E", androidx.compose.ui.graphics.Color(0xFF1A237E)),
                        Triple("Rose Petal", "#D81B60", androidx.compose.ui.graphics.Color(0xFFD81B60)),
                        Triple("Cappuccino", "#8D6E63", androidx.compose.ui.graphics.Color(0xFF8D6E63)),
                        Triple("Lavender Bliss", "#9575CD", androidx.compose.ui.graphics.Color(0xFF9575CD))
                    )
                    presets.forEach { preset ->
                        val name = preset.first
                        val hex = preset.second
                        val previewColor = preset.third
                        Surface(
                            modifier = Modifier.clickable {
                                viewModel.generatePaletteFromPrimaryHex(hex)
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = previewColor.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, previewColor.copy(alpha = 0.6f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
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
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                SettingsCategoryHeading(title = "Fine-Tune Individual Colors", icon = Icons.Rounded.Edit)

                HexColorInputItem("Primary Shade", customPrimary) { viewModel.updateCustomColor("primary", it) }
                
                Button(
                    onClick = { viewModel.generatePaletteFromPrimaryHex(customPrimary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Palette,
                        contentDescription = "Magic Wand",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Cohesive Palette from Primary", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                HexColorInputItem("Secondary/Header Shade", customPrimaryContainer) { viewModel.updateCustomColor("primary_container", it) }
                HexColorInputItem("Background Shade", customBackground) { viewModel.updateCustomColor("background", it) }
                HexColorInputItem("Surface/Panel Shade", customSurface) { viewModel.updateCustomColor("surface", it) }
                HexColorInputItem("Text Shade", customText) { viewModel.updateCustomColor("text", it) }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
