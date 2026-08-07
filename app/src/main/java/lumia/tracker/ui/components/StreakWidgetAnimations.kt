package lumia.tracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import kotlin.math.PI

data class StreakAnimationState(
    val animProgress: Float,
    val rotationDefault: Float,
    val completeScale: Float,
    val scaleMaterial: Float,
    val outerGlowAlpha: Float,
    val rotationMaterial: Float,
    val scaleBouncyX: Float,
    val scaleBouncyY: Float,
    val rotationBouncyFlame: Float,
    val rotationBouncy: Float,
    val sparkProgress: Float,
    val waveOffset: Float,
    val liquidPulse: Float,
    val scaleGlass: Float,
    val specularHighlight: Float
)

@Composable
fun rememberStreakAnimationState(
    streakPercentage: Float,
    animOverride: String,
    isCompleteToday: Boolean
): StreakAnimationState {
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

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_and_flow")

    val rotationDefault by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "rotation_default"
    )
    val completeScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if (isCompleteToday) 1.2f else 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "complete_pulse"
    )
    val scaleMaterial by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = if (isCompleteToday) 1.12f else 1.02f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "scale_material"
    )
    val outerGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "glow_material"
    )
    val rotationMaterial by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "rotation_material"
    )
    val scaleBouncyX by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = if (isCompleteToday) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "scale_bouncy_x"
    )
    val scaleBouncyY by infiniteTransition.animateFloat(
        initialValue = 1.2f, targetValue = if (isCompleteToday) 0.85f else 0.95f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "scale_bouncy_y"
    )
    val rotationBouncyFlame by infiniteTransition.animateFloat(
        initialValue = -12f, targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "rotation_bouncy_flame"
    )
    val rotationBouncy by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "rotation_bouncy"
    )
    val sparkProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "spark_progress"
    )
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "wave_offset"
    )
    val liquidPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "liquid_pulse"
    )
    val scaleGlass by infiniteTransition.animateFloat(
        initialValue = 0.98f, targetValue = if (isCompleteToday) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "scale_glass"
    )
    val specularHighlight by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4500, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "specular"
    )

    return StreakAnimationState(
        animProgress = animProgress,
        rotationDefault = rotationDefault,
        completeScale = completeScale,
        scaleMaterial = scaleMaterial,
        outerGlowAlpha = outerGlowAlpha,
        rotationMaterial = rotationMaterial,
        scaleBouncyX = scaleBouncyX,
        scaleBouncyY = scaleBouncyY,
        rotationBouncyFlame = rotationBouncyFlame,
        rotationBouncy = rotationBouncy,
        sparkProgress = sparkProgress,
        waveOffset = waveOffset,
        liquidPulse = liquidPulse,
        scaleGlass = scaleGlass,
        specularHighlight = specularHighlight
    )
}
