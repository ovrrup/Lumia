package ovrrup.lumia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ovrrup.lumia.ui.theme.glassBar
import ovrrup.lumia.viewmodel.ScholarViewModel

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
            title = { Text("Beta Feature: ${pendingFeature?.title ?: ""}", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Disclaimer: You are about to enable an experimental feature. Extreme caution is recommended. These capabilities are in active development and might present functional quirks or display modifications.",
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
                        pendingFeature?.description ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { 
                    pendingFeature?.onConfirm?.invoke()
                    pendingFeature = null 
                }) { Text("Enable Feature", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { pendingFeature = null }) { Text("Cancel") }
            }
        )
    }

    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (isGlass) {
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
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent
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

            // 1. Experimental Workflow
            SettingsGroupCard(title = "Experimental Workflow", icon = Icons.Rounded.Edit) {
                val betaNotes by viewModel.betaNotes.collectAsStateWithLifecycle()
                SettingsPremiumToggleItem(
                    title = "Quick Notes Overlay",
                    subtitle = "Draft scratchpad canvas for immediate raw notes overlay panel.",
                    checked = betaNotes,
                    icon = Icons.Rounded.Edit,
                    onCheckedChange = { handleToggle(it, "Quick Notes Overlay", "Enable immediate raw scratchpad notes overlay panel.") { isChecked -> viewModel.updateBetaNotes(isChecked) } }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Display Hacks & System Settings
            SettingsGroupCard(title = "Display Settings & Hooks", icon = Icons.Rounded.Settings) {
                val betaMinimalistMode by viewModel.betaMinimalistMode.collectAsStateWithLifecycle()
                val betaNavBarSizeControls by viewModel.betaNavBarSizeControls.collectAsStateWithLifecycle()
                SettingsPremiumToggleItem(
                    title = "Advanced NavBar Size Controls",
                    subtitle = "Expose precise custom sizing and shape sliders for the bottom navigation bar.",
                    checked = betaNavBarSizeControls,
                    icon = Icons.Rounded.Straighten,
                    enabled = !betaMinimalistMode,
                    unavailableReason = "Locked by Minimalist Focus Mode.",
                    onCheckedChange = { handleToggle(it, "Advanced NavBar Size Controls", "Reveal precise geometry and padding sliders inside the design settings.") { isChecked -> viewModel.updateBetaNavBarSizeControls(isChecked) } }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                val showActionHistory by viewModel.showActionHistory.collectAsStateWithLifecycle()
                SettingsPremiumToggleItem(
                    title = "Display Action History",
                    subtitle = "Integrate detailed reactive logs list inside Analytics interface",
                    checked = showActionHistory,
                    icon = Icons.AutoMirrored.Rounded.List,
                    onCheckedChange = { handleToggle(it, "Display Action History", "Synthesize analytics telemetry block containing audit records.") { isChecked -> viewModel.updateShowActionHistory(isChecked) } }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
