package lumia.tracker.ui.components.header

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import lumia.tracker.viewmodel.ScholarViewModel

@Composable
fun HeaderQuickNoteInput(viewModel: ScholarViewModel? = null) {
    val context = LocalContext.current
    var quickNoteText by remember { mutableStateOf("") }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.EditNote,
                contentDescription = "Quick Note",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = quickNoteText,
                onValueChange = { quickNoteText = it },
                placeholder = { Text("Type a quick note...", style = MaterialTheme.typography.bodySmall) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                textStyle = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = {
                    if (quickNoteText.isNotBlank()) {
                        viewModel?.addNote(content = quickNoteText, tag = "HeaderQuick")
                        Toast.makeText(context, "Quick Note Saved!", Toast.LENGTH_SHORT).show()
                        quickNoteText = ""
                    }
                },
                enabled = quickNoteText.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Save", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
