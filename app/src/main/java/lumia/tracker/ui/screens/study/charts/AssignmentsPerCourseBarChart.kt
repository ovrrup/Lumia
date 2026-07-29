package lumia.tracker.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.setValue

@Composable
fun AssignmentsPerCourseBarChart(
    modifier: Modifier = Modifier,
    courses: List<lumia.tracker.model.Course>,
    assignments: List<lumia.tracker.model.PracticeAssignment>,
    backgroundColor: Color,
    barColor: Color
) {
    lumia.tracker.ui.components.GlassCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text(
                "Assignments per Course",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            val assignmentsCountMap = assignments.groupBy { it.courseId }.mapValues { it.value.size }
            val topCourses = courses
                .map { Pair(it.name, assignmentsCountMap[it.id] ?: 0) }
                .filter { it.second > 0 }
                .sortedByDescending { it.second }
                .take(5)
            
            if (topCourses.isEmpty()) {
                Text(
                    "No data available.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            val maxCount = topCourses.maxOf { it.second }.toFloat()

            // Draw horizontal bars
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                topCourses.forEach { (courseName, count) ->
                    val fraction = if (maxCount > 0) count / maxCount else 0f
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = courseName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).padding(end = 16.dp)
                            )
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .background(barColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .height(16.dp)
                                    .background(barColor, RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}
