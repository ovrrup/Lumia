package lumia.tracker.ui.screens.search

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.*
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.theme.animateItemEntry
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

/**
 * Builds an AnnotatedString that highlights search query occurrences with accent styling.
 */
private fun buildHighlightedText(
    text: String,
    query: String,
    highlightColor: Color,
    highlightFontWeight: FontWeight = FontWeight.Bold
): AnnotatedString {
    val trimmedQuery = query.trim()
    if (trimmedQuery.isEmpty() || !text.contains(trimmedQuery, ignoreCase = true)) {
        return AnnotatedString(text)
    }

    return buildAnnotatedString {
        var currentIndex = 0
        val lowerText = text.lowercase(Locale.getDefault())
        val lowerQuery = trimmedQuery.lowercase(Locale.getDefault())
        val queryLength = trimmedQuery.length

        while (currentIndex < text.length) {
            val matchIndex = lowerText.indexOf(lowerQuery, currentIndex)
            if (matchIndex == -1) {
                append(text.substring(currentIndex))
                break
            }
            if (matchIndex > currentIndex) {
                append(text.substring(currentIndex, matchIndex))
            }
            pushStyle(
                SpanStyle(
                    color = highlightColor,
                    fontWeight = highlightFontWeight
                )
            )
            append(text.substring(matchIndex, matchIndex + queryLength))
            pop()
            currentIndex = matchIndex + queryLength
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(navController: NavController, viewModel: ScholarViewModel) {
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

    // Aggregate all data into a unified list
    val allAggregatedResults = remember(
        courses, subjects, chapters, topics, assignments, tasks, notes, testRecords
    ) {
        val list = mutableListOf<SearchResult>()

        // 1. Courses
        courses.forEach { c ->
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

        // 2. Subjects
        subjects.forEach { s ->
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

        // 3. Chapters
        chapters.forEach { ch ->
            val parentSubject = subjects.find { it.id == ch.subjectId }?.name ?: "Subject"
            list.add(
                SearchResult(
                    id = ch.id,
                    title = ch.name,
                    subtitle = "Chapter • In: $parentSubject",
                    type = "Chapter",
                    tags = ch.tags,
                    meta = ch.description,
                    originalEntity = ch
                )
            )
        }

        // 4. Topics
        topics.forEach { t ->
            val parentSubject = subjects.find { it.id == t.subjectId }?.name ?: "Subject"
            list.add(
                SearchResult(
                    id = t.id,
                    title = t.title,
                    subtitle = "Topic • In: $parentSubject",
                    type = "Topic",
                    tags = t.tags,
                    isCompleted = t.isCompleted,
                    originalEntity = t
                )
            )
        }

        // 5. Assignments
        assignments.forEach { a ->
            val parentCourse = courses.find { it.id == a.courseId }?.name ?: "Course"
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

        // 6. Tasks
        tasks.forEach { tk ->
            val dueStr = tk.dueDateMillis?.let {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
            } ?: "No Deadline"
            list.add(
                SearchResult(
                    id = tk.id,
                    title = tk.title,
                    subtitle = "Task • Due: $dueStr",
                    type = "Task",
                    tags = tk.tags,
                    meta = tk.description,
                    isCompleted = tk.isCompleted,
                    originalEntity = tk
                )
            )
        }

        // 7. Notes
        notes.forEach { n ->
            val associatedCourse = courses.find { it.id == n.courseId }?.name
            val associatedSubject = subjects.find { it.id == n.subjectId }?.name
            val origin = when {
                associatedCourse != null -> "Course: $associatedCourse"
                associatedSubject != null -> "Subject: $associatedSubject"
                else -> "Quick Note"
            }
            list.add(
                SearchResult(
                    id = n.id,
                    title = if (n.content.length > 60) n.content.take(60) + "..." else n.content,
                    subtitle = "Quick Note • $origin",
                    type = "Note",
                    tags = n.tag,
                    meta = n.content,
                    originalEntity = n
                )
            )
        }

        // 8. Test Records
        testRecords.forEach { tr ->
            val parentSubject = subjects.find { it.id == tr.subjectId }?.name
            val parentCourse = courses.find { it.id == tr.courseId }?.name
            val origin = parentSubject ?: parentCourse ?: "Workspace"
            list.add(
                SearchResult(
                    id = tr.id,
                    title = "${tr.title} • Score: ${tr.marksObtained}/${tr.totalMarks}",
                    subtitle = "Test Record • Under: $origin",
                    type = "Test Record",
                    tags = tr.tags,
                    meta = tr.notes,
                    originalEntity = tr
                )
            )
        }

        list
    }

    // Filter results based on search query and category
    val searchResults = remember(searchQuery, selectedFilter, allAggregatedResults) {
        val query = searchQuery.trim().lowercase(Locale.getDefault())

        allAggregatedResults.filter { item ->
            val matchesCategory = when (selectedFilter) {
                "All" -> true
                "Courses" -> item.type == "Course"
                "Subjects" -> item.type == "Subject"
                "Tasks" -> item.type == "Task"
                "Assignments" -> item.type == "Assignment"
                "Notes" -> item.type == "Note"
                "Tests" -> item.type == "Test Record"
                "Chapters" -> item.type == "Chapter"
                "Topics" -> item.type == "Topic"
                else -> item.type.equals(selectedFilter, ignoreCase = true)
            }

            if (!matchesCategory) return@filter false

            if (query.isEmpty()) return@filter true

            item.title.lowercase(Locale.getDefault()).contains(query) ||
                    item.subtitle.lowercase(Locale.getDefault()).contains(query) ||
                    item.meta.lowercase(Locale.getDefault()).contains(query) ||
                    item.tags.lowercase(Locale.getDefault()).contains(query)
        }
    }

    // Active tags extracted across workspace
    val activeTags = remember(courses, subjects, chapters, topics, assignments, tasks, notes, testRecords) {
        val tagsSet = linkedSetOf<String>()
        courses.forEach { tagsSet.addAll(it.tags.split(",").map { t -> t.trim().lowercase(Locale.getDefault()) }.filter { it.isNotEmpty() }) }
        subjects.forEach { tagsSet.addAll(it.tags.split(",").map { t -> t.trim().lowercase(Locale.getDefault()) }.filter { it.isNotEmpty() }) }
        chapters.forEach { tagsSet.addAll(it.tags.split(",").map { t -> t.trim().lowercase(Locale.getDefault()) }.filter { it.isNotEmpty() }) }
        topics.forEach { tagsSet.addAll(it.tags.split(",").map { t -> t.trim().lowercase(Locale.getDefault()) }.filter { it.isNotEmpty() }) }
        assignments.forEach { tagsSet.addAll(it.tags.split(",").map { t -> t.trim().lowercase(Locale.getDefault()) }.filter { it.isNotEmpty() }) }
        tasks.forEach { tagsSet.addAll(it.tags.split(",").map { t -> t.trim().lowercase(Locale.getDefault()) }.filter { it.isNotEmpty() }) }
        notes.forEach { tagsSet.addAll(it.tag.split(",").map { t -> t.trim().lowercase(Locale.getDefault()) }.filter { it.isNotEmpty() }) }
        testRecords.forEach { tagsSet.addAll(it.tags.split(",").map { t -> t.trim().lowercase(Locale.getDefault()) }.filter { it.isNotEmpty() }) }
        tagsSet.take(8).toList()
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
                // Header Bar with Back Button & Title
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
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Search Workspace",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Instant access to all study materials",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Full-width Search Input Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    ),
                    shadowElevation = 0.5.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = "Search courses, tasks, notes, tags...",
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

                        AnimatedVisibility(
                            visible = searchQuery.isNotEmpty(),
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            BouncyIconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("search_clear_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Clear search",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Horizontal Filter Chips
                val categories = listOf(
                    CategoryItem("All", Icons.Rounded.GridView),
                    CategoryItem("Courses", Icons.Rounded.School),
                    CategoryItem("Subjects", Icons.Rounded.AutoStories),
                    CategoryItem("Tasks", Icons.Rounded.TaskAlt),
                    CategoryItem("Assignments", Icons.Rounded.Assignment),
                    CategoryItem("Notes", Icons.Rounded.StickyNote2),
                    CategoryItem("Tests", Icons.Rounded.Grade),
                    CategoryItem("Chapters", Icons.Rounded.ListAlt),
                    CategoryItem("Topics", Icons.Rounded.BubbleChart)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(categories) { catItem ->
                        val isSelected = selectedFilter == catItem.name
                        val chipBg = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        }
                        val chipContentColor = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = chipBg,
                            border = if (!isSelected) {
                                BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            } else null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedFilter = catItem.name }
                                .testTag("filter_chip_${catItem.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = catItem.icon,
                                    contentDescription = null,
                                    tint = chipContentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = catItem.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = chipContentColor
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
                // Expressive Empty State
                if (searchQuery.trim().isEmpty()) {
                    // "Search your workspace" default state
                    SearchInitialWorkspaceView(
                        activeTags = activeTags,
                        onTagClick = { tag -> searchQuery = tag },
                        onCategorySelect = { cat -> selectedFilter = cat }
                    )
                } else {
                    // "No results found" empty state
                    SearchNoResultsView(
                        query = searchQuery,
                        selectedFilter = selectedFilter,
                        onClearSearch = { searchQuery = "" },
                        onResetFilter = { selectedFilter = "All" }
                    )
                }
            } else {
                // Results List with Staggered Animation and Highlighted Matches
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = searchResults,
                        key = { "${it.type}_${it.id}" }
                    ) { result ->
                        val index = searchResults.indexOf(result)
                        SearchResultItemCard(
                            result = result,
                            searchQuery = searchQuery,
                            modifier = Modifier
                                .animateItemEntry(index)
                                .testTag("search_result_item_${result.type}_${result.id}"),
                            onTagClick = { tag -> searchQuery = tag },
                            onClick = {
                                when (result.type) {
                                    "Course" -> navController.navigate("courseDetail/${result.id}")
                                    "Subject" -> navController.navigate("subjectDetail/${result.id}")
                                    else -> selectedResultForDialog = result
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Quick Action Details Modal
    selectedResultForDialog?.let { result ->
        SearchDetailDialog(
            result = result,
            viewModel = viewModel,
            navController = navController,
            onDismiss = { selectedResultForDialog = null }
        )
    }
}

private data class CategoryItem(val name: String, val icon: ImageVector)

@Composable
private fun SearchResultItemCard(
    result: SearchResult,
    searchQuery: String,
    modifier: Modifier = Modifier,
    onTagClick: (String) -> Unit,
    onClick: () -> Unit
) {
    val typeConfig = getEntityTypeConfig(result.type)
    val highlightColor = MaterialTheme.colorScheme.primary

    ScholarCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Entity Type Pill + Completion + Origin Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Entity Type Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = typeConfig.color.copy(alpha = 0.12f),
                    border = BorderStroke(0.5.dp, typeConfig.color.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = typeConfig.icon,
                            contentDescription = null,
                            tint = typeConfig.color,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = result.type.uppercase(Locale.getDefault()),
                            color = typeConfig.color,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (result.isCompleted) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Completed",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Done",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title with Highlighted Matches
            Text(
                text = buildHighlightedText(result.title, searchQuery, highlightColor),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Subtitle
            if (result.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = buildHighlightedText(result.subtitle, searchQuery, highlightColor),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Description / Meta context
            if (result.meta.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = buildHighlightedText(result.meta, searchQuery, highlightColor),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Tag Pills
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
                                .clickable { onTagClick(tag) }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchInitialWorkspaceView(
    activeTags: List<String>,
    onTagClick: (String) -> Unit,
    onCategorySelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.TravelExplore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Search your workspace",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Explore your courses, subjects, tasks, assignments, notes, and records in real-time.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        if (activeTags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "POPULAR TAGS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 4,
                modifier = Modifier.fillMaxWidth()
            ) {
                activeTags.forEach { tag ->
                    val colors = getTagColors(tag)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.first)
                            .clickable { onTagClick(tag) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "#$tag",
                            color = colors.second,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchNoResultsView(
    query: String,
    selectedFilter: String,
    onClearSearch: () -> Unit,
    onResetFilter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.SearchOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No results found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "We couldn't find any matches for \"$query\" in ${if (selectedFilter == "All") "your workspace" else selectedFilter}.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.9f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedFilter != "All") {
                BouncyButton(
                    onClick = onResetFilter,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Search All Categories", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
            }

            BouncyButton(
                onClick = onClearSearch,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Clear Query", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private data class EntityTypeConfig(val color: Color, val icon: ImageVector)

@Composable
private fun getEntityTypeConfig(type: String): EntityTypeConfig {
    return when (type) {
        "Course" -> EntityTypeConfig(MaterialTheme.colorScheme.primary, Icons.Rounded.School)
        "Subject" -> EntityTypeConfig(MaterialTheme.colorScheme.secondary, Icons.Rounded.AutoStories)
        "Task" -> EntityTypeConfig(MaterialTheme.colorScheme.tertiary, Icons.Rounded.TaskAlt)
        "Assignment" -> EntityTypeConfig(MaterialTheme.colorScheme.primary, Icons.Rounded.Assignment)
        "Note" -> EntityTypeConfig(MaterialTheme.colorScheme.secondary, Icons.Rounded.StickyNote2)
        "Test Record" -> EntityTypeConfig(MaterialTheme.colorScheme.error, Icons.Rounded.Grade)
        "Chapter" -> EntityTypeConfig(MaterialTheme.colorScheme.tertiary, Icons.Rounded.ListAlt)
        "Topic" -> EntityTypeConfig(MaterialTheme.colorScheme.outline, Icons.Rounded.BubbleChart)
        else -> EntityTypeConfig(MaterialTheme.colorScheme.primary, Icons.Rounded.GridView)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchDetailDialog(
    result: SearchResult,
    viewModel: ScholarViewModel,
    navController: NavController,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val typeConfig = getEntityTypeConfig(result.type)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with Entity Badge + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = typeConfig.color.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, typeConfig.color.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = typeConfig.icon,
                                contentDescription = null,
                                tint = typeConfig.color,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = result.type.uppercase(Locale.getDefault()),
                                color = typeConfig.color,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    BouncyIconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close details",
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
                    if (result.subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Detail Description Context
                if (result.meta.isNotBlank()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Details",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = result.meta,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Tags Details
                val tagList = remember(result.tags) {
                    result.tags.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                }
                if (tagList.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Tags",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
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

                Spacer(modifier = Modifier.height(4.dp))

                // Contextual Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Copy Note Content action
                    if (result.type == "Note") {
                        BouncyButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(result.meta))
                                Toast.makeText(context, "Note copied to clipboard", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("Copy Note Content", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Toggle completion status action for Topic, Assignment, Task
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
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(btnIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text(btnText, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Start Pomodoro Focus timer action
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
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("Start Focus Session", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Close action
                    BouncyTextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
