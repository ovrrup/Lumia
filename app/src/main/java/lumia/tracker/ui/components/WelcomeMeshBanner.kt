package lumia.tracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lumia.tracker.ui.theme.LocalGlassMode
import lumia.tracker.ui.theme.LocalPureBlackMode
import lumia.tracker.ui.theme.liquidGlass
import androidx.compose.foundation.border

@Composable
fun WelcomeMeshBanner() {
    val isPureBlack = LocalPureBlackMode.current
    val transition = rememberInfiniteTransition(label = "MeshAnimation")
    
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "phase"
    )

    val c1 = MaterialTheme.colorScheme.primaryContainer
    val c2 = MaterialTheme.colorScheme.secondaryContainer
    val c3 = MaterialTheme.colorScheme.tertiaryContainer

    val isGlass = LocalGlassMode.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .then(
                if (isGlass) {
                    Modifier.liquidGlass(shape = RoundedCornerShape(32.dp), tintAlpha = 0.12f)
                } else {
                    Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .then(
                            if (isPureBlack) {
                                Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
                            } else Modifier
                        )
                }
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            if (isPureBlack) {
                drawRect(color = Color.Black)
            } else {
                val cx1 = canvasWidth * 0.40f + (canvasWidth * 0.25f) * kotlin.math.cos(phase)
                val cy1 = canvasHeight * 0.50f + (canvasHeight * 0.30f) * kotlin.math.sin(phase * 0.8f)

                val cx2 = canvasWidth * 0.60f - (canvasWidth * 0.25f) * kotlin.math.cos(phase * 0.7f)
                val cy2 = canvasHeight * 0.50f - (canvasHeight * 0.30f) * kotlin.math.sin(phase)

                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(c1, c2.copy(alpha = 0.5f), Color.Transparent),
                        center = Offset(cx1, cy1),
                        radius = canvasWidth * 0.85f
                    )
                )

                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(c3, c1.copy(alpha = 0.5f), Color.Transparent),
                        center = Offset(cx2, cy2),
                        radius = canvasWidth * 0.85f
                    )
                )
                
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(c2.copy(alpha = 0.2f), Color.Transparent, c3.copy(alpha = 0.2f)),
                        start = Offset(0f, 0f),
                        end = Offset(canvasWidth, canvasHeight)
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Welcome to Lumia",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your personalized learning companion.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
