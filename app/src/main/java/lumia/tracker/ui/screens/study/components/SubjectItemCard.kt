package lumia.tracker.ui.screens.study.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.model.Subject
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.components.ScholarCardDefaults
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.study.dialogs.StudyDeleteConfirmationDialog
import lumia.tracker.ui.util.getTagColors
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * SubjectItemCard - Modern Academic Subject Card.
 * Features soft ambient card styling, monogram avatar squircle, circular completion gauge,
 * syllabus linear progress bar, and connected course capsules.
 */
@ValueScore(
    score = 90,
    importance = Importance.HIGH,
    description = "Subject overview card with syllabus coverage progress, linked course chips, and quick actions in ambient style",
    category = "Study"
)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubjectItemCard(
    subject: Subject,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    viewModel: ScholarViewModel,
    onCourseClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val allTopics by viewModel.allTopics.collectAsStateWithLifecycle(emptyList())

    val subjectTopics = remember(allTopics, subject.id) {
        allTopics.filter { it.subjectId == subject.id }
    }
    val completedTopicsCount = remember(subjectTopics) {
        subjectTopics.count { it.isCompleted }
    }
    val totalTopicsCount = subjectTopics.size
    val rawProgress = if (totalTopicsCount > 0) completedTopicsCount.toFloat() / totalTopicsCount else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(600),
        label = "topicProgress"
    )

    val linkedCourses = remember(courses, subject) {
        courses.filter { course ->
            course.subjectId == subject.id ||
                course.subjectIds.split(",").mapNotNull { it.trim().toIntOrNull() }.contains(subject.id)
        }
    }

    val progressColor = when {
        rawProgress >= 1f -> Color(0xFF10B981)
        rawProgress >= 0.5f -> MaterialTheme.colorScheme.tertiary
        rawProgress > 0f -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    }

    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) MaterialTheme.colorScheme.surfaceContainerLow
                 else MaterialTheme.colorScheme.surfaceContainerLowest

    ScholarCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        containerColor = cardBg,
        border = ScholarCardDefaults.border()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Subject Avatar, Title & Tags, Circular Progress Ring, Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = if (isDark) 0.35f else 0.45f),
                            RoundedCornerShape(13.dp)
                        )
                        .border(
                            0.5.dp,
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f),
                            RoundedCornerShape(13.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subject.name.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val tagsList = remember(subject.tags) {
                        subject.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    }
                    if (tagsList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            tagsList.take(3).forEach { tag ->
                                val (bgColor, textColor) = getTagColors(tag)
                                Surface(
                                    shape = CircleShape,
                                    color = bgColor.copy(alpha = 0.16f),
                                    border = BorderStroke(0.5.dp, textColor.copy(alpha = 0.30f))
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textColor,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Circular Completion Gauge
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
                        strokeWidth = 3.5.dp,
                        strokeCap = StrokeCap.Round
                    )
                    if (totalTopicsCount > 0 && rawProgress >= 1f) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Completed",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                    } else if (totalTopicsCount > 0) {
                        Text(
                            text = "${(rawProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (rawProgress > 0f) progressColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Options Menu
                Box {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        BouncyIconButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Subject") },
                            onClick = {
                                expanded = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Subject") },
                            onClick = {
                                expanded = false
                                showDeleteConfirmation = true
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Syllabus Progress Section
            if (totalTopicsCount > 0) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(progressColor, CircleShape)
                            )
                            Text(
                                text = if (rawProgress >= 1f) "Syllabus mastered" else "Curriculum coverage",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "$completedTopicsCount of $totalTopicsCount topics (${(rawProgress * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = progressColor
                        )
                    }

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
                        strokeCap = StrokeCap.Round
                    )
                }
            } else {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "No topics added yet • Tap to view curriculum",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Connected Courses Chips
            if (linkedCourses.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val fallbackColor = MaterialTheme.colorScheme.secondary
                    for (course in linkedCourses) {
                        val courseColor = remember(course.colorHex, fallbackColor) {
                            try {
                                Color(android.graphics.Color.parseColor(course.colorHex))
                            } catch (e: Exception) {
                                fallbackColor
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = courseColor.copy(alpha = 0.12f),
                            border = BorderStroke(0.5.dp, courseColor.copy(alpha = 0.30f)),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onCourseClick(course.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(courseColor, CircleShape)
                                )
                                Text(
                                    text = if (course.code.isNotBlank()) "${course.code}: ${course.name}" else course.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = courseColor,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        StudyDeleteConfirmationDialog(
            title = "Delete Subject",
            message = "Are you sure you want to delete ${subject.name}? All curriculum topics and study progress will be permanently removed.",
            onConfirmDelete = {
                showDeleteConfirmation = false
                viewModel.deleteSubject(subject)
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }
}
