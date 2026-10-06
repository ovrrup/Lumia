package lumia.tracker.ui.screens.study

import android.text.format.DateFormat
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.*
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.components.ScholarCardDefaults
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.study.components.StudyCapsuleFilterChip
import lumia.tracker.viewmodel.ScholarViewModel
import java.util.*

/**
 * View options for the progress tab switcher (Week / Month / Lifetime).
 */
enum class AnalyticsView(val label: String, val icon: ImageVector) {
    WEEK("Week", Icons.Rounded.DateRange),
    MONTH("Month", Icons.Rounded.CalendarMonth),
    LIFETIME("Lifetime", Icons.Rounded.AllInclusive)
}

/**
 * Data item representing a single bar in the responsive analytics chart.
 */
data class AnalyticsBarItem(
    val label: String,
    val minutes: Int,
    val isHighlighted: Boolean,
    val displayValue: String
)

/**
 * Modernized soft ambient analytical metric hero card with rounded container,
 * glowing icon pill, and clear typography.
 */
@Composable
fun MetricHeroCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    badge: String? = null
) {
    val isDark = isSystemInDarkTheme()
    ScholarCard(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerLow
                        else MaterialTheme.colorScheme.surfaceContainerLowest,
        border = ScholarCardDefaults.border()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(19.dp)
                    )
                }
                if (badge != null) {
                    Surface(
                        shape = CircleShape,
                        color = color.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * View Selector Capsule (Week / Month / Lifetime).
 * Features clean CircleShape capsule container, tactile haptics, and soft indicator.
 */
@Composable
fun ViewSelectorCapsule(
    selectedView: AnalyticsView,
    onViewSelected: (AnalyticsView) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape,
        color = if (isDark) MaterialTheme.colorScheme.surfaceContainerLow
                else MaterialTheme.colorScheme.surfaceContainerLowest,
        border = ScholarCardDefaults.border()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AnalyticsView.entries.forEach { view ->
                val isSelected = view == selectedView
                val animatedBg by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                                  else Color.Transparent,
                    animationSpec = tween(durationMillis = 220),
                    label = "view_bg_${view.name}"
                )
                val animatedTextColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                  else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(durationMillis = 220),
                    label = "view_text_${view.name}"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(animatedBg)
                        .clickable {
                            if (!isSelected) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onViewSelected(view)
                            }
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = view.icon,
                            contentDescription = null,
                            tint = animatedTextColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = view.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = animatedTextColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * AnalyticsTab - Reimagined Progress & Stats Hub with Soft Ambient Aesthetics,
 * Study Pulse Hero, 2x2 Bento Metric Grid, and Decluttered Activity Charts.
 */
@ValueScore(
    score = 96,
    importance = Importance.CRITICAL,
    description = "Reimagined ambient analytics and progress workspace with study pulse hero, bento metric grid, and focus activity charts",
    category = "Study"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsTab(
    navController: NavController,
    viewModel: ScholarViewModel,
    paddingValues: PaddingValues
) {
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val actionLogs by viewModel.actionLogs.collectAsStateWithLifecycle()
    val pomodoroSessions by viewModel.pomodoroSessions.collectAsStateWithLifecycle()
    val allTestRecords by viewModel.allTestRecords.collectAsStateWithLifecycle()
    val allAttendance by viewModel.allAttendanceRecords.collectAsStateWithLifecycle()

    val streakLongest by viewModel.streakLongest.collectAsStateWithLifecycle()
    val streakCurrent by viewModel.streakCurrent.collectAsStateWithLifecycle()
    val showActionHistory by viewModel.showActionHistory.collectAsStateWithLifecycle()

    var selectedView by remember { mutableStateOf(AnalyticsView.WEEK) }
    var selectedCourseId by remember { mutableStateOf(-1) }

    val isDark = isSystemInDarkTheme()

    // Dynamic Time & Range Calculations
    val periodFocusMins = remember(pomodoroSessions, selectedView) {
        val nowMillis = System.currentTimeMillis()
        when (selectedView) {
            AnalyticsView.WEEK -> {
                val start = nowMillis - 7 * 24 * 60 * 60 * 1000L
                pomodoroSessions.filter { it.dateMillis in start..nowMillis }.sumOf { it.durationMinutes }
            }
            AnalyticsView.MONTH -> {
                val start = nowMillis - 30 * 24 * 60 * 60 * 1000L
                pomodoroSessions.filter { it.dateMillis in start..nowMillis }.sumOf { it.durationMinutes }
            }
            AnalyticsView.LIFETIME -> {
                pomodoroSessions.sumOf { it.durationMinutes }
            }
        }
    }

    val periodFocusFormatted = remember(periodFocusMins) {
        val h = periodFocusMins / 60
        val m = periodFocusMins % 60
        if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    val periodSessionsCount = remember(pomodoroSessions, selectedView) {
        val nowMillis = System.currentTimeMillis()
        when (selectedView) {
            AnalyticsView.WEEK -> {
                val start = nowMillis - 7 * 24 * 60 * 60 * 1000L
                pomodoroSessions.count { it.dateMillis in start..nowMillis }
            }
            AnalyticsView.MONTH -> {
                val start = nowMillis - 30 * 24 * 60 * 60 * 1000L
                pomodoroSessions.count { it.dateMillis in start..nowMillis }
            }
            AnalyticsView.LIFETIME -> {
                pomodoroSessions.size
            }
        }
    }

    val periodTasksDone = remember(assignments, selectedView) {
        val nowMillis = System.currentTimeMillis()
        when (selectedView) {
            AnalyticsView.WEEK -> {
                val start = nowMillis - 7 * 24 * 60 * 60 * 1000L
                assignments.count { it.isCompleted && (it.dueDateMillis == 0L || it.dueDateMillis in start..nowMillis) }
            }
            AnalyticsView.MONTH -> {
                val start = nowMillis - 30 * 24 * 60 * 60 * 1000L
                assignments.count { it.isCompleted && (it.dueDateMillis == 0L || it.dueDateMillis in start..nowMillis) }
            }
            AnalyticsView.LIFETIME -> {
                assignments.count { it.isCompleted }
            }
        }
    }

    val totalAssignments = assignments.size
    val completedAssignments = assignments.count { it.isCompleted }
    val assignmentRate = if (totalAssignments > 0) ((completedAssignments.toFloat() / totalAssignments) * 100).toInt() else 0

    val attendanceStats = remember(allAttendance) {
        val present = allAttendance.count { it.status.equals("PRESENT", ignoreCase = true) }
        val late = allAttendance.count { it.status.equals("LATE", ignoreCase = true) }
        val absent = allAttendance.count { it.status.equals("ABSENT", ignoreCase = true) }
        val attended = present + late
        val effectiveTotal = present + late + absent
        val rateInt = if (effectiveTotal > 0) ((attended.toFloat() / effectiveTotal) * 100).toInt() else 0
        AttendanceSummary(
            total = effectiveTotal,
            present = present,
            late = late,
            absent = absent,
            attended = attended,
            ratePercent = rateInt
        )
    }

    // Dynamic Chart Data based on selected view (Week / Month / Lifetime)
    val chartData = remember(pomodoroSessions, selectedView) {
        when (selectedView) {
            AnalyticsView.WEEK -> {
                val cal = Calendar.getInstance()
                (6 downTo 0).map { daysAgo ->
                    cal.timeInMillis = System.currentTimeMillis()
                    cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
                    val dayStart = cal.apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val dayEnd = dayStart + 24 * 60 * 60 * 1000L
                    val dayLabel = when (cal.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.MONDAY -> "Mon"
                        Calendar.TUESDAY -> "Tue"
                        Calendar.WEDNESDAY -> "Wed"
                        Calendar.THURSDAY -> "Thu"
                        Calendar.FRIDAY -> "Fri"
                        Calendar.SATURDAY -> "Sat"
                        Calendar.SUNDAY -> "Sun"
                        else -> ""
                    }
                    val mins = pomodoroSessions
                        .filter { it.dateMillis in dayStart until dayEnd }
                        .sumOf { it.durationMinutes }
                    val disp = if (mins >= 60) "${mins / 60}h" else "${mins}m"
                    AnalyticsBarItem(dayLabel, mins, daysAgo == 0, disp)
                }
            }
            AnalyticsView.MONTH -> {
                val nowMillis = System.currentTimeMillis()
                (3 downTo 0).map { weekIndex ->
                    val weekStart = nowMillis - (weekIndex + 1) * 7 * 24 * 60 * 60 * 1000L
                    val weekEnd = nowMillis - weekIndex * 7 * 24 * 60 * 60 * 1000L
                    val label = "W${4 - weekIndex}"
                    val mins = pomodoroSessions
                        .filter { it.dateMillis in weekStart until weekEnd }
                        .sumOf { it.durationMinutes }
                    val disp = if (mins >= 60) "${mins / 60}h" else "${mins}m"
                    AnalyticsBarItem(label, mins, weekIndex == 0, disp)
                }
            }
            AnalyticsView.LIFETIME -> {
                val cal = Calendar.getInstance()
                (5 downTo 0).map { monthsAgo ->
                    cal.timeInMillis = System.currentTimeMillis()
                    cal.add(Calendar.MONTH, -monthsAgo)
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    val monthStart = cal.apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    cal.set(Calendar.DAY_OF_MONTH, maxDay)
                    val monthEnd = cal.apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis
                    val monthLabel = when (cal.get(Calendar.MONTH)) {
                        Calendar.JANUARY -> "Jan"
                        Calendar.FEBRUARY -> "Feb"
                        Calendar.MARCH -> "Mar"
                        Calendar.APRIL -> "Apr"
                        Calendar.MAY -> "May"
                        Calendar.JUNE -> "Jun"
                        Calendar.JULY -> "Jul"
                        Calendar.AUGUST -> "Aug"
                        Calendar.SEPTEMBER -> "Sep"
                        Calendar.OCTOBER -> "Oct"
                        Calendar.NOVEMBER -> "Nov"
                        Calendar.DECEMBER -> "Dec"
                        else -> ""
                    }
                    val mins = pomodoroSessions
                        .filter { it.dateMillis in monthStart..monthEnd }
                        .sumOf { it.durationMinutes }
                    val disp = if (mins >= 60) "${mins / 60}h" else "${mins}m"
                    AnalyticsBarItem(monthLabel, mins, monthsAgo == 0, disp)
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = paddingValues.calculateTopPadding(),
            bottom = paddingValues.calculateBottomPadding() + 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. View Selector Capsule Bar (Week / Month / Lifetime)
        item(key = "view_selector_capsule") {
            ViewSelectorCapsule(
                selectedView = selectedView,
                onViewSelected = { selectedView = it }
            )
        }

        // 2. Ambient Study Pulse Hero Card
        item(key = "study_pulse_hero") {
            val periodGoalMins = when (selectedView) {
                AnalyticsView.WEEK -> 600
                AnalyticsView.MONTH -> 2400
                AnalyticsView.LIFETIME -> 6000
            }
            val goalProgress = (periodFocusMins.toFloat() / periodGoalMins.toFloat()).coerceIn(0f, 1f)
            val animatedGoalProgress by animateFloatAsState(
                targetValue = goalProgress,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
                label = "goalProgressAnim"
            )

            ScholarCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerLow
                                else MaterialTheme.colorScheme.surfaceContainerLowest,
                border = ScholarCardDefaults.border()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Insights,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Study Pulse",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = periodFocusFormatted,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Focus time in ${selectedView.label.lowercase()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Circular Target Ring
                        Box(
                            modifier = Modifier.size(72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                strokeWidth = 7.dp,
                                trackColor = Color.Transparent
                            )
                            CircularProgressIndicator(
                                progress = { animatedGoalProgress },
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 7.dp,
                                trackColor = Color.Transparent
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${(animatedGoalProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Goal",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Key Summary Stats Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "$periodSessionsCount Sessions",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "$streakCurrent Days",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "$periodTasksDone Done",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. 2x2 Soft Bento Metric Grid
        item(key = "summary_metrics_grid") {
            val periodBadgeText = selectedView.label

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricHeroCard(
                        title = "Focus Time",
                        value = periodFocusFormatted,
                        icon = Icons.Rounded.Timer,
                        color = MaterialTheme.colorScheme.primary,
                        badge = periodBadgeText,
                        modifier = Modifier.weight(1f)
                    )
                    MetricHeroCard(
                        title = "Tasks Done",
                        value = "$periodTasksDone",
                        icon = Icons.Rounded.CheckCircle,
                        color = MaterialTheme.colorScheme.tertiary,
                        badge = "$completedAssignments/$totalAssignments",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricHeroCard(
                        title = "Completion",
                        value = "$assignmentRate%",
                        icon = Icons.Rounded.AssignmentTurnedIn,
                        color = MaterialTheme.colorScheme.secondary,
                        badge = if (assignmentRate >= 75) "Optimal" else "In Progress",
                        modifier = Modifier.weight(1f)
                    )
                    MetricHeroCard(
                        title = "Attendance",
                        value = if (attendanceStats.total > 0) "${attendanceStats.ratePercent}%" else "N/A",
                        icon = Icons.Rounded.School,
                        color = Color(0xFFFF9500),
                        badge = if (attendanceStats.ratePercent >= 75) "Good" else "Attention",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Ambient Focus Activity Bar Chart
        item(key = "focus_activity_chart") {
            val chartTitle = when (selectedView) {
                AnalyticsView.WEEK -> "Weekly Activity"
                AnalyticsView.MONTH -> "Monthly Activity"
                AnalyticsView.LIFETIME -> "Lifetime Activity"
            }
            val primaryColor = MaterialTheme.colorScheme.primary

            ScholarCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerLow
                                else MaterialTheme.colorScheme.surfaceContainerLowest,
                border = ScholarCardDefaults.border()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.BarChart,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = chartTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = primaryColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = periodFocusFormatted,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val maxMins = (chartData.maxOfOrNull { it.minutes } ?: 60).coerceAtLeast(60).toFloat()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        chartData.forEach { item ->
                            val targetHeightFrac = (item.minutes / maxMins).coerceIn(0.08f, 1f)
                            val animatedHeightFrac by animateFloatAsState(
                                targetValue = targetHeightFrac,
                                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                                label = "barHeight_${item.label}"
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (item.minutes > 0) {
                                    Text(
                                        text = item.displayValue,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = if (item.isHighlighted) FontWeight.Bold else FontWeight.Medium,
                                        color = if (item.isHighlighted) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                } else {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .fillMaxHeight(animatedHeightFrac)
                                        .clip(CircleShape)
                                        .background(
                                            if (item.isHighlighted) primaryColor
                                            else if (item.minutes > 0) primaryColor.copy(alpha = 0.40f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f)
                                        )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (item.isHighlighted) FontWeight.Bold else FontWeight.Medium,
                                    color = if (item.isHighlighted) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Study Streak & Consistency Card
        item(key = "streaks_analytics_chart") {
            val streakColor = Color(0xFFFF9500)
            val nextMilestone = remember(streakCurrent) {
                when {
                    streakCurrent < 7 -> 7
                    streakCurrent < 14 -> 14
                    streakCurrent < 30 -> 30
                    streakCurrent < 50 -> 50
                    streakCurrent < 100 -> 100
                    else -> ((streakCurrent / 50) + 1) * 50
                }
            }
            val milestoneProgress = (streakCurrent.toFloat() / nextMilestone).coerceIn(0f, 1f)

            ScholarCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerLow
                                else MaterialTheme.colorScheme.surfaceContainerLowest,
                border = ScholarCardDefaults.border()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(streakColor.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = streakColor,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Text(
                                text = "Study Streak",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = streakColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "$streakCurrent Days",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = streakColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StreakStatColumn(label = "Current", value = "$streakCurrent d", color = streakColor)
                        StreakStatColumn(label = "Longest", value = "$streakLongest d", color = MaterialTheme.colorScheme.primary)
                        StreakStatColumn(label = "Next Goal", value = "$nextMilestone d", color = MaterialTheme.colorScheme.secondary)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { milestoneProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = streakColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f)
                    )
                }
            }
        }

        // 6. Attendance Analysis Breakdown
        if (attendanceStats.total > 0) {
            item(key = "attendance_analytics_graph") {
                val healthColor = if (attendanceStats.ratePercent >= 75) Color(0xFF34C759) else Color(0xFFFF9500)

                ScholarCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerLow
                                    else MaterialTheme.colorScheme.surfaceContainerLowest,
                    border = ScholarCardDefaults.border()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(healthColor.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.School,
                                    contentDescription = null,
                                    tint = healthColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Attendance Rate",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stacked Proportional Distribution Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f))
                        ) {
                            val total = attendanceStats.total.coerceAtLeast(1).toFloat()
                            val presentWeight = attendanceStats.present / total
                            val lateWeight = attendanceStats.late / total
                            val absentWeight = attendanceStats.absent / total

                            if (presentWeight > 0f) {
                                Box(
                                    modifier = Modifier
                                        .weight(presentWeight)
                                        .fillMaxHeight()
                                        .background(Color(0xFF34C759))
                                )
                            }
                            if (lateWeight > 0f) {
                                Box(
                                    modifier = Modifier
                                        .weight(lateWeight)
                                        .fillMaxHeight()
                                        .background(Color(0xFFFF9500))
                                )
                            }
                            if (absentWeight > 0f) {
                                Box(
                                    modifier = Modifier
                                        .weight(absentWeight)
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.error)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Indicator Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AttendancePillIndicator(label = "Present", count = attendanceStats.present, color = Color(0xFF34C759))
                            AttendancePillIndicator(label = "Late", count = attendanceStats.late, color = Color(0xFFFF9500))
                            AttendancePillIndicator(label = "Absent", count = attendanceStats.absent, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // 7. Academic Performance & Tests Breakdown
        if (allTestRecords.isNotEmpty()) {
            item(key = "test_analytics_section") {
                val secColor = MaterialTheme.colorScheme.secondary

                ScholarCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerLow
                                    else MaterialTheme.colorScheme.surfaceContainerLowest,
                    border = ScholarCardDefaults.border()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(secColor.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Grading,
                                    contentDescription = null,
                                    tint = secColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Test Performance",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Capsule Course Filters
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item {
                                StudyCapsuleFilterChip(
                                    selected = selectedCourseId == -1,
                                    onClick = { selectedCourseId = -1 },
                                    label = "All Courses",
                                    accentColor = MaterialTheme.colorScheme.secondary
                                )
                            }
                            items(courses, key = { it.id }) { course ->
                                val courseColor = remember(course.colorHex) {
                                    try {
                                        Color(android.graphics.Color.parseColor(course.colorHex))
                                    } catch (_: Exception) {
                                        null
                                    }
                                }
                                StudyCapsuleFilterChip(
                                    selected = selectedCourseId == course.id,
                                    onClick = { selectedCourseId = course.id },
                                    label = course.name,
                                    leadingDotColor = courseColor,
                                    accentColor = courseColor ?: MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val filteredTestRecords = if (selectedCourseId == -1) allTestRecords else allTestRecords.filter { it.courseId == selectedCourseId }
                        val avgScore = if (filteredTestRecords.isNotEmpty()) {
                            filteredTestRecords.map {
                                val total = if (it.totalMarks > 0f) it.totalMarks else 1f
                                (it.marksObtained / total) * 100f
                            }.average().toInt()
                        } else 0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Average Score",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$avgScore%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = secColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (avgScore / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = secColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f)
                        )
                    }
                }
            }
        }

        // 8. Recent Activity History Section
        if (showActionHistory) {
            item(key = "activity_log_header") {
                val priColor = MaterialTheme.colorScheme.primary

                ScholarCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerLow
                                    else MaterialTheme.colorScheme.surfaceContainerLowest,
                    border = ScholarCardDefaults.border()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(priColor.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.History,
                                    contentDescription = "History",
                                    tint = priColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Recent Activity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (actionLogs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No recent activity yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                actionLogs.take(15).forEach { log ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(priColor, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = log.actionText,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val date = DateFormat.format("MMM d, HH:mm", Date(log.timestampMillis)).toString()
                                        Text(
                                            text = date,
                                            style = MaterialTheme.typography.labelSmall,
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
}

/**
 * Clean vertical metric column for streaks and summaries.
 */
@Composable
private fun StreakStatColumn(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Visual attendance indicator pill with accent dot.
 */
@Composable
private fun AttendancePillIndicator(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Data holder for processed attendance statistics.
 */
private data class AttendanceSummary(
    val total: Int,
    val present: Int,
    val late: Int,
    val absent: Int,
    val attended: Int,
    val ratePercent: Int
)
