package lumia.tracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
    val applyGlass = false

    val isCompleteToday by viewModel.streakIsCompleteToday.collectAsStateWithLifecycle()

    val animState = rememberStreakAnimationState(
        streakPercentage = streakPercentage,
        animOverride = animOverride,
        isCompleteToday = isCompleteToday
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .padding(end = 12.dp)
            .height(36.dp)
            .clip(CircleShape)
            .bouncyClick(onClick = { navController.navigate("settings/streaks") })
            .background(if (isCompleteToday) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        Text(
            text = streakCurrent.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = if (isCompleteToday) color else color.copy(alpha = 0.9f)
        )
        
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(28.dp)) {
            StreakWidgetCanvas(
                modifier = Modifier.fillMaxSize(),
                color = color,
                animOverride = animOverride,
                animProgress = animState.animProgress,
                rotationDefault = animState.rotationDefault,
                rotationMaterial = animState.rotationMaterial,
                rotationBouncy = animState.rotationBouncy,
                sparkProgress = animState.sparkProgress,
                waveOffset = animState.waveOffset,
                liquidPulse = animState.liquidPulse,
                specularHighlight = animState.specularHighlight,
                outerGlowAlpha = animState.outerGlowAlpha,
                isCompleteToday = isCompleteToday
            )

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
                "Material" -> animState.scaleMaterial
                "Bouncy" -> animState.scaleBouncyX
                "Glass Liquid" -> animState.scaleGlass
                else -> animState.completeScale
            }
            
            val iconScaleY = when (animOverride) {
                "Material" -> animState.scaleMaterial
                "Bouncy" -> animState.scaleBouncyY
                "Glass Liquid" -> animState.scaleGlass
                else -> animState.completeScale
            }
            
            val iconRotation = when (animOverride) {
                "Bouncy" -> animState.rotationBouncyFlame
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
