package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: ScholarViewModel) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
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

            SettingsActionItem(
                title = "Appearance",
                subtitle = "Themes, colors, and layout modifiers",
                icon = Icons.Default.Palette,
                onClick = { navController.navigate("settings/appearance") }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsActionItem(
                title = "Beta Features",
                subtitle = "Experimental optimizations and tweaks",
                icon = Icons.Default.Check,
                onClick = { navController.navigate("settings/beta") }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsActionItem(
                title = "Data Management",
                subtitle = "Export, import, and reset data",
                icon = Icons.Default.Storage,
                onClick = { navController.navigate("settings/data") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(navController: NavController, viewModel: ScholarViewModel) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
    val betaGlassUi by viewModel.betaGlassUi.collectAsStateWithLifecycle()
    val betaDynamicBackground by viewModel.betaDynamicBackground.collectAsStateWithLifecycle()
    val betaBetterTexts by viewModel.betaBetterTexts.collectAsStateWithLifecycle()
    val betaBetterTextsPalette by viewModel.betaBetterTextsPalette.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Appearance", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
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
            
            SettingsGroupMenu(
                title = "Theme Mode",
                subtitle = "Select $themeMode",
                icon = Icons.Default.DarkMode
            ) { }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ThemeOptionButton("System", themeMode == "System") { viewModel.updateThemeMode("System") }
                ThemeOptionButton("Light", themeMode == "Light") { viewModel.updateThemeMode("Light") }
                ThemeOptionButton("Dark", themeMode == "Dark") { viewModel.updateThemeMode("Dark") }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            
            val pureBlackMode by viewModel.pureBlackMode.collectAsStateWithLifecycle()
            SettingsToggleItem(
                title = "Pure Black Mode",
                subtitle = "Apply pure black background in dark mode",
                checked = pureBlackMode,
                onCheckedChange = { viewModel.updatePureBlackMode(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            SettingsToggleItem(
                title = "Glass UI",
                subtitle = "Enable modern frosted glass UI components",
                checked = betaGlassUi,
                onCheckedChange = { viewModel.updateBetaGlassUi(it) }
            )

            SettingsToggleItem(
                title = "Dynamic Lighting Background",
                subtitle = "Soft, vibrant animated gradient background",
                checked = betaDynamicBackground,
                onCheckedChange = { viewModel.updateBetaDynamicBackground(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            
            SettingsToggleItem(
                title = "Better Texts",
                subtitle = "Enhance text readability and aesthetics",
                checked = betaBetterTexts,
                onCheckedChange = { viewModel.updateBetaBetterTexts(it) }
            )
            
            androidx.compose.animation.AnimatedVisibility(visible = betaBetterTexts) {
                Column(modifier = Modifier.padding(start = 32.dp)) {
                    SettingsToggleItem(
                        title = "Use Palette Shades for Text",
                        subtitle = "Uses theme palette shades for text instead of strict black or white",
                        checked = betaBetterTextsPalette,
                        onCheckedChange = { viewModel.updateBetaBetterTextsPalette(it) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            
            Text(
                text = "Theme Palette",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val palettes = mutableListOf(
                    "Blue" to androidx.compose.ui.graphics.Color(0xFF0085FF),
                    "Green" to androidx.compose.ui.graphics.Color(0xFF00BA34),
                    "Orange" to androidx.compose.ui.graphics.Color(0xFFF98600),
                    "Red" to androidx.compose.ui.graphics.Color(0xFFE92C2C)
                )
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    palettes.add(0, "Dynamic" to androidx.compose.ui.graphics.Color(0xFF909090))
                }
                items(palettes) { (name, color) ->
                    ThemeColorPickerItem(
                        name = name,
                        color = color,
                        isSelected = themeColor == name,
                        onClick = { viewModel.updateThemeColor(name) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
data class BetaFeatureDialogData(
    val title: String,
    val description: String,
    val onConfirm: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BetaFeaturesScreen(navController: NavController, viewModel: ScholarViewModel) {
    var pendingFeature by remember { mutableStateOf<BetaFeatureDialogData?>(null) }

    val handleToggle = { isChecked: Boolean, title: String, subtitle: String, updateAction: (Boolean) -> Unit ->
        if (isChecked) {
            pendingFeature = BetaFeatureDialogData(title, subtitle) {
                updateAction(true)
            }
        } else {
            updateAction(false)
        }
    }

    if (pendingFeature != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingFeature = null },
            title = { Text("Beta Feature: ${pendingFeature!!.title}") },
            text = {
                Column {
                    Text(
                        "Disclaimer: You are about to enable a Beta feature. Beta features are experimental, currently in active development, and may not behave as expected. They are provided 'as is' and could potentially cause graphical glitches, app instability, or data inconsistencies.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "About this feature:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        pendingFeature!!.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { 
                    pendingFeature!!.onConfirm()
                    pendingFeature = null 
                }) { Text("Enable") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { pendingFeature = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Beta Features", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
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
            val betaPomodoro by viewModel.betaPomodoro.collectAsStateWithLifecycle()
            SettingsToggleItem(
                title = "Pomodoro Timer",
                subtitle = "Enable the Pomodoro timer tool",
                checked = betaPomodoro,
                onCheckedChange = { handleToggle(it, "Pomodoro Timer", "Enable the Pomodoro timer tool") { isChecked -> viewModel.updateBetaPomodoro(isChecked) } }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            val betaCgpa by viewModel.betaCgpa.collectAsStateWithLifecycle()
            SettingsToggleItem(
                title = "CGPA Calculator",
                subtitle = "Enable the CGPA calculator tool",
                checked = betaCgpa,
                onCheckedChange = { handleToggle(it, "CGPA Calculator", "Enable the CGPA calculator tool") { isChecked -> viewModel.updateBetaCgpa(isChecked) } }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            val betaNotes by viewModel.betaNotes.collectAsStateWithLifecycle()
            SettingsToggleItem(
                title = "Quick Notes",
                subtitle = "Enable the quick notes tool",
                checked = betaNotes,
                onCheckedChange = { handleToggle(it, "Quick Notes", "Enable the quick notes tool") { isChecked -> viewModel.updateBetaNotes(isChecked) } }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            val betaMotivation by viewModel.betaMotivation.collectAsStateWithLifecycle()
            SettingsToggleItem(
                title = "Motivation Panel",
                subtitle = "Enable the motivation quotes panel on the dashboard",
                checked = betaMotivation,
                onCheckedChange = { handleToggle(it, "Motivation Panel", "Enable the motivation quotes panel on the dashboard") { isChecked -> viewModel.updateBetaMotivation(isChecked) } }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            val betaFloatingNav by viewModel.betaFloatingNav.collectAsStateWithLifecycle()
            SettingsToggleItem(
                title = "Floating Action Bar",
                subtitle = "Makes the bottom navigation bar float and look chubby",
                checked = betaFloatingNav,
                onCheckedChange = { handleToggle(it, "Floating Action Bar", "Makes the bottom navigation bar float and look chubby") { isChecked -> viewModel.updateBetaFloatingNav(isChecked) } }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            val betaNotchOptimization by viewModel.betaNotchOptimization.collectAsStateWithLifecycle()
            SettingsToggleItem(
                title = "Notch & Punch Hole Optimization",
                subtitle = "Apply specialized padding to avoid display cutouts, notches, and punch hole cameras.",
                checked = betaNotchOptimization,
                onCheckedChange = { handleToggle(it, "Notch & Punch Hole Optimization", "Apply specialized padding to avoid display cutouts, notches, and punch hole cameras.") { isChecked -> viewModel.updateBetaNotchOptimization(isChecked) } }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            val betaImmersiveMode by viewModel.betaImmersiveMode.collectAsStateWithLifecycle()
            SettingsToggleItem(
                title = "Full Screen Punch Hole (Immersive)",
                subtitle = "Draw behind the punch hole camera, safely dodging UI elements.",
                checked = betaImmersiveMode,
                onCheckedChange = { handleToggle(it, "Full Screen Punch Hole (Immersive)", "Draw behind the punch hole camera, safely dodging UI elements.") { isChecked -> viewModel.updateBetaImmersiveMode(isChecked) } }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            val showActionHistory by viewModel.showActionHistory.collectAsStateWithLifecycle()
            SettingsToggleItem(
                title = "Show Action History",
                subtitle = "Display action history on the Analytics tab",
                checked = showActionHistory,
                onCheckedChange = { handleToggle(it, "Show Action History", "Display action history on the Analytics tab") { isChecked -> viewModel.updateShowActionHistory(isChecked) } }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(navController: NavController, viewModel: ScholarViewModel) {
    val status by viewModel.importExportStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.exportData(uri)
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importData(uri)
        }
    }

    LaunchedEffect(status) {
        status?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearStatus()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Data Management", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
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
            SettingsActionItem(
                title = "Export Data",
                subtitle = "Back up courses, subjects, assignments securely",
                icon = Icons.Default.Upload,
                onClick = { showExportDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsActionItem(
                title = "Import Data",
                subtitle = "Restore backup. Warning: Overwrites current data",
                icon = Icons.Default.Download,
                isDestructive = true,
                onClick = { openDocumentLauncher.launch(arrayOf("application/octet-stream", "*/*")) }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsActionItem(
                title = "Erase All Data",
                subtitle = "Permanently delete all courses, subjects, assignments, and logs",
                icon = Icons.Default.DeleteForever,
                isDestructive = true,
                onClick = { showResetDialog = true }
            )
        }
    }

    if (showExportDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Data") },
            text = { Text("Are you sure you want to export a binary backup of all your data?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExportDialog = false
                        createDocumentLauncher.launch("scholar_backup.bin")
                    }
                ) { Text("Export") }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showResetDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Erase All Data?") },
            text = { Text("This action cannot be undone. All your progress, courses, subjects, and settings will be permanently removed.", color = MaterialTheme.colorScheme.error) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Erase Data", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsCategoryHeading(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SettingsGroupMenu(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ThemeOptionButton(title: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(title) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun ThemeColorPickerItem(name: String, color: androidx.compose.ui.graphics.Color, isSelected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else if (isSelected) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsToggleItem(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsActionItem(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isDestructive: Boolean = false, onClick: () -> Unit) {
    val contentColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isDestructive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = contentColor)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
