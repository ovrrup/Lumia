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
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .padding(end = 12.dp)
            .height(40.dp)
            .clip(CircleShape)
            .bouncyClick(onClick = { navController.navigate("settings/streaks") })
            .then(
                if (applyGlass) Modifier.liquidGlass(CircleShape, tintAlpha = 0.15f)
                else Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = streakCurrent.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Icon(
            imageVector = Icons.Rounded.LocalFireDepartment,
            contentDescription = "Streak",
            tint = Color.Black, // Black will be overwritten
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    
                    val progressHeight = 1f - animProgress
                    
                    // Step 1: Fill entire icon with color
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

                    // Step 2: Make the empty part gray
                    if (animProgress < 1f) {
                        val path = androidx.compose.ui.graphics.Path()
                        path.moveTo(0f, 0f)
                        
                        val yOffset = size.height * progressHeight
                        if ((applyGlass || applyBouncy) && animProgress > 0f) {
                            val waveHeight = size.height * 0.08f
                            path.lineTo(0f, yOffset)
                            for (x in 0..size.width.toInt() step 2) {
                                val y = yOffset + kotlin.math.sin((x.toFloat() / size.width) * Math.PI * 2 + waveOffset).toFloat() * waveHeight
                                path.lineTo(x.toFloat(), y)
                            }
                            path.lineTo(size.width, 0f)
                        } else {
                            path.lineTo(0f, yOffset)
                            path.lineTo(size.width, yOffset)
                            path.lineTo(size.width, 0f)
                        }
                        path.close()
                        
                        clipPath(path) {
                            drawRect(
                                color = Color.Gray.copy(alpha = 0.3f),
                                blendMode = BlendMode.SrcIn
                            )
                        }
                    }
                }
        )
    }
}
