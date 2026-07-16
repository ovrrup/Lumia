package lumia.tracker.ui.screens.study

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.*
import lumia.tracker.viewmodel.ScholarViewModel
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.ui.util.getTagColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagsHubScreen(navController: NavController, viewModel: ScholarViewModel, initialTag: String = "") {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val testRecords by viewModel.allTestRecords.collectAsStateWithLifecycle()
    val topics by viewModel.allTopics.collectAsStateWithLifecycle()
    val chapters by viewModel.allChapters.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTagName by remember { mutableStateOf(initialTag.trim()) }

    // Aggregate tags reactively
    val allTagsMetadata = remember(courses, subjects, assignments, tasks, testRecords, topics, chapters) {
        val map = mutableMapOf<String, TagMetadata>()
        
        fun addConnection(tagName: String, update: (TagMetadata) -> TagMetadata) {
            val cleanTag = tagName.trim()
            if (cleanTag.isBlank()) return
            val key = cleanTag.lowercase()
            val current = map[key] ?: TagMetadata(name = cleanTag)
            map[key] = update(current)
        }

        courses.forEach { course ->
            course.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach {
                addConnection(it) { meta -> meta.copy(courses = (meta.courses + course).distinctBy { c -> c.id }) }
            }
        }
        subjects.forEach { subject ->
            subject.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach {
                addConnection(it) { meta -> meta.copy(subjects = (meta.subjects + subject).distinctBy { s -> s.id }) }
            }
        }
        chapters.forEach { chapter ->
            chapter.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach {
                addConnection(it) { meta -> meta.copy(chapters = (meta.chapters + chapter).distinctBy { ch -> ch.id }) }
            }
        }
        topics.forEach { topic ->
            topic.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach {
                addConnection(it) { meta -> meta.copy(topics = (meta.topics + topic).distinctBy { t -> t.id }) }
            }
        }
        assignments.forEach { assignment ->
            assignment.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach {
                addConnection(it) { meta -> meta.copy(assignments = (meta.assignments + assignment).distinctBy { a -> a.id }) }
            }
        }
        tasks.forEach { task ->
            task.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach {
                addConnection(it) { meta -> meta.copy(tasks = (meta.tasks + task).distinctBy { t -> t.id }) }
            }
        }
        testRecords.forEach { record ->
            record.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach {
                addConnection(it) { meta -> meta.copy(testRecords = (meta.testRecords + record).distinctBy { r -> r.id }) }
            }
        }
        
        map.values.sortedByDescending { it.totalCount }
    }

    // Filter tags
    val filteredTags = remember(allTagsMetadata, searchQuery) {
        if (searchQuery.isBlank()) {
            allTagsMetadata
        } else {
            allTagsMetadata.filter { it.name.lowercase().contains(searchQuery.lowercase()) }
        }
    }

    // Find metadata for selected tag
    val selectedTagMetadata = remember(allTagsMetadata, selectedTagName) {
        allTagsMetadata.find { it.name.lowercase() == selectedTagName.lowercase() }
    }

    // Set fallback if selected tag gets deleted or renamed
    LaunchedEffect(allTagsMetadata) {
        if (selectedTagName.isNotBlank() && selectedTagMetadata == null) {
            val closestMatch = allTagsMetadata.find { it.name.lowercase() == selectedTagName.lowercase() }
            if (closestMatch != null) {
                selectedTagName = closestMatch.name
            } else if (allTagsMetadata.isNotEmpty()) {
                selectedTagName = allTagsMetadata.first().name
            } else {
                selectedTagName = ""
            }
        } else if (selectedTagName.isBlank() && allTagsMetadata.isNotEmpty()) {
            selectedTagName = allTagsMetadata.first().name
        }
    }

    // Action dialog states
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameNewValue by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }

    var showMergeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            lumia.tracker.ui.components.UniversalCapsuleHeader(
                title = "Tags Network & Manager",
                onBackClick = { navController.navigateUp() }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("tag_search_field"),
                placeholder = { Text("Search tags...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            if (allTagsMetadata.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalOffer,
                            contentDescription = null,
                            modifier = Modifier
                                .size(72.dp)
                                .padding(bottom = 16.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No Tags Found",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add tags to your Courses, Subjects, Chapters, Topics, Tasks, or Test Records to unleash the full power of study interconnections!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                // Interactive Grid + Details Split
                Column(modifier = Modifier.fillMaxSize()) {
                    // Tag Mind Cloud Panel (Scrollable Horizontally/Vertically or FlowRow)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .weight(0.35f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Tag Cloud Network (${filteredTags.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    filteredTags.forEach { tagMeta ->
                                        val isSelected = tagMeta.name.lowercase() == selectedTagName.lowercase()
                                        val colors = getTagColors(tagMeta.name)
                                        
                                        // Tag Bubble
                                        Box(
                                            modifier = Modifier
                                                .shadow(
                                                    elevation = if (isSelected) 8.dp else 2.dp,
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                                .background(
                                                    color = if (isSelected) colors.second else colors.first,
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                                .then(
                                                    if (isSelected) Modifier.border(
                                                        width = 2.dp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = RoundedCornerShape(16.dp)
                                                    ) else Modifier
                                                )
                                                .clickable { selectedTagName = tagMeta.name }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                                .testTag("tag_pill_${tagMeta.name}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.LocalOffer,
                                                    contentDescription = null,
                                                    tint = if (isSelected) colors.first else colors.second,
                                                    modifier = Modifier.size(14.dp).padding(end = 4.dp)
                                                )
                                                Text(
                                                    text = tagMeta.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) colors.first else colors.second
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            color = if (isSelected) colors.first else colors.second.copy(alpha = 0.15f),
                                                            shape = CircleShape
                                                        )
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = tagMeta.totalCount.toString(),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) colors.second else colors.second
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Selected Tag Dashboard & Connections (Details Pane)
                    AnimatedContent(
                        targetState = selectedTagMetadata,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.65f),
                        label = "SelectedTagConnectionPane"
                    ) { meta ->
                        if (meta == null) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Select a tag to view and manage its connections",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Tag Header with Synergy Badge & Quick Actions
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.LocalOffer,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = meta.name,
                                                        style = MaterialTheme.typography.titleLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                
                                                // Synergy Badge
                                                val synergyLabel = when {
                                                    meta.totalCount >= 10 -> "Study Core Node"
                                                    meta.totalCount >= 5 -> "Highly Integrated"
                                                    else -> "Connected Hub"
                                                }
                                                val synergyColor = when {
                                                    meta.totalCount >= 10 -> MaterialTheme.colorScheme.error
                                                    meta.totalCount >= 5 -> MaterialTheme.colorScheme.tertiary
                                                    else -> MaterialTheme.colorScheme.secondary
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .padding(top = 4.dp)
                                                        .background(
                                                            color = synergyColor.copy(alpha = 0.15f),
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = synergyLabel,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = synergyColor
                                                    )
                                                }
                                            }

                                            // Action Buttons Row
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                // Rename Button
                                                IconButton(
                                                    onClick = {
                                                        renameNewValue = meta.name
                                                        showRenameDialog = true
                                                    },
                                                    modifier = Modifier
                                                        .testTag("rename_tag_action")
                                                        .background(
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                            CircleShape
                                                        )
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Edit,
                                                        contentDescription = "Rename Tag",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                                // Merge Button
                                                IconButton(
                                                    onClick = { showMergeDialog = true },
                                                    modifier = Modifier
                                                        .testTag("merge_tag_action")
                                                        .background(
                                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                                            CircleShape
                                                        )
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Merge,
                                                        contentDescription = "Merge Tag",
                                                        tint = MaterialTheme.colorScheme.secondary
                                                    )
                                                }

                                                // Delete Button
                                                IconButton(
                                                    onClick = { showDeleteDialog = true },
                                                    modifier = Modifier
                                                        .testTag("delete_tag_action")
                                                        .background(
                                                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                                            CircleShape
                                                        )
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Delete,
                                                        contentDescription = "Delete Tag",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Interactive connected list
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(bottom = 32.dp)
                                ) {
                                    // 1. Connected Courses
                                    if (meta.courses.isNotEmpty()) {
                                        item {
                                            ConnectionSectionHeader("Courses (${meta.courses.size})", Icons.Rounded.Book)
                                        }
                                        items(meta.courses) { course ->
                                            ConnectionCard(
                                                title = course.name,
                                                subtitle = "Instructor: ${course.instructor.ifBlank { "Unassigned" }}",
                                                infoText = "Course Code: ${course.code.ifBlank { "N/A" }}",
                                                badgeText = "Course",
                                                badgeColor = MaterialTheme.colorScheme.primary,
                                                onClick = { navController.navigate("courseDetail/${course.id}") }
                                            )
                                        }
                                    }

                                    // 2. Connected Subjects
                                    if (meta.subjects.isNotEmpty()) {
                                        item {
                                            ConnectionSectionHeader("Subjects (${meta.subjects.size})", Icons.Rounded.FolderOpen)
                                        }
                                        items(meta.subjects) { subject ->
                                            ConnectionCard(
                                                title = subject.name,
                                                subtitle = "Focus Subject",
                                                infoText = "Navigate to explore detail mind-map",
                                                badgeText = "Subject",
                                                badgeColor = MaterialTheme.colorScheme.secondary,
                                                onClick = { navController.navigate("subjectDetail/${subject.id}") }
                                            )
                                        }
                                    }

                                    // 3. Connected Chapters
                                    if (meta.chapters.isNotEmpty()) {
                                        item {
                                            ConnectionSectionHeader("Chapters (${meta.chapters.size})", Icons.Rounded.AutoStories)
                                        }
                                        items(meta.chapters) { chapter ->
                                            val parentSubject = subjects.find { it.id == chapter.subjectId }
                                            ConnectionCard(
                                                title = chapter.name,
                                                subtitle = "Subject: ${parentSubject?.name ?: "Unknown"}",
                                                infoText = chapter.description.ifBlank { "No description added" },
                                                badgeText = "Chapter",
                                                badgeColor = MaterialTheme.colorScheme.tertiary,
                                                onClick = { navController.navigate("subjectDetail/${chapter.subjectId}") }
                                            )
                                        }
                                    }

                                    // 4. Connected Topics
                                    if (meta.topics.isNotEmpty()) {
                                        item {
                                            ConnectionSectionHeader("Topics (${meta.topics.size})", Icons.Rounded.Category)
                                        }
                                        items(meta.topics) { topic ->
                                            val parentSubject = subjects.find { it.id == topic.subjectId }
                                            ConnectionCard(
                                                title = topic.title,
                                                subtitle = "Subject: ${parentSubject?.name ?: "Unknown"}",
                                                infoText = if (topic.isCompleted) "Status: Completed 🎉" else "Status: In Progress ✏️",
                                                badgeText = "Topic",
                                                badgeColor = MaterialTheme.colorScheme.tertiary,
                                                onClick = { navController.navigate("subjectDetail/${topic.subjectId}") }
                                            )
                                        }
                                    }

                                    // 5. Connected Assignments
                                    if (meta.assignments.isNotEmpty()) {
                                        item {
                                            ConnectionSectionHeader("Assignments (${meta.assignments.size})", Icons.Rounded.Assignment)
                                        }
                                        items(meta.assignments) { assignment ->
                                            val courseName = courses.find { it.id == assignment.courseId }?.name ?: "Unknown Course"
                                            val dateStr = if (assignment.dueDateMillis > 0) {
                                                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                                "Due: " + sdf.format(Date(assignment.dueDateMillis))
                                            } else "No Due Date"
                                            ConnectionCard(
                                                title = assignment.title,
                                                subtitle = "$courseName | Category: ${assignment.category}",
                                                infoText = dateStr,
                                                badgeText = "Assignment",
                                                badgeColor = try { Color(android.graphics.Color.parseColor(assignment.categoryColor)) } catch(e: Exception) { MaterialTheme.colorScheme.primary },
                                                onClick = { navController.navigate("courseDetail/${assignment.courseId}") }
                                            )
                                        }
                                    }

                                    // 6. Connected Tasks (Interactive Checkbox)
                                    if (meta.tasks.isNotEmpty()) {
                                        item {
                                            ConnectionSectionHeader("Self-Study Tasks (${meta.tasks.size})", Icons.Rounded.TaskAlt)
                                        }
                                        items(meta.tasks) { task ->
                                            val priorityLabel = when (task.priority) {
                                                2 -> "High"
                                                1 -> "Medium"
                                                else -> "Low"
                                            }
                                            val priorityColor = when (task.priority) {
                                                2 -> MaterialTheme.colorScheme.error
                                                1 -> MaterialTheme.colorScheme.tertiary
                                                else -> MaterialTheme.colorScheme.outline
                                            }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .shadow(2.dp, RoundedCornerShape(16.dp))
                                                    .background(
                                                        MaterialTheme.colorScheme.surface,
                                                        RoundedCornerShape(16.dp)
                                                    )
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = task.isCompleted,
                                                    onCheckedChange = {
                                                        viewModel.updateTask(task.copy(isCompleted = it))
                                                    }
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = task.title,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                                        textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                                    )
                                                    if (task.description.isNotBlank()) {
                                                        Text(
                                                            text = task.description,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(top = 4.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .background(
                                                                    priorityColor.copy(alpha = 0.15f),
                                                                    RoundedCornerShape(4.dp)
                                                                )
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = priorityLabel,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = priorityColor,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                                BouncyIconButton(
                                                    onClick = {
                                                        // Quick study pomodoro shortcut for task
                                                        navController.navigate("pomodoro?taskId=${task.id}")
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.PlayArrow,
                                                        contentDescription = "Start Pomodoro Session",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // 7. Connected Test Records
                                    if (meta.testRecords.isNotEmpty()) {
                                        item {
                                            ConnectionSectionHeader("Test Records (${meta.testRecords.size})", Icons.Rounded.Analytics)
                                        }
                                        items(meta.testRecords) { record ->
                                            val percentage = if (record.totalMarks > 0f) {
                                                (record.marksObtained / record.totalMarks * 100f)
                                            } else 0f
                                            ConnectionCard(
                                                title = record.title,
                                                subtitle = "Score: ${record.marksObtained}/${record.totalMarks} (${String.format("%.1f", percentage)}%)",
                                                infoText = record.notes.ifBlank { "No additional test notes" },
                                                badgeText = "Test Record",
                                                badgeColor = MaterialTheme.colorScheme.error,
                                                onClick = {
                                                    if (record.courseId != null) {
                                                        navController.navigate("courseDetail/${record.courseId}")
                                                    } else if (record.subjectId != null) {
                                                        navController.navigate("subjectDetail/${record.subjectId}")
                                                    }
                                                }
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
    }

    // Interactive Action Dialogs

    // 1. Rename Tag
    if (showRenameDialog && selectedTagMetadata != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Tag Globally") },
            text = {
                Column {
                    Text(
                        text = "Renaming '${selectedTagMetadata.name}' will update it across all Courses, Subjects, Chapters, Topics, Assignments, Tasks, and Test Records.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = renameNewValue,
                        onValueChange = { renameNewValue = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rename_tag_input"),
                        label = { Text("New Tag Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = renameNewValue.trim()
                        if (trimmed.isNotBlank() && trimmed.lowercase() != selectedTagMetadata.name.lowercase()) {
                            viewModel.renameTagGlobally(selectedTagMetadata.name, trimmed)
                            selectedTagName = trimmed
                        }
                        showRenameDialog = false
                    },
                    modifier = Modifier.testTag("rename_tag_confirm")
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 2. Delete Tag
    if (showDeleteDialog && selectedTagMetadata != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Tag Globally") },
            text = {
                Text(
                    text = "Are you sure you want to delete '${selectedTagMetadata.name}'? This tag will be removed from all connected academic objects. The objects themselves will not be deleted."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTagGlobally(selectedTagMetadata.name)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("delete_tag_confirm")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 3. Merge Tag
    if (showMergeDialog && selectedTagMetadata != null) {
        var mergeSelectedTag by remember { mutableStateOf("") }
        val otherTags = remember(allTagsMetadata, selectedTagMetadata) {
            allTagsMetadata.filter { it.name.lowercase() != selectedTagMetadata.name.lowercase() }
        }

        AlertDialog(
            onDismissRequest = { showMergeDialog = false },
            title = { Text("Merge Tags") },
            text = {
                Column {
                    Text(
                        text = "Merge and combine all connections of '${selectedTagMetadata.name}' into another tag. This tag will be deleted, and all connected items will receive the other tag instead.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    if (otherTags.isEmpty()) {
                        Text(
                            text = "No other tags available to merge with.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { dropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (mergeSelectedTag.isBlank()) "Choose Target Tag" else mergeSelectedTag,
                                        color = if (mergeSelectedTag.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            }
                            
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                otherTags.forEach { otherMeta ->
                                    DropdownMenuItem(
                                        text = { Text(otherMeta.name) },
                                        onClick = {
                                            mergeSelectedTag = otherMeta.name
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = mergeSelectedTag.trim()
                        if (target.isNotBlank()) {
                            // To merge A into B: rename A to B globally! 
                            // This automatically groups all connections under B.
                            viewModel.renameTagGlobally(selectedTagMetadata.name, target)
                            selectedTagName = target
                        }
                        showMergeDialog = false
                    },
                    enabled = mergeSelectedTag.isNotBlank(),
                    modifier = Modifier.testTag("merge_tag_confirm")
                ) {
                    Text("Merge & Combine")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMergeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ConnectionSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun ConnectionCard(
    title: String,
    subtitle: String,
    infoText: String,
    badgeText: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (infoText.isNotBlank()) {
                    Text(
                        text = infoText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Navigate to Connection",
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
