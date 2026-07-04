package lumia.tracker.ui.screens.study

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.BouncyFloatingActionButton
import lumia.tracker.ui.components.GlassCard
import lumia.tracker.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(navController: NavController, viewModel: ScholarViewModel, subjectId: Int) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.find { it.id == subjectId }
    val allCourses by viewModel.courses.collectAsStateWithLifecycle()
    val allNotes by viewModel.notes.collectAsStateWithLifecycle()
    val allTopics by viewModel.allTopics.collectAsStateWithLifecycle(emptyList())
    
    val linkedCourses = remember(allCourses, subject) {
        allCourses.filter { course ->
            val splitIds = course.subjectIds.split(",").mapNotNull { it.trim().toIntOrNull() }
            course.subjectId == subjectId || splitIds.contains(subjectId)
        }
    }
    
    val subjectNotes = remember(allNotes, subjectId) {
        allNotes.filter { it.subjectId == subjectId }
    }
    
    val subjectTopics = remember(allTopics, subjectId) {
        allTopics.filter { it.subjectId == subjectId }
    }

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddTopicDialog by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subject?.name ?: "Subject Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            if (subject != null) {
                Column(horizontalAlignment = Alignment.End) {
                    if (showFabMenu) {
                        BouncyFloatingActionButton(
                            onClick = { showAddTopicDialog = true; showFabMenu = false },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(bottom = 8.dp).size(48.dp)
                        ) {
                            Icon(Icons.Rounded.ViewList, contentDescription = "Add Topic", modifier = Modifier.size(20.dp))
                        }
                        BouncyFloatingActionButton(
                            onClick = { showAddNoteDialog = true; showFabMenu = false },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(bottom = 16.dp).size(48.dp)
                        ) {
                            Icon(Icons.Rounded.EditNote, contentDescription = "Add Note", modifier = Modifier.size(20.dp))
                        }
                    }
                    BouncyFloatingActionButton(
                        onClick = { showFabMenu = !showFabMenu },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ) {
                        Icon(if (showFabMenu) Icons.Rounded.Close else Icons.Rounded.Add, contentDescription = "Add Item")
                    }
                }
            }
        }
    ) { padding ->
        if (subject == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Subject not found")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(72.dp).background(MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = subject.name.take(1).uppercase(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(subject.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                        if (subject.tags.isNotBlank()) {
                            Text("Tags: ${subject.tags}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (linkedCourses.isEmpty() && subjectTopics.isEmpty() && subjectNotes.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No items linked to this subject yet.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (linkedCourses.isNotEmpty()) {
                item {
                    Text("Linked Courses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                }
                items(linkedCourses, key = { "crs_${it.id}" }) { course ->
                    GlassCard(
                        onClick = { navController.navigate("courseDetail/${course.id}") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val color = try { Color(android.graphics.Color.parseColor(course.colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                            Box(modifier = Modifier.size(16.dp).clip(androidx.compose.foundation.shape.CircleShape).background(color))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(course.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            
            if (subjectTopics.isNotEmpty()) {
                item {
                    Text("Topics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                }
                items(subjectTopics, key = { "topic_${it.id}" }) { topic ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(topic.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            if (topic.tags.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Tags: ${topic.tags}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            
            if (subjectNotes.isNotEmpty()) {
                item {
                    Text("Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                }
                items(subjectNotes, key = { "note_${it.id}" }) { note ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(note.content, style = MaterialTheme.typography.bodyMedium, maxLines = 3, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showAddNoteDialog) {
        var noteContent by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("Add Note") },
            text = {
                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    label = { Text("Note content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                BouncyTextButton(onClick = {
                    if (noteContent.isNotBlank()) {
                        viewModel.addNote(
                            content = noteContent,
                            subjectId = subjectId
                        )
                    }
                    showAddNoteDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showAddNoteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddTopicDialog) {
        var topicTitle by remember { mutableStateOf("") }
        var topicTags by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddTopicDialog = false },
            title = { Text("Add Topic") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = topicTitle,
                        onValueChange = { topicTitle = it },
                        label = { Text("Topic Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = topicTags,
                        onValueChange = { topicTags = it },
                        label = { Text("Tags (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                BouncyTextButton(onClick = {
                    if (topicTitle.isNotBlank()) {
                        viewModel.addTopic(
                            subjectId = subjectId,
                            title = topicTitle,
                            tags = topicTags
                        )
                    }
                    showAddTopicDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showAddTopicDialog = false }) { Text("Cancel") }
            }
        )
    }
}
