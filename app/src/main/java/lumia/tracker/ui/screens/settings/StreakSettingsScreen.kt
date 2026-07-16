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
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import lumia.tracker.ui.screens.SettingsGroupCard
import lumia.tracker.ui.theme.bouncyClick
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlin.math.roundToInt
import lumia.tracker.ui.theme.LocalAppAnimationMode
import lumia.tracker.ui.theme.LocalGlassMode
import lumia.tracker.ui.theme.liquidGlass
import lumia.tracker.viewmodel.ScholarViewModel


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
    val notificationTone by viewModel.streakNotificationTone.collectAsStateWithLifecycle()

    val isGlass = LocalGlassMode.current

    Scaffold(
        topBar = {
            lumia.tracker.ui.components.UniversalCapsuleHeader(
                title = "Streak Settings",
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsGroupCard(title = "Live Fire Chamber Preview", icon = Icons.Rounded.LocalFireDepartment) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Ambient backdrop glow reflecting the selected color
                        val primary = MaterialTheme.colorScheme.primary
                        val baseColor = if (colorHex == "Theme") primary else try {
                            Color(android.graphics.Color.parseColor(colorHex))
                        } catch (e: Exception) {
                            Color(0xFFFF9800)
                        }
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(baseColor.copy(alpha = 0.2f * brightness), Color.Transparent)
                                    )
                                )
                        )
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Live Chamber Preview",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = 1.3f
                                        scaleY = 1.3f
                                    }
                            ) {
                                lumia.tracker.ui.components.StreakWidget(viewModel, navController)
                            }
                        }
                    }
                }
            }

            item {
                SettingsGroupCard(
                    title = "Streak Goals", 
                    icon = Icons.Rounded.List,
                    infoText = "Set minimum daily requirements for a complete streak. Note: If you plan more than these limits, you'll need to complete all planned items to maintain your streak."
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
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
                        Text("$reqTasks", modifier = Modifier.width(30.dp), textAlign = TextAlign.End)
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
                        Text("$reqAssignments", modifier = Modifier.width(30.dp), textAlign = TextAlign.End)
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
                        Text("$reqStudyMins", modifier = Modifier.width(30.dp), textAlign = TextAlign.End)
                    }
                }
            }
            
            item {
                SettingsGroupCard(
                    title = "Streak Threshold", 
                    icon = Icons.Rounded.Speed,
                    infoText = "Set the minimum completion percentage needed for the day to count as a normal streak."
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Threshold", modifier = Modifier.weight(1f))
                        Slider(
                            value = partialThreshold,
                            onValueChange = { viewModel.updateStreakPartialThreshold(it) },
                            valueRange = 0.1f..1.0f,
                            steps = 8,
                            modifier = Modifier.weight(2f)
                        )
                        Text("${(partialThreshold * 100).roundToInt()}%", modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
                    }
                }
            }
            
            item {
                SettingsGroupCard(title = "Visuals & Animation", icon = Icons.Rounded.Palette) {
                    Text("Streak Fire Color", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    val colors = listOf("Theme", "#FF5722", "#FF9800", "#4CAF50", "#2196F3", "#9C27B0", "#E91E63", "#F44336")
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        colors.forEach { hex ->
                            val isSelected = hex == colorHex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (hex == "Theme") MaterialTheme.colorScheme.primary else Color(android.graphics.Color.parseColor(hex)))
                                    .bouncyClick { viewModel.updateStreakProgressColor(hex) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Fire Brightness", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Slider(
                            value = brightness,
                            onValueChange = { viewModel.updateStreakBrightness(it) },
                            valueRange = 0.5f..2.0f,
                            modifier = Modifier.weight(1f)
                        )
                        Text(String.format("%.1f", brightness), modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Animation Style (Override)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    val stylesWithDesc = listOf(
                        "Default" to "Classic smooth rotation and clean fire pulse.",
                        "Material" to "Expressive segment tracker with glowing corona aura.",
                        "Bouncy" to "Energetic dancing flames with orbiting active sparks.",
                        "Glass Liquid" to "Glossy frosted glass container with a fluid sine wave."
                    )
                    stylesWithDesc.forEach { (style, description) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .bouncyClick { viewModel.updateStreakAnimationOverride(style) }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = animOverride == style,
                                onClick = { viewModel.updateStreakAnimationOverride(style) }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(style, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                SettingsGroupCard(
                    title = "Notifications", 
                    icon = Icons.Rounded.Notifications,
                    infoText = "Select the tone of voice for your daily streak reminders."
                ) {
                    Text("Notification Tone", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    val tones = listOf("Motivational", "Aggressive")
                    tones.forEach { tone ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .bouncyClick { viewModel.updateStreakNotificationTone(tone) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = notificationTone == tone,
                                onClick = { viewModel.updateStreakNotificationTone(tone) }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(tone, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
