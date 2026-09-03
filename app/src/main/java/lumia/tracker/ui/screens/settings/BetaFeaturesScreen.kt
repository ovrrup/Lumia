package lumia.tracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.viewmodel.ScholarViewModel

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
        AlertDialog(
            onDismissRequest = { pendingFeature = null },
            title = { Text("Enable ${pendingFeature?.title ?: "Feature"}?", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "This feature is experimental and in active development. It may change or behave unexpectedly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "About this feature:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        pendingFeature?.description ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    pendingFeature?.onConfirm?.invoke()
                    pendingFeature = null 
                }) { Text("Enable", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { pendingFeature = null }) { Text("Cancel") }
            }
        )
    }

    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current
    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (betaEnhancedHeader || isGlass) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text("Experimental Features", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
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

            // 1. Workflow Tools
            SettingsGroupCard(title = "Workflow Tools", icon = Icons.Rounded.Edit) {
                val betaNotes by viewModel.betaNotes.collectAsStateWithLifecycle()

                SettingsToggleItem(
                    title = "Quick Notes",
                    subtitle = "Show a quick notes shortcut on the Home screen.",
                    checked = betaNotes,
                    icon = Icons.Rounded.Edit,
                    onCheckedChange = { handleToggle(it, "Quick Notes", "Adds a Quick Notes card to the Home screen for rapid note-taking.") { isChecked -> viewModel.updateBetaNotes(isChecked) } }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Display & Navigation
            SettingsGroupCard(title = "Display & Navigation", icon = Icons.Rounded.Settings) {
                val betaMinimalistMode by viewModel.betaMinimalistMode.collectAsStateWithLifecycle()
                val betaNavBarSizeControls by viewModel.betaNavBarSizeControls.collectAsStateWithLifecycle()
                SettingsToggleItem(
                    title = "Navigation Bar Size Controls",
                    subtitle = "Show height and padding sliders in Appearance settings.",
                    checked = betaNavBarSizeControls,
                    icon = Icons.Rounded.Straighten,
                    enabled = !betaMinimalistMode,
                    onCheckedChange = { handleToggle(it, "Navigation Bar Size Controls", "Enables sliders in Appearance settings to adjust the height and spacing of the bottom navigation bar.") { isChecked -> viewModel.updateBetaNavBarSizeControls(isChecked) } }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                val showActionHistory by viewModel.showActionHistory.collectAsStateWithLifecycle()
                SettingsToggleItem(
                    title = "Action History",
                    subtitle = "Show recent activity history in the Analytics tab.",
                    checked = showActionHistory,
                    icon = Icons.Rounded.List,
                    onCheckedChange = { handleToggle(it, "Action History", "Displays a log of recent actions and events in the Analytics tab.") { isChecked -> viewModel.updateShowActionHistory(isChecked) } }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
