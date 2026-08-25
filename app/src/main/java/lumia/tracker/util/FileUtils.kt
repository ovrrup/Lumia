package lumia.tracker.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import lumia.tracker.model.AttendanceRecord
import lumia.tracker.model.Chapter
import lumia.tracker.model.Course
import lumia.tracker.model.Note
import lumia.tracker.model.PracticeAssignment
import lumia.tracker.model.Subject
import lumia.tracker.model.Task
import lumia.tracker.model.Topic
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@ValueScore(
    score = 96,
    importance = Importance.CRITICAL,
    description = "Comprehensive Document, Multi-Page PDF Generation, Rendering & Sharing Engine with Attendance Tables and Assignment Checklists",
    category = "Utility"
)
object FileUtils {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN_LEFT = 40f
    private const val MARGIN_RIGHT = 555f
    private const val CONTENT_WIDTH = 515f
    private const val BOTTOM_MARGIN = 780f

    /**
     * Formats bytes into human-readable file size strings (e.g. 1.25 MB, 450 KB, 80 B).
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f KB", bytes.toFloat() / 1024f)
            else -> String.format(Locale.getDefault(), "%.2f MB", bytes.toFloat() / (1024f * 1024f))
        }
    }

    /**
     * Resolves the MIME type from a file path or extension.
     */
    fun getMimeType(filePath: String): String {
        val extension = if (filePath.contains(".")) filePath.substringAfterLast(".").lowercase(Locale.ROOT) else ""
        return when (extension) {
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
            "7z" -> "application/x-7z-compressed"
            "tar", "gz" -> "application/gzip"
            else -> "*/*"
        }
    }

    /**
     * Opens a document or file using the system's ACTION_VIEW intent via FileProvider.
     */
    fun openFile(context: Context, file: File, mimeType: String? = null) {
        try {
            if (!file.exists()) {
                Toast.makeText(context, "File does not exist: ${file.name}", Toast.LENGTH_SHORT).show()
                return
            }
            val authority = "${context.packageName}.provider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)
            val resolvedMime = mimeType ?: getMimeType(file.absolutePath)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, resolvedMime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Open file with"))
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Shares a file via the Android ACTION_SEND sharing sheet.
     */
    fun shareFile(context: Context, file: File, mimeType: String? = null, title: String = "Share Document") {
        try {
            if (!file.exists()) {
                Toast.makeText(context, "File does not exist: ${file.name}", Toast.LENGTH_SHORT).show()
                return
            }
            val authority = "${context.packageName}.provider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)
            val resolvedMime = mimeType ?: getMimeType(file.absolutePath)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = resolvedMime
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.nameWithoutExtension)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Generates a multi-page AI Study Guide or Lecture Summary PDF document.
     */
    fun generateStudyGuidePdf(
        context: Context,
        title: String,
        content: String,
        courseName: String? = null,
        courseCode: String? = null,
        instructor: String? = null,
        accentColorHex: String? = null
    ): File {
        val pdfDoc = PdfDocument()
        var pageNumber = 1
        var page = pdfDoc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val accentColor = try {
            AndroidColor.parseColor(accentColorHex ?: "#4F46E5")
        } catch (_: Exception) {
            AndroidColor.parseColor("#4F46E5")
        }

        // Draw First Page Header
        drawFirstPageHeader(
            canvas = canvas,
            paint = paint,
            accentColor = accentColor,
            docType = "STUDY GUIDE & LECTURE SUMMARY",
            mainTitle = title,
            subtitle = if (!courseName.isNullOrBlank()) "Course: $courseName" + (if (!courseCode.isNullOrBlank()) " ($courseCode)" else "") else "Lumia Scholar Document",
            instructor = instructor
        )

        var currentY = 175f
        val dateFormatted = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault()).format(Date())

        val lines = wrapText(content, 78)
        var i = 0
        while (i < lines.size) {
            if (currentY > BOTTOM_MARGIN) {
                // Draw Footer on finishing page
                drawPageFooter(canvas, paint, dateFormatted, pageNumber)
                pdfDoc.finishPage(page)

                pageNumber++
                page = pdfDoc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                canvas = page.canvas

                // Draw compact header for page 2+
                drawSubsequentPageHeader(canvas, paint, accentColor, title, courseName)
                currentY = 70f
            }

            val line = lines[i]
            when {
                line.startsWith("# ") -> {
                    currentY += 8f
                    paint.color = accentColor
                    paint.textSize = 15f
                    paint.isFakeBoldText = true
                    canvas.drawText(line.removePrefix("# ").trim(), MARGIN_LEFT, currentY, paint)
                    currentY += 22f
                }
                line.startsWith("## ") -> {
                    currentY += 6f
                    paint.color = AndroidColor.parseColor("#1E293B")
                    paint.textSize = 13f
                    paint.isFakeBoldText = true
                    canvas.drawText(line.removePrefix("## ").trim(), MARGIN_LEFT, currentY, paint)
                    currentY += 20f
                }
                line.startsWith("• ") || line.startsWith("- ") || line.startsWith("* ") -> {
                    paint.color = accentColor
                    canvas.drawCircle(MARGIN_LEFT + 6f, currentY - 4f, 2.5f, paint)
                    paint.color = AndroidColor.parseColor("#334155")
                    paint.textSize = 11f
                    paint.isFakeBoldText = false
                    val bulletText = line.substring(2).trim()
                    canvas.drawText(bulletText, MARGIN_LEFT + 16f, currentY, paint)
                    currentY += 17f
                }
                line.startsWith("[") && line.contains("]") -> {
                    // Tag line: e.g. [Formula] Newton's 2nd Law
                    val tag = line.substringAfter("[").substringBefore("]")
                    val rest = line.substringAfter("]").trim()

                    paint.color = accentColor
                    paint.textSize = 9f
                    paint.isFakeBoldText = true
                    val tagWidth = paint.measureText(tag) + 12f

                    val tagRect = RectF(MARGIN_LEFT, currentY - 11f, MARGIN_LEFT + tagWidth, currentY + 3f)
                    paint.color = AndroidColor.parseColor("#EEF2F6")
                    canvas.drawRoundRect(tagRect, 4f, 4f, paint)

                    paint.color = accentColor
                    canvas.drawText(tag, MARGIN_LEFT + 6f, currentY, paint)

                    paint.color = AndroidColor.parseColor("#1E293B")
                    paint.textSize = 11f
                    paint.isFakeBoldText = false
                    canvas.drawText(rest, MARGIN_LEFT + tagWidth + 8f, currentY, paint)
                    currentY += 18f
                }
                line.isBlank() -> {
                    currentY += 10f
                }
                else -> {
                    paint.color = AndroidColor.parseColor("#334155")
                    paint.textSize = 11f
                    paint.isFakeBoldText = false
                    canvas.drawText(line, MARGIN_LEFT, currentY, paint)
                    currentY += 16f
                }
            }
            i++
        }

        // Draw Footer on last page
        drawPageFooter(canvas, paint, dateFormatted, pageNumber)
        pdfDoc.finishPage(page)

        val outputDir = File(context.filesDir, "attachments").apply { if (!exists()) mkdirs() }
        val safeTitle = title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30)
        val file = File(outputDir, "Guide_${safeTitle}_${System.currentTimeMillis()}.pdf")
        val fos = FileOutputStream(file)
        pdfDoc.writeTo(fos)
        fos.flush()
        fos.close()
        pdfDoc.close()

        return file
    }

    /**
     * Generates a comprehensive, multi-page Course Summary Report PDF.
     * Contains formatted overview headers, attendance tables, assignments/task checklists, and study notes.
     */
    fun generateCourseSummaryPdf(
        context: Context,
        course: Course,
        attendanceRecords: List<AttendanceRecord>,
        assignments: List<PracticeAssignment>,
        tasks: List<Task>,
        notes: List<Note>,
        linkedSubjects: List<Subject> = emptyList()
    ): File {
        val pdfDoc = PdfDocument()
        var pageNumber = 1
        var page = pdfDoc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val accentColor = try {
            AndroidColor.parseColor(course.colorHex)
        } catch (_: Exception) {
            AndroidColor.parseColor("#3197D6")
        }

        val dateFormatted = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault()).format(Date())

        // 1. PAGE 1 HEADER
        drawFirstPageHeader(
            canvas = canvas,
            paint = paint,
            accentColor = accentColor,
            docType = "ACADEMIC COURSE DOSSIER",
            mainTitle = course.name,
            subtitle = "Course Code: ${course.code.ifBlank { "N/A" }} · Schedule: ${course.schedule.ifBlank { "TBD" }}",
            instructor = course.instructor.ifBlank { "Unassigned" }
        )

        var currentY = 160f

        fun checkPageBreak(requiredHeight: Float) {
            if (currentY + requiredHeight > BOTTOM_MARGIN) {
                drawPageFooter(canvas, paint, dateFormatted, pageNumber)
                pdfDoc.finishPage(page)

                pageNumber++
                page = pdfDoc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                canvas = page.canvas

                drawSubsequentPageHeader(canvas, paint, accentColor, "Course Summary: ${course.name}", course.code)
                currentY = 70f
            }
        }

        // 2. OVERVIEW METRICS PANEL
        checkPageBreak(90f)
        val cancelled = attendanceRecords.count { it.status.equals("Cancelled", ignoreCase = true) || it.status.equals("Holiday", ignoreCase = true) }
        val effectiveTotal = attendanceRecords.size - cancelled
        val presentCount = attendanceRecords.count { it.status.equals("Present", ignoreCase = true) }
        val lateCount = attendanceRecords.count { it.status.equals("Late", ignoreCase = true) }
        val absentCount = attendanceRecords.count { it.status.equals("Absent", ignoreCase = true) }
        val totalAttended = presentCount + lateCount
        val attendancePct = if (effectiveTotal > 0) ((totalAttended.toFloat() / effectiveTotal) * 100).roundToInt() else 100

        val completedAssignments = assignments.count { it.isCompleted }
        val completedTasks = tasks.count { it.isCompleted }

        // Draw metrics background card
        paint.color = AndroidColor.parseColor("#F8FAFC")
        val metricsRect = RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + 70f)
        canvas.drawRoundRect(metricsRect, 8f, 8f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = AndroidColor.parseColor("#E2E8F0")
        canvas.drawRoundRect(metricsRect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // 4 Key metrics
        val colWidth = CONTENT_WIDTH / 4f
        val metrics = listOf(
            Pair("Attendance Rate", "$attendancePct% ($totalAttended/$effectiveTotal)"),
            Pair("Assignments", "$completedAssignments / ${assignments.size} done"),
            Pair("Course Tasks", "$completedTasks / ${tasks.size} done"),
            Pair("Study Notes", "${notes.size} notes")
        )

        for (idx in metrics.indices) {
            val colX = MARGIN_LEFT + (idx * colWidth)
            val metric = metrics[idx]

            paint.color = AndroidColor.parseColor("#64748B")
            paint.textSize = 8.5f
            paint.isFakeBoldText = false
            canvas.drawText(metric.first, colX + 12f, currentY + 24f, paint)

            paint.color = if (idx == 0) {
                if (attendancePct >= 75) AndroidColor.parseColor("#10B981") else AndroidColor.parseColor("#EF4444")
            } else {
                AndroidColor.parseColor("#0F172A")
            }
            paint.textSize = 12f
            paint.isFakeBoldText = true
            canvas.drawText(metric.second, colX + 12f, currentY + 48f, paint)
        }

        currentY += 86f

        // 3. ATTENDANCE TABLE SECTION
        checkPageBreak(50f)
        drawSectionHeader(canvas, paint, accentColor, "1. ATTENDANCE LOGS & PARTICIPATION", currentY)
        currentY += 24f

        if (attendanceRecords.isEmpty()) {
            paint.color = AndroidColor.parseColor("#64748B")
            paint.textSize = 10.5f
            paint.isFakeBoldText = false
            canvas.drawText("No attendance logs recorded yet for this course.", MARGIN_LEFT + 8f, currentY + 12f, paint)
            currentY += 28f
        } else {
            // Attendance Summary Pills
            checkPageBreak(28f)
            paint.textSize = 9.5f
            paint.isFakeBoldText = true

            val pillItems = listOf(
                Triple("Present: $presentCount", "#10B981", "#ECFDF5"),
                Triple("Late: $lateCount", "#F59E0B", "#FFFBEB"),
                Triple("Absent: $absentCount", "#EF4444", "#FEF2F2"),
                Triple("Cancelled/Holiday: $cancelled", "#6B7280", "#F3F4F6")
            )

            var pillX = MARGIN_LEFT
            for (pill in pillItems) {
                val pillText = pill.first
                val textColor = AndroidColor.parseColor(pill.second)
                val bgColor = AndroidColor.parseColor(pill.third)
                val pWidth = paint.measureText(pillText) + 16f

                paint.color = bgColor
                canvas.drawRoundRect(RectF(pillX, currentY, pillX + pWidth, currentY + 18f), 6f, 6f, paint)
                paint.color = textColor
                canvas.drawText(pillText, pillX + 8f, currentY + 12.5f, paint)
                pillX += pWidth + 8f
            }
            currentY += 26f

            // Table Header
            checkPageBreak(24f)
            paint.color = AndroidColor.parseColor("#F1F5F9")
            canvas.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + 20f, paint)
            paint.color = AndroidColor.parseColor("#334155")
            paint.textSize = 9.5f
            paint.isFakeBoldText = true
            canvas.drawText("DATE & TIME", MARGIN_LEFT + 10f, currentY + 13.5f, paint)
            canvas.drawText("STATUS", MARGIN_LEFT + 180f, currentY + 13.5f, paint)
            canvas.drawText("SESSION TYPE / REMARKS", MARGIN_LEFT + 310f, currentY + 13.5f, paint)
            currentY += 22f

            val sortedAttendance = attendanceRecords.sortedByDescending { it.dateMillis }
            val dateFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())

            for (rec in sortedAttendance.take(25)) {
                checkPageBreak(18f)
                val dateStr = dateFormat.format(Date(rec.dateMillis))

                paint.color = AndroidColor.parseColor("#1E293B")
                paint.textSize = 9.5f
                paint.isFakeBoldText = false
                canvas.drawText(dateStr, MARGIN_LEFT + 10f, currentY + 11f, paint)

                val statusColor = when (rec.status.lowercase(Locale.ROOT)) {
                    "present" -> AndroidColor.parseColor("#10B981")
                    "late" -> AndroidColor.parseColor("#F59E0B")
                    "absent" -> AndroidColor.parseColor("#EF4444")
                    else -> AndroidColor.parseColor("#64748B")
                }
                paint.color = statusColor
                paint.isFakeBoldText = true
                canvas.drawText(rec.status, MARGIN_LEFT + 180f, currentY + 11f, paint)

                paint.color = AndroidColor.parseColor("#64748B")
                paint.isFakeBoldText = false
                canvas.drawText("Standard Lecture Session", MARGIN_LEFT + 310f, currentY + 11f, paint)

                // Row bottom separator
                paint.color = AndroidColor.parseColor("#F1F5F9")
                canvas.drawLine(MARGIN_LEFT, currentY + 16f, MARGIN_RIGHT, currentY + 16f, paint)
                currentY += 18f
            }
            if (sortedAttendance.size > 25) {
                checkPageBreak(16f)
                paint.color = AndroidColor.parseColor("#94A3B8")
                paint.textSize = 8.5f
                canvas.drawText("... and ${sortedAttendance.size - 25} earlier attendance records", MARGIN_LEFT + 10f, currentY + 10f, paint)
                currentY += 18f
            }
            currentY += 10f
        }

        // 4. ASSIGNMENT CHECKLIST SECTION
        checkPageBreak(50f)
        drawSectionHeader(canvas, paint, accentColor, "2. PRACTICE ASSIGNMENTS & DELIVERABLES", currentY)
        currentY += 24f

        if (assignments.isEmpty()) {
            paint.color = AndroidColor.parseColor("#64748B")
            paint.textSize = 10.5f
            paint.isFakeBoldText = false
            canvas.drawText("No assignments scheduled for this course.", MARGIN_LEFT + 8f, currentY + 12f, paint)
            currentY += 26f
        } else {
            val dueDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            for (assignment in assignments) {
                checkPageBreak(28f)

                // Draw Checkbox
                val isDone = assignment.isCompleted
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1.2f
                paint.color = if (isDone) AndroidColor.parseColor("#10B981") else AndroidColor.parseColor("#94A3B8")
                val boxRect = RectF(MARGIN_LEFT + 4f, currentY + 1f, MARGIN_LEFT + 16f, currentY + 13f)
                canvas.drawRoundRect(boxRect, 3f, 3f, paint)
                paint.style = Paint.Style.FILL

                if (isDone) {
                    paint.color = AndroidColor.parseColor("#10B981")
                    canvas.drawRoundRect(RectF(MARGIN_LEFT + 6f, currentY + 3f, MARGIN_LEFT + 14f, currentY + 11f), 2f, 2f, paint)
                }

                // Assignment Title
                paint.color = if (isDone) AndroidColor.parseColor("#94A3B8") else AndroidColor.parseColor("#0F172A")
                paint.textSize = 10.5f
                paint.isFakeBoldText = true
                canvas.drawText(assignment.title, MARGIN_LEFT + 24f, currentY + 11f, paint)

                // Category Badge
                val catTag = "[${assignment.category}]"
                paint.textSize = 8.5f
                paint.color = accentColor
                paint.isFakeBoldText = false
                val catX = MARGIN_LEFT + 24f + paint.measureText(assignment.title) + 12f
                if (catX < MARGIN_RIGHT - 130f) {
                    canvas.drawText(catTag, catX, currentY + 11f, paint)
                }

                // Due Date
                val dueStr = if (assignment.dueDateMillis > 0) "Due: ${dueDateFormat.format(Date(assignment.dueDateMillis))}" else "No Due Date"
                paint.color = AndroidColor.parseColor("#64748B")
                paint.textSize = 9f
                paint.isFakeBoldText = false
                val dueWidth = paint.measureText(dueStr)
                canvas.drawText(dueStr, MARGIN_RIGHT - dueWidth - 4f, currentY + 11f, paint)

                currentY += 20f
            }
            currentY += 8f
        }

        // 5. TASKS CHECKLIST SECTION
        if (tasks.isNotEmpty()) {
            checkPageBreak(50f)
            drawSectionHeader(canvas, paint, accentColor, "3. COURSE STUDY TASKS & ACTIONS", currentY)
            currentY += 24f

            for (task in tasks) {
                checkPageBreak(22f)
                val isDone = task.isCompleted

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1.2f
                paint.color = if (isDone) AndroidColor.parseColor("#10B981") else AndroidColor.parseColor("#94A3B8")
                val boxRect = RectF(MARGIN_LEFT + 4f, currentY + 1f, MARGIN_LEFT + 16f, currentY + 13f)
                canvas.drawRoundRect(boxRect, 3f, 3f, paint)
                paint.style = Paint.Style.FILL

                if (isDone) {
                    paint.color = AndroidColor.parseColor("#10B981")
                    canvas.drawRoundRect(RectF(MARGIN_LEFT + 6f, currentY + 3f, MARGIN_LEFT + 14f, currentY + 11f), 2f, 2f, paint)
                }

                paint.color = if (isDone) AndroidColor.parseColor("#94A3B8") else AndroidColor.parseColor("#1E293B")
                paint.textSize = 10f
                paint.isFakeBoldText = false
                canvas.drawText(task.title, MARGIN_LEFT + 24f, currentY + 11f, paint)

                currentY += 18f
            }
            currentY += 8f
        }

        // 6. NOTES HIGHLIGHTS SECTION
        if (notes.isNotEmpty()) {
            checkPageBreak(50f)
            drawSectionHeader(canvas, paint, accentColor, "4. KEY NOTES & LECTURE HIGHLIGHTS", currentY)
            currentY += 24f

            for (note in notes) {
                checkPageBreak(36f)
                val noteTag = if (note.tag.isNotBlank()) "[${note.tag}]" else "[Note]"

                paint.color = accentColor
                paint.textSize = 9.5f
                paint.isFakeBoldText = true
                canvas.drawText(noteTag, MARGIN_LEFT + 4f, currentY + 10f, paint)

                paint.color = AndroidColor.parseColor("#334155")
                paint.textSize = 10f
                paint.isFakeBoldText = false

                val noteWrapped = wrapText(note.content, 70)
                var noteY = currentY + 10f
                val noteTagWidth = paint.measureText(noteTag) + 8f

                for (nIdx in noteWrapped.indices) {
                    val line = noteWrapped[nIdx]
                    if (nIdx == 0) {
                        canvas.drawText(line, MARGIN_LEFT + 4f + noteTagWidth, noteY, paint)
                    } else {
                        noteY += 14f
                        checkPageBreak(16f)
                        canvas.drawText(line, MARGIN_LEFT + 4f, noteY, paint)
                    }
                }
                currentY = noteY + 16f
            }
        }

        // Draw Footer on last page
        drawPageFooter(canvas, paint, dateFormatted, pageNumber)
        pdfDoc.finishPage(page)

        val outputDir = File(context.filesDir, "attachments").apply { if (!exists()) mkdirs() }
        val safeCourse = course.name.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(25)
        val file = File(outputDir, "Summary_${safeCourse}_${System.currentTimeMillis()}.pdf")
        val fos = FileOutputStream(file)
        pdfDoc.writeTo(fos)
        fos.flush()
        fos.close()
        pdfDoc.close()

        return file
    }

    /**
     * Generates a multi-page Subject Syllabus & Outline PDF.
     */
    fun generateSubjectOutlinePdf(
        context: Context,
        subject: Subject,
        chapters: List<Chapter>,
        topics: List<Topic>,
        notes: List<Note>
    ): File {
        val pdfDoc = PdfDocument()
        var pageNumber = 1
        var page = pdfDoc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val accentColor = AndroidColor.parseColor("#4F46E5")
        val dateFormatted = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault()).format(Date())

        drawFirstPageHeader(
            canvas = canvas,
            paint = paint,
            accentColor = accentColor,
            docType = "SUBJECT SYLLABUS & CURRICULUM OUTLINE",
            mainTitle = subject.name,
            subtitle = "Chapters: ${chapters.size} · Topics: ${topics.size}",
            instructor = "Lumia Scholar Curriculum"
        )

        var currentY = 160f

        fun checkPageBreak(requiredHeight: Float) {
            if (currentY + requiredHeight > BOTTOM_MARGIN) {
                drawPageFooter(canvas, paint, dateFormatted, pageNumber)
                pdfDoc.finishPage(page)

                pageNumber++
                page = pdfDoc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                canvas = page.canvas

                drawSubsequentPageHeader(canvas, paint, accentColor, "Subject Outline: ${subject.name}", null)
                currentY = 70f
            }
        }

        drawSectionHeader(canvas, paint, accentColor, "CURRICULUM BREAKDOWN & CHAPTER TOPICS", currentY)
        currentY += 24f

        if (chapters.isEmpty() && topics.isEmpty()) {
            paint.color = AndroidColor.parseColor("#64748B")
            paint.textSize = 10.5f
            canvas.drawText("No chapters or topics added yet for this subject.", MARGIN_LEFT + 8f, currentY + 12f, paint)
            currentY += 26f
        } else {
            for (chapter in chapters) {
                checkPageBreak(30f)
                paint.color = accentColor
                paint.textSize = 12f
                paint.isFakeBoldText = true
                canvas.drawText("Chapter: ${chapter.name}", MARGIN_LEFT + 4f, currentY + 12f, paint)
                currentY += 20f

                val chapterTopics = topics.filter { it.chapterId == chapter.id }
                for (topic in chapterTopics) {
                    checkPageBreak(18f)
                    val isDone = topic.isCompleted

                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 1.2f
                    paint.color = if (isDone) AndroidColor.parseColor("#10B981") else AndroidColor.parseColor("#94A3B8")
                    val boxRect = RectF(MARGIN_LEFT + 14f, currentY + 1f, MARGIN_LEFT + 24f, currentY + 11f)
                    canvas.drawRoundRect(boxRect, 2f, 2f, paint)
                    paint.style = Paint.Style.FILL

                    if (isDone) {
                        paint.color = AndroidColor.parseColor("#10B981")
                        canvas.drawRoundRect(RectF(MARGIN_LEFT + 16f, currentY + 3f, MARGIN_LEFT + 22f, currentY + 9f), 1f, 1f, paint)
                    }

                    paint.color = if (isDone) AndroidColor.parseColor("#94A3B8") else AndroidColor.parseColor("#1E293B")
                    paint.textSize = 10f
                    paint.isFakeBoldText = false
                    canvas.drawText(topic.title, MARGIN_LEFT + 32f, currentY + 10f, paint)
                    currentY += 16f
                }
                currentY += 8f
            }

            // Unassigned topics
            val unassignedTopics = topics.filter { it.chapterId == null }
            if (unassignedTopics.isNotEmpty()) {
                checkPageBreak(30f)
                paint.color = AndroidColor.parseColor("#475569")
                paint.textSize = 11.5f
                paint.isFakeBoldText = true
                canvas.drawText("Additional Core Topics", MARGIN_LEFT + 4f, currentY + 12f, paint)
                currentY += 20f

                for (topic in unassignedTopics) {
                    checkPageBreak(18f)
                    val isDone = topic.isCompleted

                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 1.2f
                    paint.color = if (isDone) AndroidColor.parseColor("#10B981") else AndroidColor.parseColor("#94A3B8")
                    val boxRect = RectF(MARGIN_LEFT + 14f, currentY + 1f, MARGIN_LEFT + 24f, currentY + 11f)
                    canvas.drawRoundRect(boxRect, 2f, 2f, paint)
                    paint.style = Paint.Style.FILL

                    if (isDone) {
                        paint.color = AndroidColor.parseColor("#10B981")
                        canvas.drawRoundRect(RectF(MARGIN_LEFT + 16f, currentY + 3f, MARGIN_LEFT + 22f, currentY + 9f), 1f, 1f, paint)
                    }

                    paint.color = if (isDone) AndroidColor.parseColor("#94A3B8") else AndroidColor.parseColor("#1E293B")
                    paint.textSize = 10f
                    paint.isFakeBoldText = false
                    canvas.drawText(topic.title, MARGIN_LEFT + 32f, currentY + 10f, paint)
                    currentY += 16f
                }
            }
        }

        drawPageFooter(canvas, paint, dateFormatted, pageNumber)
        pdfDoc.finishPage(page)

        val outputDir = File(context.filesDir, "attachments").apply { if (!exists()) mkdirs() }
        val safeSubject = subject.name.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(25)
        val file = File(outputDir, "Syllabus_${safeSubject}_${System.currentTimeMillis()}.pdf")
        val fos = FileOutputStream(file)
        pdfDoc.writeTo(fos)
        fos.flush()
        fos.close()
        pdfDoc.close()

        return file
    }

    // --- HELPER DRAWING FUNCTIONS ---

    private fun drawFirstPageHeader(
        canvas: Canvas,
        paint: Paint,
        accentColor: Int,
        docType: String,
        mainTitle: String,
        subtitle: String,
        instructor: String?
    ) {
        // Gradient / Solid header background
        paint.color = accentColor
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 130f, paint)

        // Accent top bar
        paint.color = AndroidColor.parseColor("#0F172A")
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 6f, paint)

        // Doc Type pill/tag
        paint.color = AndroidColor.WHITE
        paint.textSize = 8.5f
        paint.isFakeBoldText = true
        canvas.drawText("LUMIA SCHOLAR · $docType", MARGIN_LEFT, 32f, paint)

        // Main Title
        paint.textSize = 20f
        paint.isFakeBoldText = true
        val safeTitle = if (mainTitle.length > 42) mainTitle.take(39) + "..." else mainTitle
        canvas.drawText(safeTitle, MARGIN_LEFT, 62f, paint)

        // Subtitle & Instructor
        paint.textSize = 11f
        paint.isFakeBoldText = false
        paint.color = AndroidColor.parseColor("#F1F5F9")
        canvas.drawText(subtitle, MARGIN_LEFT, 86f, paint)

        if (!instructor.isNullOrBlank()) {
            paint.color = AndroidColor.parseColor("#E2E8F0")
            paint.textSize = 10f
            canvas.drawText("Instructor: $instructor", MARGIN_LEFT, 106f, paint)
        }
    }

    private fun drawSubsequentPageHeader(
        canvas: Canvas,
        paint: Paint,
        accentColor: Int,
        title: String,
        code: String?
    ) {
        // Mini top header
        paint.color = accentColor
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 42f, paint)

        paint.color = AndroidColor.WHITE
        paint.textSize = 10.5f
        paint.isFakeBoldText = true
        val headerText = "$title ${if (!code.isNullOrBlank()) "($code)" else ""} — Continued"
        val safeHeader = if (headerText.length > 60) headerText.take(57) + "..." else headerText
        canvas.drawText(safeHeader, MARGIN_LEFT, 26f, paint)
    }

    private fun drawSectionHeader(
        canvas: Canvas,
        paint: Paint,
        accentColor: Int,
        title: String,
        y: Float
    ) {
        // Left vertical indicator bar
        paint.color = accentColor
        canvas.drawRoundRect(RectF(MARGIN_LEFT, y, MARGIN_LEFT + 4f, y + 14f), 2f, 2f, paint)

        // Header Title
        paint.color = AndroidColor.parseColor("#0F172A")
        paint.textSize = 11.5f
        paint.isFakeBoldText = true
        canvas.drawText(title, MARGIN_LEFT + 12f, y + 11.5f, paint)

        // Subtle underline
        paint.color = AndroidColor.parseColor("#E2E8F0")
        paint.strokeWidth = 1f
        canvas.drawLine(MARGIN_LEFT + 12f + paint.measureText(title) + 12f, y + 7f, MARGIN_RIGHT, y + 7f, paint)
    }

    private fun drawPageFooter(
        canvas: Canvas,
        paint: Paint,
        dateFormatted: String,
        pageNumber: Int
    ) {
        paint.color = AndroidColor.parseColor("#E2E8F0")
        paint.strokeWidth = 1f
        canvas.drawLine(MARGIN_LEFT, BOTTOM_MARGIN + 20f, MARGIN_RIGHT, BOTTOM_MARGIN + 20f, paint)

        paint.color = AndroidColor.parseColor("#94A3B8")
        paint.textSize = 8.5f
        paint.isFakeBoldText = false
        canvas.drawText("Generated with Lumia Scholar Tracker · $dateFormatted", MARGIN_LEFT, BOTTOM_MARGIN + 36f, paint)

        val pageStr = "Page $pageNumber"
        val pageStrWidth = paint.measureText(pageStr)
        canvas.drawText(pageStr, MARGIN_RIGHT - pageStrWidth, BOTTOM_MARGIN + 36f, paint)
    }

    private fun wrapText(text: String, maxCharsPerLine: Int): List<String> {
        val result = mutableListOf<String>()
        val paragraphs = text.split("\n")

        for (paragraph in paragraphs) {
            if (paragraph.isBlank()) {
                result.add("")
                continue
            }
            val words = paragraph.split(" ")
            var currentLine = StringBuilder()

            for (word in words) {
                if (currentLine.isEmpty()) {
                    currentLine.append(word)
                } else if (currentLine.length + 1 + word.length <= maxCharsPerLine) {
                    currentLine.append(" ").append(word)
                } else {
                    result.add(currentLine.toString())
                    currentLine = StringBuilder(word)
                }
            }
            if (currentLine.isNotEmpty()) {
                result.add(currentLine.toString())
            }
        }
        return result
    }
}
