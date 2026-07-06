package lumia.tracker.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingFlat
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import lumia.tracker.model.TestRecord
import lumia.tracker.model.Topic
import kotlin.math.max
import kotlin.math.min

@Composable
fun TestCornerCard(
    testRecords: List<TestRecord>,
    topics: List<Topic> = emptyList(),
    onAddTest: (TestRecord) -> Unit,
    onUpdateTest: (TestRecord) -> Unit,
    onDeleteTest: (TestRecord) -> Unit
) {
    var showAddTestDialog by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(32.dp),
        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Test Analysis", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
                BouncyIconButton(onClick = { showAddTestDialog = true }) {
                    Icon(Icons.Rounded.Add, "Add Test Record", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
            
            if (testRecords.isNotEmpty()) {
                val sortedRecords = testRecords.sortedBy { it.dateMillis }
                val percentages = sortedRecords.map { if (it.totalMarks > 0) (it.marksObtained / it.totalMarks) * 100f else 0f }
                val avgMarks = percentages.average().toFloat()
                val bestMark = percentages.maxOrNull() ?: 0f
                val latest = percentages.lastOrNull() ?: 0f
                val previous = if (percentages.size > 1) percentages[percentages.size - 2] else latest
                val trend = latest - previous

                Spacer(modifier = Modifier.height(16.dp))
                
                // Analytics Summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AnalyticsStatItem(label = "Average", value = "${avgMarks.toInt()}%")
                    AnalyticsStatItem(label = "Best", value = "${bestMark.toInt()}%")
                    
                    val trendColor = if (trend > 0) Color(0xFF2ECC71) else if (trend < 0) Color(0xFFE74C3C) else MaterialTheme.colorScheme.onTertiaryContainer
                    val trendIcon = if (trend > 0) Icons.Rounded.TrendingUp else if (trend < 0) Icons.Rounded.TrendingDown else Icons.Rounded.TrendingFlat
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Trend", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha=0.7f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(trendIcon, contentDescription = "Trend", tint = trendColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (trend > 0) "+" else ""}${trend.toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = trendColor
                            )
                        }
                    }
                }

                if (percentages.size > 1) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Progress Chart", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    val lineColor = MaterialTheme.colorScheme.primary
                    Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                        val maxScore = 100f
                        val minScore = 0f
                        val range = max(1f, maxScore - minScore)
                        
                        val pointSpacing = size.width / max(1, percentages.size - 1)
                        val chartHeight = size.height
                        
                        val path = Path()
                        val points = mutableListOf<Offset>()
                        
                        percentages.forEachIndexed { index, pct ->
                            val x = index * pointSpacing
                            val y = chartHeight - (pct / range) * chartHeight
                            
                            val point = Offset(x, y)
                            points.add(point)
                            
                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }
                        
                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                        
                        points.forEach { point ->
                            drawCircle(
                                color = lineColor,
                                radius = 4.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Recent Tests", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(sortedRecords.reversed()) { test ->
                        var showEditDialog by remember { mutableStateOf(false) }
                        GlassCard(
                            modifier = Modifier.width(200.dp).clickable { showEditDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(test.title, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${test.marksObtained} / ${test.totalMarks}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                
                                val pct = if (test.totalMarks > 0) (test.marksObtained / test.totalMarks * 100) else 0f
                                val pctColor = if (pct >= 80) Color(0xFF2ECC71) else if (pct >= 50) Color(0xFFF1C40F) else Color(0xFFE74C3C)
                                LinearProgressIndicator(
                                    progress = if (test.totalMarks > 0) (test.marksObtained / test.totalMarks) else 0f,
                                    modifier = Modifier.fillMaxWidth().height(4.dp).padding(top = 8.dp),
                                    color = pctColor,
                                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )

                                // Associated study concept tag
                                val linkedTopic = topics.find { it.id == test.topicId }
                                if (linkedTopic != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Category,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = linkedTopic.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // Interactive metadata tags
                                if (test.tags.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        test.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(2).forEach { tag ->
                                            Box(
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = tag,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }

                                if (test.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(test.notes, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        if (showEditDialog) {
                            AddEditTestRecordDialog(
                                testToEdit = test,
                                topics = topics,
                                onDismiss = { showEditDialog = false },
                                onSave = { updated -> onUpdateTest(updated); showEditDialog = false },
                                onDelete = { onDeleteTest(test); showEditDialog = false }
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Text("No tests recorded yet. Add one!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha=0.6f))
            }
        }
    }

    if (showAddTestDialog) {
        AddEditTestRecordDialog(
            testToEdit = null,
            topics = topics,
            onDismiss = { showAddTestDialog = false },
            onSave = { newTest ->
                onAddTest(newTest)
                showAddTestDialog = false
            },
            onDelete = {}
        )
    }
}

@Composable
fun AnalyticsStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha=0.7f))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTestRecordDialog(
    testToEdit: TestRecord?,
    topics: List<Topic> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (TestRecord) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember(testToEdit) { mutableStateOf(testToEdit?.title ?: "") }
    var marksObtained by remember(testToEdit) { mutableStateOf(testToEdit?.marksObtained?.toString() ?: "0") }
    var totalMarks by remember(testToEdit) { mutableStateOf(testToEdit?.totalMarks?.toString() ?: "100") }
    var notes by remember(testToEdit) { mutableStateOf(testToEdit?.notes ?: "") }
    var tags by remember(testToEdit) { mutableStateOf(testToEdit?.tags ?: "") }
    var selectedTopicId by remember(testToEdit) { mutableStateOf<Int?>(testToEdit?.topicId) }

    var titleError by remember { mutableStateOf<String?>(null) }
    var marksObtainedError by remember { mutableStateOf<String?>(null) }
    var totalMarksError by remember { mutableStateOf<String?>(null) }

    var topicDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (testToEdit == null) "Add Test Record" else "Edit Test Record") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { 
                        title = it
                        if (it.isNotBlank()) titleError = null
                    },
                    label = { Text("Test Name (e.g. Midterm 1)") },
                    isError = titleError != null,
                    supportingText = titleError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = marksObtained,
                        onValueChange = { 
                            marksObtained = it
                            marksObtainedError = null
                        },
                        label = { Text("Marks") },
                        isError = marksObtainedError != null,
                        supportingText = marksObtainedError?.let { { Text(it) } },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = totalMarks,
                        onValueChange = { 
                            totalMarks = it
                            totalMarksError = null
                        },
                        label = { Text("Total") },
                        isError = totalMarksError != null,
                        supportingText = totalMarksError?.let { { Text(it) } },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated, e.g. final, unit-1)") },
                    placeholder = { Text("exam, unit-2, tricky") },
                    leadingIcon = { Icon(Icons.Rounded.Label, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                if (topics.isNotEmpty()) {
                    val currentSelection = topics.find { it.id == selectedTopicId }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = currentSelection?.title ?: "No associated concept (General)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assessed Concept / Topic") },
                            leadingIcon = { Icon(Icons.Rounded.Category, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { topicDropdownExpanded = !topicDropdownExpanded }) {
                                    Icon(
                                        imageVector = if (topicDropdownExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = "Expand options"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { topicDropdownExpanded = true }
                        )
                        DropdownMenu(
                            expanded = topicDropdownExpanded,
                            onDismissRequest = { topicDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("No associated concept (General)") },
                                onClick = {
                                    selectedTopicId = null
                                    topicDropdownExpanded = false
                                }
                            )
                            topics.forEach { topic ->
                                DropdownMenuItem(
                                    text = { Text(topic.title) },
                                    onClick = {
                                        selectedTopicId = topic.id
                                        topicDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Reflection / Improvement Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            BouncyTextButton(onClick = {
                var hasError = false
                
                if (title.isBlank()) {
                    titleError = "Title is required"
                    hasError = true
                } else {
                    titleError = null
                }
                
                val obtainedVal = marksObtained.trim().toFloatOrNull()
                if (obtainedVal == null) {
                    marksObtainedError = "Invalid number"
                    hasError = true
                } else if (obtainedVal < 0f) {
                    marksObtainedError = "Cannot be negative"
                    hasError = true
                } else {
                    marksObtainedError = null
                }
                
                val totalVal = totalMarks.trim().toFloatOrNull()
                if (totalVal == null) {
                    totalMarksError = "Invalid number"
                    hasError = true
                } else if (totalVal <= 0f) {
                    totalMarksError = "Must be > 0"
                    hasError = true
                } else {
                    totalMarksError = null
                }
                
                if (obtainedVal != null && totalVal != null && obtainedVal > totalVal) {
                    marksObtainedError = "Cannot exceed Total"
                    hasError = true
                }
                
                if (!hasError && obtainedVal != null && totalVal != null) {
                    val record = testToEdit?.copy(
                        title = title.trim(),
                        marksObtained = obtainedVal,
                        totalMarks = totalVal,
                        notes = notes.trim(),
                        tags = tags.trim(),
                        topicId = selectedTopicId
                    ) ?: TestRecord(
                        title = title.trim(),
                        marksObtained = obtainedVal,
                        totalMarks = totalVal,
                        notes = notes.trim(),
                        tags = tags.trim(),
                        topicId = selectedTopicId,
                        dateMillis = System.currentTimeMillis()
                    )
                    onSave(record)
                }
            }) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (testToEdit != null) {
                    BouncyTextButton(onClick = onDelete) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                }
                BouncyTextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
