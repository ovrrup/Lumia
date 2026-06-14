package ovrrup.lumia.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ovrrup.lumia.model.*
import ovrrup.lumia.ui.components.*
import ovrrup.lumia.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    navController: NavController,
    viewModel: ScholarViewModel,
    subjectId: Int
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle(initialValue = emptyList())
    val subject = remember(subjects, subjectId) { subjects.find { it.id == subjectId } }

    val topicsFlow = remember(subjectId) { viewModel.getTopicsForSubject(subjectId) }
    val topics by topicsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val chaptersFlow = remember(subjectId) { viewModel.getChaptersForSubject(subjectId) }
    val chapters by chaptersFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedTab by remember { mutableStateOf(0) } // 0: Chapters, 1: Topics
    var showAddChapterDialog by remember { mutableStateOf(false) }
    var showAddTopicDialog by remember { mutableStateOf(false) }

    if (subject == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Column {
                        Text(
                            subject.name,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (subject.tags.isNotBlank()) {
                            Text(
                                "Tags: ${subject.tags}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    BouncyIconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    BouncyIconButton(
                        onClick = {
                            viewModel.deleteSubject(subject)
                            navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.Rounded.DeleteForever, contentDescription = "Delete Subject", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            BouncyFloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showAddChapterDialog = true else showAddTopicDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Create New")
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                listOf("Syllabus Chapters", "Knowledge Topics").forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(tab, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            when (selectedTab) {
                0 -> { // Chapters Tab
                    if (chapters.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.BookmarkBorder,
                            title = "No Chapters Yet",
                            description = "Add syllabus chapters to organize your academic lectures or book chapters.",
                            buttonText = "Add Chapter",
                            onClick = { showAddChapterDialog = true }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(chapters) { chapter ->
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                chapter.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            BouncyIconButton(
                                                onClick = { viewModel.deleteChapter(chapter) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                        if (chapter.description.isNotBlank()) {
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                chapter.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> { // Topics Tab
                    if (topics.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.Lightbulb,
                            title = "No Knowledge Topics",
                            description = "A topic represents a single concept, theorem, or question to study and master.",
                            buttonText = "Add Study Topic",
                            onClick = { showAddTopicDialog = true }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(topics) { topic ->
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = topic.isCompleted,
                                            onCheckedChange = { viewModel.toggleTopicCompleted(topic) }
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                topic.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (topic.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (topic.tags.isNotBlank()) {
                                                Text(
                                                    topic.tags,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        BouncyIconButton(onClick = { viewModel.deleteTopic(topic) }) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Chapter Dialog
    if (showAddChapterDialog) {
        var name by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddChapterDialog = false },
            title = { Text("Add Lecture Chapter") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Chapter Title / Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Lecture details / description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                BouncyButton(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addChapter(
                                name = name,
                                subjectId = subjectId,
                                description = description,
                                tags = ""
                            )
                            showAddChapterDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showAddChapterDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Topic Dialog
    if (showAddTopicDialog) {
        var title by remember { mutableStateOf("") }
        var topicTags by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddTopicDialog = false },
            title = { Text("Add Study Concept") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Topic Title (e.g. Fourier Transform)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = topicTags,
                        onValueChange = { topicTags = it },
                        label = { Text("Tags / Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                BouncyButton(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addTopic(
                                subjectId = subjectId,
                                title = title,
                                tags = topicTags,
                                chapterId = null
                            )
                            showAddTopicDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showAddTopicDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
