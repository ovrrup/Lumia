package lumia.tracker.ui.screens.study.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lumia.tracker.model.Subject
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.viewmodel.ScholarViewModel

@ValueScore(
    score = 75,
    importance = Importance.HIGH,
    description = "Edit subject modal for modifying subject name and metadata tags",
    category = "Dialog"
)
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
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        title = {
            StudyDialogHeader(
                icon = Icons.Rounded.Edit,
                title = "Edit Subject",
                subtitle = "Update subject name and tags",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name *") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.AutoStories,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (Optional)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Tag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            BouncyButton(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.updateSubject(subject.copy(name = name.trim(), tags = tags.trim()))
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            BouncyTextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
