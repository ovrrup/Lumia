package lumia.tracker.ui.screens.study

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lumia.tracker.model.Subject
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.viewmodel.ScholarViewModel

@Composable
fun EditSubjectDialog(
    subject: Subject,
    viewModel: ScholarViewModel,
    onDismiss: () -> Unit
) {
    var name by remember(subject) { mutableStateOf(subject.name) }
    var tags by remember(subject) { mutableStateOf(subject.tags) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Subject") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name") },
                    placeholder = { Text("e.g. Mathematics") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (optional)") },
                    placeholder = { Text("e.g. science, major, semester 1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text(
                    text = "Update subject details and tags used across associated chapters and topics.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            BouncyTextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.updateSubject(subject.copy(name = name.trim(), tags = tags.trim()))
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            BouncyTextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
