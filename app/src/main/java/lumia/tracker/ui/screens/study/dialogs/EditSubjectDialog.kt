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
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated, optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            BouncyTextButton(onClick = {
                if (name.isNotBlank()) {
                    viewModel.updateSubject(subject.copy(name = name, tags = tags))
                    onDismiss()
                }
            }) { Text("Save") }
        },
        dismissButton = {
            BouncyTextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
