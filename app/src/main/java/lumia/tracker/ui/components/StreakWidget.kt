package lumia.tracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.theme.LocalAppAnimationMode
import lumia.tracker.ui.theme.LocalGlassMode
import lumia.tracker.ui.theme.liquidGlass
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel
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

    // Adjust brightness
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
    val applyBouncy = animOverride == "Bouncy" || (animOverride == "Default" && animationMode == "Bouncy")

    val animProgress by animateFloatAsState(
        targetValue = streakPercentage,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "streak_progress"
    )

    // Wave animation for liquid effect
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    val progressHeight = 1f - animProgress

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(end = 12.dp)
            .height(44.dp)
            .clip(CircleShape)
            .bouncyClick(onClick = { navController.navigate("settings/streaks") })
            .then(
                if (applyGlass) Modifier.liquidGlass(CircleShape, tintAlpha = 0.15f)
                else Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    if (applyGlass || applyBouncy) {
                        drawRect(
                            color = color,
                            blendMode = BlendMode.SrcIn
                        )
                        // Draw empty part with outline style or lower opacity
                        clipRect(bottom = size.height * progressHeight) {
                            drawRect(
                                color = Color.Gray.copy(alpha = 0.3f),
                                blendMode = BlendMode.SrcIn
                            )
                        }
                        
                        // Draw liquid wave if partially filled
                        if (animProgress > 0f && animProgress < 1f) {
                            val waveHeight = size.height * 0.1f
                            val yOffset = size.height * progressHeight
                            
                            val path = androidx.compose.ui.graphics.Path()
                            path.moveTo(0f, size.height)
                            path.lineTo(0f, yOffset)
                            
                            for (x in 0..size.width.toInt() step 2) {
                                val y = yOffset + sin((x.toFloat() / size.width) * Math.PI * 2 + waveOffset).toFloat() * waveHeight
                                path.lineTo(x.toFloat(), y)
                            }
                            
                            path.lineTo(size.width, size.height)
                            path.close()
                            
                            clipPath(path) {
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(color.copy(alpha = 0.8f), color)
                                    ),
                                    blendMode = BlendMode.SrcIn
                                )
                                if (applyGlass) {
                                    drawRect(
                                        color = Color.White.copy(alpha = 0.4f),
                                        blendMode = BlendMode.ColorDodge
                                    )
                                }
                            }
                        }
                    } else {
                        // Simple Material fill
                        drawRect(
                            color = color,
                            blendMode = BlendMode.SrcIn
                        )
                        clipRect(bottom = size.height * progressHeight) {
                            drawRect(
                                color = Color.Gray.copy(alpha = 0.3f),
                                blendMode = BlendMode.SrcIn
                            )
                        }
                    }
                }
        ) {
            Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = "Streak Progress",
                tint = Color.Black // Used as mask
            )
        }
        
        Spacer(modifier = Modifier.width(6.dp))
        
        Text(
            text = "$streakCurrent",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = if (animProgress >= 1f) color else MaterialTheme.colorScheme.onSurface
        )
    }
}
