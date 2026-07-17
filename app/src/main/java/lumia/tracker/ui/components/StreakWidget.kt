package lumia.tracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.theme.LocalAppAnimationMode
import lumia.tracker.ui.theme.LocalGlassMode
import lumia.tracker.ui.theme.liquidGlass
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StreakWidget(viewModel: ScholarViewModel, navController: NavController, modifier: Modifier = Modifier) {
    val streakCurrent by viewModel.streakCurrent.collectAsStateWithLifecycle()
    val streakPercentage by viewModel.streakPercentage.collectAsStateWithLifecycle()
    val streakColorHex by viewModel.streakProgressColor.collectAsStateWithLifecycle()
    val streakBrightness by viewModel.streakBrightness.collectAsStateWithLifecycle()

    val primary = MaterialTheme.colorScheme.primary
    val baseColor = if (streakColorHex == "Theme") primary else try {
        Color(android.graphics.Color.parseColor(streakColorHex))
    } catch (e: Exception) {
        Color(0xFFFF9800)
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
    val isGlass = LocalGlassMode.current
        
    val animOverride by viewModel.streakAnimationOverride.collectAsStateWithLifecycle()
    val applyGlass = animOverride == "Glass Liquid" || (animOverride == "Default" && isGlass)

    val isCompleteToday by viewModel.streakIsCompleteToday.collectAsStateWithLifecycle()

    // Animating the progress ring filling up
    val animProgress by animateFloatAsState(
        targetValue = streakPercentage,
        animationSpec = when (animOverride) {
            "Bouncy" -> spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow)
            "Material" -> tween(durationMillis = 1400, easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1.0f))
            "Glass Liquid" -> tween(durationMillis = 2000, easing = LinearOutSlowInEasing)
            else -> tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        },
        label = "streak_progress"
    )

    // Infinite Transitions for custom mode animations
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

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .padding(end = 12.dp)
            .height(36.dp)
            .clip(CircleShape)
            .bouncyClick(onClick = { navController.navigate("settings/streaks") })
            .then(
                if (applyGlass) Modifier.liquidGlass(CircleShape, tintAlpha = if(isCompleteToday) 0.35f else 0.15f)
                else Modifier.background(if(isCompleteToday) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            )
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        Text(
            text = streakCurrent.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = if (isCompleteToday) color else color.copy(alpha = 0.9f)
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
                        // Background circle track
                        drawCircle(
                            color = color.copy(alpha = 0.08f),
                            radius = radius,
                            style = Stroke(width = strokeWidth)
                        )
                        // Segments dot markers representing progress intervals
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

                        // Thick polished progress arc with smooth Material curve
                        drawArc(
                            brush = ringBrush,
                            startAngle = -90f + rotationMaterial,
                            sweepAngle = 360f * animProgress,
                            useCenter = false,
                            style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                        
                        // Radiant outer corona glow if completed
                        if (isCompleteToday) {
                            drawCircle(
                                color = color.copy(alpha = 0.15f * outerGlowAlpha),
                                radius = radius + 3.dp.toPx(),
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                    }
                    "Bouncy" -> {
                        // Playful track
                        drawCircle(
                            color = color.copy(alpha = 0.1f),
                            radius = radius,
                            style = Stroke(width = strokeWidth)
                        )

                        // Springy rotating progress sweep arc
                        drawArc(
                            brush = ringBrush,
                            startAngle = -90f + rotationBouncy,
                            sweepAngle = 360f * animProgress,
                            useCenter = false,
                            style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Orbiting fiery sparks flying around the center
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
                    "Glass Liquid" -> {
                        // Frosted outer border
                        drawCircle(
                            color = color.copy(alpha = 0.05f),
                            radius = radius,
                            style = Stroke(width = strokeWidth)
                        )

                        // Radial backing glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(color.copy(alpha = 0.18f * liquidPulse), Color.Transparent),
                                radius = radius * 1.5f
                            )
                        )

                        // Sine-wave liquid level fill
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
                            
                            // Rich fluid vertical gradient filling the bottom
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

                        // Glossy specular highlight ring overlay
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
                        // Default high-fidelity clean design
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

            // Animate Fire Icon based on custom style selections
            val fireColor = when (animOverride) {
                "Material" -> {
                    if (isCompleteToday) color else color.copy(alpha = 0.65f).compositeOver(MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f))
                }
                "Bouncy" -> {
                    if (isCompleteToday) color else color.copy(alpha = 0.75f)
                }
                "Glass Liquid" -> {
                    if (isCompleteToday) Color.White else color.copy(alpha = 0.85f)
                }
                else -> {
                    if (isCompleteToday) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }
            }

            val iconScaleX = when (animOverride) {
                "Material" -> scaleMaterial
                "Bouncy" -> scaleBouncyX
                "Glass Liquid" -> scaleGlass
                else -> completeScale
            }
            
            val iconScaleY = when (animOverride) {
                "Material" -> scaleMaterial
                "Bouncy" -> scaleBouncyY
                "Glass Liquid" -> scaleGlass
                else -> completeScale
            }
            
            val iconRotation = when (animOverride) {
                "Bouncy" -> rotationBouncyFlame
                else -> 0f
            }

            Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = "Streak",
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
}
