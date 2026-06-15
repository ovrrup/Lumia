package ovrrup.lumia.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

@Composable fun NotificationPermissionPanel() {}
@Composable fun ExactAlarmPermissionPanel() {}
@Composable fun BatteryOptimizationPermissionPanel() {}

val liquidGlass = Color.Transparent
val glassBar = Color.Transparent
val LocalGlassMode = compositionLocalOf { false }

@Composable
fun GlassCard(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(16.dp), content: @Composable () -> Unit) {
    androidx.compose.material3.Surface(modifier = modifier, shape = shape, content = content)
}
