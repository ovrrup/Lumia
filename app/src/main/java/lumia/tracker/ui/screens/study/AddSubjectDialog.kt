package lumia.tracker.ui.screens.study

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.viewmodel.ScholarViewModel

@Composable
fun AddSubjectDialog(
    viewModel: ScholarViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Subject") },
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
                    viewModel.addSubject(name, tags)
                    onDismiss()
                }
            }) { Text("Add") }
        },
        dismissButton = {
            BouncyTextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
