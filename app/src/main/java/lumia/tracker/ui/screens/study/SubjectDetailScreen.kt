package lumia.tracker.ui.screens.study

import lumia.tracker.ui.screens.study.dialogs.*

import android.app.DatePickerDialog
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.*
import lumia.tracker.ui.components.*
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.study.dialogs.EditSubjectDialog
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.ui.util.getTagColors
import lumia.tracker.viewmodel.ScholarViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@ValueScore(
    score = 93,
    importance = Importance.CRITICAL,
    description = "Comprehensive Subject Detail screen with chapter hierarchies, syllabus progress rings, tasks, homework, and PDF export",
    category = "Study"
)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SubjectDetailScreen(
    navController: NavController,
    viewModel: ScholarViewModel,
    subjectId: Int
) {
    val context = LocalContext.current
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.find { it.id == subjectId }
    val allCourses by viewModel.courses.collectAsStateWithLifecycle()
    val allNotes by viewModel.notes.collectAsStateWithLifecycle()
    val allTopics by viewModel.allTopics.collectAsStateWithLifecycle(emptyList())
    val allAssignments by viewModel.assignments.collectAsStateWithLifecycle()
    val allTasks by viewModel.tasks.collectAsStateWithLifecycle()
    val subjectChapters by viewModel.getChaptersForSubject(subjectId).collectAsStateWithLifecycle()
    val attachments by viewModel.getAttachmentsForSubject(subjectId).collectAsStateWithLifecycle(emptyList())

    // Filter elements
    val linkedCourses = remember(allCourses, subjectId) {
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
    val subjectAssignments = remember(allAssignments, subjectId) {
        allAssignments.filter { it.subjectId == subjectId }
    }
    val subjectTasks = remember(allTasks, subjectId) {
        allTasks.filter { it.subjectId == subjectId }
    }
    val unassignedTopics = remember(subjectTopics, subjectChapters) {
        val chapterIds = subjectChapters.map { it.id }
        subjectTopics.filter { it.chapterId == null || !chapterIds.contains(it.chapterId) }
    }

    // Dialog trigger states
    var showEditSubjectDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showLinkCourseDialog by remember { mutableStateOf(false) }

    var topicToEdit by remember { mutableStateOf<Topic?>(null) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    var assignmentToEdit by remember { mutableStateOf<PracticeAssignment?>(null) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var chapterToEdit by remember { mutableStateOf<Chapter?>(null) }

    var showAddTopicDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddAssignmentDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddChapterDialog by remember { mutableStateOf(false) }
    var showAddAttachmentDialog by remember { mutableStateOf(false) }
    var attachmentTitle by remember { mutableStateOf("") }
    var attachmentContent by remember { mutableStateOf("") }

    var expandedMenu by remember { mutableStateOf(false) }
    val expandedChapters = remember { mutableStateMapOf<Int, Boolean>() }
    var selectedChapterForNewTopic by remember { mutableStateOf<Int?>(null) }

    val contentResolver = context.contentResolver
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                var displayName = "Attachment_${System.currentTimeMillis()}"
                var sizeBytes: Long = 0
                val cursor = contentResolver.query(uri, null, null, null, null)
                if (cursor != null) {
                    try {
                        val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        val sizeIdx = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                        if (cursor.moveToFirst()) {
                            if (nameIdx != -1) displayName = cursor.getString(nameIdx)
                            if (sizeIdx != -1) sizeBytes = cursor.getLong(sizeIdx)
                        }
                    } finally {
                        cursor.close()
                    }
                }
                val extension = if (displayName.contains(".")) displayName.substringAfterLast(".").lowercase() else "bin"
                val fileType = when (extension) {
                    "pdf" -> "PDF Document"
                    "doc", "docx" -> "Word Document"
                    "xls", "xlsx" -> "Excel Spreadsheet"
                    "ppt", "pptx" -> "PowerPoint Presentation"
                    "txt", "md" -> "Text File"
                    "png", "jpg", "jpeg", "gif", "webp", "bmp" -> "Image"
                    "mp3", "wav", "aac", "flac", "ogg", "m4a" -> "Audio Recording"
                    "mp4", "mkv", "avi", "mov", "webm" -> "Video File"
                    "zip", "rar", "tar", "gz", "7z" -> "Archive"
                    else -> "${extension.uppercase()} File"
                }
                val localFile = File(context.filesDir, "subj_attach_${System.currentTimeMillis()}.$extension")
                val inputStream = contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    try {
                        val outputStream = FileOutputStream(localFile)
                        try {
                            inputStream.copyTo(outputStream)
                        } finally {
                            outputStream.close()
                        }
                    } finally {
                        inputStream.close()
                    }
                }
                viewModel.addAttachment(
                    name = displayName,
                    filePath = localFile.absolutePath,
                    fileType = fileType,
                    sizeBytes = if (sizeBytes > 0) sizeBytes else localFile.length(),
                    courseId = null,
                    subjectId = subjectId
                )
                Toast.makeText(context, "Attachment linked successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to link attachment: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val generateStudyGuidePdf: (String, String) -> Unit = { titleStr, contentStr ->
        try {
            val pdfDoc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()

            paint.color = android.graphics.Color.parseColor("#4F46E5")
            canvas.drawRect(0f, 0f, 595f, 130f, paint)

            paint.color = android.graphics.Color.WHITE
            paint.textSize = 24f
            paint.isFakeBoldText = true
            canvas.drawText("LUMIA SUBJECT OUTLINE", 40f, 55f, paint)

            paint.textSize = 14f
            paint.isFakeBoldText = false
            paint.color = android.graphics.Color.parseColor("#E0E7FF")
            canvas.drawText("Subject: ${subject?.name ?: ""}", 40f, 85f, paint)
            canvas.drawText("Linked Chapters: ${subjectChapters.size} · Topics: ${subjectTopics.size}", 40f, 108f, paint)

            paint.color = android.graphics.Color.BLACK
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText(titleStr, 40f, 175f, paint)

            paint.textSize = 12f
            paint.isFakeBoldText = false
            paint.color = android.graphics.Color.parseColor("#1F2937")

            var y = 210f
            val maxLines = 26
            val words = contentStr.split(" ")
            val wrappedLines = mutableListOf<String>()
            var currentLine = StringBuilder()

            for (word in words) {
                if (word.contains("\n")) {
                    val parts = word.split("\n")
                    for (i in parts.indices) {
                        if (i > 0) {
                            wrappedLines.add(currentLine.toString())
                            currentLine = StringBuilder()
                        }
                        if (currentLine.isNotEmpty()) currentLine.append(" ")
                        currentLine.append(parts[i])
                    }
                } else {
                    if (currentLine.isNotEmpty()) currentLine.append(" ")
                    currentLine.append(word)
                    if (currentLine.length > 68) {
                        wrappedLines.add(currentLine.toString())
                        currentLine = StringBuilder()
                    }
                }
            }
            if (currentLine.isNotEmpty()) {
                wrappedLines.add(currentLine.toString())
            }

            for (line in wrappedLines.take(maxLines)) {
                canvas.drawText(line, 40f, y, paint)
                y += 22f
            }

            paint.color = android.graphics.Color.GRAY
            paint.textSize = 10f
            val dateStr = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault()).format(Date())
            canvas.drawText("Generated with Lumia Scholar Tracker · $dateStr · Page 1 of 1", 40f, 810f, paint)

            pdfDoc.finishPage(page)

            val localFile = File(context.filesDir, "subject_guide_${System.currentTimeMillis()}.pdf")
            pdfDoc.writeTo(localFile.outputStream())
            pdfDoc.close()

            viewModel.addAttachment(
                name = if (titleStr.endsWith(".pdf", ignoreCase = true)) titleStr else "$titleStr.pdf",
                filePath = localFile.absolutePath,
                fileType = "PDF Document",
                sizeBytes = localFile.length(),
                courseId = null,
                subjectId = subjectId
            )
            Toast.makeText(context, "Subject study guide PDF generated successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Generation failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    if (subject == null) {
        Scaffold { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Subject not found", style = MaterialTheme.typography.titleMedium)
            }
        }
        return
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = subject.name,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (subject.tags.isNotBlank()) {
                            Text(
                                text = subject.tags,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    BouncyIconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        BouncyIconButton(onClick = { expandedMenu = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "Subject Actions")
                        }
                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Subject") },
                                onClick = {
                                    expandedMenu = false
                                    showEditSubjectDialog = true
                                },
                                leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Link Courses") },
                                onClick = {
                                    expandedMenu = false
                                    showLinkCourseDialog = true
                                },
                                leadingIcon = { Icon(Icons.Rounded.Link, contentDescription = null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Delete Subject", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    expandedMenu = false
                                    showDeleteConfirmDialog = true
                                },
                                leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. STATS & TOPIC PROGRESS RING HERO CARD
            item {
                val completedTopics = subjectTopics.count { it.isCompleted }
                val totalTopics = subjectTopics.size
                val topicPct = if (totalTopics > 0) ((completedTopics.toFloat() / totalTopics) * 100).roundToInt() else 0
                val progressFloat by animateFloatAsState(
                    targetValue = if (totalTopics > 0) completedTopics.toFloat() / totalTopics else 0f,
                    animationSpec = tween(700),
                    label = "topic_ring"
                )

                val ringColor = when {
                    totalTopics == 0 -> MaterialTheme.colorScheme.primary
                    topicPct == 100 -> Color(0xFF10B981) // Green (Completed)
                    topicPct >= 50 -> Color(0xFF3B82F6)  // Blue (Healthy)
                    topicPct >= 20 -> Color(0xFFF59E0B)  // Amber (In Progress)
                    else -> MaterialTheme.colorScheme.primary
                }

                val animatedRingColor by animateColorAsState(
                    targetValue = ringColor,
                    animationSpec = tween(500),
                    label = "subject_ring_color"
                )

                val masteryBadge = when {
                    totalTopics == 0 -> "No Topics Added"
                    topicPct == 100 -> "Subject Mastered (100%)"
                    topicPct >= 60 -> "High Progress (On Track)"
                    topicPct > 0 -> "In Progress ($completedTopics/$totalTopics)"
                    else -> "Ready to Learn"
                }

                ScholarHeroCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Subject Avatar Box
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = subject.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = subject.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black
                                )
                                if (subject.tags.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        subject.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { tag ->
                                            val colors = getTagColors(tag)
                                            Box(
                                                modifier = Modifier
                                                    .background(colors.first, RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        navController.navigate("tags_hub?selectedTag=$tag")
                                                    }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Rounded.Sell, contentDescription = null, modifier = Modifier.size(10.dp), tint = colors.second)
                                                    Spacer(Modifier.width(2.dp))
                                                    Text(tag, style = MaterialTheme.typography.labelSmall, color = colors.second, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // PROMINENT TOPIC PROGRESS RING GAUGE
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(animatedRingColor.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                                .border(1.dp, animatedRingColor.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(88.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { progressFloat },
                                    modifier = Modifier.fillMaxSize(),
                                    color = animatedRingColor,
                                    trackColor = animatedRingColor.copy(alpha = 0.15f),
                                    strokeWidth = 8.dp,
                                    strokeCap = StrokeCap.Round
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$topicPct%",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = animatedRingColor
                                    )
                                    Text(
                                        text = "Mastery",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (topicPct == 100) Icons.Rounded.CheckCircle else Icons.Rounded.Stars,
                                        contentDescription = null,
                                        tint = animatedRingColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = masteryBadge,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = animatedRingColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "$completedTopics of $totalTopics syllabus topics fully mastered.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // STATS ROW COUNTERS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem(label = "Chapters", count = subjectChapters.size)
                            StatItem(label = "Topics", count = subjectTopics.size, detail = "$completedTopics Done")
                            StatItem(label = "Courses", count = linkedCourses.size)
                            val pendingAssignments = subjectAssignments.count { !it.isCompleted }
                            StatItem(label = "Homework", count = subjectAssignments.size, detail = "$pendingAssignments Left")
                            val pendingTasks = subjectTasks.count { !it.isCompleted }
                            StatItem(label = "Tasks", count = subjectTasks.size, detail = "$pendingTasks Left")
                        }
                    }
                }
            }

            // 2. LINKED COURSES SECTION
            item {
                SectionHeader(
                    title = "Linked Courses",
                    icon = Icons.Rounded.Book,
                    onAddClick = { showLinkCourseDialog = true }
                )
            }
            if (linkedCourses.isEmpty()) {
                item {
                    EmptySectionCard(
                        text = "No linked courses yet. Connect courses to track class schedules and attendance together.",
                        buttonText = "Link Course",
                        onClick = { showLinkCourseDialog = true }
                    )
                }
            } else {
                items(linkedCourses, key = { "course_${it.id}" }) { course ->
                    val color = try { Color(android.graphics.Color.parseColor(course.colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                    ScholarCard(
                        onClick = { navController.navigate("courseDetail/${course.id}") { launchSingleTop = true } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(color))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(course.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (course.code.isNotBlank() || course.instructor.isNotBlank()) {
                                        Text(
                                            text = listOfNotNull(course.code.ifBlank { null }, course.instructor.ifBlank { null }).joinToString(" • "),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            BouncyIconButton(
                                onClick = {
                                    val currentIds = course.subjectIds.split(",").map { it.trim() }.filter { it.isNotEmpty() && it != subjectId.toString() }
                                    val updatedIds = currentIds.joinToString(",")
                                    val newMainId = currentIds.firstOrNull()?.toIntOrNull()
                                    viewModel.updateCourse(course.copy(subjectIds = updatedIds, subjectId = newMainId))
                                }
                            ) {
                                Icon(Icons.Rounded.LinkOff, contentDescription = "Unlink Course", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // 3. STUDY OUTLINE (CHAPTERS & TOPICS)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Study Outline", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        BouncyTextButton(onClick = { showAddChapterDialog = true }) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chapter", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        BouncyTextButton(onClick = {
                            selectedChapterForNewTopic = null
                            showAddTopicDialog = true
                        }) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Topic", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            if (subjectChapters.isEmpty() && subjectTopics.isEmpty()) {
                item {
                    ScholarCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No chapters or topics added yet. Group your learning materials under chapters and track topic completion.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                BouncyButton(onClick = { showAddChapterDialog = true }) {
                                    Text("+ Add Chapter")
                                }
                                BouncyOutlinedButton(onClick = {
                                    selectedChapterForNewTopic = null
                                    showAddTopicDialog = true
                                }) {
                                    Text("+ Add Topic")
                                }
                            }
                        }
                    }
                }
            } else {
                items(subjectChapters, key = { "chapter_${it.id}" }) { chapter ->
                    val isExpanded = expandedChapters.getOrDefault(chapter.id, true)
                    val chapterTopics = remember(subjectTopics, chapter.id) {
                        subjectTopics.filter { it.chapterId == chapter.id }
                    }
                    val chapterCompletedCount = chapterTopics.count { it.isCompleted }
                    val chapterProgress = if (chapterTopics.isNotEmpty()) chapterCompletedCount.toFloat() / chapterTopics.size else 0f
                    val animatedChapterProgress by animateFloatAsState(targetValue = chapterProgress, label = "ch_progress_${chapter.id}")
                    var showChapterMenu by remember { mutableStateOf(false) }

                    ScholarCard(
                        modifier = Modifier.fillMaxWidth().animateContentSize(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f).clickable {
                                        expandedChapters[chapter.id] = !isExpanded
                                    },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowRight,
                                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Folder,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = chapter.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "$chapterCompletedCount/${chapterTopics.size} Topics Complete",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Box {
                                    IconButton(onClick = { showChapterMenu = true }) {
                                        Icon(Icons.Rounded.MoreVert, contentDescription = "Chapter Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    DropdownMenu(
                                        expanded = showChapterMenu,
                                        onDismissRequest = { showChapterMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Add Topic Here") },
                                            onClick = {
                                                showChapterMenu = false
                                                selectedChapterForNewTopic = chapter.id
                                                showAddTopicDialog = true
                                            },
                                            leadingIcon = { Icon(Icons.Rounded.Add, contentDescription = null) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Edit Chapter") },
                                            onClick = {
                                                showChapterMenu = false
                                                chapterToEdit = chapter
                                            },
                                            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete Chapter", color = MaterialTheme.colorScheme.error) },
                                            onClick = {
                                                showChapterMenu = false
                                                viewModel.deleteChapter(chapter)
                                            },
                                            leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                                        )
                                    }
                                }
                            }

                            if (chapterTopics.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { animatedChapterProgress },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                    color = if (chapterCompletedCount == chapterTopics.size) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    strokeCap = StrokeCap.Round
                                )
                            }

                            if (chapter.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = chapter.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 32.dp, bottom = 4.dp)
                                )
                            }

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                if (chapterTopics.isEmpty()) {
                                    Text(
                                        text = "No topics added to this chapter yet.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 32.dp, top = 4.dp, bottom = 4.dp)
                                    )
                                } else {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(start = 16.dp)
                                    ) {
                                        chapterTopics.forEach { topic ->
                                            var showTopicMenu by remember { mutableStateOf(false) }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        if (topic.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(
                                                    onClick = { viewModel.toggleTopicCompleted(topic) },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (topic.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                                        contentDescription = "Toggle Complete",
                                                        tint = if (topic.isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                                                    Text(
                                                        text = topic.title,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        textDecoration = if (topic.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                                        color = if (topic.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    if (topic.tags.isNotBlank()) {
                                                        Row(
                                                            modifier = Modifier.padding(top = 2.dp),
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            topic.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.take(2).forEach { tag ->
                                                                val colors = getTagColors(tag)
                                                                Box(
                                                                    modifier = Modifier
                                                                        .background(colors.first, RoundedCornerShape(4.dp))
                                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                                ) {
                                                                    Text(tag, style = MaterialTheme.typography.labelSmall, color = colors.second, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                Box {
                                                    IconButton(
                                                        onClick = { showTopicMenu = true },
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(Icons.Rounded.MoreVert, contentDescription = "Topic Options", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                                    }
                                                    DropdownMenu(
                                                        expanded = showTopicMenu,
                                                        onDismissRequest = { showTopicMenu = false }
                                                    ) {
                                                        DropdownMenuItem(
                                                            text = { Text("Start Pomodoro") },
                                                            onClick = {
                                                                showTopicMenu = false
                                                                navController.navigate("pomodoro?subjectId=${subjectId}&topicId=${topic.id}")
                                                            },
                                                            leadingIcon = { Icon(Icons.Rounded.Timer, contentDescription = null) }
                                                        )
                                                        DropdownMenuItem(
                                                            text = { Text("Edit Topic") },
                                                            onClick = {
                                                                showTopicMenu = false
                                                                topicToEdit = topic
                                                            },
                                                            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) }
                                                        )
                                                        DropdownMenuItem(
                                                            text = { Text("Delete Topic", color = MaterialTheme.colorScheme.error) },
                                                            onClick = {
                                                                showTopicMenu = false
                                                                viewModel.deleteTopic(topic)
                                                            },
                                                            leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    BouncyTextButton(
                                        onClick = {
                                            selectedChapterForNewTopic = chapter.id
                                            showAddTopicDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Topic Here", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                if (unassignedTopics.isNotEmpty()) {
                    item {
                        var isExpandedUnassigned by remember { mutableStateOf(true) }
                        ScholarCard(
                            modifier = Modifier.fillMaxWidth().animateContentSize(),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f).clickable {
                                            isExpandedUnassigned = !isExpandedUnassigned
                                        },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isExpandedUnassigned) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowRight,
                                            contentDescription = if (isExpandedUnassigned) "Collapse" else "Expand",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.HelpOutline,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "General / Unassigned Topics",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${unassignedTopics.size} Topics",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                if (isExpandedUnassigned) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(start = 16.dp)
                                    ) {
                                        unassignedTopics.forEach { topic ->
                                            var showTopicMenu by remember { mutableStateOf(false) }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        if (topic.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(
                                                    onClick = { viewModel.toggleTopicCompleted(topic) },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (topic.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                                        contentDescription = "Toggle Complete",
                                                        tint = if (topic.isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                                                    Text(
                                                        text = topic.title,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        textDecoration = if (topic.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                                        color = if (topic.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    if (topic.tags.isNotBlank()) {
                                                        Row(
                                                            modifier = Modifier.padding(top = 2.dp),
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            topic.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.take(2).forEach { tag ->
                                                                val colors = getTagColors(tag)
                                                                Box(
                                                                    modifier = Modifier
                                                                        .background(colors.first, RoundedCornerShape(4.dp))
                                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                                ) {
                                                                    Text(tag, style = MaterialTheme.typography.labelSmall, color = colors.second, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                Box {
                                                    IconButton(
                                                        onClick = { showTopicMenu = true },
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(Icons.Rounded.MoreVert, contentDescription = "Topic Options", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                                    }
                                                    DropdownMenu(
                                                        expanded = showTopicMenu,
                                                        onDismissRequest = { showTopicMenu = false }
                                                    ) {
                                                        DropdownMenuItem(
                                                            text = { Text("Start Pomodoro") },
                                                            onClick = {
                                                                showTopicMenu = false
                                                                navController.navigate("pomodoro?subjectId=${subjectId}&topicId=${topic.id}")
                                                            },
                                                            leadingIcon = { Icon(Icons.Rounded.Timer, contentDescription = null) }
                                                        )
                                                        DropdownMenuItem(
                                                            text = { Text("Edit Topic") },
                                                            onClick = {
                                                                showTopicMenu = false
                                                                topicToEdit = topic
                                                            },
                                                            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) }
                                                        )
                                                        DropdownMenuItem(
                                                            text = { Text("Delete Topic", color = MaterialTheme.colorScheme.error) },
                                                            onClick = {
                                                                showTopicMenu = false
                                                                viewModel.deleteTopic(topic)
                                                            },
                                                            leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
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

            // 4. TASKS CHECKLIST SECTION
            item {
                SectionHeader(
                    title = "Tasks Checklist",
                    icon = Icons.Rounded.Assignment,
                    onAddClick = { showAddTaskDialog = true }
                )
            }
            if (subjectTasks.isEmpty()) {
                item {
                    EmptySectionCard(
                        text = "No direct tasks created for this subject yet. Create small actionable steps here.",
                        buttonText = "Add Task",
                        onClick = { showAddTaskDialog = true }
                    )
                }
            } else {
                items(subjectTasks, key = { "task_${it.id}" }) { task ->
                    var showTaskMenu by remember { mutableStateOf(false) }
                    ScholarCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.toggleTaskCompleted(task) }) {
                                Icon(
                                    imageVector = if (task.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = "Toggle Complete",
                                    tint = if (task.isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                )
                                if (task.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(task.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (task.dueDateMillis != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(task.dueDateMillis)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Rounded.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                        Text("Due: $formattedDate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            Box {
                                IconButton(onClick = { showTaskMenu = true }) {
                                    Icon(Icons.Rounded.MoreVert, contentDescription = "Task Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                DropdownMenu(
                                    expanded = showTaskMenu,
                                    onDismissRequest = { showTaskMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Start Pomodoro") },
                                        onClick = {
                                            showTaskMenu = false
                                            val courseParam = task.courseId?.let { "&courseId=$it" } ?: ""
                                            navController.navigate("pomodoro?subjectId=${subjectId}&taskId=${task.id}$courseParam")
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Timer, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Edit Task") },
                                        onClick = {
                                            showTaskMenu = false
                                            taskToEdit = task
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete Task", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            showTaskMenu = false
                                            viewModel.deleteTask(task)
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. ASSIGNMENTS & HOMEWORK SECTION
            item {
                SectionHeader(
                    title = "Assignments & Homework",
                    icon = Icons.AutoMirrored.Rounded.LibraryBooks,
                    onAddClick = { showAddAssignmentDialog = true }
                )
            }
            if (subjectAssignments.isEmpty()) {
                item {
                    EmptySectionCard(
                        text = "No assignments, homework, or exam preparations found for this subject. Create one now.",
                        buttonText = "Add Assignment",
                        onClick = { showAddAssignmentDialog = true }
                    )
                }
            } else {
                items(subjectAssignments, key = { "assignment_${it.id}" }) { assignment ->
                    var showAssignmentMenu by remember { mutableStateOf(false) }
                    ScholarCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.toggleAssignmentCompleted(assignment) }) {
                                Icon(
                                    imageVector = if (assignment.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = "Toggle Complete",
                                    tint = if (assignment.isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = assignment.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        textDecoration = if (assignment.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (assignment.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    val catColor = try { Color(android.graphics.Color.parseColor(assignment.categoryColor)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(catColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(assignment.category, style = MaterialTheme.typography.bodySmall, color = catColor, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (assignment.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(assignment.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (assignment.dueDateMillis > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(assignment.dueDateMillis)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Rounded.Alarm, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.error)
                                        Text("Due: $formattedDate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            Box {
                                IconButton(onClick = { showAssignmentMenu = true }) {
                                    Icon(Icons.Rounded.MoreVert, contentDescription = "Assignment Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                DropdownMenu(
                                    expanded = showAssignmentMenu,
                                    onDismissRequest = { showAssignmentMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Start Pomodoro") },
                                        onClick = {
                                            showAssignmentMenu = false
                                            val courseParam = assignment.courseId.let { "&courseId=$it" }
                                            navController.navigate("pomodoro?subjectId=${subjectId}&assignmentId=${assignment.id}$courseParam")
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Timer, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Edit Assignment") },
                                        onClick = {
                                            showAssignmentMenu = false
                                            assignmentToEdit = assignment
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete Assignment", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            showAssignmentMenu = false
                                            viewModel.deleteAssignment(assignment)
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. SUBJECT NOTES SECTION
            item {
                SectionHeader(
                    title = "Subject Notes",
                    icon = Icons.Rounded.EditNote,
                    onAddClick = { showAddNoteDialog = true }
                )
            }
            if (subjectNotes.isEmpty()) {
                item {
                    EmptySectionCard(
                        text = "No quick notes added yet. Keep formulas, code snippets, or definitions safe right inside the subject.",
                        buttonText = "Add Note",
                        onClick = { showAddNoteDialog = true }
                    )
                }
            } else {
                items(subjectNotes, key = { "note_${it.id}" }) { note ->
                    var isExpanded by remember { mutableStateOf(false) }
                    ScholarCard(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.fillMaxWidth().animateContentSize(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                val formattedDate = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault()).format(note.dateMillis)
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = "Expand",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = note.content,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { noteToEdit = note }) {
                                        Icon(Icons.Rounded.Edit, contentDescription = "Edit Note", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deleteNote(note) }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Delete Note", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. RESOURCE HUB & ATTACHMENTS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Resource Hub & Attachments",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = "Reference notes, cheat sheets & lecture documents",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BouncyButton(
                        onClick = {
                            try {
                                filePickerLauncher.launch(arrayOf("*/*"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "File picking not supported", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Browse File", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }

                    BouncyButton(
                        onClick = {
                            attachmentTitle = "Study Guide - ${subject.name}"
                            attachmentContent = if (subjectNotes.isNotEmpty()) {
                                subjectNotes.joinToString("\n\n") { "• ${it.content}" }
                            } else {
                                "Subject syllabus outline for ${subject.name}.\n\nChapters:\n" +
                                        subjectChapters.joinToString("\n") { "• " + it.name } +
                                        "\n\nTopics:\n" + subjectTopics.joinToString("\n") { "[] " + it.title }
                            }
                            showAddAttachmentDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AI Auto-Guide", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (attachments.isEmpty()) {
                item {
                    ScholarCard(
                        modifier = Modifier.fillMaxWidth().height(110.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No documents attached to this subject.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Upload PDFs or tap AI Auto-Guide to generate a subject outline.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(attachments, key = { "attachment_${it.id}" }) { attachment ->
                    val extension = remember(attachment.filePath) {
                        if (attachment.filePath.contains(".")) attachment.filePath.substringAfterLast(".").lowercase() else ""
                    }
                    val visualMeta = remember(extension) {
                        when (extension) {
                            "pdf" -> Triple(Icons.Rounded.Description, Color(0xFFEF4444), Color(0xFFFEE2E2))
                            "png", "jpg", "jpeg", "gif", "webp", "bmp" -> Triple(Icons.Rounded.Photo, Color(0xFF10B981), Color(0xFFD1FAE5))
                            "txt", "md", "doc", "docx" -> Triple(Icons.Rounded.Article, Color(0xFF3B82F6), Color(0xFFDBEAFE))
                            "xls", "xlsx" -> Triple(Icons.Rounded.Article, Color(0xFF059669), Color(0xFFD1FAE5))
                            "ppt", "pptx" -> Triple(Icons.Rounded.Article, Color(0xFFF59E0B), Color(0xFFFEF3C7))
                            "mp3", "wav", "aac", "flac", "ogg", "m4a" -> Triple(Icons.Rounded.VolumeUp, Color(0xFF8B5CF6), Color(0xFFEDE9FE))
                            "mp4", "mkv", "avi", "mov", "webm" -> Triple(Icons.Rounded.PlayArrow, Color(0xFFEC4899), Color(0xFFFCE7F3))
                            "zip", "rar", "tar", "gz", "7z" -> Triple(Icons.Rounded.Folder, Color(0xFF6B7280), Color(0xFFE5E7EB))
                            else -> Triple(Icons.Rounded.AttachFile, Color(0xFF4F46E5), Color(0xFFE0E7FF))
                        }
                    }

                    ScholarCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (attachment.filePath.endsWith(".pdf", ignoreCase = true)) {
                                        val encodedPath = Uri.encode(attachment.filePath)
                                        val encodedName = Uri.encode(attachment.name)
                                        navController.navigate("pdf_viewer?filePath=$encodedPath&fileName=$encodedName")
                                    } else {
                                        try {
                                            val file = File(attachment.filePath)
                                            if (!file.exists()) {
                                                Toast.makeText(context, "File does not exist or was deleted", Toast.LENGTH_SHORT).show()
                                            } else {
                                                val authority = "${context.packageName}.provider"
                                                val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
                                                val mimeType = when (extension) {
                                                    "txt" -> "text/plain"
                                                    "md" -> "text/markdown"
                                                    "png" -> "image/png"
                                                    "jpg", "jpeg" -> "image/jpeg"
                                                    "gif" -> "image/gif"
                                                    "webp" -> "image/webp"
                                                    "bmp" -> "image/bmp"
                                                    "mp3" -> "audio/mpeg"
                                                    "wav" -> "audio/wav"
                                                    "ogg" -> "audio/ogg"
                                                    "m4a" -> "audio/mp4"
                                                    "mp4" -> "video/mp4"
                                                    "mkv" -> "video/x-matroska"
                                                    "doc", "docx" -> "application/msword"
                                                    "xls", "xlsx" -> "application/vnd.ms-excel"
                                                    "ppt", "pptx" -> "application/vnd.ms-powerpoint"
                                                    "zip" -> "application/zip"
                                                    "rar" -> "application/x-rar-compressed"
                                                    else -> "*/*"
                                                }
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                    setDataAndType(uri, mimeType)
                                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(android.content.Intent.createChooser(intent, "Open file with"))
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Could not open file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(visualMeta.third, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = visualMeta.first,
                                    contentDescription = null,
                                    tint = visualMeta.second,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = attachment.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val sizeFormatted = remember(attachment.sizeBytes) {
                                    if (attachment.sizeBytes < 1024) "${attachment.sizeBytes} B"
                                    else if (attachment.sizeBytes < 1024 * 1024) "${attachment.sizeBytes / 1024} KB"
                                    else String.format(Locale.getDefault(), "%.2f MB", attachment.sizeBytes.toFloat() / (1024 * 1024))
                                }
                                Text(
                                    text = "$sizeFormatted • ${attachment.fileType.uppercase()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteAttachment(attachment) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = "Delete Attachment",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS ---

    // Edit Subject Dialog
    if (showEditSubjectDialog) {
        EditSubjectDialog(
            subject = subject,
            viewModel = viewModel,
            onDismiss = { showEditSubjectDialog = false }
        )
    }

    // Delete Subject Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Subject?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ${subject.name}? All linked topics, notes, tasks, and assignments will stay, but they won't be mapped to this subject anymore.") },
            confirmButton = {
                BouncyTextButton(
                    onClick = {
                        viewModel.deleteSubject(subject)
                        showDeleteConfirmDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Link Course Dialog
    if (showLinkCourseDialog) {
        AlertDialog(
            onDismissRequest = { showLinkCourseDialog = false },
            title = { Text("Link Courses", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Select which courses belong to this subject:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(allCourses, key = { it.id }) { course ->
                            val currentIds = course.subjectIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            val isLinked = course.subjectId == subjectId || currentIds.contains(subjectId.toString())
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        val updatedIds = if (isLinked) {
                                            currentIds.filter { it != subjectId.toString() }
                                        } else {
                                            (currentIds + subjectId.toString()).distinct()
                                        }
                                        val updatedIdsStr = updatedIds.joinToString(",")
                                        val firstId = updatedIds.firstOrNull()?.toIntOrNull()
                                        viewModel.updateCourse(course.copy(subjectIds = updatedIdsStr, subjectId = firstId))
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isLinked,
                                    onCheckedChange = {
                                        val updatedIds = if (isLinked) {
                                            currentIds.filter { it != subjectId.toString() }
                                        } else {
                                            (currentIds + subjectId.toString()).distinct()
                                        }
                                        val updatedIdsStr = updatedIds.joinToString(",")
                                        val firstId = updatedIds.firstOrNull()?.toIntOrNull()
                                        viewModel.updateCourse(course.copy(subjectIds = updatedIdsStr, subjectId = firstId))
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(course.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                    if (course.code.isNotBlank()) {
                                        Text(course.code, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                BouncyTextButton(onClick = { showLinkCourseDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Add / Edit Topic Dialog
    val currentTopic = topicToEdit
    if (showAddTopicDialog || currentTopic != null) {
        var topicTitle by remember(currentTopic) { mutableStateOf(currentTopic?.title ?: "") }
        var topicTags by remember(currentTopic) { mutableStateOf(currentTopic?.tags ?: "") }

        var chapterDropdownExpanded by remember { mutableStateOf(false) }
        val currentSelectedChapterId = remember(currentTopic, selectedChapterForNewTopic) {
            currentTopic?.chapterId ?: selectedChapterForNewTopic
        }
        var chosenChapterId by remember(currentSelectedChapterId) { mutableStateOf(currentSelectedChapterId) }
        val chosenChapterName = remember(chosenChapterId, subjectChapters) {
            if (chosenChapterId == null) "No Chapter (General Topic)"
            else subjectChapters.find { it.id == chosenChapterId }?.name ?: "No Chapter (General Topic)"
        }

        AlertDialog(
            onDismissRequest = {
                showAddTopicDialog = false
                topicToEdit = null
            },
            title = { Text(if (currentTopic == null) "Add Topic" else "Edit Topic", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = topicTitle,
                        onValueChange = { topicTitle = it },
                        label = { Text("Topic Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = topicTags,
                        onValueChange = { topicTags = it },
                        label = { Text("Tags (comma separated, optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Assign to Chapter", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { chapterDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(chosenChapterName, color = MaterialTheme.colorScheme.onSurface)
                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = chapterDropdownExpanded,
                            onDismissRequest = { chapterDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("No Chapter (General Topic)") },
                                onClick = {
                                    chosenChapterId = null
                                    chapterDropdownExpanded = false
                                }
                            )
                            subjectChapters.forEach { ch ->
                                DropdownMenuItem(
                                    text = { Text(ch.name) },
                                    onClick = {
                                        chosenChapterId = ch.id
                                        chapterDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                BouncyTextButton(onClick = {
                    if (topicTitle.isNotBlank()) {
                        if (currentTopic == null) {
                            viewModel.addTopic(
                                subjectId = subjectId,
                                title = topicTitle,
                                tags = topicTags,
                                chapterId = chosenChapterId
                            )
                        } else {
                            viewModel.updateTopic(
                                currentTopic.copy(
                                    title = topicTitle,
                                    tags = topicTags,
                                    chapterId = chosenChapterId
                                )
                            )
                        }
                    }
                    showAddTopicDialog = false
                    topicToEdit = null
                }) { Text("Save") }
            },
            dismissButton = {
                BouncyTextButton(onClick = {
                    showAddTopicDialog = false
                    topicToEdit = null
                }) { Text("Cancel") }
            }
        )
    }

    // Add / Edit Chapter Dialog
    val currentChapter = chapterToEdit
    if (showAddChapterDialog || currentChapter != null) {
        var chapterName by remember(currentChapter) { mutableStateOf(currentChapter?.name ?: "") }
        var chapterDesc by remember(currentChapter) { mutableStateOf(currentChapter?.description ?: "") }
        var chapterTags by remember(currentChapter) { mutableStateOf(currentChapter?.tags ?: "") }

        AlertDialog(
            onDismissRequest = {
                showAddChapterDialog = false
                chapterToEdit = null
            },
            title = { Text(if (currentChapter == null) "Add Chapter" else "Edit Chapter", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = chapterName,
                        onValueChange = { chapterName = it },
                        label = { Text("Chapter Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = chapterDesc,
                        onValueChange = { chapterDesc = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = chapterTags,
                        onValueChange = { chapterTags = it },
                        label = { Text("Tags (comma separated, optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                BouncyTextButton(onClick = {
                    if (chapterName.isNotBlank()) {
                        if (currentChapter == null) {
                            viewModel.addChapter(
                                name = chapterName,
                                subjectId = subjectId,
                                description = chapterDesc,
                                tags = chapterTags
                            )
                        } else {
                            viewModel.updateChapter(
                                currentChapter.copy(
                                    name = chapterName,
                                    description = chapterDesc,
                                    tags = chapterTags
                                )
                            )
                        }
                    }
                    showAddChapterDialog = false
                    chapterToEdit = null
                }) { Text("Save") }
            },
            dismissButton = {
                BouncyTextButton(onClick = {
                    showAddChapterDialog = false
                    chapterToEdit = null
                }) { Text("Cancel") }
            }
        )
    }

    // Add / Edit Note Dialog
    val currentNote = noteToEdit
    if (showAddNoteDialog || currentNote != null) {
        var noteContent by remember(currentNote) { mutableStateOf(currentNote?.content ?: "") }
        AlertDialog(
            onDismissRequest = {
                showAddNoteDialog = false
                noteToEdit = null
            },
            title = { Text(if (currentNote == null) "Add Note" else "Edit Note", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    label = { Text("Note content") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 4
                )
            },
            confirmButton = {
                BouncyTextButton(onClick = {
                    if (noteContent.isNotBlank()) {
                        if (currentNote == null) {
                            viewModel.addNote(
                                content = noteContent,
                                subjectId = subjectId
                            )
                        } else {
                            viewModel.updateNote(
                                currentNote.copy(
                                    content = noteContent
                                )
                            )
                        }
                    }
                    showAddNoteDialog = false
                    noteToEdit = null
                }) { Text("Save") }
            },
            dismissButton = {
                BouncyTextButton(onClick = {
                    showAddNoteDialog = false
                    noteToEdit = null
                }) { Text("Cancel") }
            }
        )
    }

    // Add / Edit Task Dialog
    val currentTask = taskToEdit
    if (showAddTaskDialog || currentTask != null) {
        var taskTitle by remember(currentTask) { mutableStateOf(currentTask?.title ?: "") }
        var taskDescription by remember(currentTask) { mutableStateOf(currentTask?.description ?: "") }
        var taskDueDate by remember(currentTask) { mutableStateOf(currentTask?.dueDateMillis) }

        AlertDialog(
            onDismissRequest = {
                showAddTaskDialog = false
                taskToEdit = null
            },
            title = { Text(if (currentTask == null) "Add Task" else "Edit Task", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = taskDescription,
                        onValueChange = { taskDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val dateLabel = if (taskDueDate != null) {
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(taskDueDate!!)
                    } else {
                        "No Due Date"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                val cal = Calendar.getInstance()
                                if (taskDueDate != null) cal.timeInMillis = taskDueDate!!
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val selectedCal = Calendar.getInstance()
                                        selectedCal.set(year, month, dayOfMonth, 23, 59, 59)
                                        taskDueDate = selectedCal.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Due Date", fontWeight = FontWeight.Bold)
                        }
                        Text(dateLabel, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                BouncyTextButton(onClick = {
                    if (taskTitle.isNotBlank()) {
                        if (currentTask == null) {
                            viewModel.addTask(
                                Task(
                                    title = taskTitle,
                                    description = taskDescription,
                                    dueDateMillis = taskDueDate,
                                    subjectId = subjectId,
                                    isCompleted = false
                                )
                            )
                        } else {
                            viewModel.updateTask(
                                currentTask.copy(
                                    title = taskTitle,
                                    description = taskDescription,
                                    dueDateMillis = taskDueDate
                                )
                            )
                        }
                    }
                    showAddTaskDialog = false
                    taskToEdit = null
                }) { Text("Save") }
            },
            dismissButton = {
                BouncyTextButton(onClick = {
                    showAddTaskDialog = false
                    taskToEdit = null
                }) { Text("Cancel") }
            }
        )
    }

    // Add / Edit Assignment Dialog
    val currentAssignment = assignmentToEdit
    if (showAddAssignmentDialog || currentAssignment != null) {
        var assignmentTitle by remember(currentAssignment) { mutableStateOf(currentAssignment?.title ?: "") }
        var assignmentDesc by remember(currentAssignment) { mutableStateOf(currentAssignment?.description ?: "") }
        var assignmentCategory by remember(currentAssignment) { mutableStateOf(currentAssignment?.category ?: "Homework") }
        var assignmentDueDate by remember(currentAssignment) { mutableStateOf(currentAssignment?.dueDateMillis ?: Calendar.getInstance().timeInMillis) }
        var assignmentTags by remember(currentAssignment) { mutableStateOf(currentAssignment?.tags ?: "") }

        val categories = listOf("Homework", "Quiz", "Project", "Exam", "Lab")

        AlertDialog(
            onDismissRequest = {
                showAddAssignmentDialog = false
                assignmentToEdit = null
            },
            title = { Text(if (currentAssignment == null) "Add Assignment" else "Edit Assignment", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = assignmentTitle,
                        onValueChange = { assignmentTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = assignmentDesc,
                        onValueChange = { assignmentDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = assignmentTags,
                        onValueChange = { assignmentTags = it },
                        label = { Text("Tags (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Category", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = assignmentCategory == cat
                            val color = when (cat) {
                                "Homework" -> Color(0xFF3197D6)
                                "Quiz" -> Color(0xFFE91E63)
                                "Project" -> Color(0xFF4CAF50)
                                "Exam" -> Color(0xFFFF5722)
                                else -> Color(0xFF9C27B0)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) color else color.copy(alpha = 0.1f))
                                    .clickable { assignmentCategory = cat }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) Color.White else color,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    val dateLabel = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(assignmentDueDate)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                val cal = Calendar.getInstance()
                                cal.timeInMillis = assignmentDueDate
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val selectedCal = Calendar.getInstance()
                                        selectedCal.set(year, month, dayOfMonth, 23, 59, 59)
                                        assignmentDueDate = selectedCal.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text("Due Date", fontWeight = FontWeight.Bold)
                        }
                        Text(dateLabel, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                BouncyTextButton(onClick = {
                    if (assignmentTitle.isNotBlank()) {
                        val categoryColor = when (assignmentCategory) {
                            "Homework" -> "#3197D6"
                            "Quiz" -> "#E91E63"
                            "Project" -> "#4CAF50"
                            "Exam" -> "#FF5722"
                            else -> "#9C27B0"
                        }
                        val courseId = linkedCourses.firstOrNull()?.id ?: 0

                        if (currentAssignment == null) {
                            viewModel.addAssignment(
                                courseId = courseId,
                                title = assignmentTitle,
                                desc = assignmentDesc,
                                dueDate = assignmentDueDate,
                                category = assignmentCategory,
                                categoryColor = categoryColor,
                                tags = assignmentTags,
                                subjectId = subjectId
                            )
                        } else {
                            viewModel.updateAssignmentDetails(
                                currentAssignment.copy(
                                    title = assignmentTitle,
                                    description = assignmentDesc,
                                    category = assignmentCategory,
                                    categoryColor = categoryColor,
                                    dueDateMillis = assignmentDueDate,
                                    tags = assignmentTags,
                                    courseId = if (currentAssignment.courseId > 0) currentAssignment.courseId else courseId
                                )
                            )
                        }
                    }
                    showAddAssignmentDialog = false
                    assignmentToEdit = null
                }) { Text("Save") }
            },
            dismissButton = {
                BouncyTextButton(onClick = {
                    showAddAssignmentDialog = false
                    assignmentToEdit = null
                }) { Text("Cancel") }
            }
        )
    }

    // Add Attachment (AI Study Guide PDF) Dialog (Polished Export Dialog)
    if (showAddAttachmentDialog) {
        AlertDialog(
            onDismissRequest = { showAddAttachmentDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Generate Study Guide PDF", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Review or customize the automatic study booklet content. This will compile a PDF document linked to this subject.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = attachmentTitle,
                        onValueChange = { attachmentTitle = it },
                        label = { Text("Document Title") },
                        placeholder = { Text("e.g. Syllabus Summary - ${subject.name}") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = attachmentContent,
                        onValueChange = { attachmentContent = it },
                        label = { Text("Study Guide / Syllabus Material Content") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 5
                    )
                }
            },
            confirmButton = {
                BouncyTextButton(onClick = {
                    if (attachmentTitle.isNotBlank() && attachmentContent.isNotBlank()) {
                        generateStudyGuidePdf(attachmentTitle, attachmentContent)
                        showAddAttachmentDialog = false
                    }
                }) {
                    Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Generate PDF", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showAddAttachmentDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@ValueScore(
    score = 40,
    importance = Importance.MEDIUM,
    description = "Section header with title and action button",
    category = "Study"
)
@Composable
fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        }
        BouncyIconButton(
            onClick = onAddClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add Item", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@ValueScore(
    score = 35,
    importance = Importance.LOW,
    description = "Summary metric counter element for subject overview",
    category = "Study"
)
@Composable
fun StatItem(label: String, count: Int, detail: String? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (detail != null) {
            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@ValueScore(
    score = 30,
    importance = Importance.LOW,
    description = "Placeholder empty card for empty sections with action callout",
    category = "Study"
)
@Composable
fun EmptySectionCard(
    text: String,
    buttonText: String,
    onClick: () -> Unit
) {
    ScholarCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            BouncyTextButton(onClick = onClick) {
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
