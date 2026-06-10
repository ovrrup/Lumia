package com.example.ui.screens

import android.text.format.DateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
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
import com.example.viewmodel.ScholarViewModel
import java.util.Date

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.aspectRatio

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsTab(viewModel: ScholarViewModel, paddingValues: PaddingValues) {
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val actionLogs by viewModel.actionLogs.collectAsStateWithLifecycle()

    val totalAssignments = assignments.size
    val completedAssignments = assignments.count { it.isCompleted }
    val assignmentProgress = if (totalAssignments > 0) completedAssignments.toFloat() / totalAssignments else 0f
    
    val showActionHistory by viewModel.showActionHistory.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { innerPadding ->
        val topP = innerPadding.calculateTopPadding() + 16.dp
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

        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AssignmentsStatusDonutChart(
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    total = totalAssignments,
                    completed = completedAssignments,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    primaryColor = MaterialTheme.colorScheme.primary,
                    secondaryColor = MaterialTheme.colorScheme.tertiary
                )
                
                if (courses.isNotEmpty() && assignments.isNotEmpty()) {
                    AssignmentsPerCourseBarChart(
                        modifier = Modifier.fillMaxWidth().height(240.dp),
                        courses = courses,
                        assignments = assignments,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        barColor = MaterialTheme.colorScheme.primary
                    )
                }

                if (actionLogs.isNotEmpty()) {
                    ActivityLineChart(
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        actionLogs = actionLogs,
                        color = MaterialTheme.colorScheme.primary,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        if (showActionHistory) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Action History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (actionLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "No actions yet.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(actionLogs, key = { it.id }) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.actionText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val date = DateFormat.format("MMM dd, yyyy  •  HH:mm", Date(log.timestampMillis)).toString()
                            Text(
                                text = date,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    val pending = maxOf(0, total - completed)
                    val actualData = if (total > 0) listOf(completed.toFloat(), pending.toFloat()) else listOf(1f)
                    val colorsList = if (total > 0) listOf(primaryColor, secondaryColor) else listOf(Color.LightGray)
                    
                    val totalData = actualData.sum()
                    var startAngle = -90f
                    
                    actualData.forEachIndexed { index, value ->
                        val sweepAngle = (value / totalData) * 360f
                        drawArc(
                            color = colorsList[index],
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Butt)
                        )
                        startAngle += sweepAngle
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$completed / $total",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column(
                modifier = Modifier.weight(1f).padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Assignments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val labels = listOf("Completed", "Pending")
                val itemColors = listOf(primaryColor, secondaryColor)
                labels.forEachIndexed { index, label ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).background(itemColors[index], CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
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
    courses: List<com.example.model.Course>,
    assignments: List<com.example.model.PracticeAssignment>,
    backgroundColor: Color,
    barColor: Color
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                "Assignments per Course",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                // Count assignments per course
                val assignmentsCountMap = assignments.groupBy { it.courseId }.mapValues { it.value.size }
                // Sort courses by assignment count and take top 5
                val topCourses = courses
                    .map { Pair(it.name, assignmentsCountMap[it.id] ?: 0) }
                    .filter { it.second > 0 }
                    .sortedByDescending { it.second }
                    .take(5)
                
                if (topCourses.isEmpty()) return@Canvas

                val maxCount = topCourses.maxOf { it.second }.toFloat()
                val barWidth = size.width / (topCourses.size * 2)
                val spacing = size.width / topCourses.size

                topCourses.forEachIndexed { index, (courseName, count) ->
                    val x = index * spacing + barWidth / 2
                    val barHeight = (count / maxCount) * size.height
                    val yTop = size.height - barHeight

                    drawLine(
                        color = barColor,
                        start = androidx.compose.ui.geometry.Offset(x, size.height),
                        end = androidx.compose.ui.geometry.Offset(x, yTop),
                        strokeWidth = barWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityLineChart(modifier: Modifier = Modifier, actionLogs: List<com.example.model.ActionLog>, color: Color, backgroundColor: Color) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                "Activity Past 7 Days",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                // Determine past 7 days activity
                val calendar = java.util.Calendar.getInstance()
                val now = calendar.timeInMillis
                
                // Group by days ago (0 to 6)
                val counts = IntArray(7) { 0 }
                actionLogs.forEach { log ->
                    val diffMillis = now - log.timestampMillis
                    val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
                    if (diffDays in 0..6) {
                        counts[6 - diffDays]++ 
                    }
                }
                
                val dataPoints = counts.map { it.toFloat() }
                val maxPoint = dataPoints.maxOrNull()?.takeIf { it > 0 } ?: 10f
                val spacing = size.width / (dataPoints.size - 1)
                
                val path = androidx.compose.ui.graphics.Path()
                dataPoints.forEachIndexed { index, value ->
                    val x = index * spacing
                    val y = size.height - (value / maxPoint) * size.height
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
                )
                
                dataPoints.forEachIndexed { index, value ->
                    val x = index * spacing
                    val y = size.height - (value / maxPoint) * size.height
                    drawCircle(
                        color = backgroundColor,
                        radius = 6.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(x, y)
                    )
                    drawCircle(
                        color = color,
                        radius = 4.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(x, y)
                    )
                }
            }
        }
    }
}
