package lumia.tracker.ui.screens

import android.provider.Settings
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.rounded.Info
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight

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
fun SettingsActionItemInCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    var showInfo by remember { mutableStateOf(false) }
    val contentColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current
        val iosColor = remember(icon) { getIosIconColor(icon) }
        
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(
                    if (isGlass) iosColor.copy(alpha = 0.4f)
                    else if (isDestructive) MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    else iosColor
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGlass) iosColor else if (isDestructive) MaterialTheme.colorScheme.error else Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title, 
                style = MaterialTheme.typography.bodyLarge, 
                fontWeight = FontWeight.Normal, 
                color = contentColor
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
        
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
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
