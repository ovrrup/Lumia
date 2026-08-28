package lumia.tracker.ui.screens.study.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import lumia.tracker.model.Task
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.util.getTagColors
import lumia.tracker.viewmodel.ScholarViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskItemCard(
    task: Task,
    viewModel: ScholarViewModel,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController? = null
) {
    val isCompleted = task.isCompleted
    val contentAlpha by animateFloatAsState(
        targetValue = if (isCompleted) 0.55f else 1.0f,
        label = "taskAlpha"
    )

    ScholarCard(
        onClick = onEdit,
        modifier = modifier
            .fillMaxWidth()
            .alpha(contentAlpha),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated Checkbox Circle Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { viewModel.toggleTaskCompleted(task) },
                contentAlignment = Alignment.Center
            ) {
                val checkColor by animateColorAsState(
                    targetValue = if (isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "checkBg"
                )
                val borderColor = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(checkColor, CircleShape)
                        .border(1.5.dp, borderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Task Body
            Column(modifier = Modifier.weight(1f)) {
                // Title and Priority Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Priority Badge
                    if (task.priority > 0) {
                        Spacer(Modifier.width(6.dp))
                        val (pText, pBg, pTint) = when (task.priority) {
                            2 -> Triple("High", MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f), MaterialTheme.colorScheme.error)
                            1 -> Triple("Med", MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f), MaterialTheme.colorScheme.secondary)
                            else -> Triple("Low", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = pBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(pTint, CircleShape)
                                )
                                Text(
                                    text = pText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = pTint
                                )
                            }
                        }
                    }
                }

                // Description
                if (task.description.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Due Date
                if (task.dueDateMillis != null) {
                    Spacer(Modifier.height(4.dp))
                    val isOverdue = task.dueDateMillis < System.currentTimeMillis() && !isCompleted
                    val df = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
                    val dateFormatted = df.format(Date(task.dueDateMillis))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isOverdue) Icons.Rounded.EventBusy else Icons.Rounded.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isOverdue) "Overdue • $dateFormatted" else dateFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }

                // Tags Chips
                if (task.tags.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        task.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { tag ->
                            val colors = getTagColors(tag)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = colors.first,
                                modifier = Modifier.clickable {
                                    navController?.navigate("tags_hub?selectedTag=$tag")
                                }
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.second,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                // Linked Entities Row (clean text without redundant link icon)
                if (task.subjectId != null || task.courseId != null || task.assignmentId != null) {
                    Spacer(Modifier.height(4.dp))
                    val linkText = listOfNotNull(
                        if (task.subjectId != null) "Subject" else null,
                        if (task.courseId != null) "Course" else null,
                        if (task.assignmentId != null) "Assignment" else null
                    ).joinToString(", ")
                    Text(
                        text = "$linkText linked",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Delete Action Button
            BouncyIconButton(
                onClick = { viewModel.deleteTask(task) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete Task",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}
