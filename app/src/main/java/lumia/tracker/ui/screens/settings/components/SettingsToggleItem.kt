package lumia.tracker.ui.screens

import lumia.tracker.service.AodAccessibilityService
import lumia.tracker.util.TrueAodManager
import android.content.Intent
import android.provider.Settings
import lumia.tracker.ui.theme.liquidGlass
import lumia.tracker.ui.theme.glassBar
import lumia.tracker.ui.theme.LocalDarkTheme
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Close
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.MergeType
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ViewQuilt
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.InvertColors
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.viewmodel.ScholarViewModel
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton

private fun getIosIconColor(icon: androidx.compose.ui.graphics.vector.ImageVector): androidx.compose.ui.graphics.Color {
    return when (icon.name) {
        "Rounded.Palette" -> androidx.compose.ui.graphics.Color(0xFFAF52DE) // Purple
        "Rounded.Person" -> androidx.compose.ui.graphics.Color(0xFF007AFF) // Blue
        "Rounded.SwapHoriz" -> androidx.compose.ui.graphics.Color(0xFF34C759) // Green
        "Rounded.Settings" -> androidx.compose.ui.graphics.Color(0xFF8E8E93) // Gray
        "Rounded.LocalOffer" -> androidx.compose.ui.graphics.Color(0xFFFF9500) // Orange
        "Rounded.Check" -> androidx.compose.ui.graphics.Color(0xFF5AC8FA) // Teal
        "Rounded.Lock" -> androidx.compose.ui.graphics.Color(0xFFFF3B30) // Red
        "Rounded.Notifications" -> androidx.compose.ui.graphics.Color(0xFFFF9500) // Orange
        "Rounded.Storage" -> androidx.compose.ui.graphics.Color(0xFF5AC8FA) // Teal
        "Rounded.Info" -> androidx.compose.ui.graphics.Color(0xFF007AFF) // Blue
        else -> androidx.compose.ui.graphics.Color(0xFF007AFF) // Blue default
    }
}

@Composable
fun SettingsToggleItem(
    title: String, 
    subtitle: String, 
    checked: Boolean, 
    enabled: Boolean = true,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    var showInfo by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(end = 12.dp).alpha(if (enabled) 1f else 0.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current
                val iosColor = remember(icon) { getIosIconColor(icon) }
                
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            if (isGlass) iosColor.copy(alpha = 0.4f)
                            else iosColor
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGlass) iosColor else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = title, 
                style = MaterialTheme.typography.bodyLarge, 
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { showInfo = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        val isDark = LocalDarkTheme.current
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.85f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF34C759),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = if (isDark) Color(0xFF3A3A3C) else Color(0xFFE5E5EA),
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent,
                disabledCheckedTrackColor = Color(0xFF34C759).copy(alpha = 0.5f),
                disabledUncheckedTrackColor = (if (isDark) Color(0xFF3A3A3C) else Color(0xFFE5E5EA)).copy(alpha = 0.5f)
            )
        )
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            icon = { Icon(Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
            text = { Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) {
                    Text("Got it")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}
