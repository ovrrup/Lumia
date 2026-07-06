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
import androidx.compose.foundation.border
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.lazy.LazyRow
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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsTab(navController: NavController, viewModel: ScholarViewModel, paddingValues: PaddingValues) {
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val actionLogs by viewModel.actionLogs.collectAsStateWithLifecycle()
    val pomodoroSessions by viewModel.pomodoroSessions.collectAsStateWithLifecycle()
    val allTestRecords by viewModel.allTestRecords.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val topics by viewModel.allTopics.collectAsStateWithLifecycle()
    
    val totalAssignments = assignments.size
    val completedAssignments = assignments.count { it.isCompleted }
    val assignmentProgress = if (totalAssignments > 0) completedAssignments.toFloat() / totalAssignments else 0f
    
    val showActionHistory by viewModel.showActionHistory.collectAsStateWithLifecycle()
    var selectedCourseId by remember { mutableStateOf(-1) }

    val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val systemInDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "Dark" -> true
        "Light" -> false
        else -> systemInDark
    }

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

        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // NEW: Study Sessions Today counter
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Study Sessions Today", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            text = viewModel.getSessionsTodayCount().toString(),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                             Text("Total Notes", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text(
                                text = viewModel.getNotesCount().toString(),
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
                             Text("Active Tasks", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            Text(
                                text = viewModel.getActiveTasksCount().toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
                
                
                val streakTotalNormal by viewModel.streakTotalNormal.collectAsStateWithLifecycle()
                val streakTotalComplete by viewModel.streakTotalComplete.collectAsStateWithLifecycle()
                val streakLongest by viewModel.streakLongest.collectAsStateWithLifecycle()
                val streakCurrent by viewModel.streakCurrent.collectAsStateWithLifecycle()

                // Streaks Analytics Card
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Streaks Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                lumia.tracker.ui.components.GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Current", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(streakCurrent.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Longest", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(streakLongest.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Normal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(streakTotalNormal.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Complete", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(streakTotalComplete.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFFE67E22))
                            }
                        }
                    }
                }

                val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
                val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()

                // Overall Academic Performance & Test Analytics
                if (allTestRecords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Academic & Test Analytics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Course Selector Filter Chips (Horizontal List)
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCourseId == -1,
                                onClick = { selectedCourseId = -1 },
                                label = { Text("All Courses") }
                            )
                        }
                        items(courses) { course ->
                            FilterChip(
                                selected = selectedCourseId == course.id,
                                onClick = { selectedCourseId = course.id },
                                label = { Text(course.name) }
                            )
                        }
                    }
                    
                    lumia.tracker.ui.components.GlassCard(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            val filteredTestRecords = if (selectedCourseId == -1) allTestRecords else allTestRecords.filter { it.courseId == selectedCourseId }
                            val totalTests = filteredTestRecords.size
                            val pctScores = filteredTestRecords.map { if (it.totalMarks > 0) (it.marksObtained / it.totalMarks) * 100f else 0f }
                            val overallAverage = if (pctScores.isNotEmpty()) pctScores.average().toFloat() else 0f
                            val perfectScoresCount = filteredTestRecords.count { it.marksObtained == it.totalMarks && it.totalMarks > 0 }
                            val passingTestsCount = pctScores.count { it >= 50f }
                            val passRate = if (totalTests > 0) (passingTestsCount.toFloat() / totalTests * 100).toInt() else 0

                            if (totalTests == 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No tests logged for this course yet.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("GPA / Average", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${overallAverage.toInt()}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Tests Taken", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("$totalTests", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Pass Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("$passRate%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = if (passRate >= 75) Color(0xFF2ECC71) else Color(0xFFE74C3C))
                                    }
                                }

                                if (perfectScoresCount > 0) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(imageVector = Icons.Rounded.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$perfectScoresCount perfect scores achieved! Outstanding!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }

                            if (subjects.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Subject Strength & Mastery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    subjects.forEach { subj ->
                                        val subjTests = allTestRecords.filter { test ->
                                            test.subjectId == subj.id ||
                                            (test.topicId != null && topics.find { it.id == test.topicId }?.subjectId == subj.id) ||
                                            (test.courseId != null && courses.find { it.id == test.courseId }?.let { c ->
                                                c.subjectId == subj.id ||
                                                c.subjectIds.split(",").mapNotNull { it.trim().toIntOrNull() }.contains(subj.id)
                                            } == true)
                                        }
                                        if (subjTests.isNotEmpty()) {
                                            val subjAvg = subjTests.map { if (it.totalMarks > 0) (it.marksObtained / it.totalMarks) * 100f else 0f }.average().toFloat()
                                            
                                            val (statusText, statusColor) = when {
                                                subjAvg >= 85f -> "MASTER" to Color(0xFF2ECC71)
                                                subjAvg >= 70f -> "PROFICIENT" to MaterialTheme.colorScheme.secondary
                                                subjAvg >= 50f -> "DEVELOPING" to MaterialTheme.colorScheme.tertiary
                                                else -> "NEEDS WORK" to Color(0xFFE74C3C)
                                            }

                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(subj.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                                    Box(
                                                        modifier = Modifier
                                                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(statusText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = statusColor)
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("${subjAvg.toInt()}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                LinearProgressIndicator(
                                                    progress = subjAvg / 100f,
                                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                                    color = statusColor,
                                                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            val allTagsMap = HashMap<String, MutableList<Float>>()
                            allTestRecords.forEach { test ->
                                if (test.tags.isNotBlank()) {
                                    test.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { tag ->
                                        val pct = if (test.totalMarks > 0) (test.marksObtained / test.totalMarks) * 100f else 0f
                                        allTagsMap.getOrPut(tag) { ArrayList() }.add(pct)
                                    }
                                }
                            }

                            if (allTagsMap.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Performance by Tag Indicators", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    allTagsMap.entries.sortedByDescending { it.value.average() }.take(4).forEach { (tag, scores) ->
                                        val tagAvg = scores.average().toFloat()
                                        val tagColor = when {
                                            tagAvg >= 80f -> Color(0xFF2ECC71)
                                            tagAvg >= 60f -> MaterialTheme.colorScheme.primary
                                            else -> Color(0xFFE74C3C)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                                .border(1.dp, tagColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                                .padding(8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "#$tag",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = tagColor,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "${tagAvg.toInt()}%",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            val testedTopicIds = allTestRecords.mapNotNull { it.topicId }.distinct()
                            if (testedTopicIds.isNotEmpty() && topics.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Focused Concept Mastery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Interconnected concept mastery computed across assessments.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(12.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    testedTopicIds.take(3).forEach { tId ->
                                        val topicObj = topics.find { it.id == tId }
                                        if (topicObj != null) {
                                            val tTests = allTestRecords.filter { it.topicId == tId }
                                            val tAvg = tTests.map { if (it.totalMarks > 0) (it.marksObtained / it.totalMarks) * 100f else 0f }.average().toFloat()
                                            
                                            val statusColor = if (tAvg >= 80) Color(0xFF2ECC71) else if (tAvg >= 50) MaterialTheme.colorScheme.primary else Color(0xFFE74C3C)
                                            val iconTint = if (topicObj.isCompleted) Color(0xFF2ECC71) else MaterialTheme.colorScheme.onSurfaceVariant

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (topicObj.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.Category,
                                                    contentDescription = null,
                                                    tint = iconTint,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = topicObj.title,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "${tAvg.toInt()}% Avg",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Black,
                                                    color = statusColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                WeeklyAssignmentsDueChart(
                    modifier = Modifier.fillMaxWidth(),
                    assignments = assignments,
                    courses = courses,
                    onToggleCompletion = { viewModel.toggleAssignmentCompleted(it) }
                )

                AssignmentsStatusDonutChart(
                    modifier = Modifier.fillMaxWidth(),
                    total = totalAssignments,
                    completed = completedAssignments,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    primaryColor = MaterialTheme.colorScheme.primary,
                    secondaryColor = MaterialTheme.colorScheme.tertiary
                )

                FocusTimePerCourseChart(
                    modifier = Modifier.fillMaxWidth(),
                    sessions = pomodoroSessions,
                    courses = courses
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

                AssignmentCategoryDistributionChart(
                    modifier = Modifier.fillMaxWidth(),
                    assignments = assignments
                )

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
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
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
                lumia.tracker.ui.components.GlassCard(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
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
                                imageVector = Icons.Rounded.Info,
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
                lumia.tracker.ui.components.GlassCard(
                    modifier = Modifier.animateItem().fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
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
