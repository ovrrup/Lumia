package lumia.tracker.ui.screens.study.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import lumia.tracker.model.Task
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.components.ScholarCardDefaults
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.util.getTagColors
import lumia.tracker.viewmodel.ScholarViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@ValueScore(
    score = 92,
    importance = Importance.CRITICAL,
    description = "Reimagined ambient soft task item card with tactile check indicator, capsule metadata, and overflow actions",
    category = "Study"
)
@Composable
fun TaskItemCard(
    task: Task,
    viewModel: ScholarViewModel,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController? = null
) {
    val isCompleted = task.isCompleted
    val isDark = isSystemInDarkTheme()
    var showMenu by remember { mutableStateOf(false) }

    val contentAlpha by animateFloatAsState(
        targetValue = if (isCompleted) 0.50f else 1.0f,
        animationSpec = spring(stiffness = 300f),
        label = "taskAlpha"
    )
    val checkScale by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0f,
        animationSpec = spring(
            dampingRatio = spring.DampingRatioMediumBouncy,
            stiffness = spring.StiffnessMedium
        ),
        label = "taskCheckScale"
    )

    val cardBg = if (isCompleted) {
        MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = if (isDark) 0.40f else 0.70f)
    } else {
        ScholarCardDefaults.glassContainerColor(isDark)
    }

    ScholarCard(
        onClick = onEdit,
        modifier = modifier
            .fillMaxWidth()
            .alpha(contentAlpha),
        shape = RoundedCornerShape(22.dp),
        containerColor = cardBg,
        border = ScholarCardDefaults.border()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tactile Circular Check Indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { viewModel.toggleTaskCompleted(task) },
                contentAlignment = Alignment.Center
            ) {
                val checkBgColor by animateColorAsState(
                    targetValue = if (isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "checkBg"
                )
                val checkBorderColor by animateColorAsState(
                    targetValue = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                    label = "checkBorder"
                )

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(checkBgColor)
                        .border(1.75.dp, checkBorderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .size(15.dp)
                                .scale(checkScale)
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Main Content Body
            Column(modifier = Modifier.weight(1f)) {
                // Title
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Optional Description
                if (task.description.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Metadata Capsule Badges Row
                val hasDueDate = task.dueDateMillis != null
                val hasPriority = task.priority > 0
                val hasTags = task.tags.isNotBlank()
                val hasLinkage = task.subjectId != null || task.courseId != null || task.assignmentId != null

                if (hasDueDate || hasPriority || hasTags || hasLinkage) {
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Due Date Capsule Pill
                        if (task.dueDateMillis != null) {
                            val isOverdue = task.dueDateMillis < System.currentTimeMillis() && !isCompleted
                            val df = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
                            val dateFormatted = df.format(Date(task.dueDateMillis))
                            val dateBg = if (isOverdue) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                                         else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f)
                            val dateTint = if (isOverdue) MaterialTheme.colorScheme.error
                                           else MaterialTheme.colorScheme.onSurfaceVariant

                            Surface(
                                shape = CircleShape,
                                color = dateBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isOverdue) Icons.Rounded.EventBusy else Icons.Rounded.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = dateTint
                                    )
                                    Text(
                                        text = if (isOverdue) "Overdue • $dateFormatted" else dateFormatted,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        color = dateTint,
                                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Priority Capsule Pill
                        if (task.priority > 0) {
                            val (pText, pBg, pTint) = when (task.priority) {
                                2 -> Triple("High", MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f), MaterialTheme.colorScheme.error)
                                1 -> Triple("Medium", MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f), MaterialTheme.colorScheme.secondary)
                                else -> Triple("Low", MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f), MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(
                                shape = CircleShape,
                                color = pBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(pTint, CircleShape)
                                    )
                                    Text(
                                        text = pText,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = pTint
                                    )
                                }
                            }
                        }

                        // Linkage Capsule Pill
                        if (hasLinkage) {
                            val linkText = listOfNotNull(
                                if (task.subjectId != null) "Subject" else null,
                                if (task.courseId != null) "Course" else null,
                                if (task.assignmentId != null) "Assignment" else null
                            ).joinToString(" • ")

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.BookmarkBorder,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = linkText,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Tags Pills
                        if (hasTags) {
                            task.tags.split(",")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                                .forEach { tag ->
                                    val colors = getTagColors(tag)
                                    Surface(
                                        shape = CircleShape,
                                        color = colors.first.copy(alpha = 0.20f)
                                    ) {
                                        Text(
                                            text = "#$tag",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 11.sp,
                                            color = colors.second,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                        }
                    }
                }
            }

            Spacer(Modifier.width(4.dp))

            // Subdued Overflow Action Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Task") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Delete Task",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showMenu = false
                            viewModel.deleteTask(task)
                        }
                    )
                }
            }
        }
    }
}
