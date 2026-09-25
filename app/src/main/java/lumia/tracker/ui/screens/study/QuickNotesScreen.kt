package lumia.tracker.ui.screens.study

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lumia.tracker.ui.components.BouncyFloatingActionButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.GlassCapsule
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.components.ScholarCardDefaults
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.theme.bouncyClick
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * QuickNote data representation supporting legacy plain-text entries and categorized modern notes.
 */
data class QuickNote(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val tag: String = "Study",
    val timestamp: Long = System.currentTimeMillis()
)

@ValueScore(
    score = 90,
    importance = Importance.HIGH,
    description = "Modern quick note taking with glassmorphic cards, capsule tags, circular actions, and decluttered input",
    category = "Study"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickNotesScreen(navController: NavController) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val sharedPrefs = remember { context.getSharedPreferences("quick_notes_prefs", Context.MODE_PRIVATE) }

    val notesList = remember { mutableStateListOf<QuickNote>() }
    val coroutineScope = rememberCoroutineScope()
    var selectedFilterTag by remember { mutableStateOf<String?>(null) }

    var showAddSheet by remember { mutableStateOf(false) }
    var newNoteText by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("Study") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val savedNotes = sharedPrefs.getString("notes_json", "[]") ?: "[]"
            try {
                val jsonArray = JSONArray(savedNotes)
                val loadedNotes = mutableListOf<QuickNote>()
                for (i in 0 until jsonArray.length()) {
                    val rawItem = jsonArray.optString(i)
                    if (rawItem.startsWith("{") && rawItem.endsWith("}")) {
                        try {
                            val jsonObject = JSONObject(rawItem)
                            loadedNotes.add(
                                QuickNote(
                                    id = jsonObject.optString("id", UUID.randomUUID().toString()),
                                    content = jsonObject.optString("content", ""),
                                    tag = jsonObject.optString("tag", "Study"),
                                    timestamp = jsonObject.optLong("timestamp", System.currentTimeMillis())
                                )
                            )
                            continue
                        } catch (e: Exception) {
                            // fallback to legacy string
                        }
                    }
                    val tagMatch = Regex("""#(\w+)""").find(rawItem)
                    val detectedTag = tagMatch?.groupValues?.get(1)?.replaceFirstChar { it.uppercase() } ?: "Study"
                    loadedNotes.add(
                        QuickNote(
                            content = rawItem,
                            tag = detectedTag,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                withContext(Dispatchers.Main) {
                    notesList.clear()
                    notesList.addAll(loadedNotes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveNotes() {
        val listSnapshot = notesList.toList()
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val jsonArray = JSONArray()
                listSnapshot.forEach { note ->
                    val obj = JSONObject()
                    obj.put("id", note.id)
                    obj.put("content", note.content)
                    obj.put("tag", note.tag)
                    obj.put("timestamp", note.timestamp)
                    jsonArray.put(obj.toString())
                }
                sharedPrefs.edit().putString("notes_json", jsonArray.toString()).apply()
            }
        }
    }

    val filteredNotes = remember(notesList.toList(), selectedFilterTag) {
        if (selectedFilterTag == null) notesList.toList()
        else notesList.filter { it.tag.equals(selectedFilterTag, ignoreCase = true) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Quick Notes", fontWeight = FontWeight.Bold)
                        if (notesList.isNotEmpty()) {
                            GlassCapsule(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                border = ScholarCardDefaults.glassBorder(
                                    isDark = isDark,
                                    accentColor = MaterialTheme.colorScheme.primary,
                                    width = 0.8.dp
                                )
                            ) {
                                Text(
                                    text = "${notesList.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    BouncyIconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            BouncyFloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Rounded.Add, "Add Note")
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 90.dp
            ),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Quick Filter Capsule Pills Row
            if (notesList.isNotEmpty()) {
                item {
                    val distinctTags = remember(notesList.toList()) {
                        notesList.map { it.tag }.distinct()
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 4.dp)
                    ) {
                        item {
                            val isAllSelected = selectedFilterTag == null
                            Surface(
                                shape = CircleShape,
                                color = if (isAllSelected) MaterialTheme.colorScheme.primaryContainer else ScholarCardDefaults.glassContainerColor(isDark, alpha = 0.5f),
                                border = ScholarCardDefaults.glassBorder(
                                    isDark = isDark,
                                    accentColor = if (isAllSelected) MaterialTheme.colorScheme.primary else null,
                                    width = if (isAllSelected) 1.2.dp else 0.75.dp
                                ),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .bouncyClick { selectedFilterTag = null }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = "All",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isAllSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "(${notesList.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isAllSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        items(distinctTags) { tag ->
                            val isSelected = selectedFilterTag == tag
                            val count = notesList.count { it.tag.equals(tag, ignoreCase = true) }
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else ScholarCardDefaults.glassContainerColor(isDark, alpha = 0.5f),
                                border = ScholarCardDefaults.glassBorder(
                                    isDark = isDark,
                                    accentColor = if (isSelected) MaterialTheme.colorScheme.primary else null,
                                    width = if (isSelected) 1.2.dp else 0.75.dp
                                ),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .bouncyClick {
                                        selectedFilterTag = if (isSelected) null else tag
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "($count)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Empty State
            if (notesList.isEmpty()) {
                item {
                    ScholarCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 28.dp),
                        shape = ScholarCardDefaults.shape,
                        glassmorphic = true,
                        containerColor = ScholarCardDefaults.glassContainerColor(isDark, alpha = 0.5f),
                        border = ScholarCardDefaults.glassBorder(isDark)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.EditNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No Quick Notes Yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Capture formulas, concepts, or reminders on the fly during study sessions.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                border = ScholarCardDefaults.glassBorder(
                                    isDark = isDark,
                                    accentColor = MaterialTheme.colorScheme.primary,
                                    width = 0.8.dp
                                ),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .bouncyClick { showAddSheet = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Add First Note",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (filteredNotes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Rounded.FilterListOff,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No notes tagged with \"$selectedFilterTag\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Note Cards with Glassmorphic Surfaces, Capsule Tags, and Circular Actions
            items(filteredNotes, key = { it.id }) { note ->
                ScholarCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ScholarCardDefaults.shape,
                    glassmorphic = true,
                    containerColor = ScholarCardDefaults.glassContainerColor(isDark),
                    border = ScholarCardDefaults.glassBorder(isDark),
                    animateSize = true
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header Row: Capsule Tag Badge + Timestamp + Circular Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                NoteTagBadge(tag = note.tag)
                                Text(
                                    text = formatNoteTimestamp(note.timestamp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }

                            // Circular Action Buttons
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Circular Copy Button
                                CircularActionButton(
                                    icon = Icons.Rounded.ContentCopy,
                                    contentDescription = "Copy note",
                                    onClick = {
                                        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        clipboardManager?.setPrimaryClip(ClipData.newPlainText("Quick Note", note.content))
                                        Toast.makeText(context, "Note copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                )

                                // Circular Delete Button
                                CircularActionButton(
                                    icon = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Delete note",
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.22f),
                                    contentColor = MaterialTheme.colorScheme.error,
                                    borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.35f),
                                    onClick = {
                                        notesList.remove(note)
                                        saveNotes()
                                    }
                                )
                            }
                        }

                        // Note Content Text
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    // Modern ModalBottomSheet with Decluttered Note Input & Capsule Tag Selector
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddSheet = false
                newNoteText = ""
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header: Title & Circular Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EditNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "New Quick Note",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Circular Close Button
                    CircularActionButton(
                        icon = Icons.Rounded.Close,
                        contentDescription = "Close",
                        onClick = {
                            showAddSheet = false
                            newNoteText = ""
                        }
                    )
                }

                // Capsule Tag Selector Row
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Category Tag",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        val presetTags = listOf("Study", "Idea", "Task", "Question", "Important")
                        items(presetTags) { tag ->
                            val isSelected = selectedTag == tag
                            val tagColor = when (tag.lowercase()) {
                                "study" -> MaterialTheme.colorScheme.primary
                                "idea" -> Color(0xFFF59E0B)
                                "task" -> Color(0xFF10B981)
                                "important" -> Color(0xFFEF4444)
                                "question" -> Color(0xFF8B5CF6)
                                else -> MaterialTheme.colorScheme.secondary
                            }

                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) tagColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                                border = ScholarCardDefaults.glassBorder(
                                    isDark = isDark,
                                    accentColor = if (isSelected) tagColor else null,
                                    width = if (isSelected) 1.2.dp else 0.75.dp
                                ),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .bouncyClick { selectedTag = tag }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(tagColor, CircleShape)
                                        )
                                    }
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) tagColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Decluttered Note Input Field (Glass container, borderless input)
                ScholarCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    glassmorphic = true,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.5f else 0.8f),
                    border = ScholarCardDefaults.glassBorder(isDark = isDark, width = 0.8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        TextField(
                            value = newNoteText,
                            onValueChange = { newNoteText = it },
                            placeholder = {
                                Text(
                                    "Capture thoughts, key formulas, or reminders...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 220.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${newNoteText.length} characters",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Save Note Capsule Button
                Surface(
                    shape = CircleShape,
                    color = if (newNoteText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = ScholarCardDefaults.glassBorder(
                        isDark = isDark,
                        accentColor = if (newNoteText.isNotBlank()) MaterialTheme.colorScheme.primary else null,
                        width = 1.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .bouncyClick(
                            enabled = newNoteText.isNotBlank(),
                            onClick = {
                                if (newNoteText.isNotBlank()) {
                                    notesList.add(0, QuickNote(content = newNoteText.trim(), tag = selectedTag))
                                    saveNotes()
                                    showAddSheet = false
                                    newNoteText = ""
                                }
                            }
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (newNoteText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save Note",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (newNoteText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * NoteTagBadge - Sleek capsule badge showing the category of a quick note.
 */
@Composable
private fun NoteTagBadge(
    tag: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val cleanTag = tag.trim().trimStart('#')
    val tagColor = when (cleanTag.lowercase()) {
        "study", "course", "exam" -> MaterialTheme.colorScheme.primary
        "idea", "thought" -> Color(0xFFF59E0B)
        "task", "todo" -> Color(0xFF10B981)
        "important", "urgent", "priority" -> Color(0xFFEF4444)
        "question", "doubt" -> Color(0xFF8B5CF6)
        else -> MaterialTheme.colorScheme.secondary
    }

    val tagIcon = when (cleanTag.lowercase()) {
        "study", "course", "exam" -> Icons.Rounded.School
        "idea", "thought" -> Icons.Rounded.Lightbulb
        "task", "todo" -> Icons.Rounded.CheckCircle
        "important", "urgent", "priority" -> Icons.Rounded.PriorityHigh
        "question", "doubt" -> Icons.Rounded.HelpOutline
        else -> Icons.Rounded.Bookmark
    }

    Surface(
        modifier = modifier.clip(CircleShape),
        shape = CircleShape,
        color = tagColor.copy(alpha = if (isDark) 0.16f else 0.12f),
        border = ScholarCardDefaults.glassBorder(isDark = isDark, accentColor = tagColor, width = 0.8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = tagIcon,
                contentDescription = null,
                tint = tagColor,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = cleanTag,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = tagColor,
                letterSpacing = 0.2.sp
            )
        }
    }
}

/**
 * CircularActionButton - Circular tactile action button with glass border.
 */
@Composable
private fun CircularActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f),
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
    size: Dp = 32.dp,
    iconSize: Dp = 15.dp
) {
    Surface(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .bouncyClick(onClick = onClick),
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(0.8.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

private fun formatNoteTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diffMillis = now - timestamp
    val diffMinutes = diffMillis / (60 * 1000)
    val diffHours = diffMillis / (60 * 60 * 1000)
    val diffDays = diffMillis / (24 * 60 * 60 * 1000)

    return when {
        diffMinutes < 1 -> "Just now"
        diffMinutes < 60 -> "${diffMinutes}m ago"
        diffHours < 24 -> "${diffHours}h ago"
        diffDays == 1L -> "Yesterday"
        diffDays < 7 -> "${diffDays}d ago"
        else -> {
            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
