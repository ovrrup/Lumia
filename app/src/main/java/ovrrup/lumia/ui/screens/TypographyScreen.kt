package ovrrup.lumia.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ovrrup.lumia.model.CustomFont
import ovrrup.lumia.ui.components.GlassCard
import ovrrup.lumia.viewmodel.ScholarViewModel
import ovrrup.lumia.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypographyScreen(
    navController: NavController,
    viewModel: ScholarViewModel
) {
    val betaDynamicTypography by viewModel.betaDynamicTypography.collectAsStateWithLifecycle()
    val manualFontFamily by viewModel.manualFontFamily.collectAsStateWithLifecycle()
    val customFonts by viewModel.customFonts.collectAsStateWithLifecycle()
    val isSpectraActive by viewModel.isSpectraActive.collectAsStateWithLifecycle()
    
    var showImportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Typography & Fonts", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isSpectraActive) {
                        IconButton(onClick = { showImportDialog = true }) {
                            Icon(Icons.Rounded.Add, contentDescription = "Import Font")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        if (!isSpectraActive) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Extension,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Spectra Canvas Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Advanced google font pairs and manual ttf/otf customization modules are segregated into Lumia Spectra dynamic micro-APK package.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                ovrrup.lumia.ui.components.BouncyButton(
                    onClick = { navController.navigate("settings/plugins") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Get Spectra Canvas Plugin", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            item {
                TypographyGroupCard(title = "General Settings") {
                    TypographyToggleItem(
                        title = "Dynamic Typography",
                        subtitle = "Automatically choosing fonts based on your theme, UI and appearance changes.",
                        checked = betaDynamicTypography,
                        onCheckedChange = { viewModel.updateBetaDynamicTypography(it) },
                        icon = Icons.Rounded.AutoFixHigh
                    )
                    
                    AnimatedVisibility(visible = !betaDynamicTypography) {
                        Column {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            Text(
                                "Manual Font Selection",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            val fonts = listOf("Nunito", "Quicksand", "Poppins", "Inter", "Montserrat")
                            fonts.forEach { font ->
                                TypographyRadioButtonItem(
                                    title = font,
                                    selected = manualFontFamily == font,
                                    onClick = { viewModel.updateManualFontFamily(font) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Imported & Managed Fonts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            if (customFonts.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Rounded.FontDownload,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No imported fonts yet",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Tap the + button to import your first font from Google Fonts library.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            items(customFonts) { font ->
                FontManagedItem(
                    font = font,
                    onUpdate = { viewModel.updateCustomFont(it) },
                    onDelete = { viewModel.deleteCustomFont(it) }
                )
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

    if (showImportDialog) {
        ImportFontDialog(
            onDismiss = { showImportDialog = false },
            onImport = { name, googleFont, theme, mode ->
                viewModel.addCustomFont(name, googleFont, theme, mode)
                showImportDialog = false
            }
        )
    }
}

@Composable
fun FontManagedItem(
    font: CustomFont,
    onUpdate: (CustomFont) -> Unit,
    onDelete: (CustomFont) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(font.name.take(1).uppercase(), color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(font.name, style = MaterialTheme.typography.titleSmall)
                    Text("Source: ${font.fontName} (Google Font)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = font.isActive, onCheckedChange = { onUpdate(font.copy(isActive = it)) })
            }
            
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            Spacer(Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("Theme: ${font.conditionTheme}") },
                    leadingIcon = { Icon(Icons.Rounded.Palette, null, Modifier.size(16.dp)) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text("Mode: ${font.conditionMode}") },
                    leadingIcon = { Icon(Icons.Rounded.Brightness4, null, Modifier.size(16.dp)) }
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { onDelete(font) }) {
                    Icon(Icons.Rounded.DeleteOutline, null, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportFontDialog(
    onDismiss: () -> Unit,
    onImport: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var googleFontName by remember { mutableStateOf("") }
    var selectedTheme by remember { mutableStateOf("Any") }
    var selectedMode by remember { mutableStateOf("Any") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Font") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Font Alias Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = googleFontName,
                    onValueChange = { googleFontName = it },
                    label = { Text("Google Font Name (Exact)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("e.g. Roboto, Open Sans, Lato") }
                )
                
                Text("Condition: Theme", style = MaterialTheme.typography.labelLarge)
                val themes = listOf("Any", "Ocean", "Emerald", "Gold", "Rose", "Sage", "Twilight", "Custom")
                ScrollableRow(items = themes, selected = selectedTheme, onSelect = { selectedTheme = it })

                Text("Condition: Mode", style = MaterialTheme.typography.labelLarge)
                val modes = listOf("Any", "Light", "Dark")
                ScrollableRow(items = modes, selected = selectedMode, onSelect = { selectedMode = it })
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && googleFontName.isNotBlank()) onImport(name, googleFontName, selectedTheme, selectedMode) },
                enabled = name.isNotBlank() && googleFontName.isNotBlank()
            ) { Text("Import") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ScrollableRow(items: List<String>, selected: String, onSelect: (String) -> Unit) {
    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items) { item ->
            FilterChip(
                selected = selected == item,
                onClick = { onSelect(item) },
                label = { Text(item) }
            )
        }
    }
}

@Composable
fun TypographyRadioButtonItem(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun TypographyGroupCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    val moreRounds = ovrrup.lumia.ui.theme.LocalMoreRounds.current
    val cornerRadius = if (moreRounds) 42.dp else 28.dp
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 8.dp)
    ) {
        if (title.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.5.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }
        }
        
        if (isGlass) {
            ovrrup.lumia.ui.components.GlassCard(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                shape = RoundedCornerShape(cornerRadius)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    content()
                }
            }
        } else {
            androidx.compose.material3.OutlinedCard(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                shape = RoundedCornerShape(cornerRadius),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.2.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun TypographyToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, Modifier.size(20.dp).padding(end = 12.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun CircleShape() = androidx.compose.foundation.shape.CircleShape
