package lumia.tracker.ui.screens.settings

import android.content.Context
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.components.ScholarCardDefaults
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.viewmodel.ScholarViewModel

private fun playTonePreview(context: Context, tone: String, alertType: String) {
    if (alertType == "silent") return

    // Haptic preview
    if (alertType != "sound_only") {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(120)
            }
        } catch (_: Exception) {}
    }

    // Audible preview
    if (alertType != "vibration_only") {
        try {
            val toneType = when (tone) {
                "Gentle Chime" -> ToneGenerator.TONE_PROP_BEEP
                "Academic Bell" -> ToneGenerator.TONE_PROP_ACK
                "Subtle Gong" -> ToneGenerator.TONE_PROP_PROMPT
                "Soft Pulse" -> ToneGenerator.TONE_CDMA_PIP
                else -> ToneGenerator.TONE_PROP_BEEP
            }
            val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            tg.startTone(toneType, 180)
        } catch (_: Exception) {
            try {
                val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val r = RingtoneManager.getRingtone(context, notificationUri)
                r?.play()
            } catch (_: Exception) {}
        }
    }
}

@ValueScore(
    score = 90,
    importance = Importance.HIGH,
    description = "Simplified notification settings: alert delivery type and tone selector",
    category = "Settings"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val context = LocalContext.current
    val alertType by viewModel.notifAlertType.collectAsStateWithLifecycle()
    val notificationTone by viewModel.notifTone.collectAsStateWithLifecycle()

    val alertTypeOptions = remember {
        listOf(
            Triple("sound_and_vibrate", "Sound & Vibration", Icons.Rounded.NotificationsActive),
            Triple("sound_only", "Sound Only", Icons.Rounded.VolumeUp),
            Triple("vibration_only", "Vibration Only", Icons.Rounded.Vibration),
            Triple("silent", "Silent", Icons.Rounded.NotificationsOff)
        )
    }

    val toneOptions = remember {
        listOf("Default", "Gentle Chime", "Academic Bell", "Subtle Gong", "Soft Pulse")
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Notifications",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    BouncyIconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Alert Delivery Type Card
            ScholarCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = "Alert Type",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        alertTypeOptions.forEach { (typeKey, typeLabel, typeIcon) ->
                            val isSelected = alertType == typeKey
                            NotificationOptionRow(
                                label = typeLabel,
                                icon = typeIcon,
                                isSelected = isSelected,
                                onClick = {
                                    viewModel.updateNotifAlertType(typeKey)
                                    playTonePreview(context, notificationTone, typeKey)
                                }
                            )
                        }
                    }
                }
            }

            // 2. Notification Tone Card
            ScholarCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = "Notification Tone",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        toneOptions.forEach { toneName ->
                            val isSelected = notificationTone == toneName
                            NotificationOptionRow(
                                label = toneName,
                                icon = Icons.Rounded.VolumeUp,
                                isSelected = isSelected,
                                onClick = {
                                    viewModel.updateNotifTone(toneName)
                                    playTonePreview(context, toneName, alertType)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NotificationOptionRow(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.35f)
        },
        border = if (isSelected) {
            BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary)
        } else {
            ScholarCardDefaults.border()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
