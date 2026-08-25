package lumia.tracker.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlin.math.roundToInt
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.StreakWidget
import lumia.tracker.ui.screens.settings.components.SettingsGroupCard
import lumia.tracker.ui.theme.bouncyClick
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Streak Goals & Fire Chamber",
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    BouncyIconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Live Fire Chamber Preview
            item {
                SettingsGroupCard(title = "Live Fire Chamber Preview", icon = Icons.Rounded.LocalFireDepartment) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(18.dp))
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
                        val primary = MaterialTheme.colorScheme.primary
                        val baseColor = if (colorHex == "Theme") primary else try {
                            Color(android.graphics.Color.parseColor(colorHex))
                        } catch (e: Exception) {
                            Color(0xFFFF9800)
                        }
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(baseColor.copy(alpha = 0.25f * brightness), Color.Transparent)
                                    )
                                )
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier.graphicsLayer {
                                    scaleX = 1.25f
                                    scaleY = 1.25f
                                }
                            ) {
                                StreakWidget(viewModel, navController)
                            }
                        }
                    }
                }
            }

            // 2. Daily Streak Goals
            item {
                SettingsGroupCard(title = "Daily Streak Targets", icon = Icons.Rounded.TrackChanges) {
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Set minimum daily requirements for a complete streak. Completing planned items sustains your streak chain.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Tasks Requirement
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tasks Required", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        "$reqTasks tasks",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Slider(
                                value = reqTasks.toFloat(),
                                onValueChange = { viewModel.updateStreakReqTasks(it.roundToInt()) },
                                valueRange = 0f..20f,
                                steps = 19,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Assignments Requirement
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Assignments Required", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        "$reqAssignments assignments",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Slider(
                                value = reqAssignments.toFloat(),
                                onValueChange = { viewModel.updateStreakReqAssignments(it.roundToInt()) },
                                valueRange = 0f..10f,
                                steps = 9,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Study Mins Requirement
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Study Focus (Minutes)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        "$reqStudyMins min",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Slider(
                                value = reqStudyMins.toFloat(),
                                onValueChange = { viewModel.updateStreakReqStudyMins(it.roundToInt()) },
                                valueRange = 0f..120f,
                                steps = 23,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // 3. Streak Threshold
            item {
                SettingsGroupCard(title = "Streak Completion Threshold", icon = Icons.Rounded.Speed) {
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Minimum Completion Rate", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    "${(partialThreshold * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "Set the minimum completion percentage needed for the day to count toward streak continuity",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = partialThreshold,
                            onValueChange = { viewModel.updateStreakPartialThreshold(it) },
                            valueRange = 0.1f..1.0f,
                            steps = 8,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 4. Fire Aesthetics & Physics
            item {
                SettingsGroupCard(title = "Visuals & Animation Style", icon = Icons.Rounded.Palette) {
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Streak Fire Color", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        val colors = listOf("Theme", "#FF5722", "#FF9800", "#4CAF50", "#2196F3", "#9C27B0", "#E91E63", "#F44336")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            colors.forEach { hex ->
                                val isSelected = hex == colorHex
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (hex == "Theme") MaterialTheme.colorScheme.primary
                                            else Color(android.graphics.Color.parseColor(hex))
                                        )
                                        .bouncyClick { viewModel.updateStreakProgressColor(hex) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Fire Brightness
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Fire Brightness Multiplier", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    String.format("%.1fx", brightness),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Slider(
                            value = brightness,
                            onValueChange = { viewModel.updateStreakBrightness(it) },
                            valueRange = 0.5f..2.0f,
                            modifier = Modifier.fillMaxWidth()
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        Text("Animation Style (Physics Override)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        val stylesWithDesc = listOf(
                            "Default" to "Classic smooth rotation and clean fire pulse.",
                            "Material" to "Expressive segment tracker with glowing corona aura.",
                            "Bouncy" to "Energetic dancing flames with orbiting active sparks.",
                            "Fluid Wave" to "Dynamic resonant container with fluid sine-wave motion."
                        )
                        stylesWithDesc.forEach { (style, description) ->
                            val isSelected = animOverride == style
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.updateStreakAnimationOverride(style) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.updateStreakAnimationOverride(style) }
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(style, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
                }
            }

            // 5. Streak Notifications Tone
            item {
                SettingsGroupCard(title = "Streak Reminders", icon = Icons.Rounded.Notifications) {
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Select the tone of voice for daily streak continuity notifications",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val tones = listOf(
                            "Motivational" to "Encouraging prompts to celebrate and keep your streak alive.",
                            "Aggressive" to "Strict taunts to ensure you never break your hard-earned discipline."
                        )
                        tones.forEach { (tone, desc) ->
                            val isSelected = notificationTone == tone
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.updateStreakNotificationTone(tone) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.updateStreakNotificationTone(tone) }
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(tone, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
