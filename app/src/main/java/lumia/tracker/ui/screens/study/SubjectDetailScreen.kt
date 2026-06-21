package lumia.tracker.ui.screens

import lumia.tracker.ui.theme.liquidGlass
import lumia.tracker.ui.theme.glassBar
import android.app.DatePickerDialog
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ControlPoint
import androidx.compose.material.icons.rounded.ArrowCircleRight
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.LibraryAddCheck
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.viewmodel.ScholarViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import android.widget.Toast
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.graphics.StrokeCap
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.TestCornerCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(navController: NavController, viewModel: ScholarViewModel, subjectId: Int) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.find { it.id == subjectId }
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val systemAutoLinkByName by viewModel.systemAutoLinkByName.collectAsStateWithLifecycle()

    val linkedCourses = remember(subject, courses, systemAutoLinkByName) {
        if (subject != null) {
            courses.filter { course ->
                val crsName = course.name.trim().lowercase()
                val subName = subject.name.trim().lowercase()
                course.subjectId == subject.id || (systemAutoLinkByName && (
                    crsName == subName || 
                    (crsName.isNotEmpty() && subName.isNotEmpty() && (crsName.contains(subName) || subName.contains(crsName)))
                ))
            }
        } else {
            emptyList()
        }
    }

    val topics by viewModel.getTopicsForSubject(subjectId).collectAsStateWithLifecycle()
    var showAddTopic by remember { mutableStateOf(false) }
    val chapters by viewModel.getChaptersForSubject(subjectId).collectAsStateWithLifecycle()
    var showAddChapter by remember { mutableStateOf(false) }
    var chapterToEdit by remember { mutableStateOf<lumia.tracker.model.Chapter?>(null) }

    val allNotes by viewModel.notes.collectAsStateWithLifecycle()
    val subjectNotes = remember(allNotes, subject, linkedCourses) {
        if (subject != null) {
            val linkedCourseIds = linkedCourses.map { it.id }
            allNotes.filter { note ->
                note.subjectId == subject.id || 
                (note.courseId != null && linkedCourseIds.contains(note.courseId))
            }
        } else {
            emptyList()
        }
    }

    val allAssignments by viewModel.assignments.collectAsStateWithLifecycle()
    val subjectAssignments = remember(allAssignments, subjectId) {
        allAssignments.filter { it.subjectId == subjectId }
    }

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<lumia.tracker.model.Note?>(null) }
    var noteText by remember { mutableStateOf("") }
    var noteCustomTag by remember { mutableStateOf("Core") }

    val context = LocalContext.current

    val attachments by viewModel.getAttachmentsForSubject(subjectId).collectAsStateWithLifecycle(emptyList())
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
                    courseId = null,
                    subjectId = subjectId
                )
                Toast.makeText(context, "✅ Attachment successfully linked!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "❌ Link failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
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
            canvas.drawRect(0f, 0f, 595f, 120f, paint)
            
            paint.color = android.graphics.Color.WHITE
            paint.textSize = 24f
            paint.isFakeBoldText = true
            canvas.drawText("SCHOLAR STUDY GUIDE", 40f, 60f, paint)
            
            paint.textSize = 14f
            paint.isFakeBoldText = false
            paint.color = android.graphics.Color.parseColor("#E0E7FF")
            canvas.drawText("Linked Subject: ${subject?.name ?: ""}", 40f, 90f, paint)
            
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 16f
            paint.isFakeBoldText = true
            canvas.drawText(titleStr, 40f, 160f, paint)
            
            paint.textSize = 12f
            paint.isFakeBoldText = false
            paint.color = android.graphics.Color.parseColor("#1F2937")
            
            var y = 200f
            val maxLines = 25
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
                    if (currentLine.length > 70) {
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
            canvas.drawText("Generated with Scholar Companion App • page 1 of 1", 40f, 800f, paint)
            
            pdfDoc.finishPage(page)
            
            val localFile = File(context.filesDir, "study_guide_${System.currentTimeMillis()}.pdf")
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
            Toast.makeText(context, "✅ Study guide PDF generated!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "❌ Generation failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    if (subject == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (betaEnhancedHeader || isGlass) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                    )
                    // Sleek divider line for clean separation and anchoring
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                LargeTopAppBar(
                    title = { Text(subject.name, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Linked Courses Section
                if (linkedCourses.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                "Interconnected Academic Courses",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            linkedCourses.forEach { course ->
                                lumia.tracker.ui.components.GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    onClick = { navController.navigate("courseDetail/${course.id}") }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(20.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = course.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (course.instructor.isNotBlank()) {
                                                Text(
                                                    text = "Instructor: ${course.instructor}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (course.schedule.isNotBlank()) {
                                                Text(
                                                    text = "Schedule: ${course.schedule}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        IconButton(onClick = { navController.navigate("courseDetail/${course.id}") }) {
                                            Icon(
                                                imageVector = Icons.Rounded.ChevronRight,
                                                contentDescription = "Go to Course",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Connected Practice Assignments Section
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Connected Practice Assignments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (subjectAssignments.isEmpty()) {
                    item {
                        lumia.tracker.ui.components.GlassCard(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    "No connected assignments for this subject yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(subjectAssignments, key = { "asgn_${it.id}" }) { assignment ->
                        val associatedCourse = remember(courses, assignment.courseId) {
                            courses.find { it.id == assignment.courseId }
                        }
                        lumia.tracker.ui.components.GlassCard(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Checkbox(
                                        checked = assignment.isCompleted,
                                        onCheckedChange = { viewModel.toggleAssignmentCompleted(assignment) }
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = assignment.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            textDecoration = if (assignment.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                            color = if (assignment.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (associatedCourse != null) {
                                            Text(
                                                text = "Course: ${associatedCourse.name}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        val dateFormat = remember(assignment.dueDateMillis) {
                                            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                                        }
                                        Text(
                                            text = "Due: ${dateFormat.format(java.util.Date(assignment.dueDateMillis))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Subject Notes Section
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Subject Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Button(
                            onClick = { 
                                noteToEdit = null
                                noteText = ""
                                noteCustomTag = "Theory"
                                showAddNoteDialog = true 
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add Note", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Note")
                        }
                    }
                }

                if (subjectNotes.isEmpty()) {
                    item {
                        lumia.tracker.ui.components.GlassCard(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    "No notes for this study subject yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(subjectNotes, key = { "sn_${it.id}" }) { note ->
                        lumia.tracker.ui.components.GlassCard(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Tag/Capsule
                                    val isCurrentSubject = note.subjectId == subject.id
                                    val pillColor = if (isCurrentSubject) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                                    val onPillColor = if (isCurrentSubject) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                                    
                                    Box(
                                        modifier = Modifier
                                            .background(pillColor, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = note.tag.ifBlank { if (isCurrentSubject) "Subject" else "Linked" },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = onPillColor
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = {
                                            noteToEdit = note
                                            noteText = note.content
                                            noteCustomTag = note.tag
                                            showAddNoteDialog = true
                                        }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Rounded.Edit, "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(onClick = {
                                            viewModel.deleteNote(note)
                                        }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Rounded.Delete, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = note.content,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                val formattedDate = remember(note.dateMillis) {
                                    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy · hh:mm a", java.util.Locale.getDefault())
                                    sdf.format(java.util.Date(note.dateMillis))
                                }
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                // Test Corner Section
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    val testRecords by viewModel.getTestRecordsForSubject(subjectId).collectAsStateWithLifecycle()
                    
                    TestCornerCard(
                        testRecords = testRecords,
                        topics = topics,
                        onAddTest = { newTest ->
                            viewModel.addTestRecord(newTest.copy(subjectId = subjectId))
                        },
                        onUpdateTest = { updatedTest ->
                            viewModel.updateTestRecord(updatedTest)
                        },
                        onDeleteTest = { testToDelete ->
                            viewModel.deleteTestRecord(testToDelete)
                        }
                    )
                }

                // Chapters & Topics Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Study Chapters & Topics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { showAddChapter = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.heightIn(min = 36.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Chapter", style = MaterialTheme.typography.labelMedium)
                            }
                            Button(
                                onClick = { showAddTopic = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.heightIn(min = 36.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Topic", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                if (chapters.isEmpty() && topics.isEmpty()) {
                    item {
                        lumia.tracker.ui.components.GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth().padding(32.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LibraryAddCheck,
                                        contentDescription = null,
                                        modifier = Modifier.size(30.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No chapters or topics added yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Create chapters and associate study topics with them.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                } else {
                    // Render styled Chapters & nested Topics
                    chapters.forEach { chapter ->
                        val chapterTopics = topics.filter { it.chapterId == chapter.id }
                        val totalTopicCount = chapterTopics.size
                        val completedTopicCount = chapterTopics.count { it.isCompleted }
                        val progress = if (totalTopicCount > 0) completedTopicCount.toFloat() / totalTopicCount else 0f
                        
                        item(key = "chap_card_${chapter.id}") {
                            lumia.tracker.ui.components.GlassCard(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(20.dp),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = chapter.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            if (chapter.description.isNotEmpty()) {
                                                Text(
                                                    text = chapter.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2
                                                )
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { chapterToEdit = chapter; showAddChapter = true }) {
                                                Icon(Icons.Rounded.Edit, "Edit Chapter", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                            }
                                            IconButton(onClick = { viewModel.deleteChapter(chapter) }) {
                                                Icon(Icons.Rounded.Delete, "Delete Chapter", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(4.dp))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "${completedTopicCount}/${totalTopicCount} Topics (${(progress * 100).toInt()}%)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        if (chapterTopics.isEmpty()) {
                            item(key = "chap_empty_${chapter.id}") {
                                Text(
                                    "No topics added inside this chapter yet. Click the + Topic button to associate first concept.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp, end = 16.dp)
                                )
                            }
                        } else {
                            itemsIndexed(chapterTopics, key = { _, topic -> "t_${topic.id}" }) { index, topic ->
                                val cardColor by androidx.compose.animation.animateColorAsState(
                                    if (topic.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                                )
                                Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    lumia.tracker.ui.components.GlassCard(
                                        modifier = Modifier.weight(1f).padding(vertical = 4.dp).animateContentSize(),
                                        shape = RoundedCornerShape(16.dp),
                                        containerColor = cardColor
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                            Checkbox(
                                                checked = topic.isCompleted,
                                                onCheckedChange = { viewModel.toggleTopicCompleted(topic) }
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    "${index + 1}. ${topic.title}",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (topic.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                                                    textDecoration = if (topic.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                                )
                                                if (topic.tags.isNotBlank()) {
                                                    Text(
                                                        "Tags: ${topic.tags}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                    )
                                                }
                                            }
                                            IconButton(onClick = { viewModel.deleteTopic(topic) }) {
                                                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Render General / Uncategorized Topics
                    val generalTopics = topics.filter { it.chapterId == null }
                    if (generalTopics.isNotEmpty()) {
                        item(key = "chapters_general_header") {
                            Text(
                                text = "General Concepts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                            )
                        }
                        
                        itemsIndexed(generalTopics, key = { _, topic -> "t_${topic.id}" }) { index, topic ->
                            val cardColor by androidx.compose.animation.animateColorAsState(
                                if (topic.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                            )
                            lumia.tracker.ui.components.GlassCard(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).animateContentSize(),
                                shape = RoundedCornerShape(16.dp),
                                containerColor = cardColor
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    Checkbox(
                                        checked = topic.isCompleted,
                                        onCheckedChange = { viewModel.toggleTopicCompleted(topic) }
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "${index + 1}. ${topic.title}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (topic.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                                            textDecoration = if (topic.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                        )
                                        if (topic.tags.isNotBlank()) {
                                            Text(
                                                "Tags: ${topic.tags}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                    IconButton(onClick = { viewModel.deleteTopic(topic) }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                // Resource Hub & Attachments Section
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Text(
                            "Resource Hub & Attachments",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Link physical course materials, lecture slides, and notes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Browse Device File Button
                        FilledTonalButton(
                            onClick = { 
                                try {
                                    filePickerLauncher.launch(arrayOf("*/*"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "File picking not supported", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Browse File", style = MaterialTheme.typography.labelSmall)
                        }

                        // Generate Study Guide PDF Button
                        Button(
                            onClick = { 
                                attachmentTitle = "Summary of ${subject?.name ?: ""}"
                                attachmentContent = if (subjectNotes.isNotEmpty()) {
                                    subjectNotes.joinToString("\n\n") { "• [${it.tag}] ${it.content}" }
                                } else {
                                    "This is an automated study guide summary for ${subject?.name ?: ""}.\n\nTopics count: ${topics.size}\nChapters: ${chapters.size}\n\nTopics:\n" + topics.joinToString("\n") { "[] " + it.title }
                                }
                                showAddAttachmentDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Book, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Auto-Guide", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                if (attachments.isEmpty()) {
                    item {
                        lumia.tracker.ui.components.GlassCard(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "No study attachments linked.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Upload a PDF or tap AI Auto-Guide to generate study slips.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                                "pdf" -> Triple(Icons.Rounded.Description, Color(0xFFEF4444), Color(0xFFFEE2E2)) // Red for PDF
                                "png", "jpg", "jpeg", "gif", "webp", "bmp" -> Triple(Icons.Rounded.Photo, Color(0xFF10B981), Color(0xFFD1FAE5)) // Green for Image
                                "txt", "md", "doc", "docx" -> Triple(Icons.Rounded.Article, Color(0xFF3B82F6), Color(0xFFDBEAFE)) // Blue for Doc
                                "xls", "xlsx" -> Triple(Icons.Rounded.Article, Color(0xFF059669), Color(0xFFD1FAE5)) // Green-emerald for Sheets
                                "ppt", "pptx" -> Triple(Icons.Rounded.Article, Color(0xFFF59E0B), Color(0xFFFEF3C7)) // Amber for Presentation
                                "mp3", "wav", "aac", "flac", "ogg", "m4a" -> Triple(Icons.Rounded.VolumeUp, Color(0xFF8B5CF6), Color(0xFFEDE9FE)) // Purple for Audio/Music
                                "mp4", "mkv", "avi", "mov", "webm" -> Triple(Icons.Rounded.PlayArrow, Color(0xFFEC4899), Color(0xFFFCE7F3)) // Pink/Magenta for Video
                                "zip", "rar", "tar", "gz", "7z" -> Triple(Icons.Rounded.Folder, Color(0xFF6B7280), Color(0xFFE5E7EB)) // Gray for Archive
                                else -> Triple(Icons.Rounded.AttachFile, Color(0xFF4F46E5), Color(0xFFE0E7FF)) // Indigo for custom others
                            }
                        }

                        lumia.tracker.ui.components.GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
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
                                        .size(40.dp)
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
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
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
                                        else String.format("%.2f MB", attachment.sizeBytes.toFloat() / (1024 * 1024))
                                    }
                                    Text(
                                        text = "$sizeFormatted • ${attachment.fileType.uppercase()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                IconButton(
                                    onClick = { viewModel.deleteAttachment(attachment) }
                                ) {
                                    Icon(
                                        Icons.Rounded.Delete,
                                        contentDescription = "Delete Attachment",
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

    if (showAddTopic) {
        var title by remember { mutableStateOf("") }
        var tags by remember { mutableStateOf("") }
        var selectedChapterId by remember { mutableStateOf<Int?>(null) }
        var dropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddTopic = false },
            title = { Text("Add Topic") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Topic Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (chapters.isNotEmpty()) {
                        Text("Associate Chapter (Optional):", style = MaterialTheme.typography.labelMedium)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            val activeChapterName = chapters.find { it.id == selectedChapterId }?.name ?: "None (General concept)"
                            OutlinedButton(
                                onClick = { dropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(activeChapterName)
                                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None (General concept)") },
                                    onClick = { selectedChapterId = null; dropdownExpanded = false }
                                )
                                chapters.forEach { chapter ->
                                    DropdownMenuItem(
                                        text = { Text(chapter.name) },
                                        onClick = { selectedChapterId = chapter.id; dropdownExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addTopic(subjectId, title, tags, selectedChapterId)
                        showAddTopic = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddTopic = false }) { Text("Cancel") } }
        )
    }

    if (showAddChapter) {
        val editing = chapterToEdit
        var name by remember(editing) { mutableStateOf(editing?.name ?: "") }
        var description by remember(editing) { mutableStateOf(editing?.description ?: "") }
        var tags by remember(editing) { mutableStateOf(editing?.tags ?: "") }

        AlertDialog(
            onDismissRequest = { showAddChapter = false; chapterToEdit = null },
            title = { Text(if (editing != null) "Edit Chapter" else "Add Chapter") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Chapter Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        if (editing != null) {
                            viewModel.updateChapter(editing.copy(name = name, description = description, tags = tags))
                        } else {
                            viewModel.addChapter(name, subjectId, description, tags)
                        }
                        showAddChapter = false
                        chapterToEdit = null
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAddChapter = false; chapterToEdit = null }) { Text("Cancel") }
            }
        )
    }

    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text(if (noteToEdit == null) "Add Note" else "Edit Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Note content") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    OutlinedTextField(
                        value = noteCustomTag,
                        onValueChange = { noteCustomTag = it },
                        label = { Text("Note Tag (e.g. Theory, Lab, Formula)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
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
                                subjectId = subject.id,
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
                TextButton(onClick = { showAddNoteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddAttachmentDialog) {
        AlertDialog(
            onDismissRequest = { showAddAttachmentDialog = false },
            title = { Text("Generate Study Guide PDF") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Review or customize the automatic study booklet content. This will draft a physical PDF you can view anytime.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = attachmentTitle,
                        onValueChange = { attachmentTitle = it },
                        label = { Text("Document Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = attachmentContent,
                        onValueChange = { attachmentContent = it },
                        label = { Text("Study Guide / Lecture Material Content") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 6
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (attachmentTitle.isNotBlank() && attachmentContent.isNotBlank()) {
                        generateStudyGuidePdf(attachmentTitle, attachmentContent)
                        showAddAttachmentDialog = false
                    }
                }) { Text("Generate PDF") }
            },
            dismissButton = {
                TextButton(onClick = { showAddAttachmentDialog = false }) { Text("Cancel") }
            }
        )
    }
}
