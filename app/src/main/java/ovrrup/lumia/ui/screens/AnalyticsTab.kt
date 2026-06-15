package ovrrup.lumia.ui.screens

import ovrrup.lumia.ui.theme.liquidGlass
import ovrrup.lumia.ui.theme.glassBar
import android.text.format.DateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
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
import ovrrup.lumia.viewmodel.ScholarViewModel
import java.util.Date
import androidx.compose.material3.IconButtonDefaults
import ovrrup.lumia.ui.components.BouncyIconButton
import ovrrup.lumia.ui.components.BouncyButton
import ovrrup.lumia.ui.components.BouncyTextButton
import ovrrup.lumia.ui.components.BouncyFloatingActionButton
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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsTab(viewModel: ScholarViewModel, paddingValues: PaddingValues) {
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val actionLogs by viewModel.actionLogs.collectAsStateWithLifecycle()
    val pomodoroSessions by viewModel.pomodoroSessions.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    
    val totalAssignments = assignments.size
    val completedAssignments = assignments.count { it.isCompleted }
    val assignmentProgress = if (totalAssignments > 0) completedAssignments.toFloat() / totalAssignments else 0f
    
    val showActionHistory by viewModel.showActionHistory.collectAsStateWithLifecycle()

    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val systemInDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "Dark" -> true
        "Light" -> false
        else -> systemInDark
    }

    // Fully responsive and accurate stats derived from state with zero cached stale values!
    val sessionsTodayCount = remember(pomodoroSessions) {
        androidx.compose.runtime.derivedStateOf {
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            pomodoroSessions.count { it.dateMillis >= todayStart }
        }
    }.value

    val totalFocusMinutesToday = remember(pomodoroSessions) {
        androidx.compose.runtime.derivedStateOf {
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            pomodoroSessions.filter { it.dateMillis >= todayStart }.sumOf { it.durationMinutes }
        }
    }.value

    val notesCount = remember(notes) {
        androidx.compose.runtime.derivedStateOf { notes.size }
    }.value

    val activeTasksCount = remember(tasks) {
        androidx.compose.runtime.derivedStateOf { tasks.count { !it.isCompleted } }
    }.value

    // Interactive, dynamic and beautiful AI coach commentary (Lumia "says")
    val coachCommentary = remember(sessionsTodayCount, totalFocusMinutesToday, activeTasksCount, notesCount, completedAssignments, totalAssignments) {
        androidx.compose.runtime.derivedStateOf {
            when {
                sessionsTodayCount == 0 && activeTasksCount > 0 -> {
                    "Lumia Coach says: Today is a fresh setup! You have some active tasks pending. Slicing one into a quick 15-minute Pomodoro is an unbeatable way to spark momentum."
                }
                sessionsTodayCount == 0 -> {
                    "Lumia Coach says: Your mind thrives on focus blocks! Start your first Pomodoro session whenever you're ready to lay down some study miles."
                }
                sessionsTodayCount in 1..2 -> {
                    "Lumia Coach says: Fantastic start! You've logged $totalFocusMinutesToday minutes of deep, focused work today. Keep standard, regular intervals to protect your energy."
                }
                sessionsTodayCount >= 3 -> {
                    "Lumia Coach says: Amazing! You are in deep study flow. Today you've accomplished $totalFocusMinutesToday minutes of focus. Remember to take a nice 10-minute rest!"
                }
                activeTasksCount == 0 && notesCount > 0 -> {
                    "Lumia Coach says: Look at that! Your daily checklist is completely clear. Reviewing your $notesCount study notes is a top-tier strategy for retention."
                }
                else -> {
                    "Lumia Coach says: Consistency beats intensity. Every minor focus round gets you closer to mastering your course materials!"
                }
            }
        }
    }.value

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        val topP = paddingValues.calculateTopPadding() + 16.dp
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = topP,
                bottom = paddingValues.calculateBottomPadding() + 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Lumia AI Companion Commentary Card (Lumia "says")
        item {
            androidx.compose.material3.ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Rounded.Info,
                            contentDescription = "Lumia Coach",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Lumia Buddy Insights",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = coachCommentary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // NEW: Study Sessions Today counter & Focus minutes card row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Sessions Today", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = sessionsTodayCount.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Total Focus Today", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${totalFocusMinutesToday}m",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                             Text("Total Notes", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                             Spacer(modifier = Modifier.height(8.dp))
                             Text(
                                text = notesCount.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                             )
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                             Text("Active Tasks", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.Bold)
                             Spacer(modifier = Modifier.height(8.dp))
                             Text(
                                text = activeTasksCount.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                             )
                        }
                    }
                }
                
                AssignmentsStatusDonutChart(
                    modifier = Modifier.fillMaxWidth(),
                    total = totalAssignments,
                    completed = completedAssignments,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    primaryColor = MaterialTheme.colorScheme.primary,
                    secondaryColor = MaterialTheme.colorScheme.tertiary
                )
                
                if (courses.isNotEmpty() && assignments.isNotEmpty()) {
                    AssignmentsPerCourseBarChart(
                        modifier = Modifier.fillMaxWidth(),
                        courses = courses,
                        assignments = assignments,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        barColor = MaterialTheme.colorScheme.primary
                    )
                }

                PomodoroHeatmapChart(
                    modifier = Modifier.fillMaxWidth(),
                    sessions = pomodoroSessions,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    primaryColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (showActionHistory) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = "History",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Unified Action History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (actionLogs.isEmpty()) {
                item {
                    ovrrup.lumia.ui.components.GlassCard(
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No telemetry recorded.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                item {
                    ovrrup.lumia.ui.components.GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            // Only show last 25 logs to avoid UI pressure, full history can be managed in data settings
                            actionLogs.take(25).forEachIndexed { index, log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = if (index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = log.actionText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Medium,
                                            color = if (index == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val date = DateFormat.format("MMM dd, HH:mm", Date(log.timestampMillis)).toString()
                                        Text(
                                            text = date,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                if (index < actionLogs.take(25).size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 20.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                    )
                                }
                            }
                            
                            if (actionLogs.size > 25) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "+ ${actionLogs.size - 25} more items in local database",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
fun AssignmentsStatusDonutChart(
    modifier: Modifier = Modifier,
    total: Int,
    completed: Int,
    backgroundColor: Color,
    primaryColor: Color,
    secondaryColor: Color
) {
    ovrrup.lumia.ui.components.GlassCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Assignments Status",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 24.dp.toPx()
                    
                    // Background Ring
                    drawArc(
                        color = secondaryColor.copy(alpha = 0.3f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(strokeW / 2, strokeW / 2),
                        size = androidx.compose.ui.geometry.Size(size.width - strokeW, size.height - strokeW),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                    
                    // Foreground Ring
                    val progressAngle = if (total > 0) (completed.toFloat() / total.toFloat()) * 360f else 0f
                    if (progressAngle > 0) {
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = progressAngle,
                            useCenter = false,
                            topLeft = androidx.compose.ui.geometry.Offset(strokeW / 2, strokeW / 2),
                            size = androidx.compose.ui.geometry.Size(size.width - strokeW, size.height - strokeW),
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$completed / $total",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            val itemColors = listOf(primaryColor, secondaryColor.copy(alpha = 0.3f))
            val labels = listOf("Completed", "Pending")
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                labels.forEachIndexed { index, label ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(14.dp).background(itemColors[index], CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AssignmentsPerCourseBarChart(
    modifier: Modifier = Modifier,
    courses: List<ovrrup.lumia.model.Course>,
    assignments: List<ovrrup.lumia.model.PracticeAssignment>,
    backgroundColor: Color,
    barColor: Color
) {
    ovrrup.lumia.ui.components.GlassCard(
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

@Composable
fun PomodoroHeatmapChart(
    modifier: Modifier = Modifier,
    sessions: List<ovrrup.lumia.model.PomodoroSession>,
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

    ovrrup.lumia.ui.components.GlassCard(
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
