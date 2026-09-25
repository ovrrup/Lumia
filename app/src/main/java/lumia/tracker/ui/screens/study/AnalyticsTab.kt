package lumia.tracker.ui.screens.study

import android.text.format.DateFormat
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.*
import lumia.tracker.ui.components.GlassCapsule
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.components.ScholarCardDefaults
import lumia.tracker.ui.screens.study.components.StudyCapsuleFilterChip
import lumia.tracker.viewmodel.ScholarViewModel
import java.util.*

/**
 * Period options for the capsule selector pills.
 */
enum class AnalyticsPeriod(val label: String, val icon: ImageVector) {
    DAILY("Daily", Icons.Rounded.Today),
    WEEKLY("Weekly", Icons.Rounded.DateRange),
    MONTHLY("Monthly", Icons.Rounded.CalendarMonth)
}

/**
 * Frosted specular gradient border providing a top-lit glass refraction look.
 */
@Composable
fun frostedSpecularBorder(
    accentColor: Color? = null,
    isDark: Boolean = isSystemInDarkTheme(),
    width: Dp = 1.dp
): BorderStroke {
    val topColor = accentColor?.copy(alpha = if (isDark) 0.42f else 0.55f)
        ?: if (isDark) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.65f)
    val bottomColor = accentColor?.copy(alpha = 0.08f)
        ?: if (isDark) Color.White.copy(alpha = 0.03f) else Color.White.copy(alpha = 0.14f)
    return BorderStroke(
        width = width,
        brush = Brush.verticalGradient(listOf(topColor, bottomColor))
    )
}

/**
 * Organic blur aura drawn behind containers and charts to give depth and luminous refraction.
 */
fun Modifier.blurAura(
    color: Color,
    alpha: Float = 0.15f,
    radiusMultiplier: Float = 0.70f,
    centerOffset: Offset = Offset(0.85f, 0.25f)
): Modifier = this.drawBehind {
    val cx = size.width * centerOffset.x
    val cy = size.height * centerOffset.y
    val r = size.width * radiusMultiplier
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = alpha), Color.Transparent),
            center = Offset(cx, cy),
            radius = r
        ),
        center = Offset(cx, cy),
        radius = r
    )
}

/**
 * Modernized glassmorphic analytical hero card with frosted specular borders,
 * glowing icon container, and subtle blur aura.
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
        modifier = modifier
            .blurAura(color = color, alpha = if (isDark) 0.14f else 0.09f),
        shape = RoundedCornerShape(22.dp),
        border = frostedSpecularBorder(accentColor = color, isDark = isDark)
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
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.14f))
                        .border(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0.08f))
                            ),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (badge != null) {
                    Surface(
                        shape = CircleShape,
                        color = color.copy(alpha = 0.12f),
                        border = BorderStroke(0.5.dp, color.copy(alpha = 0.30f))
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
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Capsule Period Selector Pills (Daily / Weekly / Monthly).
 * Features glassmorphic frosted surface, tactile haptics, and specular outline.
 */
@Composable
fun PeriodSelectorCapsule(
    selectedPeriod: AnalyticsPeriod,
    onPeriodSelected: (AnalyticsPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape,
        color = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.50f)
               else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
        border = frostedSpecularBorder(isDark = isDark, width = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AnalyticsPeriod.values().forEach { period ->
                val isSelected = period == selectedPeriod
                val animatedBg by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                  else Color.Transparent,
                    animationSpec = tween(durationMillis = 220),
                    label = "period_bg_${period.name}"
                )
                val animatedTextColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                  else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(durationMillis = 220),
                    label = "period_text_${period.name}"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(animatedBg)
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 1.dp,
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                            } else Modifier
                        )
                        .clickable {
                            if (!isSelected) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onPeriodSelected(period)
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
                            imageVector = period.icon,
                            contentDescription = null,
                            tint = animatedTextColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = period.label,
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
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val topics by viewModel.allTopics.collectAsStateWithLifecycle()
    val allAttendance by viewModel.allAttendanceRecords.collectAsStateWithLifecycle()

    val streakLongest by viewModel.streakLongest.collectAsStateWithLifecycle()
    val streakCurrent by viewModel.streakCurrent.collectAsStateWithLifecycle()
    val streakTotalComplete by viewModel.streakTotalComplete.collectAsStateWithLifecycle()
    val streakPercentage by viewModel.streakPercentage.collectAsStateWithLifecycle()
    val isCompleteToday by viewModel.streakIsCompleteToday.collectAsStateWithLifecycle()
    val showActionHistory by viewModel.showActionHistory.collectAsStateWithLifecycle()

    var selectedPeriod by remember { mutableStateOf(AnalyticsPeriod.WEEKLY) }
    var selectedCourseId by remember { mutableStateOf(-1) }

    // Date boundaries
    val now = remember { System.currentTimeMillis() }
    val (todayStart, todayEnd) = remember(now) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        val end = start + 24 * 60 * 60 * 1000L
        start to end
    }

    val periodFocusMins = remember(pomodoroSessions, selectedPeriod, todayStart, todayEnd) {
        when (selectedPeriod) {
            AnalyticsPeriod.DAILY -> {
                pomodoroSessions
                    .filter { it.dateMillis in todayStart until todayEnd }
                    .sumOf { it.durationMinutes }
            }
            AnalyticsPeriod.WEEKLY -> {
                val weekStart = todayStart - 6 * 24 * 60 * 60 * 1000L
                pomodoroSessions
                    .filter { it.dateMillis in weekStart until todayEnd }
                    .sumOf { it.durationMinutes }
            }
            AnalyticsPeriod.MONTHLY -> {
                val monthStart = todayStart - 29 * 24 * 60 * 60 * 1000L
                pomodoroSessions
                    .filter { it.dateMillis in monthStart until todayEnd }
                    .sumOf { it.durationMinutes }
            }
        }
    }

    val periodFocusFormatted = remember(periodFocusMins) {
        val h = periodFocusMins / 60
        val m = periodFocusMins % 60
        if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    val periodTasksDone = remember(assignments, selectedPeriod) {
        when (selectedPeriod) {
            AnalyticsPeriod.DAILY -> assignments.count { it.isCompleted }
            AnalyticsPeriod.WEEKLY -> assignments.count { it.isCompleted }
            AnalyticsPeriod.MONTHLY -> assignments.count { it.isCompleted }
        }
    }

    val totalAssignments = assignments.size
    val completedAssignments = assignments.count { it.isCompleted }
    val assignmentRate = if (totalAssignments > 0) ((completedAssignments.toFloat() / totalAssignments) * 100).toInt() else 0

    val attendanceStats = remember(allAttendance) {
        val total = allAttendance.size
        val present = allAttendance.count { it.status.equals("PRESENT", ignoreCase = true) }
        val late = allAttendance.count { it.status.equals("LATE", ignoreCase = true) }
        val absent = allAttendance.count { it.status.equals("ABSENT", ignoreCase = true) }
        val attended = present + late
        val rateInt = if (total > 0) ((attended.toFloat() / total) * 100).toInt() else 0
        AttendanceSummary(
            total = total,
            present = present,
            late = late,
            absent = absent,
            attended = attended,
            ratePercent = rateInt
        )
    }

    // 7-day focus activity data for the chart
    val last7DaysData = remember(pomodoroSessions) {
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
            Triple(dayLabel, mins, daysAgo == 0)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = paddingValues.calculateBottomPadding() + 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Capsule Period Selector Pills
        item(key = "period_selector_capsule") {
            PeriodSelectorCapsule(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { selectedPeriod = it }
            )
        }

        // 2x2 Glassmorphic Metric Hero Cards with Frosted Specular Borders
        item(key = "summary_metrics_grid") {
            val periodBadgeText = when (selectedPeriod) {
                AnalyticsPeriod.DAILY -> "Today"
                AnalyticsPeriod.WEEKLY -> "7 Days"
                AnalyticsPeriod.MONTHLY -> "30 Days"
            }

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
                        badge = if (assignmentRate >= 75) "Optimal" else "Progress",
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

        // Enhanced Focus Activity Bar Chart with Specular Glass & Blur Aura
        item(key = "focus_activity_chart") {
            val isDark = isSystemInDarkTheme()
            val weeklyTotalMins = last7DaysData.sumOf { it.second }
            val weeklyFormatted = if (weeklyTotalMins >= 60) "${weeklyTotalMins / 60}h ${weeklyTotalMins % 60}m" else "${weeklyTotalMins}m"
            val primaryColor = MaterialTheme.colorScheme.primary

            ScholarCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .blurAura(color = primaryColor, alpha = if (isDark) 0.14f else 0.08f),
                shape = RoundedCornerShape(24.dp),
                border = frostedSpecularBorder(accentColor = primaryColor, isDark = isDark)
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
                                    .clip(RoundedCornerShape(10.dp))
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
                                text = "Focus Activity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        GlassCapsule {
                            Text(
                                text = weeklyFormatted,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val maxMins = (last7DaysData.maxOfOrNull { it.second } ?: 60).coerceAtLeast(60).toFloat()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        last7DaysData.forEach { (day, mins, isToday) ->
                            val targetHeightFrac = (mins / maxMins).coerceIn(0.08f, 1f)
                            val animatedHeightFrac by animateFloatAsState(
                                targetValue = targetHeightFrac,
                                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                                label = "barHeight_$day"
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (mins > 0) {
                                    Text(
                                        text = "${mins}m",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = if (isToday) FontWeight.Black else FontWeight.SemiBold,
                                        color = if (isToday) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                } else {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .fillMaxHeight(animatedHeightFrac)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(
                                            if (isToday) {
                                                Brush.verticalGradient(
                                                    listOf(primaryColor, primaryColor.copy(alpha = 0.70f))
                                                )
                                            } else if (mins > 0) {
                                                Brush.verticalGradient(
                                                    listOf(primaryColor.copy(alpha = 0.55f), primaryColor.copy(alpha = 0.25f))
                                                )
                                            } else {
                                                Brush.verticalGradient(
                                                    listOf(
                                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                    )
                                                )
                                            }
                                        )
                                        .then(
                                            if (isToday) {
                                                Modifier.border(
                                                    1.dp,
                                                    Color.White.copy(alpha = if (isDark) 0.35f else 0.70f),
                                                    RoundedCornerShape(11.dp)
                                                )
                                            } else Modifier
                                        )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isToday) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Enhanced Study Streak Chart with Warm Blur Aura & Milestone Visualizer
        item(key = "streaks_analytics_chart") {
            val isDark = isSystemInDarkTheme()
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
                modifier = Modifier
                    .fillMaxWidth()
                    .blurAura(color = streakColor, alpha = if (isDark) 0.16f else 0.10f),
                shape = RoundedCornerShape(24.dp),
                border = frostedSpecularBorder(accentColor = streakColor, isDark = isDark)
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
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(streakColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Whatshot,
                                    contentDescription = null,
                                    tint = streakColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "Study Streaks",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        GlassCapsule {
                            Text(
                                text = if (isCompleteToday) "Active Today" else "Needs Activity",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleteToday) Color(0xFF34C759) else streakColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 7-Day Visual Streak Timeline Nodes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        last7DaysData.forEach { (day, mins, isToday) ->
                            val isDayDone = mins > 0
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isDayDone) streakColor.copy(alpha = 0.20f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f)
                                        )
                                        .border(
                                            width = if (isToday) 1.5.dp else 1.dp,
                                            brush = if (isDayDone || isToday) {
                                                Brush.verticalGradient(
                                                    listOf(streakColor.copy(alpha = 0.8f), streakColor.copy(alpha = 0.2f))
                                                )
                                            } else {
                                                Brush.verticalGradient(
                                                    listOf(Color.White.copy(alpha = 0.15f), Color.Transparent)
                                                )
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isDayDone) {
                                        Icon(
                                            imageVector = Icons.Rounded.Whatshot,
                                            contentDescription = null,
                                            tint = streakColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                                        )
                                    }
                                }
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isToday) streakColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Milestone Progress Bar with Ambient Glow
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Next Goal: $nextMilestone Days",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$streakCurrent / $nextMilestone",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = streakColor
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(milestoneProgress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(streakColor.copy(alpha = 0.70f), streakColor)
                                        )
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3 Clean KPI Columns
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StreakStatColumn(
                            label = "Current",
                            value = "$streakCurrent d",
                            color = streakColor,
                            modifier = Modifier.weight(1f)
                        )
                        StreakStatColumn(
                            label = "Best Streak",
                            value = "$streakLongest d",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        StreakStatColumn(
                            label = "Completed",
                            value = "$streakTotalComplete",
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Attendance Visual Graph with Donut Radial Gauge & Distribution Breakdown
        if (allAttendance.isNotEmpty()) {
            item(key = "attendance_analytics_graph") {
                val isDark = isSystemInDarkTheme()
                val isGoodStanding = attendanceStats.ratePercent >= 75
                val healthColor = if (isGoodStanding) Color(0xFF34C759) else MaterialTheme.colorScheme.error

                ScholarCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .blurAura(color = healthColor, alpha = if (isDark) 0.15f else 0.08f),
                    shape = RoundedCornerShape(24.dp),
                    border = frostedSpecularBorder(accentColor = healthColor, isDark = isDark)
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
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(healthColor.copy(alpha = 0.16f)),
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
                                    text = "Attendance Health",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            GlassCapsule {
                                val margin = attendanceStats.ratePercent - 75
                                val marginText = if (margin >= 0) "+$margin% Margin" else "$margin% Margin"
                                Text(
                                    text = if (isGoodStanding) "Optimal ($marginText)" else "Needs Attention ($marginText)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = healthColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Radial Gauge + Breakdown Side-by-Side
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Circular Radial Gauge
                            Box(
                                modifier = Modifier.size(92.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val animatedRate by animateFloatAsState(
                                    targetValue = (attendanceStats.ratePercent / 100f).coerceIn(0f, 1f),
                                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                                    label = "attendance_gauge"
                                )
                                val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)

                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeWidth = 9.dp.toPx()
                                    val diameter = size.minDimension - strokeWidth
                                    val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                                    val arcSize = Size(diameter, diameter)

                                    // Background Track Arc (270 deg)
                                    drawArc(
                                        color = trackColor,
                                        startAngle = 135f,
                                        sweepAngle = 270f,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )

                                    // Foreground Progress Arc
                                    if (animatedRate > 0f) {
                                        drawArc(
                                            brush = Brush.sweepGradient(
                                                listOf(healthColor.copy(alpha = 0.8f), healthColor)
                                            ),
                                            startAngle = 135f,
                                            sweepAngle = 270f * animatedRate,
                                            useCenter = false,
                                            topLeft = topLeft,
                                            size = arcSize,
                                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${attendanceStats.ratePercent}%",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Target 75%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Minimalist Breakdown Visual Stack
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Stacked Proportional Distribution Bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
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

                                // Visual Indicator Badges
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    AttendancePillIndicator(
                                        label = "Present",
                                        count = attendanceStats.present,
                                        color = Color(0xFF34C759)
                                    )
                                    AttendancePillIndicator(
                                        label = "Late",
                                        count = attendanceStats.late,
                                        color = Color(0xFFFF9500)
                                    )
                                    AttendancePillIndicator(
                                        label = "Absent",
                                        count = attendanceStats.absent,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Test Score Distributions & Academic Breakdown with Specular Glass
        if (allTestRecords.isNotEmpty()) {
            item(key = "test_analytics_section") {
                val isDark = isSystemInDarkTheme()
                val secColor = MaterialTheme.colorScheme.secondary

                ScholarCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .blurAura(color = secColor, alpha = if (isDark) 0.15f else 0.08f),
                    shape = RoundedCornerShape(24.dp),
                    border = frostedSpecularBorder(accentColor = secColor, isDark = isDark)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(secColor.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.School,
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

                        // Capsule Course Filters
                        Spacer(modifier = Modifier.height(14.dp))
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
                                    } catch (e: Exception) {
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
                        val totalTests = filteredTestRecords.size
                        val pctScores = filteredTestRecords.map { if (it.totalMarks > 0) (it.marksObtained / it.totalMarks) * 100f else 0f }
                        val overallAverage = if (pctScores.isNotEmpty()) pctScores.average().toFloat() else 0f
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
                                    text = "No tests recorded for this selection.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            // 3 Minimalist KPI Capsules
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StreakStatColumn(
                                    label = "Average",
                                    value = "${overallAverage.toInt()}%",
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                StreakStatColumn(
                                    label = "Tests",
                                    value = "$totalTests",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                StreakStatColumn(
                                    label = "Pass Rate",
                                    value = "$passRate%",
                                    color = if (passRate >= 75) Color(0xFF34C759) else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Score Tier Distribution Histogram
                            Text(
                                text = "Score Distribution",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val tierA = pctScores.count { it >= 90f }
                            val tierB = pctScores.count { it in 75f..89.99f }
                            val tierC = pctScores.count { it in 50f..74.99f }
                            val tierD = pctScores.count { it < 50f }
                            val maxTierCount = maxOf(tierA, tierB, tierC, tierD, 1).toFloat()

                            val tierData = listOf(
                                Triple("90-100%", tierA, Color(0xFF34C759)),
                                Triple("75-89%", tierB, MaterialTheme.colorScheme.primary),
                                Triple("50-74%", tierC, Color(0xFFFF9500)),
                                Triple("<50%", tierD, MaterialTheme.colorScheme.error)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(96.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                tierData.forEach { (label, count, color) ->
                                    val heightFrac = (count / maxTierCount).coerceIn(0.10f, 1f)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "$count",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (count > 0) color else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(28.dp)
                                                .fillMaxHeight(heightFrac)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (count > 0) {
                                                        Brush.verticalGradient(
                                                            listOf(color, color.copy(alpha = 0.50f))
                                                        )
                                                    } else {
                                                        Brush.verticalGradient(
                                                            listOf(
                                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                                            )
                                                        )
                                                    }
                                                )
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Subject Scores Breakdown
                            if (subjects.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "Subject Performance",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                            val statusColor = when {
                                                subjAvg >= 85f -> Color(0xFF34C759)
                                                subjAvg >= 70f -> MaterialTheme.colorScheme.primary
                                                subjAvg >= 50f -> Color(0xFFFF9500)
                                                else -> MaterialTheme.colorScheme.error
                                            }

                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = subj.name,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = statusColor.copy(alpha = 0.12f)
                                                    ) {
                                                        Text(
                                                            text = "${subjAvg.toInt()}%",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = statusColor,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                LinearProgressIndicator(
                                                    progress = { subjAvg / 100f },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(5.dp)
                                                        .clip(RoundedCornerShape(3.dp)),
                                                    color = statusColor,
                                                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f)
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

        // Minimalist Focus Goal & Task Completion Visual Cards
        item(key = "indicator_focus_progress") {
            val isDark = isSystemInDarkTheme()
            val priColor = MaterialTheme.colorScheme.primary
            val weeklyFocusGoalMins = 600
            val weeklyProgress = (periodFocusMins.toFloat() / weeklyFocusGoalMins).coerceIn(0f, 1f)

            ScholarCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .blurAura(color = priColor, alpha = if (isDark) 0.12f else 0.07f),
                shape = RoundedCornerShape(22.dp),
                border = frostedSpecularBorder(accentColor = priColor, isDark = isDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(priColor.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Timer,
                                    contentDescription = null,
                                    tint = priColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Focus Goal",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${pomodoroSessions.size} Sessions Logged",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = periodFocusFormatted,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = priColor
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { weeklyProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = priColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f)
                    )
                }
            }
        }

        item(key = "indicator_task_completion") {
            val isDark = isSystemInDarkTheme()
            val tertColor = MaterialTheme.colorScheme.tertiary
            val completionFraction = if (totalAssignments > 0) completedAssignments.toFloat() / totalAssignments else 0f

            ScholarCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .blurAura(color = tertColor, alpha = if (isDark) 0.12f else 0.07f),
                shape = RoundedCornerShape(22.dp),
                border = frostedSpecularBorder(accentColor = tertColor, isDark = isDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(tertColor.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = tertColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Task Completion",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$completedAssignments of $totalAssignments done",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "$assignmentRate%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = tertColor
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { completionFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = tertColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f)
                    )
                }
            }
        }

        // Recent Activity History Section
        if (showActionHistory) {
            item(key = "activity_log_header") {
                val isDark = isSystemInDarkTheme()
                val priColor = MaterialTheme.colorScheme.primary

                ScholarCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    border = frostedSpecularBorder(accentColor = priColor, isDark = isDark)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
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
                                    .padding(vertical = 16.dp),
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
                                actionLogs.take(20).forEach { log ->
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
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

/**
 * Visual attendance indicator pill with glowing accent dot.
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
