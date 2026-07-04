package lumia.tracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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

    val animProgress by animateFloatAsState(
        targetValue = streakPercentage,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "streak_progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
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

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .padding(end = 12.dp)
            .height(44.dp)
            .clip(CircleShape)
            .bouncyClick(onClick = { navController.navigate("settings/streaks") })
            .then(
                if (applyGlass) Modifier.liquidGlass(CircleShape, tintAlpha = if(isCompleteToday) 0.3f else 0.15f)
                else Modifier.background(if(isCompleteToday) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = streakCurrent.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = if (isCompleteToday) color else color.copy(alpha = 0.9f)
        )
        
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(28.dp)) {
            // Iconic glowing ring
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = rotation }
                    .drawWithContent {
                        if (animProgress > 0) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(color.copy(alpha = 0.1f), color, color.copy(alpha = 0.1f))
                                ),
                                startAngle = -90f,
                                sweepAngle = 360f * animProgress,
                                useCenter = false,
                                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        if (isCompleteToday) {
                            // Extra glow when complete
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(color.copy(alpha = 0.4f), Color.Transparent)
                                ),
                                radius = size.width
                            )
                        }
                    }
            )
            Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = "Streak",
                tint = if (isCompleteToday) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer { 
                        scaleX = completeScale
                        scaleY = completeScale
                    }
            )
        }
    }
}
