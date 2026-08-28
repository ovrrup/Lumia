package lumia.tracker.ui.screens.study.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.model.Subject
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.util.getTagColors
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * SubjectItemCard - Modern Subject Card with Topic Coverage Progress,
 * Linked Course Indicators, Syllabus Count, and Smooth Navigation.
 */
@ValueScore(
    score = 86,
    importance = Importance.HIGH,
    description = "Subject overview card with syllabus coverage progress, linked course chips, and quick actions",
    category = "Study"
)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubjectItemCard(
    subject: Subject,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    viewModel: ScholarViewModel,
    onCourseClick: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
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
    val animatedProgress by animateFloatAsState(targetValue = rawProgress, label = "topicProgress")

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
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    ScholarCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Subject Avatar, Title & Tags, Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(12.dp)),
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
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            tagsList.take(2).forEach { tag ->
                                val (bgColor, textColor) = getTagColors(tag)
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = bgColor
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textColor,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Options Menu
                Box {
                    BouncyIconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
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
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Subject") },
                            onClick = {
                                expanded = false
                                viewModel.deleteSubject(subject)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            // Topic Coverage Progress Bar
            Spacer(modifier = Modifier.height(12.dp))
            if (totalTopicsCount > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Syllabus Coverage",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$completedTopicsCount/$totalTopicsCount (${(rawProgress * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = progressColor
                        )
                    }
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "No syllabus topics yet",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Connected Courses Chips
            if (linkedCourses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (course in linkedCourses) {
                        val courseColor = try {
                            Color(android.graphics.Color.parseColor(course.colorHex))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.secondary
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = courseColor.copy(alpha = 0.12f),
                            modifier = Modifier.clickable { onCourseClick(course.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .background(courseColor, CircleShape)
                                )
                                Text(
                                    text = if (course.code.isNotBlank()) "${course.code}: ${course.name}" else course.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = courseColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
