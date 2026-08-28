package lumia.tracker.ui.screens.study.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import lumia.tracker.model.Course
import lumia.tracker.model.PomodoroSession
import lumia.tracker.ui.components.ScholarCard

@Composable
fun FocusTimePerCourseChart(
    modifier: Modifier = Modifier,
    sessions: List<PomodoroSession>,
    courses: List<Course>
) {
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
                text = "Focus by Course",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No focus sessions recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val durationByCourse = sessions
                    .groupBy { it.courseId }
                    .map { (courseId, list) ->
                        val duration = list.sumOf { it.durationMinutes }
                        val courseName = if (courseId != null) {
                            courses.find { it.id == courseId }?.name ?: "Course"
                        } else {
                            "Independent Study"
                        }
                        courseName to duration
                    }
                    .sortedByDescending { it.second }
                    .take(5)

                val maxDuration = durationByCourse.maxOf { it.second }.coerceAtLeast(1).toFloat()

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    durationByCourse.forEach { (name, mins) ->
                        val fraction = mins / maxDuration
                        val formattedTime = if (mins >= 60) {
                            val h = mins / 60
                            val m = mins % 60
                            if (m > 0) "${h}h ${m}m" else "${h}h"
                        } else {
                            "${mins}m"
                        }

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp)
                                )
                                Text(
                                    text = formattedTime,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        RoundedCornerShape(4.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction)
                                        .height(8.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
