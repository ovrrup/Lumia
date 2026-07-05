package lumia.tracker.ui.screens

import androidx.navigation.NavController

import lumia.tracker.ui.theme.liquidGlass
import lumia.tracker.ui.theme.glassBar
import android.text.format.DateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.viewmodel.ScholarViewModel
import java.util.Date
import androidx.compose.material3.IconButtonDefaults
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.BouncyFloatingActionButton
import java.util.Calendar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.remember

@Composable
fun PomodoroHeatmapChart(
    modifier: Modifier = Modifier,
    sessions: List<lumia.tracker.model.PomodoroSession>,
    backgroundColor: Color,
    primaryColor: Color
) {
    var calendarForMonth by androidx.compose.runtime.remember { 
        androidx.compose.runtime.mutableStateOf(
            Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
        ) 
    }
    
    val currentYear = calendarForMonth.get(Calendar.YEAR)
    val currentMonth = calendarForMonth.get(Calendar.MONTH)
    
    val monthName = DateFormat.format("MMMM yyyy", calendarForMonth.time).toString()

    // Aggregate sessions for the selected month
    val daysInMonth = calendarForMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val dailyDurations = IntArray(daysInMonth + 1) { 0 }
    
    sessions.forEach { session ->
        val sessionCal = Calendar.getInstance().apply { timeInMillis = session.dateMillis }
        if (sessionCal.get(Calendar.YEAR) == currentYear && sessionCal.get(Calendar.MONTH) == currentMonth) {
            val day = sessionCal.get(Calendar.DAY_OF_MONTH)
            dailyDurations[day] += session.durationMinutes
        }
    }
    
    val maxDuration = dailyDurations.maxOrNull()?.takeIf { it > 0 } ?: 60 // fallback to 60 as max base

    lumia.tracker.ui.components.GlassCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Study Calendar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BouncyIconButton(
                        onClick = { 
                            val prevCal = calendarForMonth.clone() as Calendar
                            prevCal.set(Calendar.DAY_OF_MONTH, 1)
                            prevCal.add(Calendar.MONTH, -1)
                            calendarForMonth = prevCal
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("<", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    BouncyIconButton(
                        onClick = { 
                            val nextCal = calendarForMonth.clone() as Calendar
                            nextCal.set(Calendar.DAY_OF_MONTH, 1)
                            nextCal.add(Calendar.MONTH, 1)
                            calendarForMonth = nextCal
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(">", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            val calFirstDay = calendarForMonth.clone() as Calendar
            calFirstDay.set(Calendar.DAY_OF_MONTH, 1)
            val firstDayOfWeek = calFirstDay.get(Calendar.DAY_OF_WEEK) - 1 // 0 (Sunday) to 6 (Saturday)
            
            // Labels for Days
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f),
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            // Grid
            val totalCells = daysInMonth + firstDayOfWeek
            val rows = (totalCells + 6) / 7
            
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val day = cellIndex - firstDayOfWeek + 1
                        
                        if (day in 1..daysInMonth) {
                            val duration = dailyDurations[day]
                            val intensity = if (duration == 0) 0.1f else {
                                0.3f + 0.7f * (duration.toFloat() / maxDuration.toFloat()).coerceAtMost(1f)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .background(
                                        if (duration == 0) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f) 
                                        else primaryColor.copy(alpha = intensity),
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (duration == 0) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            else MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Less", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(2.dp))
                Box(modifier = Modifier.size(10.dp).background(primaryColor.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(2.dp))
                Box(modifier = Modifier.size(10.dp).background(primaryColor.copy(alpha = 0.7f), RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(2.dp))
                Box(modifier = Modifier.size(10.dp).background(primaryColor, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("More", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
