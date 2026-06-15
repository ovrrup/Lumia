package ovrrup.lumia.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.RowScope
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

@Composable fun NotificationPermissionPanel() {}
@Composable fun ExactAlarmPermissionPanel() {}
@Composable fun BatteryOptimizationPermissionPanel() {}

@Composable
fun BouncyIconButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    IconButton(onClick = onClick, modifier = modifier, content = content)
}
@Composable
fun BouncyButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, shape: Shape = RoundedCornerShape(16.dp), colors: ButtonColors = ButtonDefaults.buttonColors(), content: @Composable RowScope.() -> Unit) {
    Button(onClick = onClick, modifier = modifier, enabled = enabled, shape = shape, colors = colors, content = content)
}
@Composable
fun BouncyTextButton(onClick: () -> Unit, modifier: Modifier = Modifier, colors: ButtonColors = ButtonDefaults.textButtonColors(), content: @Composable RowScope.() -> Unit) {
    TextButton(onClick = onClick, modifier = modifier, colors = colors, content = content)
}
@Composable
fun BouncyOutlinedButton(onClick: () -> Unit, modifier: Modifier = Modifier, colors: ButtonColors = ButtonDefaults.outlinedButtonColors(), content: @Composable RowScope.() -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier, colors = colors, content = content)
}
@Composable
fun BouncyFloatingActionButton(onClick: () -> Unit, modifier: Modifier = Modifier, containerColor: Color = Color.Unspecified, content: @Composable () -> Unit) {
    FloatingActionButton(onClick = onClick, modifier = modifier, containerColor = containerColor, content = content)
}
@Composable
fun GlassCard(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(16.dp), content: @Composable () -> Unit) {
    androidx.compose.material3.Surface(modifier = modifier, shape = shape, content = content)
}
