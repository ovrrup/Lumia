package lumia.tracker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lumia.tracker.model.Course
import lumia.tracker.model.PomodoroSession
import lumia.tracker.ui.components.GlassCard
import lumia.tracker.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    navController: NavController,
    viewModel: ScholarViewModel,
    initialSubjectId: Int? = null,
    initialCourseId: Int? = null,
    initialAssignmentId: Int? = null,
    initialTaskId: Int? = null,
    initialTopicId: Int? = null
) {
    val courses by viewModel.courses.collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    
    // Timer State
    val defaultMinutes = 25
    var timeLeftSeconds by remember { mutableStateOf(defaultMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Automatic selection matching initial state
    LaunchedEffect(courses, initialCourseId) {
        if (initialCourseId != null && courses.isNotEmpty()) {
            selectedCourse = courses.find { it.id == initialCourseId }
        }
    }

    // Countdown logic
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (timeLeftSeconds > 0) {
                delay(1000)
                timeLeftSeconds--
            }
            if (timeLeftSeconds == 0) {
                // Timer completed! Auto log Pomodoro Session
                isRunning = false
                scope.launch {
                    viewModel.crud.insertPomodoroSession(
                        PomodoroSession(
                            dateMillis = System.currentTimeMillis(),
                            durationMinutes = defaultMinutes,
                            courseId = selectedCourse?.id,
                            subjectId = selectedCourse?.subjectId ?: initialSubjectId
                        )
                    )
                    viewModel.calculateTodayStreakProgress()
                }
                timeLeftSeconds = defaultMinutes * 60
            }
        }
    }

    val progressFraction = timeLeftSeconds.toFloat() / (defaultMinutes * 60f)
    val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "progress")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FOCUS SPACE", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Circle Progress Ring - Reference 1 ambient neon style
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                val ringColor = selectedCourse?.colorHex?.let {
                    try { Color(android.graphics.Color.parseColor(it)) } catch (e: Exception) { null }
                } ?: MaterialTheme.colorScheme.primary

                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Back track
                    drawCircle(
                        color = ringColor.copy(alpha = 0.1f),
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Animated glowing progress arc
                    drawArc(
                        brush = Brush.sweepGradient(listOf(ringColor, ringColor.copy(alpha = 0.6f), ringColor)),
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Digital Timer typography
                val mins = timeLeftSeconds / 60
                val secs = timeLeftSeconds % 60
                val timeString = String.format(java.util.Locale.US, "%02d:%02d", mins, secs)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 52.sp),
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isRunning) "STAY FOCUSED" else "READY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ringColor
                    )
                }
            }

            // Quick course link chips
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "LINK TO ACTIVE COURSE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(courses) { course ->
                        val isSelected = selectedCourse?.id == course.id
                        val color = try {
                            Color(android.graphics.Color.parseColor(course.colorHex))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .clickable { selectedCourse = if (isSelected) null else course }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                                Text(
                                    text = course.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (isSelected) {
                                    Icon(Icons.Rounded.Check, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Controls Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "Stop Session" else "Start 25m Focus",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
