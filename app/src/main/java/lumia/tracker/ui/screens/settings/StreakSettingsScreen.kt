package lumia.tracker.ui.screens.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlin.math.roundToInt
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.GlassCapsule
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.components.ScholarCardDefaults
import lumia.tracker.ui.components.StreakWidget
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel

@ValueScore(
    score = 92,
    importance = Importance.HIGH,
    description = "Streak targets, partial threshold, glowing visuals, and celebration calibration",
    category = "Settings"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakSettingsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val reqTasks by viewModel.streakRequirementTasks.collectAsStateWithLifecycle()
    val reqStudyMins by viewModel.streakRequirementStudyMins.collectAsStateWithLifecycle()
    val reqAssignments by viewModel.streakRequirementAssignments.collectAsStateWithLifecycle()
    val partialThreshold by viewModel.streakPartialThreshold.collectAsStateWithLifecycle()
    val brightness by viewModel.streakBrightness.collectAsStateWithLifecycle()
    val streakProgressColor by viewModel.streakProgressColor.collectAsStateWithLifecycle()
    val streakAnimationOverride by viewModel.streakAnimationOverride.collectAsStateWithLifecycle()
    val streakNotificationTone by viewModel.streakNotificationTone.collectAsStateWithLifecycle()

    val streakCurrent by viewModel.streakCurrent.collectAsStateWithLifecycle()
    val streakLongest by viewModel.streakLongest.collectAsStateWithLifecycle()
    val streakPercentage by viewModel.streakPercentage.collectAsStateWithLifecycle()
    val isCompleteToday by viewModel.streakIsCompleteToday.collectAsStateWithLifecycle()

    // Calculate dynamic ambient streak accent color
    val primary = MaterialTheme.colorScheme.primary
    val baseColor = if (streakProgressColor == "Theme") primary else try {
        Color(android.graphics.Color.parseColor(streakProgressColor))
    } catch (e: Exception) {
        primary
    }

    val hsl = remember(baseColor, brightness) {
        val array = FloatArray(3)
        androidx.core.graphics.ColorUtils.colorToHSL(
            android.graphics.Color.argb(
                (baseColor.alpha * 255).toInt(),
                (baseColor.red * 255).toInt(),
                (baseColor.green * 255).toInt(),
                (baseColor.blue * 255).toInt()
            ), array
        )
        array[2] = (array[2] * brightness).coerceIn(0f, 1f)
        array
    }
    val activeStreakColor = remember(hsl) {
        Color(androidx.core.graphics.ColorUtils.HSLToColor(hsl))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Streak Goals & Visuals",
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
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            // 1. Live Interactive Preview with Glowing Ambient Halo & Capsule Badges
            item {
                ScholarCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(26.dp),
                    glassmorphic = true
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(activeStreakColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = activeStreakColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Live Streak Preview",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            GlassCapsule(
                                containerColor = activeStreakColor.copy(alpha = 0.15f),
                                border = BorderStroke(0.6.dp, activeStreakColor.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = "${(streakPercentage * 100).roundToInt()}% Goal Met",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = activeStreakColor,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.5.dp)
                                )
                            }
                        }

                        // Preview Box with Glowing Ambient Radiant Halo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(125.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Organic Radial Gradient Glow
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val glowRadius = size.minDimension * 0.72f
                                val glowAlpha = (0.35f * brightness).coerceIn(0.10f, 0.65f)
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            activeStreakColor.copy(alpha = glowAlpha),
                                            activeStreakColor.copy(alpha = glowAlpha * 0.45f),
                                            Color.Transparent
                                        ),
                                        center = center,
                                        radius = glowRadius
                                    )
                                )
                            }

                            // Interactive Widget
                            StreakWidget(viewModel, navController)
                        }

                        // Capsule Pills Metrics Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlassCapsule(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.FlashOn,
                                        contentDescription = null,
                                        tint = activeStreakColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "${streakCurrent}d current",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            GlassCapsule(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.EmojiEvents,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "${streakLongest}d best",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            GlassCapsule(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Animation,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = streakAnimationOverride,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Daily Streak Goals & Sliders with Capsule Pills & Quick Presets
            item {
                ScholarCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    glassmorphic = true
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.TrackChanges,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Daily Target Thresholds",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            GlassCapsule(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "${reqTasks}T • ${reqStudyMins}m • ${reqAssignments}A",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Target 1: Tasks Required
                        ModernStreakSlider(
                            title = "Tasks Required",
                            subtitle = "Completed tasks counted toward today's streak",
                            value = reqTasks.toFloat(),
                            valueLabel = if (reqTasks == 0) "Off" else "$reqTasks tasks",
                            valueRange = 0f..10f,
                            steps = 9,
                            accentColor = activeStreakColor,
                            icon = Icons.Rounded.CheckCircle,
                            presets = listOf(0 to "Off", 2 to "2", 3 to "3", 5 to "5", 8 to "8"),
                            onValueChange = { viewModel.updateStreakRequirementTasks(it.roundToInt()) }
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Target 2: Study Minutes Required
                        ModernStreakSlider(
                            title = "Study Duration",
                            subtitle = "Pomodoro & focus minutes counted toward streak",
                            value = reqStudyMins.toFloat(),
                            valueLabel = if (reqStudyMins == 0) "Off" else if (reqStudyMins >= 60) "${reqStudyMins / 60}h ${reqStudyMins % 60}m" else "$reqStudyMins min",
                            valueRange = 0f..180f,
                            steps = 17,
                            accentColor = activeStreakColor,
                            icon = Icons.Rounded.Timer,
                            presets = listOf(0 to "Off", 25 to "25m", 45 to "45m", 60 to "60m", 90 to "90m", 120 to "120m"),
                            onValueChange = { viewModel.updateStreakRequirementStudyMins(it.roundToInt()) }
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Target 3: Assignments Required
                        ModernStreakSlider(
                            title = "Assignments Due",
                            subtitle = "Submitted assignments counted toward streak",
                            value = reqAssignments.toFloat(),
                            valueLabel = if (reqAssignments == 0) "Off" else "$reqAssignments assignments",
                            valueRange = 0f..5f,
                            steps = 4,
                            accentColor = activeStreakColor,
                            icon = Icons.Rounded.AssignmentTurnedIn,
                            presets = listOf(0 to "Off", 1 to "1", 2 to "2", 3 to "3"),
                            onValueChange = { viewModel.updateStreakRequirementAssignments(it.roundToInt()) }
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Target 4: Partial Credit Threshold
                        ModernStreakSlider(
                            title = "Partial Credit Threshold",
                            subtitle = "Minimum daily progress to secure partial streak",
                            value = partialThreshold,
                            valueLabel = "${(partialThreshold * 100).roundToInt()}%",
                            valueRange = 0.1f..0.9f,
                            steps = 7,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            icon = Icons.Rounded.DonutLarge,
                            presets = listOf(0.25f to "25%", 0.33f to "33%", 0.50f to "50%", 0.66f to "66%", 0.75f to "75%"),
                            onValueChange = { viewModel.updateStreakPartialThreshold(it) }
                        )
                    }
                }
            }

            // 3. Flame & Glow Color Pickers with Capsule Pills & Glowing Ambient Effects
            item {
                ScholarCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    glassmorphic = true
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(activeStreakColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Palette,
                                        contentDescription = null,
                                        tint = activeStreakColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Streak Flame Hue",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            GlassCapsule(
                                containerColor = activeStreakColor.copy(alpha = 0.15f),
                                border = BorderStroke(0.6.dp, activeStreakColor.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = if (streakProgressColor == "Theme") "Theme Dynamic" else streakProgressColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = activeStreakColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "Flame color accents, progress ring, and ambient glow radiance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Color Capsule Pills Horizontal Flow
                        val colorPalettes = listOf(
                            Triple("Theme", "Theme Dynamic", MaterialTheme.colorScheme.primary),
                            Triple("#FF5722", "Solar Flame", Color(0xFFFF5722)),
                            Triple("#FF9800", "Amber Flare", Color(0xFFFF9800)),
                            Triple("#FFC107", "Golden Sun", Color(0xFFFFC107)),
                            Triple("#E53935", "Crimson Fire", Color(0xFFE53935)),
                            Triple("#8E24AA", "Cosmic Violet", Color(0xFF8E24AA)),
                            Triple("#1E88E5", "Electric Blue", Color(0xFF1E88E5)),
                            Triple("#00BCD4", "Cyber Cyan", Color(0xFF00BCD4)),
                            Triple("#4CAF50", "Emerald Glow", Color(0xFF4CAF50)),
                            Triple("#E91E63", "Neon Rose", Color(0xFFE91E63))
                        )

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                        ) {
                            items(colorPalettes) { (colorKey, colorLabel, swatchColor) ->
                                val isSelected = streakProgressColor == colorKey
                                val haptic = LocalHapticFeedback.current

                                Surface(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .bouncyClick {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.updateStreakProgressColor(colorKey)
                                        },
                                    shape = CircleShape,
                                    color = if (isSelected) swatchColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = if (isSelected) BorderStroke(1.5.dp, swatchColor) else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(swatchColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = colorLabel,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            color = if (isSelected) swatchColor else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Animation Style Selectors with Capsule Pills
            item {
                ScholarCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    glassmorphic = true
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MotionPhotosOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Animation Style",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            GlassCapsule(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = streakAnimationOverride,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "Tactile bounce, glow pulsing, and flame physics dynamics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // 4 Animation Profile Capsule Cards
                        val animationProfiles = listOf(
                            Triple("Default", "Adaptive fluid flame physics", Icons.Rounded.AutoAwesome),
                            Triple("Bouncy", "Playful spring bounce on click", Icons.Rounded.TouchApp),
                            Triple("Pulse", "Rhythmic glowing ambient aura", Icons.Rounded.GraphicEq),
                            Triple("Subtle", "Gentle minimalist flame drift", Icons.Rounded.LensBlur)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            animationProfiles.forEach { (animKey, animDesc, animIcon) ->
                                val isSelected = streakAnimationOverride == animKey
                                val haptic = LocalHapticFeedback.current

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .bouncyClick {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.updateStreakAnimationOverride(animKey)
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) activeStreakColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                                    border = if (isSelected) BorderStroke(1.2.dp, activeStreakColor.copy(alpha = 0.6f)) else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(
                                                        if (isSelected) activeStreakColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = animIcon,
                                                    contentDescription = null,
                                                    tint = if (isSelected) activeStreakColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = animKey,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                    color = if (isSelected) activeStreakColor else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = animDesc,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        if (isSelected) {
                                            GlassCapsule(
                                                containerColor = activeStreakColor.copy(alpha = 0.18f),
                                                border = BorderStroke(0.6.dp, activeStreakColor.copy(alpha = 0.5f))
                                            ) {
                                                Text(
                                                    text = "Active",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = activeStreakColor,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Ambient Glow Radiance Slider & Glowing Ribbon
            item {
                ScholarCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    glassmorphic = true
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(activeStreakColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Flare,
                                        contentDescription = null,
                                        tint = activeStreakColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Glow & Ambient Radiance",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            GlassCapsule(
                                containerColor = activeStreakColor.copy(alpha = 0.15f),
                                border = BorderStroke(0.6.dp, activeStreakColor.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = "${(brightness * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = activeStreakColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "Calibrate backlight intensity and glowing radiant diffusion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Glowing Ambient Diffusion Ribbon
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            activeStreakColor.copy(alpha = 0.05f),
                                            activeStreakColor.copy(alpha = (0.55f * brightness).coerceIn(0.12f, 1f)),
                                            activeStreakColor.copy(alpha = 0.05f)
                                        )
                                    )
                                )
                        )

                        // Slider
                        Slider(
                            value = brightness,
                            onValueChange = { viewModel.updateStreakBrightness(it) },
                            valueRange = 0.2f..1.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = activeStreakColor,
                                activeTrackColor = activeStreakColor,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Quick Brightness Presets
                        val brightnessPresets = listOf(
                            0.5f to "Subtle",
                            0.8f to "Soft",
                            1.0f to "Standard",
                            1.3f to "Vibrant",
                            1.5f to "Hyper"
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            brightnessPresets.forEach { (presetVal, presetName) ->
                                val isSelected = kotlin.math.abs(brightness - presetVal) < 0.08f
                                val haptic = LocalHapticFeedback.current

                                Surface(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .bouncyClick {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.updateStreakBrightness(presetVal)
                                        },
                                    shape = CircleShape,
                                    color = if (isSelected) activeStreakColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = if (isSelected) BorderStroke(1.2.dp, activeStreakColor.copy(alpha = 0.6f)) else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                                ) {
                                    Text(
                                        text = presetName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isSelected) activeStreakColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Celebration Milestone Notification Tone
            item {
                ScholarCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    glassmorphic = true
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(activeStreakColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.RecordVoiceOver,
                                        contentDescription = null,
                                        tint = activeStreakColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Celebration Tone",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            GlassCapsule(
                                containerColor = activeStreakColor.copy(alpha = 0.15f),
                                border = BorderStroke(0.6.dp, activeStreakColor.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = streakNotificationTone,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = activeStreakColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "Milestone announcement tone when daily goal targets are achieved",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Milestone Tone Option Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val tones = listOf(
                                Triple("Motivational", "Inspiring & uplifting celebrations", Icons.Rounded.AutoAwesome),
                                Triple("Aggressive", "Strict & high-accountability alerts", Icons.Rounded.Bolt)
                            )

                            tones.forEach { (toneName, toneDesc, toneIcon) ->
                                val isSelected = streakNotificationTone == toneName
                                val haptic = LocalHapticFeedback.current

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .bouncyClick {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.updateStreakNotificationTone(toneName)
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) activeStreakColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                                    border = if (isSelected) BorderStroke(1.2.dp, activeStreakColor.copy(alpha = 0.6f)) else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = toneIcon,
                                                contentDescription = null,
                                                tint = if (isSelected) activeStreakColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = toneName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                color = if (isSelected) activeStreakColor else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            text = toneDesc,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
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

/**
 * ModernStreakSlider - Elegant slider item featuring icon squircle,
 * capsule pill value indicator, decluttered subtitle, and quick preset pills.
 */
@Composable
private fun <T : Number> ModernStreakSlider(
    title: String,
    subtitle: String,
    value: Float,
    valueLabel: String,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    accentColor: Color,
    icon: ImageVector,
    presets: List<Pair<T, String>>,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Label Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Capsule Value Pill
            GlassCapsule(
                containerColor = accentColor.copy(alpha = 0.14f),
                border = BorderStroke(0.6.dp, accentColor.copy(alpha = 0.45f))
            ) {
                Text(
                    text = valueLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp)
                )
            }
        }

        // Subtitle
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Slider
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Quick Preset Capsule Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presets.forEach { (presetVal, presetText) ->
                val presetFloat = presetVal.toFloat()
                val isSelected = kotlin.math.abs(value - presetFloat) < 0.05f
                val haptic = LocalHapticFeedback.current

                Surface(
                    modifier = Modifier
                        .clip(CircleShape)
                        .bouncyClick {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onValueChange(presetFloat)
                        },
                    shape = CircleShape,
                    color = if (isSelected) accentColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = if (isSelected) BorderStroke(1.2.dp, accentColor.copy(alpha = 0.6f)) else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = presetText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}
