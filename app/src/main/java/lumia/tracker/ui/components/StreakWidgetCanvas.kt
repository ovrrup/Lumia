package lumia.tracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StreakWidgetCanvas(
    modifier: Modifier = Modifier,
    color: Color,
    animOverride: String,
    animProgress: Float,
    rotationDefault: Float,
    rotationMaterial: Float,
    rotationBouncy: Float,
    sparkProgress: Float,
    waveOffset: Float,
    liquidPulse: Float,
    specularHighlight: Float,
    outerGlowAlpha: Float,
    isCompleteToday: Boolean
) {
    val ringBrush = Brush.sweepGradient(
        colors = listOf(color.copy(alpha = 0.15f), color, color.copy(alpha = 0.15f))
    )

    Canvas(modifier = modifier) {
        val canvasSize = size
        val radius = canvasSize.width / 2
        val strokeWidth = 3.dp.toPx()

        when (animOverride) {
            "Material" -> {
                drawCircle(color = color.copy(alpha = 0.08f), radius = radius, style = Stroke(width = strokeWidth))
                val segments = 8
                for (i in 0 until segments) {
                    val angle = (i * 360f / segments) * (PI / 180).toFloat()
                    val dotX = center.x + radius * cos(angle)
                    val dotY = center.y + radius * sin(angle)
                    drawCircle(color = color.copy(alpha = 0.25f), radius = 1.5.dp.toPx(), center = Offset(dotX, dotY))
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
                drawCircle(color = color.copy(alpha = 0.1f), radius = radius, style = Stroke(width = strokeWidth))
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
                        drawCircle(color = color.copy(alpha = 0.85f), radius = 2.dp.toPx(), center = Offset(sparkX, sparkY))
                    }
                }
            }
            "Glass Liquid" -> {
                drawCircle(color = color.copy(alpha = 0.05f), radius = radius, style = Stroke(width = strokeWidth))
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
                drawCircle(color = color.copy(alpha = 0.1f), radius = radius, style = Stroke(width = strokeWidth))
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
}
