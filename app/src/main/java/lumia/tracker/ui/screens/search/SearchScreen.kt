package lumia.tracker.ui.screens.search

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.R
import lumia.tracker.model.*
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.GlassCard
import lumia.tracker.ui.theme.LocalGlassMode
import lumia.tracker.ui.util.getTagColors
import lumia.tracker.viewmodel.ScholarViewModel
import java.text.SimpleDateFormat
import java.util.*

data class SearchResult(
    val id: Int,
    val title: String,
    val subtitle: String = "",
    val type: String, // "Course", "Subject", "Chapter", "Topic", "Assignment", "Task", "Note", "Test Record"
    val tags: String = "",
    val meta: String = "",
    val isCompleted: Boolean = false,
    val originalEntity: Any
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(navController: NavController, viewModel: ScholarViewModel) {
    val isGlass = LocalGlassMode.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // Observe DB streams
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val chapters by viewModel.allChapters.collectAsStateWithLifecycle()
    val topics by viewModel.allTopics.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val testRecords by viewModel.allTestRecords.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedResultForDialog by remember { mutableStateOf<SearchResult?>(null) }

    // Aggregate data and filter based on search query and category filter
    val searchResults = remember(
        searchQuery, selectedFilter, courses, subjects, chapters, topics, assignments, tasks, notes, testRecords
    ) {
        val query = searchQuery.trim().lowercase()
        val list = mutableListOf<SearchResult>()

        // 1. Courses
        if (selectedFilter == "All" || selectedFilter == "Courses") {
            courses.forEach { c ->
                if (query.isEmpty() || 
                    c.name.lowercase().contains(query) || 
                    c.code.lowercase().contains(query) || 
                    c.instructor.lowercase().contains(query) || 
                    c.description.lowercase().contains(query) || 
                    c.tags.lowercase().contains(query)
                ) {
                    list.add(
                        SearchResult(
                            id = c.id,
                            title = c.name,
                            subtitle = if (c.code.isNotBlank()) "Code: ${c.code} • ${c.instructor}" else c.instructor,
                            type = "Course",
                            tags = c.tags,
                            meta = c.description,
                            originalEntity = c
                        )
                    )
                }
            }
        }

        // 2. Subjects
        if (selectedFilter == "All" || selectedFilter == "Subjects") {
            subjects.forEach { s ->
                if (query.isEmpty() || 
                    s.name.lowercase().contains(query) || 
                    s.tags.lowercase().contains(query)
                ) {
                    list.add(
                        SearchResult(
                            id = s.id,
                            title = s.name,
                            subtitle = "Subject Workspace",
                            type = "Subject",
                            tags = s.tags,
                            originalEntity = s
                        )
                    )
                }
            }
        }

        // 3. Chapters
        if (selectedFilter == "All" || selectedFilter == "Chapters") {
            chapters.forEach { ch ->
                val parentSubject = subjects.find { it.id == ch.subjectId }?.name ?: "Subject"
                if (query.isEmpty() || 
                    ch.name.lowercase().contains(query) || 
                    ch.description.lowercase().contains(query) || 
                    ch.tags.lowercase().contains(query)
                ) {
                    list.add(
                        SearchResult(
                            id = ch.id,
                            title = ch.name,
                            subtitle = "Chapter • Under: $parentSubject",
                            type = "Chapter",
                            tags = ch.tags,
                            meta = ch.description,
                            originalEntity = ch
                        )
                    )
                }
            }
        }

        // 4. Topics
        if (selectedFilter == "All" || selectedFilter == "Topics") {
            topics.forEach { t ->
                val parentSubject = subjects.find { it.id == t.subjectId }?.name ?: "Subject"
                if (query.isEmpty() || 
                    t.title.lowercase().contains(query) || 
                    t.tags.lowercase().contains(query)
                ) {
                    list.add(
                        SearchResult(
                            id = t.id,
                            title = t.title,
                            subtitle = "Topic • Under: $parentSubject",
                            type = "Topic",
                            tags = t.tags,
                            isCompleted = t.isCompleted,
                            originalEntity = t
                        )
                    )
                }
            }
        }

        // 5. Assignments
        if (selectedFilter == "All" || selectedFilter == "Assignments") {
            assignments.forEach { a ->
                val parentCourse = courses.find { it.id == a.courseId }?.name ?: "Course"
                if (query.isEmpty() || 
                    a.title.lowercase().contains(query) || 
                    a.description.lowercase().contains(query) || 
                    a.category.lowercase().contains(query) || 
                    a.tags.lowercase().contains(query)
                ) {
                    list.add(
                        SearchResult(
                            id = a.id,
                            title = a.title,
                            subtitle = "Assignment • [${a.category}] in $parentCourse",
                            type = "Assignment",
                            tags = a.tags,
                            meta = a.description,
                            isCompleted = a.isCompleted,
                            originalEntity = a
                        )
                    )
                }
            }
        }

        // 6. Tasks
        if (selectedFilter == "All" || selectedFilter == "Tasks") {
            tasks.forEach { tk ->
                if (query.isEmpty() || 
                    tk.title.lowercase().contains(query) || 
                    tk.description.lowercase().contains(query) || 
                    tk.tags.lowercase().contains(query)
                ) {
                    list.add(
                        SearchResult(
                            id = tk.id,
                            title = tk.title,
                            subtitle = "Task • Due: " + (tk.dueDateMillis?.let {
                                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                            } ?: "No Deadline"),
                            type = "Task",
                            tags = tk.tags,
                            meta = tk.description,
                            isCompleted = tk.isCompleted,
                            originalEntity = tk
                        )
                    )
                }
            }
        }

        // 7. Notes
        if (selectedFilter == "All" || selectedFilter == "Notes") {
            notes.forEach { n ->
                val associatedCourse = courses.find { it.id == n.courseId }?.name
                val associatedSubject = subjects.find { it.id == n.subjectId }?.name
                val origin = when {
                    associatedCourse != null -> "Course: $associatedCourse"
                    associatedSubject != null -> "Subject: $associatedSubject"
                    else -> "Quick Note"
                }
                if (query.isEmpty() || 
                    n.content.lowercase().contains(query) || 
                    n.tag.lowercase().contains(query)
                ) {
                    list.add(
                        SearchResult(
                            id = n.id,
                            title = if (n.content.length > 50) n.content.take(50) + "..." else n.content,
                            subtitle = "Quick Note • $origin",
                            type = "Note",
                            tags = n.tag,
                            meta = n.content,
                            originalEntity = n
                        )
                    )
                }
            }
        }

        // 8. Test Records
        if (selectedFilter == "All" || selectedFilter == "Test Records") {
            testRecords.forEach { tr ->
                val parentSubject = subjects.find { it.id == tr.subjectId }?.name
                val parentCourse = courses.find { it.id == tr.courseId }?.name
                val origin = parentSubject ?: parentCourse ?: "Workspace"
                if (query.isEmpty() || 
                    tr.title.lowercase().contains(query) || 
                    tr.notes.lowercase().contains(query) || 
                    tr.tags.lowercase().contains(query)
                ) {
                    list.add(
                        SearchResult(
                            id = tr.id,
                            title = "${tr.title} • Grade: ${tr.marksObtained}/${tr.totalMarks}",
                            subtitle = "Test Record • Under: $origin",
                            type = "Test Record",
                            tags = tr.tags,
                            meta = tr.notes,
                            originalEntity = tr
                        )
                    )
                }
            }
        }

        list
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Header with Back button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BouncyIconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("search_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Global Search Hub",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Explore everything in your academic workspace",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Input Box
                val glassModifier = if (isGlass) {
                    Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 0.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.12f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(24.dp)
                        )
                } else {
                    Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                }

                Row(
                    modifier = glassModifier
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Type notes, tags, titles or descriptions...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_text_input")
                    )
                    if (searchQuery.isNotEmpty()) {
                        BouncyIconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.testTag("search_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear search query",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable category filter row
                val categories = listOf("All", "Courses", "Subjects", "Chapters", "Topics", "Assignments", "Tasks", "Notes", "Test Records")
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedFilter == cat
                        val filterBg = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            if (isGlass) MaterialTheme.colorScheme.surface.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHigh
                        }
                        val filterContentColor = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                        val borderMod = if (!isSelected && isGlass) {
                            Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        } else Modifier

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(filterBg)
                                .then(borderMod)
                                .clickable { selectedFilter = cat }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("filter_chip_$cat"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val catIcon = when(cat) {
                                    "Courses" -> Icons.Rounded.School
                                    "Subjects" -> Icons.Rounded.AutoStories
                                    "Chapters" -> Icons.Rounded.ListAlt
                                    "Topics" -> Icons.Rounded.BubbleChart
                                    "Assignments" -> Icons.Rounded.Assignment
                                    "Tasks" -> Icons.Rounded.TaskAlt
                                    "Notes" -> Icons.Rounded.StickyNote2
                                    "Test Records" -> Icons.Rounded.Grade
                                    else -> Icons.Rounded.GridView
                                }
                                Icon(
                                    imageVector = catIcon,
                                    contentDescription = null,
                                    tint = filterContentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = filterContentColor
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (searchResults.isEmpty()) {
                // Empty state or tip illustration
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val searchIllustrationIcon = if (searchQuery.trim().isEmpty()) Icons.Rounded.TravelExplore else Icons.Rounded.SearchOff
                    val explanationText = if (searchQuery.trim().isEmpty()) {
                        "Explore your courses, subjects, assignments, chapters, topics, notes, and records smoothly in real-time."
                    } else {
                        "No results found for \"$searchQuery\". Try broadening your filters or checking spelling."
                    }

                    Icon(
                        imageVector = searchIllustrationIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = if (searchQuery.trim().isEmpty()) "Global Study Explorer" else "No matching items",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = explanationText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )

                    if (searchQuery.trim().isEmpty()) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            "Try searching tags like:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Build unique active tags suggested row
                        val activeTags = remember(courses, subjects, chapters, topics, assignments, tasks, testRecords) {
                            val tagsSet = mutableSetOf<String>()
                            courses.forEach { tagsSet.addAll(it.tags.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }) }
                            subjects.forEach { tagsSet.addAll(it.tags.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }) }
                            chapters.forEach { tagsSet.addAll(it.tags.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }) }
                            topics.forEach { tagsSet.addAll(it.tags.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }) }
                            assignments.forEach { tagsSet.addAll(it.tags.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }) }
                            tasks.forEach { tagsSet.addAll(it.tags.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }) }
                            testRecords.forEach { tagsSet.addAll(it.tags.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }) }
                            tagsSet.take(5)
                        }

                        if (activeTags.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.Center,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                maxItemsInEachRow = 3,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                activeTags.forEach { t ->
                                    val colors = getTagColors(t)
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(colors.first)
                                            .clickable { searchQuery = t }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("#$t", color = colors.second, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            Text(
                                "No tags created yet! Create them on topics or assignments.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(searchResults) { result ->
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_result_item_${result.type}_${result.id}"),
                            onClick = {
                                when (result.type) {
                                    "Course" -> navController.navigate("courseDetail/${result.id}")
                                    "Subject" -> navController.navigate("subjectDetail/${result.id}")
                                    else -> {
                                        selectedResultForDialog = result
                                    }
                                }
                            }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Result Type Badge and title
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val badgeColor = when (result.type) {
                                        "Course" -> Color(0xFF3197D6)
                                        "Subject" -> Color(0xFF4CAF50)
                                        "Chapter" -> Color(0xFFFF9800)
                                        "Topic" -> Color(0xFF9C27B0)
                                        "Assignment" -> Color(0xFFE91E63)
                                        "Task" -> Color(0xFF3F51B5)
                                        "Note" -> Color(0xFF607D8B)
                                        else -> Color(0xFF9E9E9E)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(badgeColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = result.type.uppercase(),
                                            color = badgeColor,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    if (result.isCompleted) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = "Completed",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    val itemIcon = when (result.type) {
                                        "Course" -> Icons.Rounded.School
                                        "Subject" -> Icons.Rounded.AutoStories
                                        "Chapter" -> Icons.Rounded.ListAlt
                                        "Topic" -> Icons.Rounded.BubbleChart
                                        "Assignment" -> Icons.Rounded.Assignment
                                        "Task" -> Icons.Rounded.TaskAlt
                                        "Note" -> Icons.Rounded.StickyNote2
                                        else -> Icons.Rounded.Grade
                                    }
                                    Icon(
                                        imageVector = itemIcon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Title
                                Text(
                                    text = result.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (result.subtitle.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = result.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (result.meta.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = result.meta,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Tags inside result card
                                val tagList = remember(result.tags) {
                                    result.tags.split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotBlank() }
                                }
                                if (tagList.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        tagList.forEach { tag ->
                                            val colors = getTagColors(tag)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(colors.first)
                                                    .clickable { searchQuery = tag } // SATISFYING INTERACTIVE FILTERING
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "#$tag",
                                                    color = colors.second,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
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
    }

    // Interactive details dialog
    selectedResultForDialog?.let { result ->
        SearchDetailDialog(
            result = result,
            viewModel = viewModel,
            isGlass = isGlass,
            navController = navController,
            onDismiss = { selectedResultForDialog = null }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchDetailDialog(
    result: SearchResult,
    viewModel: ScholarViewModel,
    isGlass: Boolean,
    navController: NavController,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        val glassCardModifier = if (isGlass) {
            Modifier
                .fillMaxWidth()
                .shadow(elevation = 0.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.12f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(28.dp)
                )
        } else {
            Modifier
                .fillMaxWidth()
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        }

        Box(modifier = glassCardModifier.padding(24.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header of dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val badgeColor = when (result.type) {
                        "Chapter" -> Color(0xFFFF9800)
                        "Topic" -> Color(0xFF9C27B0)
                        "Assignment" -> Color(0xFFE91E63)
                        "Task" -> Color(0xFF3F51B5)
                        "Note" -> Color(0xFF607D8B)
                        else -> Color(0xFF9E9E9E)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = result.type.uppercase(),
                            color = badgeColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    BouncyIconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close details modal",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Title & Subtitle
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = result.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Detail Description Context
                if (result.meta.isNotBlank()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Description / Content",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = result.meta,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Tags details
                val tagList = remember(result.tags) {
                    result.tags.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                }
                if (tagList.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Associated Tags",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            tagList.forEach { tag ->
                                val colors = getTagColors(tag)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.first)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "#$tag",
                                        color = colors.second,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Contextual Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Copy Content action for Note
                    if (result.type == "Note") {
                        BouncyButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(result.meta))
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copy Note Content", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    // 2. Toggle completion status action for Topic, Assignment, Task
                    if (result.type == "Topic" || result.type == "Assignment" || result.type == "Task") {
                        val isCurrentCompleted = result.isCompleted
                        val btnText = if (isCurrentCompleted) "Mark Incomplete" else "Mark Complete"
                        val btnIcon = if (isCurrentCompleted) Icons.Rounded.Undo else Icons.Rounded.CheckCircle

                        BouncyButton(
                            onClick = {
                                when (result.type) {
                                    "Topic" -> viewModel.toggleTopicCompleted(result.originalEntity as Topic)
                                    "Assignment" -> viewModel.toggleAssignmentCompleted(result.originalEntity as PracticeAssignment)
                                    "Task" -> viewModel.toggleTaskCompleted(result.originalEntity as Task)
                                }
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrentCompleted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
                                contentColor = if (isCurrentCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(btnIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(btnText, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    // 3. Open Pomodoro Focus timer action for Tasks and Assignments
                    if (result.type == "Task" || result.type == "Assignment" || result.type == "Topic") {
                        BouncyButton(
                            onClick = {
                                val route = when (result.type) {
                                    "Task" -> "pomodoro?taskId=${result.id}"
                                    "Assignment" -> "pomodoro?assignmentId=${result.id}"
                                    "Topic" -> "pomodoro?topicId=${result.id}"
                                    else -> "pomodoro"
                                }
                                navController.navigate(route)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Focus Session", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    // Simple back action
                    BouncyButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
