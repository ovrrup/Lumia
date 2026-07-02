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
import kotlinx.coroutines.isActive

@Composable
fun WelcomeMeshBanner() {
    val transition = rememberInfiniteTransition(label = "MeshAnimation")
    
    val xOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "xOffset"
    )

    val yOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 500f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "yOffset"
    )

    val c1 = MaterialTheme.colorScheme.primaryContainer
    val c2 = MaterialTheme.colorScheme.secondaryContainer
    val c3 = MaterialTheme.colorScheme.tertiaryContainer

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(32.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(c1, c2.copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(xOffset % canvasWidth, yOffset % canvasHeight),
                    radius = canvasWidth * 0.8f
                )
            )

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(c3, c1.copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(canvasWidth - (xOffset % canvasWidth), canvasHeight - (yOffset % canvasHeight)),
                    radius = canvasWidth * 0.8f
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
