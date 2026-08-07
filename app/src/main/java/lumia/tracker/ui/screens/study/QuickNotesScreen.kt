package lumia.tracker.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import lumia.tracker.ui.components.BouncyFloatingActionButton
import lumia.tracker.ui.components.GlassCard
import lumia.tracker.ui.components.SwipeToDeleteContainer
import lumia.tracker.ui.components.UniversalCapsuleHeader
import lumia.tracker.ui.theme.LocalGlassMode
import org.json.JSONArray
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickNotesScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("quick_notes_prefs", Context.MODE_PRIVATE) }
    val notesList = remember { mutableStateListOf<String>() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val savedNotes = sharedPrefs.getString("notes_json", "[]") ?: "[]"
            try {
                val jsonArray = JSONArray(savedNotes)
                val loadedNotes = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    loadedNotes.add(jsonArray.getString(i))
                }
                withContext(Dispatchers.Main) {
                    notesList.clear()
                    notesList.addAll(loadedNotes)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun saveNotes() {
        val listSnapshot = notesList.toList()
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val jsonArray = JSONArray()
                listSnapshot.forEach { jsonArray.put(it) }
                sharedPrefs.edit().putString("notes_json", jsonArray.toString()).apply()
            }
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newNoteText by remember { mutableStateOf("") }
    val isGlass = LocalGlassMode.current

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
            floatingActionButton = {
                BouncyFloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Rounded.Add, "Add Note")
                }
            }
        ) { padding ->
            val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = statusBarHeight + 64.dp, bottom = 32.dp),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (notesList.isEmpty()) {
                    item {
                        Text(
                            "No notes yet. Tap + to add one.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                itemsIndexed(notesList, key = { _, note -> note }) { index, note ->
                    SwipeToDeleteContainer(
                        onDelete = {
                            notesList.removeAt(index)
                            saveNotes()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = note,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                IconButton(onClick = { 
                                    notesList.removeAt(index)
                                    saveNotes()
                                }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Rounded.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
        UniversalCapsuleHeader(
            title = "Quick Notes",
            onBackClick = { navController.popBackStack() }
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; newNoteText = "" },
            title = { Text("New Note") },
            text = {
                OutlinedTextField(
                    value = newNoteText,
                    onValueChange = { newNoteText = it },
                    label = { Text("Note content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newNoteText.isNotBlank()) {
                        notesList.add(newNoteText)
                        saveNotes()
                    }
                    showAddDialog = false
                    newNoteText = ""
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; newNoteText = "" }) { Text("Cancel") }
            }
        )
    }
}
