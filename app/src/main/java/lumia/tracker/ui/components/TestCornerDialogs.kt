package lumia.tracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Grade
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lumia.tracker.model.TestRecord
import lumia.tracker.viewmodel.ScholarViewModel

@Composable
fun AnalyticsStatItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AddEditTestRecordDialog(
    initialRecord: TestRecord? = null,
    subjectId: Int? = null,
    courseId: Int? = null,
    viewModel: ScholarViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialRecord?.title ?: "") }
    var marksObtained by remember { mutableStateOf(initialRecord?.marksObtained?.toString() ?: "") }
    var totalMarks by remember { mutableStateOf(initialRecord?.totalMarks?.toString() ?: "100") }
    var notes by remember { mutableStateOf(initialRecord?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialRecord == null) "Add Test Record" else "Edit Test Record") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Test Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = marksObtained,
                        onValueChange = { marksObtained = it },
                        label = { Text("Marks Obtained") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = totalMarks,
                        onValueChange = { totalMarks = it },
                        label = { Text("Total Marks") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Feedback") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val obtained = marksObtained.toFloatOrNull() ?: 0f
                    val total = totalMarks.toFloatOrNull() ?: 100f
                    if (title.isNotBlank()) {
                        if (initialRecord == null) {
                            viewModel.addTestRecord(
                                TestRecord(
                                    title = title.trim(),
                                    marksObtained = obtained,
                                    totalMarks = total,
                                    notes = notes.trim(),
                                    subjectId = subjectId,
                                    courseId = courseId
                                )
                            )
                        } else {
                            viewModel.updateTestRecord(
                                initialRecord.copy(
                                    title = title.trim(),
                                    marksObtained = obtained,
                                    totalMarks = total,
                                    notes = notes.trim()
                                )
                            )
                        }
                        onDismiss()
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
