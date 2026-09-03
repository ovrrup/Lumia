package lumia.tracker.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.viewmodel.ScholarViewModel

@Composable
fun SafetyPinDialog(viewModel: ScholarViewModel) {
    val safetyPinDialogData by viewModel.safetyPinDialogData.collectAsStateWithLifecycle()

    safetyPinDialogData?.let { data ->
        AlertDialog(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 0.dp,
            icon = {
                Icon(
                    imageVector = if (data.isConflict) Icons.Rounded.Warning else Icons.Rounded.Info,
                    contentDescription = null,
                    tint = if (data.isConflict) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = data.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            onDismissRequest = { data.onIgnore() },
            confirmButton = {
                BouncyTextButton(onClick = data.onConfirm) {
                    Text(
                        text = if (data.isConflict) "Continue" else "Apply",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = data.onIgnore) {
                    Text(
                        text = if (data.isConflict) "Cancel" else "Dismiss",
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        )
    }
}
