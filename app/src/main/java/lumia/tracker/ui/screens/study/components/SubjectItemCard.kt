package lumia.tracker.ui.screens.study.components

import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.model.Subject
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * SubjectItemCard - Professional Subject Card with Topic Coverage Progress,
 * Linked Course Indicators, and Study Navigation.
 */
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
    val topicProgress = if (totalTopicsCount > 0) completedTopicsCount.toFloat() / totalTopicsCount else 0f

    val linkedCourses = remember(courses, subject) {
        courses.filter { course ->
            course.subjectId == subject.id ||
                    course.subjectIds.split(",").mapNotNull { it.trim().toIntOrNull() }.contains(subject.id)
        }
    }

    ScholarCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Subject Avatar, Title & Tags, Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subject.name.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (subject.tags.isNotBlank()) {
                        Text(
                            text = subject.tags,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box {
                    BouncyIconButton(onClick = { expanded = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Subject") },
                            onClick = {
                                expanded = false
                                viewModel.deleteSubject(subject)
                            },
                            leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }

            // Topic Coverage Progress Bar
            if (totalTopicsCount > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Syllabus Coverage",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$completedTopicsCount / $totalTopicsCount Topics (${(topicProgress * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { topicProgress },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // Connected Courses Chips
            if (linkedCourses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (course in linkedCourses) {
                        val courseColor = try {
                            Color(android.graphics.Color.parseColor(course.colorHex))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.secondary
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = courseColor.copy(alpha = 0.12f),
                            modifier = Modifier.clickable { onCourseClick(course.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).background(courseColor, CircleShape))
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
