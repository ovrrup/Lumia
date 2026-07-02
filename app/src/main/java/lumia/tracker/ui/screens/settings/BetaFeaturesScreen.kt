package lumia.tracker.ui.screens

import lumia.tracker.service.AodAccessibilityService
import lumia.tracker.util.TrueAodManager
import android.content.Intent
import android.provider.Settings
import lumia.tracker.ui.theme.liquidGlass
import lumia.tracker.ui.theme.glassBar
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.MergeType
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ViewQuilt
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.InvertColors
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import lumia.tracker.viewmodel.ScholarViewModel
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BetaFeaturesScreen(navController: NavController, viewModel: ScholarViewModel) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
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

    val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current
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

                SettingsToggleItem(
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
                SettingsToggleItem(
                    title = "Advanced NavBar Size Controls",
                    subtitle = "Expose precise custom sizing and shape sliders for the bottom navigation bar.",
                    checked = betaNavBarSizeControls,
                    icon = Icons.Rounded.Straighten,
                    enabled = !betaMinimalistMode,
                    onCheckedChange = { handleToggle(it, "Advanced NavBar Size Controls", "Reveal precise geometry and padding sliders inside the design settings.") { isChecked -> viewModel.updateBetaNavBarSizeControls(isChecked) } }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                val showActionHistory by viewModel.showActionHistory.collectAsStateWithLifecycle()
                SettingsToggleItem(
                    title = "Display Action History",
                    subtitle = "Integrate detailed reactive logs list inside Analytics interface",
                    checked = showActionHistory,
                    icon = Icons.Rounded.List,
                    onCheckedChange = { handleToggle(it, "Display Action History", "Synthesize analytics telemetry block containing audit records.") { isChecked -> viewModel.updateShowActionHistory(isChecked) } }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
