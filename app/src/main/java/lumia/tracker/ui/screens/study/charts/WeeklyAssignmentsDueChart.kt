package lumia.tracker.ui.screens.study.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import lumia.tracker.model.Course
import lumia.tracker.model.PracticeAssignment
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.ScholarCard
import java.util.Calendar

@Composable
fun WeeklyAssignmentsDueChart(
    modifier: Modifier = Modifier,
    assignments: List<PracticeAssignment>,
    courses: List<Course>,
    onToggleCompletion: (PracticeAssignment) -> Unit
) {
    var selectedGroup by remember { mutableStateOf<Pair<String, List<PracticeAssignment>>?>(null) }

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfToday = calendar.timeInMillis
    val oneDayMillis = 24 * 60 * 60 * 1000L

    val validAssignments = assignments.filter { it.dueDateMillis > 0 }

    val groups = listOf(
        Triple("Past", "Past & Overdue", validAssignments.filter { it.dueDateMillis < startOfToday }),
        Triple("This Wk", "Due This Week", validAssignments.filter { it.dueDateMillis >= startOfToday && it.dueDateMillis < startOfToday + 7 * oneDayMillis }),
        Triple("Next Wk", "Due Next Week", validAssignments.filter { it.dueDateMillis >= startOfToday + 7 * oneDayMillis && it.dueDateMillis < startOfToday + 14 * oneDayMillis }),
        Triple("Wk 3", "Due in 2 Weeks", validAssignments.filter { it.dueDateMillis >= startOfToday + 14 * oneDayMillis && it.dueDateMillis < startOfToday + 21 * oneDayMillis }),
        Triple("Wk 4", "Due in 3 Weeks", validAssignments.filter { it.dueDateMillis >= startOfToday + 21 * oneDayMillis && it.dueDateMillis < startOfToday + 28 * oneDayMillis }),
        Triple("Later", "Scheduled Later", validAssignments.filter { it.dueDateMillis >= startOfToday + 28 * oneDayMillis })
    )

    val maxCount = groups.maxOf { it.third.size }.coerceAtLeast(1)

    ScholarCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Upcoming Deadlines",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Bars Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                groups.forEach { (shortLabel, fullTitle, weekList) ->
                    val total = weekList.size
                    val completed = weekList.count { it.isCompleted }
                    val pending = total - completed
                    val ratio = total.toFloat() / maxCount.toFloat()

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Fraction count label
                        if (total > 0) {
                            Text(
                                text = "$completed/$total",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (pending > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "·",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Bar Column
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(0.45f)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable(enabled = total > 0) {
                                    selectedGroup = fullTitle to weekList
                                },
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (total > 0) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(ratio)
                                ) {
                                    if (pending > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(pending.toFloat())
                                                .background(
                                                    if (shortLabel == "Past") MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
                                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                                )
                                        )
                                    }
                                    if (completed > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(completed.toFloat())
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Day/Week Short Label
                        Text(
                            text = shortLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Concise Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Due",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.45f), CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Overdue",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Modern Detail Dialog
    selectedGroup?.let { (title, weekList) ->
        Dialog(onDismissRequest = { selectedGroup = null }) {
            ScholarCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${weekList.size} assignments",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (weekList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No assignments due.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(weekList, key = { it.id }) { assignment ->
                                val course = courses.find { it.id == assignment.courseId }
                                val courseName = if (course != null) {
                                    if (course.code.isNotBlank()) "[${course.code}]" else course.name
                                } else "General"

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = assignment.isCompleted,
                                        onCheckedChange = { onToggleCompletion(assignment) },
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = assignment.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textDecoration = if (assignment.isCompleted) TextDecoration.LineThrough else null,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = courseName,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "• ${assignment.category}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    BouncyButton(
                        onClick = { selectedGroup = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
