package lumia.tracker.ui.screens.study.charts

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import lumia.tracker.model.PomodoroSession
import lumia.tracker.ui.components.ScholarCard
import java.util.Calendar

@Composable
fun PomodoroHeatmapChart(
    modifier: Modifier = Modifier,
    sessions: List<PomodoroSession>,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    primaryColor: Color = MaterialTheme.colorScheme.primary
) {
    var calendarForMonth by remember {
        mutableStateOf(
            Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
        )
    }

    val currentYear = calendarForMonth.get(Calendar.YEAR)
    val currentMonth = calendarForMonth.get(Calendar.MONTH)
    val monthName = DateFormat.format("MMMM yyyy", calendarForMonth.time).toString()

    val daysInMonth = calendarForMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val dailyDurations = IntArray(daysInMonth + 1) { 0 }

    sessions.forEach { session ->
        val sessionCal = Calendar.getInstance().apply { timeInMillis = session.dateMillis }
        if (sessionCal.get(Calendar.YEAR) == currentYear && sessionCal.get(Calendar.MONTH) == currentMonth) {
            val day = sessionCal.get(Calendar.DAY_OF_MONTH)
            dailyDurations[day] += session.durationMinutes
        }
    }

    val maxDuration = dailyDurations.maxOrNull()?.takeIf { it > 0 } ?: 60

    ScholarCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row with Month Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Focus Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            val prevCal = calendarForMonth.clone() as Calendar
                            prevCal.set(Calendar.DAY_OF_MONTH, 1)
                            prevCal.add(Calendar.MONTH, -1)
                            calendarForMonth = prevCal
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = {
                            val nextCal = calendarForMonth.clone() as Calendar
                            nextCal.set(Calendar.DAY_OF_MONTH, 1)
                            nextCal.add(Calendar.MONTH, 1)
                            calendarForMonth = nextCal
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Next Month",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val calFirstDay = calendarForMonth.clone() as Calendar
            calFirstDay.set(Calendar.DAY_OF_MONTH, 1)
            val firstDayOfWeek = calFirstDay.get(Calendar.DAY_OF_WEEK) - 1 // 0 (Sun) to 6 (Sat)

            // Day labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Calendar Heatmap Grid
            val totalCells = daysInMonth + firstDayOfWeek
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val day = cellIndex - firstDayOfWeek + 1

                        if (day in 1..daysInMonth) {
                            val duration = dailyDurations[day]
                            val intensity = if (duration == 0) 0f else {
                                0.35f + 0.65f * (duration.toFloat() / maxDuration.toFloat()).coerceAtMost(1f)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .background(
                                        color = if (duration == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        else primaryColor.copy(alpha = intensity),
                                        shape = RoundedCornerShape(6.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (duration == 0) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    else MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = if (duration > 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        } else {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Less",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.width(3.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(primaryColor.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(3.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(primaryColor.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(3.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(primaryColor, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "More",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
