package ovrrup.lumia.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ovrrup.lumia.ui.components.BouncyButton
import ovrrup.lumia.ui.components.BouncyIconButton
import ovrrup.lumia.ui.components.GlassCard
import ovrrup.lumia.ui.theme.glassBar
import ovrrup.lumia.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
    val betaGlassUi by viewModel.betaGlassUi.collectAsStateWithLifecycle()
    val isAdvancedMode by viewModel.advancedSettingsEnabled.collectAsStateWithLifecycle()

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
                    title = { Text("Settings Hub", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        BouncyIconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
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

            // Premium Academy Engine Info Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Device info",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Lumia Engine Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                              ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Text(
                                    text = "System active & synchronized",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ACTIVE SCHEME",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$themeColor Palette",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "ENGINE RENDER",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (betaGlassUi) "High-Fi Glass" else "Elegant Flat",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Experience Mode ---
            SettingsGroupCard(
                title = "Application Control",
                icon = Icons.Rounded.Settings
            ) {
                SettingsPremiumToggleItem(
                    title = "Advanced Designer Mode",
                    subtitle = "Grant access to deep UI engine variables and granular layout sliders",
                    checked = isAdvancedMode,
                    icon = Icons.Rounded.Speed,
                    onCheckedChange = { viewModel.updateAdvancedSettingsEnabled(it) }
                )
                
                AnimatedVisibility(visible = isAdvancedMode) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp, end = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info, 
                                contentDescription = null, 
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Advanced settings are now distributed across relevant categories below.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            SettingsGroupCard(title = "Personalization", icon = Icons.Rounded.Palette) {
                SettingsActionItemInCard(
                    title = "Appearance & Theme",
                    subtitle = "Themes, color palettes, and layout modifiers",
                    icon = Icons.Rounded.Palette,
                    onClick = { navController.navigate("settings/appearance") }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                SettingsActionItemInCard(
                    title = "Typography & Fonts",
                    subtitle = "Dynamic typography and custom font imports",
                    icon = Icons.Rounded.FontDownload,
                    onClick = { navController.navigate("settings/typography") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // System configuration
            SettingsGroupCard(title = "System Details", icon = Icons.Rounded.Settings) {
                SettingsActionItemInCard(
                    title = "System Configuration",
                    subtitle = "Advanced background features and interconnections",
                    icon = Icons.Rounded.Settings,
                    onClick = { navController.navigate("settings/system") }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                
                SettingsActionItemInCard(
                    title = "Experimental Features",
                    subtitle = "Quick tools and layouts",
                    icon = Icons.Rounded.Check,
                    onClick = { navController.navigate("settings/beta") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Safety & Notifications
            SettingsGroupCard(title = "Alerts & Security", icon = Icons.Rounded.Lock) {
                SettingsActionItemInCard(
                    title = "Safety System Guard",
                    subtitle = "Automatic alerts and smart recommendations",
                    icon = Icons.Rounded.Lock,
                    onClick = { navController.navigate("settings/safety") }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "Notifications Management",
                    subtitle = "Tones, schedules, and active alerts",
                    icon = Icons.Rounded.Notifications,
                    onClick = { navController.navigate("settings/notifications") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Data Management
            SettingsGroupCard(title = "Storage & Versioning", icon = Icons.Rounded.Storage) {
                SettingsActionItemInCard(
                    title = "Database & Management",
                    subtitle = "Manage secure active backups, exports & resets",
                    icon = Icons.Rounded.Storage,
                    onClick = { navController.navigate("settings/data") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "About Lumia (FOSS)",
                    subtitle = "Developer info, GNU license text & update status",
                    icon = Icons.Rounded.Info,
                    onClick = { navController.navigate("settings/about") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
