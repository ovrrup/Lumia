package lumia.tracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.viewmodel.ScholarViewModel

@Composable
fun SafetyPinDialog(viewModel: ScholarViewModel) {
    val safetyPinDialogData = viewModel.safetyPinDialogData.collectAsStateWithLifecycle().value
    safetyPinDialogData?.let { data ->
        AlertDialog(
            icon = {
                Icon(
                    imageVector = if (data.isConflict) Icons.Rounded.Warning else Icons.Rounded.Info,
                    contentDescription = null,
                    tint = if (data.isConflict) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            },
            title = { Text(data.title) },
            text = { Text(data.description) },
            onDismissRequest = { data.onIgnore() },
            confirmButton = {
                TextButton(onClick = data.onConfirm) {
                    Text(if (data.isConflict) "Continue" else "Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = data.onIgnore) {
                    Text(if (data.isConflict) "Stop" else "Ignore")
                }
            }
        )
    }
}
