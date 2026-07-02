package lumia.tracker.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.theme.LocalAppAnimationMode
import lumia.tracker.ui.theme.LocalGlassMode
import lumia.tracker.ui.theme.liquidGlass
import lumia.tracker.viewmodel.ScholarViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakSettingsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val reqTasks by viewModel.streakRequirementTasks.collectAsStateWithLifecycle()
    val reqAssignments by viewModel.streakRequirementAssignments.collectAsStateWithLifecycle()
    val reqStudyMins by viewModel.streakRequirementStudyMins.collectAsStateWithLifecycle()
    val partialThreshold by viewModel.streakPartialThreshold.collectAsStateWithLifecycle()
    val colorHex by viewModel.streakProgressColor.collectAsStateWithLifecycle()
    val brightness by viewModel.streakBrightness.collectAsStateWithLifecycle()
    val animOverride by viewModel.streakAnimationOverride.collectAsStateWithLifecycle()

    val isGlass = LocalGlassMode.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Streak Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    "Streak Goals",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Set minimum daily requirements for a complete streak. Note: If you plan more than these limits, you'll need to complete all planned items to maintain your streak.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Tasks Requirement
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tasks", modifier = Modifier.weight(1f))
                    Slider(
                        value = reqTasks.toFloat(),
                        onValueChange = { viewModel.updateStreakReqTasks(it.roundToInt()) },
                        valueRange = 0f..20f,
                        steps = 19,
                        modifier = Modifier.weight(2f)
                    )
                    Text("$reqTasks", modifier = Modifier.width(30.dp))
                }

                // Assignments Requirement
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Assignments", modifier = Modifier.weight(1f))
                    Slider(
                        value = reqAssignments.toFloat(),
                        onValueChange = { viewModel.updateStreakReqAssignments(it.roundToInt()) },
                        valueRange = 0f..10f,
                        steps = 9,
                        modifier = Modifier.weight(2f)
                    )
                    Text("$reqAssignments", modifier = Modifier.width(30.dp))
                }

                // Study Mins Requirement
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Study (Mins)", modifier = Modifier.weight(1f))
                    Slider(
                        value = reqStudyMins.toFloat(),
                        onValueChange = { viewModel.updateStreakReqStudyMins(it.roundToInt()) },
                        valueRange = 0f..120f,
                        steps = 23,
                        modifier = Modifier.weight(2f)
                    )
                    Text("$reqStudyMins", modifier = Modifier.width(30.dp))
                }
            }

            item {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    "Streak Threshold",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Set the minimum completion percentage needed for the day to count as a streak.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Threshold", modifier = Modifier.weight(1f))
                    Slider(
                        value = partialThreshold,
                        onValueChange = { viewModel.updateStreakPartialThreshold(it) },
                        valueRange = 0.1f..1.0f,
                        steps = 8,
                        modifier = Modifier.weight(2f)
                    )
                    Text("${(partialThreshold * 100).roundToInt()}%", modifier = Modifier.width(40.dp))
                }
            }

            item {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Visuals & Animation",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Streak Fire Color", style = MaterialTheme.typography.bodyMedium)
                val colors = listOf("Theme", "#FF5722", "#FF9800", "#4CAF50", "#2196F3", "#9C27B0", "#E91E63", "#F44336")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    colors.forEach { hex ->
                        val isSelected = hex == colorHex
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (hex == "Theme") MaterialTheme.colorScheme.primary else Color(android.graphics.Color.parseColor(hex)))
                                .clickable { viewModel.updateStreakProgressColor(hex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Fire Brightness", modifier = Modifier.weight(1f))
                    Slider(
                        value = brightness,
                        onValueChange = { viewModel.updateStreakBrightness(it) },
                        valueRange = 0.5f..2.0f,
                        modifier = Modifier.weight(2f)
                    )
                    Text(String.format("%.1f", brightness), modifier = Modifier.width(30.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Animation Style (Override)", style = MaterialTheme.typography.bodyMedium)
                val styles = listOf("Default", "Material", "Bouncy", "Glass Liquid")
                styles.forEach { style ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateStreakAnimationOverride(style) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = animOverride == style,
                            onClick = { viewModel.updateStreakAnimationOverride(style) }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(style)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
