package lumia.tracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagMultiSelect(
    tagsString: String,
    onTagsChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tags = tagsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    var newTagText by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Tags", style = MaterialTheme.typography.labelMedium)
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                InputChip(
                    selected = true,
                    onClick = {
                        val newTags = tags.filter { it != tag }
                        onTagsChanged(newTags.joinToString(", "))
                    },
                    label = { Text(tag) },
                    trailingIcon = {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Remove Tag",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = newTagText,
                onValueChange = { newTagText = it },
                label = { Text("Add Tag") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (newTagText.isNotBlank()) {
                        val currentTags = tags.toMutableList()
                        if (!currentTags.contains(newTagText.trim())) {
                            currentTags.add(newTagText.trim())
                            onTagsChanged(currentTags.joinToString(", "))
                        }
                        newTagText = ""
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Tag")
            }
        }
    }
}
