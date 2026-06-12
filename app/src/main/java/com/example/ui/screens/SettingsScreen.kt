package com.example.ui.screens

import com.example.ui.theme.liquidGlass
import com.example.ui.theme.glassBar
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val isGlass = com.example.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (betaEnhancedHeader) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text("Settings", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (betaEnhancedHeader) androidx.compose.ui.graphics.Color.Transparent else if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
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

            SettingsActionItem(
                title = "Appearance",
                subtitle = "Themes, colors, and layout modifiers",
                icon = Icons.Rounded.Palette,
                onClick = { navController.navigate("settings/appearance") }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsActionItem(
                title = "Beta Features",
                subtitle = "Experimental optimizations and tweaks",
                icon = Icons.Rounded.Check,
                onClick = { navController.navigate("settings/beta") }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsActionItem(
                title = "Safety Features",
                subtitle = "Settings protection and smart recommendations",
                icon = Icons.Rounded.Lock,
                onClick = { navController.navigate("settings/safety") }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsActionItem(
                title = "Data Management",
                subtitle = "Manage backups, configurations, and data",
                icon = Icons.Rounded.Storage,
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

    val isGlass = com.example.ui.theme.LocalGlassMode.current
    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Appearance", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
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
                icon = Icons.Rounded.DarkMode
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
                enabled = themeMode != "Light",
                unavailableReason = "Requires System/Dark mode.",
                onCheckedChange = { viewModel.updatePureBlackMode(it) }
            )

            val betaMinimalistMode by viewModel.betaMinimalistMode.collectAsStateWithLifecycle()
            SettingsToggleItem(
                title = "Minimalist Mode",
                subtitle = "Force-off and lock visual flair for focus",
                checked = betaMinimalistMode,
                onCheckedChange = { viewModel.updateBetaMinimalistMode(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            SettingsToggleItem(
                title = "Glass UI",
                subtitle = "Enable modern frosted glass UI components",
                checked = betaGlassUi,
                enabled = !betaMinimalistMode,
                unavailableReason = "Locked by Minimalist Mode.",
                onCheckedChange = { viewModel.updateBetaGlassUi(it) }
            )

            AnimatedVisibility(
                visible = betaGlassUi && !betaMinimalistMode
            ) {
                val betaGlassDynamic by viewModel.betaGlassDynamic.collectAsStateWithLifecycle()
                val betaFrostGlass by viewModel.betaFrostGlass.collectAsStateWithLifecycle()
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp)
                            .clickable { viewModel.updateBetaGlassDynamic(!betaGlassDynamic) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                            Text(
                                text = "Dynamic Color Tinting",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Blend glass texture with your active theme palette instead of stark white/black",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = betaGlassDynamic,
                            onCheckedChange = { viewModel.updateBetaGlassDynamic(it) },
                            modifier = Modifier.scale(0.85f)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp)
                            .clickable { viewModel.updateBetaFrostGlass(!betaFrostGlass) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                            Text(
                                text = "Frosted Glass Style",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Switch back to the classic high-contrast frosted glass canvas rather than a dark/liquid fluid finish",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = betaFrostGlass,
                            onCheckedChange = { viewModel.updateBetaFrostGlass(it) },
                            modifier = Modifier.scale(0.85f)
                        )
                    }
                }
            }

            val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
            SettingsToggleItem(
                title = "Enhanced Header",
                subtitle = "Apply a polished translucent look to the top navigation bar",
                checked = betaEnhancedHeader,
                enabled = !betaMinimalistMode,
                unavailableReason = "Locked by Minimalist Mode.",
                onCheckedChange = { viewModel.updateBetaEnhancedHeader(it) }
            )

            val dynamicAppIcon by viewModel.dynamicAppIcon.collectAsStateWithLifecycle()
            SettingsToggleItem(
                title = "Alternate Icon Style",
                subtitle = "Switch between default celestial blue gradient and Obsidian-Gold luxury style. (Note: System-wide wallpaper-reactive themed icons are applied automatically by launcher settings.)",
                checked = dynamicAppIcon,
                onCheckedChange = { viewModel.updateDynamicAppIcon(it) }
            )

            SettingsToggleItem(
                title = "Dynamic Lighting Background",
                subtitle = "Soft, vibrant animated gradient background",
                checked = betaDynamicBackground,
                enabled = !betaMinimalistMode,
                unavailableReason = "Locked by Minimalist Mode.",
                onCheckedChange = { viewModel.updateBetaDynamicBackground(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            
            SettingsToggleItem(
                title = "Better Texts",
                subtitle = "Enhance text readability and aesthetics",
                checked = betaBetterTexts,
                enabled = !betaMinimalistMode,
                unavailableReason = "Locked by Minimalist Mode.",
                onCheckedChange = { viewModel.updateBetaBetterTexts(it) }
            )
            
            Column(modifier = Modifier.padding(start = 32.dp)) {
                SettingsToggleItem(
                    title = "Use Palette Shades for Text",
                    subtitle = "Uses theme palette shades for text instead of strict black or white",
                    checked = betaBetterTextsPalette,
                    enabled = betaBetterTexts && !betaMinimalistMode,
                    unavailableReason = if (betaMinimalistMode) "Locked by Minimalist Mode." else "Requires 'Better Texts' to be enabled.",
                    onCheckedChange = { viewModel.updateBetaBetterTextsPalette(it) }
                )
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
                    "Ocean" to androidx.compose.ui.graphics.Color(0xFF3197D6),
                    "Emerald" to androidx.compose.ui.graphics.Color(0xFF4BC27D),
                    "Gold" to androidx.compose.ui.graphics.Color(0xFFFFC646),
                    "Rose" to androidx.compose.ui.graphics.Color(0xFFE52F28),
                    "Sage" to androidx.compose.ui.graphics.Color(0xFFACBDAA),
                    "Twilight" to androidx.compose.ui.graphics.Color(0xFF958CE8),
                    "Custom" to androidx.compose.ui.graphics.Color(0xFF999999)
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

            if (themeColor == "Custom") {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsActionItem(
                    title = "Advanced Theme Colors",
                    subtitle = "Customize the specific hex shades for the custom theme",
                    icon = Icons.Rounded.Edit,
                    onClick = { navController.navigate("settings/advanced_theme") }
                )
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

    val isGlass = com.example.ui.theme.LocalGlassMode.current
    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Beta Features", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
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
            val betaMinimalistMode by viewModel.betaMinimalistMode.collectAsStateWithLifecycle()
            val betaFloatingNav by viewModel.betaFloatingNav.collectAsStateWithLifecycle()
            SettingsToggleItem(
                title = "Floating Action Bar",
                subtitle = "Makes the bottom navigation bar float and look chubby",
                checked = betaFloatingNav,
                enabled = !betaMinimalistMode,
                unavailableReason = "Locked by Minimalist Mode.",
                onCheckedChange = { handleToggle(it, "Floating Action Bar", "Makes the bottom navigation bar float and look chubby") { isChecked -> viewModel.updateBetaFloatingNav(isChecked) } }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            val betaNotchOptimization by viewModel.betaNotchOptimization.collectAsStateWithLifecycle()
            SettingsToggleItem(
                title = "Notch & Punch Hole Optimization",
                subtitle = "Apply specialized padding to avoid display cutouts, notches, and punch hole cameras.",
                checked = betaNotchOptimization,
                enabled = false,
                unavailableReason = "Managed inversely by Immersive Mode.",
                onCheckedChange = { /* managed by immersive mode */ }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            val betaImmersiveMode by viewModel.betaImmersiveMode.collectAsStateWithLifecycle()
            SettingsToggleItem(
                title = "Full Screen Punch Hole (Immersive)",
                subtitle = "Draw behind the punch hole camera, safely dodging UI elements.",
                checked = betaImmersiveMode,
                enabled = !betaMinimalistMode,
                unavailableReason = "Locked ON by Minimalist Mode.",
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

    val isGlass = com.example.ui.theme.LocalGlassMode.current
    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Data Management", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
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
            val coursesCount by viewModel.courses.collectAsStateWithLifecycle()
            val assignmentsCount by viewModel.assignments.collectAsStateWithLifecycle()
            val subjectsCount by viewModel.subjects.collectAsStateWithLifecycle()
            val pomodoroSessionsCount by viewModel.pomodoroSessions.collectAsStateWithLifecycle()

            // System Integrity Dashboard Card
            com.example.ui.components.GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Storage,
                                contentDescription = "Active Database Info",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Database Integrity Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Secure active binary protection enabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = coursesCount.size.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text(text = "Courses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = subjectsCount.size.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text(text = "Subjects", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = assignmentsCount.size.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text(text = "Assignments", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = pomodoroSessionsCount.size.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text(text = "Pomodoros", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            SettingsActionItem(
                title = "Export Data",
                subtitle = "Back up settings, customisations, courses and assignments securely",
                icon = Icons.Rounded.Upload,
                onClick = { showExportDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsActionItem(
                title = "Import Data",
                subtitle = "Restore backup. Warning: Overwrites current data and settings",
                icon = Icons.Rounded.Download,
                isDestructive = true,
                onClick = { openDocumentLauncher.launch(arrayOf("application/octet-stream", "*/*")) }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsActionItem(
                title = "Erase All Data & Settings",
                subtitle = "Permanently delete all customisations, courses, subjects, assignments, and logs",
                icon = Icons.Rounded.DeleteForever,
                isDestructive = true,
                onClick = { showResetDialog = true }
            )
        }
    }

    if (showExportDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Data & Settings") },
            text = { Text("Are you sure you want to export a binary backup of all your data and settings?") },
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
            title = { Text("Erase All Data & Settings?") },
            text = { Text("This action cannot be undone. All your progress, custom themes, subjects, and settings will be permanently removed.", color = MaterialTheme.colorScheme.error) },
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
                    imageVector = Icons.Rounded.Check,
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
fun SettingsToggleItem(
    title: String, 
    subtitle: String, 
    checked: Boolean, 
    enabled: Boolean = true,
    unavailableReason: String? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp).alpha(if (enabled) 1f else 0.5f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!enabled && unavailableReason != null) {
                Text(
                    text = unavailableReason,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Switch(
            checked = checked,
            enabled = enabled,
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
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyFeaturesScreen(navController: NavController, viewModel: ScholarViewModel) {
    val safetyPinEnabled by viewModel.safetyPinEnabled.collectAsStateWithLifecycle()
    val safetyPinConflictWarning by viewModel.safetyPinConflictWarning.collectAsStateWithLifecycle()
    val safetyPinRecommendations by viewModel.safetyPinRecommendations.collectAsStateWithLifecycle()

    val isGlass = com.example.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (betaEnhancedHeader) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text("Safety Features", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (betaEnhancedHeader) androidx.compose.ui.graphics.Color.Transparent else if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
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
            SettingsCategoryHeading(title = "Safety Pin", icon = Icons.Rounded.Lock)
            
            SettingsToggleItem(
                title = "Enable Safety Pin",
                subtitle = "Master switch to monitor and manage settings conflicts and recommendations",
                checked = safetyPinEnabled,
                onCheckedChange = { viewModel.updateSafetyPinEnabled(it) }
            )

            androidx.compose.animation.AnimatedVisibility(visible = safetyPinEnabled) {
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    SettingsToggleItem(
                        title = "Conflict Warning",
                        subtitle = "Alert me when a newly activated setting structurally opposes another setting",
                        checked = safetyPinConflictWarning,
                        onCheckedChange = { viewModel.updateSafetyPinConflictWarning(it) }
                    )
                    
                    SettingsToggleItem(
                        title = "Smart Recommendations",
                        subtitle = "Suggest complementary settings when enabling core aesthetic features",
                        checked = safetyPinRecommendations,
                        onCheckedChange = { viewModel.updateSafetyPinRecommendations(it) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedThemeScreen(navController: NavController, viewModel: ScholarViewModel) {
    val customPrimary by viewModel.customPrimary.collectAsStateWithLifecycle()
    val customPrimaryContainer by viewModel.customPrimaryContainer.collectAsStateWithLifecycle()
    val customBackground by viewModel.customBackground.collectAsStateWithLifecycle()
    val customSurface by viewModel.customSurface.collectAsStateWithLifecycle()
    val customText by viewModel.customText.collectAsStateWithLifecycle()

    val isGlass = com.example.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (betaEnhancedHeader) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
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
                        containerColor = if (betaEnhancedHeader) androidx.compose.ui.graphics.Color.Transparent else if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = if (betaEnhancedHeader) androidx.compose.ui.graphics.Color.Transparent else if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    ) { padding ->
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

@Composable
fun HexColorInputItem(label: String, value: String, onValueChange: (String) -> Unit) {
    var textValue by remember(value) { mutableStateOf(value) }
    
    val colorPreview = remember(textValue) {
        try {
            val formatted = if (textValue.isNotEmpty() && !textValue.startsWith("#")) "#$textValue" else textValue
            androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(formatted))
        } catch(e: Exception) {
            androidx.compose.ui.graphics.Color.Transparent
        }
    }
    
    val isError = remember(textValue) {
        try {
            val formatted = if (textValue.isNotEmpty() && !textValue.startsWith("#")) "#$textValue" else textValue
            if (formatted.length == 7 || formatted.length == 9) {
                android.graphics.Color.parseColor(formatted)
                false
            } else {
                true
            }
        } catch(e: Exception) {
            true
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = textValue,
                onValueChange = { 
                    textValue = it
                    val formatted = if (it.isNotEmpty() && !it.startsWith("#")) "#$it" else it
                    try {
                        if (formatted.length == 7 || formatted.length == 9) {
                            android.graphics.Color.parseColor(formatted)
                            onValueChange(formatted)
                        }
                    } catch(e: Exception) {
                        // Safe ignore during half-typed strings
                    }
                },
                singleLine = true,
                isError = isError,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, end = 16.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(colorPreview, shape = RoundedCornerShape(8.dp))
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
        )
    }
}
