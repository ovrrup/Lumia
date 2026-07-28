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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.*
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

    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val betaGlassUi by viewModel.betaGlassUi.collectAsStateWithLifecycle()
    val betaGlassDynamic by viewModel.betaGlassDynamic.collectAsStateWithLifecycle()
    val betaFrostGlass by viewModel.betaFrostGlass.collectAsStateWithLifecycle()
    val glassBackdropStyle by viewModel.glassBackdropStyle.collectAsStateWithLifecycle()
    val glassOpacityValue by viewModel.glassOpacityValue.collectAsStateWithLifecycle()
    val navBarGlassOpacityValue by viewModel.navBarGlassOpacityValue.collectAsStateWithLifecycle()
    val navBarGlassForceEnabled by viewModel.navBarGlassForceEnabled.collectAsStateWithLifecycle()
    val navBarGlassLinkedToMain by viewModel.navBarGlassLinkedToMain.collectAsStateWithLifecycle()
    val navBarGlassBackdropStyle by viewModel.navBarGlassBackdropStyle.collectAsStateWithLifecycle()
    val navBarGlassDynamic by viewModel.navBarGlassDynamic.collectAsStateWithLifecycle()
    val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    val isSystemSystemDarkForOpacity = androidx.compose.foundation.isSystemInDarkTheme()
    androidx.compose.runtime.LaunchedEffect(themeColor, themeMode) {
        val effectiveDark = themeMode == "Dark" || (themeMode == "System" && isSystemSystemDarkForOpacity)
        viewModel.refreshNavBarGlassOpacity(themeColor, effectiveDark)
    }

    val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 80.dp, bottom = 24.dp),
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

            Spacer(modifier = Modifier.height(16.dp))

            // Experimental Glass UI Engine Card
            val betaMinimalistModeForGlass by viewModel.betaMinimalistMode.collectAsStateWithLifecycle()
            AnimatedVisibility(visible = !betaMinimalistModeForGlass) {
                SettingsGroupCard(title = "Experimental Glass Engine", icon = Icons.Rounded.Palette) {
                    SettingsToggleItem(
                        title = "Frosted Glass UI",
                        subtitle = "Enable translucent glass textures across screen panels",
                        checked = betaGlassUi,
                        icon = Icons.Rounded.Palette,
                        onCheckedChange = {
                            handleToggle(it, "Frosted Glass UI", "Enable translucent glass textures across screen panels. Warning: Glass UI requires background colors to create frosted translucency, which conflicts with Pure Black Mode.") { isChecked ->
                                viewModel.updateBetaGlassUi(isChecked)
                            }
                        }
                    )

                    AnimatedVisibility(visible = betaGlassUi) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(start = 12.dp, top = 8.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                            SettingsToggleItem(
                                title = "Dynamic Color Tinting",
                                subtitle = "Blend glass texture directly with active theme shades",
                                checked = betaGlassDynamic,
                                onCheckedChange = { viewModel.updateBetaGlassDynamic(it) }
                            )

                            SettingsToggleItem(
                                title = "Soft Frost Glaze",
                                subtitle = "Apply high-end satin texture blur to the primary panel layers",
                                checked = betaFrostGlass,
                                onCheckedChange = { viewModel.updateBetaFrostGlass(it) }
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                            // Sub-segmented backdrop style
                            SettingsSegmentedPicker(
                                title = "Backdrop Density Style",
                                subtitle = "Choose panel translucency characteristics",
                                options = listOf(
                                    Triple("Transparent", "Clear", null),
                                    Triple("Translucent", "Satin", null),
                                    Triple("Opaque", "Solid", null)
                                ),
                                selected = glassBackdropStyle,
                                onSelected = { viewModel.updateGlassBackdropStyle(it) }
                            )

                            // Slider for Translucent
                            AnimatedVisibility(visible = glassBackdropStyle == "Translucent") {
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Frosted Layer Opacity",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${(glassOpacityValue * 100).toInt()}%",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = "Calibrate the light passage density through frosted satin panes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    Slider(
                                        value = glassOpacityValue,
                                        onValueChange = { viewModel.updateGlassOpacityValue(it) },
                                        valueRange = 0.1f..1.0f,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                            // Nav Bar Glass Opacity
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Nav Bar Glass Opacity",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${(navBarGlassOpacityValue * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "Control bottom bar glass opacity for current theme and light/dark mode",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                val isSystemSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
                                val effectiveDark = themeMode == "Dark" || (themeMode == "System" && isSystemSystemDark)
                                Slider(
                                    value = navBarGlassOpacityValue,
                                    onValueChange = { viewModel.updateNavBarGlassOpacityValue(it, themeColor, effectiveDark) },
                                    valueRange = 0.1f..1.0f,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                            // Independent Glass Backdrop
                            SettingsToggleItem(
                                title = "Independent Glass Backdrop",
                                subtitle = "Force glass satin backdrop overlay specifically on bottom bar even if global Frosted UI is off",
                                checked = navBarGlassForceEnabled,
                                onCheckedChange = { viewModel.updateNavBarGlassForceEnabled(it) }
                            )

                            val isGlassTheme = lumia.tracker.ui.theme.LocalGlassMode.current
                            val isNavBarGlassActive = isGlassTheme || navBarGlassForceEnabled

                            if (isNavBarGlassActive) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                                // Sync with Global Glass Style
                                SettingsToggleItem(
                                    title = "Sync with Global Glass Style",
                                    subtitle = "Link the bottom navigation bar color, style, and glass type directly to the system-wide Glass UI theme setting.",
                                    checked = navBarGlassLinkedToMain,
                                    onCheckedChange = { viewModel.updateNavBarGlassLinkedToMain(it) }
                                )

                                if (!navBarGlassLinkedToMain) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                                    // Navbar Glass Type segmented chooser
                                    SettingsSegmentedPicker(
                                        title = "Navbar Backdrop Style",
                                        subtitle = "Adjust the glass texture from solid translucent background to completely clear dynamic panel",
                                        options = listOf(
                                            Triple("Solid", "Solid Color", null),
                                            Triple("Translucent", "Frosted Glass", null),
                                            Triple("Clear", "Totally Clear", null)
                                        ),
                                        selected = navBarGlassBackdropStyle,
                                        onSelected = { viewModel.updateNavBarGlassBackdropStyle(it) }
                                    )

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                                    // Navbar Dynamic Color Tinting Toggle
                                    SettingsToggleItem(
                                        title = "Ambient Accent Tinting",
                                        subtitle = "Infuse primary theme color highlight directly into the navigation glass backplane rendering.",
                                        checked = navBarGlassDynamic,
                                        onCheckedChange = { viewModel.updateNavBarGlassDynamic(it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Tab Personalization (Experimental)
            SettingsGroupCard(title = "Navigation Tab Customizer", icon = Icons.Rounded.ViewQuilt) {
                val tabHomeLabel by viewModel.tabHomeLabel.collectAsStateWithLifecycle()
                val tabHomeIconName by viewModel.tabHomeIcon.collectAsStateWithLifecycle()
                val tabCoursesLabel by viewModel.tabCoursesLabel.collectAsStateWithLifecycle()
                val tabCoursesIconName by viewModel.tabCoursesIcon.collectAsStateWithLifecycle()
                val tabSubjectsLabel by viewModel.tabSubjectsLabel.collectAsStateWithLifecycle()
                val tabSubjectsIconName by viewModel.tabSubjectsIcon.collectAsStateWithLifecycle()
                val tabSelfStudyLabel by viewModel.tabSelfStudyLabel.collectAsStateWithLifecycle()
                val tabSelfStudyIconName by viewModel.tabSelfStudyIcon.collectAsStateWithLifecycle()
                val tabAnalyticsLabel by viewModel.tabAnalyticsLabel.collectAsStateWithLifecycle()
                val tabAnalyticsIconName by viewModel.tabAnalyticsIcon.collectAsStateWithLifecycle()
                val tabCalendarLabel by viewModel.tabCalendarLabel.collectAsStateWithLifecycle()
                val tabCalendarIconName by viewModel.tabCalendarIcon.collectAsStateWithLifecycle()

                val featureSubjectEnabled by viewModel.featureSubjectEnabled.collectAsStateWithLifecycle()
                val featureSelfStudyEnabled by viewModel.featureSelfStudyEnabled.collectAsStateWithLifecycle()
                val featureAnalyticsEnabled by viewModel.featureAnalyticsEnabled.collectAsStateWithLifecycle()
                val featureCalendarEnabled by viewModel.featureCalendarEnabled.collectAsStateWithLifecycle()

                Text(
                    text = "Personalize Tab Labels & Icons",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Experimentally rewrite titles and select unique icons to customize your bottom navigation tabs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                TabCustomizerItem(
                    title = "Home Tab",
                    label = tabHomeLabel,
                    onLabelChange = { viewModel.updateTabHomeLabel(it) },
                    currentIcon = tabHomeIconName,
                    onIconSelect = { viewModel.updateTabHomeIcon(it) },
                    iconOptions = listOf("Home", "School", "Star", "Person", "List")
                )

                Spacer(modifier = Modifier.height(8.dp))

                TabCustomizerItem(
                    title = "Courses Tab",
                    label = tabCoursesLabel,
                    onLabelChange = { viewModel.updateTabCoursesLabel(it) },
                    currentIcon = tabCoursesIconName,
                    onIconSelect = { viewModel.updateTabCoursesIcon(it) },
                    iconOptions = listOf("MenuBook", "Class", "AutoStories", "Folder")
                )

                if (featureSubjectEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TabCustomizerItem(
                        title = "Subjects Tab",
                        label = tabSubjectsLabel,
                        onLabelChange = { viewModel.updateTabSubjectsLabel(it) },
                        currentIcon = tabSubjectsIconName,
                        onIconSelect = { viewModel.updateTabSubjectsIcon(it) },
                        iconOptions = listOf("FolderOpen", "Category", "Folder", "List")
                    )
                }

                if (featureSelfStudyEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TabCustomizerItem(
                        title = "Self Study Tab",
                        label = tabSelfStudyLabel,
                        onLabelChange = { viewModel.updateTabSelfStudyLabel(it) },
                        currentIcon = tabSelfStudyIconName,
                        onIconSelect = { viewModel.updateTabSelfStudyIcon(it) },
                        iconOptions = listOf("AutoStories", "Timer", "History", "PlayArrow")
                    )
                }

                if (featureCalendarEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TabCustomizerItem(
                        title = "Calendar Tab",
                        label = tabCalendarLabel,
                        onLabelChange = { viewModel.updateTabCalendarLabel(it) },
                        currentIcon = tabCalendarIconName,
                        onIconSelect = { viewModel.updateTabCalendarIcon(it) },
                        iconOptions = listOf("CalendarMonth", "DateRange", "Schedule", "History")
                    )
                }

                if (featureAnalyticsEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TabCustomizerItem(
                        title = "Analytics Tab",
                        label = tabAnalyticsLabel,
                        onLabelChange = { viewModel.updateTabAnalyticsLabel(it) },
                        currentIcon = tabAnalyticsIconName,
                        onIconSelect = { viewModel.updateTabAnalyticsIcon(it) },
                        iconOptions = listOf("Analytics", "CheckCircle", "Timer", "Star")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    lumia.tracker.ui.components.UniversalCapsuleHeader(
        title = "Experimental Features",
        onBackClick = { navController.popBackStack() }
    )
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabCustomizerItem(
    title: String,
    label: String,
    onLabelChange: (String) -> Unit,
    currentIcon: String,
    onIconSelect: (String) -> Unit,
    iconOptions: List<String>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = label,
                onValueChange = onLabelChange,
                label = { Text("Tab Label") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Choose Icon:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                iconOptions.forEach { iconName ->
                    val isSelected = currentIcon == iconName
                    val iconVector = getBetaTabIcon(iconName)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable { onIconSelect(iconName) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = iconName,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

fun getBetaTabIcon(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName) {
        "Home" -> Icons.Rounded.Home
        "School" -> Icons.Rounded.School
        "Star" -> Icons.Rounded.Star
        "Person" -> Icons.Rounded.Person
        "List" -> Icons.Rounded.List
        
        "MenuBook" -> Icons.AutoMirrored.Rounded.MenuBook
        "Class" -> Icons.Rounded.Class
        "AutoStories" -> Icons.Rounded.AutoStories
        "Folder" -> Icons.Rounded.Folder
        
        "FolderOpen" -> Icons.Rounded.FolderOpen
        "Category" -> Icons.Rounded.Category
        
        "Timer" -> Icons.Rounded.Timer
        "History" -> Icons.Rounded.History
        "PlayArrow" -> Icons.Rounded.PlayArrow
        
        "CalendarMonth" -> Icons.Rounded.CalendarMonth
        "DateRange" -> Icons.Rounded.DateRange
        "Schedule" -> Icons.Rounded.Schedule
        
        "Analytics" -> Icons.Rounded.Analytics
        "CheckCircle" -> Icons.Rounded.CheckCircle
        
        else -> Icons.Rounded.Home
    }
}
