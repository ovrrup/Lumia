package lumia.tracker.ui.screens.sync.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import lumia.tracker.sync.model.SyncMergeReport
import lumia.tracker.sync.model.SyncState
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton

/**
 * Interactive dialog displaying live multi-device P2P synchronization progress,
 * mutual authentication confirmation, and entity merge results.
 */
@Composable
fun SyncProgressDialog(
    syncState: SyncState,
    onDismiss: () -> Unit
) {
    if (syncState is SyncState.Idle || syncState is SyncState.Discovering) return

    Dialog(onDismissRequest = {
        if (syncState is SyncState.Success || syncState is SyncState.Error) {
            onDismiss()
        }
    }) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (syncState) {
                    is SyncState.Connecting -> {
                        SyncStepContent(
                            icon = Icons.Rounded.Wifi,
                            title = "Connecting to Peer",
                            subtitle = "Establishing secure local P2P link with ${syncState.peerName}...",
                            isLoading = true
                        )
                    }
                    is SyncState.Authenticating -> {
                        SyncStepContent(
                            icon = Icons.Rounded.VpnKey,
                            title = "Zero-Trust Authentication",
                            subtitle = "Verifying pairing PIN and exchanging mutual HMAC signatures with ${syncState.peerName}...",
                            isLoading = true
                        )
                    }
                    is SyncState.ExchangingData -> {
                        SyncStepContent(
                            icon = Icons.Rounded.SwapHoriz,
                            title = "Encrypted Data Transfer",
                            subtitle = syncState.stageText,
                            isLoading = true
                        )
                        Spacer(Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { syncState.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                        )
                    }
                    is SyncState.Merging -> {
                        SyncStepContent(
                            icon = Icons.Rounded.Autorenew,
                            title = "Smart Delta Merging",
                            subtitle = syncState.stageText,
                            isLoading = true
                        )
                        Spacer(Modifier.height(16.dp))
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                    is SyncState.Success -> {
                        SyncSuccessContent(report = syncState.report, onDismiss = onDismiss)
                    }
                    is SyncState.Error -> {
                        SyncErrorContent(message = syncState.message, onDismiss = onDismiss)
                    }
                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun SyncStepContent(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isLoading: Boolean
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
    }
    Spacer(Modifier.height(16.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun SyncSuccessContent(
    report: SyncMergeReport,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .background(Color(0xFF4CAF50).copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = "Success",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(38.dp)
        )
    }

    Spacer(Modifier.height(16.dp))
    Text(
        text = "Sync Complete!",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = "Successfully synchronized with ${report.peerDeviceName.ifBlank { "remote peer" }}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(20.dp))

    // Merged items breakdown
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Synchronized Entities:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Courses & Subjects", style = MaterialTheme.typography.bodySmall)
                Text("+${report.coursesMerged} Courses, +${report.subjectsMerged} Subjects", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tasks & Exercises", style = MaterialTheme.typography.bodySmall)
                Text("+${report.tasksMerged} Tasks, +${report.assignmentsMerged} Exercises", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Notes & Pomodoros", style = MaterialTheme.typography.bodySmall)
                Text("+${report.notesMerged} Notes, +${report.pomodoroSessionsMerged} Focus", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }
            if (report.profilesSynced > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Profiles Synced", style = MaterialTheme.typography.bodySmall)
                    Text("${report.profilesSynced} Profile(s)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    Spacer(Modifier.height(24.dp))
    BouncyButton(
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Done")
    }
}

@Composable
private fun SyncErrorContent(
    message: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.ErrorOutline,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(38.dp)
        )
    }

    Spacer(Modifier.height(16.dp))
    Text(
        text = "Sync Failed",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.error
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(24.dp))
    BouncyButton(
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
