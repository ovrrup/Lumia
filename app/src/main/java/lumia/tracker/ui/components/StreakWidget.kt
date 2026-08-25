package lumia.tracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.theme.LocalAppAnimationMode
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * StreakWidget - Compact, animated flame badge with dynamic custom shaders / styles,
 * spring scaling on touch, and a comprehensive Streak Detail Bottom Sheet popup.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakWidget(
    viewModel: ScholarViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val streakCurrent by viewModel.streakCurrent.collectAsStateWithLifecycle()
    val streakPercentage by viewModel.streakPercentage.collectAsStateWithLifecycle()
    val streakColorHex by viewModel.streakProgressColor.collectAsStateWithLifecycle()
    val streakBrightness by viewModel.streakBrightness.collectAsStateWithLifecycle()

    val streakLongest by viewModel.streakLongest.collectAsStateWithLifecycle()
    val streakTotalNormal by viewModel.streakTotalNormal.collectAsStateWithLifecycle()
    val streakTotalComplete by viewModel.streakTotalComplete.collectAsStateWithLifecycle()
    val reqTasks by viewModel.streakRequirementTasks.collectAsStateWithLifecycle()
    val reqAssignments by viewModel.streakRequirementAssignments.collectAsStateWithLifecycle()
    val reqStudyMins by viewModel.streakRequirementStudyMins.collectAsStateWithLifecycle()
    val notifTone by viewModel.streakNotificationTone.collectAsStateWithLifecycle()

    var showStreakSheet by remember { mutableStateOf(false) }

    val primary = MaterialTheme.colorScheme.primary
    val baseColor = if (streakColorHex == "Theme") primary else try {
        Color(android.graphics.Color.parseColor(streakColorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.tertiary
    }

    val hsl = FloatArray(3)
    androidx.core.graphics.ColorUtils.colorToHSL(
        android.graphics.Color.argb(
            (baseColor.alpha * 255).toInt(),
            (baseColor.red * 255).toInt(),
            (baseColor.green * 255).toInt(),
            (baseColor.blue * 255).toInt()
        ), hsl
    )
    hsl[2] = (hsl[2] * streakBrightness).coerceIn(0f, 1f)
    val color = Color(androidx.core.graphics.ColorUtils.HSLToColor(hsl))

    val animationMode = LocalAppAnimationMode.current
    val animOverride by viewModel.streakAnimationOverride.collectAsStateWithLifecycle()
    val isCompleteToday by viewModel.streakIsCompleteToday.collectAsStateWithLifecycle()

    // Animating the progress ring filling up
    val animProgress by animateFloatAsState(
        targetValue = streakPercentage.coerceIn(0f, 1f),
        animationSpec = when (animOverride) {
            "Bouncy" -> spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow)
            "Material" -> tween(durationMillis = 1400, easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1.0f))
            "Fluid Wave", "Glass Liquid" -> tween(durationMillis = 2000, easing = LinearOutSlowInEasing)
            else -> tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        },
        label = "streak_progress"
    )

    // Infinite Transitions for custom animation modes
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_and_flow")

    // 1. DEFAULT: simple rotation and pulse
    val rotationDefault by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_default"
    )
    val completeScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCompleteToday) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "complete_pulse"
    )

    // 2. MATERIAL: Smooth subtle breathing & glowing outer ring pulse
    val scaleMaterial by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = if (isCompleteToday) 1.12f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_material"
    )
    val outerGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_material"
    )
    val rotationMaterial by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_material"
    )

    // 3. BOUNCY: Heavy squash/stretch and orbiting active sparks
    val scaleBouncyX by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = if (isCompleteToday) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_bouncy_x"
    )
    val scaleBouncyY by infiniteTransition.animateFloat(
        initialValue = 1.2f,
        targetValue = if (isCompleteToday) 0.85f else 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_bouncy_y"
    )
    val rotationBouncyFlame by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation_bouncy_flame"
    )
    val rotationBouncy by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_bouncy"
    )
    val sparkProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spark_progress"
    )

    // 4. GLASS LIQUID: Fluid sine-wave displacement & glowing pulse
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )
    val liquidPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liquid_pulse"
    )
    val scaleGlass by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = if (isCompleteToday) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_glass"
    )
    val specularHighlight by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "specular"
    )

    // Streak Widget Pill
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .height(42.dp)
            .clip(CircleShape)
            .background(
                if (isCompleteToday) color.copy(alpha = 0.18f)
                else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .border(
                0.75.dp,
                if (isCompleteToday) color.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                CircleShape
            )
            .bouncyClick(onClick = { showStreakSheet = true })
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = streakCurrent.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = if (isCompleteToday) color else MaterialTheme.colorScheme.onSurface
        )

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(28.dp)) {
            val ringBrush = Brush.sweepGradient(
                colors = listOf(color.copy(alpha = 0.15f), color, color.copy(alpha = 0.15f))
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasSize = size
                val radius = canvasSize.width / 2
                val strokeWidth = 3.dp.toPx()

                when (animOverride) {
                    "Material" -> {
                        drawCircle(
                            color = color.copy(alpha = 0.08f),
                            radius = radius,
                            style = Stroke(width = strokeWidth)
                        )
                        val segments = 8
                        for (i in 0 until segments) {
                            val angle = (i * 360f / segments) * (PI / 180).toFloat()
                            val dotX = center.x + (radius) * cos(angle)
                            val dotY = center.y + (radius) * sin(angle)
                            drawCircle(
                                color = color.copy(alpha = 0.25f),
                                radius = 1.5.dp.toPx(),
                                center = Offset(dotX, dotY)
                            )
                        }
                        drawArc(
                            brush = ringBrush,
                            startAngle = -90f + rotationMaterial,
                            sweepAngle = 360f * animProgress,
                            useCenter = false,
                            style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                        if (isCompleteToday) {
                            drawCircle(
                                color = color.copy(alpha = 0.15f * outerGlowAlpha),
                                radius = radius + 3.dp.toPx(),
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                    }
                    "Bouncy" -> {
                        drawCircle(
                            color = color.copy(alpha = 0.1f),
                            radius = radius,
                            style = Stroke(width = strokeWidth)
                        )
                        drawArc(
                            brush = ringBrush,
                            startAngle = -90f + rotationBouncy,
                            sweepAngle = 360f * animProgress,
                            useCenter = false,
                            style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                        if (animProgress > 0) {
                            val sparkCount = 3
                            for (i in 0 until sparkCount) {
                                val offsetPhase = (i * (2 * PI / sparkCount)).toFloat()
                                val angle = (sparkProgress * 2 * PI + offsetPhase).toFloat()
                                val orbitRadius = radius + (2.5.dp.toPx() * sin(sparkProgress * 4 * PI + i).toFloat())
                                val sparkX = center.x + orbitRadius * cos(angle)
                                val sparkY = center.y + orbitRadius * sin(angle)
                                drawCircle(
                                    color = color.copy(alpha = 0.85f),
                                    radius = 2.dp.toPx(),
                                    center = Offset(sparkX, sparkY)
                                )
                            }
                        }
                    }
                    "Fluid Wave", "Glass Liquid" -> {
                        drawCircle(
                            color = color.copy(alpha = 0.08f),
                            radius = radius,
                            style = Stroke(width = strokeWidth)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(color.copy(alpha = 0.18f * liquidPulse), Color.Transparent),
                                radius = radius * 1.5f
                            )
                        )
                        val wavePath = Path()
                        val fillLevel = animProgress.coerceIn(0f, 1f)
                        if (fillLevel > 0f) {
                            val liquidHeight = canvasSize.height * (1f - fillLevel)
                            wavePath.moveTo(0f, canvasSize.height)
                            for (x in 0..canvasSize.width.toInt()) {
                                val y = liquidHeight + 2.5.dp.toPx() * sin((x * 0.15f) + waveOffset).toFloat()
                                wavePath.lineTo(x.toFloat(), y)
                            }
                            wavePath.lineTo(canvasSize.width, canvasSize.height)
                            wavePath.close()

                            val circleClipPath = Path().apply {
                                addOval(androidx.compose.ui.geometry.Rect(0f, 0f, canvasSize.width, canvasSize.height))
                            }
                            drawContext.canvas.save()
                            drawContext.canvas.clipPath(circleClipPath)
                            drawPath(
                                path = wavePath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(color.copy(alpha = 0.65f), color.copy(alpha = 0.2f)),
                                    startY = liquidHeight,
                                    endY = canvasSize.height
                                )
                            )
                            drawContext.canvas.restore()
                        }
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(Color.White.copy(alpha = 0.6f), color, Color.White.copy(alpha = 0.1f), color, Color.White.copy(alpha = 0.6f))
                            ),
                            startAngle = specularHighlight,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 3.5.dp.toPx())
                        )
                    }
                    else -> {
                        drawCircle(
                            color = color.copy(alpha = 0.1f),
                            radius = radius,
                            style = Stroke(width = strokeWidth)
                        )
                        drawArc(
                            brush = ringBrush,
                            startAngle = -90f + rotationDefault,
                            sweepAngle = 360f * animProgress,
                            useCenter = false,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                        if (isCompleteToday) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(color.copy(alpha = 0.35f), Color.Transparent),
                                    radius = radius * 1.8f
                                )
                            )
                        }
                    }
                }
            }

            // Dynamic flame color and scaling
            val fireColor = when (animOverride) {
                "Material" -> if (isCompleteToday) color else color.copy(alpha = 0.75f)
                "Bouncy" -> if (isCompleteToday) color else color.copy(alpha = 0.85f)
                "Fluid Wave", "Glass Liquid" -> if (isCompleteToday) Color.White else color.copy(alpha = 0.9f)
                else -> if (isCompleteToday) color else color.copy(alpha = 0.75f)
            }

            val iconScaleX = when (animOverride) {
                "Material" -> scaleMaterial
                "Bouncy" -> scaleBouncyX
                "Fluid Wave", "Glass Liquid" -> scaleGlass
                else -> completeScale
            }
            val iconScaleY = when (animOverride) {
                "Material" -> scaleMaterial
                "Bouncy" -> scaleBouncyY
                "Fluid Wave", "Glass Liquid" -> scaleGlass
                else -> completeScale
            }
            val iconRotation = when (animOverride) {
                "Bouncy" -> rotationBouncyFlame
                else -> 0f
            }

            Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = "Streak Flame",
                tint = fireColor,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer {
                        scaleX = iconScaleX
                        scaleY = iconScaleY
                        rotationZ = iconRotation
                    }
            )
        }
    }

    // Modal Bottom Sheet: Interactive Streak Statistics & Daily Milestones
    if (showStreakSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showStreakSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Hero Flame Badge
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f),
                    border = BorderStroke(1.5.dp, color.copy(alpha = 0.4f)),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$streakCurrent Day Streak!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isCompleteToday) "Daily requirements completed! 🔥" else "Keep learning today to sustain your streak",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Today's Progress Card
                ScholarCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today's Progress",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${(streakPercentage * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                color = color
                            )
                        }

                        LinearProgressIndicator(
                            progress = { animProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = color,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        // Requirements breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StreakReqItem(
                                icon = Icons.Rounded.CheckCircle,
                                label = "Tasks",
                                target = "$reqTasks planned"
                            )
                            StreakReqItem(
                                icon = Icons.Rounded.Assignment,
                                label = "Assignments",
                                target = "$reqAssignments required"
                            )
                            StreakReqItem(
                                icon = Icons.Rounded.Timer,
                                label = "Study Focus",
                                target = "$reqStudyMins mins"
                            )
                        }
                    }
                }

                // Milestone Statistics Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StreakStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Best Streak",
                        value = "$streakLongest Days",
                        icon = Icons.Rounded.EmojiEvents,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    StreakStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Perfect Days",
                        value = "$streakTotalComplete",
                        icon = Icons.Rounded.Star,
                        tint = color
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showStreakSheet = false
                            navController.navigate("settings/streaks")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Settings", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { showStreakSheet = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = color)
                    ) {
                        Text(
                            text = if (isCompleteToday) "Awesome" else "Keep Going",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakReqItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    target: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = target,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StreakStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    ScholarCard(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
