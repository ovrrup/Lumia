package lumia.tracker.ui.screens.study

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import lumia.tracker.model.Subject
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.TagMultiSelect
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
        title = { 
            Text(
                "Edit Subject",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Black
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name") },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Rounded.Book, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                TagMultiSelect(
                    tagsString = tags,
                    onTagsChanged = { tags = it }
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
