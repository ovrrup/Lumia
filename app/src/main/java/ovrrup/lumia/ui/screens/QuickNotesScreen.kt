package ovrrup.lumia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ovrrup.lumia.model.Note
import ovrrup.lumia.ui.components.*
import ovrrup.lumia.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickNotesScreen(
    navController: NavController,
    viewModel: ScholarViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    val filteredNotes = remember(notes, searchQuery) {
        if (searchQuery.isBlank()) {
            notes
        } else {
            notes.filter { it.content.contains(searchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text("Academic Notebook", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    BouncyIconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back")
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            BouncyFloatingActionButton(
                onClick = { showAddNoteDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Rounded.NoteAdd, contentDescription = "Write Quick Note")
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search your lecture scribbles...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(Modifier.height(8.dp))

            if (filteredNotes.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No Matching Notes" else "Your Notebook is Empty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try searching different terms." else "Tap the floating button to capture instant study notes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNotes) { note ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    note.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val dateString = android.text.format.DateFormat.format("MMM dd, yyyy - HH:mm", note.dateMillis).toString()
                                    Text(
                                        text = dateString,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    BouncyIconButton(
                                        onClick = { viewModel.deleteNote(note) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Delete,
                                            contentDescription = "Delete note",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Note Dialog
    if (showAddNoteDialog) {
        var content by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("Write Scholar Note") },
            text = {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Scribble your notes here...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            },
            confirmButton = {
                BouncyButton(
                    onClick = {
                        if (content.isNotBlank()) {
                            viewModel.addNote(
                                content = content,
                                courseId = null
                            )
                            showAddNoteDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
