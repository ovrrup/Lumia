package lumia.tracker.ui.screens.study

import lumia.tracker.ui.screens.study.dialogs.*

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import lumia.tracker.ui.screens.study.dialogs.EditCourseDialog
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.ui.util.getTagColors
import lumia.tracker.viewmodel.ScholarViewModel
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * CourseDetailTab - Represents distinct decluttered sections of the Course workspace.
 */
enum class CourseDetailTab(val title: String, val icon: ImageVector) {
    CURRICULUM("Topics", Icons.Rounded.Checklist),
    ATTENDANCE("Attendance", Icons.Rounded.EventAvailable),
    ASSIGNMENTS("Assignments", Icons.AutoMirrored.Rounded.LibraryBooks),
    NOTES("Notes", Icons.Rounded.Notes),
    GUIDES("Study Guides", Icons.Rounded.AttachFile)
}

@ValueScore(
    score = 96,
    importance = Importance.CRITICAL,
    description = "Streamlined Course Detail workspace featuring a responsive tab switcher, attendance analytics gauge, curriculum checklists, assignments, notes, and study guide generator",
    category = "Study"
)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CourseDetailScreen(
    navController: NavController,
    viewModel: ScholarViewModel,
    courseId: Int
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val course = courses.find { it.id == courseId }
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val systemAutoLinkByName by viewModel.systemAutoLinkByName.collectAsStateWithLifecycle()
    val enableSynergy by viewModel.systemEnableSynergy.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(CourseDetailTab.CURRICULUM) }

    val linkedSubjects = remember(course, subjects, systemAutoLinkByName) {
        if (course != null) {
            val list = mutableListOf<Subject>()
            if (course.subjectIds.isNotBlank()) {
                val ids = course.subjectIds.split(",").mapNotNull { it.trim().toIntOrNull() }
                list.addAll(subjects.filter { ids.contains(it.id) })
            }
            if (course.subjectId != null && list.none { it.id == course.subjectId }) {
                subjects.find { it.id == course.subjectId }?.let { list.add(it) }
            }
            if (list.isEmpty() && systemAutoLinkByName) {
                val autoSub = subjects.find { it.name.trim().lowercase() == course.name.trim().lowercase() }
                    ?: subjects.find {
                        val subName = it.name.trim().lowercase()
                        val crsName = course.name.trim().lowercase()
                        subName.isNotEmpty() && crsName.isNotEmpty() && (subName.contains(crsName) || crsName.contains(subName))
                    }
                if (autoSub != null) {
                    list.add(autoSub)
                }
            }
            list.distinctBy { it.id }
        } else {
            emptyList()
        }
    }

    val linkedSubject = remember(linkedSubjects) { linkedSubjects.firstOrNull() }

    val allTasks by viewModel.tasks.collectAsStateWithLifecycle()
    val courseTasks = remember(allTasks, course) {
        if (course != null) allTasks.filter { it.courseId == course.id } else emptyList()
    }

    val allTopicsGlobal by viewModel.allTopics.collectAsStateWithLifecycle(emptyList())
    val topics = remember(allTopicsGlobal, linkedSubjects) {
        val subjectIds = linkedSubjects.map { it.id }
        allTopicsGlobal.filter { subjectIds.contains(it.subjectId) }
    }

    val assignments by viewModel.getAssignmentsForCourse(courseId).collectAsStateWithLifecycle(emptyList())
    var showAddAssignmentDialog by remember { mutableStateOf(false) }
    var localAssignments by remember(assignments) { mutableStateOf(assignments) }
    var localTasks by remember(courseTasks) { mutableStateOf(courseTasks) }

    val attachments by viewModel.getAttachmentsForCourse(courseId).collectAsStateWithLifecycle(emptyList())
    var showAddAttachmentDialog by remember { mutableStateOf(false) }
    var attachmentTitle by remember { mutableStateOf("") }
    var attachmentContent by remember { mutableStateOf("") }
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
                val localFile = File(context.filesDir, "attachment_${System.currentTimeMillis()}.$extension")
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
                    courseId = courseId,
                    subjectId = null
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

            val headerColor = try {
                android.graphics.Color.parseColor(course?.colorHex ?: "#4F46E5")
            } catch (e: Exception) {
                android.graphics.Color.parseColor("#4F46E5")
            }

            paint.color = headerColor
            canvas.drawRect(0f, 0f, 595f, 130f, paint)

            paint.color = android.graphics.Color.WHITE
            paint.textSize = 24f
            paint.isFakeBoldText = true
            canvas.drawText("LUMIA STUDY GUIDE", 40f, 55f, paint)

            paint.textSize = 14f
            paint.isFakeBoldText = false
            paint.color = android.graphics.Color.parseColor("#F1F5F9")
            canvas.drawText("Course: ${course?.name ?: ""} · Code: ${course?.code ?: "N/A"}", 40f, 85f, paint)
            canvas.drawText("Instructor: ${course?.instructor?.ifBlank { "Unassigned" } ?: "Unassigned"}", 40f, 108f, paint)

            paint.color = android.graphics.Color.BLACK
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText(titleStr, 40f, 175f, paint)

            paint.textSize = 12f
            paint.isFakeBoldText = false
            paint.color = android.graphics.Color.parseColor("#1E293B")

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

            val localFile = File(context.filesDir, "study_guide_${System.currentTimeMillis()}.pdf")
            pdfDoc.writeTo(localFile.outputStream())
            pdfDoc.close()

            viewModel.addAttachment(
                name = if (titleStr.endsWith(".pdf", ignoreCase = true)) titleStr else "$titleStr.pdf",
                filePath = localFile.absolutePath,
                fileType = "PDF Document",
                sizeBytes = localFile.length(),
                courseId = courseId,
                subjectId = null
            )
            Toast.makeText(context, "Study guide PDF generated successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Generation failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    val listState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(
        listState = listState,
        onMove = { from, to ->
            if (from.key is String && to.key is String) {
                val fromStr = from.key as String
                val toStr = to.key as String
                if (fromStr.startsWith("assignment_") && toStr.startsWith("assignment_")) {
                    localAssignments = localAssignments.toMutableList().apply {
                        val fromId = fromStr.removePrefix("assignment_").toInt()
                        val toId = toStr.removePrefix("assignment_").toInt()
                        val fromIndex = indexOfFirst { it.id == fromId }
                        val toIndex = indexOfFirst { it.id == toId }
                        if (fromIndex != -1 && toIndex != -1) {
                            add(toIndex, removeAt(fromIndex))
                        }
                    }
                } else if (fromStr.startsWith("task_") && toStr.startsWith("task_")) {
                    localTasks = localTasks.toMutableList().apply {
                        val fromId = fromStr.removePrefix("task_").toInt()
                        val toId = toStr.removePrefix("task_").toInt()
                        val fromIndex = indexOfFirst { it.id == fromId }
                        val toIndex = indexOfFirst { it.id == toId }
                        if (fromIndex != -1 && toIndex != -1) {
                            add(toIndex, removeAt(fromIndex))
                        }
                    }
                }
            }
        },
        canDragOver = { draggedOver, dragged ->
            val typeOver = (draggedOver.key as? String)?.substringBefore("_")
            val typeVal = (dragged.key as? String)?.substringBefore("_")
            typeOver == typeVal && typeOver != null
        }
    )

    LaunchedEffect(reorderableState.draggingItemKey) {
        if (reorderableState.draggingItemKey == null) {
            if (localAssignments != assignments) {
                val updatedAssignments = localAssignments.mapIndexed { index, assignment -> assignment.copy(orderIndex = index) }
                viewModel.updateAssignmentsOrder(updatedAssignments)
            }
            if (localTasks != courseTasks) {
                val updatedTasks = localTasks.mapIndexed { index, task -> task.copy(orderIndex = index) }
                viewModel.updateTasksOrder(updatedTasks)
            }
        }
    }

    var showLinkSubjectDialog by remember { mutableStateOf(false) }
    var showEditCourseDialog by remember { mutableStateOf(false) }
    var showDeleteCourseDialog by remember { mutableStateOf(false) }
    var showTopMenu by remember { mutableStateOf(false) }
    var assignmentToEdit by remember { mutableStateOf<PracticeAssignment?>(null) }

    val allNotes by viewModel.notes.collectAsStateWithLifecycle()
    val courseNotes = remember(allNotes, course, linkedSubjects, subjects, courses, systemAutoLinkByName) {
        if (course != null) {
            val linkedSubjectIds = linkedSubjects.map { it.id }
            val linkedCourseIds = if (linkedSubjectIds.isNotEmpty()) {
                courses.filter { c ->
                    linkedSubjectIds.contains(c.subjectId) ||
                    linkedSubjectIds.any { sid -> c.subjectIds.split(",").mapNotNull { it.trim().toIntOrNull() }.contains(sid) } ||
                    (systemAutoLinkByName && linkedSubjects.any { s -> c.name.trim().lowercase() == s.name.trim().lowercase() })
                }.map { it.id }
            } else {
                emptyList()
            }

            allNotes.filter { note ->
                note.courseId == course.id ||
                (note.subjectId != null && linkedSubjectIds.contains(note.subjectId)) ||
                (note.courseId != null && linkedCourseIds.contains(note.courseId))
            }
        } else {
            emptyList()
        }
    }

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    var noteText by remember { mutableStateOf("") }
    var noteCustomTag by remember { mutableStateOf("Core") }

    val allTestRecordsGlobal by viewModel.allTestRecords.collectAsStateWithLifecycle(emptyList())
    val testRecords = remember(allTestRecordsGlobal, course) {
        if (course != null) allTestRecordsGlobal.filter { it.courseId == course.id } else emptyList()
    }

    val attendanceRecords by viewModel.getAttendanceForCourse(courseId).collectAsStateWithLifecycle()

    if (course == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val courseColor = remember(course.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(course.colorHex))
        } catch (e: Exception) {
            Color(0xFF3197D6)
        }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val tabCounts = remember(topics, attendanceRecords, localAssignments, courseNotes, attachments) {
        mapOf(
            CourseDetailTab.CURRICULUM to topics.size,
            CourseDetailTab.ATTENDANCE to attendanceRecords.size,
            CourseDetailTab.ASSIGNMENTS to localAssignments.size,
            CourseDetailTab.NOTES to courseNotes.size,
            CourseDetailTab.GUIDES to attachments.size
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = course.name,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (course.code.isNotBlank()) {
                                Surface(
                                    color = courseColor.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = course.code,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = courseColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        if (course.instructor.isNotBlank()) {
                            Text(
                                text = course.instructor,
                                style = MaterialTheme.typography.bodyMedium,
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
                        BouncyIconButton(onClick = { showTopMenu = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "Course Actions")
                        }
                        DropdownMenu(
                            expanded = showTopMenu,
                            onDismissRequest = { showTopMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Course") },
                                onClick = {
                                    showTopMenu = false
                                    showEditCourseDialog = true
                                },
                                leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Link Subjects") },
                                onClick = {
                                    showTopMenu = false
                                    showLinkSubjectDialog = true
                                },
                                leadingIcon = { Icon(Icons.Rounded.Link, contentDescription = null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Delete Course", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showTopMenu = false
                                    showDeleteCourseDialog = true
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
            state = listState,
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .reorderable(reorderableState)
        ) {
            // 1. MODERNIZED GLASSMORPHIC COURSE HERO WITH AMBIENT AURA & CAPSULES
            item {
                ScholarCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    glassmorphic = true,
                    containerColor = if (isDark) {
                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.60f)
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                    },
                    border = ScholarCardDefaults.glassBorder(isDark, accentColor = courseColor)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Radiant Ambient Glowing Aura in the background
                        Box(
                            modifier = Modifier
                                .size(190.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 50.dp, y = (-35).dp)
                                .blur(48.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                                .background(
                                    courseColor.copy(alpha = if (isDark) 0.28f else 0.20f),
                                    CircleShape
                                )
                        )
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .align(Alignment.BottomStart)
                                .offset(x = (-30).dp, y = 30.dp)
                                .blur(42.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                                .background(
                                    courseColor.copy(alpha = if (isDark) 0.16f else 0.10f),
                                    CircleShape
                                )
                        )

                        Column(modifier = Modifier.padding(20.dp)) {
                            // Top Row: Glowing Icon Avatar + Title + Code Capsule Pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(
                                                    courseColor.copy(alpha = 0.25f),
                                                    courseColor.copy(alpha = 0.08f)
                                                )
                                            ),
                                            CircleShape
                                        )
                                        .border(1.dp, courseColor.copy(alpha = 0.35f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.School,
                                        contentDescription = null,
                                        tint = courseColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = course.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (course.code.isNotBlank()) {
                                            GlassCapsule(
                                                containerColor = courseColor.copy(alpha = 0.15f),
                                                border = BorderStroke(0.8.dp, courseColor.copy(alpha = 0.35f))
                                            ) {
                                                Text(
                                                    text = course.code,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Black,
                                                    color = courseColor,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    if (course.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = course.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            // Capsule Status Pills
                            if (course.instructor.isNotBlank() || course.schedule.isNotBlank() || course.tags.isNotBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (course.instructor.isNotBlank()) {
                                        DetailMetaBadge(
                                            icon = Icons.Rounded.Person,
                                            text = course.instructor,
                                            tint = courseColor
                                        )
                                    }
                                    if (course.schedule.isNotBlank()) {
                                        DetailMetaBadge(
                                            icon = Icons.Rounded.Schedule,
                                            text = course.schedule,
                                            tint = courseColor
                                        )
                                    }
                                    if (course.tags.isNotBlank()) {
                                        course.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { tag ->
                                            val tagColors = getTagColors(tag)
                                            GlassCapsule(
                                                containerColor = tagColors.first.copy(alpha = 0.85f),
                                                border = BorderStroke(0.8.dp, tagColors.second.copy(alpha = 0.3f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Box(modifier = Modifier.size(5.dp).background(tagColors.second, CircleShape))
                                                    Text(
                                                        tag,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = tagColors.second,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Clean Icon-Number Pairings Metric Bar
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isDark) MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        0.8.dp,
                                        if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.35f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Topics icon-number pairing
                                val completedTopicCount = topics.count { it.isCompleted }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(Icons.Rounded.Checklist, contentDescription = null, tint = courseColor, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "$completedTopicCount/${topics.size}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Topics",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }

                                // Attendance rate pairing
                                val cancelledRecs = attendanceRecords.count { it.status.equals("Cancelled", ignoreCase = true) || it.status.equals("Holiday", ignoreCase = true) }
                                val effectiveAtt = attendanceRecords.size - cancelledRecs
                                val attendedCount = attendanceRecords.count { it.status.equals("Present", ignoreCase = true) || it.status.equals("Late", ignoreCase = true) }
                                val heroAttPct = if (effectiveAtt > 0) ((attendedCount.toFloat() / effectiveAtt) * 100).roundToInt() else 100
                                val attColor = when {
                                    effectiveAtt == 0 -> courseColor
                                    heroAttPct >= 75 -> Color(0xFF10B981)
                                    heroAttPct >= 50 -> Color(0xFFF59E0B)
                                    else -> Color(0xFFEF4444)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(Icons.Rounded.EventAvailable, contentDescription = null, tint = attColor, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "$heroAttPct%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = attColor
                                    )
                                    Text(
                                        text = "Att.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }

                                // Assignments pairing
                                val pendingAssignments = localAssignments.count { !it.isCompleted }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.LibraryBooks, contentDescription = null, tint = courseColor, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "$pendingAssignments",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Pending",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Interconnected Subjects & Synergy
                            if (linkedSubjects.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Linked Subjects",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                    GlassCapsule(
                                        onClick = { showLinkSubjectDialog = true },
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    ) {
                                        Text(
                                            "Manage",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = courseColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(linkedSubjects) { subj ->
                                        GlassCapsule(
                                            onClick = {
                                                navController.navigate("subjectDetail/${subj.id}")
                                            },
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            border = BorderStroke(0.8.dp, courseColor.copy(alpha = 0.25f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Icon(Icons.Rounded.AutoStories, contentDescription = null, tint = courseColor, modifier = Modifier.size(12.dp))
                                                Text(
                                                    text = subj.name,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Icon(
                                                    Icons.Rounded.ChevronRight,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (enableSynergy && topics.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    val completedTopics = topics.count { it.isCompleted }
                                    val totalTopics = topics.size
                                    val topicPercent = if (totalTopics > 0) completedTopics.toFloat() / totalTopics else 0f
                                    val synergyScore = (topicPercent * 100).toInt()

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                                CircleShape
                                            )
                                            .border(
                                                0.8.dp,
                                                courseColor.copy(alpha = 0.20f),
                                                CircleShape
                                            )
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        LinearProgressIndicator(
                                            progress = { topicPercent },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(6.dp)
                                                .clip(CircleShape),
                                            color = courseColor,
                                            trackColor = courseColor.copy(alpha = 0.15f),
                                            strokeCap = StrokeCap.Round
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        GlassCapsule(
                                            containerColor = courseColor.copy(alpha = 0.14f),
                                            border = BorderStroke(0.8.dp, courseColor.copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(Icons.Rounded.Bolt, contentDescription = null, tint = courseColor, modifier = Modifier.size(11.dp))
                                                Text(
                                                    text = "Synergy $synergyScore%",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = courseColor,
                                                    fontSize = 10.sp
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

            // 2. SLEEK TAB CAPSULE / SEGMENTED BAR
            item {
                CourseDetailTabCapsuleBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    counts = tabCounts,
                    courseColor = courseColor
                )
            }

            // 3. TAB CONTENT SECTIONS
            when (selectedTab) {
                CourseDetailTab.CURRICULUM -> {
                    // Curriculum Progress Counter
                    if (topics.isNotEmpty()) {
                        item {
                            val completedTopics = topics.count { it.isCompleted }
                            val totalTopics = topics.size
                            val progress = if (totalTopics > 0) completedTopics.toFloat() / totalTopics else 0f
                            val animatedChecklistProgress by animateFloatAsState(targetValue = progress, label = "curriculum_progress")

                            ScholarCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                glassmorphic = true,
                                border = ScholarCardDefaults.glassBorder(
                                    isDark,
                                    accentColor = if (completedTopics == totalTopics) Color(0xFF10B981) else courseColor
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.Checklist,
                                                contentDescription = null,
                                                tint = if (completedTopics == totalTopics) Color(0xFF10B981) else courseColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Topics Progress",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        GlassCapsule(
                                            containerColor = (if (completedTopics == totalTopics) Color(0xFF10B981) else courseColor).copy(alpha = 0.12f),
                                            border = BorderStroke(0.8.dp, (if (completedTopics == totalTopics) Color(0xFF10B981) else courseColor).copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "$completedTopics/$totalTopics",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (completedTopics == totalTopics) Color(0xFF10B981) else courseColor
                                                )
                                                Text(
                                                    text = "• ${(progress * 100).toInt()}%",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    LinearProgressIndicator(
                                        progress = { animatedChecklistProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(CircleShape),
                                        color = if (completedTopics == totalTopics) Color(0xFF10B981) else courseColor,
                                        trackColor = courseColor.copy(alpha = 0.12f),
                                        strokeCap = StrokeCap.Round
                                    )
                                }
                            }
                        }

                        items(topics, key = { "top_${it.id}" }) { topic ->
                            ScholarCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                glassmorphic = true,
                                containerColor = if (topic.isCompleted) {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f)
                                } else {
                                    if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f)
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                                },
                                border = ScholarCardDefaults.glassBorder(
                                    isDark = isDark,
                                    accentColor = if (topic.isCompleted) Color(0xFF10B981).copy(alpha = 0.35f) else null
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Sleek circular completion toggle
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (topic.isCompleted) Color(0xFF10B981).copy(alpha = 0.15f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                                CircleShape
                                            )
                                            .border(
                                                1.dp,
                                                if (topic.isCompleted) Color(0xFF10B981)
                                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                                CircleShape
                                            )
                                            .bouncyClick { viewModel.toggleTopicCompleted(topic) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (topic.isCompleted) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = "Completed",
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = topic.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            textDecoration = if (topic.isCompleted) TextDecoration.LineThrough else null,
                                            color = if (topic.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (topic.tags.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                topic.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.take(3).forEach { tag ->
                                                    val colors = getTagColors(tag)
                                                    GlassCapsule(
                                                        containerColor = colors.first.copy(alpha = 0.85f),
                                                        border = BorderStroke(0.8.dp, colors.second.copy(alpha = 0.25f))
                                                    ) {
                                                        Text(
                                                            tag,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = colors.second,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Smooth capsule action: Focus Timer
                                    GlassCapsule(
                                        onClick = {
                                            navController.navigate("pomodoro?courseId=${courseId}&topicId=${topic.id}")
                                        },
                                        containerColor = courseColor.copy(alpha = 0.12f),
                                        border = BorderStroke(0.8.dp, courseColor.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.Timer,
                                                contentDescription = "Study Pomodoro",
                                                tint = courseColor,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                "Focus",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = courseColor,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            ScholarCard(
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                shape = RoundedCornerShape(20.dp),
                                glassmorphic = true
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "No topics linked yet",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    GlassCapsule(
                                        onClick = { showLinkSubjectDialog = true },
                                        containerColor = courseColor.copy(alpha = 0.14f),
                                        border = BorderStroke(0.8.dp, courseColor.copy(alpha = 0.35f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Rounded.Add, contentDescription = null, tint = courseColor, modifier = Modifier.size(14.dp))
                                            Text("Link Study Subject", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = courseColor)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Related Course Tasks
                    if (localTasks.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Course Tasks",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                GlassCapsule(
                                    containerColor = courseColor.copy(alpha = 0.12f),
                                    border = BorderStroke(0.8.dp, courseColor.copy(alpha = 0.25f))
                                ) {
                                    Text(
                                        text = "${localTasks.count { !it.isCompleted }}/${localTasks.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = courseColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                        items(localTasks, key = { "task_${it.id}" }) { task ->
                            ReorderableItem(reorderableState, key = "task_${task.id}") { isDragging ->
                                ScholarCard(
                                    modifier = Modifier
                                        .detectReorderAfterLongPress(reorderableState)
                                        .animateItem()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    glassmorphic = true,
                                    containerColor = if (task.isCompleted) {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f)
                                    } else {
                                        if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f)
                                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                                    },
                                    border = ScholarCardDefaults.glassBorder(
                                        isDark = isDark,
                                        accentColor = if (task.isCompleted) Color(0xFF10B981).copy(alpha = 0.35f) else null
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Sleek circular checkbox toggle
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (task.isCompleted) Color(0xFF10B981).copy(alpha = 0.15f)
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                                    CircleShape
                                                )
                                                .border(
                                                    1.dp,
                                                    if (task.isCompleted) Color(0xFF10B981)
                                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                                    CircleShape
                                                )
                                                .bouncyClick { viewModel.toggleTaskCompleted(task) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (task.isCompleted) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Check,
                                                    contentDescription = "Completed",
                                                    tint = Color(0xFF10B981),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = task.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (task.description.isNotBlank()) {
                                                Text(task.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }

                                        // Smooth capsule action: Focus Timer
                                        GlassCapsule(
                                            onClick = {
                                                val subjectParam = course.subjectId?.let { "&subjectId=$it" } ?: ""
                                                navController.navigate("pomodoro?courseId=${courseId}&taskId=${task.id}$subjectParam")
                                            },
                                            containerColor = courseColor.copy(alpha = 0.12f),
                                            border = BorderStroke(0.8.dp, courseColor.copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(Icons.Rounded.Timer, contentDescription = "Start Pomodoro", tint = courseColor, modifier = Modifier.size(13.dp))
                                                Text("Focus", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = courseColor, fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                CourseDetailTab.ATTENDANCE -> {
                    item {
                        val attendanceByDay = remember(attendanceRecords) {
                            attendanceRecords.associateBy { rec ->
                                val cal = Calendar.getInstance().apply { timeInMillis = rec.dateMillis }
                                val year = cal.get(Calendar.YEAR)
                                val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
                                "$year-$dayOfYear"
                            }
                        }
                        var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
                        var showAttendanceDialog by remember { mutableStateOf(false) }
                        var isMonthlyView by remember { mutableStateOf(false) }
                        var displayMonthOffset by remember { mutableIntStateOf(0) }

                        val cancelled = attendanceRecords.count { it.status.equals("Cancelled", ignoreCase = true) || it.status.equals("Holiday", ignoreCase = true) }
                        val effectiveTotal = attendanceRecords.size - cancelled
                        val presentCount = attendanceRecords.count { it.status.equals("Present", ignoreCase = true) }
                        val lateCount = attendanceRecords.count { it.status.equals("Late", ignoreCase = true) }
                        val absentCount = attendanceRecords.count { it.status.equals("Absent", ignoreCase = true) }
                        val totalAttended = presentCount + lateCount
                        val attendancePct = if (effectiveTotal > 0) ((totalAttended.toFloat() / effectiveTotal) * 100).roundToInt() else 100

                        val gaugeProgress by animateFloatAsState(
                            targetValue = if (effectiveTotal > 0) totalAttended.toFloat() / effectiveTotal else 1f,
                            animationSpec = tween(700),
                            label = "attendance_gauge"
                        )

                        val thresholdColor = when {
                            effectiveTotal == 0 -> MaterialTheme.colorScheme.primary
                            attendancePct >= 75 -> Color(0xFF10B981)
                            attendancePct >= 50 -> Color(0xFFF59E0B)
                            else -> Color(0xFFEF4444)
                        }

                        val thresholdLabel = when {
                            effectiveTotal == 0 -> "No Classes Recorded"
                            attendancePct >= 75 -> "Healthy (>75%)"
                            attendancePct >= 50 -> "Caution (Near 75%)"
                            else -> "Critical Attendance"
                        }

                        val todayCal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val todayKey = "${todayCal.get(Calendar.YEAR)}-${todayCal.get(Calendar.DAY_OF_YEAR)}"
                        val todayRecord = attendanceByDay[todayKey]

                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Attendance Health Gauge Card
                            ScholarCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                glassmorphic = true,
                                border = ScholarCardDefaults.glassBorder(isDark, accentColor = thresholdColor)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    // Ambient glowing blur aura behind the attendance gauge
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .align(Alignment.TopStart)
                                            .offset(x = 10.dp, y = 30.dp)
                                            .blur(40.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                                            .background(thresholdColor.copy(alpha = if (isDark) 0.22f else 0.15f), CircleShape)
                                    )

                                    Column(modifier = Modifier.padding(18.dp)) {
                                        // Header Row: Attendance Health Title + Capsule Status Pill + View Toggle Capsule
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text("Attendance Health", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                GlassCapsule(
                                                    containerColor = thresholdColor.copy(alpha = 0.14f),
                                                    border = BorderStroke(0.8.dp, thresholdColor.copy(alpha = 0.35f))
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Box(modifier = Modifier.size(5.dp).background(thresholdColor, CircleShape))
                                                        Text(
                                                            text = thresholdLabel,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = thresholdColor,
                                                            fontSize = 10.sp
                                                        )
                                                    }
                                                }
                                            }

                                            GlassCapsule(
                                                onClick = { isMonthlyView = !isMonthlyView; displayMonthOffset = 0 },
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isMonthlyView) Icons.Rounded.ViewWeek else Icons.Rounded.DateRange,
                                                        contentDescription = "Toggle View",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        if (isMonthlyView) "Month" else "Week",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Metric Body: Gauge & Clean Icon-Number Capsule Badges
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    if (isDark) MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.45f)
                                                    else thresholdColor.copy(alpha = 0.06f),
                                                    RoundedCornerShape(18.dp)
                                                )
                                                .border(
                                                    0.8.dp,
                                                    thresholdColor.copy(alpha = 0.20f),
                                                    RoundedCornerShape(18.dp)
                                                )
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.size(80.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(
                                                    progress = { gaugeProgress },
                                                    modifier = Modifier.fillMaxSize(),
                                                    color = thresholdColor,
                                                    trackColor = thresholdColor.copy(alpha = 0.14f),
                                                    strokeWidth = 7.dp,
                                                    strokeCap = StrokeCap.Round
                                                )
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(
                                                        text = "$attendancePct%",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Black,
                                                        color = thresholdColor
                                                    )
                                                    Text(
                                                        text = "$totalAttended/$effectiveTotal",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            // Clean icon-number pairings in capsule format
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        AttendanceCounterItem(label = "Present", count = presentCount, color = Color(0xFF10B981))
                                                    }
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        AttendanceCounterItem(label = "Late", count = lateCount, color = Color(0xFFF59E0B))
                                                    }
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        AttendanceCounterItem(label = "Absent", count = absentCount, color = Color(0xFFEF4444))
                                                    }
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        AttendanceCounterItem(label = "Off", count = cancelled, color = Color(0xFF6B7280))
                                                    }
                                                }
                                            }
                                        }

                                        // Fast Mark Today
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = "Today's Status",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf(
                                                Triple("Present", Color(0xFF10B981), Icons.Rounded.CheckCircle),
                                                Triple("Late", Color(0xFFF59E0B), Icons.Rounded.Schedule),
                                                Triple("Absent", Color(0xFFEF4444), Icons.Rounded.Cancel)
                                            ).forEach { (statusOpt, col, statusIcon) ->
                                                val isSelected = todayRecord?.status.equals(statusOpt, ignoreCase = true)
                                                GlassCapsule(
                                                    modifier = Modifier.weight(1f),
                                                    containerColor = if (isSelected) col else col.copy(alpha = 0.08f),
                                                    border = BorderStroke(if (isSelected) 1.2.dp else 0.8.dp, if (isSelected) col else col.copy(alpha = 0.25f)),
                                                    onClick = {
                                                        if (todayRecord != null) {
                                                            viewModel.updateAttendanceRecord(todayRecord.copy(status = statusOpt))
                                                        } else {
                                                            viewModel.addAttendanceRecord(courseId, todayCal.timeInMillis, statusOpt)
                                                        }
                                                    }
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isSelected) Icons.Rounded.Check else statusIcon,
                                                            contentDescription = null,
                                                            tint = if (isSelected) Color.White else col,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(Modifier.width(4.dp))
                                                        Text(
                                                            text = statusOpt,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) Color.White else col
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                    // Compact Timeline or Calendar View
                                    Spacer(modifier = Modifier.height(14.dp))
                                    if (isMonthlyView) {
                                        val calendar = Calendar.getInstance().apply {
                                            timeInMillis = System.currentTimeMillis()
                                            set(Calendar.DAY_OF_MONTH, 1)
                                            add(Calendar.MONTH, displayMonthOffset)
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }
                                        val monthStartMillis = calendar.timeInMillis
                                        val startDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
                                        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                                        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            BouncyIconButton(onClick = { displayMonthOffset-- }) {
                                                Icon(Icons.Rounded.ChevronLeft, contentDescription = "Prev", modifier = Modifier.size(18.dp))
                                            }
                                            Text(monthName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            BouncyIconButton(onClick = { displayMonthOffset++ }) {
                                                Icon(Icons.Rounded.ChevronRight, contentDescription = "Next", modifier = Modifier.size(18.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))

                                        val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            daysOfWeek.forEach { day ->
                                                Text(
                                                    day,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))

                                        val totalCells = ceil((daysInMonth + startDayOfWeek) / 7.0).toInt() * 7
                                        var renderDay = 1
                                        for (row in 0 until (totalCells / 7)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                for (col in 0 until 7) {
                                                    if ((row == 0 && col < startDayOfWeek) || renderDay > daysInMonth) {
                                                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                                    } else {
                                                        val dateCal = Calendar.getInstance().apply {
                                                            timeInMillis = monthStartMillis
                                                            set(Calendar.DAY_OF_MONTH, renderDay)
                                                        }
                                                        val dateMillis = dateCal.timeInMillis
                                                        val displayDay = renderDay
                                                        renderDay++

                                                        val renderKey = "${dateCal.get(Calendar.YEAR)}-${dateCal.get(Calendar.DAY_OF_YEAR)}"
                                                        val record = attendanceByDay[renderKey]
                                                        val statusColor = when (record?.status?.lowercase()) {
                                                            "present" -> Color(0xFF10B981)
                                                            "absent" -> Color(0xFFEF4444)
                                                            "late" -> Color(0xFFF59E0B)
                                                            "cancelled", "holiday" -> Color.Gray
                                                            else -> Color.Transparent
                                                        }

                                                        val isToday = todayCal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
                                                                todayCal.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR)

                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .aspectRatio(1f)
                                                                .padding(1.dp)
                                                                .clip(CircleShape)
                                                                .background(statusColor)
                                                                .then(if (isToday) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                                                                .bouncyClick {
                                                                    selectedDate = dateMillis
                                                                    showAttendanceDialog = true
                                                                },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                "$displayDay",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = if (isToday) FontWeight.Black else FontWeight.Normal,
                                                                color = if (record != null) Color.White else MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // Compact 7-day strip
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            val calInstance = Calendar.getInstance().apply {
                                                timeInMillis = System.currentTimeMillis()
                                                set(Calendar.HOUR_OF_DAY, 0)
                                                set(Calendar.MINUTE, 0)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }
                                            val todayMillis = calInstance.timeInMillis

                                            for (i in 6 downTo 0) {
                                                val dateCal = Calendar.getInstance().apply {
                                                    timeInMillis = todayMillis
                                                    add(Calendar.DAY_OF_YEAR, -i)
                                                }
                                                val dateMillis = dateCal.timeInMillis
                                                val dayOfWeek = SimpleDateFormat("E", Locale.getDefault()).format(dateCal.time)
                                                val dayOfMonth = SimpleDateFormat("d", Locale.getDefault()).format(dateCal.time)

                                                val renderKey = "${dateCal.get(Calendar.YEAR)}-${dateCal.get(Calendar.DAY_OF_YEAR)}"
                                                val record = attendanceByDay[renderKey]
                                                val statusColor = when (record?.status?.lowercase()) {
                                                    "present" -> Color(0xFF10B981)
                                                    "absent" -> Color(0xFFEF4444)
                                                    "late" -> Color(0xFFF59E0B)
                                                    "cancelled", "holiday" -> Color.Gray
                                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                }
                                                val isToday = i == 0

                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .bouncyClick {
                                                            selectedDate = dateMillis
                                                            showAttendanceDialog = true
                                                        }
                                                        .padding(2.dp)
                                                ) {
                                                    Text(
                                                        text = dayOfWeek.take(1),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .background(statusColor, CircleShape)
                                                            .then(if (isToday) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = dayOfMonth,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (record != null) Color.White else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (showAttendanceDialog) {
                            val selCal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                            val renderKey = "${selCal.get(Calendar.YEAR)}-${selCal.get(Calendar.DAY_OF_YEAR)}"
                            val record = attendanceByDay[renderKey]
                            val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())

                            AlertDialog(
                                onDismissRequest = { showAttendanceDialog = false },
                                title = { Text("Log Attendance", fontWeight = FontWeight.Bold) },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = dateFormat.format(Date(selectedDate)),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))

                                        val options = listOf(
                                            "Present" to Color(0xFF10B981),
                                            "Late" to Color(0xFFF59E0B),
                                            "Absent" to Color(0xFFEF4444),
                                            "Cancelled" to Color.Gray
                                        )

                                        options.forEach { (option, optColor) ->
                                            val isSelected = record?.status.equals(option, ignoreCase = true)
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .bouncyClick {
                                                        if (record != null) {
                                                            viewModel.updateAttendanceRecord(record.copy(status = option))
                                                        } else {
                                                            viewModel.addAttendanceRecord(courseId, selectedDate, option)
                                                        }
                                                        showAttendanceDialog = false
                                                    },
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSelected) optColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    RadioButton(
                                                        selected = isSelected,
                                                        onClick = null,
                                                        colors = RadioButtonDefaults.colors(selectedColor = optColor)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = option,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) optColor else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }

                                        if (record != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            BouncyTextButton(
                                                onClick = {
                                                    viewModel.deleteAttendanceRecord(record)
                                                    showAttendanceDialog = false
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Clear Record", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    BouncyTextButton(onClick = { showAttendanceDialog = false }) {
                                        Text("Close")
                                    }
                                }
                            )
                        }
                    }
                }
            }

                CourseDetailTab.ASSIGNMENTS -> {
                    // Assignments Header & Add Button
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Assignments",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                GlassCapsule(
                                    containerColor = courseColor.copy(alpha = 0.12f),
                                    border = BorderStroke(0.8.dp, courseColor.copy(alpha = 0.25f))
                                ) {
                                    Text(
                                        text = "${localAssignments.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = courseColor,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            GlassCapsule(
                                onClick = { showAddAssignmentDialog = true },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        "Add",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }

                    if (localAssignments.isEmpty()) {
                        item(key = "assignments_empty") {
                            ScholarCard(
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                shape = RoundedCornerShape(20.dp),
                                glassmorphic = true
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "No assignments recorded",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    GlassCapsule(
                                        onClick = { showAddAssignmentDialog = true },
                                        containerColor = courseColor.copy(alpha = 0.14f),
                                        border = BorderStroke(0.8.dp, courseColor.copy(alpha = 0.35f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Rounded.Add, contentDescription = null, tint = courseColor, modifier = Modifier.size(14.dp))
                                            Text("Add First Assignment", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = courseColor)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        items(localAssignments, key = { "assignment_${it.id}" }) { assignment ->
                            ReorderableItem(reorderableState, key = "assignment_${assignment.id}") { isDragging ->
                                val cardColor by animateColorAsState(
                                    if (assignment.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f)
                                    else if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f)
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                                )
                                ScholarCard(
                                    modifier = Modifier
                                        .detectReorderAfterLongPress(reorderableState)
                                        .animateItem()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    glassmorphic = true,
                                    containerColor = cardColor,
                                    border = ScholarCardDefaults.glassBorder(
                                        isDark = isDark,
                                        accentColor = if (assignment.isCompleted) Color(0xFF10B981).copy(alpha = 0.35f) else null
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Sleek circular completion toggle
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (assignment.isCompleted) Color(0xFF10B981).copy(alpha = 0.15f)
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                                    CircleShape
                                                )
                                                .border(
                                                    1.dp,
                                                    if (assignment.isCompleted) Color(0xFF10B981)
                                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                                    CircleShape
                                                )
                                                .bouncyClick { viewModel.toggleAssignmentCompleted(assignment) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (assignment.isCompleted) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Check,
                                                    contentDescription = "Completed",
                                                    tint = Color(0xFF10B981),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = assignment.title,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                textDecoration = if (assignment.isCompleted) TextDecoration.LineThrough else null
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            // Capsule Badges Row: Category pill + Due date badge
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                if (assignment.category.isNotEmpty()) {
                                                    val catCol = try {
                                                        Color(android.graphics.Color.parseColor(assignment.categoryColor))
                                                    } catch (e: Exception) {
                                                        courseColor
                                                    }
                                                    GlassCapsule(
                                                        containerColor = catCol.copy(alpha = 0.14f),
                                                        border = BorderStroke(0.8.dp, catCol.copy(alpha = 0.35f))
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Box(modifier = Modifier.size(5.dp).background(catCol, CircleShape))
                                                            Text(
                                                                text = assignment.category,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.Bold,
                                                                color = catCol,
                                                                fontSize = 10.sp
                                                            )
                                                        }
                                                    }
                                                }

                                                if (assignment.dueDateMillis > 0) {
                                                    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                                                    GlassCapsule(
                                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Rounded.Event,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(11.dp)
                                                            )
                                                            Text(
                                                                text = dateFormat.format(Date(assignment.dueDateMillis)),
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.primary,
                                                                fontWeight = FontWeight.SemiBold,
                                                                fontSize = 10.sp
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            if (assignment.description.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = assignment.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        // Smooth capsule actions
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            GlassCapsule(
                                                onClick = {
                                                    val subjectParam = course.subjectId?.let { "&subjectId=$it" } ?: ""
                                                    navController.navigate("pomodoro?courseId=${courseId}&assignmentId=${assignment.id}$subjectParam")
                                                },
                                                containerColor = courseColor.copy(alpha = 0.12f),
                                                border = BorderStroke(0.8.dp, courseColor.copy(alpha = 0.25f))
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Timer,
                                                    contentDescription = "Start Pomodoro",
                                                    tint = courseColor,
                                                    modifier = Modifier.padding(6.dp).size(15.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { assignmentToEdit = assignment },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteAssignment(assignment) },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(15.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Test Corner & Analysis Integration
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        TestCornerCard(
                            testRecords = testRecords,
                            topics = topics,
                            onAddTest = { newTest ->
                                val finalSubjectId = newTest.topicId?.let { tId -> topics.find { it.id == tId }?.subjectId }
                                viewModel.addTestRecord(newTest.copy(courseId = courseId, subjectId = finalSubjectId))
                            },
                            onUpdateTest = { updatedTest ->
                                val finalSubjectId = updatedTest.topicId?.let { tId -> topics.find { it.id == tId }?.subjectId }
                                viewModel.updateTestRecord(updatedTest.copy(subjectId = finalSubjectId))
                            },
                            onDeleteTest = { testToDelete ->
                                viewModel.deleteTestRecord(testToDelete)
                            }
                        )
                    }
                }

                CourseDetailTab.NOTES -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Course Notes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            BouncyButton(
                                onClick = {
                                    noteToEdit = null
                                    noteText = ""
                                    noteCustomTag = "Theory"
                                    showAddNoteDialog = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Add Note", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (courseNotes.isEmpty()) {
                        item {
                            ScholarCard(
                                modifier = Modifier.fillMaxWidth().height(90.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        "No notes written for this course yet.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(courseNotes, key = { "cn_${it.id}" }) { note ->
                            ScholarCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val isCurrentCourse = note.courseId == course.id
                                        val pillColor = if (isCurrentCourse) courseColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.secondaryContainer
                                        val onPillColor = if (isCurrentCourse) courseColor else MaterialTheme.colorScheme.onSecondaryContainer

                                        Box(
                                            modifier = Modifier
                                                .background(pillColor, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = note.tag.ifBlank { if (isCurrentCourse) "Course" else "Linked" },
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = onPillColor
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    noteToEdit = note
                                                    noteText = note.content
                                                    noteCustomTag = note.tag
                                                    showAddNoteDialog = true
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Rounded.Edit, "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteNote(note) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Rounded.Delete, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(15.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = note.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                    val formattedDate = remember(note.dateMillis) {
                                        SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault()).format(Date(note.dateMillis))
                                    }
                                    Text(
                                        text = formattedDate,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }

                CourseDetailTab.GUIDES -> {
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
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Browse File", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }

                            BouncyButton(
                                onClick = {
                                    attachmentTitle = "Study Guide - ${course.name}"
                                    attachmentContent = if (courseNotes.isNotEmpty()) {
                                        courseNotes.joinToString("\n\n") { "• [${it.tag}] ${it.content}" }
                                    } else {
                                        "Summary study guide for ${course.name}.\n\nSchedule: ${course.schedule}\nInstructor: ${course.instructor}\n\nTasks:\n" +
                                                courseTasks.joinToString("\n") { "[] " + it.title }
                                    }
                                    showAddAttachmentDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI Auto-Guide", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (attachments.isEmpty()) {
                        item {
                            ScholarCard(
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "No course attachments linked",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Attach PDFs, notes, or generate an AI study guide.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            try {
                                                    val file = File(attachment.filePath)
                                                    if (!file.exists()) {
                                                        Toast.makeText(context, "File does not exist or was deleted", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        val authority = "${context.packageName}.provider"
                                                        val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
                                                        val mimeType = when (extension) {
                                                            "pdf" -> "application/pdf"
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
                                        .padding(12.dp),

                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(visualMeta.third, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = visualMeta.first,
                                            contentDescription = null,
                                            tint = visualMeta.second,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = attachment.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        val sizeFormatted = remember(attachment.sizeBytes) {
                                            if (attachment.sizeBytes < 1024) "${attachment.sizeBytes} B"
                                            else if (attachment.sizeBytes < 1024 * 1024) "${attachment.sizeBytes / 1024} KB"
                                            else String.format(Locale.getDefault(), "%.2f MB", attachment.sizeBytes.toFloat() / (1024 * 1024))
                                        }
                                        Text(
                                            text = "$sizeFormatted • ${attachment.fileType.uppercase()}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteAttachment(attachment) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Delete,
                                            contentDescription = "Delete Attachment",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
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

    // --- DIALOGS ---

    // Edit Course Dialog
    if (showEditCourseDialog) {
        EditCourseDialog(
            course = course,
            viewModel = viewModel,
            onDismiss = { showEditCourseDialog = false }
        )
    }

    // Delete Course Confirmation Dialog
    if (showDeleteCourseDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteCourseDialog = false },
            title = { Text("Delete Course?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ${course.name}? All linked assignments and attendance logs will be removed.") },
            confirmButton = {
                BouncyTextButton(
                    onClick = {
                        viewModel.deleteCourse(course)
                        showDeleteCourseDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showDeleteCourseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Link Subjects Dialog
    if (showLinkSubjectDialog) {
        var createNew by remember { mutableStateOf(false) }
        var newSubjectName by remember { mutableStateOf("") }
        var newSubjectTags by remember { mutableStateOf("") }

        val currentIds = remember(course) {
            val list = mutableListOf<Int>()
            if (course.subjectId != null) list.add(course.subjectId)
            if (course.subjectIds.isNotBlank()) {
                list.addAll(course.subjectIds.split(",").mapNotNull { it.trim().toIntOrNull() })
            }
            list.distinct()
        }
        var selectedExistingIds by remember { mutableStateOf(currentIds) }

        AlertDialog(
            onDismissRequest = { showLinkSubjectDialog = false },
            title = { Text("Link Study Subjects", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = !createNew, onClick = { createNew = false })
                        Text("Link Existing Subjects", fontWeight = FontWeight.SemiBold)
                    }
                    if (!createNew) {
                        Spacer(modifier = Modifier.height(8.dp))
                        if (subjects.isEmpty()) {
                            Text("No existing subjects found.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(subjects) { subj ->
                                    FilterChip(
                                        selected = selectedExistingIds.contains(subj.id),
                                        onClick = {
                                            selectedExistingIds = if (selectedExistingIds.contains(subj.id)) {
                                                selectedExistingIds.filter { it != subj.id }
                                            } else {
                                                selectedExistingIds + subj.id
                                            }
                                        },
                                        label = { Text(subj.name) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = createNew, onClick = { createNew = true })
                        Text("Create New Subject", fontWeight = FontWeight.SemiBold)
                    }
                    if (createNew) {
                        OutlinedTextField(
                            value = newSubjectName,
                            onValueChange = { newSubjectName = it },
                            label = { Text("New Subject Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newSubjectTags,
                            onValueChange = { newSubjectTags = it },
                            label = { Text("Tags (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                BouncyTextButton(onClick = {
                    if (createNew && newSubjectName.isNotBlank()) {
                        viewModel.createSubjectAndLinkToCourse(
                            name = newSubjectName,
                            tags = newSubjectTags,
                            course = course,
                            existingSubjectIds = selectedExistingIds
                        )
                        showLinkSubjectDialog = false
                    } else if (!createNew) {
                        val updatedIds = selectedExistingIds.joinToString(",")
                        val firstId = selectedExistingIds.firstOrNull()
                        viewModel.updateCourse(course.copy(subjectIds = updatedIds, subjectId = firstId))
                        showLinkSubjectDialog = false
                    }
                }) { Text("Confirm") }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showLinkSubjectDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Assignment Dialog
    if (showAddAssignmentDialog) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var dueDateMillis by remember { mutableStateOf(System.currentTimeMillis() + 86400000L) }
        var showDatePicker by remember { mutableStateOf(false) }
        var category by remember { mutableStateOf("Homework") }
        var categoryColor by remember { mutableStateOf("#3197D6") }
        var tags by remember { mutableStateOf("") }
        var selectedSubjectId by remember { mutableStateOf<Int?>(linkedSubject?.id) }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { dueDateMillis = it }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState, showModeToggle = false)
            }
        }

        AlertDialog(
            onDismissRequest = { showAddAssignmentDialog = false },
            title = { Text("Add Assignment", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (Optional, comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Category Preset", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                    val categoriesPresetList = listOf("Homework", "Exam", "Project", "Quiz", "Lab", "Custom")
                    var isCustomCategory by remember { mutableStateOf(!categoriesPresetList.dropLast(1).contains(category)) }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoriesPresetList.forEach { cat ->
                            val isSelected = (cat == "Custom" && isCustomCategory) || (cat == category && !isCustomCategory)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (cat == "Custom") {
                                        isCustomCategory = true
                                        category = ""
                                    } else {
                                        isCustomCategory = false
                                        category = cat
                                        categoryColor = when (cat) {
                                            "Exam" -> "#E52F28"
                                            "Homework" -> "#3197D6"
                                            "Project" -> "#2CAF5F"
                                            "Quiz" -> "#7B2CBF"
                                            "Lab" -> "#E65100"
                                            else -> "#78909C"
                                        }
                                    }
                                },
                                label = { Text(cat) }
                            )
                        }
                    }

                    if (isCustomCategory) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Custom Category Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Category Color", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                    val presetColorsList = listOf("#E52F28", "#E65100", "#FBC02D", "#2CAF5F", "#3197D6", "#7B2CBF", "#78909C")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        presetColorsList.forEach { hex ->
                            val colorObj = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { courseColor }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(colorObj, CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = if (categoryColor == hex) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { categoryColor = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (categoryColor == hex) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(Color.White, CircleShape)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Link to Study Subject (Optional)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 48.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedSubjectId == null,
                                onClick = { selectedSubjectId = null },
                                label = { Text("None") }
                            )
                        }
                        items(subjects) { subj ->
                            FilterChip(
                                selected = selectedSubjectId == subj.id,
                                onClick = { selectedSubjectId = subj.id },
                                label = { Text(subj.name) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    BouncyOutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        Text("Due Date: ${dateFormat.format(Date(dueDateMillis))}")
                    }
                }
            },
            confirmButton = {
                BouncyTextButton(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addAssignment(courseId, title, desc, dueDateMillis, category.ifBlank { "Other" }, categoryColor, tags, selectedSubjectId)
                        showAddAssignmentDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showAddAssignmentDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Edit Assignment Dialog
    if (assignmentToEdit != null) {
        val targetAssignment = assignmentToEdit!!
        var title by remember(targetAssignment) { mutableStateOf(targetAssignment.title) }
        var desc by remember(targetAssignment) { mutableStateOf(targetAssignment.description) }
        var dueDateMillis by remember(targetAssignment) { mutableStateOf(if (targetAssignment.dueDateMillis > 0) targetAssignment.dueDateMillis else System.currentTimeMillis()) }
        var category by remember(targetAssignment) { mutableStateOf(targetAssignment.category) }
        var categoryColor by remember(targetAssignment) { mutableStateOf(targetAssignment.categoryColor) }
        var tags by remember(targetAssignment) { mutableStateOf(targetAssignment.tags) }
        var showDatePicker by remember { mutableStateOf(false) }
        var selectedSubjectId by remember(targetAssignment) { mutableStateOf<Int?>(targetAssignment.subjectId) }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { dueDateMillis = it }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState, showModeToggle = false)
            }
        }

        AlertDialog(
            onDismissRequest = { assignmentToEdit = null },
            title = { Text("Edit Assignment", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (Optional, comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Category Preset", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                    val categoriesPresetList = listOf("Homework", "Exam", "Project", "Quiz", "Lab", "Custom")
                    var isCustomCategory by remember { mutableStateOf(!categoriesPresetList.dropLast(1).contains(category)) }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoriesPresetList.forEach { cat ->
                            val isSelected = (cat == "Custom" && isCustomCategory) || (cat == category && !isCustomCategory)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (cat == "Custom") {
                                        isCustomCategory = true
                                        category = ""
                                    } else {
                                        isCustomCategory = false
                                        category = cat
                                        categoryColor = when (cat) {
                                            "Exam" -> "#E52F28"
                                            "Homework" -> "#3197D6"
                                            "Project" -> "#2CAF5F"
                                            "Quiz" -> "#7B2CBF"
                                            "Lab" -> "#E65100"
                                            else -> "#78909C"
                                        }
                                    }
                                },
                                label = { Text(cat) }
                            )
                        }
                    }

                    if (isCustomCategory) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Custom Category Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Category Color", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                    val presetColorsList = listOf("#E52F28", "#E65100", "#FBC02D", "#2CAF5F", "#3197D6", "#7B2CBF", "#78909C")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        presetColorsList.forEach { hex ->
                            val colorObj = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { courseColor }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(colorObj, CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = if (categoryColor == hex) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { categoryColor = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (categoryColor == hex) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(Color.White, CircleShape)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Link to Study Subject (Optional)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 48.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedSubjectId == null,
                                onClick = { selectedSubjectId = null },
                                label = { Text("None") }
                            )
                        }
                        items(subjects) { subj ->
                            FilterChip(
                                selected = selectedSubjectId == subj.id,
                                onClick = { selectedSubjectId = subj.id },
                                label = { Text(subj.name) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    BouncyOutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        Text("Due Date: ${dateFormat.format(Date(dueDateMillis))}")
                    }
                }
            },
            confirmButton = {
                BouncyTextButton(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.updateAssignmentDetails(
                            targetAssignment.copy(
                                title = title,
                                description = desc,
                                dueDateMillis = dueDateMillis,
                                category = category.ifBlank { "Other" },
                                categoryColor = categoryColor,
                                tags = tags,
                                subjectId = selectedSubjectId
                            )
                        )
                        assignmentToEdit = null
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                BouncyTextButton(onClick = { assignmentToEdit = null }) { Text("Cancel") }
            }
        )
    }

    // Add / Edit Note Dialog
    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text(if (noteToEdit == null) "Add Note" else "Edit Note", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Note content") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3
                    )
                    OutlinedTextField(
                        value = noteCustomTag,
                        onValueChange = { noteCustomTag = it },
                        label = { Text("Note Tag (e.g. Theory, Lab, Formula)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                BouncyTextButton(onClick = {
                    if (noteText.isNotBlank()) {
                        val activeNote = noteToEdit
                        if (activeNote != null) {
                            viewModel.updateNote(
                                activeNote.copy(
                                    content = noteText,
                                    tag = noteCustomTag
                                )
                            )
                        } else {
                            viewModel.addNote(
                                content = noteText,
                                courseId = course.id,
                                tag = noteCustomTag
                            )
                        }
                        showAddNoteDialog = false
                        noteText = ""
                        noteCustomTag = "Theory"
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showAddNoteDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Attachment (AI Study Guide PDF) Dialog
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
                        "Review or customize the automatic study booklet content. This will compile a PDF document linked to this course.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = attachmentTitle,
                        onValueChange = { attachmentTitle = it },
                        label = { Text("Document Title") },
                        placeholder = { Text("e.g. Midterm Study Guide - ${course.name}") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = attachmentContent,
                        onValueChange = { attachmentContent = it },
                        label = { Text("Study Guide / Lecture Material Content") },
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

/**
 * CourseDetailTabCapsuleBar - Sleek segmented horizontal capsule bar for rapid switching between course sections.
 */
@Composable
private fun CourseDetailTabCapsuleBar(
    selectedTab: CourseDetailTab,
    onTabSelected: (CourseDetailTab) -> Unit,
    counts: Map<CourseDetailTab, Int>,
    courseColor: Color,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(CourseDetailTab.entries) { tab ->
            val isSelected = selectedTab == tab
            val containerColor by animateColorAsState(
                targetValue = if (isSelected) courseColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                animationSpec = tween(200),
                label = "tab_bg_${tab.name}"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(200),
                label = "tab_fg_${tab.name}"
            )
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.0f else 0.96f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                label = "tab_scale_${tab.name}"
            )

            Surface(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(CircleShape)
                    .bouncyClick { onTabSelected(tab) },
                shape = CircleShape,
                color = containerColor
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = contentColor
                    )
                    val count = counts[tab]
                    if (count != null && count > 0) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@ValueScore(
    score = 35,
    importance = Importance.LOW,
    description = "Metadata pill badge for course instructor, schedule and tags",
    category = "Study"
)
@Composable
private fun DetailMetaBadge(
    icon: ImageVector,
    text: String,
    tint: Color
) {
    Surface(
        color = tint.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(12.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = tint)
        }
    }
}

@ValueScore(
    score = 30,
    importance = Importance.LOW,
    description = "Attendance summary indicator metric cell",
    category = "Study"
)
@Composable
private fun AttendanceCounterItem(
    label: String,
    count: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}
