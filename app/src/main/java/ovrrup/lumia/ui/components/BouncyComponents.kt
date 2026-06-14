package ovrrup.lumia.ui.components

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ovrrup.lumia.ui.theme.glassBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Warning

@Composable
fun BouncyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(100),
        label = "bouncy_button_scale"
    )

    Button(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun BouncyIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = tween(100),
        label = "bouncy_icon_button_scale"
    )

    IconButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun BouncyTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.textShape,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = tween(100),
        label = "bouncy_text_button_scale"
    )

    TextButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        shape = shape,
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun BouncyOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(100),
        label = "bouncy_outlined_button_scale"
    )

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        shape = shape,
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.glassBar(shape)
    ) {
        content()
    }
}

@Composable
fun BouncyFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = FloatingActionButtonDefaults.shape,
    containerColor: androidx.compose.ui.graphics.Color = FloatingActionButtonDefaults.containerColor,
    contentColor: androidx.compose.ui.graphics.Color = contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = tween(100),
        label = "bouncy_fab_scale"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = elevation,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun NotificationPermissionPanel() {
    val context = LocalContext.current
    var isEnabled by remember {
        mutableStateOf(
            androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
        )
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                val intent = Intent().apply {
                    action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Notification Permissions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isEnabled) "Notifications are successfully enabled." else "Disabled. Tap to manage alert and streak reminder channels.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ExactAlarmPermissionPanel() {
    val context = LocalContext.current
    var canSchedule by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                alarmManager?.canScheduleExactAlarms() == true
            } else {
                true
            }
        )
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent().apply {
                        action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
            }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = if (canSchedule) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Scheduling Alarms",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (canSchedule) "Exact timing scheduling permitted." else "Required to trigger on-time task deadlines.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BatteryOptimizationPermissionPanel() {
    val context = LocalContext.current
    var isIgnoring by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
            } else {
                true
            }
        )
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent().apply {
                        action = android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                    }
                    context.startActivity(intent)
                }
            }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = if (isIgnoring) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Battery Optimization",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isIgnoring) "Relaxed rules. Focus session background tracking runs cleanly." else "Restricted. Focus timing may stop in background. Tap to exempt.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
